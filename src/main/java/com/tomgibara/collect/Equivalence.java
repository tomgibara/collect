package com.tomgibara.collect;

import java.util.Collection;
import java.util.Random;

import com.tomgibara.storage.Storage;

public class Equivalence<E> {

	public static Equivalence<String>.Sets setsOfStrings(String... values) {
		PerfectStringStorage storage = new PerfectStringStorage(values);
		return new Equivalence<>(storage).setsWithStorage(storage);
	}
	
	// this kludgy :(
	private static <E> Storage<E> mapStorage(Storage<E> storage) {
		if (storage instanceof PerfectStringStorage) {
			return (Storage<E>)((PerfectStringStorage) storage).depopulate();
		}
		return storage;
	}
	
	private final EquRel<E> equ;
	
	Equivalence(EquRel<E> equ) {
		this.equ = equ;
	}

	public Sets setsWithGenericStorage() {
		return new Sets(Storage.generic(true));
	}
	
	public Sets setsWithTypedStorage(Class<E> type) {
		return new Sets(Storage.typed(type, true));
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

		public <V> Maps<V> mappedToGenericStorage(boolean allowNulls) {
			return new Maps<>(storage, Storage.generic(allowNulls));
		}

		public <V> Maps<V> mappedToTypedStorage(Class<V> type, boolean allowNulls) {
			return new Maps<>(storage, Storage.typed(type, allowNulls));
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
			this.keyStorage = mapStorage(keyStorage);
			this.valueStorage = valueStorage;
		}

		public EquivalenceMap<E, V> newMap() {
			return new EquivalenceMap<E, V>(new Cuckoo<>(new Random(0L), equ), keyStorage, valueStorage, EquRel.equality(), DEFAULT_CAPACITY);
		}

		public EquivalenceMap<E, V> newMapWithValueEquivalence(EquRel<V> valueEqu) {
			if (valueEqu == null) throw new IllegalArgumentException("null valueEqu");
			return new EquivalenceMap<E, V>(new Cuckoo<>(new Random(0L), equ), keyStorage, valueStorage, valueEqu, DEFAULT_CAPACITY);
		}

		//TODO implement
		private Cuckoo<E> cuckoo(EquRel<E> keyEqu) {
			if (keyEqu instanceof PerfectStringStorage) {
				
			}
			throw new UnsupportedOperationException();
		}
	}

}
