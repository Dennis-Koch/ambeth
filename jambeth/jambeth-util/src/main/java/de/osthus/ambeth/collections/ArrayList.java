package de.osthus.ambeth.collections;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import de.osthus.ambeth.util.IPrintable;
import de.osthus.ambeth.util.StringBuilderUtil;

public class ArrayList<V> implements List<V>, IList<V>, Externalizable, IPrintable
{
	public static class FastIterator<V> implements ListIterator<V>
	{
		protected int index = -1;

		protected final ArrayList<V> list;

		public FastIterator(ArrayList<V> list)
		{
			this.list = list;
		}

		@Override
		public boolean hasNext()
		{
			return list.size() > index + 1;
		}

		@Override
		public V next()
		{
			return list.get(++index);
		}

		@Override
		public void remove()
		{
			list.remove(index--);
		}

		@Override
		public void add(final V o)
		{
			list.add(index++, o);
		}

		@Override
		public boolean hasPrevious()
		{
			return index > 0;
		}

		@Override
		public int nextIndex()
		{
			return index + 1;
		}

		@Override
		public V previous()
		{
			return list.get(--index);
		}

		@Override
		public int previousIndex()
		{
			return index - 1;
		}

		@Override
		public void set(final V o)
		{
			list.set(index, o);
		}
	}

	private static final Object[] emptyArray = new Object[0];

	protected Object[] array = emptyArray;

	protected int size;

	public ArrayList()
	{
		this(10);
	}

	public ArrayList(final Collection<V> coll)
	{
		init(coll.toArray(), coll.size());
	}

	public ArrayList(final Iterable<V> coll)
	{
		this(10);
		for (V item : coll)
		{
			add(item);
		}
	}

	public ArrayList(final Object[] array)
	{
		init(array, array.length);
	}

	public ArrayList(final int iincStep)
	{
		init(new Object[iincStep], 0);
	}

	protected void init(final Object[] array, final int size)
	{
		this.array = array;
		this.size = size;
	}

	@Override
	public final boolean add(final V value)
	{
		int size = this.size;
		Object[] array = this.array;
		if (size == array.length)
		{
			final Object[] buff = new Object[(array.length << 1) + 7];
			System.arraycopy(array, 0, buff, 0, size);
			array = buff;
			this.array = array;
		}
		array[size++] = value;
		this.size = size;
		return true;
	}

	@Override
	public final boolean remove(final Object value)
	{
		int size = this.size;
		Object[] array = this.array;
		if (value == null)
		{
			for (int a = 0; a < size; a++)
			{
				if (array[a] == null)
				{
					removeAtIndex(a);
					return true;
				}
			}
		}
		else
		{
			for (int a = 0; a < size; a++)
			{
				final Object item = array[a];
				if (value.equals(item))
				{
					removeAtIndex(a);
					return true;
				}
			}
		}
		return false;
	}

	public final boolean hasValue(final V value)
	{
		int size = this.size;
		Object[] array = this.array;
		if (value == null)
		{
			for (int a = 0; a < size; a++)
			{
				if (array[a] == null)
				{
					return true;
				}
			}
		}
		else
		{
			for (int a = 0; a < size; a++)
			{
				final Object item = array[a];
				if (value.equals(item))
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public V get(final int index)
	{
		return (V) array[index];
	}

	@SuppressWarnings("unchecked")
	public final V peek()
	{
		int size = this.size;
		if (size > 0)
		{
			return (V) array[size - 1];
		}
		else
		{
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public final V popLastElement()
	{
		int size = this.size;
		if (size > 0)
		{
			Object[] array = this.array;
			final V elem = (V) array[--size];
			array[size] = null;
			this.size = size;
			return elem;
		}
		else
		{
			return null;
		}
	}

	public final void clearFrom(final int index)
	{
		int size = this.size;
		Object[] array = this.array;
		for (int a = index; a < size; a++)
		{
			array[a] = null;
		}
		this.size = index;
	}

	@Override
	public final int size()
	{
		return size;
	}

	@Override
	public final void clear()
	{
		clearFrom(0);
	}

	@SuppressWarnings("unchecked")
	public final void copyInto(final ArrayList<V> otherList)
	{
		otherList.size = 0;
		int size = this.size;
		Object[] array = this.array;
		for (int a = 0; a < size; a++)
		{
			otherList.add((V) array[a]);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public V remove(final int index)
	{
		final V object = (V) array[index];
		removeAtIndex(index);
		return object;
	}

	public void removeAtIndex(final int index)
	{
		int size = this.size;
		Object[] array = this.array;
		for (int a = index, sizeA = size - 1; a < sizeA; a++)
		{
			array[a] = array[a + 1];
		}
		size--;
		this.size = size;
		array[size] = null;
	}

	@Override
	public void add(final int index, final V element)
	{
		int size = this.size;
		Object[] array = this.array;
		if (size == array.length)
		{
			final Object[] buff = new Object[(array.length << 1) + 7];
			System.arraycopy(array, 0, buff, 0, size);
			array = buff;
			this.array = array;
		}
		for (int a = size + 1, i = index + 1; a-- > i;)
		{
			array[a] = array[a - 1];
		}
		array[index] = element;
		size++;
		this.size = size;
	}

	@Override
	public boolean addAll(final Collection<? extends V> c)
	{
		if (c instanceof List)
		{
			final List<? extends V> list = (List<? extends V>) c;

			final int listSize = list.size();
			int size = this.size;
			Object[] array = this.array;
			if (size + listSize > array.length)
			{
				final int sizeNeeded = size + listSize;
				int newSize = array.length << 1;
				if (newSize == 0)
				{
					newSize = 1;
				}
				while (newSize < sizeNeeded)
				{
					newSize = newSize << 1;
				}
				final Object[] buff = new Object[newSize + 7];
				System.arraycopy(array, 0, buff, 0, size);
				array = buff;
				this.array = array;
			}

			for (int a = 0, sizeA = list.size(); a < sizeA; a++)
			{
				array[size++] = list.get(a);
			}
			this.size = size;
		}
		else
		{
			final Iterator<? extends V> iter = c.iterator();
			while (iter.hasNext())
			{
				add(iter.next());
			}
		}
		return c.size() > 0;
	}

	@Override
	public <T extends V> boolean addAll(final T[] externArray)
	{
		if (externArray == null)
		{
			return false;
		}
		int size = this.size;
		Object[] array = this.array;

		final int listSize = externArray.length;
		if (size + listSize > array.length)
		{
			final int sizeNeeded = size + listSize;
			int newSize = array.length << 1;
			while (newSize < sizeNeeded)
			{
				newSize = newSize << 1;
			}
			final Object[] buff = new Object[newSize + 7];
			System.arraycopy(array, 0, buff, 0, size);
			array = buff;
			this.array = array;
		}

		for (T item : externArray)
		{
			array[size++] = item;
		}
		this.size = size;
		return externArray.length > 0;
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends V> c)
	{
		int currIndex = index;
		for (V item : c)
		{
			add(currIndex, item);
			currIndex++;
		}
		return c.size() > 0;
	}

	@Override
	public boolean contains(final Object o)
	{
		return indexOf(o) >= 0;
	}

	@Override
	public boolean containsAll(final Collection<?> c)
	{
		Iterator<?> iter = c.iterator();
		while (iter.hasNext())
		{
			Object item = iter.next();
			if (!contains(item))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public int indexOf(final Object o)
	{
		int size = this.size;
		Object[] array = this.array;
		for (int a = 0; a < size; a++)
		{
			final Object item = array[a];
			if (o == null)
			{
				if (item == null)
				{
					return a;
				}
			}
			else if (o.equals(item))
			{
				return a;
			}
		}
		return -1;
	}

	@Override
	public boolean isEmpty()
	{
		return size == 0;
	}

	@Override
	public Iterator<V> iterator()
	{
		return new FastIterator<V>(this);
	}

	@Override
	public int lastIndexOf(final Object o)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<V> listIterator()
	{
		return new FastIterator<V>(this);
	}

	@Override
	public ListIterator<V> listIterator(final int index)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean removeAll(final Collection<?> c)
	{
		boolean oneRemoved = false;
		if (c instanceof List)
		{
			final List list = (List) c;
			for (int a = list.size(); a-- > 0;)
			{
				if (remove(list.get(a)))
				{
					oneRemoved = true;
				}
			}
			return oneRemoved;
		}
		final Iterator<?> iter = c.iterator();
		while (iter.hasNext())
		{
			if (remove(iter.next()))
			{
				oneRemoved = true;
			}
		}
		return oneRemoved;
	}

	@Override
	public boolean retainAll(final Collection<?> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("unchecked")
	public V set(final int index, final V element)
	{
		Object[] array = this.array;
		final V oldElement = (V) array[index];
		array[index] = element;
		return oldElement;
	}

	@Override
	@SuppressWarnings("unchecked")
	public IList<V> subList(final int fromIndex, final int toIndex)
	{
		Object[] array = this.array;
		final ArrayList<V> sublist = new ArrayList<V>(toIndex - fromIndex);
		for (int a = fromIndex; a < toIndex; a++)
		{
			sublist.add((V) array[a]);
		}
		return sublist;
	}

	@Override
	public Object[] toArray()
	{
		return toArray(new Object[size]);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(final T[] targetArray)
	{
		int size = this.size;
		Object[] array = this.array;
		for (int a = size; a-- > 0;)
		{
			targetArray[a] = (T) array[a];
		}
		return targetArray;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(final Class<T> componentType)
	{
		T[] array = (T[]) Array.newInstance(componentType, size());
		return toArray(array);
	}

	@Override
	public void readExternal(final ObjectInput arg0) throws IOException, ClassNotFoundException
	{
		int size = arg0.readInt();
		Object[] array = null;
		if (size > 0)
		{
			array = new Object[size];
			for (int a = 0; a < size; a++)
			{
				array[a] = arg0.readObject();
			}
		}
		else
		{
			array = new Object[0];
		}
		this.array = array;
		this.size = size;
	}

	@Override
	public void writeExternal(final ObjectOutput arg0) throws IOException
	{
		int size = this.size;
		Object[] array = this.array;
		arg0.writeInt(size);
		for (int a = 0; a < size; a++)
		{
			arg0.writeObject(array[a]);
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
		sb.append(size()).append(" items: [");
		for (int a = 0, size = size(); a < size; a++)
		{
			if (a > 0)
			{
				sb.append(',');
			}
			StringBuilderUtil.appendPrintable(sb, get(a));
		}
		sb.append(']');
	}

	@SuppressWarnings("unchecked")
	public <T> T[] getBackingArray()
	{
		return (T[]) array;
	}
}
