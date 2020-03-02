package com.github.fengdai.registry;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RegistryListAdapter<ItemT extends RegistryItem> extends ListAdapter<ItemT, RecyclerView.ViewHolder> {
  private final AdapterDelegate<ItemT> adapterDelegate;

  public RegistryListAdapter(AdapterDelegate<ItemT> adapterDelegate, @NonNull DiffUtil.ItemCallback<ItemT> diffCallback) {
    super(diffCallback);
    this.adapterDelegate = adapterDelegate;
  }

  public RegistryListAdapter(AdapterDelegate<ItemT> adapterDelegate, @NonNull AsyncDifferConfig<ItemT> config) {
    super(config);
    this.adapterDelegate = adapterDelegate;
  }

  @NonNull @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return adapterDelegate.onCreateViewHolder(parent, viewType);
  }

  @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    adapterDelegate.onBindViewHolder(holder, getItem(position));
  }

  @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
    adapterDelegate.onBindViewHolder(holder, getItem(position), payloads);
  }

  @Override public int getItemViewType(int position) {
    return adapterDelegate.getItemViewType(getItem(position));
  }
}
