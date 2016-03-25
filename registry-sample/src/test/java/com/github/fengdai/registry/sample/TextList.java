package com.github.fengdai.registry.sample;

import com.github.fengdai.registry.Register;
import com.github.fengdai.registry.Mapper;
import com.github.fengdai.registry.ViewBinder;
import com.github.fengdai.registry.sample.binder.BarIconAndText;
import com.github.fengdai.registry.sample.binder.BarTextOnly;
import com.github.fengdai.registry.sample.model.Bar;

@Register(mappers = TextList.BarMapper.class)
public @interface TextList {

  class BarMapper implements Mapper<Bar, Class<? extends ViewBinder<Bar, ?>>> {
    @Override public Class<? extends ViewBinder<Bar, ?>> map(Bar model) {
      if (model.drawableId == -1) {
        return BarTextOnly.class;
      }
      return BarIconAndText.class;
    }
  }
}
