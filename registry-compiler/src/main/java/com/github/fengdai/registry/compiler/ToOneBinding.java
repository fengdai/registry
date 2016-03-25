package com.github.fengdai.registry.compiler;

import javax.lang.model.element.TypeElement;

public class ToOneBinding extends Binding {
  private final ItemViewClass itemViewClass;

  ToOneBinding(TypeElement modelType, ItemViewClass itemViewClass) {
    super(modelType);
    this.itemViewClass = itemViewClass;
  }

  ItemViewClass getItemViewClass() {
    return itemViewClass;
  }
}
