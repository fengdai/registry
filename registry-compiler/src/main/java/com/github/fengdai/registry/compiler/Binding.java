package com.github.fengdai.registry.compiler;

import javax.lang.model.element.TypeElement;

class Binding {
  private final TypeElement modelType;

  Binding(TypeElement modelType) {
    this.modelType = modelType;
  }

  TypeElement getModelType() {
    return modelType;
  }
}
