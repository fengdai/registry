package com.github.fengdai.registry.internal;

import android.view.View;
import com.github.fengdai.registry.Mapper;
import com.github.fengdai.registry.Registry;
import com.github.fengdai.registry.ViewBinder;
import com.github.fengdai.registry.ViewProvider;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Model<T> {
  protected final Class<T> modelClass;

  Model(Class<T> modelClass) {
    this.modelClass = modelClass;
  }

  Class<T> getModelClass() {
    return modelClass;
  }

  abstract Registry.ItemView<T, ?> getItemView(T item);

  public static <T> Builder<T> oneToOne(Class<T> modelClass) {
    return new Builder<>(modelClass);
  }

  public static <T> Builder<T> oneToMany(Class<T> modelClass,
      Class<? extends Mapper<T, Class<? extends ViewBinder<T, ?>>>> mapperClass) {
    return new Builder<>(modelClass, mapperClass);
  }

  public static class Builder<T> {
    private final Class<T> modelClass;
    private final Class<? extends Mapper<T, Class<? extends ViewBinder<T, ?>>>> mapperClass;
    private Map<Class<? extends ViewBinder<T, ?>>, Registry.ItemView<T, ?>> itemViewMap =
        new LinkedHashMap<>();
    private Registry.ItemView<T, ?> itemView;

    private Builder(Class<T> modelClass) {
      this.modelClass = modelClass;
      this.mapperClass = null;
    }

    private Builder(Class<T> modelClass,
        Class<? extends Mapper<T, Class<? extends ViewBinder<T, ?>>>> mapperClass) {
      this.modelClass = modelClass;
      this.mapperClass = mapperClass;
    }

    public <V extends View> Builder<T> add(int itemViewType,
        Class<? extends ViewBinder<T, V>> viewBinderClass, final int layoutRes) {
      Registry.ItemView<T, V> itemView =
          new IvForLayoutRes<>(modelClass, itemViewType, viewBinderClass, layoutRes);
      if (mapperClass == null) {
        this.itemView = itemView;
      } else {
        itemViewMap.put(viewBinderClass, itemView);
      }
      return this;
    }

    public <BV extends View, PV extends BV> Builder<T> add(int itemViewType,
        Class<? extends ViewBinder<T, BV>> viewBinderClass,
        Class<? extends ViewProvider<PV>> viewProviderClass) {
      Registry.ItemView<T, BV> itemView =
          new IvForViewProvider<>(modelClass, itemViewType, viewBinderClass, viewProviderClass);
      if (mapperClass == null) {
        this.itemView = itemView;
      } else {
        itemViewMap.put(viewBinderClass, itemView);
      }
      return this;
    }

    public Model<T> build() {
      if (mapperClass == null) {
        return new ModelToOne<>(modelClass, itemView);
      } else {
        return new ModelToMany<>(modelClass, mapperClass, itemViewMap);
      }
    }
  }
}
