package de.osthus.ambeth.persistence.jdbc.connection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.PersistenceException;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.jdbc.JdbcUtil;
import de.osthus.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import de.osthus.ambeth.util.ParamChecker;

public class ConnectionFactory extends AbstractConnectionFactory
{
	@LogInstance
	private ILogger log;

	protected String databaseConnection;

	protected String userName;

	protected String userPass;

	protected Class<?> databaseDriver;

	protected int tryCount;

	protected volatile boolean isDriverRegistered;

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		ParamChecker.assertNotNull(databaseConnection, "databaseConnection");
		ParamChecker.assertNotNull(userName, "userName");
		ParamChecker.assertNotNull(userPass, "userPass");
	}

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseConnection)
	public void setDatabaseConnection(String databaseConnection)
	{
		this.databaseConnection = databaseConnection;
	}

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseUser)
	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	@Property(name = PersistenceJdbcConfigurationConstants.DatabasePass)
	public void setUserPass(String userPass)
	{
		this.userPass = userPass;
	}

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseDriver, mandatory = false)
	public void setDatabaseDriver(Class<?> databaseDriver)
	{
		this.databaseDriver = databaseDriver;
	}

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseRetryConnect, defaultValue = "1")
	public void setTryCount(int tryCount)
	{
		this.tryCount = tryCount;
	}

	protected void registerDriverIfNeeded()
	{
		Class<?> databaseDriver = this.databaseDriver;
		if (databaseDriver == null || !Driver.class.isAssignableFrom(databaseDriver))
		{
			return;
		}
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			if (isDriverRegistered)
			{
				return;
			}
			Driver driver = (Driver) databaseDriver.newInstance();
			DriverManager.registerDriver(driver);
			isDriverRegistered = true;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	protected Connection createIntern() throws Exception
	{
		try
		{
			if (log.isInfoEnabled())
			{
				log.info("Creating jdbc connection to '" + databaseConnection + "' with user='" + userName + "'");
			}
			registerDriverIfNeeded();
			boolean success = false;
			Connection connection = DriverManager.getConnection(databaseConnection, userName, userPass);
			try
			{
				if (log.isDebugEnabled())
				{
					log.debug("Done creating jdbc connection");
				}
				DatabaseMetaData dbmd = connection.getMetaData();
				// if (dbmd.supportsTransactionIsolationLevel(Connection.TRANSACTION_SERIALIZABLE))
				// {
				// connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
				// }
				if (dbmd.supportsTransactionIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ))
				{
					connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
				}
				else if (dbmd.supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED))
				{
					connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
				}
				else
				{
					throw new PersistenceException(
							"At least READ_COMMITTED it required from a JDBC database provider as a supported transaction isolation level");
				}
				success = true;
				return connection;
			}
			finally
			{
				if (!success)
				{
					JdbcUtil.close(connection);
				}
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e, "Error occured while connecting to '" + databaseConnection + "' with user='" + userName + "'");
		}
	}

	@Override
	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseSchemaName)
	public void setSchemaName(String schemaName)
	{
		this.schemaName = schemaName;
	}
}
