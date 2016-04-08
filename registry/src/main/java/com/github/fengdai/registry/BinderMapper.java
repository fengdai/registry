package com.github.fengdai.registry;

public interface BinderMapper<T> extends Mapper<T, Class<? extends ViewBinder<T, ?>>> {
}
