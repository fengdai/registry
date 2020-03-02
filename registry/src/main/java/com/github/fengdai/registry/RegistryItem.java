package com.github.fengdai.registry;

import androidx.annotation.Nullable;

public interface RegistryItem {

  Object getData();

  int getViewType();

  @Nullable Binder getBinder();
}
