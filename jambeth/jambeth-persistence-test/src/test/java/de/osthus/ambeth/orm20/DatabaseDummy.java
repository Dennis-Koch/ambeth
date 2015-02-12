package de.osthus.ambeth.orm20;

import java.util.List;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IContextProvider;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IDatabaseDisposeHook;
import de.osthus.ambeth.persistence.IDatabaseMetaData;
import de.osthus.ambeth.persistence.IDatabasePool;
import de.osthus.ambeth.persistence.ILink;
import de.osthus.ambeth.persistence.ISavepoint;
import de.osthus.ambeth.persistence.ITable;

public class DatabaseDummy implements IDatabase
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void dispose()
	{
	}

	@Override
	public <T> T getAutowiredBeanInContext(Class<T> autowiredType)
	{
		return null;
	}

	@Override
	public <T> T getNamedBeanInContext(String beanName, Class<T> expectedType)
	{
		return null;
	}

	@Override
	public long getSessionId()
	{
		return 0;
	}

	@Override
	public void setSessionId(long sessionId)
	{
	}

	@Override
	public IContextProvider getContextProvider()
	{
		return null;
	}

	@Override
	public IDatabasePool getPool()
	{
		return null;
	}

	@Override
	public void flushAndRelease()
	{
	}

	@Override
	public void release(boolean errorOccured)
	{
	}

	@Override
	public void acquired(boolean readOnly)
	{
	}

	@Override
	public IDatabase getCurrent()
	{
		return null;
	}

	@Override
	public String getName()
	{
		return null;
	}

	@Override
	public String[] getSchemaNames()
	{
		return null;
	}

	@Override
	public List<ITable> getTables()
	{
		return null;
	}

	@Override
	public List<ILink> getLinks()
	{
		return null;
	}

	@Override
	public ITable getTableByType(Class<?> entityType)
	{
		return null;
	}

	@Override
	public ITable getArchiveTableByType(Class<?> entityType)
	{
		return null;
	}

	@Override
	public ITable getTableByName(String tableName)
	{
		return null;
	}

	@Override
	public boolean test()
	{
		return false;
	}

	@Override
	public void flush()
	{
	}

	@Override
	public void revert()
	{
	}

	@Override
	public void revert(ISavepoint savepoint)
	{
	}

	@Override
	public ISavepoint setSavepoint()
	{
		return null;
	}

	@Override
	public void releaseSavepoint(ISavepoint savepoint)
	{
	}

	@Override
	public void rollback(ISavepoint savepoint)
	{
	}

	@Override
	public IList<String[]> disableConstraints()
	{
		return null;
	}

	@Override
	public void enableConstraints(IList<String[]> disabled)
	{
	}

	@Override
	public List<ILink> getLinksByTables(ITable table1, ITable table2)
	{
		return null;
	}

	@Override
	public void registerDisposeHook(IDatabaseDisposeHook disposeHook)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void unregisterDisposeHook(IDatabaseDisposeHook disposeHook)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isDisposed()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IDatabaseMetaData getMetaData()
	{
		return null;
	}
}
