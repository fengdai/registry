package com.github.fengdai.registry;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public abstract class BindableViewHolder<TData> extends RecyclerView.ViewHolder {

  public BindableViewHolder(@NonNull View itemView) {
    super(itemView);
  }

  public abstract void bind(TData data);
}
