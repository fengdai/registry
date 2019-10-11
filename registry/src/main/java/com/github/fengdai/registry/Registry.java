package com.github.fengdai.registry;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.github.fengdai.viewholder.ViewHolderFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Registry {
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
  private final SparseIntArray staticContentLayoutViewTypes = new SparseIntArray();
  private final Map<Class<?>, DataResolverBucket> dataResolverBuckets = new HashMap<>();

  protected void registerViewHolderFactory(int viewType, ViewHolderFactory factory) {
    viewHolderFactories.put(viewType, factory);
  }

  protected void registerDataType(Class<?> dataType, DataResolverBucket bucket) {
    dataResolverBuckets.put(dataType, bucket);
  }

  protected DataResolverBucket singleDataResolverBucket(int viewType, Binder binder) {
    return new SingleDataResolverBucket(viewType, binder);
  }

  public final int getItemViewType(Object data) {
    DataResolver resolver = getResolver(data);
    if (resolver != null) {
      return resolver.viewType;
    } else if (data instanceof StaticContentLayoutData) {
      @LayoutRes final int layoutRes = ((StaticContentLayoutData) data).getLayoutRes();
      int viewType = staticContentLayoutViewTypes.get(layoutRes, -1);
      if (viewType == -1) {
        viewType = viewHolderFactories.size();
        registerViewHolderFactory(viewType, new StaticContentLayoutViewHolderFactory(layoutRes));
        staticContentLayoutViewTypes.put(layoutRes, viewType);
      }
      return viewType;
    } else {
      throw new IllegalStateException("Can't resolve " + data);
    }
  }

  @SuppressWarnings("unchecked")
  @Nullable
  private DataResolver getResolver(Object data) {
    if (data == null || data instanceof StaticContentLayoutData) {
      return null;
    } else {
      Class<?> clazz = data.getClass();
      DataResolverBucket bucket = dataResolverBuckets.get(clazz);
      if (bucket == null) {
        for (Map.Entry<Class<?>, DataResolverBucket> entry : dataResolverBuckets.entrySet()) {
          if (entry.getKey().isInstance(data)) {
            bucket = entry.getValue();
            dataResolverBuckets.put(clazz, bucket);
            break;
          }
        }
      }
      return bucket != null ? bucket.getDataResolver(data) : null;
    }
  }

  @NonNull public final RecyclerView.ViewHolder createViewHolder(ViewGroup parent, int viewType) {
    return viewHolderFactories.get(viewType).create(parent);
  }

  @SuppressWarnings("unchecked")
  public final void bindViewHolder(RecyclerView.ViewHolder viewHolder, Object data,
      @NonNull List<Object> payloads) {
    DataResolver resolver = getResolver(data);
    if (resolver != null) {
      resolver.binder.bind(data, viewHolder, payloads);
    }
  }

  private final static class StaticContentLayoutViewHolderFactory
      implements ViewHolderFactory<RecyclerView.ViewHolder> {
    private final int layoutRes;

    private StaticContentLayoutViewHolderFactory(@LayoutRes final int layoutRes) {
      this.layoutRes = layoutRes;
    }

    @NonNull @Override public RecyclerView.ViewHolder create(@NonNull ViewGroup parent) {
      return new RecyclerView.ViewHolder(
          LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false)) {
      };
    }
  }

  protected static class DataResolver {
    final int viewType;
    final Binder binder;

    public DataResolver(int viewType, Binder binder) {
      this.viewType = viewType;
      this.binder = binder;
    }
  }

  protected interface DataResolverBucket<T> {
    DataResolver getDataResolver(T data);
  }

  private static class SingleDataResolverBucket<T> extends DataResolver
      implements DataResolverBucket<T> {
    private SingleDataResolverBucket(int viewType, Binder binder) {
      super(viewType, binder);
    }

    @Override public DataResolver getDataResolver(T data) {
      return this;
    }
  }
}
