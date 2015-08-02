package com.tomgibara.collect;

import com.tomgibara.hashing.Hashing;

public final class Collect {

	public static Tokens tokens(String... strings) {
		if (strings == null) throw new IllegalArgumentException("null strings");
		strings = strings.clone();
		return new Tokens(strings, Hashing.minimalPerfect(strings));
	}

	public static <E> Equivalence<E> equivalence(EquRel<E> equ) {
		if (equ == null) throw new IllegalArgumentException("null equ");
		return new Equivalence<E>(equ);
	}

	public static <E> Equivalence<E> equality() {
		return new Equivalence<>(EquRel.equality());
	}

	public static <E> Equivalence<E> identity() {
		return new Equivalence<>(EquRel.identity());
	}

	private Collect() {}

}
