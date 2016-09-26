package com.tomgibara.collect;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.tomgibara.hashing.Hasher;
import com.tomgibara.storage.Storage;
import com.tomgibara.storage.Store;

final class CuckooEquivalenceMap<K, V> extends AbstractMap<K, V> implements EquivalenceMap<K, V> {

	private final Cuckoo<K> cuckoo;
	private final Storage<K> keyStorage;
	private final Storage<V> valueStorage;
	private final Equivalence<V> equ;
	private Hasher<K> hasher = null;
	private Store<K> keyStore;
	private Store<V> valueStore;
	
	private Entries entries = null;
	private Keys keys = null;
	private Values values = null;

	CuckooEquivalenceMap(Cuckoo<K> cuckoo, Storage<K> keyStorage, Storage<V> valueStorage, Equivalence<V> equ, int initialCapacity) {
		this.cuckoo = cuckoo;
		this.keyStorage = keyStorage;
		this.valueStorage = valueStorage;
		this.equ = equ;
		hasher = cuckoo.updateHasher(hasher, initialCapacity);
		keyStore = keyStorage.newStore(initialCapacity);
		valueStore = valueStorage.newStore(initialCapacity);
	}
	
	private CuckooEquivalenceMap(CuckooEquivalenceMap<K, V> that, Store<K> keyStore, Store<V> valueStore) {
		this.cuckoo = that.cuckoo;
		this.keyStorage = that.keyStorage;
		this.valueStorage = that.valueStorage;
		this.equ = that.equ;
		this.hasher = cuckoo.updateHasher(that.hasher, keyStore.size());
		this.keyStore = keyStore;
		this.valueStore = valueStore;
	}

	// equivalence methods
	
	@Override
	public Equivalence<K> getKeyEquivalence() {
		return cuckoo.equ;
	}
	
	@Override
	public Equivalence<V> getValueEquivalence() {
		return equ;
	}
	
	// map methods
	
	@Override
	public boolean containsKey(Object key) {
		return access().indexOf(key) != -1;
	}
	
	@Override
	public void clear() {
		checkMutable();
		keyStore.clear();
		valueStore.clear();
	}
	
	@Override
	public int size() {
		return keyStore.count();
	}
	
	@Override
	public boolean containsValue(Object value) {
		return indexOfValue(value) != -1;
	}
	
	@Override
	public boolean isEmpty() {
		return size() == 0;
	}
	
	@Override
	public V get(Object key) {
		int i = access().indexOf(key);
		return i == -1 ? null : valueStore.get(i);
	}
	
	@Override
	public V getOrDefault(Object key, V defaultValue) {
		int i = access().indexOf(key);
		if (i == -1) return defaultValue;
		V value = valueStore.get(i);
		return value == null ? defaultValue : value;
	}
	
	@Override
	public V remove(Object key) {
		checkMutable();
		int i = access().indexOf(key);
		if (i == -1) return null;
		V value = valueStore.get(i);
		keyStore.set(i, null);
		valueStore.set(i, null);
		return value;
	}
	
	@Override
	public boolean remove(Object key, Object value) {
		checkMutable();
		if (value == null) return false;
		int i = access().indexOf(key);
		if (i == -1) return false;
		V previous = valueStore.get(i);
		if (!previous.equals(value)) return false;
		keyStore.set(i, null);
		valueStore.set(i, null);
		return true;
	}

	@Override
	public V put(K key, V value) {
		return putImpl(key, value, true);
	}

	@Override
	public V putIfAbsent(K key, V value) {
		return putImpl(key, value, false);
	}

	@Override
	public V replace(K key, V value) {
		if (value == null) throw new IllegalArgumentException("null value");
		checkMutable();
		int i = access().checkedIndexOf(key);
		if (i == -1) return null;
		V previous = valueStore.get(i);
		valueStore.set(i, value);
		return previous;
	}

	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		if (newValue == null) throw new IllegalArgumentException("null value");
		checkMutable();
		if (oldValue == null) return false;
		int i = access().checkedIndexOf(key);
		if (i == -1 || !equ.isEquivalent(valueStore.get(i), oldValue)) return false;
		valueStore.set(i, newValue);
		return true;
	}
	
	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return entries == null ? entries = new Entries() : entries;
	}
	
	@Override
	public EquivalenceSet<K> keySet() {
		return keys == null ? keys = new Keys() : keys;
	}
	
	@Override
	public Collection<V> values() {
		return values == null ? values = new Values() : values;
	}
	
	// mutability methods

	@Override
	public boolean isMutable() {
		return keyStore.isMutable();
	}
	
	@Override
	public EquivalenceMap<K, V> mutable() {
		return isMutable() ? this : mutableCopy();
	}

	@Override
	public EquivalenceMap<K, V> immutable() {
		return isMutable() ? immutableView() : this;
	}
	
	@Override
	public EquivalenceMap<K, V> mutableCopy() {
		return new CuckooEquivalenceMap<>(this, keyStore.mutableCopy(), valueStore.mutableCopy());
	}
	
	@Override
	public EquivalenceMap<K, V> immutableCopy() {
		return new CuckooEquivalenceMap<>(this, keyStore.immutableCopy(), valueStore.immutableCopy());
	}
	
	@Override
	public EquivalenceMap<K, V> immutableView() {
		return new ImmutableEquivalenceMap<>(this);
	}

	// private helper methods

	private Cuckoo<K>.Access<V> resize() {
		Store<K> oldKeyStore = keyStore;
		Store<V> oldValueStore = valueStore;
		int oldCapacity = oldKeyStore.size();
		int newCapacity = 2 * oldCapacity;
		keyStore = keyStorage.newStore(newCapacity);
		valueStore = valueStorage.newStore(newCapacity);
		hasher = cuckoo.updateHasher(hasher, newCapacity);
		Cuckoo<K>.Access<V> access = access();
		for (int j = 0; j < oldCapacity; j++) {
			K ko = oldKeyStore.get(j);
			if (ko != null) {
				V vo = oldValueStore.get(j);
				access.put(ko, vo, false);
			}
		}
		return access;
	}
	
	private Cuckoo<K>.Access<V> access() {
		return cuckoo.access(keyStore, hasher, this::resize, valueStore);
	}
	
	private V putImpl(K key, V value, boolean overwrite) {
		if (value == null) throw new IllegalArgumentException("null value");
		checkMutable();
		return access().put(key, value, overwrite);
	}

	private int indexOfValue(Object value) {
		if (value == null) return -1;
		int capacity = valueStore.size();
		for (int i = 0; i < capacity; i++) {
			V candidate = valueStore.get(i);
			if (candidate != null) try {
				if (equ.isEquivalent(candidate, (V) value)) return i;
			} catch (IllegalArgumentException|ClassCastException e) {
				/* swallowed */
			}
		}
		return -1;
	}

	private void checkMutable() {
		if (!valueStore.isMutable()) throw new IllegalStateException("immutable");
	}
	
	// inner classes
	
	private final class Keys extends AbstractSet<K> implements EquivalenceSet<K> {
		
		// equivalence methods
		
		@Override
		public Equivalence<K> getEquivalence() {
			return cuckoo.equ;
		}
		
		@Override
		public K get(K e) {
			if (e == null) throw new IllegalArgumentException("null e");
			int i = access().checkedIndexOf(e);
			return i == -1 ? null : keyStore.get(i);
		}
		
		// set methods
		
		@Override
		public int size() {
			return keyStore.count();
		}
		
		@Override
		public boolean isEmpty() {
			return keyStore.count() == 0;
		}
		
		@Override
		public void clear() {
			CuckooEquivalenceMap.this.clear();
		}
		
		@Override
		public boolean contains(Object o) {
			return containsKey(o);
		}
		
		@Override
		public boolean remove(Object o) {
			checkMutable();
			int i = access().indexOf(o);
			if (i == -1) return false;
			keyStore.set(i, null);
			valueStore.set(i, null);
			return true;
		}
		
		@Override
		public Iterator<K> iterator() {
			return keyStore.iterator();
		}

		// mutability methods
		
		@Override
		public boolean isMutable() {
			return keyStore.isMutable();
		}
		
		@Override
		public EquivalenceSet<K> mutableCopy() {
			return new CuckooEquivalenceSet<K>(cuckoo, keyStorage, keyStore.mutableCopy());
		}

		@Override
		public EquivalenceSet<K> immutableCopy() {
			return new CuckooEquivalenceSet<K>(cuckoo, keyStorage, keyStore.immutableCopy());
		}

		@Override
		public EquivalenceSet<K> immutableView() {
			return CuckooEquivalenceMap.this.immutableView().keySet();
		}
	}
	
	private final class Values extends AbstractCollection<V> {

		@Override
		public int size() {
			return valueStore.count();
		}
		
		@Override
		public boolean isEmpty() {
			return valueStore.count() == 0;
		}
		
		@Override
		public void clear() {
			CuckooEquivalenceMap.this.clear();
		}
		
		@Override
		public boolean contains(Object o) {
			return containsValue(o);
		}

		@Override
		public boolean remove(Object o) {
			checkMutable();
			int i = indexOfValue(o);
			if (i == -1) return false;
			keyStore.set(i, null);
			valueStore.set(i, null);
			return true;
		}

		@Override
		public Iterator<V> iterator() {
			return valueStore.iterator();
		}

	}
	
	private final class Entries extends AbstractSet<Entry<K, V>> {

		@Override
		public int size() {
			return keyStore.count();
		}
		
		@Override
		public boolean isEmpty() {
			return keyStore.count() == 0;
		}
		
		@Override
		public void clear() {
			CuckooEquivalenceMap.this.clear();
		}

		@Override
		public boolean contains(Object o) {
			return containsImpl(o, true);
		}
		
		@Override
		public boolean remove(Object o) {
			checkMutable();
			return containsImpl(o, true);
		}
		
		private boolean containsImpl(Object o, boolean remove) {
			if (!(o instanceof Entry)) return false;
			Entry<?,?> e = (Entry<?,?>) o;
			Object k = e.getKey();
			Object v = e.getValue();
			if (k == null || v == null) return false;
			int i = access().indexOf(k);
			if (i == -1) return false;
			//TODO again... there may be no better way
			boolean contained;
			try {
				contained = equ.isEquivalent(valueStore.get(i), (V) v);
			} catch (IllegalArgumentException|ClassCastException ex) {
				return false;
			}
			if (!contained) return false;
			if (remove) {
				keyStore.set(i, null);
				valueStore.set(i, null);
			}
			return true;
		}
		
		@Override
		public Iterator<Entry<K, V>> iterator() {
			return keyStore.transformedIterator((i,k) -> new CuckooEntry(i,k));
		}
	}
	
	final private class CuckooEntry extends AbstractMapEntry<K, V> {
		
		private final K key;
		private final int index;
		
		CuckooEntry(int index, K key) {
			this.index = index;
			this.key = key;
		}
		
		@Override
		public K getKey() {
			return key;
		}
		
		@Override
		public V getValue() {
			return valueStore.get(index);
		}
		
		@Override
		public V setValue(V value) {
			if (value == null) throw new IllegalArgumentException("null value");
			return valueStore.set(index, value);
		}

	}
}
