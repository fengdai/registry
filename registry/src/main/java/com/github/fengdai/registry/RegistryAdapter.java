package com.github.fengdai.registry;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

public abstract class RegistryAdapter<TItem extends Registry.Item>
    extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private final Registry<TItem> registry;

  protected RegistryAdapter(Registry<TItem> registry) {
    this.registry = registry;
  }

  @NonNull @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return registry.createViewHolder(parent, viewType);
  }

  @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    registry.bindViewHolder(holder, getItem(position));
  }

  @Override public int getItemViewType(int position) {
    return registry.getItemViewType(getItem(position));
  }

  public abstract TItem getItem(int position);
}
