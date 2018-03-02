package com.tomgibara.collect;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Objects;

import com.tomgibara.hashing.HashCode;
import com.tomgibara.hashing.HashSize;
import com.tomgibara.hashing.Hasher;
import com.tomgibara.hashing.Hashing;

class Equivalences {

	static final Equivalence<?> EQUALITY = new Equivalence<Object>() {
		@Override public boolean isEquivalent(Object e1, Object e2) { return Objects.equals(e1, e2); }
	};

	static final Equivalence<?> IDENTITY = new Equivalence<Object>() {
		@Override public boolean isEquivalent(Object e1, Object e2) { return e1 == e2; }
		@Override public Hasher<Object> getHasher() { return Hashing.identityHasher(); }
	};

	static final Equivalence<byte[]> BYTES = new Equivalence<byte[]>() {
		@Override public boolean isEquivalent(byte[] e1, byte[] e2) { return Arrays.equals(e1, e2); }
		@Override public Hasher<byte[]> getHasher() { return Hashing.bytesHasher(); }
	};

	static final Equivalence<short[]> SHORTS = new Equivalence<short[]>() {
		@Override public boolean isEquivalent(short[] e1, short[] e2) { return Arrays.equals(e1, e2); }
		@Override public Hasher<short[]> getHasher() { return Hashing.shortsHasher(); }
	};

	static final Equivalence<int[]> INTS = new Equivalence<int[]>() {
		@Override public boolean isEquivalent(int[] e1, int[] e2) { return Arrays.equals(e1, e2); }
		@Override public Hasher<int[]> getHasher() { return Hashing.intsHasher(); }
	};

	static final Equivalence<long[]> LONGS = new Equivalence<long[]>() {
		@Override public boolean isEquivalent(long[] e1, long[] e2) { return Arrays.equals(e1, e2); }
		@Override public Hasher<long[]> getHasher() { return Hashing.longsHasher(); }
	};

	static final Equivalence<boolean[]> BOOLEANS = new Equivalence<boolean[]>() {
		@Override public boolean isEquivalent(boolean[] e1, boolean[] e2) { return Arrays.equals(e1, e2); }
		@Override public Hasher<boolean[]> getHasher() { return Hashing.booleansHasher(); }
	};

	static final Equivalence<char[]> CHARS = new Equivalence<char[]>() {
		@Override public boolean isEquivalent(char[] e1, char[] e2) { return Arrays.equals(e1, e2); }
		@Override public Hasher<char[]> getHasher() { return Hashing.charsHasher(); }
	};

	static final Equivalence<float[]> FLOATS = new Equivalence<float[]>() {
		@Override public boolean isEquivalent(float[] e1, float[] e2) { return Arrays.equals(e1, e2); }
		@Override public Hasher<float[]> getHasher() { return Hashing.floatsHasher(); }
	};

	static final Equivalence<double[]> DOUBLES = new Equivalence<double[]>() {
		@Override public boolean isEquivalent(double[] e1, double[] e2) { return Arrays.equals(e1, e2); }
		@Override public Hasher<double[]> getHasher() { return Hashing.doublesHasher(); }
	};

	static final Equivalence<BigDecimal> BIG_DECIMAL = new Equivalence<BigDecimal>() {

		@Override
		public boolean isEquivalent(BigDecimal e1, BigDecimal e2) {
			if (e1 == e2) return true;
			if (e1 == null) return e2 == null;
			if (e2 == null) return false;
			return e1.compareTo(e2) == 0;
		}

		@Override
		public Hasher<BigDecimal> getHasher() {
			return new Hasher<BigDecimal>() {
				@Override public HashSize getSize()                 { return HashSize.INT_SIZE;                                    }
				@Override public HashCode hash(BigDecimal value)    { return HashCode.fromInt(intHashValue(value));                }
				@Override public int intHashValue(BigDecimal value) { return value == null ? 0 : value.hashCode() - value.scale(); }
			};
		}

	};
}
