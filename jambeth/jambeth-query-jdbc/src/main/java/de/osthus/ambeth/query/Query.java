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
	protected IOperand[] orderByOperands;

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

	protected Object retrieveAsVersionsIntern(IMap<Object, Object> nameToValueMapSrc, boolean versionOnly)
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

		ArrayList<String> additionalSelectColumnList = new ArrayList<String>();
		StringBuilder whereSB = tlObjectCollector.create(StringBuilder.class);
		ArrayList<Object> parameters = new ArrayList<Object>();
		try
		{
			Object pagingSizeObject = nameToValueMap.get(QueryConstants.PAGING_SIZE_OBJECT);

			String[] sqlParts = getSqlParts(nameToValueMap, parameters, additionalSelectColumnList);
			String joinSql = sqlParts[0];
			String whereSql = sqlParts[1];
			String orderBySql = sqlParts[2];

			String tableAlias = (stringQuery.isJoinQuery() || containsSubQuery) ? tableAliasHolder.getTableAlias() : null;

			Table table = (Table) this.database.getTableByType(this.entityType);

			if (!versionOnly)
			{
				fillAdditionalFieldsSQL(additionalSelectColumnList, null, nameToValueMap, stringQuery.isJoinQuery(), parameters);
			}
			if (pagingSizeObject == null)
			{
				if (!orderBySql.isEmpty())
				{
					whereSB.append(whereSql).append(' ').append(orderBySql);
					if (versionOnly)
					{
						return table.selectVersionJoin(additionalSelectColumnList, joinSql, whereSB, parameters, tableAlias);
					}
					return table.selectDataJoin(additionalSelectColumnList, joinSql, whereSB, parameters, tableAlias);
				}
				else if (versionOnly)
				{
					return table.selectVersionJoin(additionalSelectColumnList, joinSql, whereSql, parameters, tableAlias);
				}
				else
				{
					return table.selectDataJoin(additionalSelectColumnList, joinSql, whereSql, parameters, tableAlias);
				}
			}
			else
			{
				Object pagingIndexObject = nameToValueMap.get(QueryConstants.PAGING_INDEX_OBJECT);

				int pagingLimit = conversionHelper.convertValueToType(Integer.TYPE, pagingSizeObject);
				int pagingOffset = conversionHelper.convertValueToType(Integer.TYPE, pagingIndexObject);

				if (versionOnly)
				{
					return table.selectVersionPaging(additionalSelectColumnList, joinSql, whereSql, orderBySql, pagingOffset, pagingLimit, parameters,
							tableAlias);
				}
				return table.selectDataPaging(additionalSelectColumnList, joinSql, whereSql, orderBySql, pagingOffset, pagingLimit, parameters);
			}
		}
		finally
		{
			tlObjectCollector.dispose(whereSB);
		}
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
		AppendableStringBuilder orderBySB = tlObjectCollector.create(AppendableStringBuilder.class);
		try
		{
			fillOrderBySQL(additionalSelectColumnList, orderBySB, nameToValueMap, joinQuery, parameters);

			String[] sqlParts = { joinSql, whereSql, orderBySB.toString() };
			return sqlParts;
		}
		finally
		{
			nameToValueMap.put(QueryConstants.USE_TABLE_ALIAS, useTableAliasOriginal);
			tlObjectCollector.dispose(orderBySB);
		}
	}

	protected void fillOrderBySQL(List<String> additionalSelectColumnList, IAppendable orderBySB, IMap<Object, Object> nameToValueMap, boolean joinQuery,
			IList<Object> parameters)
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
				orderByOperands[a].expandQuery(orderBySB, nameToValueMap, joinQuery, parameters);
			}
		}
		finally
		{
			nameToValueMap.remove(QueryConstants.ADDITIONAL_SELECT_SQL_SB);
			nameToValueMap.remove(QueryConstants.FIRST_ORDER_BY_STATE);
		}
	}

	protected void fillAdditionalFieldsSQL(IList<String> additionalSelectColumnList, IAppendable querySB, IMap<Object, Object> nameToValueMap,
			boolean joinQuery, IList<Object> parameters)
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
		return (IVersionCursor) retrieveAsVersionsIntern(null, true);
	}

	@Override
	public IVersionCursor retrieveAsVersions(IMap<Object, Object> nameToValueMap)
	{
		return (IVersionCursor) retrieveAsVersionsIntern(nameToValueMap, true);
	}

	@Override
	public IDataCursor retrieveAsData()
	{
		return (IDataCursor) retrieveAsVersionsIntern(null, false);
	}

	@Override
	public IDataCursor retrieveAsData(IMap<Object, Object> nameToValueMap)
	{
		return (IDataCursor) retrieveAsVersionsIntern(nameToValueMap, false);
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
