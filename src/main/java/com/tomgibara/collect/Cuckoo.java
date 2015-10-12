package com.tomgibara.collect;

import java.util.Random;

import com.tomgibara.hashing.HashCode;
import com.tomgibara.hashing.HashSize;
import com.tomgibara.hashing.Hasher;
import com.tomgibara.storage.Store;

//TODO hash count should be adjustable
final class Cuckoo<E> {

	// statics
	
	//hack to overcome lack of union return types in Java
	
	// indicates value was successfully added to store
	static final Object SUCCESS = new Object();
	// indicates value could not be added to store because it was already present
	static final Object FAILURE = new Object();
	
	private static final int HASH_COUNT = 3;
	private static final int RETRY_LIMIT = 3;
	
	// fields
	
	final Random random;
	final EquRel<E> equ;
	private final Hasher<E> basis;

	// constructors
	
	Cuckoo(Random random, EquRel<E> equ) {
		this.random = random;
		this.equ = equ;
		basis = equ.getHasher().ints();
	}
	
	// package scoped methods

	Hasher<E> updateHasher(Hasher<E> oldHasher, int newCapacity) {
		return oldHasher != null && oldHasher.getSize().asInt() == newCapacity ?
				oldHasher :
				basis.sized(HashSize.fromInt(newCapacity));
	}
	
	<V> Access<V> access(Store<E> store, Hasher<E> hasher, Resizer<E,V> resize, Store<V> values) {
		return new Access<V>(store, hasher, resize, values);
	}
	
	// private utility methods
	
	private int[] newHashesArray() {
		return new int[HASH_COUNT];
	}

	// inner classes
	
	interface Resizer<E,V> {
		
		Cuckoo<E>.Access<V> resize();
		
	}

	final class Access<V> {
		
		private final Store<E> store;
		private final Hasher<E> hasher;
		private final Resizer<E,V> resize;
		private final Store<V> values;
		
		Access(Store<E> store, Hasher<E> hasher, Resizer<E,V> resize, Store<V> values) {
			this.store = store;
			this.hasher = hasher;
			this.resize = resize;
			this.values = values;
		}

		// Note: logic between adding/putting is so very similar, but slightly different
		// duplication appears to be the only practical option at the moment.

		boolean add(E e) {
			int[] hashes = newHashesArray();
			int retryCount = 0;
			boolean first = true;

			while (true) {
				// initially check e not present
				HashCode hash = hasher.hash(e);
				int firstNull = -1;
				for (int i = 0; i < HASH_COUNT; i++) {
					int h = hash.intValue();
					hashes[i] = h;
					E e2 = store.get(h);
					if (first) {
						// on the first pass, the value may not already by present
						if (e2 == null) {
							// note can't just break here, e may still be present at another index
							if (firstNull == -1) firstNull = h;
						} else if (equ.isEquivalent(e, e2)) {
							return false;
						}
					} else {
						// we know the value is no longer there, because we overwrote it, so just looking for nulls
						if (e2 == null) {
							firstNull = h;
							break;
						}
					}
				}
				// easy case - we have a null
				if (firstNull != -1) {
					store.set(firstNull, e);
					return true;
				}

				// there's work to do to find a slot
				int i = random.nextInt(HASH_COUNT);
				int h = hashes[i];
				E e2 = store.get(h);
				store.set(h, e);

				// this has gone on too long, enlarge the backing store and continue;
				if (retryCount >= RETRY_LIMIT) {
					Cuckoo<E>.Access<V> access = resize.resize();
					access.add(e2);
					return true;
				}

				// update variables and retry
				e = e2;
				first = false;
				retryCount++;
			}
		}

		// Note: non-recursive add, but resize recurses
		V put(E e, V v, boolean overwrite) {
			int[] hashes = new int[HASH_COUNT];
			V previous = null;
			int retryCount = 0;
			boolean first = true;

			outer: while (true) {
				// initially check e not present
				HashCode hash = hasher.hash(e);
				int firstNull = -1;
				for (int i = 0; i < HASH_COUNT; i++) {
					int h = hash.intValue();
					hashes[i] = h;
					E e2 = store.get(h);
					if (first) {
						// on the first pass, the value may not already by present
						if (e2 == null) {
							// note can't just break here, e may still be present at another index
							if (firstNull == -1) firstNull = h;
						} else if (equ.isEquivalent(e, e2)) {
							// we can insert the value here
							if (values != null) {
								previous = values.get(h);
								// the key is already present
								if (overwrite) values.set(h, v);
							}
							break outer;
						}
					} else {
						// we know the value is no longer there, because we overwrote it, so just looking for nulls
						if (e2 == null) {
							firstNull = h;
							break;
						}
					}
				}
				// easy case - we have a null
				if (firstNull != -1) {
					store.set(firstNull, e);
					if (values != null) {
						if (first) previous = values.get(firstNull);
						values.set(firstNull, v);
					}
					break;
				}

				// there's work to do to find a slot
				int i = random.nextInt(HASH_COUNT);
				int h = hashes[i];
				E e2 = store.get(h);
				V v2 = values == null ? null : values.get(h);
				store.set(h, e);
				if (values != null) {
					if (first) previous = values.get(h);
					values.set(h, v);
				}

				// this has gone on too long, enlarge the backing store and continue;
				if (retryCount >= RETRY_LIMIT) {
					Cuckoo<E>.Access<V> access = resize.resize();
					V result = access.put(e2, v2, false); // overwrite value actually irrelevant since key not 
					if (first) previous = result;
					break;
				}

				// update variables and retry
				e = e2;
				v = v2;
				first = false;
				retryCount++;
			}
			return previous;
		}

		@SuppressWarnings("unchecked")
		int indexOf(Object o) {
			if (o == null) return -1;
			// we don't really have a way of avoiding these possible exceptions
			try {
				return checkedIndexOf((E) o);
			} catch (ClassCastException|IllegalArgumentException e ) {
				return -1;
			}
		}
		
		int checkedIndexOf(E e) {
			if (e == null) throw new IllegalArgumentException("null e");
			HashCode hash = hasher.hash(e);
			for (int i = 0; i < HASH_COUNT; i++) {
				int index = hash.intValue();
				E e2 = store.get(index);
				if (e2 != null && equ.isEquivalent(e, e2)) return index;
			}
			return -1;
		}
	
	}

}
