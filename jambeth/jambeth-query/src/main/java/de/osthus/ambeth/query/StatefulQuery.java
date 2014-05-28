package de.osthus.ambeth.query;

import java.util.List;
import java.util.Map;

import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.persistence.IDataCursor;
import de.osthus.ambeth.persistence.IEntityCursor;
import de.osthus.ambeth.persistence.IVersionCursor;
import de.osthus.ambeth.persistence.IVersionItem;

public class StatefulQuery<T> implements IQuery<T>
{
	protected IQueryIntern<T> query;

	protected final HashMap<Object, Object> paramMap = new HashMap<Object, Object>();

	public StatefulQuery(IQueryIntern<T> query)
	{
		this.query = query;
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
	public void dispose()
	{
		query = null;
		paramMap.clear();
	}

	@Override
	public IVersionCursor retrieveAsVersions()
	{
		return query.retrieveAsVersions(paramMap);
	}

	@Override
	public IDataCursor retrieveAsData()
	{
		return query.retrieveAsData(paramMap);
	}

	@Override
	public IVersionCursor retrieveAsVersions(Map<Object, Object> nameToValueMap)
	{
		throw new UnsupportedOperationException("Only retrieveAsVersions() allowed");
	}

	@Override
	public IEntityCursor<T> retrieveAsCursor()
	{
		return query.retrieveAsCursor(paramMap);
	}

	@Override
	public IEntityCursor<T> retrieveAsCursor(Map<Object, Object> nameToValueMap)
	{
		throw new UnsupportedOperationException("Only retrieveAsCursor() allowed");
	}

	@Override
	public IList<T> retrieve()
	{
		return query.retrieve(paramMap);
	}

	@Override
	public IList<T> retrieve(Map<Object, Object> nameToValueMap)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IQuery<T> param(Object paramKey, Object param)
	{
		if (!paramMap.putIfNotExists(paramKey, param))
		{
			throw new IllegalArgumentException("Parameter '" + paramKey + "' already added with value '" + paramMap.get(paramKey) + "'");
		}
		return this;
	}

	@Override
	public IVersionItem retrieveAsVersion()
	{
		IVersionCursor cursor = retrieveAsVersions();
		if (cursor == null || !cursor.moveNext())
		{
			return null;
		}
		IVersionItem versionItem = cursor.getCurrent();
		cursor.dispose();
		return versionItem;
	}

	@Override
	public T retrieveSingle()
	{
		IList<T> result = retrieve();
		if (result == null || result.size() == 0)
		{
			return null;
		}
		if (result.size() != 1)
		{
			throw new IllegalStateException("Query result is not unique: " + result.size());
		}
		return result.get(0);
	}
}
