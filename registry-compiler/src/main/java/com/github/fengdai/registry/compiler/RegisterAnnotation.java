package com.github.fengdai.registry.compiler;

import com.google.auto.common.MoreTypes;
import com.google.common.collect.ImmutableSet;
import com.sun.tools.javac.code.Attribute;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

import static com.google.auto.common.AnnotationMirrors.getAnnotationValue;

final class RegisterAnnotation {
  final AnnotationMirror annotation;

  RegisterAnnotation(AnnotationMirror annotation) {
    this.annotation = annotation;
  }

  ImmutableSet<TypeElement> binders() {
    return ((Attribute.Array) getAnnotationValue(annotation, "binders")).getValue()
        .stream()
        .filter(attribute -> attribute instanceof Attribute.Class)
        .map(it -> MoreTypes.asTypeElement(((Attribute.Class) it).getValue()))
        .collect(ImmutableSet.toImmutableSet());
  }

  ImmutableSet<TypeElement> binderViewHolders() {
    return ((Attribute.Array) getAnnotationValue(annotation, "binderViewHolders")).getValue()
        .stream()
        .filter(attribute -> attribute instanceof Attribute.Class)
        .map(it -> MoreTypes.asTypeElement(((Attribute.Class) it).getValue()))
        .collect(ImmutableSet.toImmutableSet());
  }

  ImmutableSet<Integer> staticContentLayoutsValues() {
    return ((Attribute.Array) getAnnotationValue(annotation, "staticContentLayouts")).getValue()
        .stream()
        .map(it -> (Integer) ((Attribute.Constant) it).value)
        .collect(ImmutableSet.toImmutableSet());
  }
}
