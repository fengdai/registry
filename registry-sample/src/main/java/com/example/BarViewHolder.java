package com.example;

import android.widget.TextView;
import com.github.fengdai.registry.BinderViewHolder;

public class BarViewHolder extends BinderViewHolder<Bar> {
  private final TextView text;

  public BarViewHolder(TextView itemView) {
    super(itemView);
    this.text = itemView;
  }

  @Override public void bind(Bar data) {
    text.setText(data.text);
  }
}
