package com.koch.ambeth.query.jdbc.sql;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.filter.QueryConstants;
import com.koch.ambeth.persistence.jdbc.IConnectionExtension;
import com.koch.ambeth.persistence.sql.ParamsUtil;
import com.koch.ambeth.query.IMultiValueOperand;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IValueOperand;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public class DirectValueOperand implements IOperand, IValueOperand, IMultiValueOperand
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConnectionExtension connectionExtension;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected ListToSqlUtil listToSqlUtil;

	@Property
	protected Object value;

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
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		Object value = getValue(nameToValueMap);
		Class<?> expectedTypeHint = (Class<?>) nameToValueMap.get(QueryConstants.EXPECTED_TYPE_HINT);
		if (expectedTypeHint != null)
		{
			value = conversionHelper.convertValueToType(expectedTypeHint, value);
		}
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
