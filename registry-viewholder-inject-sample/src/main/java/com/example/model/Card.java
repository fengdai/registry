package com.example.model;

import android.support.annotation.DrawableRes;

public class Card {
  public final Integer drawableId;
  public final String text;

  public Card(String text) {
    this(text, null);
  }

  public Card(String text, @DrawableRes Integer drawableId) {
    this.drawableId = drawableId;
    this.text = text;
  }
}
