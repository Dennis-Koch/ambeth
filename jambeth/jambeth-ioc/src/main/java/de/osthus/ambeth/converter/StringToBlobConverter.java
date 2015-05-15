package de.osthus.ambeth.converter;

import java.sql.Blob;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.IDedicatedConverter;

public class StringToBlobConverter implements IDedicatedConverter
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> targetClass, Object value, Object additionalInformation)
	{
		if (expectedType.equals(Blob.class))
		{
			byte[] bytes = conversionHelper.convertValueToType(byte[].class, value);
			return conversionHelper.convertValueToType(Blob.class, bytes);
		}
		byte[] bytes = conversionHelper.convertValueToType(byte[].class, value);
		return conversionHelper.convertValueToType(String.class, bytes);
	}
}
