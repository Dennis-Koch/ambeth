package com.koch.ambeth.query.jdbc.sql;

import java.util.List;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.filter.QueryConstants;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IOperator;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public class SqlAdditionalSelectOperand implements IOperator, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IOperand column;

	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(column, "column");
		ParamChecker.assertNotNull(objectCollector, "objectCollector");
	}

	public void setColumn(IOperand column)
	{
		this.column = column;
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	@Override
	public void expandQuery(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		operate(querySB, nameToValueMap, joinQuery, parameters);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void operate(IAppendable querySB, IMap<Object, Object> nameToValueMap, boolean joinQuery, IList<Object> parameters)
	{
		List<String> additionalSelectColumnList = (List<String>) nameToValueMap.get(QueryConstants.ADDITIONAL_SELECT_SQL_SB);
		if (additionalSelectColumnList == null)
		{
			return;
		}
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		AppendableStringBuilder sb = tlObjectCollector.create(AppendableStringBuilder.class);
		try
		{
			column.expandQuery(sb, nameToValueMap, joinQuery, parameters);
			additionalSelectColumnList.add(sb.toString());
			if (querySB != null)
			{
				column.expandQuery(querySB, nameToValueMap, joinQuery, parameters);
			}
		}
		finally
		{
			tlObjectCollector.dispose(sb);
		}
	}
}
