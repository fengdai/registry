package com.github.fengdai.registry.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
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
import javax.lang.model.type.TypeMirror;

final class RegistryClass {
  private static final ClassName REGISTRY_IMPL =
      ClassName.get("com.github.fengdai.registry", "Registry");
  private static final ClassName MODEL = ClassName.get("com.github.fengdai.registry", "Model");

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
    ItemViewClass itemViewClass = binding.getItemViewClass();
    result.addStatement("return Model.oneToOne($L, new $T(), $L)", itemViewClass.getType(),
        itemViewClass.getBinderType(), itemViewClass.getLayoutRes());
    return result.build();
  }

  private MethodSpec createToManyModelMethod(ToManyBinding binding) {
    MethodSpec.Builder result = buildCreateModelMethod(binding);
    CodeBlock.Builder cbb = CodeBlock.builder();
    cbb.add("return Model.oneToMany(new $T())\n", binding.getMapperType());
    for (Map.Entry<TypeMirror, ItemViewClass> entry : binding.getItemViewClasses().entrySet()) {
      TypeMirror key = entry.getKey();
      ItemViewClass itemViewClass = entry.getValue();
      cbb.add("    .add($T.class, $L, new $T(), $L)\n", TypeName.get(key), itemViewClass.getType(),
          itemViewClass.getBinderType(), itemViewClass.getLayoutRes());
    }
    cbb.add("    .build();\n");
    result.addCode(cbb.build());
    return result.build();
  }

  private static MethodSpec.Builder buildCreateModelMethod(Binding binding) {
    return MethodSpec.methodBuilder(createModelMethodName(binding))
        .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
        .returns(ParameterizedTypeName.get(MODEL, binding.getModelType()));
  }

  private MethodSpec createModelsMethod() {
    MethodSpec.Builder result =
        MethodSpec.methodBuilder("createModels").addModifiers(Modifier.PRIVATE, Modifier.STATIC).
            returns(ParameterizedTypeName.get(ClassName.get(Map.class),
                ParameterizedTypeName.get(ClassName.get(Class.class),
                    WildcardTypeName.subtypeOf(TypeName.OBJECT)),
                ParameterizedTypeName.get(MODEL, WildcardTypeName.subtypeOf(TypeName.OBJECT))));
    result.addStatement("Map<Class<?>, Model<?>> map = new $T<>()", LinkedHashMap.class);
    for (Binding binding : bindings) {
      result.addStatement("map.put($T.class, $L())", binding.getModelType(),
          createModelMethodName(binding));
    }
    result.addStatement("return map");
    return result.build();
  }

  private static String createModelMethodName(Binding binding) {
    return binding.getModelType().toString().replace('.', '_');
  }
}
