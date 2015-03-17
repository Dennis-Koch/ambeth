package de.osthus.ambeth.util;

public interface IDedicatedConverter
{
	/**
	 * Converts a defined set of types.
	 * 
	 * @param expectedType
	 *            Type to convert to
	 * @param sourceType
	 *            Type to convert from
	 * @param value
	 *            Value of class sourceType
	 * @param additionalInformation
	 *            Optional information if neede for this conversion
	 * @return Value converted to expectedType
	 */
	Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation) throws Throwable;
}