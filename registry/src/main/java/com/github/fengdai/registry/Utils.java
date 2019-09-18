package com.github.fengdai.registry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Utils {

  public static <T extends View> T inflate(ViewGroup parent, int layoutRes) {
    // noinspection unchecked
    return (T) LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
  }
}
