package com.github.fengdai.registry.internal;

class Utils {
  private Utils() {
    throw new AssertionError("No instance.");
  }

  static <T> T newInstanceOf(Class<T> clazz) {
    try {
      return clazz.newInstance();
    } catch (Exception e) {
      throw new RuntimeException(String.format("Unable to create instance of %s.", clazz.getName()),
          e);
    }
  }
}
