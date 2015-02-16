package de.osthus.ambeth.oracle;

import java.sql.Array;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashSet;

import oracle.jdbc.OracleConnection;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.persistence.IConnectionDialect;
import de.osthus.ambeth.persistence.jdbc.IConnectionExtension;

public class OracleConnectionExtension implements IConnectionExtension
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConnectionDialect connectionDialect;

	@Autowired
	protected Connection connection;

	protected final HashSet<Class<?>> numbersToConvert = new HashSet<Class<?>>(Arrays.<Class<?>> asList(byte.class, Byte.class, Short.class, Integer.class,
			Long.class));

	@Override
	public Array createJDBCArray(Class<?> expectedComponentType, Object javaArray)
	{
		if (expectedComponentType == null)
		{
			expectedComponentType = javaArray.getClass().getComponentType();
		}
		if (Object.class.equals(expectedComponentType))
		{
			Object firstItem = null;
			if (java.lang.reflect.Array.getLength(javaArray) > 0)
			{
				firstItem = java.lang.reflect.Array.get(javaArray, 0);
			}
			if (firstItem != null)
			{
				expectedComponentType = firstItem.getClass();
			}
		}

		if (numbersToConvert.contains(expectedComponentType))
		{
			long[] longArray = new long[java.lang.reflect.Array.getLength(javaArray)];
			for (int i = longArray.length; i-- > 0;)
			{
				longArray[i] = ((Number) java.lang.reflect.Array.get(javaArray, i)).longValue();
			}
			javaArray = longArray;
		}
		else if (expectedComponentType == char.class)
		{
			Character[] characterArray = new Character[java.lang.reflect.Array.getLength(javaArray)];
			for (int i = characterArray.length; i-- > 0;)
			{
				characterArray[i] = (Character) java.lang.reflect.Array.get(javaArray, i);
			}
			javaArray = characterArray;
		}

		String arrayTypeName = connectionDialect.getFieldTypeNameByComponentType(expectedComponentType);
		if (arrayTypeName == null)
		{
			throw new IllegalArgumentException("Can not handle arrays of type " + expectedComponentType.getName());
		}
		try
		{
			return connection.unwrap(OracleConnection.class).createARRAY(arrayTypeName, javaArray);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
