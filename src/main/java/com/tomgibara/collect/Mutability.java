package com.tomgibara.collect;

public interface Mutability<T> {

	boolean isMutable();
	
	T mutable();
	
	T mutableCopy();
	
	T mutableView();
	
	T immutable();
	
	T immutableCopy();
	
	T immutableView();
}
