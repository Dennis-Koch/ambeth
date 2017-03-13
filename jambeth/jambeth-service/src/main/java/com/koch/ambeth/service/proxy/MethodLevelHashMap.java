package com.koch.ambeth.service.proxy;

import java.util.Arrays;

import com.koch.ambeth.util.collections.Tuple2KeyEntry;
import com.koch.ambeth.util.collections.Tuple2KeyHashMap;

public class MethodLevelHashMap<T> extends Tuple2KeyHashMap<String, Class<?>[], T>
{
	public MethodLevelHashMap()
	{
		super();
	}

	public MethodLevelHashMap(float loadFactor)
	{
		super(loadFactor);
	}

	public MethodLevelHashMap(int initialCapacity, float loadFactor)
	{
		super(initialCapacity, loadFactor);
	}

	public MethodLevelHashMap(int initialCapacity)
	{
		super(initialCapacity);
	}

	@Override
	protected boolean equalKeys(String key1, Class<?>[] key2, Tuple2KeyEntry<String, Class<?>[], T> entry)
	{
		return key1.equals(entry.getKey1()) && Arrays.equals(key2, entry.getKey2());
	}

	@Override
	protected int extractHash(String key1, Class<?>[] key2)
	{
		return key1.hashCode() ^ Arrays.hashCode(key2);
	}
}