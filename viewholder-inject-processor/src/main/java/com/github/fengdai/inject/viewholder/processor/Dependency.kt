package com.github.fengdai.inject.viewholder.processor

import com.squareup.inject.assisted.processor.NamedKey

sealed class Dependency {
  abstract val namedKey: NamedKey

  val key get() = namedKey.key
  val name get() = namedKey.name

  data class Inflate(
    override val namedKey: NamedKey,
    val layoutRes: Id
  ) : Dependency()

  data class Parent(
    override val namedKey: NamedKey
  ) : Dependency()

  data class Request(
    override val namedKey: NamedKey,
    val isNotProvided: Boolean
  ) : Dependency()
}
