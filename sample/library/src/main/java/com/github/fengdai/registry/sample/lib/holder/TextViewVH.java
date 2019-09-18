package com.github.fengdai.registry.sample.lib.holder;

import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import com.github.fengdai.registry.BindableViewHolder;
import com.github.fengdai.registry.Register;
import com.github.fengdai.registry.Utils;
import com.github.fengdai.registry.sample.lib.R;

@Register.ViewHolder
public class TextViewVH extends BindableViewHolder<CharSequence> {
  public final TextView view;

  public TextViewVH(RecyclerView parent) {
    super(Utils.inflate(parent, R.layout.simple_list_item_1));
    view = (TextView) itemView;
  }

  @Override public void bind(CharSequence data) {
    view.setText(data);
  }
}
