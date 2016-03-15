package com.tomgibara.collect;

import java.util.Collection;
import java.util.Random;

import com.tomgibara.storage.Storage;
import com.tomgibara.storage.Stores;

/**
 * Creates collections in which keys are distinct up-to a specified equivalence.
 * This means that a set will only contain up to one object of any given
 * equivalence class and that a map will map all equivalent keys to the same
 * value.
 * 
 * @author Tom Gibara
 *
 * @param <E>
 *            the type objects under equivalence
 */

public class EquivalenceCollections<E> {

	private final Equivalence<E> equ;
	
	EquivalenceCollections(Equivalence<E> equ) {
		this.equ = equ;
	}

	/**
	 * Creates sets backed by generic storage. This is comparable to the common
	 * implementation of Java collection classes using <code>Object</code>
	 * arrays.
	 * 
	 * @return sets backed by generic storage
	 */

	public Sets setsWithGenericStorage() {
		return new Sets(Storage.generic());
	}
	
	/**
	 * Creates sets backed by typed storage. Specifying a primitive type will
	 * result in storage backed by arrays of primitives, yielding significant
	 * memory savings and improved type safety.
	 * 
	 * @param type
	 *            the type of value to be stored in the sets
	 * @return sets backed by typed storage
	 */

	public Sets setsWithTypedStorage(Class<E> type) {
		return new Sets(Storage.typed(type));
	}

	/**
	 * Creates sets backed by the specified storage.
	 * 
	 * @param storage
	 *            the storage that should back set instances
	 * @return sets backed by the specified storage
	 */

	public Sets setsWithStorage(Storage<E> storage) {
		if (storage == null) throw new IllegalArgumentException("null storage");
		return new Sets(storage);
	}

	/**
	 * <p>
	 * Creates new sets. The sets produced by this object do not support the
	 * addition of <code>null</code> elements.
	 * 
	 * <p>
	 * Method are also provided to create maps in which key equivalence and key
	 * storage are induced by these sets.
	 */

	public final class Sets {

		private static final int DEFAULT_CAPACITY = 16;
		
		private final Storage<E> storage;
		
		Sets(Storage<E> storage) {
			this.storage = storage;
		}
	
		/**
		 * Creates a new mutable empty set.
		 * 
		 * @return an empty set
		 */
		public EquivalenceSet<E> newSet() {
			return new CuckooEquivalenceSet<E>(new Cuckoo<>(new Random(0L), equ), storage, DEFAULT_CAPACITY);
		}
		
		/**
		 * Creates a new mutable set initially containing the supplied elements.
		 * 
		 * @param es the elements the set should contain
		 * @return a new set containing those elements
		 */

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
			EquivalenceSet<E> set = new CuckooEquivalenceSet<E>(new Cuckoo<>(new Random(0L), equ), storage, capacity);
			set.addAll(es);
			return set;
		}

		/**
		 * Creates maps with values backed by generic storage.
		 * 
		 * @return maps backed by generic storage
		 */

		public <V> Maps<V> mappedToGenericStorage() {
			return new Maps<>(storage, Storage.generic());
		}

		/**
		 * Creates maps with values backed by typed storage.
		 * 
		 * @param type
		 *            the type of value to be stored in the maps
		 * @return maps backed by typed storage
		 */

		public <V> Maps<V> mappedToTypedStorage(Class<V> type) {
			return new Maps<>(storage, Storage.typed(type, Stores.defaultNullValue(type).orElse(null)));
		}

		/**
		 * Creates maps with values backed the specified storage.
		 * 
		 * @param storage
		 *            the storage that should back map values
		 * @return maps backed by the specified storage
		 */

		public <V> Maps<V> mappedToStorage(Storage<V> valueStorage) {
			if (valueStorage == null) throw new IllegalArgumentException("null valueStorage");
			return new Maps<>(storage, valueStorage);
		}

	}
	
	/**
	 * Creates new maps. The maps do not support <code>null</code> keys or
	 * <code>null</code> values.
	 */

	public final class Maps<V> {

		private static final int DEFAULT_CAPACITY = 16;

		private final Storage<E> keyStorage;
		private final Storage<V> valueStorage;

		Maps(Storage<E> keyStorage, Storage<V> valueStorage) {
			this.keyStorage = keyStorage;
			this.valueStorage = valueStorage;
		}

		public EquivalenceMap<E, V> newMap() {
			return new CuckooEquivalenceMap<E, V>(new Cuckoo<>(new Random(0L), equ), keyStorage, valueStorage, Equivalence.equality(), DEFAULT_CAPACITY);
		}

		public EquivalenceMap<E, V> newMapWithValueEquivalence(Equivalence<V> valueEqu) {
			if (valueEqu == null) throw new IllegalArgumentException("null valueEqu");
			return new CuckooEquivalenceMap<E, V>(new Cuckoo<>(new Random(0L), equ), keyStorage, valueStorage, valueEqu, DEFAULT_CAPACITY);
		}

	}

}
