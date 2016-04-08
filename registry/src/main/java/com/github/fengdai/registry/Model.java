package com.github.fengdai.registry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Model<T> {

  public static <T, V extends View> Model<T> oneToOne(int itemViewType, ViewBinder<T, V> viewBinder,
      int layoutRes) {
    return new ModelToOne<>(new Iv<>(itemViewType, viewBinder, layoutRes));
  }

  public static <T, K extends Class<? extends ViewBinder<T, ?>>> Builder<T, K> oneToMany(
      Mapper<T, K> mapper) {
    return new Builder<>(mapper);
  }

  abstract Registry.ItemView getItemView(T item);

  static class ModelToMany<T, K> extends Model<T> {
    private Mapper<T, K> mapper = null;
    private final Map<K, Registry.ItemView> itemMap;

    ModelToMany(Mapper<T, K> mapper, Map<K, Registry.ItemView> itemMap) {
      this.mapper = mapper;
      this.itemMap = itemMap;
    }

    @Override Registry.ItemView getItemView(T item) {
      return itemMap.get(mapper.map(item));
    }
  }

  static class ModelToOne<T> extends Model<T> {
    private final Registry.ItemView itemView;

    ModelToOne(Registry.ItemView itemView) {
      this.itemView = itemView;
    }

    @Override Registry.ItemView getItemView(T item) {
      return this.itemView;
    }
  }

  static class Iv<T, V extends View> implements Registry.ItemView {
    private final int type;
    private final ViewBinder<T, V> viewBinder;
    private final int layoutRes;

    Iv(int type, ViewBinder<T, V> viewBinder, int layoutRes) {
      this.type = type;
      this.viewBinder = viewBinder;
      this.layoutRes = layoutRes;
    }

    @Override public int getType() {
      return type;
    }

    @Override public View newView(ViewGroup parent) {
      return LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
    }

    @Override public void bindView(Object item, View convertView) {
      // noinspection unchecked
      viewBinder.bind((T) item, (V) convertView);
    }
  }

  public static class Builder<T, K> {
    private final Mapper<T, K> mapper;
    private Map<K, Registry.ItemView> itemViewMap = new LinkedHashMap<>();

    Builder(Mapper<T, K> mapper) {
      this.mapper = mapper;
    }

    public <V extends View> Builder<T, K> add(K key, int itemViewType, ViewBinder<T, V> viewBinder,
        int layoutRes) {
      Registry.ItemView itemView = new Iv<>(itemViewType, viewBinder, layoutRes);
      itemViewMap.put(key, itemView);
      return this;
    }

    public Model<T> build() {
      return new ModelToMany<>(mapper, itemViewMap);
    }
  }
}
