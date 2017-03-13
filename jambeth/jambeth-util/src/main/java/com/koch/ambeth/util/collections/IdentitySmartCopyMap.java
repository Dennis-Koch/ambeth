package com.koch.ambeth.util.collections;

public class IdentitySmartCopyMap<K, V> extends SmartCopyMap<K, V>
{
	public IdentitySmartCopyMap()
	{
		super();
	}

	public IdentitySmartCopyMap(float loadFactor)
	{
		super(loadFactor);
	}

	public IdentitySmartCopyMap(int initialCapacity, float loadFactor)
	{
		super(initialCapacity, loadFactor);
	}

	public IdentitySmartCopyMap(int initialCapacity)
	{
		super(initialCapacity);
	}

	@Override
	protected int extractHash(Object key)
	{
		return System.identityHashCode(key);
	}

	@Override
	protected boolean equalKeys(K key, IMapEntry<K, V> entry)
	{
		return key == entry.getKey();
	}

	@Override
	public ISet<K> keySet()
	{
		final IdentityLinkedSet<K> keySet = IdentityLinkedSet.<K> create(size());
		keySet(keySet);
		return keySet;
	}

	@Override
	public ISet<Entry<K, V>> entrySet()
	{
		final IdentityLinkedSet<Entry<K, V>> entrySet = IdentityLinkedSet.<Entry<K, V>> create(size());
		entrySet(entrySet);
		return entrySet;
	}
}
