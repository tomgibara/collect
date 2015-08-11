package com.tomgibara.collect;

import com.tomgibara.hashing.HashCode;
import com.tomgibara.hashing.HashSize;
import com.tomgibara.hashing.Hasher;

import junit.framework.TestCase;

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
	
	public void testModulo() {
		for (StorageType storageType : StorageType.values()) {
			for (int n = 1; n <= 128; n++) {
				testModulo(n, storageType);
			}
		}
	}
	
	private void testModulo(int n, StorageType s) {
		Equivalence<Integer> equivalence = Collect.equivalence(modulo(n));
		EquivalenceSet<Integer> set;
		switch (s) {
		case GENERIC:
			set = equivalence.setsWithGenericStorage().newSet(); break;
		case OBJECT:
			set = equivalence.setsWithTypedStorage(int.class).newSet(); break;
		case PRIMITIVE:
			set = equivalence.setsWithTypedStorage(Integer.class).newSet(); break;
			default: throw new IllegalStateException();
		}
		for (int i = 0; i < 2 * n; i++) {
			assertEquals(i >= n, set.contains(i));
			set.add(i);
			assertTrue(set.contains(i));
			assertEquals(Math.min(i + 1, n), set.size());

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
	
}
