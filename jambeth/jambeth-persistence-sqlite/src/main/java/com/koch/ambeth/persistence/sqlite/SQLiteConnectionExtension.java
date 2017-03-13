package com.koch.ambeth.persistence.sqlite;

import java.sql.Array;
import java.sql.Connection;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.persistence.jdbc.IConnectionExtension;

public class SQLiteConnectionExtension implements IConnectionExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected Connection connection;

	@Override
	public Array createJDBCArray(Class<?> expectedComponentType, Object javaArray)
	{
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}
}
