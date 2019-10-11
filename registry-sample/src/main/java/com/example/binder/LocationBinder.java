package com.example.binder;

import com.example.holder.TextViewVH;
import com.example.model.Location;
import com.github.fengdai.registry.Binder;

public class LocationBinder extends Binder<Location, TextViewVH> {
  @Override public void bind(Location location, TextViewVH viewHolder) {
    viewHolder.view.setText("Location: " + location.location);
  }
}
