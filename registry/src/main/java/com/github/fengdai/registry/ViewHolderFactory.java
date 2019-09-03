package com.github.fengdai.registry;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

public interface ViewHolderFactory {
  RecyclerView.ViewHolder create(ViewGroup parent);
}
