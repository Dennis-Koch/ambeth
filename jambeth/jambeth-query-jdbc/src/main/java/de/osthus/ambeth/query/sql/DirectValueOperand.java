package de.osthus.ambeth.query.sql;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.filter.QueryConstants;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.jdbc.IConnectionExtension;
import de.osthus.ambeth.query.IMultiValueOperand;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IValueOperand;
import de.osthus.ambeth.sql.ParamsUtil;
import de.osthus.ambeth.util.ParamChecker;

public class DirectValueOperand implements IOperand, IValueOperand, IMultiValueOperand, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IConnectionExtension connectionExtension;

	protected IThreadLocalObjectCollector objectCollector;

	protected ListToSqlUtil listToSqlUtil;

	protected Object value;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(connectionExtension, "ConnectionExtension");
		ParamChecker.assertNotNull(listToSqlUtil, "listToSqlUtil");
		ParamChecker.assertNotNull(objectCollector, "objectCollector");
		ParamChecker.assertNotNull(value, "value");
	}

	public void setConnectionExtension(IConnectionExtension connectionExtension)
	{
		this.connectionExtension = connectionExtension;
	}

	public void setListToSqlUtil(ListToSqlUtil listToSqlUtil)
	{
		this.listToSqlUtil = listToSqlUtil;
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	public void setValue(Object value)
	{
		this.value = value;
	}

	@Override
	public boolean isNull(Map<Object, Object> nameToValueMap)
	{
		return value == null;
	}

	@Override
	public boolean isNullOrEmpty(Map<Object, Object> nameToValueMap)
	{
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

	@Override
	public Object getValue(Map<Object, Object> nameToValueMap)
	{
		return listToSqlUtil.extractValue(value, nameToValueMap);
	}

	@Override
	public IList<Object> getMultiValue(Map<Object, Object> nameToValueMap)
	{
		ArrayList<Object> items = new ArrayList<Object>();
		listToSqlUtil.extractValueList(value, items, nameToValueMap);
		return items;
	}

	@Override
	public void expandQuery(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, List<Object> parameters) throws IOException
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
				sb.append(value);
				if (postValue != null)
				{
					sb.append(postValue);
				}
				value = sb.toString();
				tlObjectCollector.dispose(sb);
			}
			ParamsUtil.addParam(parameters, value);
			querySB.append('?');
		}
		else
		{
			listToSqlUtil.expandValue(querySB, value, this, nameToValueMap);
		}
	}
}
