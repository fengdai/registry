package com.github.fengdai.viewholder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

public interface ViewHolderFactory<ViewHolderT extends RecyclerView.ViewHolder> {
  @NonNull ViewHolderT create(@NonNull ViewGroup parent);
}
