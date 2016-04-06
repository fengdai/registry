package com.github.fengdai.registry;

public @interface Provider {

  Class<? extends ViewProvider<?>> value();
}
