package com.github.fengdai.registry;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import java.util.List;

/**
 * Provides binding logic which binds {@link DataT} to {@link ViewHolderT}.
 */
public abstract class Binder<DataT, ViewHolderT extends RecyclerView.ViewHolder> {

  public abstract void bind(DataT data, ViewHolderT viewHolder);

  public void bind(DataT data, ViewHolderT viewHolder, @NonNull List<Object> payloads) {
    bind(data, viewHolder);
  }
}
