package com.github.fengdai.registry.sample.model;

import android.support.annotation.DrawableRes;

public class Head {
  public final int drawableId;
  public final String text;

  public Head(String text, @DrawableRes int drawableId) {
    this.drawableId = drawableId;
    this.text = text;
  }
}
