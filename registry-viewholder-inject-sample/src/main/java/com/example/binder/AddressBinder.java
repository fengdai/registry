package com.example.binder;

import com.example.holder.TextViewVH;
import com.example.model.Address;
import com.example.model.Email;
import com.github.fengdai.registry.Binder;

public class AddressBinder extends Binder<Address, TextViewVH> {
  @Override public void bind(Address address, TextViewVH viewHolder) {
    if (address instanceof Email) {
      viewHolder.view.setText("Email: " + address.address);
    } else {
      viewHolder.view.setText("Address: " + address.address);
    }
  }
}
