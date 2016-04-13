package com.github.fengdai.registry.compiler;

import com.squareup.javapoet.TypeName;

class Binding {
  private final TypeName modelType;

  Binding(TypeName modelType) {
    this.modelType = modelType;
  }

  TypeName getModelType() {
    return modelType;
  }
}
