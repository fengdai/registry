package com.example.binder;

import com.example.holder.CardVH;
import com.example.model.Head;
import com.github.fengdai.registry.Binder;

public class HeadBinder extends Binder<Head, CardVH> {
  @Override public void bind(Head head, CardVH viewHolder) {
    viewHolder.icon.setImageResource(head.drawableId);
    viewHolder.text.setText(head.text);
  }
}
