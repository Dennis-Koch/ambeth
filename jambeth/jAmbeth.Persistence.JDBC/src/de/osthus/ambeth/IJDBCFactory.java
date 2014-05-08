package de.osthus.ambeth;

import java.sql.Connection;

public interface IJDBCFactory
{

	Connection createDatabaseInstance();

}
