package com.tomgibara.collect;

import com.tomgibara.hashing.Hasher;
import com.tomgibara.storage.Storage;

public final class Tokens {

	final String[] strings;
	final Hasher<String> hasher;
	
	Tokens(String[] tokens, Hasher<String> hasher) {
		this.strings = tokens;
		this.hasher = hasher;
	}
	
	public TokenSet newSet() {
		return new TokenSet(this);
	}
	
	public <V> Maps<V> withStorage(Storage<V> storage) {
		if (storage == null) throw new IllegalArgumentException("null storage");
		return new Maps<>(storage);
	}
	
	public <V> Maps<V> withGenericStorage() {
		return new Maps<>(Storage.generic());
	}
	
	public <V> Maps<V> withTypedStorage(Class<V> type) {
		if (type == null) throw new IllegalArgumentException("null type");
		return new Maps<>(Storage.typed(type));
	}
	
	public final class Maps<V> {

		private final Storage<V> storage;

		Maps(Storage<V> storage) {
			this.storage = storage;
		}

		public TokenMap<V> newMap() {
			return new TokenMap<>(strings, hasher, storage.newStore(strings.length));
		}

	}
	
}
