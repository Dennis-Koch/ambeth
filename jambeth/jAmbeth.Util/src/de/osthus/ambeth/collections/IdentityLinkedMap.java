package de.osthus.ambeth.collections;

import java.util.Map;

/**
 * Eine Identity-Variante der LinkedMap
 * 
 * @author kochd
 * 
 * @param <K>
 *            Der Typ der in der Map enthaltenen Keys
 * @param <V>
 *            Der Typ der in der Map enthaltenen Values
 */
public class IdentityLinkedMap<K, V> extends LinkedHashMap<K, V>
{
	public static <K, V> IdentityLinkedMap<K, V> create(int size)
	{
		return create(size, DEFAULT_LOAD_FACTOR);
	}

	public static <K, V> IdentityLinkedMap<K, V> create(int size, float loadFactor)
	{
		return new IdentityLinkedMap<K, V>((int) (size / loadFactor) + 1, loadFactor);
	}

	public IdentityLinkedMap()
	{
		super();
	}

	public IdentityLinkedMap(float loadFactor)
	{
		super(loadFactor);
	}

	public IdentityLinkedMap(int initialCapacity)
	{
		super(initialCapacity);
	}

	public IdentityLinkedMap(int initialCapacity, float loadFactor)
	{
		super(initialCapacity, loadFactor);
	}

	public IdentityLinkedMap(Map<? extends K, ? extends V> map)
	{
		super((int) (map.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR);
		putAll(map);
	}

	@Override
	protected int extractHash(K key)
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
