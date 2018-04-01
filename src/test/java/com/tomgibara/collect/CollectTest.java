package com.tomgibara.collect;

import org.junit.Assert;
import org.junit.Test;

public class CollectTest {

	@Test
	public void testCopyEmpty() {
		Assert.assertTrue(Collect.sets().emptySet().isEmpty());
		Assert.assertTrue(Collect.sets().mapped().emptyMap().isEmpty());
		Collect.sets().emptySet().mutableCopy().add("element");
		Collect.sets().mapped().emptyMap().mutableCopy().put("key", "value");
	}
}
