package com.example;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.github.fengdai.registry.BinderViewHolder;

public class FooViewHolder extends BinderViewHolder<Foo> {
  private final TextView text;

  public FooViewHolder(LayoutInflater layoutInflater, ViewGroup parent) {
    super(layoutInflater.inflate(android.R.layout.activity_list_item, parent, false));
    ImageView icon = itemView.findViewById(android.R.id.icon);
    icon.setImageResource(R.mipmap.ic_launcher);
    this.text = itemView.findViewById(android.R.id.text1);
  }

  @Override public void bind(Foo data) {
    text.setText(data.text);
  }
}
