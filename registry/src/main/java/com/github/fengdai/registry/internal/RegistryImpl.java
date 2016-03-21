package com.github.fengdai.registry.internal;

import android.view.View;
import android.view.ViewGroup;
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
import java.util.Set;

public class RegistryImpl extends Registry {
  private final Map<Class<?>, Model> models;
  private final int viewTypeCount;

  public RegistryImpl(Map<Class<?>, Model> models, int viewTypeCount) {
    this.models = models;
    this.viewTypeCount = viewTypeCount;
  }

  public View getView(Object item, View convertView, ViewGroup parent) {
    return getModel(item).getView(item, convertView, parent);
  }

  private Model getModel(Object item) {
    Model model = findModelFor(item);
    if (model == null) {
      throw new RuntimeException("Unregistered type: " + item.getClass().getName());
    }
    return model;
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

  @Override public int getItemViewType(Object item) {
    return getModel(item).getItemViewType(item);
  }

  public int getViewTypeCount() {
    return viewTypeCount;
  }

  @Override public boolean hasRegistered(Object item) {
    return findModelFor(item) != null;
  }

  public static class Builder {
    private final Map<Class<?>, Model> models = new LinkedHashMap<>();
    List<Object> viewTypes = new LinkedList<>();

    public Builder registerItem(Item item) {
      models.put(item.model(), new ClassModel(parse(item)));
      return this;
    }

    public Builder registerItemSet(ItemSet itemSet, Class<? extends Enum<?>> itemSetEnum) {
      final Class<?> modelClass = itemSet.model();
      final Class<? extends Mapper<?, ?>> mapperClass = itemSet.mapper();
      final Map<Enum<?>, ItemView> items = new LinkedHashMap<>();
      final List<Class<?>> knownModelClasses = new LinkedList<>();
      knownModelClasses.add(modelClass);
      for (Enum<?> enumConstant : itemSetEnum.getEnumConstants()) {
        try {
          Field field = itemSetEnum.getField(enumConstant.name());
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
        models.put(knownClass, new MultiModel(mapperClass, items));
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

    public RegistryImpl build() {
      return new RegistryImpl(models, viewTypes.size());
    }
  }
}
