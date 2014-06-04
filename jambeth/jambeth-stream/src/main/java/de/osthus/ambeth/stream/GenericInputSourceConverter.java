package de.osthus.ambeth.stream;

import de.osthus.ambeth.util.IDedicatedConverter;

public class GenericInputSourceConverter implements IDedicatedConverter
{
	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		if (expectedType.isAssignableFrom(IInputStream.class))
		{
			if (IInputSource.class.isAssignableFrom(sourceType))
			{
				return ((IInputSource) value).deriveInputStream();
			}
		}
		return null;
	}
}