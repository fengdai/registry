package com.github.fengdai.registry.internal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.github.fengdai.viewholder.ViewHolderFactory;

public final class LayoutViewHolderFactory implements ViewHolderFactory<RecyclerView.ViewHolder> {
  @LayoutRes
  private final int layoutRes;

  public LayoutViewHolderFactory(@LayoutRes int layoutRes) {
    this.layoutRes = layoutRes;
  }

  @NonNull @Override public RecyclerView.ViewHolder create(@NonNull ViewGroup parent) {
    return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false));
  }

  private static class ViewHolder extends RecyclerView.ViewHolder {
    ViewHolder(@NonNull View itemView) {
      super(itemView);
    }
  }
}
