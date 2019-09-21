package com.github.fengdai.registry;

import android.support.annotation.LayoutRes;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

@Target(ANNOTATION_TYPE)
@Retention(CLASS)
public @interface Register {

  Class<? extends Binder<?, ?>>[] binders() default {};

  Class<? extends BinderViewHolder<?>>[] binderViewHolders() default {};

  @LayoutRes int[] staticContentLayouts() default {};
}
