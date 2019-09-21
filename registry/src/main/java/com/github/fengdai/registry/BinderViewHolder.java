package com.github.fengdai.registry;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import java.util.List;

/**
 * A {@link RecyclerView.ViewHolder} which is also {@link Binder} like. Provides binding logic
 * which binds instance of {@link DataT} to itself.
 */
public abstract class BinderViewHolder<DataT> extends RecyclerView.ViewHolder {

  public BinderViewHolder(@NonNull View itemView) {
    super(itemView);
  }

  public abstract void bind(DataT data);

  public void bind(DataT data, @NonNull List<Object> payloads) {
    bind(data);
  }
}
