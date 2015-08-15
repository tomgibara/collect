package com.tomgibara.collect;

import java.util.Collection;
import java.util.Random;

import com.tomgibara.storage.Storage;

public class Equivalence<E> {

	private final EquRel<E> equ;
	
	Equivalence(EquRel<E> equ) {
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
			return new EquivalenceSet<E>(new Cuckoo<>(new Random(), equ), storage, DEFAULT_CAPACITY);
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
			EquivalenceSet<E> set = new EquivalenceSet<E>(new Cuckoo<>(new Random(), equ), storage, capacity);
			set.addAll(es);
			return set;
		}

		public <V> Maps<V> mappedToGenericStorage() {
			return new Maps<>(storage, Storage.generic());
		}

		public <V> Maps<V> mappedToTypedStorage(Class<V> type) {
			return new Maps<>(storage, Storage.typed(type));
		}

		public <V> Maps<V> mappedToStorage(Storage<V> valueStorage) {
			if (valueStorage == null) throw new IllegalArgumentException("null valueStorage");
			return new Maps<>(storage, valueStorage);
		}

	}
	
	public final class Maps<V> {

		private static final int DEFAULT_CAPACITY = 16;

		private final Storage<E> keyStorage;
		private final Storage<V> valueStorage;

		Maps(Storage<E> keyStorage, Storage<V> valueStorage) {
			this.keyStorage = keyStorage;
			this.valueStorage = valueStorage;
		}

		public EquivalenceMap<E, V> newMap() {
			return new EquivalenceMap<E, V>(new Cuckoo<>(new Random(), equ), keyStorage, valueStorage, EquRel.equality(), DEFAULT_CAPACITY);
		}

		public EquivalenceMap<E, V> newMapWithValueEquivalence(EquRel<V> valueEqu) {
			if (valueEqu == null) throw new IllegalArgumentException("null valueEqu");
			return new EquivalenceMap<E, V>(new Cuckoo<>(new Random(), equ), keyStorage, valueStorage, valueEqu, DEFAULT_CAPACITY);
		}

	}

}
