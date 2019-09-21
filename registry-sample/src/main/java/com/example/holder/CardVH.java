package com.example.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.model.Card;
import com.github.fengdai.registry.BinderViewHolder;

public class CardVH extends BinderViewHolder<Card> {
  public final ImageView icon;
  public final TextView text;

  public CardVH(View itemView) {
    super(itemView);
    this.icon = itemView.findViewById(android.R.id.icon);
    this.text = itemView.findViewById(android.R.id.text1);
  }

  @Override public void bind(Card item) {
    icon.setImageResource(item.drawableId);
    text.setText(item.text);
  }
}