package com.github.fengdai.registry;

import android.support.v7.widget.RecyclerView;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

@Target(METHOD)
@Retention(SOURCE)
public @interface Identifier {
  Class<? extends RecyclerView.ViewHolder> value();
}
