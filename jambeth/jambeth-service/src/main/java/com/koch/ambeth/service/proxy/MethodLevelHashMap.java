package com.koch.ambeth.service.proxy;

/*-
 * #%L
 * jambeth-service
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
