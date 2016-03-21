package com.github.fengdai.registry.sample.provider;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;
import com.github.fengdai.registry.ViewProvider;

public class TextViewProvider implements ViewProvider<TextView> {
  @Override public TextView provideView(Context context) {
    return (TextView) LayoutInflater.from(context)
        .inflate(android.R.layout.simple_list_item_1, null, false);
  }
}
