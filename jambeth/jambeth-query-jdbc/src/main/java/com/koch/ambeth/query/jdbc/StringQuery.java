package com.koch.ambeth.query.jdbc;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.ISqlJoin;
import com.koch.ambeth.query.jdbc.sql.SqlQueryBuilder;
import com.koch.ambeth.util.appendable.AppendableStringBuilder;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public class StringQuery implements IStringQuery, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Property
	protected IOperand rootOperand;

	@Property
	protected Class<?> entityType;

	@Property(mandatory = false)
	protected ISqlJoin[] joinClauses = SqlQueryBuilder.emptyJoins;

	@Property(mandatory = false)
	protected ISqlJoin[] allJoinClauses = SqlQueryBuilder.emptyJoins;

	protected boolean join;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		join = allJoinClauses.length > 0;
	}

	@Override
	public boolean isJoinQuery()
	{
		return join;
	}

	@Override
	public String fillQuery(IList<Object> parameters)
	{
		return fillQuery(null, parameters);
	}

	@Override
	public String fillQuery(IMap<Object, Object> nameToValueMap, IList<Object> parameters)
	{
		if (rootOperand == null)
		{
			return null;
		}
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		AppendableStringBuilder whereSB = tlObjectCollector.create(AppendableStringBuilder.class);
		try
		{
			rootOperand.expandQuery(whereSB, nameToValueMap, false, parameters);
			return whereSB.toString();
		}
		finally
		{
			tlObjectCollector.dispose(whereSB);
		}
	}

	@Override
	public String[] fillJoinQuery(IMap<Object, Object> nameToValueMap, IList<Object> parameters)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		AppendableStringBuilder whereSB = tlObjectCollector.create(AppendableStringBuilder.class);
		AppendableStringBuilder joinSB = tlObjectCollector.create(AppendableStringBuilder.class);
		try
		{
			nameToValueMap.put("#JoinSB", joinSB);

			for (int i = 0, size = joinClauses.length; i < size; i++)
			{
				if (i > 0)
				{
					joinSB.append(' ');
				}
				joinClauses[i].expandQuery(joinSB, nameToValueMap, true, parameters);
			}
			if (rootOperand != null)
			{
				rootOperand.expandQuery(whereSB, nameToValueMap, true, parameters);
			}
			return new String[] { joinSB.length() > 0 ? joinSB.toString() : null, whereSB.length() > 0 ? whereSB.toString() : null };
		}
		finally
		{
			nameToValueMap.remove("#JoinSB");
			tlObjectCollector.dispose(joinSB);
			tlObjectCollector.dispose(whereSB);
		}
	}
}
