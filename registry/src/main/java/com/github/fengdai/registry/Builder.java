package com.github.fengdai.registry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.LinkedHashMap;
import java.util.Map;

public class Builder<T> {
  private final Class<T> modelClass;
  private final Mapper<T, Class<? extends ViewBinder<T, ?>>> mapper;
  private Map<Class<? extends ViewBinder<T, ?>>, Registry.ItemView<T, ?>> itemViewMap =
      new LinkedHashMap<>();
  private Registry.ItemView<T, ?> itemView;

  public static <T> Builder<T> oneToOne(Class<T> modelClass) {
    return new Builder<>(modelClass);
  }

  public static <T> Builder<T> oneToMany(Class<T> modelClass,
      Mapper<T, Class<? extends ViewBinder<T, ?>>> mapper) {
    return new Builder<>(modelClass, mapper);
  }

  private Builder(Class<T> modelClass) {
    this.modelClass = modelClass;
    this.mapper = null;
  }

  private Builder(Class<T> modelClass, Mapper<T, Class<? extends ViewBinder<T, ?>>> mapper) {
    this.modelClass = modelClass;
    this.mapper = mapper;
  }

  public <V extends View> Builder<T> add(int itemViewType, ViewBinder<T, V> viewBinder,
      int layoutRes) {
    ViewFactory<V> viewFactory = factory(layoutRes);
    return add(itemViewType, viewBinder, viewFactory);
  }

  public <BV extends View, FV extends BV> Builder<T> add(int itemViewType,
      ViewBinder<T, BV> viewBinder, ViewFactory<FV> viewFactory) {
    Registry.ItemView<T, BV> itemView = new Iv<>(modelClass, itemViewType, viewBinder, viewFactory);
    if (mapper == null) {
      this.itemView = itemView;
    } else {
      // noinspection unchecked
      itemViewMap.put((Class<? extends ViewBinder<T, ?>>) viewBinder.getClass(), itemView);
    }
    return this;
  }

  public Model<T> build() {
    if (mapper == null) {
      return new Model.ModelToOne<>(modelClass, itemView);
    } else {
      return new Model.ModelToMany<>(modelClass, mapper, itemViewMap);
    }
  }

  private static <V extends ViewFactory> V factory(int layoutRes) {
    // noinspection unchecked
    return (V) new LayoutViewFactory<>(layoutRes);
  }

  static class Iv<T, BV extends View, FV extends BV> implements Registry.ItemView<T, BV> {
    private final Class<T> modelClass;
    private final int type;
    private final ViewBinder<T, BV> viewBinder;
    private final ViewFactory<FV> viewFactory;

    Iv(Class<T> modelClass, int type, ViewBinder<T, BV> viewBinder, ViewFactory<FV> viewFactory) {
      this.modelClass = modelClass;
      this.type = type;
      this.viewBinder = viewBinder;
      this.viewFactory = viewFactory;
    }

    public Class<?> getModelClass() {
      return modelClass;
    }

    @Override public int getType() {
      return type;
    }

    @Override public BV providerView(ViewGroup parent) {
      return viewFactory.createView(parent);
    }

    @Override public void bindView(T item, BV convertView) {
      viewBinder.bind(item, convertView);
    }
  }

  static class LayoutViewFactory<V extends View> implements ViewFactory<V> {
    private final int layoutRes;

    LayoutViewFactory(int layoutRes) {
      this.layoutRes = layoutRes;
    }

    @Override public V createView(ViewGroup parent) {
      // noinspection unchecked
      return (V) LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
    }
  }
}
