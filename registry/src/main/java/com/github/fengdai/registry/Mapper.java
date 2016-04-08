package com.github.fengdai.registry;

interface Mapper<T, K> {

  K map(T model);
}
