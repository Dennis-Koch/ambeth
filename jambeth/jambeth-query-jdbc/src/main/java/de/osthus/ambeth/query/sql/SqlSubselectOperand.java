package de.osthus.ambeth.query.sql;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.ISubQuery;
import de.osthus.ambeth.util.ParamChecker;

public class SqlSubselectOperand implements IOperand, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance(SqlSubselectOperand.class)
	private ILogger log;

	protected ISubQuery<?> subQuery;

	protected SqlColumnOperand[] selectedColumns;

	protected IDatabase database;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(subQuery, "subQuery");
		ParamChecker.assertNotNull(selectedColumns, "selectedColumns");

		ParamChecker.assertNotNull(database, "database");
	}

	public void setSubQuery(ISubQuery<?> subQuery)
	{
		this.subQuery = subQuery;
	}

	public void setSelectedColumns(SqlColumnOperand[] selectedColumns)
	{
		this.selectedColumns = selectedColumns;
	}

	public void setDatabase(IDatabase database)
	{
		this.database = database;
	}

	public ISubQuery<?> getSubQuery()
	{
		return subQuery;
	}

	@Override
	public void expandQuery(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) throws IOException
	{
		Class<?> entityType = subQuery.getEntityType();
		ITable table = database.getTableByType(entityType);
		String tableName = table.getFullqualifiedEscapedName();
		String tableAlias = subQuery.getMainTableAlias();

		String[] sqlParts = subQuery.getSqlParts(nameToValueMap, parameters, Collections.<String> emptyList());
		String joinSql = sqlParts[0];
		String whereSql = sqlParts[1];
		String orderBySql = sqlParts[2];

		querySB.append("SELECT ");
		querySB.append(tableAlias).append(".").append(selectedColumns[0].columnName);
		for (int i = 1; i < selectedColumns.length; i++)
		{
			SqlColumnOperand column = selectedColumns[i];
			querySB.append(",").append(tableAlias).append(".").append(column.columnName);
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
	}
}