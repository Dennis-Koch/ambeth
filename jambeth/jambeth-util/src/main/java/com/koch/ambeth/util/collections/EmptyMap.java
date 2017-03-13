package com.koch.ambeth.util.collections;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.koch.ambeth.util.IImmutableType;

public final class EmptyMap<K, V> implements ILinkedMap<K, V>, IImmutableType
{
	public static final class EmptyMapIterator<K, V> implements Iterator<Entry<K, V>>
	{
		@SuppressWarnings("rawtypes")
		private static final EmptyMapIterator iteratorInstance = new EmptyMapIterator();

		@SuppressWarnings("unchecked")
		static <K, V> EmptyMapIterator<K, V> emptyIterator()
		{
			return iteratorInstance;
		}

		private EmptyMapIterator()
		{
			// intended blank
		}

		@Override
		public boolean hasNext()
		{
			return false;
		}

		@Override
		public Entry<K, V> next()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

	}

	@SuppressWarnings("rawtypes")
	private static final EmptyMap instance = new EmptyMap();

	@SuppressWarnings("unchecked")
	public static <K, V> EmptyMap<K, V> emptyMap()
	{
		return instance;
	}

	private EmptyMap()
	{
		// intended blank
	}

	@Override
	public int size()
	{
		return 0;
	}

	@Override
	public boolean containsValue(Object value)
	{
		return false;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		throw new UnsupportedOperationException("Map is read-only");
	}

	@Override
	public Iterator<Entry<K, V>> iterator()
	{
		return EmptyMapIterator.emptyIterator();
	}

	@Override
	public Iterator<Entry<K, V>> iterator(boolean removeAllowed)
	{
		return EmptyMapIterator.emptyIterator();
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException("Map is read-only");
	}

	@Override
	public boolean containsKey(Object key)
	{
		return false;
	}

	@Override
	public ISet<Entry<K, V>> entrySet()
	{
		return null;
	}

	@Override
	public void entrySet(ISet<Entry<K, V>> targetEntrySet)
	{
		// intended blank
	}

	@Override
	public V get(Object key)
	{
		return null;
	}

	@Override
	public boolean isEmpty()
	{
		return true;
	}

	@Override
	public ISet<K> keySet()
	{
		return EmptySet.emptySet();
	}

	@Override
	public void keySet(Collection<K> targetKeySet)
	{
		// intended blank
	}

	@Override
	public IList<K> keyList()
	{
		return EmptyList.<K> getInstance();
	}

	@Override
	public V put(K key, V value)
	{
		throw new UnsupportedOperationException("Map is read-only");
	}

	@Override
	public V remove(Object key)
	{
		throw new UnsupportedOperationException("Map is read-only");
	}

	@Override
	public IList<V> values()
	{
		return EmptyList.<V> getInstance();
	}

	@Override
	public K getKey(K key)
	{
		return null;
	}

	@Override
	public boolean putIfNotExists(K key, V value)
	{
		throw new UnsupportedOperationException("Map is read-only");
	}

	@Override
	public boolean removeIfValue(K key, V value)
	{
		throw new UnsupportedOperationException("Map is read-only");
	}

	@SuppressWarnings("unchecked")
	@Override
	public V[] toArray(Class<V> arrayType)
	{
		return (V[]) Array.newInstance(arrayType, 0);
	}
}
