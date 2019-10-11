package com.github.fengdai.registry;

import android.support.annotation.LayoutRes;

public final class StaticContentLayoutData {
  private final int layoutRes;

  public StaticContentLayoutData(@LayoutRes int layoutRes) {
    this.layoutRes = layoutRes;
  }

  public int getLayoutRes() {
    return layoutRes;
  }
}
