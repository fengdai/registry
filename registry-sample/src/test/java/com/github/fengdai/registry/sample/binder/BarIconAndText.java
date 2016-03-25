package com.github.fengdai.registry.sample.binder;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.fengdai.registry.Item;
import com.github.fengdai.registry.ViewBinder;
import com.github.fengdai.registry.sample.TextList;
import com.github.fengdai.registry.sample.model.Bar;

@TextList
@Item(layout = android.R.layout.activity_list_item)
public class BarIconAndText implements ViewBinder<Bar, LinearLayout> {
  @Override public void bind(Bar item, LinearLayout view) {
    ((ImageView) view.findViewById(android.R.id.icon)).setImageResource(item.drawableId);
    ((TextView) view.findViewById(android.R.id.text1)).setText(item.text);
  }
}
