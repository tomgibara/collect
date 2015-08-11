package com.tomgibara.collect;

import java.util.Random;

import com.tomgibara.hashing.HashCode;
import com.tomgibara.hashing.HashSize;
import com.tomgibara.hashing.Hasher;
import com.tomgibara.storage.Store;

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

	Cuckoo(Random random, EquRel<E> equ) {
		this.random = random;
		this.equ = equ;
		basis = equ.getHasher().ints();
	}

	int[] newHashesArray() {
		return new int[HASH_COUNT];
	}

	Hasher<E> updateHasher(Hasher<E> oldHasher, int newCapacity) {
		return oldHasher != null && oldHasher.getSize().asInt() == newCapacity ?
				oldHasher :
				basis.sized(HashSize.fromInt(newCapacity));
	}
	
	Access access(Store<E> store, Hasher<E> hasher) {
		return new Access(store, hasher);
	}

	final class Access {
		
		private final Store<E> store;
		private final Hasher<E> hasher;
		
		Access(Store<E> store, Hasher<E> hasher) {
			this.store = store;
			this.hasher = hasher;
		}

		Object add(E e) {
			return insert(e, -1, 0);
		}
		
		// Note: non-recursive implementation
		private Object insert(E e, int oldIndex, int retryCount) {
//			System.out.println("inserting " + e + " from " + oldIndex + " (" + retryCount + ")");
			int[] hashes = new int[HASH_COUNT];

			while (true) {
				// initially check e not present
				HashCode hash = hasher.hash(e);
				int firstNull = -1;
				for (int i = 0; i < HASH_COUNT; i++) {
					int h = hash.intValue();
					hashes[i] = h;
					E e2 = store.get(h);
					if (oldIndex == -1) {
						// on the first pass, the value may not already by present
						if (e2 == null) {
							if (firstNull == -1) firstNull = h;
						} else {
							if (equ.isEquivalent(e, e2)) return FAILURE;
						}
					} else {
						// we know the value already there, just looking for nulls
						if (e2 == null) {
							firstNull = i;
							break;
						}
					}
				}
				// easy case - we have a null
				if (firstNull != -1) {
					store.set(firstNull, e);
					return SUCCESS;
				}
		
				// there's work to do to find a slot
				int i = random.nextInt(HASH_COUNT);
				int h = hashes[i];
				E e2 = store.get(h);
				store.set(h, e);

				// this has gone on too long, expect caller to enlarge the backing store and try again;
				if (retryCount >= RETRY_LIMIT) return e2;

				// update variables and retry
				e = e2;
				oldIndex = h;
				retryCount++;
			}
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
		
		private int checkedIndexOf(E e) {
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
