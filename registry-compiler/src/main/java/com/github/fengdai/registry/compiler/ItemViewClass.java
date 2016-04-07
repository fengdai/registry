package com.github.fengdai.registry.compiler;

import javax.lang.model.element.TypeElement;

class ItemViewClass {
  private final int type;
  private final TypeElement binderType;
  private final int layoutRes;

  ItemViewClass(int type, TypeElement binderType, int layoutRes) {
    this.type = type;
    this.binderType = binderType;
    this.layoutRes = layoutRes;
  }

  int getType() {
    return type;
  }

  TypeElement getBinderType() {
    return binderType;
  }

  int getLayoutRes() {
    return layoutRes;
  }
}
