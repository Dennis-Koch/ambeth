package com.koch.ambeth.cache.collections;

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

public class DefaultCacheMapEntry extends CacheMapEntry
{
	private final Class<?> entityType;
	private final byte idIndex;
	private Object id;

	public DefaultCacheMapEntry(Class<?> entityType, byte idIndex, Object id, Object value, CacheMapEntry nextEntry)
	{
		super(entityType, idIndex, id, value, nextEntry);
		this.entityType = entityType;
		this.idIndex = idIndex;
	}

	@Override
	public Object getId()
	{
		return id;
	}

	@Override
	protected void setId(Object id)
	{
		this.id = id;
	}

	@Override
	public Class<?> getEntityType()
	{
		return entityType;
	}

	@Override
	public byte getIdIndex()
	{
		return idIndex;
	}
}
