package com.github.fengdai.registry;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import com.github.fengdai.registry.internal.Utils;

public abstract class Registry<TItem extends Registry.Item> {
  public abstract static class Item {
    public final Object data;
    public final int viewType;
    public final Binder<?, ? extends RecyclerView.ViewHolder> binder;

    protected Item(Object data, int viewType, Binder<?, ? extends RecyclerView.ViewHolder> binder) {
      this.data = data;
      this.viewType = viewType;
      this.binder = binder;
    }
  }

  protected final static Binder BINDABLE_VIEW_HOLDER_BINDER =
      new Binder<Object, BindableViewHolder<Object>>() {
        @Override public void bind(Object o, BindableViewHolder<Object> viewHolder) {
          viewHolder.bind(o);
        }
      };

  private final SparseArray<ViewHolderFactory> viewHolderFactories = new SparseArray<>();

  protected void registerViewHolderFactory(int viewType, ViewHolderFactory factory) {
    viewHolderFactories.put(viewType, factory);
  }

  protected void registerStaticContentLayout(final int viewType, @LayoutRes final int layoutRes) {
    viewHolderFactories.put(viewType, new ViewHolderFactory() {
      @Override public RecyclerView.ViewHolder create(ViewGroup parent) {
        return new StaticContentLayoutViewHolder(Utils.inflate(parent, layoutRes));
      }
    });
  }

  public final int getItemViewType(TItem item) {
    return item.viewType;
  }

  @NonNull public final RecyclerView.ViewHolder createViewHolder(ViewGroup parent, int viewType) {
    return viewHolderFactories.get(viewType).create(parent);
  }

  public final void bindViewHolder(RecyclerView.ViewHolder viewHolder, TItem item) {
    Binder binder = item.binder;
    if (binder != null) {
      // noinspection unchecked
      binder.bind(item.data, viewHolder);
    }
  }

  protected static Object staticContentLayoutData(@LayoutRes int layoutRes) {
    return new StaticContentLayoutData(layoutRes);
  }

  private static class StaticContentLayoutData {
    @LayoutRes int layoutRes;

    StaticContentLayoutData(int layoutRes) {
      this.layoutRes = layoutRes;
    }

    @Override public int hashCode() {
      return super.hashCode();
    }

    @Override public boolean equals(@Nullable Object that) {
      if (this == that) {
        return true;
      }
      if (that instanceof StaticContentLayoutData) {
        return ((StaticContentLayoutData) that).layoutRes == layoutRes;
      }
      return false;
    }

    @NonNull @Override public String toString() {
      return "StaticContentLayoutData(layoutRes=" + layoutRes + ")";
    }
  }

  private class StaticContentLayoutViewHolder extends RecyclerView.ViewHolder {
    StaticContentLayoutViewHolder(@NonNull View itemView) {
      super(itemView);
    }
  }
}
