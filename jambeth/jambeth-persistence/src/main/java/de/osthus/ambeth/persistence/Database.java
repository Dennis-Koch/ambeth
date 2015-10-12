package de.osthus.ambeth.persistence;

import java.util.List;
import java.util.Map;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Factory;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.database.IDatabaseProvider;
import de.osthus.ambeth.ioc.DefaultExtendableContainer;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.parallel.IModifyingDatabase;
import de.osthus.ambeth.proxy.ICascadedInterceptor;
import de.osthus.ambeth.util.ParamChecker;

public class Database implements IDatabase, IInitializingBean, IStartingBean, IDisposableBean
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IContextProvider contextProvider;

	@Autowired
	protected IDatabaseProvider databaseProvider;

	@Autowired
	protected IDatabaseMetaData metaData;

	@Autowired
	protected IModifyingDatabase modifyingDatabase;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IDatabasePool pool;

	@Autowired
	protected IServiceContext serviceContext;

	protected final HashMap<String, ITable> nameToTableDict = new HashMap<String, ITable>();

	protected final HashMap<Class<?>, ITable> typeToTableDict = new HashMap<Class<?>, ITable>();

	protected final HashMap<Class<?>, ITable> typeToArchiveTableDict = new HashMap<Class<?>, ITable>();

	protected final HashMap<TablesMapKey, List<ILink>> tablesToLinkDict = new HashMap<TablesMapKey, List<ILink>>();

	protected final HashMap<Class<?>, IEntityHandler> typeToEntityHandler = new HashMap<Class<?>, IEntityHandler>();

	protected final DefaultExtendableContainer<IDatabaseDisposeHook> databaseDisposeHooks = new DefaultExtendableContainer<IDatabaseDisposeHook>(
			IDatabaseDisposeHook.class, "databaseDisposeHook");

	protected long sessionId;

	protected final ArrayList<ITable> tables = new ArrayList<ITable>();

	protected final List<ILink> links = new ArrayList<ILink>();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intended blank
	}

	@Override
	public void afterStarted()
	{
		// Intended blank
	}

	@Override
	public IDatabaseMetaData getMetaData()
	{
		return metaData;
	}

	@Override
	public void registerDisposeHook(IDatabaseDisposeHook disposeHook)
	{
		databaseDisposeHooks.register(disposeHook);
	}

	@Override
	public void unregisterDisposeHook(IDatabaseDisposeHook disposeHook)
	{
		databaseDisposeHooks.unregister(disposeHook);
	}

	@Override
	public boolean isDisposed()
	{
		return serviceContext.isDisposed();
	}

	@Override
	public <T> T getAutowiredBeanInContext(Class<T> autowiredType)
	{
		return serviceContext.getService(autowiredType, false);
	}

	@Override
	public <T> T getNamedBeanInContext(String beanName, Class<T> expectedType)
	{
		return serviceContext.getService(beanName, expectedType, false);
	}

	@Override
	public IContextProvider getContextProvider()
	{
		return contextProvider;
	}

	@Override
	public IDatabasePool getPool()
	{
		return pool;
	}

	public IServiceContext getServiceProvider()
	{
		return serviceContext;
	}

	public Map<Class<?>, IEntityHandler> getTypeToEntityHandler()
	{
		return typeToEntityHandler;
	}

	@Override
	public void acquired(boolean readOnly)
	{
		modifyingDatabase.setModifyingAllowed(!readOnly);
		contextProvider.acquired();
		contextProvider.setCurrentTime(Long.valueOf(System.currentTimeMillis()));
	}

	@Override
	public void flushAndRelease()
	{
		ThreadLocal<IDatabase> databaseLocal = databaseProvider.getDatabaseLocal();
		IDatabase currentDatabase = databaseLocal.get();
		if (currentDatabase instanceof Factory)
		{
			Callback interceptor = ((Factory) currentDatabase).getCallback(0);

			currentDatabase = (IDatabase) ((ICascadedInterceptor) interceptor).getTarget();
		}
		if (this == currentDatabase)
		{
			databaseLocal.remove();
		}
		try
		{
			flush();
		}
		finally
		{
			clear();
			if (pool != null)
			{
				pool.releaseDatabase(this, true);
			}
		}
	}

	@Override
	public void release(boolean errorOccured)
	{
		ThreadLocal<IDatabase> databaseLocal = databaseProvider.getDatabaseLocal();
		IDatabase currentDatabase = databaseLocal.get();
		if (currentDatabase instanceof Factory)
		{
			Callback interceptor = ((Factory) currentDatabase).getCallback(0);

			currentDatabase = (IDatabase) ((ICascadedInterceptor) interceptor).getTarget();
		}
		if (this == currentDatabase)
		{
			databaseLocal.remove();
		}
		clear();
		try
		{
			modifyingDatabase.setModifyingAllowed(true);
		}
		catch (Throwable e)
		{
			// intended blank
		}
		if (pool != null)
		{
			pool.releaseDatabase(this, !errorOccured);
		}
	}

	@Override
	public void destroy() throws Throwable
	{
		ThreadLocal<IDatabase> databaseLocal = databaseProvider.getDatabaseLocal();
		IDatabase currentDatabase = databaseLocal.get();
		if (currentDatabase instanceof Factory)
		{
			Callback interceptor = ((Factory) currentDatabase).getCallback(0);

			currentDatabase = (IDatabase) ((ICascadedInterceptor) interceptor).getTarget();
		}
		if (this == currentDatabase)
		{
			databaseLocal.remove();
		}
		clear();
		for (IDatabaseDisposeHook disposeHook : databaseDisposeHooks.getExtensions())
		{
			disposeHook.databaseDisposed(this);
		}
	}

	protected void clear()
	{
		contextProvider.clear();
	}

	@Override
	public void dispose()
	{
		serviceContext.dispose();
	}

	@Override
	public IDatabase getCurrent()
	{
		return this;
	}

	@Override
	public long getSessionId()
	{
		return sessionId;
	}

	@Override
	public void setSessionId(long sessionId)
	{
		this.sessionId = sessionId;
	}

	@Override
	public String getName()
	{
		return getMetaData().getName();
	}

	@Override
	public String[] getSchemaNames()
	{
		return getMetaData().getSchemaNames();
	}

	@Override
	public List<ITable> getTables()
	{
		return tables;
	}

	@Override
	public List<ILink> getLinks()
	{
		return links;
	}

	@Override
	public ITable getTableByName(String tableName)
	{
		return nameToTableDict.get(tableName);
	}

	@Override
	public List<ILink> getLinksByTables(ITable table1, ITable table2)
	{
		TablesMapKey tablesMapKey = new TablesMapKey(table1.getMetaData(), table2.getMetaData());
		return tablesToLinkDict.get(tablesMapKey);
	}

	@Override
	public ITable getTableByType(Class<?> entityType)
	{
		ParamChecker.assertParamNotNull(entityType, "entityType");
		ITable table = typeToTableDict.get(entityType);
		if (table == null)
		{
			throw new IllegalStateException("No table found for entity type '" + entityType.getName() + "'");
		}
		return table;
	}

	@Override
	public ITable getArchiveTableByType(Class<?> entityType)
	{
		return typeToArchiveTableDict.get(entityType);
	}

	@Override
	public void flush()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void revert()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void revert(ISavepoint savepoint)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public boolean test()
	{
		return true;
	}

	@Override
	public ISavepoint setSavepoint()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void releaseSavepoint(ISavepoint savepoint)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void rollback(ISavepoint savepoint)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IList<String> disableConstraints()
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void enableConstraints(IList<String> disabled)
	{
		throw new UnsupportedOperationException("Not implemented");
	}
}
