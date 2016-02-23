package com.tomgibara.collect;

import java.util.Iterator;
import java.util.function.Consumer;

class ImmutableIterator<E> implements Iterator<E> {

	private final Iterator<E> i;

	ImmutableIterator(Iterator<E> i) {
		this.i = i;
	}

	@Override
	public boolean hasNext() {
		return i.hasNext();
	}

	@Override
	public E next() {
		return i.next();
	}

	@Override
	public void forEachRemaining(Consumer<? super E> action) {
		i.forEachRemaining(action);
	}

}
