package de.osthus.ambeth.query;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.database.ITransaction;
import de.osthus.ambeth.database.ResultingDatabaseCallback;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.filter.QueryConstants;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.EntityCursor;
import de.osthus.ambeth.persistence.IDataCursor;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IEntityCursor;
import de.osthus.ambeth.persistence.IServiceUtil;
import de.osthus.ambeth.persistence.IVersionCursor;
import de.osthus.ambeth.persistence.IVersionItem;
import de.osthus.ambeth.persistence.Table;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.query.sql.ITableAliasHolder;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.ParamChecker;

@PersistenceContext
public class Query<T> implements IQuery<T>, IQueryIntern<T>, IInitializingBean, ISubQuery<T>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IServiceContext beanContext;

	protected IDatabase database;

	protected Class<T> entityType;

	protected boolean containsSubQuery;

	protected IConversionHelper conversionHelper;

	protected IThreadLocalObjectCollector objectCollector;

	protected IOperand rootOperand;

	protected IOperand[] orderByOperands;

	protected IOperand[] selectOperands;

	protected ITableAliasHolder tableAliasHolder;

	protected IStringQuery stringQuery;

	protected IServiceUtil serviceUtil;

	protected List<Class<?>> relatedEntityTypes;

	protected IQueryKey queryKey;

	protected IQueryIntern<T> transactionalQuery;

	protected ITransaction transaction;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(beanContext, "BeanContext");
		ParamChecker.assertNotNull(conversionHelper, "ConversionHelper");
		ParamChecker.assertNotNull(database, "Database");
		ParamChecker.assertNotNull(entityType, "EntityType");
		ParamChecker.assertNotNull(objectCollector, "ObjectCollector");
		ParamChecker.assertNotNull(relatedEntityTypes, "RelatedEntityTypes");
		ParamChecker.assertNotNull(rootOperand, "RootOperand");
		ParamChecker.assertNotNull(serviceUtil, "ServiceUtil");
		ParamChecker.assertNotNull(stringQuery, "StringQuery");
		ParamChecker.assertNotNull(tableAliasHolder, "tableAliasHolder");
		ParamChecker.assertNotNull(transaction, "Transaction");
		ParamChecker.assertNotNull(transactionalQuery, "TransactionalQuery");
	}

	public void setBeanContext(IServiceContext beanContext)
	{
		this.beanContext = beanContext;
	}

	public void setConversionHelper(IConversionHelper conversionHelper)
	{
		this.conversionHelper = conversionHelper;
	}

	public void setDatabase(IDatabase database)
	{
		this.database = database;
	}

	public void setEntityType(Class<T> entityType)
	{
		this.entityType = entityType;
	}

	public void setContainsSubQuery(boolean containsSubQuery)
	{
		this.containsSubQuery = containsSubQuery;
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	public void setOrderByOperands(IOperand[] orderByOperands)
	{
		this.orderByOperands = orderByOperands;
	}

	public void setSelectOperands(IOperand[] selectOperands)
	{
		this.selectOperands = selectOperands;
	}

	public void setRelatedEntityTypes(List<Class<?>> relatedEntityTypes)
	{
		this.relatedEntityTypes = relatedEntityTypes;
	}

	public void setRootOperand(IOperand rootOperand)
	{
		this.rootOperand = rootOperand;
	}

	public void setServiceUtil(IServiceUtil serviceUtil)
	{
		this.serviceUtil = serviceUtil;
	}

	public void setStringQuery(IStringQuery stringQuery)
	{
		this.stringQuery = stringQuery;
	}

	public void setTableAliasHolder(ITableAliasHolder tableAliasHolder)
	{
		this.tableAliasHolder = tableAliasHolder;
	}

	public void setTransaction(ITransaction transaction)
	{
		this.transaction = transaction;
	}

	public void setTransactionalQuery(IQueryIntern<T> transactionalQuery)
	{
		this.transactionalQuery = transactionalQuery;
	}

	@Override
	public void dispose()
	{
		beanContext.dispose();
		beanContext = null;
	}

	@Override
	public Class<T> getEntityType()
	{
		return this.entityType;
	}

	@Override
	public void fillRelatedEntityTypes(List<Class<?>> relatedEntityTypes)
	{
		for (int a = 0, size = this.relatedEntityTypes.size(); a < size; a++)
		{
			relatedEntityTypes.add(this.relatedEntityTypes.get(a));
		}
	}

	@Override
	public IQueryKey getQueryKey(Map<Object, Object> nameToValueMap)
	{
		if (this.queryKey == null)
		{
			IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();

			IMap<Object, Object> tempNameToValueMap = null;
			if (nameToValueMap == null)
			{
				tempNameToValueMap = new HashMap<Object, Object>();
				nameToValueMap = tempNameToValueMap;
			}

			String[] sqlParts = getSqlParts(nameToValueMap, null, null);
			StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
			try
			{
				for (int i = 0; i < sqlParts.length; i++)
				{
					if (sqlParts[i] != null)
					{
						sb.append(sqlParts[i]);
					}
				}
				this.queryKey = new StringQueryKey(this.entityType, sb.toString());
			}
			finally
			{
				tlObjectCollector.dispose(sb);
			}
		}

		return this.queryKey;
	}

	protected Object retrieveAsVersionsIntern(Map<Object, Object> nameToValueMapSrc, boolean versionOnly)
	{
		if (!transaction.isActive())
		{
			throw new IllegalStateException(IQuery.class.getSimpleName() + ".retrieveAsVersions() can only be called from within a @"
					+ PersistenceContext.class.getSimpleName() + ". This is because the IVersionCursor may hold active resources of underlying databases");
		}
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();

		HashMap<Object, Object> nameToValueMap = new HashMap<Object, Object>();
		if (nameToValueMapSrc == null)
		{
			nameToValueMapSrc = Collections.<Object, Object> emptyMap();
		}
		else
		{
			Iterator<Entry<Object, Object>> iter = nameToValueMapSrc.entrySet().iterator();
			while (iter.hasNext())
			{
				Entry<Object, Object> entry = iter.next();
				nameToValueMap.put(entry.getKey(), entry.getValue());
			}
		}
		if (containsSubQuery)
		{
			nameToValueMap.put(QueryConstants.USE_TABLE_ALIAS, Boolean.TRUE);
		}

		ArrayList<String> additionalSelectColumnList = new ArrayList<String>();
		StringBuilder whereSB = tlObjectCollector.create(StringBuilder.class);
		LinkedHashMap<Integer, Object> params = new LinkedHashMap<Integer, Object>();
		try
		{
			Object pagingSizeObject = nameToValueMap.get(QueryConstants.PAGING_SIZE_OBJECT);

			String[] sqlParts = getSqlParts(nameToValueMap, params, additionalSelectColumnList);
			String joinSql = sqlParts[0];
			String whereSql = sqlParts[1];
			String orderBySql = sqlParts[2];

			String tableAlias = (stringQuery.isJoinQuery() || containsSubQuery) ? tableAliasHolder.getTableAlias() : null;

			Table table = (Table) this.database.getTableByType(this.entityType);

			if (!versionOnly)
			{
				fillAdditionalFieldsSQL(additionalSelectColumnList, null, nameToValueMap, stringQuery.isJoinQuery(), params);
			}
			if (pagingSizeObject == null)
			{
				if (!orderBySql.isEmpty())
				{
					whereSB.append(whereSql).append(' ').append(orderBySql);
					if (versionOnly)
					{
						return table.selectVersionJoin(additionalSelectColumnList, joinSql, whereSB, params, tableAlias);
					}
					return table.selectDataJoin(additionalSelectColumnList, joinSql, whereSB, params, tableAlias);
				}
				else if (versionOnly)
				{
					return table.selectVersionJoin(additionalSelectColumnList, joinSql, whereSql, params, tableAlias);
				}
				else
				{
					return table.selectDataJoin(additionalSelectColumnList, joinSql, whereSql, params, tableAlias);
				}
			}
			else
			{
				Object pagingIndexObject = nameToValueMap.get(QueryConstants.PAGING_INDEX_OBJECT);

				int pagingLimit = conversionHelper.convertValueToType(Integer.TYPE, pagingSizeObject);
				int pagingOffset = conversionHelper.convertValueToType(Integer.TYPE, pagingIndexObject);

				if (versionOnly)
				{
					return table.selectVersionPaging(additionalSelectColumnList, joinSql, whereSql, orderBySql, pagingOffset, pagingLimit, params, tableAlias);
				}
				return table.selectDataPaging(additionalSelectColumnList, joinSql, whereSql, orderBySql, pagingOffset, pagingLimit, params);
			}
		}
		finally
		{
			tlObjectCollector.dispose(whereSB);
		}
	}

	@Override
	public String[] getSqlParts(Map<Object, Object> nameToValueMap, ILinkedMap<Integer, Object> params, List<String> additionalSelectColumnList)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();

		Object useTableAliasOriginal = nameToValueMap.get(QueryConstants.USE_TABLE_ALIAS);
		if (containsSubQuery)
		{
			nameToValueMap.put(QueryConstants.USE_TABLE_ALIAS, Boolean.TRUE);
		}

		String joinSql, whereSql;
		boolean joinQuery = stringQuery.isJoinQuery();
		if (!joinQuery)
		{
			joinSql = null;
			whereSql = stringQuery.fillQuery(nameToValueMap, params);
		}
		else
		{
			String[] sqlParts = stringQuery.fillJoinQuery(nameToValueMap, params);

			joinSql = sqlParts[0];
			whereSql = sqlParts[1];
		}
		StringBuilder orderBySB = tlObjectCollector.create(StringBuilder.class);
		try
		{
			fillOrderBySQL(additionalSelectColumnList, orderBySB, nameToValueMap, joinQuery, params);

			String[] sqlParts = { joinSql, whereSql, orderBySB.toString() };
			return sqlParts;
		}
		finally
		{
			nameToValueMap.put(QueryConstants.USE_TABLE_ALIAS, useTableAliasOriginal);
			tlObjectCollector.dispose(orderBySB);
		}
	}

	protected void fillOrderBySQL(List<String> additionalSelectColumnList, StringBuilder orderBySB, Map<Object, Object> nameToValueMap, boolean joinQuery,
			ILinkedMap<Integer, Object> params)
	{
		if (orderByOperands == null)
		{
			return;
		}
		nameToValueMap.put(QueryConstants.FIRST_ORDER_BY_STATE, Boolean.TRUE);
		nameToValueMap.put(QueryConstants.ADDITIONAL_SELECT_SQL_SB, additionalSelectColumnList);
		try
		{
			for (int a = 0, size = orderByOperands.length; a < size; a++)
			{
				orderByOperands[a].expandQuery(orderBySB, nameToValueMap, joinQuery, params);
			}
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			nameToValueMap.remove(QueryConstants.ADDITIONAL_SELECT_SQL_SB);
			nameToValueMap.remove(QueryConstants.FIRST_ORDER_BY_STATE);
		}
	}

	protected void fillAdditionalFieldsSQL(List<String> additionalSelectColumnList, Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery,
			ILinkedMap<Integer, Object> params)
	{
		if (selectOperands == null)
		{
			return;
		}
		nameToValueMap.put(QueryConstants.ADDITIONAL_SELECT_SQL_SB, additionalSelectColumnList);
		try
		{
			for (int a = 0, size = selectOperands.length; a < size; a++)
			{
				selectOperands[a].expandQuery(querySB, nameToValueMap, joinQuery, params);
			}
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			nameToValueMap.remove(QueryConstants.ADDITIONAL_SELECT_SQL_SB);
		}
	}

	@Override
	public IVersionCursor retrieveAsVersions()
	{
		return (IVersionCursor) retrieveAsVersionsIntern(null, true);
	}

	@Override
	public IVersionCursor retrieveAsVersions(Map<Object, Object> nameToValueMap)
	{
		return (IVersionCursor) retrieveAsVersionsIntern(nameToValueMap, true);
	}

	@Override
	public IDataCursor retrieveAsData()
	{
		return (IDataCursor) retrieveAsVersionsIntern(null, false);
	}

	@Override
	public IDataCursor retrieveAsData(Map<Object, Object> nameToValueMap)
	{
		return (IDataCursor) retrieveAsVersionsIntern(nameToValueMap, false);
	}

	@Override
	public IEntityCursor<T> retrieveAsCursor()
	{
		return retrieveAsCursor(null);
	}

	@Override
	public IEntityCursor<T> retrieveAsCursor(Map<Object, Object> nameToValueMap)
	{
		IVersionCursor cursor = retrieveAsVersions(nameToValueMap);
		return new EntityCursor<T>(cursor, this.entityType, this.serviceUtil);
	}

	@Override
	public IList<T> retrieve()
	{
		return retrieve(null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public IList<T> retrieve(Map<Object, Object> nameToValueMap)
	{
		IVersionCursor cursor = retrieveAsVersions(nameToValueMap);
		if (cursor == null)
		{
			return (IList<T>) EmptyList.instance;
		}
		ArrayList<T> resultList = new ArrayList<T>();
		serviceUtil.loadObjectsIntoCollection(resultList, entityType, cursor);
		return resultList;
	}

	@Override
	public IVersionItem retrieveAsVersion()
	{
		if (!transaction.isActive())
		{
			return transaction.processAndCommit(new ResultingDatabaseCallback<IVersionItem>()
			{

				@Override
				public IVersionItem callback(ILinkedMap<Object, IDatabase> persistenceUnitToDatabaseMap)
				{
					return retrieveAsVersion();
				}
			});
		}
		IVersionCursor cursor = retrieveAsVersions();
		if (cursor == null || !cursor.moveNext())
		{
			return null;
		}
		IVersionItem item = cursor.getCurrent();
		cursor.dispose();
		return item;
	}

	@Override
	public T retrieveSingle()
	{
		IList<T> resultList = retrieve();
		if (resultList == null)
		{
			return null;
		}
		if (resultList.size() == 0)
		{
			return null;
		}
		return resultList.get(0);
	}

	@Override
	public IQuery<T> param(Object paramKey, Object param)
	{
		StatefulQuery<T> statefulQuery = new StatefulQuery<T>(transactionalQuery);
		return statefulQuery.param(paramKey, param);
	}

	@Override
	public String getMainTableAlias()
	{
		return tableAliasHolder.getTableAlias();
	}

	@Override
	public void setMainTableAlias(String alias)
	{
		tableAliasHolder.setTableAlias(alias);
	}
}
