package com.github.fengdai.registry;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

@Target(TYPE)
@Retention(SOURCE)
public @interface Register {

  Class<? extends Binder<?, ?>>[] binders() default {};

  Class<? extends BinderViewHolder<?>>[] binderViewHolders() default {};
}
