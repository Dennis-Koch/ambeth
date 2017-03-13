package com.koch.ambeth.persistence.jdbc.connection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;

import javax.persistence.PersistenceException;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.jdbc.JdbcUtil;
import com.koch.ambeth.persistence.jdbc.config.PersistenceJdbcConfigurationConstants;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class ConnectionFactory extends AbstractConnectionFactory
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IDatabaseConnectionUrlProvider databaseConnectionUrlProvider;

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseUser)
	protected String userName;

	@Property(name = PersistenceJdbcConfigurationConstants.DatabasePass)
	protected String userPass;

	@Property(name = PersistenceJdbcConfigurationConstants.DatabaseRetryConnect, defaultValue = "1")
	protected int tryCount;

	@Override
	protected Connection createIntern() throws Exception
	{
		String connectionUrl = databaseConnectionUrlProvider.getConnectionUrl();
		try
		{
			if (log.isInfoEnabled())
			{
				log.info("Creating jdbc connection to '" + connectionUrl + "' with user='" + userName + "'");
			}
			boolean success = false;
			Connection connection = DriverManager.getConnection(connectionUrl, userName, userPass);
			try
			{
				if (log.isDebugEnabled())
				{
					log.debug("[" + System.identityHashCode(connection) + "] created connection");
				}
				DatabaseMetaData dbmd = connection.getMetaData();
				if (dbmd.supportsTransactionIsolationLevel(Connection.TRANSACTION_REPEATABLE_READ))
				{
					connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
				}
				else if (dbmd.supportsTransactionIsolationLevel(Connection.TRANSACTION_READ_COMMITTED))
				{
					connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
				}
				else if (dbmd.supportsTransactionIsolationLevel(Connection.TRANSACTION_SERIALIZABLE))
				{
					connection.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
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
			throw RuntimeExceptionUtil.mask(e, "Error occured while connecting to '" + connectionUrl + "' with user='" + userName + "'");
		}
	}
}
