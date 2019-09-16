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
import java.util.ArrayList;
import java.util.Collection;
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
  private final Collection<MappingInfo> mappingInfoList;
  private final Collection<IndexedViewHolderInfo> indexedViewHolders;
  private final Collection<StaticContentLayout> staticContentLayouts;

  RegistryClass(TypeElement annotationElement, Collection<MappingInfo> mappingInfoList,
      Collection<IndexedViewHolderInfo> indexedViewHolders,
      Collection<Id> staticContentLayouts) {
    this.originatingElement = annotationElement;
    this.annotation = ClassName.get(annotationElement);
    this.classPackage = getPackage(annotationElement).getQualifiedName().toString();
    this.className = annotationElement.getQualifiedName()
        .toString()
        .substring(classPackage.length() + 1)
        .replace('.', '$') + REGISTRY_IMPL_SUFFIX;
    this.mappingInfoList = mappingInfoList;
    this.indexedViewHolders = indexedViewHolders;

    int staticContentLayoutIndex = indexedViewHolders.size();
    this.staticContentLayouts = new ArrayList<>(staticContentLayouts.size());
    for (Id layout : staticContentLayouts) {
      this.staticContentLayouts.add(new StaticContentLayout(staticContentLayoutIndex, layout));
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

    for (MappingInfo mappingInfo : mappingInfoList) {
      builder.addMethod(itemOfMethod(itemClassName, mappingInfo));
    }

    for (StaticContentLayout layout : staticContentLayouts) {
      FieldSpec staticContentLayoutItemField =
          FieldSpec.builder(itemClassName, "ITEM_" + layout.id.resourceName)
              .addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
              .initializer("new $T(staticContentLayoutData($L), $L, null)", itemClassName,
                  layout.id.code,
                  layout.viewType)
              .build();
      MethodSpec staticContentLayoutItemGetter =
          MethodSpec.methodBuilder("itemOf_" + layout.id.resourceName)
              .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
              .returns(itemClassName)
              .addStatement("return $N", staticContentLayoutItemField)
              .build();
      builder.addField(staticContentLayoutItemField);
      builder.addMethod(staticContentLayoutItemGetter);
    }

    // Constructor
    builder.addMethod(constructor(indexedViewHolders, staticContentLayouts));
    return builder.build();
  }

  private MethodSpec itemOfMethod(ClassName itemClassName, MappingInfo mappingInfo) {
    MethodSpec.Builder builder = MethodSpec.methodBuilder("itemOf")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .addParameter(ParameterSpec.builder(mappingInfo.getDataClassName(), "data").build())
        .returns(itemClassName);
    if (!mappingInfo.isOneToMany()) {
      MappingInfo.Mapping mapping = mappingInfo.getMappings().get(0);
      if (mapping.binderInfo == null) {
        builder.addStatement("return new $T(data, $L, BINDABLE_VIEW_HOLDER_BINDER)",
            itemClassName, mapping.viewType);
      } else {
        builder.addStatement("return new $T(data, $L, new $T())",
            itemClassName, mapping.viewType, mapping.binderInfo.binderClassName);
      }
    } else {
      builder.addParameter(
          ParameterSpec.builder(ANDROID_VIEW_HOLDER_CLASS, "viewHolderClass").build());

      int i = 0;
      for (MappingInfo.Mapping mapping : mappingInfo.getMappings()) {
        if (i == 0) {
          builder.beginControlFlow("if (viewHolderClass == $T.class)",
              mapping.viewHolderInfo.viewHolderClassName);
        } else {
          builder.nextControlFlow("else if (viewHolderClass == $T.class)",
              mapping.viewHolderInfo.viewHolderClassName);
        }
        if (mapping.binderInfo == null) {
          builder.addStatement("return new $T(data, $L, BINDABLE_VIEW_HOLDER_BINDER)",
              itemClassName, mapping.viewType);
        } else {
          builder.addStatement("return new $T(data, $L, new $T())",
              itemClassName, mapping.viewType, mapping.binderInfo.binderClassName);
        }
        i++;
      }
      builder.nextControlFlow("else")
          .addStatement("throw new $T($S)", ILLEGAL_ARGUMENT_EXCEPTION, "viewHolderClass")
          .endControlFlow();
    }
    return builder.build();
  }

  private static MethodSpec constructor(Collection<IndexedViewHolderInfo> indexedViewHolders,
      Collection<StaticContentLayout> staticContentLayouts) {
    MethodSpec.Builder builder = MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addStatement("super()");

    // registerViewHolderFactory
    for (IndexedViewHolderInfo viewHolder : indexedViewHolders) {
      ParameterSpec factoryParameter = null;
      if (!viewHolder.info.unresolvedDependencies.isEmpty()) {
        factoryParameter = ParameterSpec.builder(viewHolder.info.factoryImplClassName,
            "factory" + viewHolder.index).build();
        builder.addParameter(factoryParameter);
      }
      builder.addStatement(registerViewHolderFactory(viewHolder, factoryParameter));
    }

    // registerStaticContentLayout
    for (StaticContentLayout layout : staticContentLayouts) {
      builder.addStatement(registerStaticContentLayout(layout));
    }

    return builder.build();
  }

  private static CodeBlock registerViewHolderFactory(IndexedViewHolderInfo viewHolder,
      ParameterSpec factoryParameter) {
    CodeBlock factory = factoryParameter != null
        ? CodeBlock.of("$N", factoryParameter)
        : CodeBlock.of("new $T()", viewHolder.info.factoryImplClassName);
    return CodeBlock.of("registerViewHolderFactory($L, $L)", viewHolder.index, factory);
  }

  private static CodeBlock registerStaticContentLayout(StaticContentLayout layout) {
    return CodeBlock.of("registerStaticContentLayout($L, $L)", layout.viewType, layout.id.code);
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
