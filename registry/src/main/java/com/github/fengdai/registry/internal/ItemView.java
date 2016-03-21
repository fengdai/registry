package com.github.fengdai.registry.internal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.github.fengdai.registry.ViewBinder;
import com.github.fengdai.registry.ViewProvider;
import java.util.LinkedHashMap;
import java.util.Map;

class ItemView {
  private static final Map<Class<? extends ViewProvider<?>>, ViewProvider<View>> PROVIDERS =
      new LinkedHashMap<>();
  private static final Map<Class<? extends ViewBinder<?, ?>>, ViewBinder<Object, View>> BINDERS =
      new LinkedHashMap<>();

  private final int itemViewType;

  private final Class<? extends ViewProvider<?>> viewProviderClass;
  private final Class<? extends ViewBinder<?, ?>> viewBinderClass;

  private ViewProvider<View> viewProvider = null;
  private ViewBinder<Object, View> viewBinder = null;

  ItemView(int itemViewType, Class<? extends ViewProvider<?>> viewProviderClass,
      Class<? extends ViewBinder<?, ?>> viewBinderClass) {
    this.itemViewType = itemViewType;
    this.viewProviderClass = viewProviderClass;
    this.viewBinderClass = viewBinderClass;
  }

  ItemView(int itemViewType, final int layoutRes,
      Class<? extends ViewBinder<?, ?>> viewBinderClass) {
    this.itemViewType = itemViewType;
    this.viewProviderClass = null;
    this.viewBinderClass = viewBinderClass;
    this.viewProvider = new ViewProvider<View>() {
      @Override public View provideView(Context context) {
        return LayoutInflater.from(context).inflate(layoutRes, null, false);
      }
    };
  }

  int getItemViewType() {
    return itemViewType;
  }

  View providerView(Object model, ViewGroup parent) {
    if (viewProvider == null) {
      try {
        viewProvider = getViewProvider(viewProviderClass);
      } catch (Exception e) {
        throw new RuntimeException("Unable provide view for " + model.getClass().getName(), e);
      }
    }
    return viewProvider.provideView(parent.getContext());
  }

  void bindView(Object model, View convertView) {
    if (viewBinder == null) {
      try {
        viewBinder = getViewBinder(viewBinderClass);
      } catch (Exception e) {
        throw new RuntimeException("Unable bind view for " + model.getClass().getName(), e);
      }
    }
    viewBinder.bind(model, convertView);
  }

  private static ViewProvider<View> getViewProvider(
      Class<? extends ViewProvider<?>> viewProviderClass)
      throws IllegalAccessException, InstantiationException {
    ViewProvider<View> provider = PROVIDERS.get(viewProviderClass);
    if (provider != null) {
      return provider;
    }
    //noinspection unchecked
    provider = (ViewProvider<View>) viewProviderClass.newInstance();
    PROVIDERS.put(viewProviderClass, provider);
    return provider;
  }

  private static ViewBinder<Object, View> getViewBinder(
      Class<? extends ViewBinder<?, ?>> viewBinderClass)
      throws IllegalAccessException, InstantiationException {
    ViewBinder<Object, View> binder = BINDERS.get(viewBinderClass);
    if (binder != null) {
      return binder;
    }
    //noinspection unchecked
    binder = (ViewBinder<Object, View>) viewBinderClass.newInstance();
    BINDERS.put(viewBinderClass, binder);
    return binder;
  }
}
