package com.github.fengdai.registry.sample.binder;

import com.github.fengdai.registry.Binder;
import com.github.fengdai.registry.sample.lib.holder.TextViewVH;
import com.github.fengdai.registry.sample.model.Address;
import com.github.fengdai.registry.sample.model.Email;

public class AddressBinder extends Binder<Address, TextViewVH> {
  @Override public void bind(Address address, TextViewVH viewHolder) {
    if (address instanceof Email) {
      viewHolder.view.setText("Email: " + address.address);
    } else {
      viewHolder.view.setText("Address: " + address.address);
    }
  }
}
