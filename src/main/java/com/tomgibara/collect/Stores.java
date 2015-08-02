package com.tomgibara.collect;

import java.lang.reflect.Array;

public class Stores {

	static <V> int countNonNulls(V[] vs) {
		int sum = 0;
		for (V v : vs) { sum++; }
		return sum;
	}
	
	static <V> V[] toArray(Store<V> store) {
		int capacity = store.capacity();
		V[] vs = (V[]) Array.newInstance(store.valueType(), capacity);
		for (int i = 0; i < capacity; i++) {
			vs[i] = store.get(i);
		}
		return vs;
	}

}
