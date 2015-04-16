package de.osthus.ambeth.persistence.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.ITransactionState;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.connection.IDatabaseConnectionUrlProvider;

public abstract class AbstractConnectionDialect implements IConnectionDialect, IInitializingBean, IDisposableBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IDatabaseConnectionUrlProvider databaseConnectionUrlProvider;

	@Autowired
	protected IProperties props;

	@Autowired(optional = true)
	protected ITransactionState transactionState;

	@Property(name = PersistenceConfigurationConstants.ExternalTransactionManager, defaultValue = "false")
	protected boolean externalTransactionManager;

	protected Driver driverRegisteredExplicitly;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		registerDriverIfNeeded();
	}

	protected abstract Class<?> getDriverType();

	protected void registerDriverIfNeeded()
	{
		Class<?> databaseDriver = getDriverType();
		if (databaseDriver == null || !Driver.class.isAssignableFrom(databaseDriver))
		{
			return;
		}
		try
		{
			try
			{
				DriverManager.getDriver(databaseConnectionUrlProvider.getConnectionUrl());
			}
			catch (SQLException e)
			{
				if (!"08001".equals(e.getSQLState()))
				{
					throw e;
				}
				driverRegisteredExplicitly = (Driver) databaseDriver.newInstance();
				DriverManager.registerDriver(driverRegisteredExplicitly);
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public void destroy() throws Throwable
	{
		if (driverRegisteredExplicitly != null)
		{
			DriverManager.deregisterDriver(driverRegisteredExplicitly);
			driverRegisteredExplicitly = null;
		}
	}

	@Override
	public int getMaxInClauseBatchThreshold()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean useVersionOnOptimisticUpdate()
	{
		return false;
	}

	@Override
	public void commit(Connection connection) throws SQLException
	{
		Boolean active = transactionState != null ? transactionState.isExternalTransactionManagerActive() : null;
		if (active == null)
		{
			active = Boolean.valueOf(externalTransactionManager);
		}
		if (active.booleanValue())
		{
			// No Action!
			// Transactions are externally managed.
		}
		else
		{
			connection.commit();
		}
	}

	@Override
	public void rollback(Connection connection) throws SQLException
	{
		Boolean active = transactionState != null ? transactionState.isExternalTransactionManagerActive() : null;
		if (active == null)
		{
			active = Boolean.valueOf(externalTransactionManager);
		}
		if (active.booleanValue())
		{
			// No Action!
			// Transactions are externally managed.
		}
		else
		{
			connection.rollback();
		}
	}

	protected void printResultSet(ResultSet rs) throws SQLException
	{
		ResultSetMetaData metaData = rs.getMetaData();
		int columnCount = metaData.getColumnCount();
		for (int a = 0, size = columnCount; a < size; a++)
		{
			System.out.print(metaData.getColumnLabel(a + 1));
			System.out.print("\t\t");
		}
		System.out.println("\t\t");
		while (rs.next())
		{
			for (int a = 0, size = columnCount; a < size; a++)
			{
				System.out.print(rs.getObject(a + 1));
				System.out.print("\t\t");
			}
			System.out.println();
		}
	}
}
