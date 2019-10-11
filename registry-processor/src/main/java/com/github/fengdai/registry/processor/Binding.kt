package com.github.fengdai.registry.processor

import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import javax.lang.model.element.ExecutableElement

class Binding(
  val targetType: ClassName,
  val targetIsBinder: Boolean,
  val dataRawType: ClassName,
  val viewHolderType: TypeName,
  val viewType: Int,
  val identifier: ExecutableElement?
)
