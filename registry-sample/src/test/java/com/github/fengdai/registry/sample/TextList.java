package com.github.fengdai.registry.sample;

import com.github.fengdai.registry.BinderMapper;
import com.github.fengdai.registry.Register;
import com.github.fengdai.registry.ViewBinder;
import com.github.fengdai.registry.sample.binder.BarIconAndText;
import com.github.fengdai.registry.sample.binder.BarTextOnly;
import com.github.fengdai.registry.sample.model.Bar;

@Register(mappers = TextList.BarMapper.class)
public @interface TextList {

  class BarMapper implements BinderMapper<Bar> {
    @Override public Class<? extends ViewBinder<Bar, ?>> map(Bar model) {
      if (model.drawableId == -1) {
        return BarTextOnly.class;
      }
      return BarIconAndText.class;
    }
  }
}
