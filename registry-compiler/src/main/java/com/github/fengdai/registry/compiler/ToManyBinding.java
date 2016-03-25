package com.github.fengdai.registry.compiler;

import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.TypeElement;

public class ToManyBinding extends Binding {
  private final TypeElement mapperType;

  private final List<ItemViewClass> itemViewClassList = new LinkedList<>();

  ToManyBinding(TypeElement modelType, TypeElement mapperType) {
    super(modelType);
    this.mapperType = mapperType;
  }

  void add(ItemViewClass itemViewClass) {
    itemViewClassList.add(itemViewClass);
  }

  public List<ItemViewClass> getItemViewClasses() {
    return itemViewClassList;
  }

  TypeElement getMapperType() {
    return mapperType;
  }
}
