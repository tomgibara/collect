package com.tomgibara.collect;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.tomgibara.storage.Store;

abstract class StoreIterator<V, E> implements Iterator<E> {

	final Store<V> store;
	private int previous = -1;
	private int next;

	StoreIterator(Store<V> store) {
		this.store = store;
		next = subsequent(previous + 1);
	}
	
	@Override
	public E next() {
		if (next == store.capacity()) throw new NoSuchElementException();
		E x = get(next);
		int tmp = next;
		next = subsequent(previous + 1);
		previous = tmp;
		return x;
	}

	@Override
	public boolean hasNext() {
		return next != store.capacity();
	}

	@Override
	public void remove() {
		if (previous == -1) throw new NoSuchElementException();
		V value = store.get(previous);
		if (value == null) throw new IllegalStateException();
		store.set(previous, null);
	}
	
	abstract E get(int index);

	private int subsequent(int i) {
		int length = store.capacity();
		for (; i < length; i++) {
			if (store.get(i) != null) return i;
		}
		return length;
	}

}
