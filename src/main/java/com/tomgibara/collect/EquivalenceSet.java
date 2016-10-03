package com.tomgibara.collect;

import java.util.Set;

import com.tomgibara.fundament.Mutability;

/**
 * A set in which the notion of equality is generalized to 'equivalence'. Sets
 * of this nature also support mutability control through the
 * <code>Mutability</code> interface.
 * 
 * @author Tom Gibara
 *
 * @param <E>
 *            the type objects under equivalence
 * @see Equivalence
 */

public interface EquivalenceSet<E> extends Set<E>, Mutability<EquivalenceSet<E>> {

	/**
	 * The equivalence relation used to distinguish elements of the set.
	 * 
	 * @return the equivalence under which the set operates.
	 */

	Equivalence<E> getEquivalence();

	/**
	 * Returns the element in the set that is equivalent to the supplied
	 * element, or null.
	 * 
	 * @param e
	 *            a possible element of the set
	 * @return an equivalent element from the set, or null
	 */

	E get(E e);

	/**
	 * <p>
	 * Returns some element from the set, or null if the set is empty.
	 * 
	 * <p>
	 * There is guarantee that this method will return the same or a different
	 * element on each call.
	 * 
	 * @return an element from the set, or null if the set is empty
	 */

	E some();
}
