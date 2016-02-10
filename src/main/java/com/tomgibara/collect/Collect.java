package com.tomgibara.collect;


public final class Collect {

	public static <E> EquivalenceCollections<E> equivalence(Equivalence<E> equ) {
		if (equ == null) throw new IllegalArgumentException("null equ");
		return new EquivalenceCollections<E>(equ);
	}

	public static <E> EquivalenceCollections<E> equality() {
		return new EquivalenceCollections<>(Equivalence.equality());
	}

	public static <E> EquivalenceCollections<E> identity() {
		return new EquivalenceCollections<>(Equivalence.identity());
	}

	private Collect() {}

}
