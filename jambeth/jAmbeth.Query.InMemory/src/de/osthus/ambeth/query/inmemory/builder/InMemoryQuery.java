package de.osthus.ambeth.query.inmemory.builder;

import java.util.List;
import java.util.Map;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.persistence.IDataCursor;
import de.osthus.ambeth.persistence.IEntityCursor;
import de.osthus.ambeth.persistence.IVersionCursor;
import de.osthus.ambeth.persistence.IVersionItem;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryIntern;
import de.osthus.ambeth.query.IQueryKey;
import de.osthus.ambeth.query.StatefulQuery;

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
	public IQueryKey getQueryKey(Map<Object, Object> nameToValueMap)
	{
		return null;
	}

	@Override
	public IVersionItem retrieveAsVersion()
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
	public IVersionCursor retrieveAsVersions(Map<Object, Object> nameToValueMap)
	{
		return null;
	}

	@Override
	public IEntityCursor<T> retrieveAsCursor()
	{
		return null;
	}

	@Override
	public IEntityCursor<T> retrieveAsCursor(Map<Object, Object> nameToValueMap)
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
	public IList<T> retrieve(Map<Object, Object> nameToValueMap)
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
	public IDataCursor retrieveAsData(Map<Object, Object> nameToValueMap)
	{
		return null;
	}
}
