package com.koch.ambeth.util.collections;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class ReadOnlyList<V> implements IList<V>, Serializable
{
	private static final long serialVersionUID = -9172592637280733333L;

	public static class ReadOnlyIter<V> implements ListIterator<V>
	{
		private final ReadOnlyList<V> list;

		private int currIndex;

		public ReadOnlyIter(ReadOnlyList<V> list)
		{
			this.list = list;
		}

		@Override
		public boolean hasNext()
		{
			return list.size > currIndex;
		}

		@Override
		public V next()
		{
			return list.get(currIndex++);
		}

		@Override
		public boolean hasPrevious()
		{
			return currIndex > 0;
		}

		@Override
		public int nextIndex()
		{
			return currIndex + 1;
		}

		@Override
		public V previous()
		{
			return list.get(--currIndex);
		}

		@Override
		public int previousIndex()
		{
			return currIndex - 1;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void add(V arg0)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void set(V arg0)
		{
			throw new UnsupportedOperationException();
		}
	}

	private static final Object[] emptyArray = new Object[0];

	protected Object[] array;

	protected int size;

	public ReadOnlyList()
	{
		// Intended blank
	}

	public ReadOnlyList(Collection<V> objects)
	{
		if (objects == null || objects.size() == 0)
		{
			array = emptyArray;
			size = 0;
		}
		else
		{
			array = new Object[objects.size()];
			Iterator<V> iter = objects.iterator();
			int tempSize = 0;
			while (iter.hasNext())
			{
				V item = iter.next();
				array[tempSize++] = item;
			}
			size = tempSize;
		}
	}

	public ReadOnlyList(V object)
	{
		array = new Object[1];
		array[0] = object;
		size = 1;
	}

	public ReadOnlyList(V[] objects)
	{
		array = objects;
		size = objects.length;
	}

	public ReadOnlyList(List<V> objects, int startIndex, int endIndex)
	{
		if (objects == null || objects.size() == 0)
		{
			array = emptyArray;
			size = 0;
		}
		else
		{
			if (endIndex < startIndex)
			{
				throw new IllegalArgumentException("endIndex must be >= startIndex");
			}
			int length = endIndex - startIndex;
			array = new Object[length];
			int tempSize = 0;
			for (int a = startIndex; a <= endIndex; a++)
			{
				array[tempSize++] = objects.get(a);
			}
			size = tempSize;
		}
	}

	public boolean hasValue(final V value)
	{
		for (int a = 0; a < size; a++)
		{
			if (array[a] == value)
			{
				return true;
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

	@Override
	public int size()
	{
		return size;
	}

	@SuppressWarnings("unchecked")
	public void copyInto(final ArrayList<V> otherList)
	{
		otherList.size = 0;
		for (int a = 0; a < size; a++)
		{
			otherList.add((V) array[a]);
		}
	}

	@Override
	public boolean contains(Object o)
	{
		return indexOf(o) != -1;
	}

	@Override
	public boolean isEmpty()
	{
		return size() == 0;
	}

	@Override
	public Object[] toArray()
	{
		Object[] targetArray = new Object[size()];
		return toArray(targetArray);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] array)
	{
		for (int a = size(); a-- > 0;)
		{
			array[a] = (T) this.array[a];
		}
		return array;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(Class<T> componentType)
	{
		return toArray((T[]) Array.newInstance(componentType, size()));
	}

	@Override
	public int indexOf(Object o)
	{
		if (o == null)
		{
			for (int a = 0, size = size(); a < size; a++)
			{
				Object item = array[a];
				if (item == null)
				{
					return a;
				}
			}
		}
		else
		{
			for (int a = 0, size = size(); a < size; a++)
			{
				Object item = array[a];
				if (item != null && item.equals(o))
				{
					return a;
				}
			}
		}
		return -1;
	}

	@Override
	public int lastIndexOf(Object o)
	{
		if (o == null)
		{
			for (int a = size(); a-- > 0;)
			{
				Object item = array[a];
				if (item == null)
				{
					return a;
				}
			}
		}
		else
		{
			for (int a = size(); a-- > 0;)
			{
				Object item = array[a];
				if (item != null && item.equals(o))
				{
					return a;
				}
			}
		}
		return -1;
	}

	@Override
	public Iterator<V> iterator()
	{
		return listIterator();
	}

	@Override
	public ListIterator<V> listIterator()
	{
		return new ReadOnlyIter<V>(this);
	}

	@Override
	public IList<V> subList(int startIndex, int endIndex)
	{
		return new ReadOnlyList<V>(this, startIndex, endIndex);
	}

	@Override
	public ListIterator<V> listIterator(int arg0)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean add(V arg0)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int arg0, V arg1)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends V> arg0)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends V> arg1)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends V> boolean addAll(T[] array)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> arg0)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public V peek()
	{
		if (size() > 0)
		{
			return get(size() - 1);
		}
		return null;
	}

	@Override
	public V popLastElement()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object arg0)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public V remove(int arg0)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> arg0)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <T extends V> boolean removeAll(T[] array)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> arg0)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public V set(int arg0, V arg1)
	{
		throw new UnsupportedOperationException();
	}
}
