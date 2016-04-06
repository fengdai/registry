package com.github.fengdai.registry;

import android.view.View;
import android.view.ViewGroup;

public interface ViewFactory<V extends View> {

  V createView(ViewGroup parent);
}
