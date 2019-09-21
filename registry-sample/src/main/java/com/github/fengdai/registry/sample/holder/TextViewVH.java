package com.github.fengdai.registry.sample.holder;

import android.widget.TextView;
import com.github.fengdai.registry.BinderViewHolder;

public class TextViewVH extends BinderViewHolder<CharSequence> {
  public final TextView view;

  public TextViewVH(TextView itemView) {
    super(itemView);
    view = itemView;
  }

  @Override public void bind(CharSequence data) {
    view.setText(data);
  }
}
