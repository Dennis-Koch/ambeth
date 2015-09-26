package de.osthus.ambeth.converter;

import java.nio.charset.Charset;

import de.osthus.ambeth.util.IDedicatedConverter;

public class StringToCharsetConverter implements IDedicatedConverter
{
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
