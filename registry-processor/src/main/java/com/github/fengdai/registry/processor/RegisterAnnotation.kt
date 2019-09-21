package com.github.fengdai.registry.processor

import com.google.auto.common.AnnotationMirrors.getAnnotationValue
import com.google.auto.common.MoreTypes
import com.sun.tools.javac.code.Attribute
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

data class RegisterAnnotation(val annotation: AnnotationMirror) {
  val binders: Set<TypeElement> =
    (getAnnotationValue(annotation, "binders") as Attribute.Array).value
        .asSequence()
        .filterIsInstance<Attribute.Class>()
        .map { it.value.asTypeElement() }
        .toSet()

  val binderViewHolders: Set<TypeElement> =
    (getAnnotationValue(annotation, "binderViewHolders") as Attribute.Array).value
        .filterIsInstance<Attribute.Class>()
        .map { it.value.asTypeElement() }
        .toSet()

  val staticContentLayoutsValues: Set<Int> =
    (getAnnotationValue(annotation, "staticContentLayouts") as Attribute.Array).value
        .map { (it as Attribute.Constant).value as Int }
        .toSet()
}

private fun TypeMirror.asTypeElement() = MoreTypes.asTypeElement(this)
