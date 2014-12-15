package de.osthus.ambeth.query;

import java.util.List;

import de.osthus.ambeth.appendable.AppendableStringBuilder;
import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;

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
	protected List<ISqlJoin> joinClauses = EmptyList.getInstance();

	@Property(mandatory = false)
	protected List<ISqlJoin> allJoinClauses = EmptyList.getInstance();

	protected boolean join;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		join = allJoinClauses != null && !allJoinClauses.isEmpty();
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
