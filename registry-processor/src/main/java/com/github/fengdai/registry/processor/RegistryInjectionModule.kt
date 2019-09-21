package com.github.fengdai.registry.processor

import com.github.fengdai.registry.processor.internal.applyEach
import com.github.fengdai.registry.processor.internal.peerClassWithReflectionNesting
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

private val VIEW_HOLDER_FACTORY = ClassName.get("com.github.fengdai.viewholder", "ViewHolderFactory")
private val PROVIDER = ClassName.get("javax.inject", "Provider")
private val MODULE = ClassName.get("dagger", "Module")
private val BINDS = ClassName.get("dagger", "Binds")
private val PROVIDES = ClassName.get("dagger", "Provides")
private val VIEW_HOLDER_FACTORIES =
  ClassName.get("com.github.fengdai.registry.internal", "ViewHolderFactories")

data class RegistryInjectionModule(
  val annotation: ClassName,
  val public: Boolean,
  val viewHolderInjections: List<ClassName>,
  val javaxInjections: List<ClassName>
) {
  val generatedType = annotation.registryInjectionModuleName()

  fun brewJava(): TypeSpec {
    return TypeSpec.classBuilder(generatedType)
        .addAnnotation(MODULE)
        .addModifiers(ABSTRACT)
        .apply {
          if (public) {
            addModifiers(PUBLIC)
          }
        }
        .addMethod(
            MethodSpec.constructorBuilder()
                .addModifiers(PRIVATE)
                .build())
        .applyEach(viewHolderInjections) { injectedName ->
          addMethod(
              MethodSpec.methodBuilder(injectedName.bindMethodName())
                  .addAnnotation(BINDS)
                  .addModifiers(ABSTRACT)
                  .returns(ParameterizedTypeName.get(VIEW_HOLDER_FACTORY, injectedName))
                  .addParameter(injectedName.viewHolderInjectFactoryName(), "factory")
                  .build())
        }
        .applyEach(javaxInjections) { injectedName ->
          addMethod(
              MethodSpec.methodBuilder(injectedName.bindMethodName())
                  .addAnnotation(PROVIDES)
                  .addModifiers(STATIC)
                  .returns(ParameterizedTypeName.get(VIEW_HOLDER_FACTORY, injectedName))
                  .addParameter(
                      ParameterizedTypeName.get(PROVIDER, injectedName), "provider")
                  .addStatement("return \$T.create(provider)", VIEW_HOLDER_FACTORIES)
                  .build())
        }
        .build()
  }
}

private fun ClassName.bindMethodName() = "bind_" + reflectionName().replace('.', '_')

fun ClassName.registryInjectionModuleName(): ClassName =
  peerClassWithReflectionNesting(simpleName() + "_RegistryModule")

fun ClassName.viewHolderInjectFactoryName(): ClassName =
  peerClassWithReflectionNesting(simpleName() + "_Factory")
