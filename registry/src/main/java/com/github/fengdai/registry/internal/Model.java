package com.github.fengdai.registry.internal;

abstract class Model {
  protected final Class<?> modelClass;

  Model(Class<?> modelClass) {
    this.modelClass = modelClass;
  }

  public Class<?> getModelClass() {
    return modelClass;
  }

  abstract ItemView getItemView(Object item);
}
