package com.github.fengdai.registry.processor

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.ParameterizedTypeName
import com.squareup.javapoet.TypeName

data class BindingSet(
  val dataRawType: ClassName,
  val bindings: List<Binding>
) {

  class Builder(private val dataRawType: ClassName) {
    private val bindings = mutableMapOf<TypeName, Binding>()

    fun add(binding: Binding) {
      require(binding.dataRawType == dataRawType)

      val viewHolderType = binding.viewHolderType
      val existedBinding = bindings[viewHolderType]
      if (existedBinding != null) {
        throw DuplicateBindingException(
            "Duplicate binding of ${binding.dataRawType.displayName} to ${viewHolderType.displayName}.\n" +
                "${binding.targetType.displayName} conflicts with ${existedBinding.targetType.displayName}."
        )
      }
      bindings[viewHolderType] = binding
    }

    fun build(): BindingSet {
      return BindingSet(dataRawType, bindings.values.toList())
    }
  }
}

private val TypeName.displayName: String
  get() = "[${
  when (this) {
    is ClassName -> simpleName()
    is ParameterizedTypeName -> "${rawType.simpleName()}<${typeArguments.joinToString(", ") { it.displayName }}>"
    else -> throw IllegalStateException()
  }
  }]($this)"
