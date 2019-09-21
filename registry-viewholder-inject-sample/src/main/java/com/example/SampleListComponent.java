package com.example;

import android.view.View;
import dagger.BindsInstance;
import dagger.Component;

@Component(modules = SampleList.Module.class)
public interface SampleListComponent {

  SampleList_Registry registry();

  @Component.Factory
  interface Factory {
    SampleListComponent create(@BindsInstance View.OnClickListener onClickListener);
  }
}
