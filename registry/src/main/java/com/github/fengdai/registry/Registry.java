package com.github.fengdai.registry;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;

public abstract class Registry<ItemT> {
  protected final static Binder BINDER_VIEW_HOLDER_BINDER =
      new Binder<Object, BinderViewHolder<Object>>() {
        @Override public void bind(Object o, BinderViewHolder<Object> viewHolder) {
          throw new IllegalStateException();
        }

        @Override public void bind(Object o, BinderViewHolder<Object> viewHolder,
            @NonNull List<Object> payloads) {
          viewHolder.bind(o, payloads);
        }
      };

  private final SparseArray<ViewHolderFactory> viewHolderFactories = new SparseArray<>();

  protected void registerViewHolderFactory(int viewType, ViewHolderFactory factory) {
    viewHolderFactories.put(viewType, factory);
  }

  protected void registerStaticContentLayout(final int viewType, @LayoutRes final int layoutRes) {
    viewHolderFactories.put(viewType, new ViewHolderFactory() {
      @NonNull @Override public RecyclerView.ViewHolder create(@NonNull ViewGroup parent) {
        return new StaticContentLayoutViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false));
      }
    });
  }

  public final int getItemViewTypeCount() {
    return viewHolderFactories.size();
  }

  public abstract Object getItemData(ItemT item);

  public abstract int getItemViewType(ItemT item);

  public abstract Binder getItemBinder(ItemT item);

  @NonNull public final RecyclerView.ViewHolder createViewHolder(ViewGroup parent, int viewType) {
    return viewHolderFactories.get(viewType).create(parent);
  }

  @SuppressWarnings("unchecked")
  public final void bindViewHolder(RecyclerView.ViewHolder viewHolder, ItemT item,
      @NonNull List<Object> payloads) {
    Binder binder = getItemBinder(item);
    if (binder != null) {
      binder.bind(getItemData(item), viewHolder, payloads);
    }
  }

  private class StaticContentLayoutViewHolder extends RecyclerView.ViewHolder {
    StaticContentLayoutViewHolder(@NonNull View itemView) {
      super(itemView);
    }
  }
}
