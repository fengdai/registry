package com.github.fengdai.registry.processor

import com.github.fengdai.registry.processor.ItemOfMethodNameSuffix.FULL
import com.github.fengdai.registry.processor.ItemOfMethodNameSuffix.NONE
import com.github.fengdai.registry.processor.ItemOfMethodNameSuffix.SIMPLE
import com.github.fengdai.registry.processor.internal.applyEach
import com.github.fengdai.registry.processor.internal.peerClassWithReflectionNesting
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeName.INT
import com.squareup.javapoet.TypeName.OBJECT
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

private val VIEW_HOLDER_FACTORY = ClassName.get("com.github.fengdai.viewholder", "ViewHolderFactory")
private val REGISTRY = ClassName.get("com.github.fengdai.registry", "Registry")
private val BINDER = ClassName.get("com.github.fengdai.registry", "Binder")

data class RegistryClass(
  val annotation: ClassName,
  val public: Boolean,
  val injected: Boolean,
  val bindingSets: List<BindingSet>,
  val indexedViewHolderTypes: List<Pair<Int, TypeName>>,
  val staticContentLayouts: List<Id>
) {
  val generatedType: ClassName =
    annotation.run { peerClassWithReflectionNesting(simpleName() + "_Registry") }
  private val generatedItemType = generatedType.nestedClass("Item")
  private val sortedIndexedViewHolders = indexedViewHolderTypes.sortedBy { it.second.toString() }
  private val indexedStaticContentLayouts: List<Pair<Int, Id>> = staticContentLayouts
      .mapIndexed { index, id -> Pair(index + indexedViewHolderTypes.size, id) }

  fun brewJava(): TypeSpec {
    return TypeSpec.classBuilder(generatedType)
        .addAnnotation(annotation)
        .addModifiers(FINAL)
        .apply {
          if (public) {
            addModifiers(PUBLIC)
          }
        }
        .superclass(ParameterizedTypeName.get(REGISTRY, generatedItemType))
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
              addStatement("registerViewHolderFactory(\$L, \$L)", it.first, "factory${it.first}")
            }
            .applyEach(indexedStaticContentLayouts) {
              addStatement("registerStaticContentLayout(\$L, \$L)", it.first, it.second.code)
            }
            .build())
        .applyEach(bindingSets) {
          val nameSuffixType = it.itemOfMethodNameSuffixType()
          addMethods(it.bindings.map { binding -> binding.itemOfMethod(nameSuffixType) })
        }
        .applyEach(indexedStaticContentLayouts) {
          val viewType = it.first
          val layout = it.second
          val field = FieldSpec.builder(generatedItemType, "ITEM_" + layout.resourceName!!)
              .addModifiers(PRIVATE, FINAL, STATIC)
              .initializer("new \$T(\$L, \$L, null)", generatedItemType, layout.code, viewType)
              .build()
          addField(field)
          addMethod(MethodSpec.methodBuilder("itemOf_" + layout.resourceName)
              .addModifiers(PUBLIC, STATIC)
              .returns(generatedItemType)
              .addStatement("return \$N", field)
              .build())
        }
        .addMethod(MethodSpec.methodBuilder("getItemData")
            .addModifiers(PUBLIC, FINAL)
            .addAnnotation(Override::class.java)
            .returns(ClassName.OBJECT)
            .addParameter(generatedItemType, "item")
            .addStatement("return item.data")
            .build())
        .addMethod(MethodSpec.methodBuilder("getItemViewType")
            .addModifiers(PUBLIC, FINAL)
            .addAnnotation(Override::class.java)
            .returns(ClassName.INT)
            .addParameter(generatedItemType, "item")
            .addStatement("return item.viewType")
            .build())
        .addMethod(MethodSpec.methodBuilder("getItemBinder")
            .addModifiers(PUBLIC, FINAL)
            .addAnnotation(Override::class.java)
            .returns(BINDER)
            .addParameter(generatedItemType, "item")
            .addStatement("return item.binder")
            .build())
        .addType(TypeSpec.classBuilder(generatedItemType)
            .addModifiers(PUBLIC, FINAL, STATIC)
            .addField(ClassName.OBJECT, "data", PUBLIC, FINAL)
            .addField(ClassName.INT, "viewType", PUBLIC, FINAL)
            .addField(BINDER, "binder", PUBLIC, FINAL)
            .addMethod(MethodSpec.constructorBuilder()
                .addModifiers(PRIVATE)
                .addParameter(OBJECT, "data")
                .addParameter(INT, "viewType")
                .addParameter(BINDER, "binder")
                .addStatement("this.data = data")
                .addStatement("this.viewType = viewType")
                .addStatement("this.binder = binder")
                .build())
            .build())
        .build()
  }

  private fun Binding.itemOfMethod(nameSuffixType: ItemOfMethodNameSuffix = NONE): MethodSpec {
    val name = "itemOf" + when (nameSuffixType) {
      NONE -> ""
      SIMPLE -> "_$itemOfMethodNameSimpleSuffix"
      FULL -> "_$itemOfMethodNameFullSuffix"
    }
    return MethodSpec.methodBuilder(name)
        .addModifiers(PUBLIC, STATIC)
        .addParameter(dataType, "data")
        .returns(generatedItemType)
        .addStatement(CodeBlock.of(
            "return new \$T(data, \$L, \$L)", generatedItemType, viewType,
            if (targetIsBinder) CodeBlock.of("new \$T()", targetType)
            else CodeBlock.of("BINDER_VIEW_HOLDER_BINDER")))
        .build()
  }
}

private enum class ItemOfMethodNameSuffix {
  NONE, SIMPLE, FULL
}

private val Binding.itemOfMethodNameSimpleSuffix: String
  get() = targetType.reflectionName().split(".").last()

private val Binding.itemOfMethodNameFullSuffix: String
  get() = targetType.reflectionName().replace('.', '_')

private fun BindingSet.itemOfMethodNameSuffixType(): ItemOfMethodNameSuffix {
  if (bindings.size == 1) return NONE
  val simpleNameCounts = bindings.distinctBy { it.itemOfMethodNameSimpleSuffix }.size
  return if (simpleNameCounts == bindings.size) SIMPLE else FULL
}
