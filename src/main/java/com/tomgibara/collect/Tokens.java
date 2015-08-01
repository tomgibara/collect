package com.tomgibara.collect;

import java.util.Map;
import java.util.Set;

import com.tomgibara.hashing.Hasher;
import com.tomgibara.hashing.Hashing;

public final class Tokens {

	final String[] strings;
	final Hasher<String> hasher;
	
	public static Tokens of(String... strings) {
		if (strings == null) throw new IllegalArgumentException("null strings");
		strings = strings.clone();
		return new Tokens(strings, Hashing.minimalPerfect(strings));
	}
	
	private Tokens(String[] tokens, Hasher<String> hasher) {
		this.strings = tokens;
		this.hasher = hasher;
	}
	
	public Set<String> newEmptySet() {
		return new TokenSet(this);
	}
	
	public Set<String> newFullSet() {
		TokenSet set = new TokenSet(this);
		set.fill();
		return set;
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

		public Map<String, V> newMap() {
			return new TokenMap<>(strings, hasher, storage.newStore(strings.length));
		}

	}
	
}
