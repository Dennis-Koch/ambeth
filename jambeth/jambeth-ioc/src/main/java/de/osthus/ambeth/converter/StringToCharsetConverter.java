package de.osthus.ambeth.converter;

import java.nio.charset.Charset;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.IDedicatedConverter;

public class StringToCharsetConverter implements IDedicatedConverter
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		if (expectedType.equals(Charset.class))
		{
			return Charset.forName((String) value);
		}
		return ((Charset) value).name();
	}
}
