package com.tomgibara.collect;

import java.math.BigDecimal;
import java.util.Arrays;

import com.tomgibara.hashing.Hasher;
import com.tomgibara.hashing.Hashing;
import com.tomgibara.streams.StreamBytes;
import com.tomgibara.streams.StreamSerializer;
import com.tomgibara.streams.Streams;

/**
 * <p>
 * An equivalence defines when two object are equivalent. A well defined
 * equivalence shares the same characteristics as a Java equality:
 * 
 * <ul>
 * <li>reflexivity
 * <li>symmetry
 * <li>transitivity
 * <li>consistency
 * </ul>
 * 
 * <p>
 * Equivalences are analogues of the abstract notion of 'equivalence relations'
 * and provide the ability to define notions of equality that are not consistent
 * with regular object equality.
 * 
 * <p>
 * Standard equivalences
 * are available based on regular object equality ({@link #equality()}) and
 * object identity ({@link #identity()}).
 * 
 * @author Tom Gibara
 *
 * @param <E>
 *            the type objects under equivalence
 */

public interface Equivalence<E> {

	/**
	 * An equivalence in which two objects <code>objA</code> and
	 * <code>objB</code> are equivalent if
	 * <code>Objects.equals(objA, objB)</code>, with a hasher derived from
	 * <code>Object.hashCode()</code>
	 * 
	 * @return equivalence based on object equality
	 */

	@SuppressWarnings("unchecked")
	public static <E> Equivalence<E> equality() {
		return (Equivalence<E>) Equivalences.EQUALITY;
	}

	/**
	 * An equivalence in which two objects <code>objA</code> and
	 * <code>objB</code> are equivalent if
	 * <code>objA == objB</code>, with a hasher derived from
	 * <code>System.identityHashCode()</code>
	 * 
	 * @return equivalence based on object identity
	 */

	@SuppressWarnings("unchecked")
	public static <E> Equivalence<E> identity() {
		return (Equivalence<E>) Equivalences.IDENTITY;
	}

	/**
	 * An equivalence in which two <code>byte</code> arrays <code>arrA</code>
	 * and <code>arrB</code> are equivalent if
	 * <code>Arrays.equals(arrA, arrB)</code>, with a hasher derived from
	 * <code>Arrays.hashCode()</code>.
	 * 
	 * @return an equivalence based on element-wise equality.
	 */

	public static Equivalence<byte[]> bytes() {
		return Equivalences.BYTES;
	}

	/**
	 * An equivalence in which two <code>short</code> arrays <code>arrA</code>
	 * and <code>arrB</code> are equivalent if
	 * <code>Arrays.equals(arrA, arrB)</code>, with a hasher derived from
	 * <code>Arrays.hashCode()</code>.
	 * 
	 * @return an equivalence based on element-wise equality.
	 */

	public static Equivalence<short[]> shorts() {
		return Equivalences.SHORTS;
	}

	/**
	 * An equivalence in which two <code>int</code> arrays <code>arrA</code>
	 * and <code>arrB</code> are equivalent if
	 * <code>Arrays.equals(arrA, arrB)</code>, with a hasher derived from
	 * <code>Arrays.hashCode()</code>.
	 * 
	 * @return an equivalence based on element-wise equality.
	 */

	public static Equivalence<int[]> ints() {
		return Equivalences.INTS;
	}

	/**
	 * An equivalence in which two <code>long</code> arrays <code>arrA</code>
	 * and <code>arrB</code> are equivalent if
	 * <code>Arrays.equals(arrA, arrB)</code>, with a hasher derived from
	 * <code>Arrays.hashCode()</code>.
	 * 
	 * @return an equivalence based on element-wise equality.
	 */

	public static Equivalence<long[]> longs() {
		return Equivalences.LONGS;
	}

	/**
	 * An equivalence in which two <code>boolean</code> arrays <code>arrA</code>
	 * and <code>arrB</code> are equivalent if
	 * <code>Arrays.equals(arrA, arrB)</code>, with a hasher derived from
	 * <code>Arrays.hashCode()</code>.
	 * 
	 * @return an equivalence based on element-wise equality.
	 */

	public static Equivalence<boolean[]> booleans() {
		return Equivalences.BOOLEANS;
	}

	/**
	 * An equivalence in which two <code>char</code> arrays <code>arrA</code>
	 * and <code>arrB</code> are equivalent if
	 * <code>Arrays.equals(arrA, arrB)</code>, with a hasher derived from
	 * <code>Arrays.hashCode()</code>.
	 * 
	 * @return an equivalence based on element-wise equality.
	 */

	public static Equivalence<char[]> chars() {
		return Equivalences.CHARS;
	}

	/**
	 * An equivalence in which two <code>float</code> arrays <code>arrA</code>
	 * and <code>arrB</code> are equivalent if
	 * <code>Arrays.equals(arrA, arrB)</code>, with a hasher derived from
	 * <code>Arrays.hashCode()</code>.
	 * 
	 * @return an equivalence based on element-wise equality.
	 */

	public static Equivalence<float[]> floats() {
		return Equivalences.FLOATS;
	}

	/**
	 * An equivalence in which two <code>double</code> arrays <code>arrA</code>
	 * and <code>arrB</code> are equivalent if
	 * <code>Arrays.equals(arrA, arrB)</code>, with a hasher derived from
	 * <code>Arrays.hashCode()</code>.
	 * 
	 * @return an equivalence based on element-wise equality.
	 */

	public static Equivalence<double[]> doubles() {
		return Equivalences.DOUBLES;
	}

	/**
	 * <p>
	 * An equivalence in which two {@link BigDecimal} values are equivalent if
	 * they compare as zero. This avoids the dislocation that occurs when two
	 * {@link BigDecimal} that have two equal numerical values compare as
	 * <i>unequal</i> because they have different scales.
	 *
	 * <p>
	 * <b>Warning:</b> this equivalence attempts to provide a scale invariant
	 * hash that is consistent with {@code BigDecimal.compare()}. In the absence
	 * of any efficient means by which to generate a hash, it makes assumptions
	 * about the implementation of the {@code BigDecimal.hashCode()} method that
	 * may not be true on all Java platforms.
	 *
	 * @return a comparison based equivalence on big decimals
	 */

	public static Equivalence<BigDecimal> bigDecimal() {
		return Equivalences.BIG_DECIMAL;
	}

	/**
	 * Defines an equivalence based on byte serialization of the object, with
	 * two objects being equivalent if they have the same serialization.
	 * 
	 * @param serializer
	 *            a serializer for an object type
	 * @return an equivalence over the object type derived from the specified
	 *         serializer
	 */

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

	/**
	 * Whether the two objects are considered equivalent under this equivalence
	 * relation. Unlike the standard <code>Object.equals()</code> method,
	 * implementations are not required to accommodate invalid parameters and
	 * are free to throw exceptions in such cases.
	 * 
	 * @param e1
	 *            one object to be tested for equivalence
	 * @param e2
	 *            another object to be tested for equivalence
	 * @return true if and only if e1 is equivalent to e2
	 */

	boolean isEquivalent(E e1, E e2);
	
	/**
	 * A hash consistent with this equivalence. Similarly to the
	 * <code>Object.hashCode()</code> method this implies that if <code>a</code>
	 * and <code>b</code> are equivalent under <code>e</code> then
	 * <code>e.getHasher().hash(a)</code> yields the same hash values as
	 * <code>e.getHasher().hash(b)</code>.
	 * 
	 * @return a hash consistent with this equivalence
	 */

	default Hasher<E> getHasher() {
		return Hashing.objectHasher();
	}
}
