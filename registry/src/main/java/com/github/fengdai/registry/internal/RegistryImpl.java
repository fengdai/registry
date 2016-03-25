package com.github.fengdai.registry.internal;

import android.view.View;
import com.github.fengdai.registry.Registry;
import java.util.Map;
import java.util.Set;

public class RegistryImpl extends Registry {
  private final Map<Class<?>, Model<?>> models;
  private final int viewTypeCount;

  public RegistryImpl(Map<Class<?>, Model<?>> models, int viewTypeCount) {
    this.models = models;
    this.viewTypeCount = viewTypeCount;
  }

  @Override public ItemView<Object, View> getItemView(Object item) {
    Model model = findModelFor(item);
    if (model == null) {
      throw new IllegalStateException("Unregistered type: " + item.getClass().getName());
    }
    // noinspection unchecked
    ItemView<Object, View> itemView = model.getItemView(item);
    if (itemView == null) {
      // TODO message
      throw new IllegalStateException("");
    }
    return itemView;
  }

  @Override public int getViewTypeCount() {
    return viewTypeCount;
  }

  @Override public boolean hasRegistered(Object item) {
    return findItemViewFor(item) != null;
  }

  private <T> ItemView<T, ?> findItemViewFor(T item) {
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
