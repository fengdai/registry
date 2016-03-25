package com.github.fengdai.registry.internal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.github.fengdai.registry.ViewBinder;

class IvForLayoutRes<T, V extends View> extends AbsItemView<T, V> {
  private final int layoutRes;

  IvForLayoutRes(Class<T> modelClass, int itemViewType,
      Class<? extends ViewBinder<T, V>> viewBinderClass, final int layoutRes) {
    super(modelClass, itemViewType, viewBinderClass);
    this.layoutRes = layoutRes;
  }

  @Override public V providerView(ViewGroup parent) {
    // noinspection unchecked
    return (V) LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
  }
}
