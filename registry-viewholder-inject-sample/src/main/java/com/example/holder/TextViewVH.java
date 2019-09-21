package com.example.holder;

import android.view.View;
import android.widget.TextView;
import com.github.fengdai.inject.viewholder.Inflate;
import com.github.fengdai.inject.viewholder.ViewHolderInject;
import com.github.fengdai.registry.BinderViewHolder;

public class TextViewVH extends BinderViewHolder<CharSequence> {
  public final TextView view;

  @ViewHolderInject
  public TextViewVH(@Inflate(android.R.layout.simple_list_item_1) TextView itemView,
      View.OnClickListener onClickListener) {
    super(itemView);
    view = itemView;
    view.setOnClickListener(onClickListener);
  }

  @Override public void bind(CharSequence data) {
    view.setText(data);
  }
}
