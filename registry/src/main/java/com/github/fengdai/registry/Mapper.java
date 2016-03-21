package com.github.fengdai.registry;

public interface Mapper<T, E extends Enum<?>> {

  E map(T model);
}
