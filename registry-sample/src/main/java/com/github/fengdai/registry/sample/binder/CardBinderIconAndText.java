package com.github.fengdai.registry.sample.binder;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.fengdai.registry.Item;
import com.github.fengdai.registry.ViewBinder;
import com.github.fengdai.registry.sample.SampleList;
import com.github.fengdai.registry.sample.model.Card;

@SampleList
@Item(layout = android.R.layout.activity_list_item)
public class CardBinderIconAndText implements ViewBinder<Card, LinearLayout> {
  @Override public void bind(Card item, LinearLayout view) {
    ((ImageView) view.findViewById(android.R.id.icon)).setImageResource(item.drawableId);
    ((TextView) view.findViewById(android.R.id.text1)).setText(item.text);
  }
}
