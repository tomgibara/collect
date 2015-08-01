package com.tomgibara.collect;

import java.lang.reflect.Array;

public interface Store<V> {

	Class<? extends V> valueType();
	
	int size();
	
	V get(int index);
	
	default V set(int index, V value) { throw new UnsupportedOperationException(); }
	
	default void clear() { throw new UnsupportedOperationException(); }
	
	//TODO use to check calls early?
	default boolean isMutable() { return false; }
	
	default Store<V> immutable() { return isMutable() ? Stores.newImmutableStore(this) : this; }
	
	default Store<V> mutableCopy() {
		V[] vs = (V[]) Array.newInstance(valueType(), 64);
		for (int i = 0; i < 64; i++) {
			vs[i] = get(i);
		}
		return new ArrayStore<V>(vs, size());
	}

}
