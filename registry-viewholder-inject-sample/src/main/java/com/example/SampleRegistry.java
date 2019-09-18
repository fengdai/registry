package com.example;

import com.github.fengdai.registry.BindsLayout;
import com.github.fengdai.registry.BindsViewHolder;
import com.github.fengdai.registry.Registry;
import com.github.fengdai.registry.RegistryItem;

@Registry
interface SampleRegistry {

  @Registry.Item
  interface Item extends RegistryItem {}

  // Binds Foo to FooViewHolder
  @BindsViewHolder(FooViewHolder.class)
  Item fooItem(Foo foo);

  // Binds Bar to BarViewHolder
  @BindsViewHolder(BarViewHolder.class)
  Item barItem(Bar bar);

  // Binds a layout 'footer' which has a TextView showing "end"
  @BindsLayout(R.layout.footer)
  Item footerItem();

  @Registry.Module
  @dagger.Module(includes = SampleRegistry_RegistryModule.class)
  abstract class Module {
  }
}
