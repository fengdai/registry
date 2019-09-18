package com.github.fengdai.registry;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import java.util.List;

/**
 * Provides binding logic which binds {@code DataT} to {@code ViewHolderT}.
 * <p>
 * Subclasses must have a public no-argument constructor.
 *
 * @see BinderViewHolder
 */
public abstract class Binder<DataT, ViewHolderT extends RecyclerView.ViewHolder> {

  /**
   * Binds the {@code data} to {@code viewHolder}.
   *
   * @see #bind(Object, RecyclerView.ViewHolder, List)
   */
  public abstract void bind(DataT data, ViewHolderT viewHolder);

  /**
   * Binds the {@code data} to {@code viewHolder}. If the the payloads list is not empty, an
   * efficient partial update using the payload info may be applied.
   *
   * @see #bind(Object, RecyclerView.ViewHolder)
   */
  public void bind(DataT data, ViewHolderT viewHolder, @NonNull List<Object> payloads) {
    bind(data, viewHolder);
  }
}
