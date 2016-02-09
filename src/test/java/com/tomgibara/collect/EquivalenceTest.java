package com.tomgibara.collect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
		EquivalenceCol<Integer> equivalence = Collect.equivalence(modulo(n));
		EquivalenceSet<Integer> set;
		switch (s) {
		case GENERIC:
			set = equivalence.setsWithGenericStorage().newSet(); break;
		case OBJECT:
			set = equivalence.setsWithTypedStorage(Integer.class).newSet(); break;
		case PRIMITIVE:
			set = equivalence.setsWithTypedStorage(int.class).newSet(); break;
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
		EquivalenceCol<Integer> equivalence = Collect.equivalence(rel);
		EquivalenceMap<Integer, String> map;
		switch (s) {
		case GENERIC:
			map = equivalence.setsWithGenericStorage().mappedToTypedStorage(String.class, "").newMap(); break;
		case OBJECT:
			map = equivalence.setsWithTypedStorage(Integer.class).mappedToTypedStorage(String.class, "").newMap(); break;
		case PRIMITIVE:
			map = equivalence.setsWithTypedStorage(int.class).mappedToTypedStorage(String.class, "").newMap(); break;
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
		EquivalenceCol<String> equality = Collect.equality();
		EquivalenceMap<String, String> map = equality.setsWithTypedStorage(String.class).mappedToTypedStorage(String.class, "").newMap();
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
		EquivalenceMap<Object, Object> map = Collect.equality().setsWithGenericStorage().mappedToGenericStorage().newMap();
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
				.<String>equality()
				.setsWithGenericStorage()
				.<String>mappedToGenericStorage()
				.newMap();
		checkKeys(map);
	}

	@Test
	public void testKeys2() {
		EquivalenceMap<String,String> map = Collect
				.<String>equality()
				.setsWithTypedStorage(String.class)
				.mappedToTypedStorage(String.class, "")
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


}
