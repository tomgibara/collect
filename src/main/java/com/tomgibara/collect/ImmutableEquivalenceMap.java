package com.tomgibara.collect;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

class ImmutableEquivalenceMap<K, V> implements EquivalenceMap<K, V> {

	private final EquivalenceMap<K, V> map;
	
	ImmutableEquivalenceMap(EquivalenceMap<K, V> map) {
		this.map = map;
	}

	// equivalence
	
	public Equivalence<K> getKeyEquivalence() {
		return map.getKeyEquivalence();
	}

	public Equivalence<V> getValueEquivalence() {
		return map.getValueEquivalence();
	}

	// mutability
	
	public boolean isMutable() {
		return map.isMutable();
	}

	public EquivalenceMap<K, V> mutableCopy() {
		return map.mutableCopy();
	}

	public EquivalenceMap<K, V> immutableCopy() {
		return map.immutableCopy();
	}

	public EquivalenceMap<K, V> immutableView() {
		return new ImmutableEquivalenceMap<>(map);
	}

	// map
	
	public EquivalenceSet<K> keySet() {
		return new ImmutableEquivalenceSet<K>(map.keySet());
	}

	public int size() {
		return map.size();
	}

	public boolean isEmpty() {
		return map.isEmpty();
	}

	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	public V get(Object key) {
		return map.get(key);
	}

	public V put(K key, V value) {
		throw new IllegalStateException("immutable");
	}

	public V remove(Object key) {
		throw new IllegalStateException("immutable");
	}

	public void putAll(Map<? extends K, ? extends V> m) {
		throw new IllegalStateException("immutable");
	}

	public void clear() {
		throw new IllegalStateException("immutable");
	}

	public Collection<V> values() {
		return Collections.unmodifiableCollection(map.values());
	}

	public Set<Map.Entry<K, V>> entrySet() {
		return Collections.unmodifiableSet(map.entrySet());
	}

	public V getOrDefault(Object key, V defaultValue) {
		return map.getOrDefault(key, defaultValue);
	}

	public void forEach(BiConsumer<? super K, ? super V> action) {
		map.forEach(action);
	}

	public void replaceAll( BiFunction<? super K, ? super V, ? extends V> function) {
		throw new IllegalStateException("immutable");
	}

	public V putIfAbsent(K key, V value) {
		throw new IllegalStateException("immutable");
	}

	public boolean remove(Object key, Object value) {
		throw new IllegalStateException("immutable");
	}

	public boolean replace(K key, V oldValue, V newValue) {
		throw new IllegalStateException("immutable");
	}

	public V replace(K key, V value) {
		throw new IllegalStateException("immutable");
	}

	public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
		throw new IllegalStateException("immutable");
	}

	public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		throw new IllegalStateException("immutable");
	}

	public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
		throw new IllegalStateException("immutable");
	}

	public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
		throw new IllegalStateException("immutable");
	}

	// object methods

	public boolean equals(Object o) {
		return map.equals(o);
	}

	public int hashCode() {
		return map.hashCode();
	}

	@Override
	public String toString() {
		return map.toString();
	}

}
