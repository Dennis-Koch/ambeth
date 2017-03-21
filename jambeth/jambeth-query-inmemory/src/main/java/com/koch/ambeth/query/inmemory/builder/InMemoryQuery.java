package com.koch.ambeth.query.inmemory.builder;

/*-
 * #%L
 * jambeth-query-inmemory
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

import java.util.List;

import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryIntern;
import com.koch.ambeth.query.IQueryKey;
import com.koch.ambeth.query.StatefulQuery;
import com.koch.ambeth.query.persistence.IDataCursor;
import com.koch.ambeth.query.persistence.IEntityCursor;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;

public class InMemoryQuery<T> implements IQuery<T>, IQueryIntern<T>
{
	protected final Class<T> entityType;

	public InMemoryQuery(Class<T> entityType)
	{
		this.entityType = entityType;

	}

	@Override
	public void dispose()
	{
		// Intended blank
	}

	@Override
	public Class<T> getEntityType()
	{
		return entityType;
	}

	@Override
	public void fillRelatedEntityTypes(List<Class<?>> relatedEntityTypes)
	{
	}

	@Override
	public IQueryKey getQueryKey(IMap<Object, Object> nameToValueMap)
	{
		return null;
	}

	@Override
	public IVersionCursor retrieveAsVersions()
	{
		return null;
	}

	@Override
	public IDataCursor retrieveAsData()
	{
		return null;
	}

	@Override
	public IVersionCursor retrieveAsVersions(boolean retrieveAlternateIds)
	{
		return null;
	}

	@Override
	public IVersionCursor retrieveAsVersions(IMap<Object, Object> paramNameToValueMap, boolean retrieveAlternateIds)
	{
		return null;
	}

	@Override
	public IVersionCursor retrieveAsVersions(IMap<Object, Object> nameToValueMap)
	{
		return null;
	}

	@Override
	public IEntityCursor<T> retrieveAsCursor()
	{
		return null;
	}

	@Override
	public IEntityCursor<T> retrieveAsCursor(IMap<Object, Object> nameToValueMap)
	{
		return null;
	}

	@Override
	public IList<T> retrieve()
	{
		return null;
	}

	@Override
	public T retrieveSingle()
	{
		return null;
	}

	@Override
	public IList<T> retrieve(IMap<Object, Object> nameToValueMap)
	{
		return null;
	}

	@Override
	public IQuery<T> param(Object paramKey, Object param)
	{
		StatefulQuery<T> statefulQuery = new StatefulQuery<T>(this);
		return statefulQuery.param(paramKey, param);
	}

	@Override
	public IDataCursor retrieveAsData(IMap<Object, Object> nameToValueMap)
	{
		return null;
	}

	@Override
	public long count()
	{
		return 0;
	}

	@Override
	public long count(IMap<Object, Object> paramNameToValueMap)
	{
		return 0;
	}

	@Override
	public boolean isEmpty()
	{
		return true;
	}

	@Override
	public boolean isEmpty(IMap<Object, Object> paramNameToValueMap)
	{
		return true;
	}
}
