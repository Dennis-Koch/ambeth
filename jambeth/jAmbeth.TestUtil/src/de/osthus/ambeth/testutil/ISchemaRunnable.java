package de.osthus.ambeth.testutil;

import java.sql.Connection;

public interface ISchemaRunnable
{
	void executeSchemaSql(Connection connection) throws Exception;
}
