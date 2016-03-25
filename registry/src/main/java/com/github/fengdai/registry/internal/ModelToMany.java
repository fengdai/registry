package com.github.fengdai.registry.internal;

import com.github.fengdai.registry.Mapper;
import com.github.fengdai.registry.Registry;
import com.github.fengdai.registry.ViewBinder;
import java.util.Map;

class ModelToMany<T> extends Model<T> {
  private final Class<? extends Mapper<T, Class<? extends ViewBinder<T, ?>>>> mapperClass;
  private Mapper<T, Class<? extends ViewBinder<T, ?>>> mapper = null;
  private final Map<Class<? extends ViewBinder<T, ?>>, Registry.ItemView<T, ?>> itemMap;

  ModelToMany(Class<T> modelClass,
      Class<? extends Mapper<T, Class<? extends ViewBinder<T, ?>>>> mapperClass,
      Map<Class<? extends ViewBinder<T, ?>>, Registry.ItemView<T, ?>> itemMap) {
    super(modelClass);
    this.mapperClass = mapperClass;
    this.itemMap = itemMap;
  }

  @Override Registry.ItemView<T, ?> getItemView(T item) {
    if (mapper == null) {
      mapper = Utils.newInstanceOf(mapperClass);
    }
    return itemMap.get(mapper.map(item));
  }
}
