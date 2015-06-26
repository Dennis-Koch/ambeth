package de.osthus.ambeth.query.sql;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IDatabaseMetaData;
import de.osthus.ambeth.persistence.ITableMetaData;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.ISubQuery;
import de.osthus.ambeth.util.ParamChecker;

public class SqlSubselectOperand implements IOperand, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected ISubQuery<?> subQuery;

	protected SqlColumnOperand[] selectedColumns;

	protected IDatabaseMetaData databaseMetaData;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(subQuery, "subQuery");
		ParamChecker.assertNotNull(selectedColumns, "selectedColumns");

		ParamChecker.assertNotNull(databaseMetaData, "database");
	}

	public void setSubQuery(ISubQuery<?> subQuery)
	{
		this.subQuery = subQuery;
	}

	public void setSelectedColumns(SqlColumnOperand[] selectedColumns)
	{
		this.selectedColumns = selectedColumns;
	}

	public void setDatabase(IDatabaseMetaData databaseMetaData)
	{
		this.databaseMetaData = databaseMetaData;
	}

	public ISubQuery<?> getSubQuery()
	{
		return subQuery;
	}

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		Class<?> entityType = subQuery.getEntityType();
		ITableMetaData table = databaseMetaData.getTableByType(entityType);
		String tableName = table.getFullqualifiedEscapedName();
		String tableAlias = subQuery.getMainTableAlias();

		String[] sqlParts = subQuery.getSqlParts(nameToValueMap, parameters, EmptyList.<String> getInstance());
		String joinSql = sqlParts[0];
		String whereSql = sqlParts[1];
		String orderBySql = sqlParts[2];
		String limitSql = sqlParts[3];

		querySB.append("SELECT ");
		boolean firstColumn = true;
		SqlColumnOperand[] selectedColumns = this.selectedColumns;
		for (int i = 0, size = selectedColumns.length; i < size; i++)
		{
			if (firstColumn)
			{
				firstColumn = false;
			}
			else
			{
				querySB.append(',');
			}
			SqlColumnOperand column = selectedColumns[i];
			querySB.append(tableAlias).append(".").append('"').append(column.columnName).append('"');
		}
		querySB.append(" FROM ").append(tableName).append(" ").append(tableAlias);
		if (joinSql != null && !joinSql.isEmpty())
		{
			querySB.append(" ").append(joinSql);
		}
		if (whereSql != null && !whereSql.isEmpty())
		{
			querySB.append(" WHERE ").append(whereSql);
		}
		if (orderBySql != null && !orderBySql.isEmpty())
		{
			querySB.append(" ").append(orderBySql);
		}
		if (limitSql != null && !limitSql.isEmpty())
		{
			querySB.append(" ").append(limitSql);
		}
	}
}