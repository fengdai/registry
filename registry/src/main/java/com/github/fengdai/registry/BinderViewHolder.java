package com.github.fengdai.registry;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import java.util.List;

/**
 * A {@code RecyclerView.ViewHolder} which is also {@link Binder} like. Provides binding logic which
 * binds instance of {@code DataT} to itself.
 *
 * @see Binder
 */
public abstract class BinderViewHolder<DataT> extends RecyclerView.ViewHolder {

  public BinderViewHolder(@NonNull View itemView) {
    super(itemView);
  }

  /**
   * Binds the {@code data} to this ViewHolder.
   *
   * @see #bind(Object, List)
   */
  public abstract void bind(DataT data);

  /**
   * Binds the {@code data} to this ViewHolder. If the the payloads list is not empty, an efficient
   * partial update using the payload info may be applied.
   *
   * @see #bind(Object)
   */
  public void bind(DataT data, @NonNull List<Object> payloads) {
    bind(data);
  }
}
