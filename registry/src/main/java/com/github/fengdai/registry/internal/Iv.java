package com.github.fengdai.registry.internal;

import android.view.View;
import android.view.ViewGroup;
import com.github.fengdai.registry.Registry;
import com.github.fengdai.registry.ViewBinder;
import com.github.fengdai.registry.ViewFactory;

class Iv<T, BV extends View, FV extends BV> implements Registry.ItemView<T, BV> {
  private final Class<T> modelClass;
  private final int type;
  private final ViewBinder<T, BV> viewBinder;
  private final ViewFactory<FV> viewFactory;

  Iv(Class<T> modelClass, int type, ViewBinder<T, BV> viewBinder, ViewFactory<FV> viewFactory) {
    this.modelClass = modelClass;
    this.type = type;
    this.viewBinder = viewBinder;
    this.viewFactory = viewFactory;
  }

  public Class<?> getModelClass() {
    return modelClass;
  }

  @Override public int getType() {
    return type;
  }

  @Override public BV providerView(ViewGroup parent) {
    return viewFactory.createView(parent);
  }

  @Override public void bindView(T item, BV convertView) {
    viewBinder.bind(item, convertView);
  }
}
