package com.koch.ambeth.persistence.jdbc.connection;

import java.sql.Connection;
import java.sql.SQLException;

import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.proxy.ICgLibUtil;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.IConnectionDialect;
import com.koch.ambeth.persistence.jdbc.IConnectionFactory;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.persistence.jdbc.connection.IConnectionKeyHandle;
import com.koch.ambeth.persistence.jdbc.event.ConnectionCreatedEvent;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.proxy.IProxyFactory;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;

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
