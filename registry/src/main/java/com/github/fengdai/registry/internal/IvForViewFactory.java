package com.github.fengdai.registry.internal;

import android.view.View;
import android.view.ViewGroup;
import com.github.fengdai.registry.ViewBinder;
import com.github.fengdai.registry.ViewFactory;

class IvForViewFactory<T, BV extends View, FV extends BV> extends Iv<T, BV> {
  private final Class<? extends ViewFactory<FV>> viewFactoryClass;
  private ViewFactory<FV> viewFactory = null;

  IvForViewFactory(Class<T> modelClass, int itemViewType,
      Class<? extends ViewBinder<T, BV>> viewBinderClass,
      Class<? extends ViewFactory<FV>> viewFactoryClass) {
    super(modelClass, itemViewType, viewBinderClass);
    this.viewFactoryClass = viewFactoryClass;
  }

  @Override public FV providerView(ViewGroup parent) {
    if (viewFactory == null) {
      viewFactory = Utils.newInstanceOf(viewFactoryClass);
    }
    return viewFactory.createView(parent);
  }
}
