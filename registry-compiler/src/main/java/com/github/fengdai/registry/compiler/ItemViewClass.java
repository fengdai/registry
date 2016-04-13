package com.github.fengdai.registry.compiler;

import com.squareup.javapoet.TypeName;

class ItemViewClass {
  private final int type;
  private final TypeName binderType;
  private final int layoutRes;

  ItemViewClass(int type, TypeName binderType, int layoutRes) {
    this.type = type;
    this.binderType = binderType;
    this.layoutRes = layoutRes;
  }

  int getType() {
    return type;
  }

  TypeName getBinderType() {
    return binderType;
  }

  int getLayoutRes() {
    return layoutRes;
  }
}
