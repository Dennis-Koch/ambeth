package com.koch.ambeth.query.inmemory.builder;

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
