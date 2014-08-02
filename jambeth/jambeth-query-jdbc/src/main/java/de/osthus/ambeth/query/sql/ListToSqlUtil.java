package de.osthus.ambeth.query.sql;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.OperandConstants;
import de.osthus.ambeth.sql.ISqlBuilder;
import de.osthus.ambeth.util.ParamChecker;

public final class ListToSqlUtil implements IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IEntityMetaDataProvider entityMetaDataProvider;

	protected ISqlBuilder sqlBuilder;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(entityMetaDataProvider, "EntityMetaDataProvider");
		ParamChecker.assertNotNull(sqlBuilder, "SqlBuilder");
	}

	public void setEntityMetaDataProvider(IEntityMetaDataProvider entityMetaDataProvider)
	{
		this.entityMetaDataProvider = entityMetaDataProvider;
	}

	public void setSqlBuilder(ISqlBuilder sqlBuilder)
	{
		this.sqlBuilder = sqlBuilder;
	}

	public void expandValue(Appendable querySB, Object value, IOperand self, Map<Object, Object> nameToValueMap) throws IOException
	{
		expandValue(querySB, value, self, nameToValueMap, null, null);
	}

	public void expandValue(Appendable querySB, Object value, IOperand self, Map<Object, Object> nameToValueMap, String prefix, String suffix)
			throws IOException
	{
		if (value instanceof List)
		{
			List<?> list = (List<?>) value;
			for (int a = 0, size = list.size(); a < size; a++)
			{
				if (a > 0)
				{
					querySB.append(',');
				}
				Object item = list.get(a);
				if (prefix != null)
				{
					querySB.append(prefix);
				}
				expandItem(querySB, item, self, nameToValueMap);
				if (suffix != null)
				{
					querySB.append(suffix);
				}
			}
		}
		else if (value instanceof Collection)
		{
			boolean first = true;
			Iterator<?> iter = ((Collection<?>) value).iterator();
			while (iter.hasNext())
			{
				Object item = iter.next();
				if (first)
				{
					first = false;
				}
				else
				{
					querySB.append(',');
				}
				if (prefix != null)
				{
					querySB.append(prefix);
				}
				expandItem(querySB, item, self, nameToValueMap);
				if (suffix != null)
				{
					querySB.append(suffix);
				}
			}
		}
		else if (value != null && value.getClass().isArray())
		{
			int size = Array.getLength(value);
			for (int a = 0; a < size; a++)
			{
				Object item = Array.get(value, a);
				if (a > 0)
				{
					querySB.append(',');
				}
				if (prefix != null)
				{
					querySB.append(prefix);
				}
				expandItem(querySB, item, self, nameToValueMap);
				if (suffix != null)
				{
					querySB.append(suffix);
				}
			}
		}
		else
		{
			if (prefix != null)
			{
				querySB.append(prefix);
			}
			expandItem(querySB, value, self, nameToValueMap);
			if (suffix != null)
			{
				querySB.append(suffix);
			}
		}
	}

	public void extractValueList(Object value, List<Object> items)
	{
		extractValueList(value, items, (String) null);
	}

	protected void extractValueList(Object value, List<Object> items, String propertyName)
	{
		if (value instanceof List)
		{
			List<?> list = (List<?>) value;
			for (int a = 0, size = list.size(); a < size; a++)
			{
				Object item = list.get(a);
				extractValueList(item, items, propertyName);
			}
		}
		else if (value instanceof Collection)
		{
			Iterator<?> iter = ((Collection<?>) value).iterator();
			while (iter.hasNext())
			{
				Object item = iter.next();
				extractValueList(item, items, propertyName);
			}
		}
		else if (value != null && value.getClass().isArray())
		{
			int size = Array.getLength(value);
			for (int a = 0; a < size; a++)
			{
				Object item = Array.get(value, a);
				extractValueList(item, items, propertyName);
			}
		}
		else
		{
			value = extractValue(value, propertyName);
			items.add(value);
		}
	}

	@SuppressWarnings("unchecked")
	public void extractValueList(Object value, List<Object> items, Map<Object, Object> nameToValueMap)
	{
		String propertyName = null;
		List<String> propertyNameStack = (List<String>) nameToValueMap.get(OperandConstants.PropertyName);
		if (propertyNameStack != null)
		{
			propertyName = propertyNameStack.get(propertyNameStack.size() - 1);
		}
		extractValueList(value, items, propertyName);
	}

	@SuppressWarnings("unchecked")
	public Object extractValue(Object value, Map<Object, Object> nameToValueMap)
	{
		if (value == null)
		{
			return null;
		}
		String propertyName = null;
		List<String> propertyNameStack = (List<String>) nameToValueMap.get(OperandConstants.PropertyName);
		if (propertyNameStack != null)
		{
			propertyName = propertyNameStack.get(propertyNameStack.size() - 1);
		}
		return extractValue(value, propertyName);
	}

	protected Object extractValue(Object value, String propertyName)
	{
		if (propertyName == null || value == null)
		{
			return value;
		}
		IEntityMetaData valueMetaData = entityMetaDataProvider.getMetaData(value.getClass(), true);
		if (valueMetaData == null)
		{
			return value;
		}
		Member member = valueMetaData.getMemberByName(propertyName);
		return member.getValue(value);
	}

	protected void expandItem(Appendable querySB, Object value, IOperand self, Map<Object, Object> nameToValueMap) throws IOException
	{
		if (value == null)
		{
			querySB.append("NULL");
		}
		else if (value instanceof String)
		{
			if (SqlEscapeHelper.escapeIfNecessary(self, nameToValueMap))
			{
				querySB.append('\'');
			}
			sqlBuilder.escapeValue((String) value, querySB);
			if (SqlEscapeHelper.unescapeIfNecessary(self, nameToValueMap))
			{
				querySB.append('\'');
			}
		}
		else
		{
			querySB.append(value.toString());
		}
	}
}
