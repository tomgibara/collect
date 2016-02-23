package com.tomgibara.collect;

import java.util.Set;

import com.tomgibara.fundament.Mutability;

public interface EquivalenceSet<E> extends Set<E>, Mutability<EquivalenceSet<E>> {

	Equivalence<E> getEquivalence();

}
