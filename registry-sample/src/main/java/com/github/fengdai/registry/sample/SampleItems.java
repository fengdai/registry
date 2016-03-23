package com.github.fengdai.registry.sample;

import com.github.fengdai.registry.Adapter;
import com.github.fengdai.registry.Ignore;
import com.github.fengdai.registry.Item;
import com.github.fengdai.registry.ItemSet;
import com.github.fengdai.registry.Mapper;
import com.github.fengdai.registry.sample.binder.AddressBinder;
import com.github.fengdai.registry.sample.binder.CardBinder;
import com.github.fengdai.registry.sample.binder.IconCardBinder;
import com.github.fengdai.registry.sample.model.Card;
import com.github.fengdai.registry.sample.provider.TextViewProvider;

@Adapter(
    items = @Item(
        binder = AddressBinder.class,
        view = TextViewProvider.class),
    itemSets = SampleItems.CardItemSet.class)
public class SampleItems {

  @ItemSet(
      mapper = CardItemSet.CardMapper.class)
  public enum CardItemSet {

    @Ignore UNKNOWN,

    @Item(
        binder = IconCardBinder.class,
        layout = android.R.layout.activity_list_item)
    ICON_AND_TEXT,

    @Item(
        binder = CardBinder.class,
        view = TextViewProvider.class)
    TEXT_ONLY;

    public static class CardMapper implements Mapper<Card, CardItemSet> {
      public CardItemSet map(Card model) {
        if (model.drawableId == -1) {
          return CardItemSet.TEXT_ONLY;
        }
        return CardItemSet.ICON_AND_TEXT;
      }
    }
  }
}
