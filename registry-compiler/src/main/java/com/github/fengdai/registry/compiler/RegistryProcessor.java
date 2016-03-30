package com.github.fengdai.registry.compiler;

import com.github.fengdai.registry.Item;
import com.github.fengdai.registry.Mapper;
import com.github.fengdai.registry.Register;
import com.github.fengdai.registry.ViewBinder;
import com.google.auto.common.AnnotationMirrors;
import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import static com.google.auto.common.AnnotationMirrors.getAnnotationValue;

@AutoService(Processor.class)
public class RegistryProcessor extends AbstractProcessor {
  private static final String REGISTRY_CLASS_SUFFIX = "$$Registry";

  private Elements elementUtils;
  private Filer filer;

  @Override public synchronized void init(ProcessingEnvironment env) {
    super.init(env);
    elementUtils = env.getElementUtils();
    filer = env.getFiler();
  }

  @Override public Set<String> getSupportedAnnotationTypes() {
    Set<String> types = new LinkedHashSet<>();
    types.add(Register.class.getCanonicalName());
    return types;
  }

  @Override public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
    Map<TypeElement, RegistryClass> registryClassMap = findAndParse(env);
    for (Map.Entry<TypeElement, RegistryClass> entry : registryClassMap.entrySet()) {
      TypeElement typeElement = entry.getKey();
      RegistryClass registryClass = entry.getValue();
      try {
        registryClass.brewJava().writeTo(filer);
      } catch (IOException e) {
        error(typeElement, "Unable to write Registry for %s: %s", typeElement, e.getMessage());
      }
    }
    return true;
  }

  private Map<TypeElement, RegistryClass> findAndParse(RoundEnvironment env) {
    Map<TypeElement, RegistryClass> registryClassMap = new LinkedHashMap<>();
    for (Element element : env.getElementsAnnotatedWith(Register.class)) {
      TypeElement annotationElement = (TypeElement) element;
      // Find all Mappers.
      Map<TypeElement, TypeElement> mapperMap = findAllMappers(annotationElement);
      Map<TypeElement, Binding> bindingMap = new LinkedHashMap<>();
      List<Object> viewType = new LinkedList<>();
      Set<? extends Element> binderElements = env.getElementsAnnotatedWith(annotationElement);
      for (Element binderElement : binderElements) {
        parseBinder((TypeElement) binderElement, bindingMap, mapperMap, viewType);
      }
      RegistryClass registryClass = createRegistryClass(annotationElement, viewType.size());
      for (Map.Entry<TypeElement, Binding> entry : bindingMap.entrySet()) {
        registryClass.addBinding(entry.getValue());
      }
      registryClassMap.put(annotationElement, registryClass);
    }
    return registryClassMap;
  }

  private RegistryClass createRegistryClass(TypeElement annotationElement, int viewTypeCount) {
    String packageName = getPackageName(annotationElement);
    String className = getClassName(annotationElement, packageName) + REGISTRY_CLASS_SUFFIX;
    return new RegistryClass(packageName, className, viewTypeCount);
  }

  private void parseBinder(TypeElement binderElement, Map<TypeElement, Binding> bindingMap,
      Map<TypeElement, TypeElement> mapperMap, List<Object> viewType) {
    ItemViewClass itemViewClass;
    try {
      itemViewClass = parseItem(binderElement, viewType);
    } catch (Exception e) {
      return;
    }
    TypeElement modelType = getInterfaceTypeArgument(binderElement, ViewBinder.class, 0);
    Binding binding = bindingMap.get(modelType);
    if (binding == null) {
      TypeElement mapperType = mapperMap.get(modelType);
      if (mapperType == null) {
        binding = new ToOneBinding(modelType, itemViewClass);
        bindingMap.put(modelType, binding);
      } else {
        ToManyBinding toManyBinding = new ToManyBinding(modelType, mapperType);
        toManyBinding.add(itemViewClass);
        bindingMap.put(modelType, toManyBinding);
      }
    } else {
      if (binding instanceof ToOneBinding) {
        // TODO: 16/3/27 Error message.
        error(binderElement, "");
        return;
      }
      ToManyBinding toManyBinding = (ToManyBinding) binding;
      toManyBinding.add(itemViewClass);
    }
  }

  private ItemViewClass parseItem(TypeElement binderType, List<Object> viewType) throws Exception {
    AnnotationMirror item = MoreElements.getAnnotationMirror(binderType, Item.class).get();
    if (item == null) {
      error(binderType, "Missing @%s annotation." + Item.class.getSimpleName());
      throw new AssertionError();
    }
    int layoutRes = (int) AnnotationMirrors.getAnnotationValue(item, "layout").getValue();
    TypeElement viewProviderType =
        (TypeElement) ((DeclaredType) AnnotationMirrors.getAnnotationValue(item, "view").getValue())
            .asElement();
    boolean specifiedViewProvider =
        !viewProviderType.getQualifiedName().contentEquals(Item.NONE.class.getCanonicalName());
    if (layoutRes == -1 && !specifiedViewProvider) {
      // TODO: 16/3/27 Error message.
      error(binderType, "");
      throw new AssertionError();
    } else if (layoutRes != -1 && specifiedViewProvider) {
      // TODO: 16/3/27 Error message.
      error(binderType, "");
      throw new AssertionError();
    }
    Object view = layoutRes != -1 ? layoutRes : viewProviderType;
    if (!viewType.contains(view)) {
      viewType.add(view);
    }
    return new ItemViewClass(viewType.indexOf(view), binderType, view);
  }

  private Map<TypeElement, TypeElement> findAllMappers(Element annotationElement) {
    Map<TypeElement, TypeElement> mapperMap = new LinkedHashMap<>();
    AnnotationMirror register =
        MoreElements.getAnnotationMirror(annotationElement, Register.class).get();
    List<TypeElement> mappers = getAnnotationElements(register, "mappers");
    for (TypeElement mapper : mappers) {
      if (isValid(mapper)) {
        TypeElement modelType = getInterfaceTypeArgument(mapper, Mapper.class, 0);
        if (mapperMap.get(modelType) != null) {
          // TODO: 16/3/27 Error message.
          error(annotationElement, "Conflict: ");
        }
        mapperMap.put(modelType, mapper);
      }
    }
    return mapperMap;
  }

  private void error(Element element, String message, Object... args) {
    if (args.length > 0) {
      message = String.format(message, args);
    }
    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
  }

  private boolean isValid(TypeElement mapper) {
    // TODO: 16/3/27
    return true;
  }

  static TypeElement getInterfaceTypeArgument(TypeElement element, Class<?> interfaceClass,
      int typeArgumentIndex) {
    if (!interfaceClass.isInterface()) {
      throw new AssertionError(
          String.format("%s isn't an interface.", interfaceClass.getSimpleName()));
    }
    for (TypeMirror mirror : element.getInterfaces()) {
      DeclaredType type = (DeclaredType) mirror;
      if (((TypeElement) type.asElement()).getQualifiedName()
          .contentEquals(interfaceClass.getCanonicalName())) {
        return (TypeElement) ((DeclaredType) type.getTypeArguments()
            .get(typeArgumentIndex)).asElement();
      }
    }
    throw new AssertionError();
  }

  static ImmutableList<TypeElement> getAnnotationElements(AnnotationMirror annotationMirror,
      final String elementName) {
    // noinspection unchecked That's the whole point of this method
    List<? extends AnnotationValue> listValue =
        (List<? extends AnnotationValue>) getAnnotationValue(annotationMirror,
            elementName).getValue();
    return FluentIterable.from(listValue).transform(new Function<AnnotationValue, TypeElement>() {
      @Override public TypeElement apply(AnnotationValue typeValue) {
        return (TypeElement) ((DeclaredType) typeValue.getValue()).asElement();
      }
    }).toList();
  }

  private String getPackageName(TypeElement type) {
    return elementUtils.getPackageOf(type).getQualifiedName().toString();
  }

  private String getFqcn(TypeElement typeElement) {
    String packageName = getPackageName(typeElement);
    return packageName + "." + getClassName(typeElement, packageName);
  }

  private static String getClassName(TypeElement type, String packageName) {
    int packageLen = packageName.length() + 1;
    return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
  }
}