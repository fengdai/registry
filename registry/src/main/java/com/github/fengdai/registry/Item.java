package com.github.fengdai.registry;

import android.support.annotation.LayoutRes;
import android.view.View;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface Item {

  Class<? extends ViewBinder<?, ?>> binder();

  @LayoutRes int layout() default -1;

  Class<? extends ViewProvider<?>> view() default NONE.class;

  interface NONE extends ViewProvider<View> {
  }
}
