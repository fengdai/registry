package com.example;

import android.view.View;
import com.github.fengdai.registry.AdapterDelegate;
import dagger.BindsInstance;
import dagger.Component;

@Component(modules = SampleRegistry.Module.class)
interface SampleRegistryComponent {

  AdapterDelegate<SampleRegistry.Item> adapterDelegate();

  @Component.Factory
  interface Factory {
    SampleRegistryComponent create(@BindsInstance View.OnClickListener onClickListener);
  }
}
