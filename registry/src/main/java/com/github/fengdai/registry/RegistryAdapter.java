package com.github.fengdai.registry;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class RegistryAdapter extends BaseAdapter {
  private final Registry registry;

  protected RegistryAdapter(Registry registry) {
    this.registry = registry;
  }

  @Override public View getView(int position, View convertView, ViewGroup parent) {
    Object item = getItem(position);
    Registry.ItemView<Object, View> itemView = registry.getItemView(item);
    if (convertView == null) {
      convertView = itemView.providerView(parent);
    }
    itemView.bindView(item, convertView);
    return convertView;
  }

  @Override public int getItemViewType(int position) {
    return registry.getItemView(getItem(position)).getType();
  }

  @Override public int getViewTypeCount() {
    return registry.getViewTypeCount();
  }
}
