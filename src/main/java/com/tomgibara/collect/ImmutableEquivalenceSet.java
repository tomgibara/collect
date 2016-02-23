package com.tomgibara.collect;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

class ImmutableEquivalenceSet<E> implements EquivalenceSet<E> {

	// fields
	
	private final EquivalenceSet<E> set;

	// constructors
	
	ImmutableEquivalenceSet(EquivalenceSet<E> set) {
		this.set = set;
	}
	
	// set methods

	// collection methods
	
	@Override
	public int size() {
		return set.size();
	}

	@Override
	public boolean isEmpty() {
		return set.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return set.contains(o);
	}

	@Override
	public Object[] toArray() {
		return set.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return set.toArray(a);
	}

	@Override
	public boolean add(E e) {
		throw new IllegalStateException("immutable");
	}

	@Override
	public boolean remove(Object o) {
		throw new IllegalStateException("immutable");
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return set.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new IllegalStateException("immutable");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new IllegalStateException("immutable");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new IllegalStateException("immutable");
	}

	@Override
	public void clear() {
		throw new IllegalStateException("immutable");
	}

	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		return set.removeIf(filter);
	}
	
	@Override
	public Stream<E> parallelStream() {
		return set.parallelStream();
	}
	
	@Override
	public Stream<E> stream() {
		return set.stream();
	}
	
	@Override
	public Spliterator<E> spliterator() {
		return set.spliterator();
	}
	
	// iterable methods
	
	@Override
	public Iterator<E> iterator() {
		return new ImmutableIterator<E>(set.iterator());
	}

	@Override
	public void forEach(Consumer<? super E> action) {
		set.forEach(action);
	}
	
	
	
	// mutability methods

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public EquivalenceSet<E> mutableCopy() {
		return set.mutableCopy();
	}

	@Override
	public EquivalenceSet<E> immutableCopy() {
		return set.immutableCopy();
	}

	@Override
	public EquivalenceSet<E> immutableView() {
		return new ImmutableEquivalenceSet<>(set);
	}

	// equivalence methods
	
	@Override
	public Equivalence<E> getEquivalence() {
		return set.getEquivalence();
	}

	// object methods
	
	@Override
	public boolean equals(Object obj) {
		return set.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return set.hashCode();
	}
	
	@Override
	public String toString() {
		return set.toString();
	}
}
