package de.osthus.ambeth.persistence.jdbc;

import java.sql.Connection;

import de.osthus.ambeth.ioc.IDisposableBean;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.ParamChecker;

public class ConnectionShutdownBean implements IInitializingBean, IDisposableBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected Connection connection;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(connection, "connection");
	}

	@Override
	public void destroy() throws Throwable
	{
		if (connection != null)
		{
			JdbcUtil.close(connection);
			connection = null;
		}
	}

	public void setConnection(Connection connection)
	{
		this.connection = connection;
	}
}
