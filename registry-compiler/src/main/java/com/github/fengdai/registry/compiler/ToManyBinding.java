package com.github.fengdai.registry.compiler;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.lang.model.element.TypeElement;

public class ToManyBinding extends Binding {
  private final TypeElement mapperType;

  private final Map<TypeElement, ItemViewClass> itemViewClasses = new LinkedHashMap<>();

  ToManyBinding(TypeElement modelType, TypeElement mapperType) {
    super(modelType);
    this.mapperType = mapperType;
  }

  void add(TypeElement keyTypeElement, ItemViewClass itemViewClass) {
    itemViewClasses.put(keyTypeElement, itemViewClass);
  }

  public Map<TypeElement, ItemViewClass> getItemViewClasses() {
    return itemViewClasses;
  }

  TypeElement getMapperType() {
    return mapperType;
  }
}
