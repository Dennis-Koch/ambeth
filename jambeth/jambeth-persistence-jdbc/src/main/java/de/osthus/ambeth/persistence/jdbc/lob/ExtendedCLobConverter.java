package de.osthus.ambeth.persistence.jdbc.lob;

import java.sql.Clob;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.IDedicatedConverter;

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
