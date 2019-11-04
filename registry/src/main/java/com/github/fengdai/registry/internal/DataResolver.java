package com.github.fengdai.registry.internal;

import android.support.annotation.Nullable;
import com.github.fengdai.registry.Binder;

public final class DataResolver {
  public final int viewType;
  @Nullable public final Binder binder;

  public DataResolver(int viewType, @Nullable Binder binder) {
    this.viewType = viewType;
    this.binder = binder;
  }
}
