package de.osthus.ambeth.query;

import java.util.List;
import java.util.Map;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.persistence.IDataCursor;
import de.osthus.ambeth.persistence.IEntityCursor;
import de.osthus.ambeth.persistence.IVersionCursor;
import de.osthus.ambeth.persistence.IVersionItem;

public class QueryWeakReference<T> implements IQuery<T>
{
	protected IQuery<T> query;

	public QueryWeakReference(IQuery<T> query)
	{
		this.query = query;
	}

	@Override
	protected void finalize() throws Throwable
	{
		if (query != null)
		{
			query.dispose();
			query = null;
		}
	}

	@Override
	public void dispose()
	{
		if (query != null)
		{
			query.dispose();
			query = null;
		}
	}

	@Override
	public Class<T> getEntityType()
	{
		return query.getEntityType();
	}

	@Override
	public void fillRelatedEntityTypes(List<Class<?>> relatedEntityTypes)
	{
		query.fillRelatedEntityTypes(relatedEntityTypes);
	}

	@Override
	public IQueryKey getQueryKey(Map<Object, Object> nameToValueMap)
	{
		return query.getQueryKey(nameToValueMap);
	}

	@Override
	public IVersionCursor retrieveAsVersions()
	{
		return query.retrieveAsVersions();
	}

	@Override
	public IDataCursor retrieveAsData()
	{
		return query.retrieveAsData();
	}

	@Override
	@Deprecated
	public IVersionCursor retrieveAsVersions(Map<Object, Object> nameToValueMap)
	{
		return query.retrieveAsVersions(nameToValueMap);
	}

	@Override
	public IEntityCursor<T> retrieveAsCursor()
	{
		return query.retrieveAsCursor();
	}

	@Override
	@Deprecated
	public IEntityCursor<T> retrieveAsCursor(Map<Object, Object> nameToValueMap)
	{
		return query.retrieveAsCursor(nameToValueMap);
	}

	@Override
	public IList<T> retrieve()
	{
		return query.retrieve();
	}

	@Override
	@Deprecated
	public IList<T> retrieve(Map<Object, Object> nameToValueMap)
	{
		return query.retrieve(nameToValueMap);
	}

	@Override
	public IVersionItem retrieveAsVersion()
	{
		return query.retrieveAsVersion();
	}

	@Override
	public T retrieveSingle()
	{
		return query.retrieveSingle();
	}

	@Override
	public IQuery<T> param(Object paramKey, Object param)
	{
		IQuery<T> resultQuery = query.param(paramKey, param);
		if (resultQuery == query)
		{
			// Query instance is the same, so our weakreference may remain the same, too
			return this;
		}
		return resultQuery;
	}
}
