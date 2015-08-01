package com.tomgibara.collect;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

import com.tomgibara.collect.Tokens.Maps;

import junit.framework.TestCase;

public class TokensTest extends TestCase {

	@Test
	public void testSet() {
		Tokens animals = Tokens.of("cat", "dog", "cow", "horse");
		Set<String> mammals = animals.newFullSet();
		assertEquals(4, mammals.size());
		assertTrue(mammals.contains("dog"));
		assertFalse(mammals.contains("ant"));
		int count = 0;
		Set<String> set = new HashSet<String>();
		for (String mammal : mammals) {
			set.add(mammal);
		}
		assertEquals(4, set.size());
		assertEquals(mammals, set);
		assertEquals(set, mammals);
		assertEquals(mammals.hashCode(), set.hashCode());
		for (Iterator<String> i = mammals.iterator(); i.hasNext(); ) {
			String mammal = i.next();
			if (mammal.equals("dog")) {
				i.remove();
			}
		}
		assertEquals(3, mammals.size());
		assertFalse(mammals.contains("dog"));
		mammals.remove("cow");
		assertEquals(2, mammals.size());
		assertFalse(mammals.contains("cow"));
		mammals.remove("ant");
		assertEquals(2, mammals.size());
		try {
			mammals.add("ant");
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
	}

	@Test
	public void testMap() {
		Tokens animals = Tokens.of("ostrich", "dog", "snail", "centipede");
		Maps<Integer> counts = animals.withTypedStorage(int.class);
		Map<String, Integer> legs = counts.newMap();
		assertNull( legs.get("ostrich") );
		legs.put("ostrich", 2);
		assertEquals((Integer)2, legs.get("ostrich"));
		try {
			legs.put("whippet", 3);
			fail();
		} catch (IllegalArgumentException e) {
			/* expected */
		}
		legs.put("dog", 3);
		for (Entry<String,Integer> entry : legs.entrySet()) {
			switch (entry.getKey()) {
			case "dog" : entry.setValue(4); continue;
			case "ostrich": continue;
			default: fail();
			}
		}
		assertEquals((Integer) 4, legs.get("dog"));
		assertEquals(2, legs.keySet().size());
		legs.remove("dog");
		assertEquals(1, legs.size());
		assertNull(legs.get("dog"));
		legs.remove("whippet");
		legs.putAll(Collections.singletonMap("snail", 1));
		assertEquals((Integer) 1, legs.get("snail"));
		assertEquals(2, legs.size());
	}
	
}