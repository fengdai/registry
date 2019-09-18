package com.example;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.github.fengdai.inject.viewholder.Inflate;
import com.github.fengdai.inject.viewholder.ViewHolderInject;
import com.github.fengdai.registry.BinderViewHolder;

public class FooViewHolder extends BinderViewHolder<Foo> {
  private final TextView text;

  @ViewHolderInject
  public FooViewHolder(@Inflate(android.R.layout.activity_list_item) View itemView) {
    super(itemView);
    ImageView icon = itemView.findViewById(android.R.id.icon);
    icon.setImageResource(R.mipmap.ic_launcher);
    this.text = itemView.findViewById(android.R.id.text1);
  }

  @Override public void bind(Foo data) {
    text.setText(data.text);
  }
}
