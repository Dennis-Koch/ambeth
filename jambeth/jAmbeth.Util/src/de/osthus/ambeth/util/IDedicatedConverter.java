package de.osthus.ambeth.util;

public interface IDedicatedConverter
{
	Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation);
}