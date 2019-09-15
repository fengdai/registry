package com.github.fengdai.registry.compiler;

final class DuplicateBindingException extends RuntimeException {
  DuplicateBindingException(String message) {
    super(message);
  }
}
