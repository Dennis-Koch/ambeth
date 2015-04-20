package de.osthus.ambeth.persistence.jdbc.connection;

import java.sql.Connection;
import java.sql.SQLException;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.jdbc.IConnectionFactory;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.event.ConnectionCreatedEvent;
import de.osthus.ambeth.proxy.ICgLibUtil;
import de.osthus.ambeth.proxy.IProxyFactory;
import de.osthus.ambeth.util.ParamChecker;

public abstract class AbstractConnectionFactory implements IConnectionFactory, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected ICgLibUtil cgLibUtil;

	@Autowired
	protected IConnectionDialect connectionDialect;

	@Autowired(optional = true)
	protected IEventDispatcher eventDispatcher;

	@Autowired
	protected IProxyFactory proxyFactory;

	@Property(name = PersistenceJdbcConfigurationConstants.PreparedConnectionInstances, mandatory = false)
	protected ArrayList<Connection> preparedConnectionInstances;

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseSchemaName)
	protected String schemaName;

	protected String[] schemaNames;

	protected final IConnectionKeyHandle connectionKeyHandle = new DefaultConnectionKeyHandle();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(schemaName, "schemaName");

		schemaNames = connectionDialect.toDefaultCase(schemaName).split("[:;]");
	}

	@Override
	public final Connection create()
	{
		while (preparedConnectionInstances != null && preparedConnectionInstances.size() > 0)
		{
			Connection preparedConnection = preparedConnectionInstances.remove(preparedConnectionInstances.size() - 1);
			try
			{
				if (preparedConnection.isClosed())
				{
					continue;
				}
			}
			catch (SQLException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			connectionDialect.preProcessConnection(preparedConnection, schemaNames, false);

			if (eventDispatcher != null)
			{
				eventDispatcher.dispatchEvent(new ConnectionCreatedEvent(preparedConnection));
			}
			return preparedConnection;
		}
		try
		{
			Connection connection = createIntern();
			connection.setAutoCommit(false);

			MethodInterceptor logConnectionInterceptor = beanContext.registerExternalBean(new LogConnectionInterceptor(connectionKeyHandle))
					.propertyValue("Connection", connection).finish();
			Connection conn = proxyFactory.createProxy(Connection.class, cgLibUtil.getAllInterfaces(connection), logConnectionInterceptor);

			connectionDialect.preProcessConnection(conn, schemaNames, false);

			if (eventDispatcher != null)
			{
				eventDispatcher.dispatchEvent(new ConnectionCreatedEvent(conn));
			}
			return conn;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public final void create(Connection reusableConnection)
	{
		try
		{
			if (!(reusableConnection instanceof Factory))
			{
				throw new IllegalArgumentException("Connection is not reusable");
			}
			Callback callback = ((Factory) reusableConnection).getCallback(0);
			if (!(callback instanceof LogConnectionInterceptor))
			{
				throw new IllegalArgumentException("Connection is not reusable");
			}
			LogConnectionInterceptor lci = (LogConnectionInterceptor) callback;
			if (lci.getConnection() != null)
			{
				return;
			}
			Connection connection = createIntern();
			((LogConnectionInterceptor) callback).setConnection(connection);
			if (eventDispatcher != null)
			{
				eventDispatcher.dispatchEvent(new ConnectionCreatedEvent(reusableConnection));
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected abstract Connection createIntern() throws Exception;
}
