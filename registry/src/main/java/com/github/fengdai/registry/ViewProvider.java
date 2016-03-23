package com.github.fengdai.registry;

import android.view.View;
import android.view.ViewGroup;

public interface ViewProvider<V extends View> {

  V provideView(ViewGroup parent);
}
