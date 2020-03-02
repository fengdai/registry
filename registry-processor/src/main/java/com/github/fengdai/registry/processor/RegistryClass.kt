package com.github.fengdai.registry.processor

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.github.fengdai.registry.processor.internal.applyEach
import com.github.fengdai.registry.processor.internal.peerClassWithReflectionNesting
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.lang.model.element.Modifier.FINAL
import javax.lang.model.element.Modifier.PRIVATE
import javax.lang.model.element.Modifier.PUBLIC
import javax.lang.model.element.Modifier.STATIC

private val BINDER = ClassName.get("com.github.fengdai.registry", "Binder")
private val ADAPTER_DELEGATE = ClassName.get("com.github.fengdai.registry", "AdapterDelegate")
private val VIEW_HOLDER_FACTORY = ClassName.get("com.github.fengdai.viewholder", "ViewHolderFactory")
private val VIEW_GROUP = ClassName.get("android.view", "ViewGroup")
private val SPARSE_ARRAY = ClassName.get("android.util", "SparseArray")
private val VIEW_HOLDER = ClassName.get("androidx.recyclerview.widget", "RecyclerView", "ViewHolder")
private val LAYOUT_VIEW_HOLDER_FACTORY = ClassName.get("com.github.fengdai.registry.internal", "LayoutViewHolderFactory")
private val BINDERS = ClassName.get("com.github.fengdai.registry.internal", "Binders")
private val DATA_RESOLVER = ClassName.get("com.github.fengdai.registry.internal", "DataResolver")

data class RegistryClass(
  val targetType: ClassName,
  val itemType: ClassName,
  val public: Boolean,
  val injected: Boolean,
  val bindingSets: List<BindingSet>,
  val indexedViewHolderTypes: List<Pair<Int, TypeName>>,
  val layouts: List<LayoutBinding>
) {
  val generatedType: ClassName = targetType.run { peerClassWithReflectionNesting(simpleName() + "_Impl") }
  private val generatedItemType = generatedType.nestedClass("Item")
  private val generatedAdapterDelegateType = generatedType.nestedClass("AdapterDelegate")
  private val sortedIndexedViewHolders = indexedViewHolderTypes.sortedBy { it.second.toString() }
  private val indexedLayouts: List<Pair<Int, LayoutBinding>> = layouts
      .mapIndexed { index, id -> Pair(index + indexedViewHolderTypes.size, id) }

  fun brewJava(): TypeSpec {
    return TypeSpec.classBuilder(generatedType)
        .addModifiers(FINAL)
        .apply {
          if (public) {
            addModifiers(PUBLIC)
          }
        }
        .addSuperinterface(targetType)
        .applyEach(bindingSets) {
          it.bindings.forEach { binding ->
            val dataResolverField =
              FieldSpec.builder(DATA_RESOLVER, binding.dataResolverFieldName, PRIVATE, FINAL)
                  .initializer(CodeBlock.of(
                      "new \$T(\$L, \$L)", DATA_RESOLVER, binding.viewType,
                      if (binding.targetIsBinder) CodeBlock.of("new \$T()", binding.targetType)
                      else CodeBlock.of("\$T.BINDER_VIEW_HOLDER_BINDER", BINDERS)))
                  .build()
            addField(dataResolverField)
            addMethod(MethodSpec.overriding(binding.factoryMethod)
                .addStatement(CodeBlock.of(
                    "return new \$T(\$L, \$N)", generatedItemType,
                    binding.factoryMethod.parameters.single().simpleName,
                    dataResolverField))
                .build())
          }
        }
        .applyEach(indexedLayouts) {
          val (viewType, layoutBinding) = it
          val factoryMethod = layoutBinding.factoryMethod
          val layout = layoutBinding.layout
          val field =
            FieldSpec.builder(itemType, "item_" + layout.resourceName)
                .addModifiers(PRIVATE, FINAL)
                .initializer("new \$T(\$L, new \$T(\$L, null))", generatedItemType, layout.code, DATA_RESOLVER, viewType)
                .build()
          addField(field)
          addMethod(MethodSpec.overriding(factoryMethod)
              .addStatement("return \$N", field)
              .build())
        }
        .addType(TypeSpec.classBuilder(generatedItemType)
            .addSuperinterface(itemType)
            .addModifiers(STATIC, PRIVATE, FINAL)
            .addField(TypeName.OBJECT, "data", PRIVATE, FINAL)
            .addField(DATA_RESOLVER, "dataResolver", PRIVATE, FINAL)
            .addMethod(MethodSpec.constructorBuilder()
                .addParameter(TypeName.OBJECT, "data")
                .addParameter(DATA_RESOLVER, "dataResolver")
                .addStatement("this.data = data")
                .addStatement("this.dataResolver = dataResolver")
                .build())
            .addMethod(MethodSpec.methodBuilder("getData")
                .addModifiers(PUBLIC)
                .addAnnotation(Override::class.java)
                .returns(TypeName.OBJECT)
                .addStatement("return data")
                .build())
            .addMethod(MethodSpec.methodBuilder("getViewType")
                .addModifiers(PUBLIC)
                .addAnnotation(Override::class.java)
                .returns(TypeName.INT)
                .addStatement("return dataResolver.viewType")
                .build())
            .addMethod(MethodSpec.methodBuilder("getBinder")
                .addModifiers(PUBLIC)
                .addAnnotation(Nullable::class.java)
                .addAnnotation(Override::class.java)
                .returns(BINDER)
                .addStatement("return dataResolver.binder")
                .build())
            .build())
        .addType(TypeSpec.classBuilder(generatedAdapterDelegateType)
            .addModifiers(STATIC, FINAL)
            .apply {
              if (public) {
                addModifiers(PUBLIC)
              }
            }
            .superclass(ParameterizedTypeName.get(ADAPTER_DELEGATE, itemType))
            .addField(FieldSpec.builder(ParameterizedTypeName.get(SPARSE_ARRAY, VIEW_HOLDER_FACTORY), "viewHolderFactories", PRIVATE, FINAL)
                .initializer("new SparseArray<>()")
                .build())
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
                  addStatement("viewHolderFactories.put(\$L, \$L)", it.first, "factory${it.first}")
                }
                .applyEach(indexedLayouts) {
                  addStatement("viewHolderFactories.put(\$L, new \$T(\$L))", it.first, LAYOUT_VIEW_HOLDER_FACTORY, it.second.layout.code)
                }
                .build())
            .addMethod(MethodSpec.methodBuilder("onCreateViewHolder")
                .addAnnotation(Override::class.java)
                .addAnnotation(NonNull::class.java)
                .addModifiers(PUBLIC)
                .returns(VIEW_HOLDER)
                .addParameter(ParameterSpec.builder(VIEW_GROUP, "parent").build())
                .addParameter(ParameterSpec.builder(TypeName.INT, "viewType").build())
                .addStatement("return viewHolderFactories.get(viewType).create(parent)")
                .build())
            .build())
        .build()
  }
}

private val Binding.dataResolverFieldName get() = "resolver_" + factoryMethod.simpleName + "_" + dataRawType.reflectionName().replace('.', '_')
