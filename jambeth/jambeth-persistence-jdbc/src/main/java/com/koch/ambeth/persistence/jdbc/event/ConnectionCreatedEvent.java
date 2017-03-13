package com.koch.ambeth.persistence.jdbc.event;

import java.sql.Connection;

public class ConnectionCreatedEvent
{
	protected final Connection connection;

	public ConnectionCreatedEvent(Connection connection)
	{
		this.connection = connection;
	}

	public Connection getConnection()
	{
		return connection;
	}
}
