package com.github.fengdai.registry.sample.model;

public class Bar {
  public final int drawableId;
  public final String text;

  public Bar(String text) {
    this(text, -1);
  }

  public Bar(String text, int drawableId) {
    this.drawableId = drawableId;
    this.text = text;
  }
}
