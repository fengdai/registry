package com.github.fengdai.registry;

import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.AsyncDifferConfig;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import java.util.List;

public class RegistryListAdapter<TItem extends Registry.Item>
    extends ListAdapter<TItem, RecyclerView.ViewHolder> {
  private final Registry<TItem> registry;

  public RegistryListAdapter(Registry<TItem> registry,
      @NonNull DiffUtil.ItemCallback<TItem> diffCallback) {
    super(diffCallback);
    this.registry = registry;
  }

  public RegistryListAdapter(Registry<TItem> registry,
      @NonNull AsyncDifferConfig<TItem> config) {
    super(config);
    this.registry = registry;
  }

  @NonNull @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return registry.createViewHolder(parent, viewType);
  }

  @Override public final void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
    throw new IllegalStateException();
  }

  @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position,
      @NonNull List<Object> payloads) {
    registry.bindViewHolder(holder, getItem(position), payloads);
  }

  @Override public int getItemViewType(int position) {
    return registry.getItemViewType(getItem(position));
  }
}