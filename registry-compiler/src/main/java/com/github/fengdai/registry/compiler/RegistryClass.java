package com.github.fengdai.registry.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import com.sun.tools.javac.util.Pair;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import static com.google.auto.common.MoreElements.getPackage;

final class RegistryClass {
  private static final String REGISTRY_IMPL_SUFFIX = "Registry";
  private static final String ITEM_IMPL_NAME = "Item";

  private static final ClassName ILLEGAL_ARGUMENT_EXCEPTION =
      ClassName.get(IllegalArgumentException.class);
  private static final ClassName ANDROID_VIEW_HOLDER =
      ClassName.get("android.support.v7.widget", "RecyclerView", "ViewHolder");
  private static final ParameterizedTypeName ANDROID_VIEW_HOLDER_CLASS =
      ParameterizedTypeName.get(ClassName.get(Class.class),
          WildcardTypeName.subtypeOf(ANDROID_VIEW_HOLDER));

  private static final ClassName REGISTRY =
      ClassName.get("com.github.fengdai.registry", "Registry");
  private static final ClassName ITEM =
      ClassName.get("com.github.fengdai.registry", "Registry", "Item");
  private static final ClassName BINDER =
      ClassName.get("com.github.fengdai.registry", "Binder");

  private final Element originatingElement;
  private final ClassName annotation;
  private final String classPackage;
  private final String className;
  private final Collection<BindingSet> bindingSets;
  private final Collection<Pair<Integer, ViewHolderInfo>> indexedViewHolders;
  private final Collection<Pair<Integer, Id>> indexedStaticContentLayouts;

  RegistryClass(TypeElement annotationElement, Collection<BindingSet> bindingSets,
      Collection<Pair<Integer, ViewHolderInfo>> indexedViewHolders,
      Collection<Id> staticContentLayouts) {
    this.originatingElement = annotationElement;
    this.annotation = ClassName.get(annotationElement);
    this.classPackage = getPackage(annotationElement).getQualifiedName().toString();
    this.className = annotationElement.getQualifiedName()
        .toString()
        .substring(classPackage.length() + 1)
        .replace('.', '$') + REGISTRY_IMPL_SUFFIX;
    this.bindingSets = bindingSets;
    this.indexedViewHolders = indexedViewHolders;

    int staticContentLayoutIndex = indexedViewHolders.size();
    this.indexedStaticContentLayouts = new ArrayList<>(staticContentLayouts.size());
    for (Id layout : staticContentLayouts) {
      this.indexedStaticContentLayouts.add(new Pair<>(staticContentLayoutIndex, layout));
      staticContentLayoutIndex++;
    }
  }

  JavaFile brewJava() {
    return JavaFile.builder(classPackage, buildRegistryImpl())
        .addFileComment("Generated code from Registry. Do not modify!")
        .build();
  }

  private TypeSpec buildRegistryImpl() {
    TypeSpec.Builder builder = TypeSpec.classBuilder(className)
        .addOriginatingElement(originatingElement)
        .addAnnotation(annotation)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

    ClassName itemClassName = ClassName.get(classPackage, className, ITEM_IMPL_NAME);
    builder.addType(itemClass(itemClassName));
    builder.superclass(ParameterizedTypeName.get(REGISTRY, itemClassName));

    for (BindingSet bindingSet : bindingSets) {
      builder.addMethod(itemOfMethod(itemClassName, bindingSet));
    }

    for (Pair<Integer, Id> indexedStaticContentLayout : indexedStaticContentLayouts) {
      int viewType = indexedStaticContentLayout.fst;
      Id layout = indexedStaticContentLayout.snd;
      FieldSpec staticContentLayoutItemField =
          FieldSpec.builder(itemClassName, "ITEM_" + layout.resourceName)
              .addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
              .initializer("new $T(staticContentLayoutData($L), $L, null)", itemClassName,
                  layout.code, viewType)
              .build();
      MethodSpec staticContentLayoutItemGetter =
          MethodSpec.methodBuilder("itemOf_" + layout.resourceName)
              .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
              .returns(itemClassName)
              .addStatement("return $N", staticContentLayoutItemField)
              .build();
      builder.addField(staticContentLayoutItemField);
      builder.addMethod(staticContentLayoutItemGetter);
    }

    // Constructor
    builder.addMethod(constructor(indexedViewHolders, indexedStaticContentLayouts));
    return builder.build();
  }

  private MethodSpec itemOfMethod(ClassName itemClassName, BindingSet bindingSet) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("itemOf")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .addParameter(ParameterSpec.builder(bindingSet.dataClassName, "data").build())
        .returns(itemClassName);
    if (!bindingSet.isMultiBinding) {
      Binding binding = bindingSet.bindings.iterator().next();
      builder.addStatement("return new $T(data, $L, $L)", itemClassName, binding.viewType,
          binding.instanceCode);
    } else {
      builder.addParameter(
          ParameterSpec.builder(ANDROID_VIEW_HOLDER_CLASS, "viewHolderClass").build());

      int i = 0;
      for (Binding binding : bindingSet.bindings) {
        CodeBlock conditionCode =
            CodeBlock.of("viewHolderClass == $T.class", binding.viewHolderClassName);
        if (i == 0) {
          builder.beginControlFlow("if ($L)", conditionCode);
        } else {
          builder.nextControlFlow("else if ($L)", conditionCode);
        }
        builder.addStatement("return new $T(data, $L, $L)", itemClassName, binding.viewType,
            binding.instanceCode);
        i++;
      }
      builder.nextControlFlow("else")
          .addStatement("throw new $T($S)", ILLEGAL_ARGUMENT_EXCEPTION, "viewHolderClass")
          .endControlFlow();
    }
    return builder.build();
  }

  private static MethodSpec constructor(
      Collection<Pair<Integer, ViewHolderInfo>> indexedViewHolders,
      Collection<Pair<Integer, Id>> indexedStaticContentLayouts) {
    MethodSpec.Builder builder = MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addStatement("super()");

    Collection<ParameterSpec> factoryParameters = new LinkedList<>();

    // registerViewHolderFactory
    for (Pair<Integer, ViewHolderInfo> indexedViewHolder : indexedViewHolders) {
      int viewType = indexedViewHolder.fst;
      ViewHolderInfo viewHolder = indexedViewHolder.snd;
      ClassName factoryImplClassName = viewHolder.factoryImplClassName;
      CodeBlock factoryInstanceCode;
      if (!viewHolder.unresolvedDependencies.isEmpty()) {
        ParameterSpec factoryParameter = ParameterSpec.builder(factoryImplClassName,
            "factory" + viewType).build();
        factoryParameters.add(factoryParameter);
        factoryInstanceCode = CodeBlock.of("$N", factoryParameter);
      } else {
        factoryInstanceCode = CodeBlock.of("new $T()", factoryImplClassName);
      }
      builder.addStatement("registerViewHolderFactory($L, $L)", viewType, factoryInstanceCode);
    }

    // registerStaticContentLayout
    for (Pair<Integer, Id> indexedStaticContentLayout : indexedStaticContentLayouts) {
      int viewType = indexedStaticContentLayout.fst;
      Id layout = indexedStaticContentLayout.snd;
      builder.addStatement("registerStaticContentLayout($L, $L)", viewType, layout.code);
    }

    builder.addParameters(
        factoryParameters.stream()
            .sorted((p0, p1) -> ((ClassName) p0.type).simpleName()
                .compareTo(((ClassName) p1.type).simpleName()))
            .collect(Collectors.toList()));
    return builder.build();
  }

  private static TypeSpec itemClass(ClassName name) {
    return TypeSpec.classBuilder(name)
        .superclass(ITEM)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC)
        .addMethod(MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PRIVATE)
            .addParameter(ParameterSpec.builder(TypeName.OBJECT, "data").build())
            .addParameter(ParameterSpec.builder(TypeName.INT, "viewType").build())
            .addParameter(ParameterSpec.builder(BINDER, "binder").build())
            .addStatement("super(data, viewType, binder)")
            .build())
        .build();
  }
}
