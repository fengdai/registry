package com.github.fengdai.registry.internal;

import android.view.View;
import com.github.fengdai.registry.Mapper;
import com.github.fengdai.registry.Registry;
import com.github.fengdai.registry.ViewBinder;
import com.github.fengdai.registry.ViewFactory;
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
      Mapper<T, Class<? extends ViewBinder<T, ?>>> mapper) {
    return new Builder<>(modelClass, mapper);
  }

  public static class Builder<T> {
    private final Class<T> modelClass;
    private final Mapper<T, Class<? extends ViewBinder<T, ?>>> mapper;
    private Map<Class<? extends ViewBinder<T, ?>>, Registry.ItemView<T, ?>> itemViewMap =
        new LinkedHashMap<>();
    private Registry.ItemView<T, ?> itemView;

    private Builder(Class<T> modelClass) {
      this.modelClass = modelClass;
      this.mapper = null;
    }

    private Builder(Class<T> modelClass, Mapper<T, Class<? extends ViewBinder<T, ?>>> mapper) {
      this.modelClass = modelClass;
      this.mapper = mapper;
    }

    public <BV extends View, FV extends BV> Builder<T> add(int itemViewType,
        ViewBinder<T, BV> viewBinder, ViewFactory<FV> viewFactory) {
      Registry.ItemView<T, BV> itemView =
          new Iv<>(modelClass, itemViewType, viewBinder, viewFactory);
      if (mapper == null) {
        this.itemView = itemView;
      } else {
        // noinspection unchecked
        itemViewMap.put((Class<? extends ViewBinder<T, ?>>) viewBinder.getClass(), itemView);
      }
      return this;
    }

    public Model<T> build() {
      if (mapper == null) {
        return new ModelToOne<>(modelClass, itemView);
      } else {
        return new ModelToMany<>(modelClass, mapper, itemViewMap);
      }
    }
  }
}
