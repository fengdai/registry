package com.github.fengdai.registry.sample.holder;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.github.fengdai.registry.BindableViewHolder;
import com.github.fengdai.registry.Inflate;
import com.github.fengdai.registry.Register;
import com.github.fengdai.registry.sample.model.Card;

@Register.ViewHolder
public class CardVH extends BindableViewHolder<Card> {
  public final ImageView icon;
  public final TextView text;

  public CardVH(@Inflate(android.R.layout.activity_list_item) LinearLayout itemView) {
    super(itemView);
    this.icon = itemView.findViewById(android.R.id.icon);
    this.text = itemView.findViewById(android.R.id.text1);
  }

  @Override public void bind(Card item) {
    icon.setImageResource(item.drawableId);
    text.setText(item.text);
  }
}
