package com.github.fengdai.registry.internal;

import com.github.fengdai.registry.Mapper;
import java.util.Map;

public class MultiModel extends Model {
  private final Class<? extends Mapper<?, ? extends Enum<?>>> mapperClass;
  private Mapper<Object, ? extends Enum<?>> mapper = null;
  private final Map<? extends Enum<?>, ItemView> itemMap;

  public MultiModel(Class<? extends Mapper<?, ? extends Enum<?>>> mapperClass,
      Map<? extends Enum<?>, ItemView> itemMap) {
    this.mapperClass = mapperClass;
    this.itemMap = itemMap;
  }

  @Override public ItemView getItemView(Object model) {
    return itemMap.get(getMapper().map(model));
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
