package com.github.fengdai.registry.internal;

import androidx.annotation.NonNull;
import com.github.fengdai.registry.Binder;
import com.github.fengdai.registry.BinderViewHolder;
import java.util.List;

public final class Binders {

  public final static Binder BINDER_VIEW_HOLDER_BINDER =
      new Binder<Object, BinderViewHolder<Object>>() {
        @Override public void bind(Object o, BinderViewHolder<Object> viewHolder) {
          throw new IllegalStateException();
        }

        @Override public void bind(Object o, BinderViewHolder<Object> viewHolder,
            @NonNull List<Object> payloads) {
          viewHolder.bind(o, payloads);
        }
      };
}
