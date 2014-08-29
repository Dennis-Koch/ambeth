package de.osthus.ambeth.query.sql;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.filter.QueryConstants;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IOperatorAwareOperand;
import de.osthus.ambeth.query.OperandConstants;
import de.osthus.ambeth.util.ParamChecker;

public class SqlColumnOperand implements IOperand, IOperatorAwareOperand, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected String columnName;

	protected Class<?> entityType;

	protected String propertyName;

	protected Class<?> columnType;

	protected Class<?> columnSubType;

	protected SqlJoinOperator joinClause;

	protected ITableAliasHolder tableAliasHolder;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(columnName, "ColumnName");
		ParamChecker.assertNotNull(tableAliasHolder, "tableAliasHolder");
	}

	public Class<?> getColumnType()
	{
		return columnType;
	}

	public Class<?> getColumnSubType()
	{
		return columnSubType;
	}

	public void setColumnName(String columnName)
	{
		this.columnName = columnName;
	}

	public void setColumnType(Class<?> columnType)
	{
		this.columnType = columnType;
	}

	public void setColumnSubType(Class<?> columnSubType)
	{
		this.columnSubType = columnSubType;
	}

	public void setEntityType(Class<?> entityType)
	{
		this.entityType = entityType;
	}

	public void setJoinClause(SqlJoinOperator joinClause)
	{
		this.joinClause = joinClause;
	}

	public void setTableAliasHolder(ITableAliasHolder tableAliasHolder)
	{
		this.tableAliasHolder = tableAliasHolder;
	}

	public void setPropertyName(String propertyName)
	{
		this.propertyName = propertyName;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void operatorStart(Map<Object, Object> nameToValueMap)
	{
		IList<Class<?>> entityTypeStack = (IList<Class<?>>) nameToValueMap.get(OperandConstants.EntityType);
		if (entityTypeStack == null)
		{
			entityTypeStack = new ArrayList<Class<?>>();
			nameToValueMap.put(OperandConstants.EntityType, entityTypeStack);
		}
		entityTypeStack.add(entityType);

		IList<String> propertyNameStack = (IList<String>) nameToValueMap.get(OperandConstants.PropertyName);
		if (propertyNameStack == null)
		{
			propertyNameStack = new ArrayList<String>();
			nameToValueMap.put(OperandConstants.PropertyName, propertyNameStack);
		}
		propertyNameStack.add(propertyName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void operatorEnd(Map<Object, Object> nameToValueMap)
	{
		IList<Class<?>> entityTypeStack = (IList<Class<?>>) nameToValueMap.get(OperandConstants.EntityType);
		entityTypeStack.remove(entityTypeStack.size() - 1);
		if (entityTypeStack.size() == 0)
		{
			nameToValueMap.remove(OperandConstants.EntityType);
		}
		IList<String> propertyNameStack = (IList<String>) nameToValueMap.get(OperandConstants.PropertyName);
		propertyNameStack.remove(propertyNameStack.size() - 1);
		if (propertyNameStack.size() == 0)
		{
			nameToValueMap.remove(OperandConstants.PropertyName);
		}
	}

	@Override
	public void expandQuery(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) throws IOException
	{
		if (joinQuery || Boolean.TRUE.equals(nameToValueMap.get(QueryConstants.USE_TABLE_ALIAS)))
		{
			if (joinClause != null)
			{
				querySB.append(joinClause.getTableAlias());
			}
			else
			{
				querySB.append(tableAliasHolder.getTableAlias());
			}
			querySB.append('.');
		}
		querySB.append('"').append(columnName).append('"');
	}
}
