package com.github.fengdai.registry.sample.binder;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.fengdai.registry.ViewBinder;
import com.github.fengdai.registry.sample.model.Card;

public class IconCardBinder implements ViewBinder<Card, LinearLayout> {
  @Override public void bind(Card item, LinearLayout view) {
    ((ImageView) view.findViewById(android.R.id.icon)).setImageResource(item.drawableId);
    ((TextView) view.findViewById(android.R.id.text1)).setText(item.text);
  }
}
