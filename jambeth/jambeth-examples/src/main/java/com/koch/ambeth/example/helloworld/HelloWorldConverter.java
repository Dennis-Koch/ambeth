package com.koch.ambeth.example.helloworld;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.IDedicatedConverter;

public class HelloWorldConverter implements IDedicatedConverter {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation) {
		if (sourceType.equals(HelloWorldToken.class) && expectedType.equals(String.class)) {
			return "Hello World!";
		}

		return null;
	}
}
