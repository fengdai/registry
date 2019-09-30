package com.github.fengdai.inject.viewholder.processor

import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

private val namedKeyComparator = compareBy<NamedKey>({ it.key }, { it.name })

/** Represents a [Key] associated with a name. */
data class NamedKey(
  val key: Key,
  val name: String
) : Comparable<NamedKey> {
  override fun toString() = "$key $name"
  override fun compareTo(other: NamedKey) = namedKeyComparator.compare(this, other)
}

/** Create a [NamedKey] from this type, any qualifier annotation, and the name. */
fun VariableElement.asNamedKey(mirror: TypeMirror = asType()) = NamedKey(asKey(mirror), simpleName.toString())
