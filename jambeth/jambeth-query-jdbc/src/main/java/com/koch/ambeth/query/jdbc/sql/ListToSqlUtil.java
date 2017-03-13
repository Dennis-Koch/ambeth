package com.koch.ambeth.query.jdbc.sql;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.api.sql.ISqlBuilder;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.OperandConstants;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IMap;

public final class ListToSqlUtil
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected ISqlBuilder sqlBuilder;

	public void expandValue(IAppendable querySB, Object value, IOperand self, IMap<Object, Object> nameToValueMap)
	{
		expandValue(querySB, value, self, nameToValueMap, null, null);
	}

	public void expandValue(IAppendable querySB, Object value, IOperand self, IMap<Object, Object> nameToValueMap, String prefix, String suffix)
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

	protected void expandItem(IAppendable querySB, Object value, IOperand self, IMap<Object, Object> nameToValueMap)
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
