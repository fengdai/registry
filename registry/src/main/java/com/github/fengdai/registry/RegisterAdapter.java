package com.github.fengdai.registry;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class RegisterAdapter extends BaseAdapter {
  private final Registry registry;

  protected RegisterAdapter(Registry registry) {
    this.registry = registry;
  }

  @Override public View getView(int position, View convertView, ViewGroup parent) {
    return registry.getView(getItem(position), convertView, parent);
  }

  @Override public int getItemViewType(int position) {
    return registry.getItemViewType(getItem(position));
  }

  @Override public int getViewTypeCount() {
    return registry.getViewTypeCount();
  }
}
