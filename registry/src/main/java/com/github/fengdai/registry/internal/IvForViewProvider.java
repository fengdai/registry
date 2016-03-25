package com.github.fengdai.registry.internal;

import android.view.View;
import android.view.ViewGroup;
import com.github.fengdai.registry.ViewBinder;
import com.github.fengdai.registry.ViewProvider;

class IvForViewProvider<T, BV extends View, PV extends BV> extends AbsItemView<T, BV> {
  private final Class<? extends ViewProvider<PV>> viewProviderClass;
  private ViewProvider<PV> viewProvider = null;

  IvForViewProvider(Class<T> modelClass, int itemViewType,
      Class<? extends ViewBinder<T, BV>> viewBinderClass,
      Class<? extends ViewProvider<PV>> viewProviderClass) {
    super(modelClass, itemViewType, viewBinderClass);
    this.viewProviderClass = viewProviderClass;
  }

  @Override public PV providerView(ViewGroup parent) {
    if (viewProvider == null) {
      viewProvider = Utils.newInstanceOf(viewProviderClass);
    }
    return viewProvider.provideView(parent);
  }
}
