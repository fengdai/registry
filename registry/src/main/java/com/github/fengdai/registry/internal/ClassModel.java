package com.github.fengdai.registry.internal;

public class ClassModel extends Model {
  private final ItemView itemView;

  public ClassModel(ItemView itemView) {
    this.itemView = itemView;
  }

  @Override public ItemView getItemView(Object model) {
    return this.itemView;
  }
}
