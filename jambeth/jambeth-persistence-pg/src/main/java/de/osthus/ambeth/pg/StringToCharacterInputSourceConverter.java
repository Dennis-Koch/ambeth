package de.osthus.ambeth.pg;

import java.sql.Connection;

import org.postgresql.PGConnection;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.IDedicatedConverter;

public class StringToCharacterInputSourceConverter implements IDedicatedConverter
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected Connection connection;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation) throws Throwable
	{
		long oid = conversionHelper.convertValueToType(Number.class, value).longValue();
		return new PostgresCharacterInputSource(oid, connection.unwrap(PGConnection.class));
	}
}
