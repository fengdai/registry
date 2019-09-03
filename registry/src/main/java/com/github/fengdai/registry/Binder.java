package com.github.fengdai.registry;

import android.support.v7.widget.RecyclerView;

public abstract class Binder<TData, TViewHolder extends RecyclerView.ViewHolder> {
  public abstract void bind(TData data, TViewHolder viewHolder);
}
