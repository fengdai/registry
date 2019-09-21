package com.github.fengdai.registry.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.sun.tools.javac.util.Pair;
import javax.annotation.Nullable;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

final class Binding {
  final Element element;
  final TypeElement dataElement;
  final int viewType;
  final TypeElement viewHolderElement;
  @Nullable final TypeElement binderElement;

  final ClassName viewHolderClassName;
  final CodeBlock instanceCode;

  Binding(Element element, TypeElement dataElement,
      Pair<Integer, ViewHolderInfo> indexedViewHolderInfo, @Nullable TypeElement binderElement) {
    this.element = element;
    this.dataElement = dataElement;
    this.viewType = indexedViewHolderInfo.fst;
    this.viewHolderElement = indexedViewHolderInfo.snd.viewHolderElement;
    this.viewHolderClassName = indexedViewHolderInfo.snd.viewHolderClassName;
    this.binderElement = binderElement;

    if (binderElement == null) {
      instanceCode = CodeBlock.of("BINDER_VIEW_HOLDER_BINDER");
    } else {
      instanceCode = CodeBlock.of("new $T()", ClassName.get(binderElement));
    }
  }
}
