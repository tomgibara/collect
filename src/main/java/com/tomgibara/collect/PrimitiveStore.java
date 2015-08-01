package com.tomgibara.collect;

import java.lang.reflect.Array;
import java.util.Arrays;

import com.tomgibara.bits.BitVector;

abstract class PrimitiveStore<V> implements Store<V> {

	private static final int BYTE    =  1;
	private static final int FLOAT   =  2;
	private static final int CHAR    =  3;
	private static final int SHORT   =  4;
	private static final int LONG    =  6;
	private static final int INT     =  7;
	private static final int DOUBLE  = 11;
	private static final int BOOLEAN = 12;

	
	static <V> PrimitiveStore<V> newStore(Class<V> type, int capacity) {
		switch((type.getName().hashCode() >> 8) & 0xf) {
		case BYTE:    return (PrimitiveStore<V>) new ByteStore     (capacity);
		case FLOAT:   return (PrimitiveStore<V>) new FloatStore    (capacity);
		case CHAR:    return (PrimitiveStore<V>) new CharacterStore(capacity);
		case SHORT:   return (PrimitiveStore<V>) new ShortStore    (capacity);
		case LONG:    return (PrimitiveStore<V>) new LongStore     (capacity);
		case INT:     return (PrimitiveStore<V>) new IntegerStore  (capacity);
		case DOUBLE:  return (PrimitiveStore<V>) new DoubleStore   (capacity);
		case BOOLEAN: return (PrimitiveStore<V>) new BooleanStore  (capacity);
		default: throw new IllegalArgumentException(type.getName());
		}
	}

	//TODO how to support this?
//	static <V> PrimitiveStore(V[] values) {
//		size = populated.countOnes();
//	}

	int size;
	BitVector populated;
	
	protected PrimitiveStore(BitVector populated, int size) {
		this.populated = populated;
		this.size = size;
	}
	
	protected PrimitiveStore(int capacity) {
		populated = new BitVector(capacity);
		this.size = 0;
	}
	
	@Override
	public int size() {
		return size;
	}
	
	@Override
	public void clear() {
		populated.set(false);
		size = 0;
	}

	@Override
	public boolean isMutable() {
		return populated.isMutable();
	}
	
	@Override
	public V get(int index) {
		return populated.getBit(index) ? getImpl(index) : null;
	}

	@Override
	public V set(int index, V value) {
		if (populated.getBit(index)) {
			V previous = getImpl(index);
			if (value == null) {
				populated.setBit(index, false);
				size --;
			} else {
				setImpl(index, value);
			}
			return previous;
		} else if (value != null) {
			setImpl(index, value);
			populated.setBit(index, true);
			size ++;
		}
		return null;
	}

	@Override
	public Store<V> mutableCopy() {
		return isMutable() ? immutableCopy() : this;
	}
	
	abstract protected V getImpl(int index);
	
	abstract protected void setImpl(int index, V value);
	
	abstract protected PrimitiveStore<V> immutableCopy();
	
	// inner classes

	private final static class ByteStore extends PrimitiveStore<Byte> {

		private byte[] values;

		ByteStore(int capacity) {
			super(capacity);
			this.values = new byte[capacity];
		}

		private ByteStore(ByteStore that) {
			super(that.populated.immutableCopy(), that.size);
			this.values = that.values.clone();
		}

		@Override
		public Class<? extends Byte> valueType() {
			return byte.class;
		}

		@Override
		protected Byte getImpl(int index) {
			return values[index];
		}

		@Override
		protected void setImpl(int index, Byte value) {
			values[index] = value;
		}

		@Override
		public ByteStore immutableCopy() {
			return new ByteStore(this);
		}

	}

	private final static class FloatStore extends PrimitiveStore<Float> {

		private float[] values;

		FloatStore(int capacity) {
			super(capacity);
			this.values = new float[capacity];
		}

		private FloatStore(FloatStore that) {
			super(that.populated.immutableCopy(), that.size);
			this.values = that.values.clone();
		}

		@Override
		public Class<? extends Float> valueType() {
			return float.class;
		}

		@Override
		protected Float getImpl(int index) {
			return values[index];
		}

		@Override
		protected void setImpl(int index, Float value) {
			values[index] = value;
		}

		@Override
		public FloatStore immutableCopy() {
			return new FloatStore(this);
		}

	}

	private final static class CharacterStore extends PrimitiveStore<Character> {

		private char[] values;

		CharacterStore(int capacity) {
			super(capacity);
			this.values = new char[capacity];
		}

		private CharacterStore(CharacterStore that) {
			super(that.populated.immutableCopy(), that.size);
			this.values = that.values.clone();
		}

		@Override
		public Class<? extends Character> valueType() {
			return char.class;
		}

		@Override
		protected Character getImpl(int index) {
			return values[index];
		}

		@Override
		protected void setImpl(int index, Character value) {
			values[index] = value;
		}

		@Override
		public CharacterStore immutableCopy() {
			return new CharacterStore(this);
		}

	}

	private final static class ShortStore extends PrimitiveStore<Short> {

		private short[] values;

		ShortStore(int capacity) {
			super(capacity);
			this.values = new short[capacity];
		}

		private ShortStore(ShortStore that) {
			super(that.populated.immutableCopy(), that.size);
			this.values = that.values.clone();
		}

		@Override
		public Class<? extends Short> valueType() {
			return short.class;
		}

		@Override
		protected Short getImpl(int index) {
			return values[index];
		}

		@Override
		protected void setImpl(int index, Short value) {
			values[index] = value;
		}

		@Override
		public ShortStore immutableCopy() {
			return new ShortStore(this);
		}

	}

	private final static class LongStore extends PrimitiveStore<Long> {

		private long[] values;

		LongStore(int capacity) {
			super(capacity);
			this.values = new long[capacity];
		}

		private LongStore(LongStore that) {
			super(that.populated.immutableCopy(), that.size);
			this.values = that.values.clone();
		}

		@Override
		public Class<? extends Long> valueType() {
			return long.class;
		}

		@Override
		protected Long getImpl(int index) {
			return values[index];
		}

		@Override
		protected void setImpl(int index, Long value) {
			values[index] = value;
		}

		@Override
		public LongStore immutableCopy() {
			return new LongStore(this);
		}

	}

	private final static class IntegerStore extends PrimitiveStore<Integer> {

		private int[] values;

		IntegerStore(int capacity) {
			super(capacity);
			this.values = new int[capacity];
		}

		private IntegerStore(IntegerStore that) {
			super(that.populated.immutableCopy(), that.size);
			this.values = that.values.clone();
		}

		@Override
		public Class<? extends Integer> valueType() {
			return int.class;
		}

		@Override
		protected Integer getImpl(int index) {
			return values[index];
		}

		@Override
		protected void setImpl(int index, Integer value) {
			values[index] = value;
		}

		@Override
		public IntegerStore immutableCopy() {
			return new IntegerStore(this);
		}

	}

	private final static class DoubleStore extends PrimitiveStore<Double> {

		private double[] values;

		DoubleStore(int capacity) {
			super(capacity);
			this.values = new double[capacity];
		}

		private DoubleStore(DoubleStore that) {
			super(that.populated.immutableCopy(), that.size);
			this.values = that.values.clone();
		}

		@Override
		public Class<? extends Double> valueType() {
			return double.class;
		}

		@Override
		protected Double getImpl(int index) {
			return values[index];
		}

		@Override
		protected void setImpl(int index, Double value) {
			values[index] = value;
		}

		@Override
		public DoubleStore immutableCopy() {
			return new DoubleStore(this);
		}

	}

	private final static class BooleanStore extends PrimitiveStore<Boolean> {

		private boolean[] values;

		BooleanStore(int capacity) {
			super(capacity);
			this.values = new boolean[capacity];
		}

		private BooleanStore(BooleanStore that) {
			super(that.populated.immutableCopy(), that.size);
			this.values = that.values.clone();
		}

		@Override
		public Class<? extends Boolean> valueType() {
			return boolean.class;
		}

		@Override
		protected Boolean getImpl(int index) {
			return values[index];
		}

		@Override
		protected void setImpl(int index, Boolean value) {
			values[index] = value;
		}

		@Override
		public BooleanStore immutableCopy() {
			return new BooleanStore(this);
		}

	}

}
