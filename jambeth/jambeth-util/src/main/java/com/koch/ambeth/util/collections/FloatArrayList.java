package com.koch.ambeth.util.collections;

/*-
 * #%L
 * jambeth-util
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.koch.ambeth.util.IPrintable;

public class FloatArrayList implements Externalizable, IPrintable {
	private static final float[] emptyArray = new float[0];

	protected final int incStep;

	protected float[] array = emptyArray;

	protected int size;

	public FloatArrayList() {
		this(10);
	}

	public FloatArrayList(final float[] array) {
		incStep = 10;
		init(array, array.length);
	}

	public FloatArrayList(final int iincStep) {
		float[] array = new float[iincStep];
		incStep = Math.max(10, iincStep);
		init(array, 0);
	}

	protected void init(final float[] array, final int size) {
		this.array = array;
		this.size = size;
	}

	public final boolean add(final float value) {
		int size = this.size;
		float[] array = this.array;
		if (size == array.length) {
			final float[] buff = new float[(array.length << 1) + incStep];
			System.arraycopy(array, 0, buff, 0, size);
			array = buff;
			this.array = array;
		}
		array[size++] = value;
		this.size = size;
		return true;
	}

	public final boolean remove(final float value) {
		int size = this.size;
		float[] array = this.array;
		if (value == 0) {
			for (int a = 0; a < size; a++) {
				if (array[a] == 0) {
					removeAtIndex(a);
					return true;
				}
			}
		}
		else {
			for (int a = 0; a < size; a++) {
				final float item = array[a];
				if (value == item) {
					removeAtIndex(a);
					return true;
				}
			}
		}
		return false;
	}

	public final boolean hasValue(final float value) {
		int size = this.size;
		float[] array = this.array;
		if (value == 0) {
			for (int a = 0; a < size; a++) {
				if (array[a] == 0) {
					return true;
				}
			}
		}
		else {
			for (int a = 0; a < size; a++) {
				final float item = array[a];
				if (value == item) {
					return true;
				}
			}
		}
		return false;
	}

	public float get(final int index) {
		return array[index];
	}

	public final float peek() {
		int size = this.size;
		if (size > 0) {
			return array[size - 1];
		}
		else {
			return Float.NaN;
		}
	}

	public final float popLastElement() {
		int size = this.size;
		if (size > 0) {
			float[] array = this.array;
			final float elem = array[--size];
			array[size] = 0;
			this.size = size;
			return elem;
		}
		else {
			return Float.NaN;
		}
	}

	public final void clearFrom(final int index) {
		int size = this.size;
		float[] array = this.array;
		for (int a = index; a < size; a++) {
			array[a] = 0;
		}
		this.size = index;
	}

	public final int size() {
		return size;
	}

	public final void clear() {
		clearFrom(0);
	}

	public final void copyInto(final FloatArrayList otherList) {
		otherList.size = 0;
		int size = this.size;
		float[] array = this.array;
		for (int a = 0; a < size; a++) {
			otherList.add(array[a]);
		}
	}

	public float remove(final int index) {
		final float object = array[index];
		removeAtIndex(index);
		return object;
	}

	public void removeAtIndex(final int index) {
		int size = this.size;
		float[] array = this.array;
		for (int a = index, sizeA = size - 1; a < sizeA; a++) {
			array[a] = array[a + 1];
		}
		size--;
		this.size = size;
		array[size] = 0;
	}

	public void add(final int index, final float element) {
		int size = this.size;
		float[] array = this.array;
		if (size == array.length) {
			final float[] buff = new float[(array.length << 1) + incStep];
			System.arraycopy(array, 0, buff, 0, size);
			array = buff;
			this.array = array;
		}
		for (int a = size + 1, i = index + 1; a-- > i;) {
			array[a] = array[a - 1];
		}
		array[index] = element;
		size++;
		this.size = size;
	}

	public boolean contains(final float o) {
		return indexOf(o) >= 0;
	}

	public int indexOf(final float o) {
		int size = this.size;
		float[] array = this.array;
		for (int a = 0; a < size; a++) {
			final float item = array[a];
			if (o == 0) {
				if (item == 0) {
					return a;
				}
			}
			else if (o == item) {
				return a;
			}
		}
		return -1;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public int lastIndexOf(final float o) {
		throw new UnsupportedOperationException();
	}

	public float set(final int index, final float element) {
		float[] array = this.array;
		final float oldElement = array[index];
		array[index] = element;
		return oldElement;
	}

	public FloatArrayList subList(final int fromIndex, final int toIndex) {
		float[] array = this.array;
		final FloatArrayList sublist = new FloatArrayList(toIndex - fromIndex);
		for (int a = fromIndex; a < toIndex; a++) {
			sublist.add(array[a]);
		}
		return sublist;
	}

	public float[] toArray() {
		return toArray(new float[size]);
	}

	public float[] toArray(final float[] targetArray) {
		System.arraycopy(array, 0, targetArray, 0, size);
		return targetArray;
	}

	@Override
	public void readExternal(final ObjectInput arg0) throws IOException, ClassNotFoundException {
		int size = arg0.readInt();
		float[] array = null;
		if (size > 0) {
			array = new float[size];
			for (int a = 0; a < size; a++) {
				array[a] = arg0.readFloat();
			}
		}
		else {
			array = new float[0];
		}
		this.array = array;
		this.size = size;
	}

	@Override
	public void writeExternal(final ObjectOutput arg0) throws IOException {
		int size = this.size;
		float[] array = this.array;
		arg0.writeInt(size);
		for (int a = 0; a < size; a++) {
			arg0.writeFloat(array[a]);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb) {
		sb.append(size()).append(" items: [");
		for (int a = 0, size = size(); a < size; a++) {
			if (a > 0) {
				sb.append(',');
			}
			sb.append(get(a));
		}
		sb.append(']');
	}

	public float[] getBackingArray() {
		return array;
	}
}
