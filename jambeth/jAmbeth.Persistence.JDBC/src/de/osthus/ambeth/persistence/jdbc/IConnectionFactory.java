package de.osthus.ambeth.persistence.jdbc;

import java.sql.Connection;

public interface IConnectionFactory
{
	Connection create();

	void create(Connection reusableConnection);
}
