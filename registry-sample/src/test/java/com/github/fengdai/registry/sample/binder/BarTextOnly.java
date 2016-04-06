package com.github.fengdai.registry.sample.binder;

import android.widget.TextView;
import com.github.fengdai.registry.Layout;
import com.github.fengdai.registry.ViewBinder;
import com.github.fengdai.registry.sample.TextList;
import com.github.fengdai.registry.sample.model.Bar;

@TextList
@Layout(layout = android.R.layout.simple_list_item_1)
public class BarTextOnly implements ViewBinder<Bar, TextView> {
  @Override public void bind(Bar item, TextView view) {
    view.setText(item.text);
  }
}
