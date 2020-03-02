package com.github.fengdai.viewholder;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public interface ViewHolderFactory<ViewHolderT extends RecyclerView.ViewHolder> {

  @NonNull ViewHolderT create(@NonNull ViewGroup parent);
}
