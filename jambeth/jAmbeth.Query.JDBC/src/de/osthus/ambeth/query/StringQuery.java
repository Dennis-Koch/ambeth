package de.osthus.ambeth.query;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
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
	public String fillQuery(Map<Integer, Object> params)
	{
		return fillQuery(null, params);
	}

	@Override
	public String fillQuery(Map<Object, Object> nameToValueMap, Map<Integer, Object> params)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder whereSB = tlObjectCollector.create(StringBuilder.class);
		try
		{
			rootOperand.expandQuery(whereSB, nameToValueMap, false, params);
			return whereSB.toString();
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			tlObjectCollector.dispose(whereSB);
		}
	}

	@Override
	public String[] fillJoinQuery(Map<Object, Object> nameToValueMap, Map<Integer, Object> params)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder whereSB = tlObjectCollector.create(StringBuilder.class);
		StringBuilder joinSB = tlObjectCollector.create(StringBuilder.class);
		try
		{
			rootOperand.expandQuery(whereSB, nameToValueMap, true, params);
			joinClauses.get(0).expandQuery(joinSB, nameToValueMap, true, params);
			for (int i = 1; i < joinClauses.size(); i++)
			{
				joinSB.append(' ');
				joinClauses.get(i).expandQuery(joinSB, nameToValueMap, true, params);
			}
			return new String[] { joinSB.toString(), whereSB.toString() };
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			tlObjectCollector.dispose(whereSB);
		}
	}
}
