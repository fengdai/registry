package com.example.binder;

import com.example.holder.TextViewVH;
import com.example.model.Card;
import com.github.fengdai.registry.Binder;

public class CardBinder_TextOnly extends Binder<Card, TextViewVH> {
  @Override public void bind(Card card, TextViewVH viewHolder) {
    viewHolder.view.setText(card.text);
  }
}
