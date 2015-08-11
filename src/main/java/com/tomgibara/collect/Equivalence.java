package com.tomgibara.collect;

import java.util.Random;

import com.tomgibara.storage.Storage;

public class Equivalence<E> {

	private final EquRel<E> equ;
	
	Equivalence(EquRel<E> equ) {
		this.equ = equ;
	}

	public Sets setsWithGenericStorage() {
		return new Sets(Storage.generic());
	}
	
	public Sets setsWithTypedStorage(Class<E> type) {
		return new Sets(Storage.typed(type));
	}
	
	public Sets setsWithStorage(Storage<E> storage) {
		if (storage == null) throw new IllegalArgumentException("null storage");
		return new Sets(storage);
	}
	
	public final class Sets {

		private static final int DEFAULT_CAPACITY = 16;
		
		private final Storage<E> storage;
		
		Sets(Storage<E> storage) {
			this.storage = storage;
		}
		
		public EquivalenceSet<E> newSet() {
			return new EquivalenceSet<E>(new Cuckoo<>(new Random(), equ), storage, DEFAULT_CAPACITY);
		}

	}
	
	
}
