package com.github.fengdai.registry;

import android.content.Context;
import android.view.View;

public interface ViewProvider<V extends View> {

  V provideView(Context context);
}
