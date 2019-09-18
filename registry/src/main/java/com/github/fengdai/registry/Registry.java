package com.github.fengdai.registry;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Identifies a registry interface for a specific RecyclerView.
 * <p>
 * For type-safe reason, a registry interface must have a nested interface which extends
 * {@link RegistryItem} and annotated by {@link Item @Reigstry.Item}. The interface is used as the
 * item type for the specific RecyclerView and can avoid user from adding a RecyclerView's item to
 * another.
 * <p>
 * The registry interface must also contain abstract methods returning the defined item type.
 * They are item factories and must be annotated by {@link BindsViewHolder @BindsViewHolder},
 * {@link BindsBinder @BindsBinder} or {@link BindsLayout @BindsLayout} which reveal the
 * {@link BinderViewHolder}, {@link Binder} or layout that the created item will be bound to.
 * <p>
 * Implementation classes are generated by the processor for every {@link Registry @Registry}
 * types and named with '_Impl' suffix. Generated classes contain implementations of the defined
 * item factories and a {@link AdapterDelegate} which is used to help RecyclerView.Adapter to deal
 * with the items and ViewHolder creation.
 * <p>
 * If you use Dagger to do dependency injection with ViewHolders, you can use
 * {@link Module @Reigstry.Module} to define a nested Dagger module and the processor will generate
 * a ViewHolder module for the registry and the generated {@link AdapterDelegate} implementation
 * will also have a @Inject-annotated constructor.
 *
 * @see BindsViewHolder
 * @see BindsBinder
 * @see BindsLayout
 * @see AdapterDelegate
 */
@Target(TYPE)
@Retention(SOURCE)
public @interface Registry {

  @Target(TYPE)
  @Retention(SOURCE)
  @interface Item {
  }

  @Target(TYPE)
  @Retention(SOURCE)
  @interface Module {
  }
}
