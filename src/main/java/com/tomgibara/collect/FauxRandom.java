package com.tomgibara.collect;

import java.util.Random;

class FauxRandom extends Random {

	private static final long serialVersionUID = -5783830681722690526L;

	static final FauxRandom INSTANCE = new FauxRandom();

	private FauxRandom() { super(0L); }

	public int nextInt(int bound) { return 0; }

}
