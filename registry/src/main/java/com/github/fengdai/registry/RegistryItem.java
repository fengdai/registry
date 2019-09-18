package com.github.fengdai.registry;

import android.support.annotation.Nullable;

public interface RegistryItem {

  Object getData();

  int getViewType();

  @Nullable Binder getBinder();
}
