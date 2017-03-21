package com.koch.ambeth.util.collections;

/*-
 * #%L
 * jambeth-util
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

/**
 * 66 percent faster compared to a normal HashMap with a Tuple2 (Composite-)Key as the Map-Key. This is due to the fact that there is no need to instantiate
 * Tuple2 Keys for put() or get() operations. Of course the overall memory footprint is also the half compared to a normal map: There is only the entry object
 * with 2 key-fields compared to the entry object compared to 1 key-field which contains a Tuple2 Key instance
 * 
 * @param <Key1>
 * @param <Key2>
 * @param <V>
 */
public class Tuple4KeyHashMap<Key1, Key2, Key3, Key4, V> extends AbstractTuple4KeyHashMap<Key1, Key2, Key3, Key4, V>
{
	public static <Key1, Key2, Key3, Key4, V> Tuple4KeyHashMap<Key1, Key2, Key3, Key4, V> create(int size)
	{
		return create(size, DEFAULT_LOAD_FACTOR);
	}

	public static <Key1, Key2, Key3, Key4, V> Tuple4KeyHashMap<Key1, Key2, Key3, Key4, V> create(int size, float loadFactor)
	{
		return new Tuple4KeyHashMap<Key1, Key2, Key3, Key4, V>((int) (size / loadFactor) + 1, loadFactor);
	}

	protected int size;

	public Tuple4KeyHashMap()
	{
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
	}

	public Tuple4KeyHashMap(float loadFactor)
	{
		this(DEFAULT_INITIAL_CAPACITY, loadFactor);
	}

	public Tuple4KeyHashMap(int initialCapacity)
	{
		this(initialCapacity, DEFAULT_LOAD_FACTOR);
	}

	public Tuple4KeyHashMap(int initialCapacity, float loadFactor)
	{
		super(initialCapacity, loadFactor);
	}

	public Tuple4KeyHashMap(AbstractTuple4KeyHashMap<? extends Key1, ? extends Key2, ? extends Key3, ? extends Key4, ? extends V> map)
	{
		this((int) (map.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR);
		putAll(map);
	}

	@Override
	protected Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> createEntry(int hash, Key1 key1, Key2 key2, Key3 key3, Key4 key4, V value,
			final Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> nextEntry)
	{
		return new Tuple4KeyEntry<Key1, Key2, Key3, Key4, V>(key1, key2, key3, key4, value, hash, nextEntry);
	}

	@Override
	protected void entryAdded(final Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> entry)
	{
		size++;
	}

	@Override
	protected void entryRemoved(final Tuple4KeyEntry<Key1, Key2, Key3, Key4, V> entry)
	{
		size--;
	}

	@Override
	public int size()
	{
		return size;
	}
}
