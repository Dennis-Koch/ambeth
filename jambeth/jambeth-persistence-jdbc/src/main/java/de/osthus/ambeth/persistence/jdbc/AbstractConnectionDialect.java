package de.osthus.ambeth.persistence.jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.ITransactionState;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.config.PersistenceConfigurationConstants;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;

public abstract class AbstractConnectionDialect implements IConnectionDialect, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired(optional = true)
	protected ITransactionState transactionState;

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseConnection)
	protected String connectionString;

	@Property(name = PersistenceConfigurationConstants.ExternalTransactionManager, defaultValue = "false")
	protected boolean externalTransactionManager;

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
				DriverManager.getDriver(connectionString);
			}
			catch (SQLException e)
			{
				if (!"08001".equals(e.getSQLState()))
				{
					throw e;
				}
				Driver driver = (Driver) databaseDriver.newInstance();
				DriverManager.registerDriver(driver);
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
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

	protected String escapeName(String schemaName, String tableName)
	{
		if (schemaName == null)
		{
			return "\"" + tableName + "\"";
		}
		return "\"" + schemaName + "\".\"" + tableName + "\"";
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
