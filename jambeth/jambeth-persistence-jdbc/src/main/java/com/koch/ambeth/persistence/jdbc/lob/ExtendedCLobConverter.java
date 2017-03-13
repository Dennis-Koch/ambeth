package com.koch.ambeth.persistence.jdbc.lob;

import java.sql.Clob;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.IDedicatedConverter;

public class ExtendedCLobConverter implements IDedicatedConverter
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		if (Clob.class.isAssignableFrom(expectedType))
		{
			String stringValue = conversionHelper.convertValueToType(String.class, value);
			return conversionHelper.convertValueToType(Clob.class, stringValue);
		}
		String stringValue = conversionHelper.convertValueToType(String.class, value);
		return conversionHelper.convertValueToType(expectedType, stringValue);
	}
}
