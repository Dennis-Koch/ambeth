package com.koch.ambeth.cache.datachange.revert;

import java.lang.reflect.Array;

public class ArrayBackup implements IBackup {
	protected final Object arrayClone;

	public ArrayBackup(Object arrayClone) {
		this.arrayClone = arrayClone;
	}

	@Override
	public void restore(Object targetArray) {
		int length = Array.getLength(arrayClone);
		System.arraycopy(arrayClone, 0, targetArray, 0, length);
	}
}