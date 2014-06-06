package de.osthus.ambeth.converter;

import java.util.regex.Pattern;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.IDedicatedConverter;

public class StringToPatternConverter implements IDedicatedConverter
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		if (Pattern.class.equals(expectedType))
		{
			return Pattern.compile((String) value);
		}
		else
		{
			return ((Pattern) value).pattern();
		}
	}
}
