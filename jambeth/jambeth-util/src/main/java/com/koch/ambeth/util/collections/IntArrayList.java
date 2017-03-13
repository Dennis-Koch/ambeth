package com.koch.ambeth.util.collections;

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
public class IntArrayList implements Externalizable, IPrintable
{
	private byte byteLength = 4;

	private boolean sorted = false;

	public int[] array;

	public int size = 0;

	public IntArrayList()
	{
		this(10, false);
	}

	public IntArrayList(boolean sorted)
	{
		this(10, sorted);
	}

	public IntArrayList(final int incStep, final boolean isorted)
	{
		sorted = isorted;
		array = new int[incStep];
		size = 0;
	}

	private final void checkArraySize()
	{
		if (size == array.length - 1)
		{
			int[] buff = new int[array.length << 1];
			System.arraycopy(array, 0, buff, 0, size);
			array = buff;
		}
	}

	public final void add(final int value)
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

	public final boolean addIfNotExists(final int value)
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

	private final void addAtIndex(final int index, final int value)
	{
		System.arraycopy(array, index, array, index + 1, size - index);
		array[index] = value;
		size++;
	}

	public final int addGiveIndex(final int value)
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

	public final void remove(final int value)
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

	public final int removeAtIndexGiveValue(final int index)
	{
		final int value = array[index];
		for (int a = index; a < size; a++)
		{
			array[a] = array[a + 1];
		}
		size--;
		return value;
	}

	public final boolean removeIfExists(final int value)
	{
		return (removeGiveIndex(value) != -1);
	}

	public final int removeGiveIndex(final int value)
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

	public final boolean hasValue(final int value)
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

	public final int getFirstIndexWithValue(final int value)
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

	private final int getIndex(int value)
	{
		boolean increasing = true;
		int startIndex = 0, arrayValue, maxIndex = size - 1;
		// if (size > 6)
		{
			int startValue = array[0], endValue = array[maxIndex];
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
			array = new int[size];
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
		else
		{
			for (int a = 0; a < size; a++)
			{
				array[a] = in.readInt();
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
		else
		{
			for (int a = 0; a < size; a++)
			{
				out.writeInt(array[a]);
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
