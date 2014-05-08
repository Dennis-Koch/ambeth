package de.osthus.ambeth.query.sql;

import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.ParamChecker;

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
