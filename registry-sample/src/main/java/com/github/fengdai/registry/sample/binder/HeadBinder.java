package com.github.fengdai.registry.sample.binder;

import com.github.fengdai.registry.Binder;
import com.github.fengdai.registry.sample.holder.CardVH;
import com.github.fengdai.registry.sample.model.Head;

public class HeadBinder extends Binder<Head, CardVH> {
  @Override public void bind(Head head, CardVH viewHolder) {
    viewHolder.icon.setImageResource(head.drawableId);
    viewHolder.text.setText(head.text);
  }
}
