package com.github.fengdai.registry.compiler;

import com.squareup.javapoet.ClassName;
import javax.lang.model.element.TypeElement;

final class BinderInfo {
  final TypeElement binderElement;
  final TypeElement dataElement;
  final TypeElement viewHolderElement;

  final ClassName binderClassName;
  final ClassName dataClassName;
  final ClassName viewHolderClassName;

  final IndexedViewHolderInfo viewHolderInfo;

  BinderInfo(TypeElement binderElement, TypeElement dataElement, TypeElement viewHolderElement,
      IndexedViewHolderInfo viewHolderInfo) {
    this.binderElement = binderElement;
    this.dataElement = dataElement;
    this.viewHolderElement = viewHolderElement;

    this.binderClassName = ClassName.get(binderElement);
    this.dataClassName = ClassName.get(dataElement);
    this.viewHolderClassName = ClassName.get(viewHolderElement);

    this.viewHolderInfo = viewHolderInfo;
  }
}
