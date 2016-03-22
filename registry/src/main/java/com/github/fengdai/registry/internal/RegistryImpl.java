package com.github.fengdai.registry.internal;

import android.view.View;
import android.view.ViewGroup;
import com.github.fengdai.registry.Registry;
import java.util.Map;
import java.util.Set;

class RegistryImpl extends Registry {
  private final Map<Class<?>, Model> models;
  private final int viewTypeCount;

  RegistryImpl(Map<Class<?>, Model> models, int viewTypeCount) {
    this.models = models;
    this.viewTypeCount = viewTypeCount;
  }

  @Override public View getView(Object item, View convertView, ViewGroup parent) {
    ItemView itemView = getItemView(item);
    if (convertView == null) {
      convertView = itemView.providerView(item, parent);
    }
    itemView.bindView(item, convertView);
    return convertView;
  }

  @Override public int getItemViewType(Object item) {
    return getItemView(item).getItemViewType();
  }

  @Override public int getViewTypeCount() {
    return viewTypeCount;
  }

  @Override public boolean hasRegistered(Object item) {
    return findItemViewFor(item) != null;
  }

  private ItemView getItemView(Object item) {
    Model model = findModelFor(item);
    if (model == null) {
      throw new IllegalStateException("Unregistered type: " + item.getClass().getName());
    }
    ItemView itemView = model.getItemView(item);
    if (itemView == null) {
      // TODO message
      throw new IllegalStateException("");
    }
    return itemView;
  }

  private ItemView findItemViewFor(Object item) {
    Model model = findModelFor(item);
    return model != null ? model.getItemView(item) : null;
  }

  private Model findModelFor(Object item) {
    Model model = models.get(item.getClass());
    if (model == null) {
      Set<Map.Entry<Class<?>, Model>> entrySet = models.entrySet();
      for (Map.Entry<Class<?>, Model> entry : entrySet) {
        if (entry.getKey().isAssignableFrom(item.getClass())) {
          model = entry.getValue();
          models.put(item.getClass(), model);
          break;
        }
      }
    }
    return model;
  }
}
