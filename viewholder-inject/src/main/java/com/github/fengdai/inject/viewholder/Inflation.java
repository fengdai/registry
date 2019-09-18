package com.github.fengdai.inject.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Inflation {

  @SuppressWarnings("unchecked")
  public static <T extends View> T inflate(ViewGroup parent, int layoutRes) {
    return (T) LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
  }
}
