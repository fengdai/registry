package com.github.fengdai.registry;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Identifies an item factory method. Binds the returned item instances to the specific {@link
 * Binder}.
 * <p>
 * Annotated factory methods must have a parameter of a data type which the returned item is
 * corresponding to.
 *
 * @see Registry
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface BindsBinder {
  Class<? extends Binder<?, ?>> value();
}
