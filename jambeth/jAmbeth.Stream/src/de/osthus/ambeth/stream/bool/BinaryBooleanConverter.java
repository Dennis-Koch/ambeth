package de.osthus.ambeth.stream.bool;

import de.osthus.ambeth.stream.binary.IBinaryInputStream;
import de.osthus.ambeth.util.IDedicatedConverter;

public class BinaryBooleanConverter implements IDedicatedConverter
{
	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		if (expectedType.isAssignableFrom(IBinaryInputStream.class))
		{
			if (IBooleanInputStream.class.isAssignableFrom(sourceType))
			{
				return new BooleanToBinaryInputStream((IBooleanInputStream) value);
			}
			else if (IBooleanInputSource.class.isAssignableFrom(sourceType))
			{
				return new BooleanToBinaryInputStream(((IBooleanInputSource) value).deriveBooleanInputStream());
			}
			else if (boolean[].class.equals(sourceType))
			{
				return new BooleanToBinaryInputStream(new BooleanInMemoryInputStream((boolean[]) value));
			}
		}
		else if (expectedType.isAssignableFrom(IBooleanInputStream.class))
		{
			if (IBooleanInputStream.class.isAssignableFrom(sourceType))
			{
				return ((IBooleanInputSource) value).deriveBooleanInputStream();
			}
			else if (boolean[].class.equals(sourceType))
			{
				return new BooleanInMemoryInputStream((boolean[]) value);
			}
		}
		return null;
	}
}
