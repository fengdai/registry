package com.github.fengdai.registry.compiler;

import com.squareup.javapoet.ClassName;
import javax.lang.model.element.TypeElement;

class InflateLayout {
  final Id layoutRes;
  final ClassName viewClassName;

  InflateLayout(Id layoutRes, TypeElement viewElement) {
    this.layoutRes = layoutRes;
    this.viewClassName = ClassName.get(viewElement);
  }
}
