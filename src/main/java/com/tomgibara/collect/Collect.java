package com.tomgibara.collect;

/**
 * Provides static method for creating equivalence collections from equivalences.
 * Serves as the entrypoint for the API.
 * 
 * @author Tom Gibara
 */

public final class Collect {

	/**
	 * Creates collections based on the supplied equivalence. The equivalence
	 * determines key equality.
	 * 
	 * @param equ an equivalence
	 * @return collections based the equivalence
	 */

	public static <E> EquivalenceCollections<E> equivalence(Equivalence<E> equ) {
		if (equ == null) throw new IllegalArgumentException("null equ");
		return new EquivalenceCollections<E>(equ);
	}

	/**
	 * Creates collections from the equality equivalence.
	 * 
	 * @return collections based on java object equality
	 * @see Equivalence#equality()
	 */

	public static <E> EquivalenceCollections<E> equality() {
		return new EquivalenceCollections<>(Equivalence.equality());
	}

	/**
	 * Creates collections from the identity equivalence.
	 * 
	 * @return collections based on java object identity
	 * @see Equivalence#identity()
	 */

	public static <E> EquivalenceCollections<E> identity() {
		return new EquivalenceCollections<>(Equivalence.identity());
	}

	private Collect() {}

}
