package com.github.fengdai.registry.compiler;

import com.google.auto.common.AnnotationMirrors;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.sun.source.util.Trees;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType;

import static com.github.fengdai.registry.compiler.Utils.inferSuperTypeArgument;
import static java.util.Objects.requireNonNull;
import static javax.tools.Diagnostic.Kind.ERROR;

@AutoService(Processor.class)
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
public final class RegistryProcessor extends AbstractProcessor {
  private static final String REGISTER_TYPE = "com.github.fengdai.registry.Register";
  private static final String REGISTER_VIEW_HOLDER_TYPE =
      "com.github.fengdai.registry.Register.ViewHolder";

  private static final String ANDROID_VIEW = "android.view.View";
  private static final String ANDROID_VIEW_HOLDER =
      "android.support.v7.widget.RecyclerView.ViewHolder";

  private static final String INFLATE_TYPE = "com.github.fengdai.registry.Inflate";
  private static final String BINDABLE_VIEW_HOLDER =
      "com.github.fengdai.registry.BindableViewHolder";
  private static final String BINDER = "com.github.fengdai.registry.Binder";

  private Elements elementUtils;
  private Types typesUtils;
  private Filer filer;
  private Trees trees;

  private TypeMirror androidViewType;
  private TypeMirror androidViewHolderType;

  private final RScanner rScanner = new RScanner();

  @Override public synchronized void init(ProcessingEnvironment env) {
    super.init(env);
    elementUtils = env.getElementUtils();
    typesUtils = env.getTypeUtils();
    filer = env.getFiler();
    try {
      trees = Trees.instance(processingEnv);
    } catch (IllegalArgumentException ignored) {
      try {
        // Get original ProcessingEnvironment from Gradle-wrapped one or KAPT-wrapped one.
        for (Field field : processingEnv.getClass().getDeclaredFields()) {
          if (field.getName().equals("delegate") || field.getName().equals("processingEnv")) {
            field.setAccessible(true);
            ProcessingEnvironment javacEnv = (ProcessingEnvironment) field.get(processingEnv);
            trees = Trees.instance(javacEnv);
            break;
          }
        }
      } catch (Throwable ignored2) {
      }
    }

    androidViewType = elementUtils.getTypeElement(ANDROID_VIEW).asType();
    androidViewHolderType = elementUtils.getTypeElement(ANDROID_VIEW_HOLDER).asType();
  }

  @Override public Set<String> getSupportedAnnotationTypes() {
    return ImmutableSet.<String>builder()
        .add(REGISTER_TYPE)
        .add(REGISTER_VIEW_HOLDER_TYPE)
        .build();
  }

  @Override public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
    Map<TypeElement, ViewHolderFactoryClass> viewHolderFactoryClasses =
        parseViewHolderFactory(env);
    for (Map.Entry<TypeElement, ViewHolderFactoryClass> entry : viewHolderFactoryClasses.entrySet()) {
      TypeElement element = entry.getKey();
      ViewHolderFactoryClass registryClass = entry.getValue();
      try {
        registryClass.brewJava().writeTo(filer);
      } catch (IOException e) {
        error(element, "Unable to write Factory for %s: %s", element, e.getMessage());
      }
    }

    Map<TypeElement, RegistryClass> registryClasses = parseRegistry(env);
    for (Map.Entry<TypeElement, RegistryClass> entry : registryClasses.entrySet()) {
      TypeElement element = entry.getKey();
      RegistryClass registryClass = entry.getValue();
      try {
        registryClass.brewJava().writeTo(filer);
      } catch (IOException e) {
        error(element, "Unable to write Registry for %s: %s", element, e.getMessage());
      }
    }
    return true;
  }

  private Map<TypeElement, ViewHolderFactoryClass> parseViewHolderFactory(RoundEnvironment env) {
    Map<TypeElement, ViewHolderInfo> viewHolders = new LinkedHashMap<>();
    Map<TypeElement, ViewHolderFactoryClass> viewHolderFactoryClassMap = new LinkedHashMap<>();
    for (Element element : env.getElementsAnnotatedWith(
        elementUtils.getTypeElement(REGISTER_VIEW_HOLDER_TYPE))) {
      if (typesUtils.isSubtype(element.asType(), androidViewHolderType)) {
        TypeElement viewHolderElement = (TypeElement) element;
        ViewHolderInfo viewHolderInfo = viewHolders.get(viewHolderElement);
        if (viewHolderInfo == null) {
          viewHolderInfo = createViewHolderInfo(viewHolderElement);
          viewHolders.put(viewHolderElement, viewHolderInfo);
        }
        viewHolderFactoryClassMap.put(viewHolderElement,
            new ViewHolderFactoryClass(viewHolderInfo));
      } else {
        error(element, "@Register.ViewHolder can only be applied to subclass of %s.",
            ANDROID_VIEW_HOLDER);
      }
    }
    return viewHolderFactoryClassMap;
  }

  private Map<TypeElement, RegistryClass> parseRegistry(RoundEnvironment env) {
    Map<TypeElement, RegistryClass> registryClassMap = new LinkedHashMap<>();
    for (Element element : env.getElementsAnnotatedWith(
        elementUtils.getTypeElement(REGISTER_TYPE))) {
      // TODO validation

      TypeElement annotationElement = (TypeElement) element;
      AnnotationMirror register = getAnnotationMirror(annotationElement, REGISTER_TYPE);

      Map<TypeMirror, MappingInfo> mappings = new LinkedHashMap<>();
      Map<TypeElement, IndexedViewHolderInfo> viewHolders = new LinkedHashMap<>();

      List<TypeElement> binderElements = getAnnotationElements(register, "binders");
      for (TypeElement binderElement : binderElements) {
        parseBinder(binderElement, mappings, viewHolders);
      }

      List<TypeElement> bindableViewHolderElements =
          getAnnotationElements(register, "bindableViewHolders");
      for (TypeElement bindableViewHolderElement : bindableViewHolderElements) {
        parseBindableViewHolder(bindableViewHolderElement, mappings, viewHolders);
      }

      List<Integer> staticContentLayouts =
          ((List<Attribute.Constant>) AnnotationMirrors.getAnnotationValue(register,
              "staticContentLayouts").getValue())
              .stream().map(it -> (Integer) it.value).collect(Collectors.toList());

      RegistryClass registryClass =
          new RegistryClass(annotationElement, mappings.values(), viewHolders.values(),
              elementToIds(annotationElement, register, staticContentLayouts).values());
      registryClassMap.put(annotationElement, registryClass);
    }
    return registryClassMap;
  }

  private void parseBindableViewHolder(TypeElement bindableViewHolderElement,
      Map<TypeMirror, MappingInfo> mappings,
      Map<TypeElement, IndexedViewHolderInfo> viewHolders) {
    // TODO validation

    TypeMirror dataType = typesUtils.erasure(
        inferSuperTypeArgument(bindableViewHolderElement, BINDABLE_VIEW_HOLDER, false, 0));

    getMapping(mappings, dataType).addBindableViewHolderInfo(
        getOrCreateIndexViewHolderInfo(bindableViewHolderElement, viewHolders));
  }

  private void parseBinder(TypeElement binderElement,
      Map<TypeMirror, MappingInfo> mappings,
      Map<TypeElement, IndexedViewHolderInfo> viewHolders) {
    // TODO validation

    TypeMirror dataType = typesUtils.erasure(
        inferSuperTypeArgument(binderElement, BINDER, false, 0));
    TypeMirror viewHolderType = typesUtils.erasure(
        inferSuperTypeArgument(binderElement, BINDER, false, 1));

    TypeElement dataElement = (TypeElement) typesUtils.asElement(dataType);
    TypeElement viewHolderElement = (TypeElement) typesUtils.asElement(viewHolderType);
    IndexedViewHolderInfo indexedViewHolderInfo =
        getOrCreateIndexViewHolderInfo(viewHolderElement, viewHolders);
    BinderInfo binderInfo =
        new BinderInfo(binderElement, dataElement, viewHolderElement, indexedViewHolderInfo);

    getMapping(mappings, dataType).addBinderInfo(binderInfo);
  }

  private MappingInfo getMapping(Map<TypeMirror, MappingInfo> mappings, TypeMirror dataType) {
    MappingInfo mapping = mappings.get(dataType);
    if (mapping == null) {
      mapping = new MappingInfo((TypeElement) typesUtils.asElement(dataType));
      mappings.put(dataType, mapping);
    }
    return mapping;
  }

  private IndexedViewHolderInfo getOrCreateIndexViewHolderInfo(TypeElement viewHolderElement,
      Map<TypeElement, IndexedViewHolderInfo> viewHolders) {
    IndexedViewHolderInfo indexedViewHolderInfo = viewHolders.get(viewHolderElement);
    if (indexedViewHolderInfo == null) {
      indexedViewHolderInfo =
          new IndexedViewHolderInfo(viewHolders.size(), createViewHolderInfo(viewHolderElement));
      viewHolders.put(viewHolderElement, indexedViewHolderInfo);
    }
    return indexedViewHolderInfo;
  }

  private ViewHolderInfo createViewHolderInfo(TypeElement viewHolderElement) {
    List<ExecutableElement> constructors =
        ElementFilter.constructorsIn(elementUtils.getAllMembers(viewHolderElement));

    if (constructors.size() == 0) {
      error(viewHolderElement, "Missing default constructor.");
    } else if (constructors.size() > 1) {
      error(viewHolderElement, "More than one constructor.");
    }

    ExecutableElement constructorElement = constructors.get(0);
    List<? extends VariableElement> parameterElements = constructorElement.getParameters();
    Collection<ViewHolderInfo.Dependency> dependencies = new LinkedList<>();
    for (VariableElement parameterElement : parameterElements) {
      AnnotationMirror inflateAnnotation = getAnnotationMirror(parameterElement, INFLATE_TYPE);
      InflateLayout inflateLayout = null;
      if (inflateAnnotation != null) {
        TypeMirror viewType = parameterElement.asType();
        if (!typesUtils.isAssignable(viewType, androidViewType)) {
          error(parameterElement, "%s must be %s or a subtype of %s.", viewType, ANDROID_VIEW,
              ANDROID_VIEW);
        }
        int id = (int) AnnotationMirrors.getAnnotationValue(inflateAnnotation, "value").getValue();
        Id layoutRes = elementToId(parameterElement, inflateAnnotation, id);
        inflateLayout = new InflateLayout(layoutRes, viewType);
      }
      dependencies.add(new ViewHolderInfo.Dependency(parameterElement, inflateLayout));
    }
    return new ViewHolderInfo(viewHolderElement, dependencies);
  }

  private void error(Element element, String message, Object... args) {
    if (args.length > 0) {
      message = String.format(message, args);
    }
    processingEnv.getMessager().printMessage(ERROR, message, element);
  }

  private List<TypeElement> getAnnotationElements(AnnotationMirror annotationMirror,
      final String elementName) {
    // noinspection unchecked
    List<? extends AnnotationValue> listValue =
        (List<? extends AnnotationValue>) AnnotationMirrors.getAnnotationValue(annotationMirror,
            elementName).getValue();
    return listValue.stream()
        .map(typeValue -> (TypeElement) typesUtils.asElement((TypeMirror) typeValue.getValue()))
        .collect(Collectors.toList());
  }

  private Id elementToId(Element element, AnnotationMirror annotationMirror, int value) {
    JCTree tree =
        (JCTree) trees.getTree(element, annotationMirror);
    if (tree != null) { // tree can be null if the references are compiled types and not source
      rScanner.reset();
      tree.accept(rScanner);
      if (!rScanner.resourceIds.isEmpty()) {
        return rScanner.resourceIds.values().iterator().next();
      }
    }
    return new Id(value);
  }

  private Map<Integer, Id> elementToIds(Element element, AnnotationMirror annotationMirror,
      Iterable<Integer> values) {
    Map<Integer, Id> resourceIds = new LinkedHashMap<>();
    JCTree tree = (JCTree) trees.getTree(element, annotationMirror);
    if (tree != null) { // tree can be null if the references are compiled types and not source
      rScanner.reset();
      tree.accept(rScanner);
      resourceIds = rScanner.resourceIds;
    }

    // Every value looked up should have an Id
    for (int value : values) {
      resourceIds.putIfAbsent(value, new Id(value));
    }
    return resourceIds;
  }

  private static AnnotationMirror getAnnotationMirror(Element element, String annotationClassName) {
    for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
      if (annotationMirror.getAnnotationType().toString().contentEquals(annotationClassName)) {
        return annotationMirror;
      }
    }
    return null;
  }

  private static class RScanner extends TreeScanner {
    Map<Integer, Id> resourceIds = new LinkedHashMap<>();

    @Override public void visitSelect(JCTree.JCFieldAccess jcFieldAccess) {
      Symbol symbol = jcFieldAccess.sym;
      if (symbol.getEnclosingElement() != null
          && symbol.getEnclosingElement().getEnclosingElement() != null
          && symbol.getEnclosingElement().getEnclosingElement().enclClass() != null) {
        try {
          int value = (Integer) requireNonNull(((Symbol.VarSymbol) symbol).getConstantValue());
          resourceIds.put(value, new Id(value, symbol));
        } catch (Exception ignored) {
        }
      }
    }

    @Override public void visitLiteral(JCTree.JCLiteral jcLiteral) {
      try {
        int value = (Integer) jcLiteral.value;
        resourceIds.put(value, new Id(value));
      } catch (Exception ignored) {
      }
    }

    void reset() {
      resourceIds.clear();
    }
  }
}
