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
