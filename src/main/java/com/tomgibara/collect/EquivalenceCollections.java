package com.tomgibara.collect;

import java.util.Collection;
import java.util.Random;

import com.tomgibara.storage.Storage;

public class EquivalenceCollections<E> {

	private final Equivalence<E> equ;
	
	EquivalenceCollections(Equivalence<E> equ) {
		this.equ = equ;
	}

	public Sets setsWithGenericStorage() {
		return new Sets(Storage.generic());
	}
	
	public Sets setsWithTypedStorage(Class<E> type) {
		return new Sets(Storage.typed(type));
	}
	
	public Sets setsWithStorage(Storage<E> storage) {
		if (storage == null) throw new IllegalArgumentException("null storage");
		return new Sets(storage);
	}
	
	public final class Sets {

		private static final int DEFAULT_CAPACITY = 16;
		
		private final Storage<E> storage;
		
		Sets(Storage<E> storage) {
			this.storage = storage;
		}
		
		public EquivalenceSet<E> newSet() {
			return new EquivalenceSet<E>(new Cuckoo<>(new Random(0L), equ), storage, DEFAULT_CAPACITY);
		}
		
		public EquivalenceSet<E> newSet(Collection<? extends E> es) {
			if (es == null) throw new IllegalArgumentException("null es");
			int capacity;
			int size = es.size();
			if (size < 2) {
				capacity = size;
			} else if (size < 10) {
				capacity = size + 2;
			} else {
				capacity = Math.round(es.size() * 1.2f);
			}
			EquivalenceSet<E> set = new EquivalenceSet<E>(new Cuckoo<>(new Random(0L), equ), storage, capacity);
			set.addAll(es);
			return set;
		}

		public <V> Maps<V> mappedToGenericStorage() {
			return new Maps<>(storage, Storage.generic(), null);
		}

		public <V> Maps<V> mappedToGenericStorage(V initialValue) {
			return new Maps<>(storage, Storage.generic(initialValue), initialValue);
		}

		public <V> Maps<V> mappedToTypedStorage(Class<V> type) {
			return new Maps<>(storage, Storage.typed(type), null);
		}

		public <V> Maps<V> mappedToTypedStorage(Class<V> type, V initialValue) {
			return new Maps<>(storage, Storage.typed(type, initialValue), initialValue);
		}

		public <V> Maps<V> mappedToStorage(Storage<V> valueStorage) {
			if (valueStorage == null) throw new IllegalArgumentException("null valueStorage");
			return new Maps<>(storage, valueStorage, null);
		}

	}
	
	public final class Maps<V> {

		private static final int DEFAULT_CAPACITY = 16;

		private final Storage<E> keyStorage;
		private final Storage<V> valueStorage;
		private final V removalValue;

		Maps(Storage<E> keyStorage, Storage<V> valueStorage, V removalValue) {
			this.keyStorage = keyStorage;
			this.valueStorage = valueStorage;
			this.removalValue = removalValue;
		}

		public EquivalenceMap<E, V> newMap() {
			return new EquivalenceMap<E, V>(new Cuckoo<>(new Random(0L), equ), keyStorage, valueStorage, Equivalence.equality(), removalValue, DEFAULT_CAPACITY);
		}

		public EquivalenceMap<E, V> newMapWithValueEquivalence(Equivalence<V> valueEqu) {
			if (valueEqu == null) throw new IllegalArgumentException("null valueEqu");
			return new EquivalenceMap<E, V>(new Cuckoo<>(new Random(0L), equ), keyStorage, valueStorage, valueEqu, removalValue, DEFAULT_CAPACITY);
		}

	}

}