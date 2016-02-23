package com.tomgibara.collect;

import java.util.AbstractSet;
import java.util.Iterator;

import com.tomgibara.hashing.Hasher;
import com.tomgibara.storage.Storage;
import com.tomgibara.storage.Store;

final class CuckooEquivalenceSet<E> extends AbstractSet<E> implements EquivalenceSet<E> {

	// fields
	
	private final Cuckoo<E> cuckoo;
	private final Storage<E> storage;
	private Hasher<E> hasher = null;
	private Store<E> store;
	
	// constructors
	
	CuckooEquivalenceSet(Cuckoo<E> cuckoo, Storage<E> storage, int initialCapacity) {
		this.cuckoo = cuckoo;
		this.storage = storage;
		hasher = cuckoo.updateHasher(hasher, initialCapacity);
		store = storage.newStore(initialCapacity);
	}

	private CuckooEquivalenceSet(CuckooEquivalenceSet<E> that, Store<E> store) {
		this.cuckoo = that.cuckoo;
		this.storage = that.storage;
		this.store = store;
		hasher = cuckoo.updateHasher(hasher, store.size());
	}

	// equivalence

	@Override
	public Equivalence<E> getEquivalence() {
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
		return new CuckooEquivalenceSet<>(this, store.mutableCopy());
	}
	
	@Override
	public EquivalenceSet<E> immutableCopy() {
		return new CuckooEquivalenceSet<>(this, store.immutableCopy());
	}
	
	@Override
	public EquivalenceSet<E> immutableView() {
		return new CuckooEquivalenceSet<>(this, store.immutable());
	}

	// set
	
	@Override
	public int size() {
		return store.count();
	}
	
	@Override
	public boolean contains(Object o) {
		return access().indexOf(o) != -1;
	}
	
	@Override
	public boolean remove(Object o) {
		if (!store.isMutable()) throw new IllegalStateException("immutable");
		int i = access().indexOf(o);
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
		return store.count() == 0;
	}
	
	@Override
	public boolean add(E e) {
		if (e == null) throw new IllegalArgumentException("null e");
		if (!store.isMutable()) throw new IllegalStateException("immutable");
		return access().add(e);
	}
	
	@Override
	public Iterator<E> iterator() {
		return store.iterator();
	}

	// private utility methods
	
	private Cuckoo<E>.Access<Void> resize() {
		Store<E> oldStore = store;
		int oldCapacity = oldStore.size();
		store = storage.newStore(oldCapacity * 2);
		hasher = cuckoo.updateHasher(hasher, store.size());
		Cuckoo<E>.Access<Void> access = access();
		for (int j = 0; j < oldCapacity; j++) {
			E t = oldStore.get(j);
			if (t != null) access.add(t);
		}
		return access;
	}
	
	private Cuckoo<E>.Access<Void> access() {
		return cuckoo.access(store, hasher, this::resize, null);
	}
}
