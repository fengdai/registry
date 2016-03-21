package com.github.fengdai.registry.sample;

import com.github.fengdai.registry.Registry;
import org.junit.Assert;
import org.junit.Test;

public class RegistryTest {
  @Test public void testCreate() throws Exception {
    Registry registry = Registry.create(SampleItems.class);
    Assert.assertEquals(2, registry.getViewTypeCount());
  }
}
