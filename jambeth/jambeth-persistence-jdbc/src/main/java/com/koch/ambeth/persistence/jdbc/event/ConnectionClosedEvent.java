package com.koch.ambeth.persistence.jdbc.event;

import java.sql.Connection;

public class ConnectionClosedEvent
{
	protected final Connection connection;

	public ConnectionClosedEvent(Connection connection)
	{
		this.connection = connection;
	}

	public Connection getConnection()
	{
		return connection;
	}
}
