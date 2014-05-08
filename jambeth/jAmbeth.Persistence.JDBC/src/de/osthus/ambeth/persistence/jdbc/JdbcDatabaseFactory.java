package de.osthus.ambeth.persistence.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import de.osthus.ambeth.IDatabasePool;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.database.IDatabaseFactory;
import de.osthus.ambeth.database.IDatabaseMappedListenerExtendable;
import de.osthus.ambeth.database.IDatabaseMapper;
import de.osthus.ambeth.database.IDatabaseMapperExtendable;
import de.osthus.ambeth.database.IDatabaseProvider;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IBeanContextAware;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.RegisterPhaseDelegate;
import de.osthus.ambeth.ioc.extendable.ExtendableContainer;
import de.osthus.ambeth.ioc.extendable.IExtendableContainer;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.ContextProvider;
import de.osthus.ambeth.persistence.IContextProvider;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.persistence.parallel.IModifyingDatabase;
import de.osthus.ambeth.persistence.parallel.ModifyingDatabase;
import de.osthus.ambeth.proxy.IProxyFactory;
import de.osthus.ambeth.util.AlreadyLinkedCache;
import de.osthus.ambeth.util.AlreadyLoadedCache;
import de.osthus.ambeth.util.IAlreadyLinkedCache;
import de.osthus.ambeth.util.IAlreadyLoadedCache;
import de.osthus.ambeth.util.ParamChecker;

public class JdbcDatabaseFactory implements IDatabaseFactory, IInitializingBean, IDatabaseMapperExtendable
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IServiceContext serviceContext;

	protected Class<?>[] additionalModules;

	protected IExtendableContainer<IDatabaseMapper> databaseMapperEC;

	protected IConnectionFactory connectionFactory;

	protected IConnectionHolder connectionHolder;

	protected IDatabaseProvider databaseProvider;

	protected IProxyFactory proxyFactory;

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseSchemaCacheActive, defaultValue = "false")
	protected boolean schemaCacheActive;

	// TODO JH 2012-07-10 temporary solution
	protected volatile Map<String, Object> cachedSchemaInfos = null;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(this.connectionFactory, "ConnectionFactory");
		ParamChecker.assertNotNull(this.connectionHolder, "ConnectionHolder");
		ParamChecker.assertNotNull(this.databaseProvider, "DatabaseProvider");
		ParamChecker.assertNotNull(this.serviceContext, "ServiceContext");
		ParamChecker.assertNotNull(this.proxyFactory, "ProxyFactory");

		this.databaseMapperEC = new ExtendableContainer<IDatabaseMapper>(IDatabaseMapper.class, "databaseMapper");
	}

	public void setAdditionalModules(Class<?>[] additionalModules)
	{
		this.additionalModules = additionalModules;
	}

	public void setConnectionFactory(IConnectionFactory connectionFactory)
	{
		this.connectionFactory = connectionFactory;
	}

	public void setConnectionHolder(IConnectionHolder connectionHolder)
	{
		this.connectionHolder = connectionHolder;
	}

	public void setDatabaseProvider(IDatabaseProvider databaseProvider)
	{
		this.databaseProvider = databaseProvider;
	}

	public void setProxyFactory(IProxyFactory proxyFactory)
	{
		this.proxyFactory = proxyFactory;
	}

	public void setServiceProvider(IServiceContext serviceContext)
	{
		this.serviceContext = serviceContext;
	}

	@Override
	public void registerDatabaseMapper(IDatabaseMapper databaseMapper)
	{
		this.databaseMapperEC.register(databaseMapper);
	}

	@Override
	public void unregisterDatabaseMapper(IDatabaseMapper databaseMapper)
	{
		this.databaseMapperEC.unregister(databaseMapper);
	}

	@Override
	public void activate(IDatabase database)
	{
		Connection connection = database.getAutowiredBeanInContext(Connection.class);

		this.connectionFactory.create(connection);
	}

	@Override
	public void passivate(IDatabase database)
	{
		Connection connection = database.getAutowiredBeanInContext(Connection.class);
		try
		{
			connection.close();
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public IDatabase createDatabaseInstance(final IDatabasePool pool)
	{
		Connection conn = null;
		boolean success = false;
		try
		{
			conn = this.connectionFactory.create();
			conn.setAutoCommit(false);

			final Connection fConn = conn;

			IServiceContext childService;
			IConnectionHolder connectionHolder = this.connectionHolder;
			Connection oldConnection = connectionHolder.getConnection();
			if (oldConnection != null)
			{
				connectionHolder.setConnection(null);
			}
			try
			{
				connectionHolder.setConnection(conn);
				childService = this.serviceContext.createService("jdbc-session", new RegisterPhaseDelegate()
				{
					@Override
					public void invoke(IBeanContextFactory confSP)
					{
						confSP.registerExternalBean(pool).autowireable(IDatabasePool.class);
						confSP.registerExternalBean(fConn).autowireable(Connection.class);
						confSP.registerAutowireableBean(IModifyingDatabase.class, ModifyingDatabase.class);
						confSP.registerAutowireableBean(IAlreadyLoadedCache.class, AlreadyLoadedCache.class);
						confSP.registerAutowireableBean(IAlreadyLinkedCache.class, AlreadyLinkedCache.class);
						confSP.registerAutowireableBean(IContextProvider.class, ContextProvider.class);
						confSP.registerAnonymousBean(ConnectionShutdownBean.class);
						confSP.registerExternalBean(JdbcDatabaseFactory.this.databaseProvider).autowireable(IDatabaseProvider.class);

						confSP.registerBean("database", JDBCDatabaseWrapper.class).propertyValue("DefaultVersionFieldName", "Version")
								.propertyValue("DefaultCreatedByFieldName", "Created_By").propertyValue("DefaultCreatedOnFieldName", "Created_On")
								.propertyValue("DefaultUpdatedByFieldName", "Updated_By").propertyValue("DefaultUpdatedOnFieldName", "Updated_On")
								.propertyValue("CachedSchemaInfos", JdbcDatabaseFactory.this.cachedSchemaInfos)
								.autowireable(IDatabase.class, IDatabaseMappedListenerExtendable.class);

						if (JdbcDatabaseFactory.this.additionalModules != null)
						{
							for (int a = JdbcDatabaseFactory.this.additionalModules.length; a-- > 0;)
							{
								confSP.registerAnonymousBean(JdbcDatabaseFactory.this.additionalModules[a]);
							}
						}
					}
				});
				success = true;
			}
			finally
			{
				if (oldConnection != null)
				{
					connectionHolder.setConnection(null);
				}
				connectionHolder.setConnection(oldConnection);
			}
			// Re-bind the LCI to the child context. This is to allow bean injection to subsequently created LogStatements from the child context
			if (conn.isWrapperFor(IBeanContextAware.class))
			{
				conn.unwrap(IBeanContextAware.class).setBeanContext(childService);
			}
			IDatabase database = childService.getService(IDatabase.class);

			if (this.schemaCacheActive && this.cachedSchemaInfos == null && database instanceof JDBCDatabaseWrapper)
			{
				this.cachedSchemaInfos = ((JDBCDatabaseWrapper) database).getCachedSchemaInfos();
			}

			// IDatabaseMapper[] databaseMappers =
			// databaseMapperEC.getListeners();
			//
			// for (IDatabaseMapper databaseMapper : databaseMappers)
			// {
			// databaseMapper.mapFields(database.getDatabase());
			// }
			success = true;
			return database;
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			if (!success && conn != null)
			{
				JdbcUtil.close(conn);
			}
		}
	}
}
