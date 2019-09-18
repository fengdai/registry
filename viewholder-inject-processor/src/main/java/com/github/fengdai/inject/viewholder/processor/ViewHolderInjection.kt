package com.github.fengdai.inject.viewholder.processor

import android.support.annotation.NonNull
import com.github.fengdai.inject.viewholder.Inflation
import com.github.fengdai.inject.viewholder.processor.DependencyRequest.Inflate
import com.github.fengdai.inject.viewholder.processor.DependencyRequest.Parent
import com.github.fengdai.inject.viewholder.processor.DependencyRequest.Provided
import com.github.fengdai.inject.viewholder.processor.internal.applyEach
import com.github.fengdai.inject.viewholder.processor.internal.joinToCode
import com.github.fengdai.inject.viewholder.processor.internal.peerClassWithReflectionNesting
import com.github.fengdai.inject.viewholder.processor.internal.rawClassName
import com.github.fengdai.inject.viewholder.processor.internal.toClassName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import com.squareup.javapoet.TypeVariableName
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC

private val VIEW_GROUP = ClassName.get("android.view", "ViewGroup")
private val JAVAX_INJECT = ClassName.get("javax.inject", "Inject")
private val JAVAX_PROVIDER = ClassName.get("javax.inject", "Provider")
private val INFLATION = Inflation::class.toClassName()

data class ViewHolderInjection(
  val targetType: TypeName,
  val dependencyRequests: List<DependencyRequest>
) {
  val generatedType: ClassName = targetType.rawClassName().viewHolderInjectFactoryName()
  private val provided = dependencyRequests.filterIsInstance<Provided>()

  fun brewJava(): TypeSpec {
    return TypeSpec.classBuilder(generatedType)
        .addModifiers(PUBLIC, FINAL)
        .addSuperinterface(targetType.viewHolderFactoryInterfaceName())
        .applyEach(provided) {
          addField(it.providerType.withoutAnnotations(), it.name, PRIVATE, FINAL)
        }
        .addMethod(MethodSpec.constructorBuilder()
            .addModifiers(PUBLIC)
            .addAnnotation(JAVAX_INJECT)
            .applyEach(provided) {
              addParameter(it.providerType, it.name)
              addStatement("this.$1N = $1N", it.name)
            }
            .build())
        .addMethod(MethodSpec.methodBuilder("create")
            .addAnnotation(NonNull::class.java)
            .addAnnotation(Override::class.java)
            .addModifiers(PUBLIC)
            .returns(targetType)
            .apply {
              if (targetType is ParameterizedTypeName) {
                addTypeVariables(targetType.typeArguments.filterIsInstance<TypeVariableName>())
              }
            }
            .addParameter(ParameterSpec.builder(VIEW_GROUP, "parent").addAnnotation(NonNull::class.java).build())
            .addStatement("return new \$T(\n\$L)", targetType,
                dependencyRequests.map { it.argumentProvider }.joinToCode(",\n"))
            .build())
        .build()
  }
}

fun ClassName.viewHolderInjectFactoryName(): ClassName =
  peerClassWithReflectionNesting(simpleName() + "_Factory")

fun TypeName.viewHolderFactoryInterfaceName(): ParameterizedTypeName =
  ParameterizedTypeName.get(ClassName.get("com.github.fengdai.viewholder", "ViewHolderFactory"), this)

/** True when this key represents a parameterized JSR 330 `Provider`. */
private val Key.isProvider get() = type is ParameterizedTypeName && type.rawType == JAVAX_PROVIDER

private val Provided.providerType: TypeName
  get() {
    val type = if (key.isProvider) {
      key.type // Do not wrap a Provider inside another Provider.
    } else {
      ParameterizedTypeName.get(JAVAX_PROVIDER, key.type.box())
    }
    key.qualifier?.let {
      return type.annotated(it)
    }
    return type
  }

private val DependencyRequest.argumentProvider
  get() = when (this) {
    is Inflate -> CodeBlock.of("(\$T) \$T.inflate(parent, \$L)", key.type, INFLATION, layoutRes.code)
    is Parent -> CodeBlock.of("parent")
    is Provided -> CodeBlock.of(if (key.isProvider) "this.\$N" else "this.\$N.get()", name)
  }
