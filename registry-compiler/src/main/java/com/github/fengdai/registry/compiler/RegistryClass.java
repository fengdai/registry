package com.github.fengdai.registry.compiler;

import com.github.fengdai.registry.internal.Model;
import com.github.fengdai.registry.internal.RegistryImpl;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.Modifier;

final class RegistryClass {
  private static final ClassName REGISTRY_IMPL = ClassName.get(RegistryImpl.class);

  private final String classPackage;
  private final String className;
  private final int viewTypeCount;
  private final List<Binding> bindings = new LinkedList<>();

  RegistryClass(String classPackage, String className, int viewTypeCount) {
    this.classPackage = classPackage;
    this.className = className;
    this.viewTypeCount = viewTypeCount;
  }

  void addBinding(Binding binding) {
    bindings.add(binding);
  }

  JavaFile brewJava() {
    TypeSpec.Builder result =
        TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC).superclass(REGISTRY_IMPL);
    result.addMethod(constructor());
    addCreateModelMethods(result);
    result.addMethod(createModelsMethod());
    return JavaFile.builder(classPackage, result.build())
        .addFileComment("Generated code from Registry. Do not modify!")
        .build();
  }

  private void addCreateModelMethods(TypeSpec.Builder result) {
    for (Binding binding : bindings) {
      MethodSpec method;
      if (binding instanceof ToOneBinding) {
        method = createToOneModelMethod((ToOneBinding) binding);
      } else {
        method = createToManyModelMethod((ToManyBinding) binding);
      }
      result.addMethod(method);
    }
  }

  private MethodSpec constructor() {
    return MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addStatement("super(createModels(), $L)", viewTypeCount)
        .build();
  }

  private MethodSpec createToOneModelMethod(ToOneBinding binding) {
    MethodSpec.Builder result = buildCreateModelMethod(binding);
    result.addStatement("$T<$T> builder = Model.oneToOne($T.class)", Model.Builder.class,
        ClassName.get(binding.getModelType()), ClassName.get(binding.getModelType()));
    ItemViewClass itemViewClass = binding.getItemViewClass();
    addItemView(itemViewClass, result);
    result.addStatement("return builder.build()");
    return result.build();
  }

  private MethodSpec createToManyModelMethod(ToManyBinding binding) {
    MethodSpec.Builder result = buildCreateModelMethod(binding);
    result.addStatement("$T<$T> builder = Model.oneToMany($T.class, $T.class)", Model.Builder.class,
        ClassName.get(binding.getModelType()), ClassName.get(binding.getModelType()),
        ClassName.get(binding.getMapperType()));
    for (ItemViewClass itemViewClass : binding.getItemViewClasses()) {
      addItemView(itemViewClass, result);
    }
    result.addStatement("return builder.build()");
    return result.build();
  }

  private static MethodSpec.Builder buildCreateModelMethod(Binding binding) {
    return MethodSpec.methodBuilder(createModelMethodName(binding))
        .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
        .returns(ParameterizedTypeName.get(ClassName.get(Model.class),
            ClassName.get(binding.getModelType())));
  }

  private void addItemView(ItemViewClass itemViewClass, MethodSpec.Builder result) {
    if (itemViewClass.isViewLayoutRes()) {
      result.addStatement("builder.add($L, $T.class, $L)", itemViewClass.getType(),
          ClassName.get(itemViewClass.getBinderType()), itemViewClass.getLayoutRes());
    } else {
      result.addStatement("builder.add($L, $T.class, $T.class)", itemViewClass.getType(),
          ClassName.get(itemViewClass.getBinderType()),
          ClassName.get(itemViewClass.getViewProviderType()));
    }
  }

  private MethodSpec createModelsMethod() {
    MethodSpec.Builder result =
        MethodSpec.methodBuilder("createModels").addModifiers(Modifier.PRIVATE, Modifier.STATIC).
            returns(ParameterizedTypeName.get(ClassName.get(Map.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                    WildcardTypeName.subtypeOf(TypeName.OBJECT)),
                ParameterizedTypeName.get(ClassName.get(Model.class),
                    WildcardTypeName.subtypeOf(TypeName.OBJECT))));
    result.addStatement("Map<Class<?>, Model<?>> map = new $T<>()", LinkedHashMap.class);
    for (Binding binding : bindings) {
      result.addStatement("map.put($T.class, $L())", ClassName.get(binding.getModelType()),
          createModelMethodName(binding));
    }
    result.addStatement("return map");
    return result.build();
  }

  private static String createModelMethodName(Binding binding) {
    return binding.getModelType().getQualifiedName().toString().replace('.', '_');
  }
}