package com.github.fengdai.registry;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import java.util.List;

/**
 * Provides binding logic which binds {@link TData} to {@link TViewHolder}.
 */
public abstract class Binder<TData, TViewHolder extends RecyclerView.ViewHolder> {

  public abstract void bind(TData data, TViewHolder viewHolder);

  public void bind(TData data, TViewHolder viewHolder, @NonNull List<Object> payloads) {
    bind(data, viewHolder);
  }
}
