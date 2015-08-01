package com.tomgibara.collect;

final class ImmutableArrayStore<V> implements Store<V> {

	private final V[] values;
	private final int size;

	ImmutableArrayStore(ArrayStore<V> store) {
		values = store.values.clone();
		size = store.size;
	}

	@Override
	public Class<? extends V> valueType() {
		return (Class<? extends V>) values.getClass().getComponentType();
	}

	@Override
	public int size() { return size; }

	@Override
	public V get(int index) { return values[index]; }

	@Override
	public Store<V> mutableCopy() { return new ArrayStore<V>(values.clone(), size); }
	
}