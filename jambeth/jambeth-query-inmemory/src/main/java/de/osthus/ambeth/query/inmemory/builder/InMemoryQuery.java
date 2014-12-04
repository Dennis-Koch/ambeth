package de.osthus.ambeth.query.inmemory.builder;

import java.util.List;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
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
	public IQueryKey getQueryKey(IMap<Object, Object> nameToValueMap)
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
}
