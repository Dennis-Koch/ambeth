package de.osthus.ambeth.query;

import java.util.List;

import de.osthus.ambeth.appendable.AppendableStringBuilder;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.ParamChecker;

public class StringQuery implements IStringQuery, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IThreadLocalObjectCollector objectCollector;

	protected IOperand rootOperand;

	protected Class<?> entityType;

	protected List<ISqlJoin> joinClauses;

	protected boolean join = false;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(objectCollector, "objectCollector");
		ParamChecker.assertNotNull(rootOperand, "rootOperand");

		join = joinClauses != null && !joinClauses.isEmpty();
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	public void setRootOperand(IOperand rootOperand)
	{
		this.rootOperand = rootOperand;
	}

	public void setEntityType(Class<?> entityType)
	{
		this.entityType = entityType;
	}

	public void setJoinClauses(List<ISqlJoin> joinClauses)
	{
		this.joinClauses = joinClauses;
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
			nameToValueMap.put("JoinSB", joinSB);

			for (int i = 0; i < joinClauses.size(); i++)
			{
				if (i > 0)
				{
					joinSB.append(' ');
				}
				joinClauses.get(i).expandQuery(joinSB, nameToValueMap, true, parameters);
			}
			rootOperand.expandQuery(whereSB, nameToValueMap, true, parameters);
			return new String[] { joinSB.toString(), whereSB.toString() };
		}
		finally
		{
			nameToValueMap.remove("JoinSB");
			tlObjectCollector.dispose(joinSB);
			tlObjectCollector.dispose(whereSB);
		}
	}
}
