package com.tomgibara.collect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import junit.framework.TestCase;

import com.tomgibara.hashing.HashCode;
import com.tomgibara.hashing.HashSize;
import com.tomgibara.hashing.Hasher;

public class EquivalenceTest extends TestCase {

	enum StorageType {
		PRIMITIVE,
		OBJECT,
		GENERIC
	}
	
	EquRel<Integer> modulo(int n) {
		return new EquRel<Integer>() {

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

	public void testModuloSet() {
		for (StorageType storageType : StorageType.values()) {
			for (int n = 1; n <= 64; n++) {
				testModuloSet(n, storageType);
			}
		}
	}
	
	private void testModuloSet(int n, StorageType s) {
		Equivalence<Integer> equivalence = Collect.equivalence(modulo(n));
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
			set.add(i);
			assertTrue(set.contains(i));
			assertEquals(Math.min(i + 1, n), set.size());

			
			Set<Integer> check = new HashSet<Integer>();
			for (Integer e : set) {
				check.add(e);
			}
			assertFalse(check.contains(null));
			assertEquals(set.size(), check.size());
			assertEquals(set, check);

			EquivalenceSet<Integer> mv = set.mutableView();
			assertTrue(mv.contains(i));
			assertTrue(mv.remove(i));
			assertFalse(set.contains(i));
			assertTrue(mv.add(i));
			assertTrue(set.contains(i));

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

	public void testModuloMap() {
		for (StorageType storageType : StorageType.values()) {
			for (int n = 1; n <= 64; n++) {
				testModuloMap(n, storageType);
			}
		}
	}

	private void testModuloMap(int n, StorageType s) {
		Equivalence<Integer> equivalence = Collect.equivalence(modulo(n));
		EquivalenceMap<Integer, String> map;
		switch (s) {
		case GENERIC:
			map = equivalence.setsWithGenericStorage().mappedToTypedStorage(String.class).newMap(); break;
		case OBJECT:
			map = equivalence.setsWithTypedStorage(Integer.class).mappedToTypedStorage(String.class).newMap(); break;
		case PRIMITIVE:
			map = equivalence.setsWithTypedStorage(int.class).mappedToTypedStorage(String.class).newMap(); break;
			default: throw new IllegalStateException();
		}
		assertTrue(map.isEmpty());
		for (int i = 0; i < 2 * n; i++) {
			assertEquals(i >= n, map.containsKey(i));
			String value = Integer.toString(i);
			map.put(i, value);
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

			EquivalenceMap<Integer, String> mv = map.mutableView();
			assertTrue(mv.containsKey(i));
			assertNotNull(mv.remove(i));
			assertFalse(map.containsKey(i));
			assertNull(mv.put(i, value));
			assertTrue(map.containsKey(i));

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
}
