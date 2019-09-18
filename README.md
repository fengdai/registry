# Registry

Registry helps you use ViewHolders to modularize RecyclerView and compose them easily.

# Example

Say, we have a RecyclerView which displays two kinds of item: Foo and Bar. It also has a footer which shows static content like "end".

1. Extend `BinderViewHolder` to define your ViewHolders.
```java
public class FooViewHolder extends BinderViewHolder<Foo> {
  public FooViewHolder(View itemView) {
    super(itemView)
  }

  @Override public void bind(Foo data) {
    // Do binding.
  }
}
```
```java
public class BarViewHolder extends BinderViewHolder<Bar> {
  public BarViewHolder(View itemView) {
    super(itemView)
  }

  @Override public void bind(Bar data) {
    // Do binding.
  }
}
```

2. Define your Registry interface:
```java
@Registry
public interface SampleRegistry {
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
}
```

3. Create RecyclerView.Adapter:
```java
AdapterDelegate<SampleRegistry.Item> adapterDelegate = new SampleRegistry_Impl.AdapterDelegate(
    parent -> new FooViewHolder(layoutInflater.inflate(R.layout.foo, parent, false)),
    parent -> new BarViewHolder((TextView) layoutInflater.inflate(R.layout.bar, parent, false)));
RegistryListAdapter<SampleRegistry.Item> adapter = new RegistryListAdapter<>(adapterDelegate, new DiffCallback());
```

4. Render RecyclerView:
```java
SampleRegistry sampleRegistry = new SampleRegistry_Impl();
adapter.submitList(Arrays.asList(
  sampleRegistry.fooItem(new Foo()), // create the Foo item
  sampleRegistry.barItem(new Bar()), // create the Bar item
  sampleRegistry.footerItem())); // create the footer item
```

Done.:tada:

# More details

* [Simple example](https://github.com/fengdai/registry/tree/master/registry-sample)
* [Simple example with ViewHolder injection](https://github.com/fengdai/registry/tree/master/registry-viewholder-inject-sample)

# Download

Gradle:
```groovy
dependencies {
  implementation 'com.github.fengdai:registry:0.2.0'
  annotationProcessor 'com.github.fengdai:registry-compiler:0.2.0'
}
```

# License

    Copyright (C) 2016 Feng Dai

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
