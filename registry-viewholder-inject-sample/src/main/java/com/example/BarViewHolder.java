package com.example;

import android.view.View;
import android.widget.TextView;
import com.github.fengdai.inject.viewholder.Inflate;
import com.github.fengdai.inject.viewholder.ViewHolderInject;
import com.github.fengdai.registry.BinderViewHolder;

public class BarViewHolder extends BinderViewHolder<Bar> {
  private final TextView text;

  @ViewHolderInject
  public BarViewHolder(@Inflate(android.R.layout.simple_list_item_1) TextView itemView, View.OnClickListener onClickListener) {
    super(itemView);
    this.text = itemView;
    itemView.setOnClickListener(onClickListener);
  }

  @Override public void bind(Bar data) {
    text.setText(data.text);
  }
}
