package com.github.fengdai.registry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Adapter {

  Item[] items() default {};

  Class<? extends Enum<?>>[] itemSets() default {};
}
