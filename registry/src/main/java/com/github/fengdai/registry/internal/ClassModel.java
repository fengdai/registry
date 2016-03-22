package com.github.fengdai.registry.internal;

class ClassModel extends Model {
  private final ItemView itemView;

  ClassModel(Class<?> modelClass, ItemView itemView) {
    super(modelClass);
    this.itemView = itemView;
  }

  @Override ItemView getItemView(Object model) {
    return this.itemView;
  }
}
