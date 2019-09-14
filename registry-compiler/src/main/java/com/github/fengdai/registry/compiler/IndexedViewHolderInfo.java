package com.github.fengdai.registry.compiler;

final class IndexedViewHolderInfo {
  final int index;
  final ViewHolderInfo info;

  IndexedViewHolderInfo(int index, ViewHolderInfo info) {
    this.index = index;
    this.info = info;
  }
}
