package com.tomgibara.collect;

import java.util.Map.Entry;
import java.util.Objects;

abstract class AbstractMapEntry<K, V> implements Entry<K, V> {

	@Override
	public V setValue(V value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean equals(Object object) {
		if (object == this) return true;
		if (!(object instanceof Entry<?,?>)) return false;
		Entry<?, ?> that = (Entry<?, ?>) object;
		return
				Objects.equals(this.getKey(), that.getKey()) &&
				Objects.equals(this.getValue(), that.getValue());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
	}

	@Override
	public String toString() {
		return getKey() + "=" + getValue();
	}
}
