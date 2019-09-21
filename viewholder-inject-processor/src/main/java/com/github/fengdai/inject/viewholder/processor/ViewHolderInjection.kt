package com.github.fengdai.inject.viewholder.processor

import com.github.fengdai.inject.viewholder.processor.Dependency.Inflate
import com.github.fengdai.inject.viewholder.processor.Dependency.Parent
import com.github.fengdai.inject.viewholder.processor.Dependency.Request
import com.squareup.inject.assisted.Assisted
import com.squareup.inject.assisted.AssistedInject
import com.squareup.inject.assisted.processor.Key
import com.squareup.inject.assisted.processor.internal.applyEach
import com.squareup.inject.assisted.processor.internal.joinToCode
import com.squareup.inject.assisted.processor.internal.peerClassWithReflectionNesting
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier.ABSTRACT
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC

private val VIEW_GROUP = ClassName.get("android.view", "ViewGroup")
private val UTILS = ClassName.get("com.github.fengdai.registry", "Utils")
private val JAVAX_INJECT = ClassName.get("javax.inject", "Inject")
private val JAVAX_PROVIDER = ClassName.get("javax.inject", "Provider")

data class ViewHolderInjection(
  val targetType: ClassName,
  val dependencies: List<Dependency>
) {
  val generatedType: ClassName = targetType.viewHolderInjectFactoryName()
  private val requests = dependencies.filterIsInstance<Request>()

  fun brewJava(): TypeSpec {
    val (notProvidedRequests, providedRequests) = requests.partition { it.isNotProvided }
    val inject = providedRequests.isNotEmpty() || notProvidedRequests.isEmpty()
    val assistedInject = providedRequests.isNotEmpty() && notProvidedRequests.isNotEmpty()
    return TypeSpec.classBuilder(generatedType)
        .addModifiers(PUBLIC, FINAL)
        .addSuperinterface(targetType.viewHolderFactoryInterfaceName())
        .applyEach(notProvidedRequests) {
          addField(it.key.type.withoutAnnotations(), it.name, PRIVATE, FINAL)
        }
        .applyEach(providedRequests) {
          addField(it.providerType.withoutAnnotations(), it.name, PRIVATE, FINAL)
        }
        .addMethod(MethodSpec.constructorBuilder()
            .addModifiers(PUBLIC)
            .apply {
              if (assistedInject) addAnnotation(AssistedInject::class.java)
              else if (inject) addAnnotation(JAVAX_INJECT)
            }
            .applyEach(requests) {
              if (it.isNotProvided) addParameter(ParameterSpec.builder(it.key.type, it.name)
                  .apply { if (assistedInject) addAnnotation(Assisted::class.java) }
                  .build())
              else addParameter(it.providerType, it.name)
              addStatement("this.$1N = $1N", it.name)
            }
            .build())
        .addMethod(targetType.viewHolderFactoryMethodBuilder()
            .addStatement("return new \$T(\n\$L)", targetType,
                dependencies.map { it.argumentProvider }.joinToCode(",\n"))
            .build())
        .apply {
          if (assistedInject) {
            addType(TypeSpec.interfaceBuilder("Factory")
                .addModifiers(PUBLIC)
                .addAnnotation(AssistedInject.Factory::class.java)
                .addMethod(MethodSpec.methodBuilder("create")
                    .addModifiers(PUBLIC, ABSTRACT)
                    .returns(generatedType)
                    .applyEach(requests.filter { it.isNotProvided }) {
                      addParameter(it.key.type.withoutAnnotations(), it.name)
                    }
                    .build())
                .build())
          }
        }
        .build()
  }
}

fun ClassName.viewHolderInjectFactoryName(): ClassName =
  peerClassWithReflectionNesting(simpleName() + "_Factory")

fun ClassName.viewHolderFactoryInterfaceName(): ParameterizedTypeName =
  ParameterizedTypeName.get(ClassName.get("com.github.fengdai.registry", "ViewHolderFactory"), this)

fun ClassName.viewHolderFactoryMethodBuilder(): MethodSpec.Builder =
  MethodSpec.methodBuilder("create")
      .addAnnotation(Override::class.java)
      .addModifiers(PUBLIC)
      .returns(this)
      .addParameter(ParameterSpec.builder(VIEW_GROUP, "parent").build())

/** True when this key represents a parameterized JSR 330 `Provider`. */
private val Key.isProvider get() = type is ParameterizedTypeName && (type as ParameterizedTypeName).rawType == JAVAX_PROVIDER

private val Request.providerType: TypeName
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

private val Dependency.argumentProvider
  get() = when (this) {
    is Inflate -> CodeBlock.of("(\$T) \$T.inflate(parent, \$L)", key.type, UTILS, layoutRes.code)
    is Parent -> CodeBlock.of("parent")
    is Request -> CodeBlock.of(if (isNotProvided || key.isProvider) "this.\$N" else "this.\$N.get()", name)
  }
