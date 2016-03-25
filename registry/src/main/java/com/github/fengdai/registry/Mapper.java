package com.github.fengdai.registry;

public interface Mapper<T, E extends Class<? extends ViewBinder<T, ?>>> {

  E map(T model);
}
