package com.github.fengdai.registry.internal;

import com.github.fengdai.registry.Mapper;
import java.util.Map;

class MultiModel extends Model {
  private final Class<? extends Mapper<?, ? extends Enum<?>>> mapperClass;
  private Mapper<Object, ? extends Enum<?>> mapper = null;
  private final Map<? extends Enum<?>, ItemView> itemMap;

  MultiModel(Class<?> modelClass, Class<? extends Mapper<?, ? extends Enum<?>>> mapperClass,
      Map<? extends Enum<?>, ItemView> itemMap) {
    super(modelClass);
    this.mapperClass = mapperClass;
    this.itemMap = itemMap;
  }

  @Override ItemView getItemView(Object item) {
    return itemMap.get(getMapper().map(item));
  }

  Mapper<Object, ? extends Enum<?>> getMapper() {
    if (mapper != null) {
      return mapper;
    }
    try {
      //noinspection unchecked
      mapper = (Mapper<Object, Enum<?>>) mapperClass.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(String.format("Unable to create %s.", mapperClass.getName()), e);
    }
    return mapper;
  }
}
