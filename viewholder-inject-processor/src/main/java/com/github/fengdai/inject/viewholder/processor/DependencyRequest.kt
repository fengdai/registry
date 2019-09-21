package com.github.fengdai.inject.viewholder.processor

sealed class DependencyRequest {
  abstract val namedKey: NamedKey

  val key get() = namedKey.key
  val name get() = namedKey.name

  data class Inflate(
    override val namedKey: NamedKey,
    val layoutRes: Id
  ) : DependencyRequest()

  data class Parent(override val namedKey: NamedKey) : DependencyRequest()

  data class Provided(override val namedKey: NamedKey) : DependencyRequest()
}
