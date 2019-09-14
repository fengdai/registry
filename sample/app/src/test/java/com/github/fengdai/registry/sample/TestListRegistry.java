package com.github.fengdai.registry.sample;

import android.support.v7.widget.RecyclerView;
import com.github.fengdai.registry.Binder;
import com.github.fengdai.registry.Registry;
import com.github.fengdai.registry.sample.binder.CardBinder_TextOnly;
import com.github.fengdai.registry.sample.binder.HeadBinder;
import com.github.fengdai.registry.sample.holder.CardVH_Factory;
import com.github.fengdai.registry.sample.lib.holder.TextViewVH_Factory;
import com.github.fengdai.registry.sample.model.Address;
import com.github.fengdai.registry.sample.model.Card;
import com.github.fengdai.registry.sample.model.Head;

public class TestListRegistry extends Registry<TestListRegistry.Item> {
  private static final HeadBinder headBinder = new HeadBinder();
  private static final CardBinder_TextOnly cardBinder_TextOnly = new CardBinder_TextOnly();

  private static final Item ITEM_gap =
      new Item(staticContentLayoutData(R.layout.gap), 2, null);

  public TestListRegistry() {
    super();
    registerViewHolderFactory(0, new TextViewVH_Factory());
    registerViewHolderFactory(1, new CardVH_Factory());

    registerStaticContentLayout(2, R.layout.gap);
  }

  public static Item itemOf(Address data) {
    return new Item(data, 0, BINDABLE_VIEW_HOLDER_BINDER);
  }

  public static Item itemOf_TextViewVH(Card data) {
    return new Item(data, 0, cardBinder_TextOnly);
  }

  public static Item itemOf_CardVH(Card data) {
    return new Item(data, 1, BINDABLE_VIEW_HOLDER_BINDER);
  }

  public static Item itemOf_(Head data) {
    return new Item(data, 1, headBinder);
  }

  public static Item itemOf_gap() {
    return ITEM_gap;
  }

  public static class Item extends Registry.Item {
    private Item(Object data, int viewType, Binder<?, ? extends RecyclerView.ViewHolder> binder) {
      super(data, viewType, binder);
    }
  }
}
