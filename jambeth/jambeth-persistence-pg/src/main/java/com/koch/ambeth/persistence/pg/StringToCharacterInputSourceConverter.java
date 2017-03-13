package com.koch.ambeth.persistence.pg;

import java.sql.Connection;

import org.postgresql.PGConnection;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.IDedicatedConverter;

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
