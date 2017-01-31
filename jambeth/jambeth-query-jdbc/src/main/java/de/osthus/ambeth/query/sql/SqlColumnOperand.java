package de.osthus.ambeth.query.sql;

import java.util.Map;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.filter.QueryConstants;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IOperatorAwareOperand;
import de.osthus.ambeth.query.OperandConstants;

public class SqlColumnOperand implements IOperand, IOperatorAwareOperand
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property
	protected String columnName;

	@Property(mandatory = false)
	protected Class<?> entityType;

	@Property(mandatory = false)
	protected String propertyName;

	@Property(mandatory = false)
	protected Class<?> columnType;

	@Property(mandatory = false)
	protected Class<?> columnSubType;

	@Property(mandatory = false)
	protected SqlJoinOperator joinClause;

	@Autowired
	protected IConnectionDialect connectionDialect;

	@Autowired
	protected ITableAliasHolder tableAliasHolder;

	public Class<?> getColumnType()
	{
		return columnType;
	}

	public Class<?> getColumnSubType()
	{
		return columnSubType;
	}

	public void setColumnType(Class<?> columnType)
	{
		this.columnType = columnType;
	}

	public void setColumnSubType(Class<?> columnSubType)
	{
		this.columnSubType = columnSubType;
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
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
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
		connectionDialect.escapeName(columnName, querySB);
	}

	@Override
	public String toString()
	{
		if (tableAliasHolder == null)
		{
			return columnName;
		}
		String tableAlias = tableAliasHolder.getTableAlias();
		if (tableAlias == null)
		{
			return columnName;
		}
		return tableAlias + '.' + columnName;
	}
}
