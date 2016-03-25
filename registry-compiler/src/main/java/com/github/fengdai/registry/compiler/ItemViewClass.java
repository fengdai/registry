package com.github.fengdai.registry.compiler;

import javax.lang.model.element.TypeElement;

class ItemViewClass {
  private final int type;
  private final TypeElement binderType;
  private final Object view;

  ItemViewClass(int type, TypeElement binderType, Object view) {
    this.type = type;
    this.binderType = binderType;
    this.view = view;
  }

  int getType() {
    return type;
  }

  TypeElement getBinderType() {
    return binderType;
  }

  TypeElement getViewProviderType() {
    return (TypeElement) view;
  }

  int getLayoutRes() {
    return (int) view;
  }

  boolean isViewLayoutRes() {
    return !(view instanceof TypeElement);
  }
}
