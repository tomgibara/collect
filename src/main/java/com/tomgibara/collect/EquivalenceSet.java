package com.tomgibara.collect;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Random;

import com.tomgibara.hashing.HashCode;
import com.tomgibara.hashing.HashSize;
import com.tomgibara.hashing.Hasher;

public class EquivalenceSet<E> extends AbstractSet<E> implements Mutability<EquivalenceSet<E>> {

	// statics
	
	private static final int HASH_COUNT = 3;
	private static final int RETRY_LIMIT = 3;
	
	// fields
	
	private final Random random = new Random();
	private int[] hashes = new int[HASH_COUNT];
	private EquRel<E> equ;
	private Store<E> store;
	private Hasher<E> hasher;
	
	// constructors
	
	EquivalenceSet(EquRel<E> equ, Store<E> store) {
		this.equ = equ;
		this.store = store;
		hasher = equ.getHasher().ints().sized(HashSize.fromInt(store.capacity()));
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
		return new EquivalenceSet<>(equ, store.mutableCopy());
	}
	
	@Override
	public EquivalenceSet<E> immutableCopy() {
		return new EquivalenceSet<>(equ, store.immutableCopy());
	}
	
	@Override
	public EquivalenceSet<E> mutableView() {
		if (!store.isMutable()) throw new IllegalStateException("Cannot take a mutable view of an immutable set");
		return new EquivalenceSet<>(equ, store);
	}
	
	@Override
	public EquivalenceSet<E> immutableView() {
		return new EquivalenceSet<>(equ, store.immutable());
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
		int[] hashes = hashes(e);
		// first check e not present
		int firstNull = -1;
		for (int i = 0; i < HASH_COUNT; i++) {
			int h = hashes[i];
			E e2 = store.get(h);
			if (oldIndex == -1) {
				// on the first pass, the value may not already by present
				if (e2 == null) {
					if (firstNull != -1) firstNull = h;
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
		E e2 = store.get(i);
		store.set(h, e);
		if (retryCount == RETRY_LIMIT) {
			//TODO need to grow table
		}

		insert(e2, h, retryCount++);
		return true;
	}
	
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