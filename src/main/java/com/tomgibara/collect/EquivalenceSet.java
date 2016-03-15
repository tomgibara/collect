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

}
