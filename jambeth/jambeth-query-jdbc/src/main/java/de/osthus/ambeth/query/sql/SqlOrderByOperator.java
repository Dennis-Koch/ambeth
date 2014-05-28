package de.osthus.ambeth.query.sql;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import de.osthus.ambeth.filter.QueryConstants;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IOperator;
import de.osthus.ambeth.query.OrderByType;
import de.osthus.ambeth.util.ParamChecker;

public class SqlOrderByOperator implements IOperator, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected static final Pattern ignoredColumnNamesPattern = Pattern.compile("([A-Z_]+\\.)?\"?(ID|VERSION)\"?");

	protected IOperand column;

	protected IThreadLocalObjectCollector objectCollector;

	protected OrderByType orderByType;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(column, "column");
		ParamChecker.assertNotNull(objectCollector, "objectCollector");
		ParamChecker.assertNotNull(orderByType, "orderByType");
	}

	public void setColumn(IOperand column)
	{
		this.column = column;
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	public void setOrderByType(OrderByType orderByType)
	{
		this.orderByType = orderByType;
	}

	@Override
	public void expandQuery(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, Map<Integer, Object> params) throws IOException
	{
		operate(querySB, nameToValueMap, joinQuery, params);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void operate(Appendable querySB, Map<Object, Object> nameToValueMap, boolean joinQuery, Map<Integer, Object> params) throws IOException
	{
		Boolean firstOrderByState = (Boolean) nameToValueMap.get(QueryConstants.FIRST_ORDER_BY_STATE);
		List<String> additionalSelectColumnList = (List<String>) nameToValueMap.get(QueryConstants.ADDITIONAL_SELECT_SQL_SB);
		if (firstOrderByState == null || Boolean.TRUE.equals(firstOrderByState))
		{
			nameToValueMap.put(QueryConstants.FIRST_ORDER_BY_STATE, Boolean.FALSE);
			querySB.append(" ORDER BY ");
		}
		else
		{
			querySB.append(',');
		}
		if (additionalSelectColumnList != null)
		{
			IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
			StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
			try
			{
				column.expandQuery(sb, nameToValueMap, joinQuery, params);
				if (!ignoredColumnNamesPattern.matcher(sb).matches())
				{
					additionalSelectColumnList.add(sb.toString());
				}
				column.expandQuery(querySB, nameToValueMap, joinQuery, params);
			}
			finally
			{
				tlObjectCollector.dispose(sb);
			}
		}
		else
		{
			column.expandQuery(querySB, nameToValueMap, joinQuery, params);
		}
		switch (orderByType)
		{
			case ASC:
				querySB.append(" ASC");
				break;
			case DESC:
				querySB.append(" DESC");
				break;
			default:
				throw new IllegalStateException("Type " + orderByType + " not supported");
		}
	}
}
