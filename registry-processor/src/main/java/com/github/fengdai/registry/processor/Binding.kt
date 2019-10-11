package com.github.fengdai.registry.processor

import com.github.fengdai.registry.processor.internal.rawClassName
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import javax.lang.model.element.ExecutableElement

class Binding(
  val targetType: ClassName,
  val targetIsBinder: Boolean,
  val dataType: TypeName,
  val viewHolderType: TypeName,
  val viewType: Int,
  val identifier: ExecutableElement?
) {
  val dataRawType: ClassName = dataType.rawClassName()
}
