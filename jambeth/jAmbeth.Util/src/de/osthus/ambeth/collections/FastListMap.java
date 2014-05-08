package de.osthus.ambeth.collections;

public class FastListMap<V> extends LinkedIntKeyMap<FastListEntry<V>>
{
	public FastListMap()
	{
	}

	public FastListMap(int initialCapacity)
	{
		super(initialCapacity);
	}

	public FastListMap(int initialCapacity, float loadFactor)
	{
		super(initialCapacity, loadFactor);
	}

	@Override
	protected FastListEntry<V> createEntry(int hash, int key, FastListEntry<V> value, LinkedIntKeyMapEntry<FastListEntry<V>> next)
	{
		value.initEntry(hash, key, value, next);
		return value;
	}
}
