package de.osthus.ambeth.sql;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.OptimisticLockException;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.IPersistenceHelper;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.IDisposable;
import de.osthus.ambeth.util.ParamChecker;

public abstract class SqlConnection implements ISqlConnection, IInitializingBean, IDisposable
{
	// RegEx to add field aliases to paging subselects, e.g. S_A."ID":
	// outer sql: "S_A.ID"
	// inner sql: S_A."ID" AS "S_A.ID"
	private static final Pattern fieldWithAlias = Pattern.compile("(([SJ]_[A-Z]+\\.)\"([^\"]+)\")");
	private static final String innerFieldPattern = "$1 AS \"$2$3\"";
	private static final String outerFieldPattern = "\"$2$3\"";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IConversionHelper conversionHelper;

	protected IPersistenceHelper persistenceHelper;

	protected ISqlBuilder sqlBuilder;

	protected IThreadLocalObjectCollector objectCollector;

	protected int maxInClauseBatchThreshold;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(conversionHelper, "conversionHelper");
		ParamChecker.assertNotNull(persistenceHelper, "persistenceHelper");
		ParamChecker.assertNotNull(sqlBuilder, "sqlBuilder");
		ParamChecker.assertNotNull(objectCollector, "objectCollector");
	}

	@Override
	public void dispose()
	{
		// Intended blank
	}

	public void setConversionHelper(IConversionHelper conversionHelper)
	{
		this.conversionHelper = conversionHelper;
	}

	public void setPersistenceHelper(IPersistenceHelper persistenceHelper)
	{
		this.persistenceHelper = persistenceHelper;
	}

	public void setSqlBuilder(ISqlBuilder sqlBuilder)
	{
		this.sqlBuilder = sqlBuilder;
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	@Property(name = PersistenceConfigurationConstants.MaxInClauseBatchThreshold, defaultValue = "8000")
	public void setMaxInClauseBatchThreshold(int maxInClauseBatchThreshold)
	{
		this.maxInClauseBatchThreshold = maxInClauseBatchThreshold;
	}

	public void directSql(String sql)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	protected void queueSqlExecute(String sql, ILinkedMap<Integer, Object> params)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	protected int[] queueSqlExecute(String[] sql)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	protected IResultSet sqlSelect(String sql, ILinkedMap<Integer, Object> params)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	protected void checkExecutionResult(int[] result)
	{
		for (int i = result.length; i-- > 0;)
		{
			if (result[i] == 0)
			{
				throw new OptimisticLockException("Object to delete has been altered");
			}
		}
	}

	@Override
	public void queueDelete(String tableName, String whereSql, ILinkedMap<Integer, Object> params)
	{
		IThreadLocalObjectCollector current = objectCollector.getCurrent();
		StringBuilder sb = current.create(StringBuilder.class);
		try
		{
			sb.append("DELETE FROM ");
			sqlBuilder.appendName(tableName, sb);
			sb.append(" WHERE ").append(whereSql);
			queueSqlExecute(sb.toString(), params);
		}
		finally
		{
			current.dispose(sb);
		}
	}

	@Override
	public void queueDelete(String tableName, String[] whereSql)
	{
		IThreadLocalObjectCollector current = objectCollector.getCurrent();
		StringBuilder sb = current.create(StringBuilder.class);
		String[] sqls = new String[whereSql.length];
		try
		{
			sb.append("DELETE FROM ");
			sqlBuilder.appendName(tableName, sb);
			sb.append(" WHERE ");
			String sqlBase = sb.toString();
			for (int i = whereSql.length; i-- > 0;)
			{
				sb.setLength(0);
				sb.append(sqlBase).append(whereSql[i]);
				sqls[i] = sb.toString();
			}
			checkExecutionResult(queueSqlExecute(sqls));
		}
		finally
		{
			current.dispose(sb);
		}
	}

	@Override
	public void queueDeleteAll(String tableName)
	{
		IThreadLocalObjectCollector current = objectCollector.getCurrent();
		StringBuilder sb = current.create(StringBuilder.class);
		try
		{
			sb.append("DELETE FROM ");
			sqlBuilder.appendName(tableName, sb);
			queueSqlExecute(sb.toString(), null);
		}
		finally
		{
			current.dispose(sb);
		}
	}

	@Override
	public void queueUpdate(String tableName, String valueAndNamesSql, String whereSql)
	{
		IThreadLocalObjectCollector current = objectCollector.getCurrent();
		StringBuilder sb = current.create(StringBuilder.class);
		try
		{
			sb.append("UPDATE ");
			sqlBuilder.appendName(tableName, sb);
			sb.append(" SET ").append(valueAndNamesSql);
			if (whereSql != null && !whereSql.isEmpty())
			{
				sb.append(" WHERE ").append(whereSql);
			}
			queueSqlExecute(sb.toString(), null);
		}
		finally
		{
			current.dispose(sb);
		}
	}

	@Override
	public IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence whereSql, ILinkedMap<Integer, Object> params)
	{
		return selectFields(tableName, fieldNamesSql, "", whereSql, params);
	}

	@Override
	public IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence joinSql, CharSequence whereSql, ILinkedMap<Integer, Object> params)
	{
		boolean join = joinSql != null && joinSql.length() > 0;
		String tableAlias = join ? "A" : null;
		return selectFields(tableName, fieldNamesSql, joinSql, whereSql, params, tableAlias);
	}

	@Override
	public IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence joinSql, CharSequence whereSql,
			ILinkedMap<Integer, Object> params, String tableAlias)
	{
		boolean join = joinSql != null && joinSql.length() > 0;
		IThreadLocalObjectCollector current = objectCollector.getCurrent();
		StringBuilder sb = current.create(StringBuilder.class);
		try
		{
			sb.append("SELECT ");
			if (join)
			{
				sb.append("DISTINCT ");
			}
			sb.append(fieldNamesSql).append(" FROM ");
			sqlBuilder.appendName(tableName, sb);
			if (tableAlias != null)
			{
				sb.append(" ").append(tableAlias);
			}
			if (join)
			{
				sb.append(" ").append(joinSql);
			}
			if (whereSql != null && whereSql.length() > 0)
			{
				sb.append(" WHERE ").append(whereSql);
			}
			return sqlSelect(sb.toString(), params);
		}
		finally
		{
			current.dispose(sb);
		}
	}

	@Override
	public IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence joinSql, CharSequence whereSql,
			List<String> additionalSelectColumnList, CharSequence orderBySql, int offset, int length, ILinkedMap<Integer, Object> params)
	{
		boolean join = joinSql != null && joinSql.length() > 0;
		String tableAlias = join ? "A" : null;
		return selectFields(tableName, fieldNamesSql, joinSql, whereSql, additionalSelectColumnList, orderBySql, offset, length, params, tableAlias);
	}

	@Override
	public IResultSet selectFields(String tableName, CharSequence fieldNamesSql, CharSequence joinSql, CharSequence whereSql,
			List<String> additionalSelectColumnList, CharSequence orderBySql, int offset, int length, ILinkedMap<Integer, Object> params, String tableAlias)
	{
		boolean join = joinSql != null && joinSql.length() > 0;
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();

		StringBuilder sb = tlObjectCollector.create(StringBuilder.class);

		CharSequence outerFieldNamesSql, innerFieldNamesSql;
		if (tableAlias == null)
		{
			outerFieldNamesSql = fieldNamesSql;
			innerFieldNamesSql = fieldNamesSql;
		}
		else
		{
			Matcher fieldWithAliasMatcher = fieldWithAlias.matcher(fieldNamesSql);
			outerFieldNamesSql = fieldWithAliasMatcher.replaceAll(outerFieldPattern);
			innerFieldNamesSql = fieldWithAliasMatcher.replaceAll(innerFieldPattern);
		}

		try
		{
			sb.append("SELECT ").append(outerFieldNamesSql).append(" FROM (SELECT");
			if (join)
			{
				sb.append(" DISTINCT");
			}
			sb.append(" ROW_NUMBER() OVER");
			if (orderBySql != null && orderBySql.length() > 0)
			{
				sb.append(" (").append(orderBySql).append(")");
			}
			sb.append(" AS rn,").append(innerFieldNamesSql);

			if (additionalSelectColumnList != null)
			{
				for (int a = 0, size = additionalSelectColumnList.size(); a < size; a++)
				{
					String additionalSelectColumn = additionalSelectColumnList.get(a);
					// additionalSelectColumn is expected to be already escaped at this point. No need to double escape
					sb.append(',').append(additionalSelectColumn);
				}
			}
			sb.append(" FROM ");
			sqlBuilder.appendName(tableName, sb);
			if (tableAlias != null)
			{
				sb.append(" ").append(tableAlias).append(" ").append(joinSql);
			}
			if (whereSql != null && whereSql.length() > 0)
			{
				sb.append(" WHERE ").append(whereSql);
			}
			sb.append(") WHERE rn>? AND rn<=?");

			ParamsUtil.addParam(params, offset);
			ParamsUtil.addParam(params, offset + length);

			return sqlSelect(sb.toString(), params);
		}
		finally
		{
			tlObjectCollector.dispose(sb);
		}
	}

	@Override
	public IResultSet createResultSet(final String tableName, final String idFieldName, final Class<?> idFieldType, final String fieldsSQL,
			final String additionalWhereSQL, List<?> ids)
	{
		if (ids == null || ids.size() == 0)
		{
			return EmptyResultSet.instance;
		}
		if (ids.size() <= maxInClauseBatchThreshold)
		{
			return createResultSetIntern(tableName, idFieldName, idFieldType, fieldsSQL, additionalWhereSQL, ids);
		}
		IList<IList<Object>> splitValues = persistenceHelper.splitValues(ids, maxInClauseBatchThreshold);

		ArrayList<IResultSetProvider> resultSetProviderStack = new ArrayList<IResultSetProvider>(splitValues.size());
		// Stack gets evaluated last->first so back iteration is correct to execute the sql in order later
		for (int a = splitValues.size(); a-- > 0;)
		{
			final IList<Object> values = splitValues.get(a);
			resultSetProviderStack.add(new IResultSetProvider()
			{
				@Override
				public void skipResultSet()
				{
					// Intended blank
				}

				@Override
				public IResultSet getResultSet()
				{
					return createResultSetIntern(tableName, idFieldName, idFieldType, fieldsSQL, additionalWhereSQL, values);
				}
			});
		}
		CompositeResultSet compositeResultSet = new CompositeResultSet();
		compositeResultSet.setResultSetProviderStack(resultSetProviderStack);
		compositeResultSet.afterPropertiesSet();
		return compositeResultSet;
	}

	protected IResultSet createResultSetIntern(String tableName, String idFieldName, Class<?> idFieldType, String fieldsSQL, String additionalWhereSQL,
			List<?> ids)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		LinkedHashMap<Integer, Object> params = new LinkedHashMap<Integer, Object>();
		StringBuilder whereSB = tlObjectCollector.create(StringBuilder.class);
		try
		{
			persistenceHelper.appendSplittedValues(idFieldName, idFieldType, ids, params, whereSB);
			if (additionalWhereSQL != null)
			{
				whereSB.append(" AND ").append(additionalWhereSQL);
			}
			// if (forceOrder)
			// {
			// whereSB.Append(" ORDER BY ");
			// SqlBuilder.Append(idFieldName, whereSB);
			// }
			return selectFields(tableName, fieldsSQL, whereSB.toString(), params);
		}
		finally
		{
			tlObjectCollector.dispose(whereSB);
		}
	}

	protected abstract Object createArray(String tableName, String idFieldName, List<?> ids);

	protected abstract void disposeArray(Object array);

}