package com.tomgibara.collect;


import com.tomgibara.hashing.Hasher;
import com.tomgibara.hashing.Hashing;

public interface EquRel<E> {

	public static <E> EquRel<E> equality() {
		return (EquRel<E>) EquRels.EQUALITY;
	}

	public static <E> EquRel<E> identity() {
		return (EquRel<E>) EquRels.IDENTITY;
	}
	
	boolean isEquivalent(E e1, E e2);
	
	// a hash consistent with this equivalence
	default Hasher<E> getHasher() {
		return Hashing.objectHasher();
	}
}
