package com.tomgibara.collect;


public final class Collect {

	public static <E> EquivalenceCol<E> equivalence(Equivalence<E> equ) {
		if (equ == null) throw new IllegalArgumentException("null equ");
		return new EquivalenceCol<E>(equ);
	}

	public static <E> EquivalenceCol<E> equality() {
		return new EquivalenceCol<>(Equivalence.equality());
	}

	public static <E> EquivalenceCol<E> identity() {
		return new EquivalenceCol<>(Equivalence.identity());
	}

	private Collect() {}

}
