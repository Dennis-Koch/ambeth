package de.osthus.ambeth.collections;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ReadOnlyMapWrapper<K, V> implements IMap<K, V>, ILinkedMap<K, V>
{
	protected IMap<K, V> hashMap;

	protected ILinkedMap<K, V> linkedHashMap;

	public ReadOnlyMapWrapper(IMap<K, V> map)
	{
		if (map instanceof ILinkedMap)
		{
			linkedHashMap = (ILinkedMap<K, V>) map;
		}

		hashMap = map;
	}

	@Override
	public int size()
	{
		return hashMap.size();
	}

	@Override
	public boolean isEmpty()
	{
		return hashMap.isEmpty();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return hashMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return hashMap.containsValue(value);
	}

	@Override
	public V get(Object key)
	{
		return hashMap.get(key);
	}

	@Override
	public K getKey(K key)
	{
		return hashMap.getKey(key);
	}

	@Override
	public V put(K key, V value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public V remove(Object key)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<Entry<K, V>> iterator()
	{
		return linkedHashMap.iterator(false);
	}

	@Override
	public Iterator<Entry<K, V>> iterator(boolean removeAllowed)
	{
		return iterator();
	}

	@Override
	public void toKeysList(List<K> list)
	{
		linkedHashMap.toKeysList(list);
	}

	@Override
	public ISet<Entry<K, V>> entrySet()
	{
		return hashMap.entrySet();
	}

	@Override
	public void entrySet(ISet<Entry<K, V>> targetEntrySet)
	{
		hashMap.entrySet(targetEntrySet);
	}

	@Override
	public ISet<K> keySet()
	{
		return hashMap.keySet();
	}

	@Override
	public void keySet(ISet<K> targetKeySet)
	{
		hashMap.keySet(targetKeySet);
	}

	@Override
	public V[] toArray(Class<V> arrayType)
	{
		return hashMap.toArray(arrayType);
	}

	@Override
	public IList<V> values()
	{
		return hashMap.values();
	}

	@Override
	public boolean putIfNotExists(K key, V value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeIfValue(K key, V value)
	{
		throw new UnsupportedOperationException();
	}
}
