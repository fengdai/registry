package com.github.fengdai.registry.compiler;

import com.squareup.javapoet.ClassName;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;
import javax.lang.model.element.TypeElement;

final class MappingInfo {
  private final ClassName dataClassName;
  private final List<Mapping> mappings = new LinkedList<>();

  MappingInfo(TypeElement dataElement) {
    this.dataClassName = ClassName.get(dataElement);
  }

  void addBindableViewHolderInfo(IndexedViewHolderInfo viewHolderInfo) {
    mappings.add(new Mapping(viewHolderInfo.index, viewHolderInfo.info, null));
  }

  void addBinderInfo(BinderInfo binderInfo) {
    mappings.add(
        new Mapping(binderInfo.viewHolderInfo.index, binderInfo.viewHolderInfo.info, binderInfo));
  }

  ClassName getDataClassName() {
    return dataClassName;
  }

  List<Mapping> getMappings() {
    return mappings;
  }

  boolean isOneToMany() {
    return mappings.size() > 1;
  }

  static final class Mapping {
    final int viewType;
    final ViewHolderInfo viewHolderInfo;
    @Nullable final BinderInfo binderInfo;

    Mapping(int viewType, ViewHolderInfo viewHolderInfo,
        @Nullable BinderInfo binderInfo) {
      this.viewType = viewType;
      this.viewHolderInfo = viewHolderInfo;

      this.binderInfo = binderInfo;
    }
  }
}
