package com.github.fengdai.registry.internal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.github.fengdai.registry.ViewFactory;

public class Layouts {
  public static <V extends ViewFactory> V factoryOf(int layoutRes) {
    // noinspection unchecked
    return (V) new LayoutViewFactory<>(layoutRes);
  }

  static class LayoutViewFactory<V extends View> implements ViewFactory<V> {
    private final int layoutRes;

    LayoutViewFactory(int layoutRes) {
      this.layoutRes = layoutRes;
    }

    @Override public V createView(ViewGroup parent) {
      // noinspection unchecked
      return (V) LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
    }
  }
}
