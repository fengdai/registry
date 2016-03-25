package com.github.fengdai.registry.sample.binder;

import android.widget.TextView;
import com.github.fengdai.registry.Item;
import com.github.fengdai.registry.ViewBinder;
import com.github.fengdai.registry.sample.TextList;
import com.github.fengdai.registry.sample.model.Foo;

@TextList
@Item(layout = android.R.layout.simple_list_item_1)
public class FooBinder implements ViewBinder<Foo, TextView> {
  @Override public void bind(Foo item, TextView view) {
    view.setText(item.text);
  }
}
