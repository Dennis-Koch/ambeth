package de.osthus.ambeth.query.sql;

import javax.persistence.criteria.JoinType;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.ISqlJoin;
import de.osthus.ambeth.util.ParamChecker;

public class SqlJoinOperator implements ISqlJoin, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected JoinType joinType = JoinType.LEFT;

	protected IOperand clause;

	protected String fullqualifiedEscapedTableName;

	protected String tableName;

	protected String tableAlias;

	protected IOperand joinedColumn;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(clause, "clause");
		ParamChecker.assertNotNull(tableName, "tableName");
		ParamChecker.assertFalse(tableName.isEmpty(), "tableName.isNotEmpty");
		ParamChecker.assertNotNull(fullqualifiedEscapedTableName, "fullqualifiedEscapedTableName");
		ParamChecker.assertFalse(fullqualifiedEscapedTableName.isEmpty(), "fullqualifiedEscapedTableName.isNotEmpty");
	}

	public void setClause(IOperand clause)
	{
		this.clause = clause;
	}

	public void setJoinType(JoinType joinType)
	{
		this.joinType = joinType;
	}

	@Override
	public String getFullqualifiedEscapedTableName()
	{
		return fullqualifiedEscapedTableName;
	}

	public void setFullqualifiedEscapedTableName(String fullqualifiedEscapedTableName)
	{
		this.fullqualifiedEscapedTableName = fullqualifiedEscapedTableName;
	}

	@Override
	public String getTableName()
	{
		return tableName;
	}

	public void setTableName(String tableName)
	{
		this.tableName = tableName;
	}

	public String getTableAlias()
	{
		return tableAlias;
	}

	public void setTableAlias(String tableAlias)
	{
		this.tableAlias = tableAlias;
	}

	@Override
	public IOperand getJoinedColumn()
	{
		return joinedColumn;
	}

	public void setJoinedColumn(IOperand joinedColumn)
	{
		this.joinedColumn = joinedColumn;
	}

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		operate(querySB, nameToValueMap, joinQuery, parameters);
	}

	@Override
	public void operate(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		if (!joinQuery)
		{
			throw new IllegalStateException("Join clause in non-join statement!");
		}

		switch (joinType)
		{
			case INNER:
				querySB.append("INNER");
				break;
			case LEFT:
				querySB.append("LEFT OUTER");
				break;
			case RIGHT:
				querySB.append("RIGHT OUTER");
				break;
			default:
				throw RuntimeExceptionUtil.createEnumNotSupportedException(joinType);
		}
		querySB.append(" JOIN ").append(fullqualifiedEscapedTableName).append(' ').append(getTableAlias()).append(" ON ");
		clause.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
	}
}
