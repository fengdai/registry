package com.github.fengdai.registry;

import android.view.View;
import android.view.ViewGroup;
import java.lang.annotation.Annotation;

public abstract class Registry {
  public abstract ItemView<Object, View> getItemView(Object item);

  public abstract int getViewTypeCount();

  public abstract boolean hasRegistered(Object model);

  public static Registry create(Class<? extends Annotation> cls) {
    String clsName = cls.getName();
    try {
      Class<?> registryClass = Class.forName(clsName + "$$Registry");
      return (Registry) registryClass.newInstance();
    } catch (Exception e) {
      throw new RuntimeException("Unable to create Registry for " + cls.getName(), e);
    }
  }

  public interface ItemView<T, V extends View> {

    int getType();

    V providerView(ViewGroup parent);

    void bindView(T item, V convertView);
  }
}
