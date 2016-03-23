package com.github.fengdai.registry.sample;

import com.github.fengdai.registry.Adapter;
import com.github.fengdai.registry.Ignore;
import com.github.fengdai.registry.Item;
import com.github.fengdai.registry.ItemSet;
import com.github.fengdai.registry.Mapper;
import com.github.fengdai.registry.sample.binder.AddressBinder;
import com.github.fengdai.registry.sample.binder.CardBinder;
import com.github.fengdai.registry.sample.binder.IconCardBinder;
import com.github.fengdai.registry.sample.model.Address;
import com.github.fengdai.registry.sample.model.Card;
import com.github.fengdai.registry.sample.provider.TextViewProvider;

@Adapter(
    items = @Item(
        model = Address.class,
        view = TextViewProvider.class,
        binder = AddressBinder.class),
    itemSets = SampleItems.CardItemSet.class)
public class SampleItems {

  @ItemSet(
      model = Card.class,
      mapper = CardItemSet.CardMapper.class)
  public enum CardItemSet {

    @Ignore UNKNOWN,

    @Item(
        model = Card.class,
        layout = android.R.layout.activity_list_item,
        binder = IconCardBinder.class)
    ICON_AND_TEXT,

    @Item(
        model = Card.class,
        view = TextViewProvider.class,
        binder = CardBinder.class)
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
