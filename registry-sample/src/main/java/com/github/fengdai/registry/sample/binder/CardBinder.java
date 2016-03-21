package com.github.fengdai.registry.sample.binder;

import android.widget.TextView;
import com.github.fengdai.registry.ViewBinder;
import com.github.fengdai.registry.sample.model.Card;

public class CardBinder implements ViewBinder<Card, TextView> {
  @Override public void bind(Card item, TextView view) {
    view.setText(item.text);
  }
}
