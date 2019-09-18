package com.example;

import android.view.View;
import dagger.BindsInstance;
import dagger.Component;

@Component(modules = SampleRegistry.Module.class)
interface SampleRegistryComponent {

  SampleRegistry_Impl.AdapterDelegate adapterDelegate();

  @Component.Factory
  interface Factory {
    SampleRegistryComponent create(@BindsInstance View.OnClickListener onClickListener);
  }
}
