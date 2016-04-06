package com.github.fengdai.registry.sample.binder;

import android.widget.TextView;
import com.github.fengdai.registry.Layout;
import com.github.fengdai.registry.ViewBinder;
import com.github.fengdai.registry.sample.SampleList;
import com.github.fengdai.registry.sample.model.Address;

@SampleList
@Layout(android.R.layout.simple_list_item_1)
public class AddressBinder implements ViewBinder<Address, TextView> {
  @Override public void bind(Address item, TextView view) {
    view.setText(item.address);
  }
}
