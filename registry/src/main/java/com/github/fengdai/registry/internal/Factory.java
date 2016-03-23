package com.github.fengdai.registry.internal;

import com.github.fengdai.registry.Adapter;
import com.github.fengdai.registry.Ignore;
import com.github.fengdai.registry.Item;
import com.github.fengdai.registry.ItemSet;
import com.github.fengdai.registry.Mapper;
import com.github.fengdai.registry.Registry;
import com.github.fengdai.registry.ViewProvider;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Factory {
  private final Map<Class<?>, Model> models = new LinkedHashMap<>();
  List<Object> viewTypes = new LinkedList<>();

  public static Registry create(Class<?> clazz) {
    Adapter adapter = clazz.getAnnotation(Adapter.class);
    if (adapter == null) {
      throw new IllegalStateException(
          String.format("%s missing @%s annotation.", clazz.getSimpleName(),
              Adapter.class.getSimpleName()));
    }
    Factory factory = new Factory();
    Item[] items = adapter.items();
    for (Item item : items) {
      factory.registerItem(item);
    }
    Class<? extends Enum<?>>[] itemSetEnums = adapter.itemSets();
    for (Class<? extends Enum<?>> itemSetEnum : itemSetEnums) {
      factory.registerItemSet(itemSetEnum);
    }
    return factory.build();
  }

  private Factory registerItem(Item item) {
    models.put(item.model(), new ClassModel(item.model(), parse(item)));
    return this;
  }

  private Factory registerItemSet(Class<? extends Enum<?>> itemSetEnum) {
    ItemSet itemSet = itemSetEnum.getAnnotation(ItemSet.class);
    if (itemSet == null) {
      throw new IllegalStateException(
          String.format("%s missing @%s annotation.", itemSetEnum.getClass().getSimpleName(),
              ItemSet.class.getSimpleName()));
    }
    final Class<?> modelClass = itemSet.model();
    final Class<? extends Mapper<?, ?>> mapperClass = itemSet.mapper();
    final Map<Enum<?>, ItemView> items = new LinkedHashMap<>();
    final List<Class<?>> knownModelClasses = new LinkedList<>();
    knownModelClasses.add(modelClass);
    for (Enum<?> enumConstant : itemSetEnum.getEnumConstants()) {
      try {
        Field field = itemSetEnum.getField(enumConstant.name());
        if (field.getAnnotation(Ignore.class) != null) {
          continue;
        }
        Item item = field.getAnnotation(Item.class);
        if (item == null) {
          throw new IllegalStateException(
              String.format("%s.%s missing @%s annotation.", itemSetEnum.getSimpleName(),
                  enumConstant.name(), Item.class.getSimpleName()));
        }
        if (!modelClass.isAssignableFrom(item.model())) {
          throw new IllegalStateException(
              String.format("Can't assign %s Item to %s ItemSet", item.model().getSimpleName(),
                  modelClass.getSimpleName()));
        }
        if (!knownModelClasses.contains(item.model())) {
          knownModelClasses.add(item.model());
        }
        items.put(enumConstant, parse(item));
      } catch (NoSuchFieldException e) {
        throw new AssertionError(e);
      }
    }
    for (Class<?> knownClass : knownModelClasses) {
      models.put(knownClass, new MultiModel(modelClass, mapperClass, items));
    }
    return this;
  }

  private ItemView parse(Item item) {
    Class<? extends ViewProvider<?>> viewProviderClass = item.view();
    int viewLayoutRes = item.layout();
    if (viewLayoutRes != -1 && viewProviderClass != Item.NONE.class) {
      // TODO message
      throw new IllegalStateException();
    }
    ItemView itemView;
    if (viewLayoutRes != -1) {
      itemView = new ItemView(typeOf(viewLayoutRes), viewLayoutRes, item.binder());
    } else if (viewProviderClass != Item.NONE.class) {
      itemView = new ItemView(typeOf(viewProviderClass), viewProviderClass, item.binder());
    } else {
      // TODO message
      throw new IllegalStateException();
    }
    return itemView;
  }

  private int typeOf(Object view) {
    if (!viewTypes.contains(view)) {
      viewTypes.add(view);
    }
    return viewTypes.indexOf(view);
  }

  private RegistryImpl build() {
    return new RegistryImpl(models, viewTypes.size());
  }
}
