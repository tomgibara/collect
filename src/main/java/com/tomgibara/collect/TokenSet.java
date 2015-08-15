package com.tomgibara.collect;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.tomgibara.bits.BitVector;
import com.tomgibara.hashing.Hasher;
import com.tomgibara.storage.Mutability;

public final class TokenSet extends AbstractSet<String> implements Mutability<TokenSet> {

	private final String[] strings;
	private final Hasher<String> hasher;
	private final BitVector bits;
	
	TokenSet(Tokens tokens) {
		this(tokens.strings, tokens.hasher, new BitVector(tokens.strings.length));
	}

	private TokenSet(String[] strings, Hasher<String> hasher, BitVector bits) {
		this.strings = strings;
		this.hasher = hasher;
		this.bits = bits;
	}
	
	// methods
	
	public void fill() {
		bits.set(true);
	}
	
	public boolean isFull() {
		return bits.isAllOnes();
	}
	
	// mutability
	
	@Override
	public boolean isMutable() {
		return bits.isMutable();
	}
	
	@Override
	public TokenSet mutable() {
		return isMutable() ? this : mutableCopy();
	}

	@Override
	public TokenSet immutable() {
		return isMutable() ? immutableView() : this;
	}
	
	@Override
	public TokenSet mutableCopy() {
		return new TokenSet(strings, hasher, bits.mutableCopy());
	}
	
	@Override
	public TokenSet immutableCopy() {
		return new TokenSet(strings, hasher, bits.immutableCopy());
	}
	
	@Override
	public TokenSet immutableView() {
		return new TokenSet(strings, hasher, bits.immutable());
	}

	// set
	
	@Override
	public int size() {
		return bits.countOnes();
	}
	
	@Override
	public boolean contains(Object o) {
		int i = indexOf(o);
		return i == -1 ? false : bits.getBit(i);
	}

	@Override
	public boolean remove(Object o) {
		int i = indexOf(o);
		return i == -1 ? false : bits.getThenSetBit(i, false);
	}

	@Override
	public void clear() {
		bits.set(false);
	}
	
	@Override
	public boolean isEmpty() {
		return bits.isAllZeros();
	}
	
	@Override
	public boolean add(String e) {
		int i = checkedIndexOf(e);
		return !bits.getThenSetBit(i, true);
	}
	
	@Override
	public Object[] toArray() {
		int length = bits.countOnes();
		Object[] array = new Object[length];
		populateArray(array, length);
		return array;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		int length = bits.countOnes();
		Object[] array;
		if (a.length >= length) {
			array = a;
		} else if (a.getClass().getComponentType() == String.class) {
			array = new String[length];
		} else {
			array = new Object[length];
		}
		populateArray(array, length);
		return (T[]) array;
	}
	
	@Override
	public Iterator<String> iterator() {
		return new Iterator<String>() {
			private int previous = -1;
			private int next = bits.nextOne(previous + 1);
			@Override
			public String next() {
				previous = next;
				next = bits.nextOne(previous + 1);
				return strings[previous];
			}
			@Override
			public boolean hasNext() {
				return next != strings.length;
			}
			@Override
			public void remove() {
				if (previous == -1 || !bits.getThenSetBit(previous, false)) throw new IllegalStateException();
			}
		};
	}
	
	@Override
	public boolean removeIf(Predicate<? super String> filter) {
		boolean modified = false;
		for (int p = -1; p < strings.length; p = bits.nextOne(p + 1)) {
			String s = strings[p];
			if (filter.test(s)) {
				modified = bits.getThenSetBit(p, false) || modified;
			}
		}
		return modified;
	}
	
	@Override
	public void forEach(Consumer<? super String> action) {
		for (int p = -1; p < strings.length; p = bits.nextOne(p + 1)) {
			action.accept(strings[p]);
		}
	}
	
	// private utility methods
	
	private int indexOf(Object o) {
		if (!(o instanceof String)) return -1;
		String s = (String) o;
		//TODO no way to make this more efficient yet
		int i;
		try {
			i = hasher.intHashValue(s);
		} catch (IllegalArgumentException e) {
			return -1;
		}
		return strings[i].equals(s) ? i : -1;
	}
	
	private int checkedIndexOf(String s) {
		int i = hasher.intHashValue(s);
		if (!strings[i].equals(s)) throw new IllegalArgumentException("invalid token");
		return i;
	}
	
	private void populateArray(Object[] array, int length) {
		int p = -1;
		for (int i = 0; i < array.length; i++) {
			p = bits.nextOne(p + 1);
			array[i] = strings[p];
		}
	}
}
