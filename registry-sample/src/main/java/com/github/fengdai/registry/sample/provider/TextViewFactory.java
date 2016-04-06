package com.github.fengdai.registry.sample.provider;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import com.github.fengdai.registry.ViewFactory;

public class TextViewFactory implements ViewFactory<TextView> {
  @Override public TextView createView(ViewGroup parent) {
    return (TextView) LayoutInflater.from(parent.getContext())
        .inflate(android.R.layout.simple_list_item_1, parent, false);
  }
}
