package com.koch.ambeth.persistence.jdbc;

import java.sql.Connection;

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.ParamChecker;

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
