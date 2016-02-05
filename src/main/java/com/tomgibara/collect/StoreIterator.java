package com.tomgibara.collect;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.tomgibara.storage.Store;

//TODO move to store project and maybe change to using a lambda instead of a protected method?
public abstract class StoreIterator<V, E> implements Iterator<E> {

	final Store<V> store;
	private int previous = -1;
	private int next;

	public StoreIterator(Store<V> store) {
		this.store = store;
		next = subsequent(0);
	}
	
	@Override
	public E next() {
		if (next == store.size()) throw new NoSuchElementException();
		E x = get(next);
		previous = next;
		next = subsequent(next + 1);
		return x;
	}

	@Override
	public boolean hasNext() {
		return next != store.size();
	}

	@Override
	public void remove() {
		if (previous == -1) throw new NoSuchElementException();
		V value = store.get(previous);
		if (value == null) throw new IllegalStateException();
		store.set(previous, null);
		previous = -1;
	}
	
	protected abstract E get(int index);

	private int subsequent(int i) {
		int length = store.size();
		for (; i < length; i++) {
			if (store.get(i) != null) break;
		}
		return i;
	}

}
