package com.github.fengdai.registry.compiler;

import com.squareup.javapoet.TypeName;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.lang.model.type.TypeMirror;

public class ToManyBinding extends Binding {
  private final TypeName mapperType;

  private final Map<TypeMirror, ItemViewClass> itemViewClasses = new LinkedHashMap<>();

  ToManyBinding(TypeName modelType, TypeName mapperType) {
    super(modelType);
    this.mapperType = mapperType;
  }

  void add(TypeMirror key, ItemViewClass itemViewClass) {
    itemViewClasses.put(key, itemViewClass);
  }

  public Map<TypeMirror, ItemViewClass> getItemViewClasses() {
    return itemViewClasses;
  }

  TypeName getMapperType() {
    return mapperType;
  }
}
