package com.github.fengdai.registry;

public @interface Factory {

  Class<? extends ViewFactory<?>> value();
}
