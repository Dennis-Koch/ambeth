package de.osthus.ambeth.query;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Map;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IDataCursor;
import de.osthus.ambeth.persistence.IEntityCursor;
import de.osthus.ambeth.persistence.IVersionCursor;
import de.osthus.ambeth.persistence.IVersionItem;
import de.osthus.ambeth.util.ParamChecker;

public class QueryBean<T> implements IInitializingBean, IQuery<T>
{
	@SuppressWarnings("unused")
	@LogInstance(QueryBean.class)
	private ILogger log;

	protected Class<T> entityType;

	protected IQueryBuilderFactory queryBuilderFactory;

	protected IQueryCreator queryCreator;

	protected Reference<IQuery<T>> queryRef;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(entityType, "entityType");
		ParamChecker.assertNotNull(queryBuilderFactory, "QueryBuilderFactory");
		ParamChecker.assertNotNull(queryCreator, "QueryCreator");
	}

	public void setEntityType(Class<T> entityType)
	{
		this.entityType = entityType;
	}

	public void setQueryBuilderFactory(IQueryBuilderFactory queryBuilderFactory)
	{
		this.queryBuilderFactory = queryBuilderFactory;
	}

	public void setQueryCreator(IQueryCreator queryCreator)
	{
		this.queryCreator = queryCreator;
	}

	@Override
	public void dispose()
	{
		queryRef = null;
	}

	protected IQuery<T> getQuery()
	{
		IQuery<T> query = null;
		if (queryRef != null)
		{
			query = queryRef.get();
		}
		if (query == null)
		{
			IQueryBuilder<T> queryBuilder = queryBuilderFactory.create(entityType);
			query = queryCreator.createCustomQuery(queryBuilder);
			if (query == null)
			{
				throw new IllegalStateException("QueryCreator " + queryCreator + " returned no query handle");
			}
			queryRef = new SoftReference<IQuery<T>>(query);
		}
		return query;
	}

	@Override
	public Class<T> getEntityType()
	{
		return entityType;
	}

	@Override
	public void fillRelatedEntityTypes(List<Class<?>> relatedEntityTypes)
	{
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public IQueryKey getQueryKey(Map<Object, Object> nameToValueMap)
	{
		return getQuery().getQueryKey(nameToValueMap);
	}

	@Override
	@Deprecated
	public IVersionCursor retrieveAsVersions(Map<Object, Object> nameToValueMap)
	{
		return getQuery().retrieveAsVersions(nameToValueMap);
	}

	@Override
	@Deprecated
	public IEntityCursor<T> retrieveAsCursor(Map<Object, Object> nameToValueMap)
	{
		return getQuery().retrieveAsCursor(nameToValueMap);
	}

	@Override
	@Deprecated
	public IList<T> retrieve(Map<Object, Object> nameToValueMap)
	{
		return getQuery().retrieve(nameToValueMap);
	}

	@Override
	public IList<T> retrieve()
	{
		return getQuery().retrieve();
	}

	@Override
	public IEntityCursor<T> retrieveAsCursor()
	{
		return getQuery().retrieveAsCursor();
	}

	@Override
	public IVersionCursor retrieveAsVersions()
	{
		return getQuery().retrieveAsVersions();
	}

	@Override
	public IDataCursor retrieveAsData()
	{
		return getQuery().retrieveAsData();
	}

	@Override
	public IVersionItem retrieveAsVersion()
	{
		return getQuery().retrieveAsVersion();
	}

	@Override
	public T retrieveSingle()
	{
		return getQuery().retrieveSingle();
	}

	@Override
	public IQuery<T> param(Object paramKey, Object param)
	{
		return getQuery().param(paramKey, param);
	}
}
