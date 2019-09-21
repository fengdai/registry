package com.github.fengdai.registry;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import java.util.List;

/**
 * A {@link RecyclerView.ViewHolder} which is also {@link Binder} like. Provides binding logic
 * which binds instance of {@link TData} to itself.
 */
public abstract class BinderViewHolder<TData> extends RecyclerView.ViewHolder {

  public BinderViewHolder(@NonNull View itemView) {
    super(itemView);
  }

  public abstract void bind(TData data);

  public void bind(TData data, @NonNull List<Object> payloads) {
    bind(data);
  }
}
