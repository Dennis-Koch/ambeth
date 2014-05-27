package de.osthus.ambeth.query;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.database.ITransaction;
import de.osthus.ambeth.database.ResultingDatabaseCallback;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IDataCursor;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IEntityCursor;
import de.osthus.ambeth.persistence.IVersionCursor;
import de.osthus.ambeth.persistence.IVersionItem;
import de.osthus.ambeth.util.ParamChecker;

public class QueryDelegate<T> implements IInitializingBean, IQuery<T>, IQueryIntern<T>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IServiceContext beanContext;

	protected Connection connection;

	protected IQuery<T> query;

	protected IQuery<T> transactionalQuery;

	protected ITransaction transaction;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(beanContext, "BeanContext");
		ParamChecker.assertNotNull(connection, "Connection");
		ParamChecker.assertNotNull(query, "Query");
		ParamChecker.assertNotNull(transaction, "transaction");
		ParamChecker.assertNotNull(transactionalQuery, "TransactionalQuery");
	}

	public void setBeanContext(IServiceContext beanContext)
	{
		this.beanContext = beanContext;
	}

	public void setConnection(Connection connection)
	{
		this.connection = connection;
	}

	public void setQuery(IQuery<T> query)
	{
		this.query = query;
	}

	public void setTransaction(ITransaction transaction)
	{
		this.transaction = transaction;
	}

	public void setTransactionalQuery(IQuery<T> transactionalQuery)
	{
		this.transactionalQuery = transactionalQuery;
	}

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

	@SuppressWarnings("unchecked")
	@Override
	public IDataCursor retrieveAsData(Map<Object, Object> nameToValueMap)
	{
		return ((IQueryIntern<T>) query).retrieveAsData(nameToValueMap);
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
	@Deprecated
	public IList<T> retrieve(final Map<Object, Object> nameToValueMap)
	{
		if (transaction.isActive())
		{
			return query.retrieve(nameToValueMap);
		}
		return transaction.processAndCommit(new ResultingDatabaseCallback<IList<T>>()
		{
			@Override
			public IList<T> callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
			{
				return query.retrieve(nameToValueMap);
			}
		}, true, true);
	}

	@Override
	public IVersionItem retrieveAsVersion()
	{
		return transactionalQuery.retrieveAsVersion();
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
}
