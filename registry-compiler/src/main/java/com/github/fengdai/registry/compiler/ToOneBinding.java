package com.github.fengdai.registry.compiler;

import com.squareup.javapoet.TypeName;

public class ToOneBinding extends Binding {
  private final ItemViewClass itemViewClass;

  ToOneBinding(TypeName modelType, ItemViewClass itemViewClass) {
    super(modelType);
    this.itemViewClass = itemViewClass;
  }

  ItemViewClass getItemViewClass() {
    return itemViewClass;
  }
}
