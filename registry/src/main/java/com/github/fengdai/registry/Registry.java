package com.github.fengdai.registry;

import android.view.View;
import android.view.ViewGroup;
import com.github.fengdai.registry.internal.RegistryImpl;

public abstract class Registry {
  public abstract View getView(Object model, View convertView, ViewGroup parent);

  public abstract int getItemViewType(Object model);

  public abstract int getViewTypeCount();

  public abstract boolean hasRegistered(Object model);

  public static Registry create(Class<?> clazz) {
    Adapter adapter = clazz.getAnnotation(Adapter.class);
    if (adapter == null) {
      throw new IllegalStateException(
          String.format("%s missing @%s annotation.", clazz.getSimpleName(),
              Adapter.class.getSimpleName()));
    }
    RegistryImpl.Builder builder = new RegistryImpl.Builder();
    Item[] items = adapter.items();
    for (Item item : items) {
      builder.registerItem(item);
    }
    Class<? extends Enum<?>>[] itemSetEnums = adapter.itemSets();
    for (Class<? extends Enum<?>> itemSetEnum : itemSetEnums) {
      ItemSet itemSet = itemSetEnum.getAnnotation(ItemSet.class);
      if (itemSet == null) {
        throw new IllegalStateException(
            String.format("%s missing @%s annotation.", itemSetEnum.getClass().getSimpleName(),
                ItemSet.class.getSimpleName()));
      }
      builder.registerItemSet(itemSet, itemSetEnum);
    }
    return builder.build();
  }
}
