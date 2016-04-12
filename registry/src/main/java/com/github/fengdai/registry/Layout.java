package com.github.fengdai.registry;

import android.support.annotation.LayoutRes;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ ElementType.TYPE })
public @interface Layout {

  @LayoutRes int value();
}
