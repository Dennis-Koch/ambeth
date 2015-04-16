package de.osthus.ambeth.query.sql;

import java.lang.reflect.Array;
import java.util.Collection;
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
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.jdbc.IConnectionExtension;
import de.osthus.ambeth.query.IMultiValueOperand;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IValueOperand;
import de.osthus.ambeth.sql.ParamsUtil;
import de.osthus.ambeth.util.IConversionHelper;

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
