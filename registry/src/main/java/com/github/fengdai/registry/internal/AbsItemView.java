package com.github.fengdai.registry.internal;

import android.view.View;
import android.view.ViewGroup;
import com.github.fengdai.registry.Registry;
import com.github.fengdai.registry.ViewBinder;

abstract class AbsItemView<T, V extends View> implements Registry.ItemView<T, V> {
  private final Class<T> modelClass;

  private final int type;

  private final Class<? extends ViewBinder<T, V>> viewBinderClass;

  private ViewBinder<T, V> viewBinder = null;

  AbsItemView(Class<T> modelClass, int type, Class<? extends ViewBinder<T, V>> viewBinderClass) {
    this.modelClass = modelClass;
    this.type = type;
    this.viewBinderClass = viewBinderClass;
  }

  public Class<?> getModelClass() {
    return modelClass;
  }

  @Override public int getType() {
    return type;
  }

  @Override public abstract V providerView(ViewGroup parent);

  @Override public void bindView(T item, V convertView) {
    if (viewBinder == null) {
      viewBinder = Utils.newInstanceOf(viewBinderClass);
    }
    viewBinder.bind(item, convertView);
  }
}
