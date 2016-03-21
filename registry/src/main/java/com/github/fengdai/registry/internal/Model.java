package com.github.fengdai.registry.internal;

import android.view.View;
import android.view.ViewGroup;

abstract class Model {

  View getView(Object model, View convertView, ViewGroup parent) {
    ItemView itemView = getItemView(model);
    if (convertView == null) {
      convertView = itemView.providerView(model, parent);
    }
    itemView.bindView(model, convertView);
    return convertView;
  }

  int getItemViewType(Object model) {
    return getItemView(model).getItemViewType();
  }

  public abstract ItemView getItemView(Object model);
}
