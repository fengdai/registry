package com.github.fengdai.registry.compiler;

import com.squareup.javapoet.TypeName;
import javax.lang.model.type.TypeMirror;

class InflateLayout {
  final Id layoutRes;
  final TypeName viewTypeName;

  InflateLayout(Id layoutRes, TypeMirror viewType) {
    this.layoutRes = layoutRes;
    this.viewTypeName = TypeName.get(viewType);
  }
}
