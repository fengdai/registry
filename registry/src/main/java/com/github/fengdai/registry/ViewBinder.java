package com.github.fengdai.registry;

import android.view.View;

public interface ViewBinder<T, V extends View> {

  void bind(T item, V view);
}
