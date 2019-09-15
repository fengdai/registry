package com.github.fengdai.registry.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import java.util.Collection;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;

final class ViewHolderFactoryClass {
  private static final ClassName VIEW_GROUP = ClassName.get("android.view", "ViewGroup");
  private static final ClassName VIEW_HOLDER =
      ClassName.get("android.support.v7.widget", "RecyclerView", "ViewHolder");
  private static final ClassName VIEW_HOLDER_FACTORY =
      ClassName.get("com.github.fengdai.registry", "ViewHolderFactory");

  private final ViewHolderInfo viewHolderInfo;

  ViewHolderFactoryClass(ViewHolderInfo viewHolderInfo) {
    this.viewHolderInfo = viewHolderInfo;
  }

  JavaFile brewJava() {
    TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(viewHolderInfo.factoryImplClassName)
        .addOriginatingElement(viewHolderInfo.viewHolderElement)
        .addSuperinterface(VIEW_HOLDER_FACTORY)
        .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
    // constructor
    MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder();
    for (ViewHolderInfo.Dependency unresolvedDependency : viewHolderInfo.unresolvedDependencies) {
      FieldSpec field = FieldSpec.builder(unresolvedDependency.typeName, unresolvedDependency.parameterSpec.name)
          .addModifiers(Modifier.PRIVATE, Modifier.FINAL).build();
      typeBuilder.addField(field);
      constructorBuilder.addParameter(unresolvedDependency.parameterSpec);
      constructorBuilder.addStatement("this.$N = $N", field, unresolvedDependency.parameterSpec);
    }
    typeBuilder.addMethod(constructorBuilder.addModifiers(Modifier.PUBLIC).build());
    // create method
    Collection<CodeBlock> dependencies = viewHolderInfo.dependencies.stream()
        .map(it -> it.instanceCode)
        .collect(Collectors.toList());
    MethodSpec.Builder createMethodBuilder = MethodSpec.methodBuilder("create")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(VIEW_HOLDER)
        .addParameter(ParameterSpec.builder(VIEW_GROUP, "parent").build())
        .addStatement("return new $T($L)", viewHolderInfo.viewHolderClassName,
            CodeBlock.join(dependencies, ", "));
    typeBuilder.addMethod(createMethodBuilder.build());
    return JavaFile.builder(viewHolderInfo.factoryImplClassName.packageName(), typeBuilder.build())
        .addFileComment("Generated code from Registry. Do not modify!")
        .build();
  }
}
