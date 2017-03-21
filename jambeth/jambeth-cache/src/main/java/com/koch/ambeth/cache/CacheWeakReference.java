package com.koch.ambeth.cache;

/*-
 * #%L
 * jambeth-cache
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

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

import com.koch.ambeth.cache.collections.CacheMapEntry;
import com.koch.ambeth.cache.collections.ICacheMapEntryAware;

public class CacheWeakReference<V> extends WeakReference<V> implements ICacheReference, IParentCacheValueHardRef, ICacheMapEntryAware
{
	@SuppressWarnings("unused")
	private Object parentCacheValueHardRef;

	private CacheMapEntry cacheMapEntry;

	public CacheWeakReference(V referent, ReferenceQueue<V> queue)
	{
		super(referent, queue);
	}

	@Override
	public void setCacheMapEntry(CacheMapEntry cacheMapEntry)
	{
		this.cacheMapEntry = cacheMapEntry;
	}

	@Override
	public Class<?> getEntityType()
	{
		return cacheMapEntry.getEntityType();
	}

	@Override
	public byte getIdIndex()
	{
		return cacheMapEntry.getIdIndex();
	}

	@Override
	public Object getId()
	{
		return cacheMapEntry.getId();
	}

	@Override
	public void setParentCacheValueHardRef(Object parentCacheValueHardRef)
	{
		this.parentCacheValueHardRef = parentCacheValueHardRef;
	}
}
