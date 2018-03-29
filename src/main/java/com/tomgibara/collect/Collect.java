package com.tomgibara.collect;

import java.util.Collection;
import java.util.Random;

import com.tomgibara.storage.Storage;
import com.tomgibara.storage.StoreType;

/**
 * Provides static method for creating equivalence collections from equivalences.
 * Serves as the entrypoint for the API.
 *
 * @author Tom Gibara
 */

public final class Collect {

	/**
	 * Creates sets backed by generic storage. This is comparable to the common
	 * implementation of Java collection classes using <code>Object</code>
	 * arrays.
	 *
	 * @return sets backed by generic storage
	 */

	public static <E> Sets<E> sets() {
		return new Sets<>(StoreType.<E>generic().storage());
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

	public static <E> Sets<E> setsOf(Class<E> type) {
		return new Sets<>(StoreType.of(type).storage());
	}

	/**
	 * Creates sets backed by the specified storage.
	 *
	 * @param storage
	 *            the storage that should back set instances
	 * @return sets backed by the specified storage
	 */

	public static <E> Sets<E> setsWithStorage(Storage<E> storage) {
		if (storage == null) throw new IllegalArgumentException("null storage");
		return new Sets<>(storage);
	}

	/**
	 * <p>
	 * Creates new sets. The sets produced by this object do not support the
	 * addition of <code>null</code> elements.
	 *
	 * <p>
	 * Method are also provided to create maps in which key equivalence and key
	 * storage are induced by these sets.
	 *
	 * @param <E>
	 *            the type of objects in the set
	 */

	public static final class Sets<E> {

		private static final int DEFAULT_CAPACITY = 16;

		private final Storage<E> storage;
		private final Equivalence<E> equivalence;
		private Cuckoo<E> trivialCuckoo = null;
		private EquivalenceSet<E> emptySet = null;

		Sets(Storage<E> storage) {
			this.storage = storage;
			this.equivalence = Equivalence.equality();
		}

		private Sets(Sets<E> sets, Equivalence<E> equivalence) {
			this.storage = sets.storage;
			this.equivalence = equivalence;
		}

		/**
		 * Creates sets under the equality equivalence. The storage backing the
		 * sets is unchanged.
		 *
		 * @return sets based on Java object equality
		 * @see Equivalence#equality()
		 */

		public Sets<E> underEquality() {
			return under(Equivalence.equality());
		}

		/**
		 * Creates sets under the identity equivalence. The storage backing the
		 * sets is unchanged.
		 *
		 * @return sets based on Java object identity
		 * @see Equivalence#identity()
		 */

		public Sets<E> underIdentity() {
			return under(Equivalence.identity());
		}

		/**
		 * Creates sets under the supplied equivalence. The equivalence
		 * determines equality for the purpose of set operations.
		 *
		 * @param equivalence
		 *            an equivalence
		 * @return sets based the equivalence
		 */

		public Sets<E> underEquivalence(Equivalence<E> equivalence) {
			if (equivalence == null) throw new IllegalArgumentException("null equivalence");
			return under(equivalence);
		}

		/**
		 * Creates a new mutable empty set.
		 *
		 * @return an empty set
		 */
		public EquivalenceSet<E> newSet() {
			return new CuckooEquivalenceSet<>(new Cuckoo<>(new Random(0L), equivalence), storage, DEFAULT_CAPACITY);
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
			EquivalenceSet<E> set = new CuckooEquivalenceSet<>(new Cuckoo<>(new Random(0L), equivalence), storage, capacity);
			set.addAll(es);
			return set;
		}

		public EquivalenceSet<E> singletonSet(E el) {
			if (el == null) throw new IllegalArgumentException("null el");
			EquivalenceSet<E> set = new CuckooEquivalenceSet<>(trivialCuckoo(), storage, 1);
			set.add(el);
			return set.immutable();
		}

		public EquivalenceSet<E> emptySet() {
			return emptySet == null ? emptySet = new CuckooEquivalenceSet<>(trivialCuckoo(), storage, 0).immutable() : emptySet;
		}

		/**
		 * Creates maps with values backed by generic storage.
		 *
		 * @return maps backed by generic storage
		 */

		public <V> Maps<E,V> mapped() {
			return new Maps<>(this, StoreType.<V>generic().storage());
		}

		/**
		 * Creates maps with values backed by typed storage.
		 *
		 * @param type
		 *            the type of value to be stored in the maps
		 * @return maps backed by typed storage
		 */

		public <V> Maps<E,V> mappedTo(Class<V> type) {
			return new Maps<>(this, StoreType.of(type).storage());
		}

		/**
		 * Creates maps with values backed the specified storage.
		 *
		 * @param storage
		 *            the storage that should back map values
		 * @return maps backed by the specified storage
		 */

		public <V> Maps<E,V> mappedWithStorage(Storage<V> storage) {
			if (storage == null) throw new IllegalArgumentException("null storage");
			return new Maps<>(this, storage);
		}

		private Cuckoo<E> trivialCuckoo() {
			return trivialCuckoo == null ? trivialCuckoo = new Cuckoo<>(FauxRandom.INSTANCE, equivalence) : trivialCuckoo;
		}

		private Sets<E> under(Equivalence<E> equivalence) {
			return this.equivalence == equivalence ? this : new Sets<>(this, equivalence);
		}
	}

	/**
	 * Creates new maps. The maps do not support <code>null</code> keys.
	 */

	public static final class Maps<K,V> {

		private static final int DEFAULT_CAPACITY = 16;

		final Sets<K> sets;
		final Storage<V> storage;
		final Equivalence<V> equivalence;
		private EquivalenceMap<K,V> empty = null;

		Maps(Sets<K> sets, Storage<V> storage) {
			this.sets = sets;
			this.storage = storage;
			this.equivalence = Equivalence.equality();
		}

		private Maps(Maps<K,V> that, Equivalence<V> equivalence) {
			this.sets = that.sets;
			this.storage = that.storage;
			this.equivalence = equivalence;
		}

		public EquivalenceMap<K, V> newMap() {
			return new CuckooEquivalenceMap<>(new Cuckoo<>(new Random(0L), sets.equivalence), sets.storage, storage, equivalence, DEFAULT_CAPACITY);
		}

		public EquivalenceMap<K, V> emptyMap() {
			return empty == null ? empty = new CuckooEquivalenceMap<>(sets.trivialCuckoo(), sets.storage, storage, equivalence, 0).immutable() : empty;
		}

		public Maps<K,V> underEquality() {
			return under(Equivalence.equality());
		}

		public Maps<K,V> underIdentity() {
			return under(Equivalence.identity());
		}

		public Maps<K,V> underEquivalence(Equivalence<V> equivalence) {
			if (equivalence == null) throw new IllegalArgumentException("null equivalence");
			return under(equivalence);
		}

		private Maps<K,V> under(Equivalence<V> equivalence) {
			return equivalence == this.equivalence ? this : new Maps<>(this, equivalence);
		}

	}

	private Collect() {}

}
