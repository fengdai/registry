package com.github.fengdai.inject.viewholder;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.RetentionPolicy.CLASS;

@Target(CONSTRUCTOR)
@Retention(CLASS)
public @interface ViewHolderInject {
}
