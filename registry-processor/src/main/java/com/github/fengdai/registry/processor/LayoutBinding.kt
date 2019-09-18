package com.github.fengdai.registry.processor

import javax.lang.model.element.ExecutableElement

data class LayoutBinding(
  val factoryMethod: ExecutableElement,
  val layout: Id
)
