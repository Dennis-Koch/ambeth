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

import java.util.Collection;
import java.util.Set;

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
public class IdentityLinkedSet<K> extends LinkedHashSet<K>
{
	public static <K> IdentityLinkedSet<K> create(int size)
	{
		return create(size, DEFAULT_LOAD_FACTOR);
	}

	public static <K> IdentityLinkedSet<K> create(int size, float loadFactor)
	{
		return new IdentityLinkedSet<K>((int) (size / loadFactor) + 1, loadFactor);
	}

	public IdentityLinkedSet()
	{
		super();
	}

	public IdentityLinkedSet(Collection<? extends K> sourceCollection)
	{
		this((int) (sourceCollection.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR);
		addAll(sourceCollection);
	}

	public IdentityLinkedSet(K[] sourceArray)
	{
		this((int) (sourceArray.length / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR);
		addAll(sourceArray);
	}

	public IdentityLinkedSet(float loadFactor)
	{
		super(loadFactor);
	}

	public IdentityLinkedSet(int initialCapacity)
	{
		super(initialCapacity);
	}

	public IdentityLinkedSet(int initialCapacity, float loadFactor)
	{
		super(initialCapacity, loadFactor);
	}

	public IdentityLinkedSet(Set<? extends K> map)
	{
		super((int) (map.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_LOAD_FACTOR);
		addAll(map);
	}

	@Override
	protected int extractHash(K key)
	{
		return System.identityHashCode(key);
	}

	@Override
	protected boolean equalKeys(K key, ISetEntry<K> entry)
	{
		return key == entry.getKey();
	}
}
