package com.github.fengdai.registry;

import android.view.View;
import android.view.ViewGroup;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

public abstract class Registry {
  private final Map<Class<?>, Model<?>> models;
  private final int viewTypeCount;

  public interface ItemView {

    int getType();

    View newView(ViewGroup parent);

    void bindView(Object item, View convertView);
  }

  public static Registry create(Class<? extends Annotation> cls) {
    String clsName = cls.getName();
    try {
      Class<?> registryClass = Class.forName(clsName + "$$Registry");
      return (Registry) registryClass.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(String.format("Unable to create Registry for %s.", cls.getName()),
          e);
    }
  }

  public Registry(Map<Class<?>, Model<?>> models, int viewTypeCount) {
    this.models = models;
    this.viewTypeCount = viewTypeCount;
  }

  public ItemView getItemView(Object item) {
    Model model = findModelFor(item);
    if (model == null) {
      throw new IllegalStateException("Unregistered type: " + item.getClass().getName());
    }
    // noinspection unchecked
    ItemView itemView = model.getItemView(item);
    if (itemView == null) {
      // TODO message
      throw new IllegalStateException("");
    }
    return itemView;
  }

  public int getViewTypeCount() {
    return viewTypeCount;
  }

  public boolean hasRegistered(Object item) {
    return findItemViewFor(item) != null;
  }

  private <T> ItemView findItemViewFor(T item) {
    Model<T> model = findModelFor(item);
    return model != null ? model.getItemView(item) : null;
  }

  private <T> Model<T> findModelFor(T item) {
    Model<?> model = models.get(item.getClass());
    if (model == null) {
      Set<Map.Entry<Class<?>, Model<?>>> entrySet = models.entrySet();
      for (Map.Entry<Class<?>, Model<?>> entry : entrySet) {
        if (entry.getKey().isAssignableFrom(item.getClass())) {
          model = entry.getValue();
          models.put(item.getClass(), model);
          break;
        }
      }
    }
    // noinspection unchecked
    return (Model<T>) model;
  }
}
