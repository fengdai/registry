package com.github.fengdai.registry.sample.binder;

import com.github.fengdai.registry.Binder;
import com.github.fengdai.registry.sample.holder.TextViewVH;
import com.github.fengdai.registry.sample.model.Card;

public class CardBinder_TextOnly extends Binder<Card, TextViewVH> {
  @Override public void bind(Card card, TextViewVH viewHolder) {
    viewHolder.view.setText(card.text);
  }
}
