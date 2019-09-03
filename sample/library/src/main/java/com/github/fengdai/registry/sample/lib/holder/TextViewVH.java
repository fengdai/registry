package com.github.fengdai.registry.sample.lib.holder;

import android.widget.TextView;
import com.github.fengdai.registry.BindableViewHolder;
import com.github.fengdai.registry.Inflate;
import com.github.fengdai.registry.Register;
import com.github.fengdai.registry.sample.lib.R2;

@Register.ViewHolder
public class TextViewVH extends BindableViewHolder<CharSequence> {
  public final TextView view;

  public TextViewVH(@Inflate(R2.layout.simple_list_item_1) TextView itemView) {
    super(itemView);
    view = itemView;
  }

  @Override public void bind(CharSequence data) {
    view.setText(data);
  }
}
