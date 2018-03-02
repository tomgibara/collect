package com.tomgibara.collect;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.UNNECESSARY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;

import com.tomgibara.hashing.Hasher;

public class EquivalencesTest {

	@Test
	public void testBigDecimal() {
		Equivalence<BigDecimal> e = Equivalence.bigDecimal();
		Hasher<BigDecimal> h = e.getHasher();
		// check basic equivalence
		assertTrue ( e.isEquivalent(ONE, ONE)  );
		assertFalse( e.isEquivalent(ONE, ZERO) );
		assertFalse( e.isEquivalent(ZERO, ONE) );
		// check null
		assertEquals(0, h.intHashValue(null)   );
		assertTrue  (e.isEquivalent(null, null));
		assertFalse (e.isEquivalent(null, ONE) );
		assertFalse (e.isEquivalent(ZERO, null));
		// check scale invariance
		assertTrue( e.isEquivalent(ONE, ONE.setScale(3, UNNECESSARY)) );
	}
}
