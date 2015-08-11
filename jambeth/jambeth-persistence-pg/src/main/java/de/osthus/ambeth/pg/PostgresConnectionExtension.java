package de.osthus.ambeth.pg;

import java.sql.Array;
import java.sql.Connection;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.jdbc.IConnectionExtension;

public class PostgresConnectionExtension implements IConnectionExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected Connection connection;

	@Override
	public Array createJDBCArray(Class<?> expectedComponentType, Object javaArray)
	{
		if (expectedComponentType == null)
		{
			expectedComponentType = javaArray.getClass().getComponentType();
		}
		String[] strings = PostgresDialect.typeToArrayTypeNameMap.get(expectedComponentType);
		try
		{
			return connection.createArrayOf(strings[1], (Object[]) javaArray);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
