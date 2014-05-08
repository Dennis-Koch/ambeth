package de.osthus.ambeth.persistence.jdbc;

import java.sql.Connection;

public interface IConnectionHolder
{
	void setConnection(Connection conn);

	Connection getConnection();
}