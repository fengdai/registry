package com.example.model;

import android.support.annotation.DrawableRes;

public class Card {
  public final int drawableId;
  public final String text;

  public Card(String text) {
    this(text, -1);
  }

  public Card(String text, @DrawableRes int drawableId) {
    this.drawableId = drawableId;
    this.text = text;
  }
}
