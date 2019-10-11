package com.example;

import com.example.binder.CardBinder_TextOnly;
import com.example.binder.HeadBinder;
import com.example.binder.LocationBinder;
import com.example.holder.CardVH;
import com.example.holder.TextViewVH;
import com.example.model.Card;
import com.github.fengdai.registry.Identifier;
import com.github.fengdai.registry.Register;
import com.github.fengdai.registry.StaticContentLayoutData;

@Register(
    binders = {
        HeadBinder.class,
        CardBinder_TextOnly.class,
        LocationBinder.class,
    },
    binderViewHolders = {
        CardVH.class
    }
) class SampleList {

  static StaticContentLayoutData GAP = new StaticContentLayoutData(R.layout.gap);

  @Identifier(TextViewVH.class)
  static boolean cardTextOnly(Card card) {
    return card.drawableId == null;
  }
}
