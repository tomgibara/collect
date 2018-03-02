package com.tomgibara.collect;

import java.util.Map;

import com.tomgibara.fundament.Mutability;

/**
 * <p>
 * A map in which the notions of both key equality and value equality are
 * generalized to 'equivalence'. Maps of this nature also support mutability
 * control through the <code>Mutability</code> interface.
 *
 * <p>
 * In adhering to the Java Collections API, the definitions of
 * {@link #equals(Object)} and {@link #hashCode()} do not reflect the
 * equivalence relation imposed on the map's keys or values.
 * 
 * @author Tom Gibara
 *
 * @param <K>
 *            the keys type under equivalence
 * @param <V>
 *            the value type under equivalence
 * @see Equivalence
 */

public interface EquivalenceMap<K,V> extends Map<K, V>, Mutability<EquivalenceMap<K, V>> {

	/**
	 * The equivalence relation used to distinguish map keys.
	 * 
	 * @return the equivalence under which the key set operates.
	 */

	Equivalence<K> getKeyEquivalence();
	
	/**
	 * The equivalence relation used to distinguish map values.
	 * 
	 * @return the equivalence under which the values operate.
	 */

	Equivalence<V> getValueEquivalence();
	
	@Override
	EquivalenceSet<K> keySet();

}
