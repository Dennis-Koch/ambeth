package de.osthus.ambeth.query;

import java.util.List;

import de.osthus.ambeth.appendable.AppendableStringBuilder;
import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.EmptyMap;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.database.ITransaction;
import de.osthus.ambeth.database.ResultingDatabaseCallback;
import de.osthus.ambeth.filter.QueryConstants;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
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
import de.osthus.ambeth.security.config.SecurityConfigurationConstants;
import de.osthus.ambeth.util.IConversionHelper;

@PersistenceContext
public class Query<T> implements IQuery<T>, IQueryIntern<T>, ISubQuery<T>
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IDatabase database;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IServiceUtil serviceUtil;

	@Autowired
	protected IStringQuery stringQuery;

	@Autowired
	protected ITableAliasHolder tableAliasHolder;

	@Autowired
	protected ITransaction transaction;

	@Property
	protected Class<T> entityType;

	@Property(defaultValue = "false")
	protected boolean containsSubQuery;

	@Property
	protected IOperand rootOperand;

	@Property
	protected IOperand[] groupByOperands;

	@Property
	protected IOperand[] orderByOperands;

	@Property
	protected IOperand limitOperand;

	@Property(name = SecurityConfigurationConstants.SecurityActive, defaultValue = "false")
	protected boolean securityActive;

	@Property
	protected IOperand[] selectOperands;

	@Property
	protected List<Class<?>> relatedEntityTypes;

	@Property
	protected IQueryIntern<T> transactionalQuery;

	@Property
	protected IQueryBuilderExtension[] queryBuilderExtensions;

	protected IQueryKey queryKey;

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
	public IQueryKey getQueryKey(IMap<Object, Object> nameToValueMap)
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
					sb.append('#');
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

	protected Object buildCursor(IMap<Object, Object> nameToValueMapSrc, RetrievalType retrievalType, int limitValue)
	{
		if (!transaction.isActive())
		{
			throw new IllegalStateException(IQuery.class.getSimpleName() + ".retrieveAsVersions() can only be called from within a @"
					+ PersistenceContext.class.getSimpleName() + ". This is because the IVersionCursor may hold active resources of underlying databases");
		}
		HashMap<Object, Object> nameToValueMap = new HashMap<Object, Object>();
		if (nameToValueMapSrc == null)
		{
			nameToValueMapSrc = EmptyMap.emptyMap();
		}
		else
		{
			nameToValueMap.putAll(nameToValueMapSrc);
		}
		if (containsSubQuery)
		{
			nameToValueMap.put(QueryConstants.USE_TABLE_ALIAS, Boolean.TRUE);
		}
		if (limitValue > 0)
		{
			nameToValueMap.put(QueryConstants.LIMIT_VALUE, Integer.valueOf(limitValue));
		}
		else
		{
			nameToValueMap.put(QueryConstants.LIMIT_VALUE, null);
		}

		ArrayList<String> additionalSelectColumnList = new ArrayList<String>();
		ArrayList<Object> parameters = new ArrayList<Object>();
		Object pagingSizeObject = nameToValueMap.get(QueryConstants.PAGING_SIZE_OBJECT);

		String[] sqlParts = getSqlParts(nameToValueMap, parameters, additionalSelectColumnList);
		String joinSql = sqlParts[0];
		String whereSql = sqlParts[1];
		String orderBySql = sqlParts[2];
		String limitSql = sqlParts[3];

		String tableAlias = (stringQuery.isJoinQuery() || containsSubQuery) ? tableAliasHolder.getTableAlias() : null;

		Table table = (Table) this.database.getTableByType(this.entityType);

		if (RetrievalType.DATA.equals(retrievalType))
		{
			fillAdditionalFieldsSQL(additionalSelectColumnList, null, nameToValueMap, stringQuery.isJoinQuery(), parameters);
		}
		if (pagingSizeObject == null)
		{
			if (RetrievalType.VERSION.equals(retrievalType))
			{
				return table.selectVersionJoin(additionalSelectColumnList, joinSql, whereSql, orderBySql, limitSql, parameters, tableAlias);
			}
			else if (RetrievalType.COUNT.equals(retrievalType))
			{
				return Long.valueOf(table.selectCountJoin(joinSql, whereSql, orderBySql, parameters, tableAlias));
			}
			return table.selectDataJoin(additionalSelectColumnList, joinSql, whereSql, orderBySql, limitSql, parameters, tableAlias);
		}
		Object pagingIndexObject = nameToValueMap.get(QueryConstants.PAGING_INDEX_OBJECT);

		int pagingLimit = conversionHelper.convertValueToType(Integer.TYPE, pagingSizeObject);
		int pagingOffset = conversionHelper.convertValueToType(Integer.TYPE, pagingIndexObject);

		if (RetrievalType.VERSION.equals(retrievalType))
		{
			return table.selectVersionPaging(additionalSelectColumnList, joinSql, whereSql, orderBySql, limitSql, pagingOffset, pagingLimit, parameters,
					tableAlias);
		}
		return table.selectDataPaging(additionalSelectColumnList, joinSql, whereSql, orderBySql, limitSql, pagingOffset, pagingLimit, parameters);
	}

	@Override
	public String[] getSqlParts(IMap<Object, Object> nameToValueMap, IList<Object> parameters, IList<String> additionalSelectColumnList)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();

		Object useTableAliasOriginal = nameToValueMap.get(QueryConstants.USE_TABLE_ALIAS);
		if (containsSubQuery)
		{
			nameToValueMap.put(QueryConstants.USE_TABLE_ALIAS, Boolean.TRUE);
		}
		for (IQueryBuilderExtension queryBuilderExtension : queryBuilderExtensions)
		{
			queryBuilderExtension.applyOnQuery(nameToValueMap, parameters, additionalSelectColumnList);
		}
		String joinSql, whereSql;
		boolean joinQuery = stringQuery.isJoinQuery();
		if (!joinQuery)
		{
			joinSql = null;
			whereSql = stringQuery.fillQuery(nameToValueMap, parameters);
		}
		else
		{
			String[] sqlParts = stringQuery.fillJoinQuery(nameToValueMap, parameters);

			joinSql = sqlParts[0];
			whereSql = sqlParts[1];
		}
		AppendableStringBuilder tempSB = tlObjectCollector.create(AppendableStringBuilder.class);
		try
		{
			fillOrderBySQL(additionalSelectColumnList, tempSB, nameToValueMap, joinQuery, parameters);
			String orderBySql = tempSB.toString();
			tempSB.reset();
			fillLimitSQL(additionalSelectColumnList, tempSB, nameToValueMap, joinQuery, parameters);

			String[] sqlParts = { joinSql, whereSql, orderBySql, tempSB.toString() };
			return sqlParts;
		}
		finally
		{
			nameToValueMap.put(QueryConstants.USE_TABLE_ALIAS, useTableAliasOriginal);
			tlObjectCollector.dispose(tempSB);
		}
	}

	protected void fillOrderBySQL(List<String> additionalSelectColumnList, IAppendable orderBySB, IMap<Object, Object> nameToValueMap, boolean joinQuery,
			IList<Object> parameters)
	{
		if (orderByOperands.length == 0 && groupByOperands.length == 0)
		{
			return;
		}
		nameToValueMap.put(QueryConstants.FIRST_ORDER_BY_STATE, Boolean.TRUE);
		nameToValueMap.put(QueryConstants.ADDITIONAL_SELECT_SQL_SB, additionalSelectColumnList);
		try
		{
			boolean firstGroupBy = true;
			for (int a = 0, size = groupByOperands.length; a < size; a++)
			{
				if (firstGroupBy)
				{
					orderBySB.append("GROUP BY ");
					firstGroupBy = false;
				}
				else
				{
					orderBySB.append(',');
				}
				groupByOperands[a].expandQuery(orderBySB, nameToValueMap, joinQuery, parameters);
			}
			for (int a = 0, size = orderByOperands.length; a < size; a++)
			{
				orderByOperands[a].expandQuery(orderBySB, nameToValueMap, joinQuery, parameters);
			}
		}
		finally
		{
			nameToValueMap.remove(QueryConstants.ADDITIONAL_SELECT_SQL_SB);
			nameToValueMap.remove(QueryConstants.FIRST_ORDER_BY_STATE);
		}
	}

	protected void fillLimitSQL(List<String> additionalSelectColumnList, IAppendable limitSB, IMap<Object, Object> nameToValueMap, boolean joinQuery,
			IList<Object> parameters)
	{
		if (limitOperand == null)
		{
			return;
		}
		limitOperand.expandQuery(limitSB, nameToValueMap, joinQuery, parameters);
	}

	protected void fillAdditionalFieldsSQL(IList<String> additionalSelectColumnList, IAppendable querySB, IMap<Object, Object> nameToValueMap,
			boolean joinQuery, IList<Object> parameters)
	{
		if (selectOperands.length == 0)
		{
			return;
		}
		nameToValueMap.put(QueryConstants.ADDITIONAL_SELECT_SQL_SB, additionalSelectColumnList);
		try
		{
			for (int a = 0, size = selectOperands.length; a < size; a++)
			{
				selectOperands[a].expandQuery(querySB, nameToValueMap, joinQuery, parameters);
			}
		}
		finally
		{
			nameToValueMap.remove(QueryConstants.ADDITIONAL_SELECT_SQL_SB);
		}
	}

	@Override
	public IVersionCursor retrieveAsVersions()
	{
		return (IVersionCursor) buildCursor(null, RetrievalType.VERSION, 0);
	}

	@Override
	public IVersionCursor retrieveAsVersions(IMap<Object, Object> nameToValueMap)
	{
		return (IVersionCursor) buildCursor(nameToValueMap, RetrievalType.VERSION, 0);
	}

	@Override
	public IDataCursor retrieveAsData()
	{
		return (IDataCursor) buildCursor(null, RetrievalType.DATA, 0);
	}

	@Override
	public IDataCursor retrieveAsData(IMap<Object, Object> nameToValueMap)
	{
		return (IDataCursor) buildCursor(nameToValueMap, RetrievalType.DATA, 0);
	}

	@Override
	public IEntityCursor<T> retrieveAsCursor()
	{
		return retrieveAsCursor(null);
	}

	@Override
	public IEntityCursor<T> retrieveAsCursor(IMap<Object, Object> nameToValueMap)
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
	public IList<T> retrieve(IMap<Object, Object> nameToValueMap)
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
	public long count()
	{
		return count(null);
	}

	@Override
	public long count(IMap<Object, Object> paramNameToValueMap)
	{
		return ((Long) buildCursor(paramNameToValueMap, RetrievalType.COUNT, 0)).longValue();
	}

	@Override
	public boolean isEmpty()
	{
		return isEmpty(null);
	}

	@Override
	public boolean isEmpty(IMap<Object, Object> paramNameToValueMap)
	{
		IVersionCursor versionCursor = (IVersionCursor) buildCursor(paramNameToValueMap, RetrievalType.VERSION, 1);
		try
		{
			return !versionCursor.moveNext();
		}
		finally
		{
			versionCursor.dispose();
		}
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
