package com.github.fengdai.registry;

public interface ViewBinder<T, V> {

  void bind(T item, V view);
}
