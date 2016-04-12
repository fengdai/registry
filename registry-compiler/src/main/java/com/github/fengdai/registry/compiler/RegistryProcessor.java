package com.github.fengdai.registry.compiler;

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
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import static com.github.fengdai.registry.compiler.Utils.inferSuperTypeArgument;
import static com.google.auto.common.AnnotationMirrors.getAnnotationValue;

@AutoService(Processor.class)
public class RegistryProcessor extends AbstractProcessor {
  private static final String REGISTRY_CLASS_SUFFIX = "$$Registry";
  private static final String REGISTER_TYPE = "com.github.fengdai.registry.Register";
  private static final String LAYOUT_TYPE = "com.github.fengdai.registry.Layout";
  private static final String MAPPER_TYPE = "com.github.fengdai.registry.Mapper";
  private static final String VIEW_BINDER_TYPE = "com.github.fengdai.registry.ViewBinder";

  private Elements elementUtils;
  private Filer filer;

  @Override public synchronized void init(ProcessingEnvironment env) {
    super.init(env);
    elementUtils = env.getElementUtils();
    filer = env.getFiler();
  }

  @Override public Set<String> getSupportedAnnotationTypes() {
    Set<String> types = new LinkedHashSet<>();
    types.add(REGISTER_TYPE);
    return types;
  }

  @Override public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
    for (TypeElement annotation : annotations) {
      if (!REGISTER_TYPE.equals(annotation.asType().toString())) {
        continue;
      }
      Map<TypeElement, RegistryClass> registryClassMap = findAndParse(env, annotation);
      for (Map.Entry<TypeElement, RegistryClass> entry : registryClassMap.entrySet()) {
        TypeElement typeElement = entry.getKey();
        RegistryClass registryClass = entry.getValue();
        try {
          registryClass.brewJava().writeTo(filer);
        } catch (IOException e) {
          error(typeElement, "Unable to write Registry for %s: %s", typeElement, e.getMessage());
        }
      }
    }
    return true;
  }

  private Map<TypeElement, RegistryClass> findAndParse(RoundEnvironment env, TypeElement register) {
    Map<TypeElement, RegistryClass> registryClassMap = new LinkedHashMap<>();
    for (Element element : env.getElementsAnnotatedWith(register)) {
      TypeElement annotationElement = (TypeElement) element;
      // Find all Mappers.
      Map<TypeElement, TypeElement> mapperMap = findAllMappers(annotationElement);
      Map<TypeElement, Binding> bindingMap = new LinkedHashMap<>();
      List<Integer> viewTypes = new LinkedList<>();
      Set<? extends Element> binderElements = env.getElementsAnnotatedWith(annotationElement);
      for (Element binderElement : binderElements) {
        parseBinder((TypeElement) binderElement, bindingMap, mapperMap, viewTypes);
      }
      RegistryClass registryClass = createRegistryClass(annotationElement, viewTypes.size());
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
      Map<TypeElement, TypeElement> mapperMap, List<Integer> viewTypes) {
    ItemViewClass itemViewClass;
    try {
      itemViewClass = parseItem(binderElement, viewTypes);
    } catch (Exception e) {
      return;
    }
    TypeElement modelType = inferSuperTypeArgument(binderElement, VIEW_BINDER_TYPE, 0);
    Binding binding = bindingMap.get(modelType);
    if (binding == null) {
      TypeElement mapperType = mapperMap.get(modelType);
      if (mapperType == null) {
        binding = new ToOneBinding(modelType, itemViewClass);
        bindingMap.put(modelType, binding);
      } else {
        ToManyBinding toManyBinding = new ToManyBinding(modelType, mapperType);
        toManyBinding.add(binderElement, itemViewClass);
        bindingMap.put(modelType, toManyBinding);
      }
    } else {
      if (binding instanceof ToOneBinding) {
        // TODO: 16/3/27 Error message.
        error(binderElement, "");
        return;
      }
      ToManyBinding toManyBinding = (ToManyBinding) binding;
      toManyBinding.add(binderElement, itemViewClass);
    }
  }

  private ItemViewClass parseItem(TypeElement binderType, List<Integer> viewTypes)
      throws Exception {
    AnnotationMirror layout = getAnnotationMirror(binderType, LAYOUT_TYPE);
    if (layout == null) {
      // TODO: 16/3/27 Error message.
      error(binderType, "Missing @%s annotation." + LAYOUT_TYPE);
      throw new AssertionError();
    }
    int layoutRes = (int) AnnotationMirrors.getAnnotationValue(layout, "value").getValue();
    if (!viewTypes.contains(layoutRes)) {
      viewTypes.add(layoutRes);
    }
    return new ItemViewClass(viewTypes.indexOf(layoutRes), binderType, layoutRes);
  }

  private Map<TypeElement, TypeElement> findAllMappers(Element annotationElement) {
    Map<TypeElement, TypeElement> mapperMap = new LinkedHashMap<>();
    AnnotationMirror register = getAnnotationMirror(annotationElement, REGISTER_TYPE);
    List<TypeElement> mappers = getAnnotationElements(register, "mappers");
    for (TypeElement mapper : mappers) {
      if (isValid(mapper)) {
        TypeElement modelType = inferSuperTypeArgument(mapper, MAPPER_TYPE, 0);
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

  static AnnotationMirror getAnnotationMirror(Element element, String annotationClassName) {
    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      TypeElement annotationTypeElement =
          MoreElements.asType(annotationMirror.getAnnotationType().asElement());
      if (annotationTypeElement.getQualifiedName().contentEquals(annotationClassName)) {
        return annotationMirror;
      }
    }
    return null;
  }

  private String getPackageName(TypeElement type) {
    return elementUtils.getPackageOf(type).getQualifiedName().toString();
  }

  private static String getClassName(TypeElement type, String packageName) {
    int packageLen = packageName.length() + 1;
    return type.getQualifiedName().toString().substring(packageLen).replace('.', '$');
  }
}
