package de.osthus.ambeth.query.sql;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.filter.QueryConstants;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.query.IMultiValueOperand;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IValueOperand;
import de.osthus.ambeth.sql.ParamsUtil;
import de.osthus.ambeth.util.ParamChecker;

public class SimpleValueOperand implements IOperand, IValueOperand, IMultiValueOperand, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IEntityMetaDataProvider entityMetaDataProvider;

	protected ListToSqlUtil listToSqlUtil;

	protected IThreadLocalObjectCollector objectCollector;

	protected String paramName;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(entityMetaDataProvider, "EntityMetaDataProvider");
		ParamChecker.assertNotNull(listToSqlUtil, "listToSqlUtil");
		ParamChecker.assertNotNull(objectCollector, "ObjectCollector");
		ParamChecker.assertNotNull(paramName, "paramName");
	}

	public void setEntityMetaDataProvider(IEntityMetaDataProvider entityMetaDataProvider)
	{
		this.entityMetaDataProvider = entityMetaDataProvider;
	}

	public void setListToSqlUtil(ListToSqlUtil listToSqlUtil)
	{
		this.listToSqlUtil = listToSqlUtil;
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	public void setParamName(String paramName)
	{
		this.paramName = paramName;
	}

	@Override
	public boolean isNull(Map<Object, Object> nameToValueMap)
	{
		return getValue(nameToValueMap) == null;
	}

	@Override
	public boolean isNullOrEmpty(Map<Object, Object> nameToValueMap)
	{
		Object value = getValue(nameToValueMap);
		if (value == null)
		{
			return true;
		}
		else if (value instanceof Collection)
		{
			return ((Collection<?>) value).isEmpty();
		}
		else if (value.getClass().isArray())
		{
			return Array.getLength(value) == 0;
		}
		return "".equals(value);
	}

	protected Object getValueIntern(Map<Object, Object> nameToValueMap)
	{
		Object value = nameToValueMap.get(paramName);
		if (value == null)
		{
			if (!nameToValueMap.containsKey(paramName))
			{
				throw new IllegalArgumentException("No entry for paramName '" + paramName + "' found to expand query");
			}
		}
		return value;
	}

	@Override
	public Object getValue(Map<Object, Object> nameToValueMap)
	{
		Object value = getValueIntern(nameToValueMap);
		return listToSqlUtil.extractValue(value, nameToValueMap);
	}

	@Override
	public IList<Object> getMultiValue(Map<Object, Object> nameToValueMap)
	{
		Object value = getValueIntern(nameToValueMap);
		ArrayList<Object> items = new ArrayList<Object>();
		listToSqlUtil.extractValueList(value, items, nameToValueMap);
		return items;
	}

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		Object value = getValue(nameToValueMap);

		if (parameters != null)
		{
			String preValue = (String) nameToValueMap.get(QueryConstants.PRE_VALUE_KEY);
			String postValue = (String) nameToValueMap.get(QueryConstants.POST_VALUE_KEY);
			if (preValue != null || postValue != null)
			{
				IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
				StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
				if (preValue != null)
				{
					sb.append(preValue);
				}
				if (value == null)
				{
					sb.append("NULL");
				}
				else
				{
					sb.append(value);
				}
				if (postValue != null)
				{
					sb.append(postValue);
				}
				value = sb.toString();
				tlObjectCollector.dispose(sb);
			}
			if (value != null)
			{
				ParamsUtil.addParam(parameters, value);
				querySB.append('?');
			}
			else
			{
				querySB.append("NULL");
			}
		}
		else
		{
			listToSqlUtil.expandValue(querySB, value, this, nameToValueMap);
		}
	}
}
