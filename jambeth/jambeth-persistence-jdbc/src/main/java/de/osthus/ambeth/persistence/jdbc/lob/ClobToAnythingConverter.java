package de.osthus.ambeth.persistence.jdbc.lob;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.IDedicatedConverter;

public class ClobToAnythingConverter implements IDedicatedConverter
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation) throws Throwable
	{
		String stringValue = conversionHelper.convertValueToType(String.class, value);
		return conversionHelper.convertValueToType(expectedType, stringValue);
	}
}
