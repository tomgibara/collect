package com.tomgibara.collect;

import java.util.Arrays;

import com.tomgibara.hashing.Hasher;
import com.tomgibara.hashing.Hashing;
import com.tomgibara.streams.StreamBytes;
import com.tomgibara.streams.StreamSerializer;
import com.tomgibara.streams.Streams;

public interface Equivalence<E> {

	@SuppressWarnings("unchecked")
	public static <E> Equivalence<E> equality() {
		return (Equivalence<E>) Equivalences.EQUALITY;
	}

	@SuppressWarnings("unchecked")
	public static <E> Equivalence<E> identity() {
		return (Equivalence<E>) Equivalences.IDENTITY;
	}

	public static Equivalence<byte[]> bytes() {
		return Equivalences.BYTES;
	}

	public static Equivalence<short[]> shorts() {
		return Equivalences.SHORTS;
	}

	public static Equivalence<int[]> ints() {
		return Equivalences.INTS;
	}

	public static Equivalence<long[]> longs() {
		return Equivalences.LONGS;
	}

	public static Equivalence<boolean[]> booleans() {
		return Equivalences.BOOLEANS;
	}

	public static Equivalence<char[]> chars() {
		return Equivalences.CHARS;
	}

	public static Equivalence<float[]> floats() {
		return Equivalences.FLOATS;
	}

	public static Equivalence<double[]> doubles() {
		return Equivalences.DOUBLES;
	}

	public static <E> Equivalence<E> fromSerializer(StreamSerializer<E> serializer) {
		return new Equivalence<E>() {

			private final Hasher<E> hasher = Hashing.murmur3Int().hasher(serializer);

			@Override
			public boolean isEquivalent(E e1, E e2) {
				return Arrays.equals(toBytes(e1), toBytes(e2));
			}
			@Override
			public Hasher<E> getHasher() {
				return hasher;
			}

			private byte[] toBytes(E e) {
				StreamBytes bytes = Streams.bytes();
				serializer.serialize(e, bytes.writeStream());
				return bytes.bytes();
			}
		};
	}

	boolean isEquivalent(E e1, E e2);
	
	// a hash consistent with this equivalence
	default Hasher<E> getHasher() {
		return Hashing.objectHasher();
	}
}
