package com.koch.ambeth.query.jdbc;

import java.util.List;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.database.ITransaction;
import com.koch.ambeth.persistence.api.database.ResultingDatabaseCallback;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryIntern;
import com.koch.ambeth.query.IQueryKey;
import com.koch.ambeth.query.persistence.IDataCursor;
import com.koch.ambeth.query.persistence.IEntityCursor;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

public class QueryDelegate<T> implements IQuery<T>, IQueryIntern<T>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected ITransaction transaction;

	@Autowired
	protected IQuery<T> query;

	@Autowired
	protected IQueryIntern<T> queryIntern;

	@Autowired
	protected IQuery<T> transactionalQuery;

	@Override
	public void dispose()
	{
		beanContext.dispose();
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
	public IQueryKey getQueryKey(IMap<Object, Object> nameToValueMap)
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
	public IDataCursor retrieveAsData(IMap<Object, Object> nameToValueMap)
	{
		return queryIntern.retrieveAsData(nameToValueMap);
	}

	@Override
	public IVersionCursor retrieveAsVersions(IMap<Object, Object> nameToValueMap)
	{
		return queryIntern.retrieveAsVersions(nameToValueMap, true);
	}

	@Override
	public IVersionCursor retrieveAsVersions(boolean retrieveAlternateIds)
	{
		return query.retrieveAsVersions(retrieveAlternateIds);
	}

	@Override
	public IVersionCursor retrieveAsVersions(IMap<Object, Object> paramNameToValueMap, boolean retrieveAlternateIds)
	{
		return queryIntern.retrieveAsVersions(paramNameToValueMap, retrieveAlternateIds);
	}

	@Override
	public IEntityCursor<T> retrieveAsCursor()
	{
		return query.retrieveAsCursor();
	}

	@Override
	public IEntityCursor<T> retrieveAsCursor(IMap<Object, Object> nameToValueMap)
	{
		return queryIntern.retrieveAsCursor(nameToValueMap);
	}

	@Override
	public IList<T> retrieve()
	{
		if (transaction.isActive())
		{
			return query.retrieve();
		}
		return transaction.processAndCommit(new ResultingDatabaseCallback<IList<T>>()
		{
			@Override
			public IList<T> callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
			{
				return query.retrieve();
			}
		}, true, true);
	}

	@Override
	public IList<T> retrieve(final IMap<Object, Object> nameToValueMap)
	{
		if (transaction.isActive())
		{
			return queryIntern.retrieve(nameToValueMap);
		}
		return transaction.processAndCommit(new ResultingDatabaseCallback<IList<T>>()
		{
			@Override
			public IList<T> callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
			{
				return queryIntern.retrieve(nameToValueMap);
			}
		}, true, true);
	}

	@Override
	public T retrieveSingle()
	{
		return transactionalQuery.retrieveSingle();
	}

	@Override
	public IQuery<T> param(Object paramKey, Object param)
	{
		return query.param(paramKey, param);
	}

	@Override
	public long count()
	{
		if (transaction.isActive())
		{
			return query.count();
		}
		return transaction.processAndCommit(new ResultingDatabaseCallback<Long>()
		{
			@Override
			public Long callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Throwable
			{
				return Long.valueOf(query.count());
			}
		}).longValue();
	}

	@Override
	public long count(final IMap<Object, Object> paramNameToValueMap)
	{
		if (transaction.isActive())
		{
			return queryIntern.count(paramNameToValueMap);
		}
		return transaction.processAndCommit(new ResultingDatabaseCallback<Long>()
		{
			@Override
			public Long callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Throwable
			{
				return Long.valueOf(queryIntern.count(paramNameToValueMap));
			}
		}).intValue();
	}

	@Override
	public boolean isEmpty()
	{
		if (transaction.isActive())
		{
			return query.isEmpty();
		}
		return transaction.runInTransaction(new IResultingBackgroundWorkerDelegate<Boolean>()
		{
			@Override
			public Boolean invoke() throws Throwable
			{
				return Boolean.valueOf(query.isEmpty());
			}
		}).booleanValue();
	}

	@Override
	public boolean isEmpty(final IMap<Object, Object> paramNameToValueMap)
	{
		if (transaction.isActive())
		{
			return queryIntern.isEmpty(paramNameToValueMap);
		}
		return transaction.processAndCommit(new ResultingDatabaseCallback<Boolean>()
		{
			@Override
			public Boolean callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap) throws Throwable
			{
				return Boolean.valueOf(queryIntern.isEmpty(paramNameToValueMap));
			}
		}).booleanValue();
	}
}
