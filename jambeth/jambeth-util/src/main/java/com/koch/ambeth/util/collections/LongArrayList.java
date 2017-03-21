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

/**
 * Hochperformante Implementierung einer Liste speziell f�r primitive Integer. Die �bliche Verwendung einer auf Integer.class typisierten java.util.List
 * f�hrt zu einer enormen Garbage-Last bei intensiver Nutzung.
 * 
 * Diese Klasse bietet �hnliche Funktionalit�t ohne Garbage.
 * 
 * @author kochd
 * 
 */
public class LongArrayList implements Externalizable, IPrintable
{
	private byte byteLength = 8;

	private boolean sorted = false;

	public long[] array;

	public int size = 0;

	public LongArrayList()
	{
		this(10, false);
	}

	public LongArrayList(boolean sorted)
	{
		this(10, sorted);
	}

	public LongArrayList(final int incStep, final boolean isorted)
	{
		sorted = isorted;
		array = new long[incStep];
		size = 0;
	}

	private final void checkArraySize()
	{
		if (size == array.length - 1)
		{
			long[] buff = new long[array.length << 1];
			System.arraycopy(array, 0, buff, 0, size);
			array = buff;
		}
	}

	public final void add(final long value)
	{
		checkArraySize();
		if (sorted)
		{
			int index = 0;
			if (size > 0)
			{
				index = getIndex(value);
			}
			addAtIndex(index, value);
			return;
		}
		array[size++] = value;
	}

	public final boolean addIfNotExists(final long value)
	{
		checkArraySize();
		if (sorted)
		{
			int index = 0;
			if (size > 0)
			{
				index = getIndex(value);
				if (index < size && array[index] == value)
				{
					return false;
				}
			}
			addAtIndex(index, value);
			return true;
		}
		array[size++] = value;
		return true;
	}

	private final void addAtIndex(final int index, final long value)
	{
		System.arraycopy(array, index, array, index + 1, size - index);
		array[index] = value;
		size++;
	}

	public final int addGiveIndex(final long value)
	{
		checkArraySize();
		if (sorted)
		{
			int index = 0;
			if (size > 0)
			{
				index = getIndex(value);
			}
			addAtIndex(index, value);
			return index;
		}
		else
		{
			throw new IllegalStateException();
		}
	}

	public final void remove(final long value)
	{
		if (sorted)
		{
			if (size > 0)
			{
				int index = getIndex(value);
				if (index < size && array[index] == value)
				{
					removeAtIndex(index);
				}
			}
		}
		else
		{
			for (int a = 0; a < size; a++)
			{
				if (array[a] == value)
				{
					removeAtIndex(a);
					return;
				}
			}
		}
	}

	public final void removeAtIndex(final int index)
	{
		for (int a = index; a < size; a++)
		{
			array[a] = array[a + 1];
		}
		size--;
	}

	public final long removeAtIndexGiveValue(final int index)
	{
		final long value = array[index];
		for (int a = index; a < size; a++)
		{
			array[a] = array[a + 1];
		}
		size--;
		return value;
	}

	public final boolean removeIfExists(final long value)
	{
		return (removeGiveIndex(value) != -1);
	}

	public final int removeGiveIndex(final long value)
	{
		if (sorted)
		{
			if (size > 0)
			{
				int index = getIndex(value);
				if (index < size && array[index] == value)
				{
					removeAtIndex(index);
					return index;
				}
			}
			return -1;
		}
		for (int a = 0; a < size; a++)
		{
			if (array[a] == value)
			{
				removeAtIndex(a);
				return a;
			}
		}
		return -1;
	}

	public final boolean hasValue(final long value)
	{
		if (sorted)
		{
			if (size > 0)
			{
				int index = getIndex(value);
				return (index < size && array[index] == value);
			}
			return false;
		}
		for (int a = 0; a < size; a++)
		{
			if (array[a] == value)
			{
				return true;
			}
		}
		return false;
	}

	public final int getFirstIndexWithValue(final long value)
	{
		if (sorted)
		{
			if (size > 0)
			{
				int index = getIndex(value);
				return (index < size && array[index] == value ? index : -1);
			}
			return -1;
		}
		for (int a = 0; a < size; a++)
		{
			if (array[a] == value)
			{
				return a;
			}
		}
		return -1;
	}

	public final void clear()
	{
		size = 0;
	}

	public byte getByteLength()
	{
		return byteLength;
	}

	public void setByteLength(int byteLength)
	{
		this.byteLength = (byte) byteLength;
	}

	private final int getIndex(long value)
	{
		boolean increasing = true;
		int startIndex = 0;
		long arrayValue;
		int maxIndex = size - 1;
		// if (size > 6)
		{
			long startValue = array[0], endValue = array[maxIndex];
			float fac;
			if (startValue >= value)
			{
				fac = 0;
			}
			else if (endValue <= value)
			{
				fac = 1;
			}
			else
			{
				fac = (endValue + startValue) / (float) (2 * value);
			}
			startIndex = (int) (fac * maxIndex);
			if (array[startIndex] > value)
			{
				increasing = false;
			}
		}
		if (increasing)
		{
			for (int a = startIndex; a < size; a++)
			{
				arrayValue = array[a];
				if (arrayValue >= value)
				{
					return a;
				}
			}
			return size;
		}
		else
		{
			for (int a = startIndex; a-- > 0;)
			{
				arrayValue = array[a];
				if (arrayValue <= value)
				{
					return a;
				}
			}
			return 0;
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		size = in.readInt();
		sorted = in.readBoolean();
		byteLength = in.readByte();
		if (array.length < size)
		{
			array = new long[size];
		}
		if (byteLength == 1)
		{
			for (int a = 0; a < size; a++)
			{
				array[a] = in.readByte();
			}
		}
		else if (byteLength == 2)
		{
			for (int a = 0; a < size; a++)
			{
				array[a] = in.readShort();
			}
		}
		else if (byteLength == 4)
		{
			for (int a = 0; a < size; a++)
			{
				array[a] = in.readInt();
			}
		}
		else
		{
			for (int a = 0; a < size; a++)
			{
				array[a] = in.readLong();
			}
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(size);
		out.writeBoolean(sorted);
		out.writeByte(byteLength);
		if (byteLength == 1)
		{
			for (int a = 0; a < size; a++)
			{
				out.writeByte((byte) array[a]);
			}
		}
		else if (byteLength == 2)
		{
			for (int a = 0; a < size; a++)
			{
				out.writeShort((short) array[a]);
			}
		}
		else if (byteLength == 4)
		{
			for (int a = 0; a < size; a++)
			{
				out.writeInt((int) array[a]);
			}
		}
		else
		{
			for (int a = 0; a < size; a++)
			{
				out.writeLong(array[a]);
			}
		}
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		toString(sb);
		return sb.toString();
	}

	@Override
	public void toString(StringBuilder sb)
	{
		sb.append(size).append(" items: [");
		for (int a = 0; a < size; a++)
		{
			if (a > 0)
			{
				sb.append(',');
			}
			sb.append(array[a]);
		}
		sb.append(']');
	}
}
