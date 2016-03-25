package com.github.fengdai.registry.internal;

import com.github.fengdai.registry.Registry;

class ModelToOne<T> extends Model<T> {
  private final Registry.ItemView<T, ?> itemView;

  ModelToOne(Class<T> modelClass, Registry.ItemView<T, ?> itemView) {
    super(modelClass);
    this.itemView = itemView;
  }

  @Override Registry.ItemView<T, ?> getItemView(T item) {
    return this.itemView;
  }
}
