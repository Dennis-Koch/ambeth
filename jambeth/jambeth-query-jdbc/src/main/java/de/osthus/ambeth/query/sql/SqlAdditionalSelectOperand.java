package de.osthus.ambeth.query.sql;

import java.util.List;

import de.osthus.ambeth.appendable.AppendableStringBuilder;
import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.filter.QueryConstants;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IOperator;
import de.osthus.ambeth.util.ParamChecker;

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
