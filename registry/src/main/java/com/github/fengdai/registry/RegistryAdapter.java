package com.github.fengdai.registry;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public abstract class RegistryAdapter<ItemT extends RegistryItem> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
  private final AdapterDelegate<ItemT> adapterDelegate;

  protected RegistryAdapter(AdapterDelegate<ItemT> adapterDelegate) {
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

  public abstract ItemT getItem(int position);
}
