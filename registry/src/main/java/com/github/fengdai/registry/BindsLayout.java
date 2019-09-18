package com.github.fengdai.registry;

import android.support.annotation.LayoutRes;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Identifies item factory methods. Binds the returned item instances to the specific layout.
 * <p>
 * Annotated factory methods can't have parameter.
 *
 * @see Registry
 */
@Target(METHOD)
@Retention(SOURCE)
public @interface BindsLayout {
  @LayoutRes int value();
}
