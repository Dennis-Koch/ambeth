package de.osthus.ambeth.stream.int32;

import de.osthus.ambeth.stream.binary.IBinaryInputStream;
import de.osthus.ambeth.util.IDedicatedConverter;

public class BinaryIntConverter implements IDedicatedConverter
{
	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		if (expectedType.isAssignableFrom(IBinaryInputStream.class))
		{
			if (IIntInputStream.class.isAssignableFrom(sourceType))
			{
				return new IntToBinaryInputStream((IIntInputStream) value);
			}
			else if (IIntInputSource.class.isAssignableFrom(sourceType))
			{
				return new IntToBinaryInputStream(((IIntInputSource) value).deriveIntInputStream());
			}
			else if (int[].class.equals(sourceType))
			{
				return new IntToBinaryInputStream(new IntInMemoryInputStream((int[]) value));
			}
		}
		else if (expectedType.isAssignableFrom(IIntInputStream.class))
		{
			if (IIntInputSource.class.isAssignableFrom(sourceType))
			{
				return ((IIntInputSource) value).deriveIntInputStream();
			}
			else if (int[].class.equals(sourceType))
			{
				return new IntInMemoryInputStream((int[]) value);
			}
		}
		return null;
	}
}
