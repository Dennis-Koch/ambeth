package de.osthus.ambeth.collections;

import java.util.Collection;
import java.util.Iterator;

public class ReadOnlySetWrapper<K> implements ISet<K>
{
	protected ISet<K> hashSet;

	protected ILinkedSet<K> linkedHashSet;

	public ReadOnlySetWrapper(ISet<K> set)
	{
		if (set instanceof ILinkedSet)
		{
			linkedHashSet = (ILinkedSet<K>) set;
		}

		hashSet = set;
	}

	@Override
	public int size()
	{
		return hashSet.size();
	}

	@Override
	public K get(K key)
	{
		return hashSet.get(key);
	}

	@Override
	public boolean isEmpty()
	{
		return hashSet.isEmpty();
	}

	@Override
	public boolean contains(Object o)
	{
		return hashSet.contains(o);
	}

	@Override
	public Iterator<K> iterator()
	{
		return hashSet.iterator(false);
	}

	@Override
	public Iterator<K> iterator(boolean removeAllowed)
	{
		return iterator();
	}

	@Override
	public Object[] toArray()
	{
		return hashSet.toArray();
	}

	@Override
	public <T> T[] toArray(Class<T> componentType)
	{
		return hashSet.toArray(componentType);
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return hashSet.toArray(a);
	}

	@Override
	public boolean add(K e)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <S extends K> boolean addAll(S[] array)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <S extends K> boolean removeAll(S[] array)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		return hashSet.containsAll(c);
	}

	@Override
	public boolean containsAny(Collection<?> coll)
	{
		return hashSet.containsAny(coll);
	}

	@Override
	public boolean addAll(Iterable<? extends K> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends K> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public K removeAndGet(K key)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IList<K> toList()
	{
		return hashSet.toList();
	}

	@Override
	public void toList(Collection<K> targetList)
	{
		hashSet.toList(targetList);
	}
}
