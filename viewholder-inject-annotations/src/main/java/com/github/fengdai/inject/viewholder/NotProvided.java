package com.github.fengdai.inject.viewholder;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.CLASS;

@Target(PARAMETER)
@Retention(CLASS)
public @interface NotProvided {
}
