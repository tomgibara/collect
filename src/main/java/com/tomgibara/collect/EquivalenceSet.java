package com.tomgibara.collect;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Random;

import com.tomgibara.hashing.HashCode;
import com.tomgibara.hashing.HashSize;
import com.tomgibara.hashing.Hasher;
import com.tomgibara.storage.Mutability;
import com.tomgibara.storage.Storage;
import com.tomgibara.storage.Store;

public class EquivalenceSet<E> extends AbstractSet<E> implements Mutability<EquivalenceSet<E>> {

	// statics
	
	private static final int HASH_COUNT = 3;
	private static final int RETRY_LIMIT = 3;
	
	// fields
	
	private final int[] hashes = new int[HASH_COUNT];
	private final Random random;
	private final EquRel<E> equ;
	private final Storage<E> storage;
	private final Hasher<E> basis;
	private Store<E> store;
	private Hasher<E> hasher;
	
	// constructors
	
	EquivalenceSet(Random random, EquRel<E> equ, Storage<E> storage, int initialCapacity) {
		this.random = random;
		this.equ = equ;
		this.storage = storage;
		basis = equ.getHasher().ints();
		store = storage.newStore(initialCapacity);
		hasher = deriveHasher();
	}

	private EquivalenceSet(EquivalenceSet<E> that, Store<E> store) {
		this.random = that.random;
		this.equ = that.equ;
		this.storage = that.storage;
		this.basis = that.basis;
		this.store = store;
		hasher = that.hasher.getSize().asInt() == store.capacity() ? that.hasher : deriveHasher();
	}

	// accessors
	
	public EquRel<E> getEquivalence() {
		return equ;
	}

	// mutability
	
	@Override
	public boolean isMutable() {
		return store.isMutable();
	}
	
	@Override
	public EquivalenceSet<E> mutable() {
		return isMutable() ? this : mutableCopy();
	}

	@Override
	public EquivalenceSet<E> immutable() {
		return isMutable() ? immutableView() : this;
	}
	
	@Override
	public EquivalenceSet<E> mutableCopy() {
		return new EquivalenceSet<>(this, store.mutableCopy());
	}
	
	@Override
	public EquivalenceSet<E> immutableCopy() {
		return new EquivalenceSet<>(this, store.immutableCopy());
	}
	
	@Override
	public EquivalenceSet<E> mutableView() {
		if (!store.isMutable()) throw new IllegalStateException("Cannot take a mutable view of an immutable set");
		return new EquivalenceSet<>(this, store);
	}
	
	@Override
	public EquivalenceSet<E> immutableView() {
		return new EquivalenceSet<>(this, store.immutable());
	}

	// set
	
	@Override
	public int size() {
		return store.size();
	}
	
	@Override
	public boolean contains(Object o) {
		return indexOf(o) != -1;
	}
	
	@Override
	public boolean remove(Object o) {
		if (!store.isMutable()) throw new IllegalStateException("immutable");
		int i = indexOf(o);
		if (i == -1) return false;
		store.set(i, null);
		return true;
	}
	
	@Override
	public void clear() {
		store.clear();
	}
	
	@Override
	public boolean isEmpty() {
		return store.size() == 0;
	}
	
	@Override
	public boolean add(E e) {
		if (e == null) throw new IllegalArgumentException("null e");
		if (!store.isMutable()) throw new IllegalStateException("immutable");
		return insert(e, -1, 0);
	}
	
	@Override
	public Iterator<E> iterator() {
		return new StoreIterator<E, E>(store) {
			@Override
			E get(int index) {
				return store.get(index);
			}
		};
	}
	
	private boolean insert(E e, int oldIndex, int retryCount) {
//		System.out.println("inserting " + e + " from " + oldIndex + " (" + retryCount + ")");
		int[] hashes = hashes(e);
		// first check e not present
		int firstNull = -1;
		for (int i = 0; i < HASH_COUNT; i++) {
			int h = hashes[i];
			E e2 = store.get(h);
			if (oldIndex == -1) {
				// on the first pass, the value may not already by present
				if (e2 == null) {
					if (firstNull == -1) firstNull = h;
				} else {
					if (equ.isEquivalent(e, e2)) return false;
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
			return true;
		}

		// there's work to do to find a slot
		int i = random.nextInt(HASH_COUNT);
		int h = hashes[i];
		E e2 = store.get(h);
		store.set(h, e);

		if (retryCount < RETRY_LIMIT) {
			insert(e2, h, retryCount + 1);
			return true;
		}

		// this has gone on too long, enlarge the backing store;
		Store<E> oldStore = store;
		int oldCapacity = oldStore.capacity();
		store = storage.newStore(oldCapacity * 2);
		hasher = deriveHasher();
		for (int j = 0; j < oldCapacity; j++) {
			E t = oldStore.get(j);
			if (t != null) insert(t, -1, 0);
		}
		insert(e2, -1, 0);
		return true;
	}

	private Hasher<E> deriveHasher() {
		return basis.sized(HashSize.fromInt(store.capacity()));
	}
	
	@SuppressWarnings("unchecked")
	private int indexOf(Object o) {
		if (o == null) return -1;
		// we don't really have a way of avoiding these possible exceptions
		try {
			return checkedIndexOf( (E) o );
		} catch (ClassCastException|IllegalArgumentException e ) {
			return -1;
		}
	}
	
	private int checkedIndexOf(E e) {
		if (e == null) throw new IllegalArgumentException("null e");
		HashCode hash = hasher.hash(e);
		for (int i = 0; i < HASH_COUNT; i++) {
			int index = hash.intValue();
			if (check(index, e)) return index;
		}
		return -1;
	}
	
	private int[] hashes(E e) {
		HashCode hash = hasher.hash(e);
		for (int i = 0; i < HASH_COUNT; i++) {
			hashes[i] = hash.intValue();
		}
		return hashes;
	}
	
	private boolean check(int i, E e) {
		E e2 = store.get(i);
		return e2 != null && equ.isEquivalent(e, e2);
	}
}
