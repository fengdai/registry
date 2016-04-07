package com.github.fengdai.registry.sample;

import com.github.fengdai.registry.internal.Layouts;
import com.github.fengdai.registry.internal.Model;
import com.github.fengdai.registry.internal.RegistryImpl;
import com.github.fengdai.registry.sample.binder.BarIconAndText;
import com.github.fengdai.registry.sample.binder.BarTextOnly;
import com.github.fengdai.registry.sample.binder.FooBinder;
import com.github.fengdai.registry.sample.model.Bar;
import com.github.fengdai.registry.sample.model.Foo;
import java.util.LinkedHashMap;
import java.util.Map;

public class TextList$$Registry extends RegistryImpl {
  public TextList$$Registry() {
    super(createModels(), 3);
  }

  private static Map<Class<?>, Model<?>> createModels() {
    Map<Class<?>, Model<?>> map = new LinkedHashMap<>();
    map.put(Foo.class, com_github_fengdai_registry_internal_model_Foo());
    map.put(Bar.class, com_github_fengdai_registry_internal_model_Bar());
    return map;
  }

  private static Model<Foo> com_github_fengdai_registry_internal_model_Foo() {
    Model.Builder<Foo> builder = Model.oneToOne(Foo.class);
    builder.add(0, new FooBinder(), Layouts.factoryOf(android.R.layout.simple_list_item_1));
    return builder.build();
  }

  private static Model<Bar> com_github_fengdai_registry_internal_model_Bar() {
    Model.Builder<Bar> builder = Model.oneToMany(Bar.class, new TextList.BarMapper());
    builder.add(1, new BarIconAndText(), Layouts.factoryOf(android.R.layout.activity_list_item));
    builder.add(2, new BarTextOnly(), Layouts.factoryOf(android.R.layout.simple_list_item_1));
    return builder.build();
  }
}
