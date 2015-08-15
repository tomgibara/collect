package com.tomgibara.collect;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.tomgibara.hashing.Hasher;
import com.tomgibara.storage.Mutability;
import com.tomgibara.storage.Storage;
import com.tomgibara.storage.Store;

public final class EquivalenceMap<K, V> extends AbstractMap<K, V> implements Mutability<EquivalenceMap<K, V>> {

	private final Cuckoo<K> cuckoo;
	private final Storage<K> keyStorage;
	private final Storage<V> valueStorage;
	private final EquRel<V> equ;
	private Hasher<K> hasher = null;
	private Store<K> keyStore;
	private Store<V> valueStore;
	
	private Entries entries = null;
	private Keys keys = null;
	private Values values = null;

	EquivalenceMap(Cuckoo<K> cuckoo, Storage<K> keyStorage, Storage<V> valueStorage, EquRel<V> equ, int initialCapacity) {
		this.cuckoo = cuckoo;
		this.keyStorage = keyStorage;
		this.valueStorage = valueStorage;
		this.equ = equ;
		hasher = cuckoo.updateHasher(hasher, initialCapacity);
		keyStore = keyStorage.newStore(initialCapacity);
		valueStore = valueStorage.newStore(initialCapacity);
	}
	
	public EquivalenceMap(EquivalenceMap<K, V> that, Store<K> keyStore, Store<V> valueStore) {
		this.cuckoo = that.cuckoo;
		this.keyStorage = that.keyStorage;
		this.valueStorage = that.valueStorage;
		this.equ = that.equ;
		this.hasher = cuckoo.updateHasher(that.hasher, keyStore.capacity());
		this.keyStore = keyStore;
		this.valueStore = valueStore;
	}

	@Override
	public boolean containsKey(Object key) {
		return cuckoo.access(keyStore, hasher).indexOf(key) != -1;
	}
	
	@Override
	public void clear() {
		checkMutable();
		keyStore.clear();
		valueStore.clear();
	}
	
	@Override
	public int size() {
		return keyStore.size();
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
		int i = cuckoo.access(keyStore, hasher).indexOf(key);
		return i == -1 ? null : valueStore.get(i);
	}
	
	@Override
	public V getOrDefault(Object key, V defaultValue) {
		int i = cuckoo.access(keyStore, hasher).indexOf(key);
		if (i == -1) return defaultValue;
		V value = valueStore.get(i);
		return value == null ? defaultValue : value;
	}
	
	@Override
	public V remove(Object key) {
		checkMutable();
		int i = cuckoo.access(keyStore, hasher).indexOf(key);
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
		int i = cuckoo.access(keyStore, hasher).indexOf(key);
		if (i == -1) return false;
		V previous = valueStore.get(i);
		if (!previous.equals(value)) return false;
		keyStore.set(i, null);
		valueStore.set(i, null);
		return true;
	}

	@Override
	public V put(K key, V value) {
		return putImpl(key, value, false);
	}

	@Override
	public V putIfAbsent(K key, V value) {
		return putImpl(key, value, false);
	}

	@Override
	public V replace(K key, V value) {
		if (value == null) throw new IllegalArgumentException("null value");
		checkMutable();
		int i = cuckoo.access(keyStore, hasher).checkedIndexOf(key);
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
		int i = cuckoo.access(keyStore, hasher).checkedIndexOf(key);
		if (i == -1 || !equ.isEquivalent(valueStore.get(i), oldValue)) return false;
		valueStore.set(i, newValue);
		return true;
	}
	
	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return entries == null ? entries = new Entries() : entries;
	}
	
	@Override
	public Set<K> keySet() {
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
		return new EquivalenceMap<>(this, keyStore.mutableCopy(), valueStore.mutableCopy());
	}
	
	@Override
	public EquivalenceMap<K, V> immutableCopy() {
		return new EquivalenceMap<>(this, keyStore.immutableCopy(), valueStore.immutableCopy());
	}
	
	@Override
	public EquivalenceMap<K, V> immutableView() {
		return new EquivalenceMap<>(this, keyStore.immutable(), valueStore.immutable());
	}

	// private helper methods

	private V putImpl(K key, V value, boolean ifAbsent) {
		if (value == null) throw new IllegalArgumentException("null value");
		checkMutable();
		Cuckoo<K>.Access access = cuckoo.access(keyStore, hasher);
		K k = key;
		while (true) {
			Object result = access.add(k);
			if (result == Cuckoo.SUCCESS) {
				int index = access.indexOf(key);
				V previousValue = valueStore.set(index, value);
				return previousValue;
			}
			if (result == Cuckoo.FAILURE) {
				if (!ifAbsent) {
					int index = access.indexOf(key);
					valueStore.set(index, value);
				}
				return null;
			}

			k = (K) result;
			Store<K> oldKeyStore = keyStore;
			Store<V> oldValueStore = valueStore;
			int oldCapacity = oldKeyStore.capacity();
			int newCapacity = 2 * oldCapacity;
			keyStore = keyStorage.newStore(newCapacity);
			valueStore = valueStorage.newStore(newCapacity);
			hasher = cuckoo.updateHasher(hasher, newCapacity);
			access = cuckoo.access(keyStore, hasher);
			for (int j = 0; j < oldCapacity; j++) {
				K ko = oldKeyStore.get(j);
				if (ko != null) {
					access.add(ko);
					int index = access.indexOf(ko);
					V vo = oldValueStore.get(j);
					valueStore.set(index, vo);
				}
			}
		}
	}

	private int indexOfValue(Object value) {
		if (value == null) return -1;
		int capacity = valueStore.capacity();
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
	
	private final class Keys extends AbstractSet<K> {
		
		@Override
		public int size() {
			return keyStore.size();
		}
		
		@Override
		public boolean isEmpty() {
			return keyStore.size() == 0;
		}
		
		@Override
		public void clear() {
			EquivalenceMap.this.clear();
		}
		
		@Override
		public boolean contains(Object o) {
			return containsKey(o);
		}
		
		@Override
		public boolean remove(Object o) {
			checkMutable();
			int i = cuckoo.access(keyStore, hasher).indexOf(o);
			if (i == -1) return false;
			keyStore.set(i, null);
			valueStore.set(i, null);
			return true;
		}
		
		@Override
		public Iterator<K> iterator() {
			return new StoreIterator<K, K>(keyStore) {
				@Override
				K get(int index) {
					return keyStore.get(index);
				}
			};
		}

	}
	
	private final class Values extends AbstractCollection<V> {

		@Override
		public int size() {
			return valueStore.size();
		}
		
		@Override
		public boolean isEmpty() {
			return valueStore.size() == 0;
		}
		
		@Override
		public void clear() {
			EquivalenceMap.this.clear();
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
			return new StoreIterator<V, V>(valueStore) {
				@Override
				V get(int index) {
					return valueStore.get(index);
				}
			};
		}

	}
	
	private final class Entries extends AbstractSet<Entry<K, V>> {

		@Override
		public int size() {
			return keyStore.size();
		}
		
		@Override
		public boolean isEmpty() {
			return keyStore.size() == 0;
		}
		
		@Override
		public void clear() {
			EquivalenceMap.this.clear();
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
			int i = cuckoo.access(keyStore, hasher).indexOf(k);
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
			return new StoreIterator<K, Entry<K, V>>(keyStore) {
				@Override
				CuckooEntry get(int index) {
					return new CuckooEntry(index);
				}
			};
		}
	}
	
	final private class CuckooEntry extends AbstractMapEntry<K, V> {
		
		private final K key;
		private final int index;
		
		CuckooEntry(int index) {
			this.index = index;
			key = keyStore.get(index);
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
