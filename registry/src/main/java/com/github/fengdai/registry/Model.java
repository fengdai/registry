package com.github.fengdai.registry;

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

  static class ModelToMany<T> extends Model<T> {
    private Mapper<T, Class<? extends ViewBinder<T, ?>>> mapper = null;
    private final Map<Class<? extends ViewBinder<T, ?>>, Registry.ItemView<T, ?>> itemMap;

    ModelToMany(Class<T> modelClass, Mapper<T, Class<? extends ViewBinder<T, ?>>> mapper,
        Map<Class<? extends ViewBinder<T, ?>>, Registry.ItemView<T, ?>> itemMap) {
      super(modelClass);
      this.mapper = mapper;
      this.itemMap = itemMap;
    }

    @Override Registry.ItemView<T, ?> getItemView(T item) {
      return itemMap.get(mapper.map(item));
    }
  }

  static class ModelToOne<T> extends Model<T> {
    private final Registry.ItemView<T, ?> itemView;

    ModelToOne(Class<T> modelClass, Registry.ItemView<T, ?> itemView) {
      super(modelClass);
      this.itemView = itemView;
    }

    @Override Registry.ItemView<T, ?> getItemView(T item) {
      return this.itemView;
    }
  }
}
