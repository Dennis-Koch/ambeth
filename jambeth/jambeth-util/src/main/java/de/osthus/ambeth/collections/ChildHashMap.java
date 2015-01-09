package de.osthus.ambeth.collections;

public class ChildHashMap<K, V> extends HashMap<K, V>
{
	protected final HashMap<K, V> parentMap;

	public ChildHashMap(HashMap<K, V> parentMap)
	{
		this.parentMap = parentMap;
	}

	@Override
	public K getKey(K key)
	{
		K resolvedKey = super.getKey(key);
		if (resolvedKey != null)
		{
			return resolvedKey;
		}
		return parentMap.getKey(key);
	}

	@Override
	public V get(Object key)
	{
		V value = super.get(key);
		if (value != null)
		{
			return value;
		}
		if (super.containsKey(key))
		{
			return null;
		}
		return parentMap.get(key);
	}

	@Override
	public boolean containsKey(Object key)
	{
		if (super.containsKey(key))
		{
			return true;
		}
		return parentMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		if (super.containsValue(value))
		{
			return true;
		}
		return parentMap.containsValue(value);
	}

	@Override
	public boolean isEmpty()
	{
		if (!super.isEmpty())
		{
			return false;
		}
		return parentMap.isEmpty();
	}

	@Override
	public boolean putIfNotExists(K key, V value)
	{
		if (parentMap.containsKey(key))
		{
			return false;
		}
		return super.putIfNotExists(key, value);
	}

	@Override
	public int size()
	{
		return super.size() + parentMap.size();
	}
}
