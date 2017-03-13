package com.koch.ambeth.query.jdbc.sql;

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

/**
 * Provides TableAliases to a Query and its SubQueries.
 * 
 * NOT thread-save! Each Query (not SubQuery) needs its own instance.
 */
public class TableAliasProvider implements ITableAliasProvider, IInitializingBean, IDisposableBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	private static final int lettersInTheAlphabet = 26;

	private static final char firstTableAlias = 'A';

	protected IThreadLocalObjectCollector objectCollector;

	protected StringBuilder sb;

	private int nextJoinIndex = 0;

	private int nextSubQueryIndex = 0;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(objectCollector, "objectCollector");

		sb = objectCollector.create(StringBuilder.class);
	}

	@Override
	public void destroy() throws Throwable
	{
		objectCollector.dispose(sb);
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	@Override
	public String getNextJoinAlias()
	{
		return getNextAlias("J_", nextJoinIndex++);
	}

	@Override
	public String getNextSubQueryAlias()
	{
		return getNextAlias("S_", nextSubQueryIndex++);
	}

	private String getNextAlias(String prefix, int index)
	{
		try
		{
			sb.append(prefix);
			appendNextAlias(index);
			return sb.toString();
		}
		finally
		{
			sb.setLength(0);
		}
	}

	private void appendNextAlias(int index)
	{
		int mine = index % lettersInTheAlphabet;
		int others = index / lettersInTheAlphabet;

		if (others > 0)
		{
			appendNextAlias(others - 1);
		}
		sb.append((char) (firstTableAlias + mine));
	}
}
