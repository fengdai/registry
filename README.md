# Registry

Registry helps you use ViewHolders to modularize RecyclerView and compose them easily. It also supports Dagger2 based ViewHolder injection.

# Example with assisted ViewHolder injection:

Say, we have a RecyclerView which displays two kinds of item: Foo and Bar. It also has a footer which shows static content like "end".

1. Extend `BinderViewHolder` to define your ViewHolders and use `@ViewHolderInject` for ViewHolder injection.
```java
public class FooViewHolder extends BinderViewHolder<Foo> {
  private final TextView text;

  @ViewHolderInject
  public FooViewHolder(@Inflate(android.R.layout.activity_list_item) View itemView) {
    super(itemView);
    ImageView icon = itemView.findViewById(android.R.id.icon);
    icon.setImageResource(R.mipmap.ic_launcher);
    this.text = itemView.findViewById(android.R.id.text1);
  }

  @Override public void bind(Foo data) {
    text.setText(data.text);
  }
}
```
```java
public class BarViewHolder extends BinderViewHolder<Bar> {
  private final TextView text;

  @ViewHolderInject
  public BarViewHolder(@Inflate(android.R.layout.simple_list_item_1) TextView itemView, View.OnClickListener onClickListener) {
    super(itemView);
    this.text = itemView;
    itemView.setOnClickListener(onClickListener);
  }

  @Override public void bind(Bar data) {
    text.setText(data.text);
  }
}
```

2. Define your Registry interface and Dagger module:
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

  @Registry.Module
  @dagger.Module(includes = SampleRegistry_RegistryModule.class)
  abstract class Module {
  }
}
```

3. Add the Dagger module to your Dagger component:
```java
@Component(modules = SampleRegistry.Module.class)
interface SampleRegistryComponent {

  SampleRegistry_Impl.AdapterDelegate adapterDelegate();

  @Component.Factory
  interface Factory {
    SampleRegistryComponent create(@BindsInstance View.OnClickListener onClickListener);
  }
}
```

4. Create RecyclerView.Adapter:
```java
AdapterDelegate<SampleRegistry.Item> adapterDelegate = DaggerSampleRegistryComponent.factory()
        .create(v -> { ... })
        .adapterDelegate();
RegistryListAdapter<SampleRegistry.Item> adapter = new RegistryListAdapter<>(adapterDelegate, new DiffCallback());
```

5. Render RecyclerView:
```java
SampleRegistry sampleRegistry = new SampleRegistry_Impl();
adapter.submitList(Arrays.asList(
  sampleRegistry.fooItem(new Foo()), // create the Foo item
  sampleRegistry.barItem(new Bar()), // create the Bar item
  sampleRegistry.footerItem())); // create the footer item
```

# More details

* [Simple example](https://github.com/fengdai/registry/tree/master/registry-sample)
* [Simple example with ViewHolder injection](https://github.com/fengdai/registry/tree/master/registry-viewholder-inject-sample)

# Download

```groovy
implementation 'com.github.fengdai:registry:0.3.0'
annotationProcessor 'com.github.fengdai:registry-processor:0.3.0'
```

Assisted ViewHolder injection:
```groovy
implementation 'com.github.fengdai.inject:viewholder-inject:0.3.0'
annotationProcessor 'com.github.fengdai.inject:viewholder-inject-processor:0.3.0'
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
