package com.koch.ambeth.util.collections;

import java.util.Map;

public class IdentityWeakHashMap<K, V> extends WeakHashMap<K, V>
{
	public static <K, V> IdentityWeakHashMap<K, V> create(int size)
	{
		return create(size, DEFAULT_LOAD_FACTOR);
	}

	public static <K, V> IdentityWeakHashMap<K, V> create(int size, float loadFactor)
	{
		return new IdentityWeakHashMap<K, V>((int) (size / loadFactor) + 1, loadFactor);
	}

	public IdentityWeakHashMap()
	{
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
	}

	public IdentityWeakHashMap(float loadFactor)
	{
		this(DEFAULT_INITIAL_CAPACITY, loadFactor);
	}

	public IdentityWeakHashMap(int initialCapacity)
	{
		this(initialCapacity, DEFAULT_LOAD_FACTOR);
	}

	public IdentityWeakHashMap(int initialCapacity, float loadFactor)
	{
		super(initialCapacity, loadFactor, WeakMapEntry.class);
	}

	public IdentityWeakHashMap(Map<? extends K, ? extends V> map)
	{
		super((int) (map.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR, WeakMapEntry.class);
		putAll(map);
	}

	@Override
	protected int extractHash(K key)
	{
		return System.identityHashCode(key);
	}

	@Override
	protected boolean equalKeys(K key, final IMapEntry<K, V> entry)
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
