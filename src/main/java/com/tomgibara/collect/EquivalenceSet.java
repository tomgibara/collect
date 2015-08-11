package com.tomgibara.collect;

import java.util.AbstractSet;
import java.util.Iterator;

import com.tomgibara.hashing.Hasher;
import com.tomgibara.storage.Mutability;
import com.tomgibara.storage.Storage;
import com.tomgibara.storage.Store;

public final class EquivalenceSet<E> extends AbstractSet<E> implements Mutability<EquivalenceSet<E>> {

	// fields
	
	private final Cuckoo<E> cuckoo;
	private final Storage<E> storage;
	private Hasher<E> hasher = null;
	private Store<E> store;
	
	// constructors
	
	EquivalenceSet(Cuckoo<E> cuckoo, Storage<E> storage, int initialCapacity) {
		this.cuckoo = cuckoo;
		this.storage = storage;
		hasher = cuckoo.updateHasher(hasher, initialCapacity);
		store = storage.newStore(initialCapacity);
	}

	private EquivalenceSet(EquivalenceSet<E> that, Store<E> store) {
		this.cuckoo = that.cuckoo;
		this.storage = that.storage;
		this.store = store;
		hasher = cuckoo.updateHasher(hasher, store.capacity());
	}

	// accessors
	
	public EquRel<E> getEquivalence() {
		return cuckoo.equ;
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
		return cuckoo.access(store, hasher).indexOf(o) != -1;
	}
	
	@Override
	public boolean remove(Object o) {
		if (!store.isMutable()) throw new IllegalStateException("immutable");
		int i = cuckoo.access(store, hasher).indexOf(o);
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
		Cuckoo<E>.Access access = cuckoo.access(store, hasher);
		while (true) {
			Object result = access.add(e);
			if (result == Cuckoo.SUCCESS) return true;
			if (result == Cuckoo.FAILURE) return false;

			e = (E) result;
			Store<E> oldStore = store;
			int oldCapacity = oldStore.capacity();
			store = storage.newStore(oldCapacity * 2);
			hasher = cuckoo.updateHasher(hasher, store.capacity());
			access = cuckoo.access(store, hasher);
			for (int j = 0; j < oldCapacity; j++) {
				E t = oldStore.get(j);
				if (t != null) access.add(t);
			}
		}
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

}
