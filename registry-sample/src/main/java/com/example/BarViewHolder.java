package com.example;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.github.fengdai.registry.BinderViewHolder;

public class BarViewHolder extends BinderViewHolder<Bar> {
  private final TextView text;

  public BarViewHolder(LayoutInflater layoutInflater, ViewGroup parent, View.OnClickListener onClickListener) {
    super(layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false));
    this.text = (TextView) itemView;
    itemView.setOnClickListener(onClickListener);
  }

  @Override public void bind(Bar data) {
    text.setText(data.text);
  }
}
