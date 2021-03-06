package com.tomgibara.collect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import com.tomgibara.hashing.HashCode;
import com.tomgibara.hashing.HashSize;
import com.tomgibara.hashing.Hasher;

public class EquivalenceTest {

	enum StorageType {
		PRIMITIVE,
		OBJECT,
		GENERIC
	}
	
	Equivalence<Integer> modulo(int n) {
		return new Equivalence<Integer>() {

			private final Hasher<Integer> hasher = new Hasher<Integer>() {

				private final HashSize size = HashSize.fromInt(n);

				@Override
				public HashSize getSize() {
					return size;
				}

				@Override
				public HashCode hash(Integer value) throws IllegalArgumentException {
					return HashCode.fromInt(value % n);
				}

			};

			@Override
			public boolean isEquivalent(Integer e1, Integer e2) {
				int i1 = e1.intValue();
				int i2 = e2.intValue();
				return (i1 - i2) % n == 0;
			}

			@Override
			public Hasher<Integer> getHasher() {
				return hasher;
			}

		};
	}

	@Test
	public void testModuloSet() {
		for (StorageType storageType : StorageType.values()) {
			for (int n = 1; n <= 64; n++) {
				testModuloSet(n, storageType);
			}
		}
	}
	
	private void testModuloSet(int n, StorageType s) {
		EquivalenceSet<Integer> set;
		Equivalence<Integer> equ = modulo(n);
		switch (s) {
		case GENERIC:
			set = Collect.<Integer>sets().underEquivalence(equ).newSet(); break;
		case OBJECT:
			set = Collect.setsOf(Integer.class).underEquivalence(equ).newSet(); break;
		case PRIMITIVE:
			set = Collect.setsOf(int.class).underEquivalence(equ).newSet(); break;
			default: throw new IllegalStateException();
		}
		assertTrue(set.isEmpty());
		for (int i = 0; i < 2 * n; i++) {
			assertEquals(i >= n, set.contains(i));
			for (int j = i; j < n; j++) {
				assertFalse(set.contains(j));
			}
			set.add(i);
			assertTrue(set.contains(i));
			for (int j = 0; j < i; j++) {
				assertTrue("missing " + j + " after adding " + i, set.contains(j));
			}
			assertEquals(Math.min(i + 1, n), set.size());

			Set<Integer> check = new TreeSet<Integer>();
			for (Integer e : set) {
				check.add(e);
			}
			assertEquals(set.size(), check.size());
			assertEquals(set, check);

			try {
				EquivalenceSet<Integer> imv = set.immutableView();
				assertTrue(imv.contains(i));
				imv.add(i);
				fail();
			} catch (IllegalStateException e) {
				/* expected */
			}
		}

		for (int i = 0; i < 2 * n; i++) {
			assertEquals(i < n, set.contains(i));
			assertTrue(set.remove(i) == i < n);
			assertEquals(Math.max(0, n - i - 1), set.size());
		}
	}

	@Test
	public void testModuloMap() {
		for (StorageType storageType : StorageType.values()) {
			for (int n = 1; n <= 64; n++) {
				testModuloMap(n, storageType);
			}
		}
	}

	private void testModuloMap(int n, StorageType s) {
		Equivalence<Integer> rel = modulo(n);
		EquivalenceMap<Integer, String> map;
		switch (s) {
		case GENERIC:
			map = Collect.<Integer>sets().underEquivalence(rel).mappedTo(String.class).newMap(); break;
		case OBJECT:
			map = Collect.setsOf(Integer.class).underEquivalence(rel).mappedTo(String.class).newMap(); break;
		case PRIMITIVE:
			map = Collect.setsOf(int.class).underEquivalence(rel).mappedTo(String.class).newMap(); break;
			default: throw new IllegalStateException();
		}
		assertTrue(map.isEmpty());
		for (int i = 0; i < 2 * n; i++) {
			assertEquals(i >= n, map.containsKey(i));
			String value = Integer.toString(i);
			map.put(i, value);
			for (int j = 0; j <= i; j++) {
				assertTrue(rel.isEquivalent(j, Integer.valueOf(map.get(j))));
			}
			assertTrue(map.containsKey(i));
			assertTrue(map.containsValue(value));
			assertEquals(Math.min(i + 1, n), map.size());

			
			Map<Integer, String> check = new HashMap<Integer, String>();
			for (Entry<Integer,String> e : map.entrySet()) {
				check.put(e.getKey(), e.getValue());
			}
			assertFalse(map.containsKey(null));
			assertFalse(map.containsValue(null));
			assertEquals(map.size(), check.size());
			assertEquals(map, check);
			assertEquals(check, map);

			try {
				EquivalenceMap<Integer, String> imv = map.immutableView();
				assertTrue(imv.containsKey(i));
				imv.put(i, "Oops!");
				fail();
			} catch (IllegalStateException e) {
				/* expected */
			}
		}

		for (int i = 0; i < 2 * n; i++) {
			assertEquals(i < n, map.containsKey(i));
			assertEquals(i < n, map.remove(i) != null);
			assertEquals(Math.max(0, n - i - 1), map.size());
		}

		map.put(0, "X");
		map.keySet().remove(0);
		assertFalse(map.containsKey(0));
		
		map.put(0, "X");
		map.entrySet().iterator().next().setValue("Y");
		assertEquals("Y", map.get(0));
	
		map.put(n/2, "A");
		map.replace(n/2, "X", "B");
		assertFalse(map.values().contains("B"));
		map.replace(n/2, "A", "B");
		assertTrue(map.values().contains("B"));
		map.values().remove("B");
		assertFalse(map.containsKey(n/2));
		assertFalse(map.containsValue("B"));
	}
	
	@Test
	public void testMapSetConsistency() {
		EquivalenceMap<String, String> map = Collect.setsOf(String.class).mappedTo(String.class).newMap();
		int size = 1000;
		String[] strs = new String[size];
		for (int i = 0; i < 1000; i++) {
			strs[i] = Integer.toString(i);
		}
		for (String str : strs) {
			map.put(str, str);
		}
		Set<String> keys = map.keySet();
		for (String key : keys) {
			assertEquals(key, map.get(key));
		}
	}
	
	@Test
	public void testValues() {
		EquivalenceMap<Object, Object> map = Collect.sets().mapped().newMap();
		Collection<Object> values = map.values();
		assertTrue(values.isEmpty());
		assertFalse(values.iterator().hasNext());
		map.put("one", "1");
		assertFalse(values.isEmpty());
		Iterator<Object> it = values.iterator();
		assertTrue(it.hasNext());
		assertEquals("1", it.next());
		assertFalse(it.hasNext());
	}

	@Test
	public void testKeys() {
		EquivalenceMap<String, String> map = Collect
				.<String>sets()
				.<String>mapped()
				.newMap();
		checkKeys(map);
	}

	@Test
	public void testKeys2() {
		EquivalenceMap<String,String> map = Collect
				.setsOf(String.class)
				.mappedTo(String.class)
				.newMap();
		checkKeys(map);
	}

	private void checkKeys(EquivalenceMap<String, String> map) {
		Set<String> keys = map.keySet();
		assertTrue(keys.isEmpty());
		assertFalse(keys.iterator().hasNext());
		map.put("one", "1");
		assertFalse(keys.isEmpty());
		Iterator<String> it = keys.iterator();
		assertTrue(it.hasNext());
		assertEquals("one", it.next());
		assertFalse(it.hasNext());
	}

	@Test
	public void testKeySet() {
		EquivalenceMap<Integer, Integer> map = Collect
				.setsOf(int.class)
				.mappedTo(int.class)
				.newMap();
		// build map
		map.put(1, 2);
		map.put(2, 3);
		map.put(3, 5);
		// check keys matches
		EquivalenceSet<Integer> keys = map.keySet();
		assertEquals(3, keys.size());
		assertTrue(keys.contains(1));
		assertTrue(keys.contains(2));
		assertTrue(keys.contains(3));
		// check mutations to keys {
		{
			Iterator<Integer> it = keys.iterator();
			while (it.hasNext()) {
				if (it.next() == 2) it.remove();
			}
		}
		assertTrue(keys.contains(1));
		assertFalse(keys.contains(2));
		assertTrue(keys.contains(3));
		// check mutable copy
		EquivalenceSet<Integer> mc = keys.mutableCopy();
		assertTrue(mc.isMutable());
		mc.add(2);
		assertTrue(mc.contains(2));
		assertEquals(3, mc.size());
		assertFalse(keys.contains(2));
		assertEquals(2, keys.size());
		// check immutable copy and view
		EquivalenceSet<Integer> ic = keys.immutableCopy();
		EquivalenceSet<Integer> iv = keys.immutableView();
		assertFalse(ic.isMutable());
		assertFalse(iv.isMutable());
		map.put(2,  3);
		assertTrue(keys.contains(2));
		assertFalse(ic.contains(2));
		assertTrue(iv.contains(2));
		// check clear
		keys.clear();
		assertTrue(map.isEmpty());
		assertFalse(keys.contains(1));
		assertFalse(keys.contains(2));
		assertFalse(keys.contains(3));
	}

	
	@Test
	public void testImmutableSetView() {
		EquivalenceSet<Integer> set = Collect
				.setsOf(int.class)
				.newSet();
		EquivalenceSet<Integer> view = set.immutableView();
		
		for (int i = 0; i < 1000; i++) {
			set.add(i);
			assertTrue("element " + i + " missing", view.contains(i));
		}
	}

	@Test
	public void testImmutableMapView() {
		EquivalenceMap<Integer, Integer> map = Collect
				.setsOf(int.class)
				.mappedTo(int.class)
				.newMap();
		EquivalenceMap<Integer, Integer> view = map.immutableView();
		
		for (int i = 0; i < 1000; i++) {
			map.put(i,i);
			assertTrue("element " + i + " missing", view.containsKey(i));
			assertEquals("element " + i + " incorrect", i, view.get(i).intValue());
		}
	}

	@Test
	public void testSome() {
		EquivalenceSet<String> set = Collect.<String>sets().newSet();
		assertNull(set.some());
		set.add("X");
		assertEquals("X", set.some());
		set.add("Y");
		assertNotNull(set.some());
		set.remove("X");
		assertEquals("Y", set.some());

		assertEquals("Y", set.immutable().some());
		set.clear();
		assertNull(set.some());

		EquivalenceMap<Integer, Integer> map = Collect
				.setsOf(int.class)
				.mappedTo(int.class)
				.newMap();

		assertNull(map.keySet().some());
		map.put(3,15);
		assertEquals(3, map.keySet().some().intValue());
	}

	@Test
	public void testSingletonSet() {
		EquivalenceSet<String> set = Collect.<String>sets().singletonSet("X");
		assertEquals(1, set.size());
		assertTrue(set.contains("X"));
		assertFalse(set.isMutable());
	}

	@Test
	public void testEmptySet() {
		EquivalenceSet<String> set = Collect.<String>sets().emptySet();
		assertTrue(set.isEmpty());
		assertFalse(set.isMutable());
	}

	@Test
	public void testPuttingNull() {
		EquivalenceMap<Integer, Integer> map = Collect
				.setsOf(int.class)
				.mappedTo(int.class)
				.newMap();

		assertFalse( map.containsKey(1) );
		assertNull( map.put(1, 99) );
		assertTrue( map.containsKey(1) );
		assertEquals(99, map.put(1, null).intValue());
		assertTrue( map.containsKey(1) );
		assertNull( map.put(1, 66) );
		assertEquals(66, map.remove(1).intValue());
	}
}
