package com.github.fengdai.registry;

import android.view.View;
import android.view.ViewGroup;
import com.github.fengdai.registry.internal.Factory;

public abstract class Registry {
  public abstract View getView(Object model, View convertView, ViewGroup parent);

  public abstract int getItemViewType(Object model);

  public abstract int getViewTypeCount();

  public abstract boolean hasRegistered(Object model);

  public static Registry create(Class<?> clazz) {
    return Factory.create(clazz);
  }
}
