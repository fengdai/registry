package com.github.fengdai.registry.sample.holder;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.fengdai.registry.BinderViewHolder;
import com.github.fengdai.registry.sample.model.Card;

public class CardVH extends BinderViewHolder<Card> {
  public final ImageView icon;
  public final TextView text;

  public CardVH(LinearLayout itemView) {
    super(itemView);
    this.icon = itemView.findViewById(android.R.id.icon);
    this.text = itemView.findViewById(android.R.id.text1);
  }

  @Override public void bind(Card item) {
    icon.setImageResource(item.drawableId);
    text.setText(item.text);
  }
}
