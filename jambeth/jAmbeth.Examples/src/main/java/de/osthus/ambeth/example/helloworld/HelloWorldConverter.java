package de.osthus.ambeth.example.helloworld;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.IDedicatedConverter;

public class HelloWorldConverter implements IDedicatedConverter
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		if (sourceType.equals(HelloWorldToken.class) && expectedType.equals(String.class))
		{
			return "Hello World!";
		}

		return null;
	}
}
