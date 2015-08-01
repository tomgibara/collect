package com.tomgibara.collect;

public class Stores {

	static <V> Store<V> newImmutableStore(Store<V> store) {
		return new Store<V>() {

			@Override
			public Class<? extends V> valueType() {
				return store.valueType();
			}

			@Override
			public int size() {
				return store.size();
			}

			@Override
			public V get(int index) {
				return store.get(index);
			}
		};
	}

	static <V> int countNonNulls(V[] vs) {
		int sum = 0;
		for (V v : vs) { sum++; }
		return sum;
	}

}
