package com.github.fengdai.registry.sample;

import com.github.fengdai.registry.BinderMapper;
import com.github.fengdai.registry.Register;
import com.github.fengdai.registry.ViewBinder;
import com.github.fengdai.registry.sample.binder.CardBinderIconAndText;
import com.github.fengdai.registry.sample.binder.CardBinderTextOnly;
import com.github.fengdai.registry.sample.model.Card;

@Register(mappers = SampleList.CardMapper.class)
public @interface SampleList {

  class CardMapper implements BinderMapper<Card> {
    @Override public Class<? extends ViewBinder<Card, ?>> map(Card model) {
      if (model.drawableId == -1) {
        return CardBinderTextOnly.class;
      }
      return CardBinderIconAndText.class;
    }
  }
}
