package de.osthus.ambeth.query;

import java.util.List;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.database.ITransaction;
import de.osthus.ambeth.database.ResultingDatabaseCallback;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IDataCursor;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IEntityCursor;
import de.osthus.ambeth.persistence.IVersionCursor;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;

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
