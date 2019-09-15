package com.github.fengdai.registry.compiler;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.lang.model.element.TypeElement;

final class BindingSet {
  final ClassName dataClassName;
  final ImmutableList<Binding> bindings;
  final boolean isMultiBinding;

  private BindingSet(TypeElement dataElement, ImmutableList<Binding> bindings) {
    this.dataClassName = ClassName.get(dataElement);
    this.bindings = bindings;
    this.isMultiBinding = bindings.size() > 1;
  }

  static class Builder {
    private final TypeElement dataElement;
    private final Map<TypeElement, Binding> bindings = new LinkedHashMap<>();

    Builder(TypeElement dataElement) {
      this.dataElement = dataElement;
    }

    void add(Binding binding) {
      if (!binding.dataElement.equals(dataElement)) {
        throw new IllegalStateException();
      }

      final TypeElement viewHolderElement = binding.viewHolderElement;
      final Binding existedBinding = bindings.get(viewHolderElement);
      if (existedBinding != null) {
        throw new DuplicateBindingException(
            String.format("Duplicate binding of %s to %s.\n%s conflicts with %s.",
                binding.dataElement, viewHolderElement, binding.element, existedBinding.element));
      }

      bindings.put(viewHolderElement, binding);
    }

    BindingSet build() {
      return new BindingSet(dataElement, ImmutableList.copyOf(bindings.values()));
    }
  }
}
