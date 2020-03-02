package com.github.fengdai.registry.internal;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.github.fengdai.viewholder.ViewHolderFactory;
import javax.inject.Provider;

public final class ViewHolderFactories {

  public static <T extends RecyclerView.ViewHolder> ViewHolderFactory<T> create(final Provider<T> provider) {
    return new ViewHolderFactory<T>() {
      @NonNull @Override public T create(@NonNull ViewGroup parent) {
        return provider.get();
      }
    };
  }
}
