package com.github.fengdai.registry.sample;

import android.support.v7.widget.RecyclerView;
import com.github.fengdai.registry.Binder;
import com.github.fengdai.registry.Registry;
import com.github.fengdai.registry.sample.binder.AddressBinder;
import com.github.fengdai.registry.sample.binder.CardBinder_TextOnly;
import com.github.fengdai.registry.sample.binder.HeadBinder;
import com.github.fengdai.registry.sample.holder.CardVH;
import com.github.fengdai.registry.sample.holder.CardVH_Factory;
import com.github.fengdai.registry.sample.lib.holder.TextViewVH;
import com.github.fengdai.registry.sample.lib.holder.TextViewVH_Factory;
import com.github.fengdai.registry.sample.model.Address;
import com.github.fengdai.registry.sample.model.Card;
import com.github.fengdai.registry.sample.model.Head;

public class TestListRegistry extends Registry<TestListRegistry.Item> {
  private static final Item ITEM_gap = new Item(staticContentLayoutData(R.layout.gap), 2, null);

  public TestListRegistry() {
    super();
    registerViewHolderFactory(0, new TextViewVH_Factory());
    registerViewHolderFactory(1, new CardVH_Factory());
    registerStaticContentLayout(2, R.layout.gap);
  }

  public static Item itemOf(Address data) {
    return new Item(data, 0, new AddressBinder());
  }

  public static Item itemOf(Card data, Class<? extends RecyclerView.ViewHolder> viewHolderClass) {
    if (viewHolderClass == TextViewVH.class) {
      return new Item(data, 0, new CardBinder_TextOnly());
    } else if (viewHolderClass == CardVH.class) {
      return new Item(data, 1, BINDABLE_VIEW_HOLDER_BINDER);
    } else {
      throw new IllegalArgumentException("viewHolderClass");
    }
  }

  public static Item itemOf(Head data) {
    return new Item(data, 1, new HeadBinder());
  }

  public static Item itemOf_gap() {
    return ITEM_gap;
  }

  public static final class Item extends Registry.Item {
    private Item(Object data, int viewType, Binder binder) {
      super(data, viewType, binder);
    }
  }
}
