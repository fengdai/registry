package com.github.fengdai.registry.internal;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import com.github.fengdai.viewholder.ViewHolderFactory;
import javax.inject.Provider;

public class ViewHolderFactories {

  public static <T extends RecyclerView.ViewHolder> ViewHolderFactory<T> create(final Provider<T> provider) {
    return new ViewHolderFactory<T>() {
      @NonNull @Override public T create(@NonNull ViewGroup parent) {
        return provider.get();
      }
    };
  }
}
