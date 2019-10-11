package com.github.fengdai.registry.processor

import com.github.fengdai.registry.processor.internal.applyEach
import com.github.fengdai.registry.processor.internal.peerClassWithReflectionNesting
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC

private val VIEW_HOLDER_FACTORY =
  ClassName.get("com.github.fengdai.viewholder", "ViewHolderFactory")
private val REGISTRY = ClassName.get("com.github.fengdai.registry", "Registry")
private val DATA_RESOLVER = ClassName.get("com.github.fengdai.registry", "Registry", "DataResolver")
private val DATA_RESOLVER_BUCKET = ClassName.get("com.github.fengdai.registry", "Registry", "DataResolverBucket")

data class RegistryClass(
  val targetType: ClassName,
  val public: Boolean,
  val injected: Boolean,
  val bindingSets: List<BindingSet>,
  val indexedViewHolderTypes: List<Pair<Int, TypeName>>
) {
  val generatedType: ClassName =
    targetType.run { peerClassWithReflectionNesting(simpleName() + "_Registry") }
  private val sortedIndexedViewHolders = indexedViewHolderTypes.sortedBy { it.second.toString() }

  fun brewJava(): TypeSpec {
    return TypeSpec.classBuilder(generatedType)
        .addModifiers(FINAL)
        .apply {
          if (public) {
            addModifiers(PUBLIC)
          }
        }
        .superclass(REGISTRY)
        .addMethod(MethodSpec.constructorBuilder()
            .addModifiers(PUBLIC)
            .apply {
              if (injected) {
                addAnnotation(ClassName.get("javax.inject", "Inject"))
              }
            }
            .applyEach(sortedIndexedViewHolders) {
              addParameter(
                  ParameterizedTypeName.get(VIEW_HOLDER_FACTORY, it.second), "factory${it.first}"
              )
            }
            .addStatement("super()")
            .applyEach(indexedViewHolderTypes) {
              addStatement(
                  "registerViewHolderFactory(\$L, \$L)", it.first, "factory${it.first}"
              )
            }
            .addCode("\n")
            .applyEach(bindingSets) {bindingSet ->
              val bucket = if (bindingSet.bindings.size == 1) {
                val binding = bindingSet.bindings.single()
                CodeBlock.of("singleDataResolverBucket(\$L, \$L)", binding.viewType, binding.binder)
              } else {
                val bucketType = TypeSpec.anonymousClassBuilder("")
                    .addSuperinterface(
                        ParameterizedTypeName.get(DATA_RESOLVER_BUCKET, bindingSet.dataRawType)
                    )
                    .applyEach(bindingSet.bindings) { binding ->
                      addField(FieldSpec.builder(DATA_RESOLVER, binding.dataResolverName, PRIVATE, FINAL)
                          .initializer("new \$T(\$L, \$L)", DATA_RESOLVER, binding.viewType, binding.binder)
                          .build())
                    }
                    .addMethod(MethodSpec.methodBuilder("getDataResolver")
                        .addAnnotation(Override::class.java)
                        .addModifiers(PUBLIC)
                        .addParameter(bindingSet.dataRawType, "data")
                        .returns(DATA_RESOLVER)
                        .apply {
                          val (identifierProvided, default) = bindingSet.bindings.partition { it.identifier != null }
                          (if (default.singleOrNull() != null) identifierProvided else identifierProvided.subList(0, identifierProvided.size - 1))
                              .forEach { binding ->
                                beginControlFlow("if (\$T.\$N(data))", targetType, binding.identifierName!!)
                                    .addStatement("return ${binding.dataResolverName}")
                                    .endControlFlow()
                              }
                          addStatement("return ${(default.singleOrNull() ?: identifierProvided.last()).dataResolverName}")
                        }
                        .build())
                    .build()
                CodeBlock.of("\$L", bucketType)
              }
              addStatement("registerDataType(\$T.class, \$L)", bindingSet.dataRawType, bucket)
            }
            .build())
        .build()
  }
}

private val Binding.binder
  get() =
    if (targetIsBinder) CodeBlock.of("new \$T()", targetType)
    else CodeBlock.of("BINDER_VIEW_HOLDER_BINDER")

private val Binding.identifierName get() = identifier?.simpleName?.toString()
private val Binding.dataResolverName get() = identifier?.simpleName?.toString() ?: "defaultDataResolver"
