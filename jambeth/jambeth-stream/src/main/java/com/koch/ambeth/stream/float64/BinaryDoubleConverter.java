package com.koch.ambeth.stream.float64;

import com.koch.ambeth.stream.binary.IBinaryInputStream;
import com.koch.ambeth.util.IDedicatedConverter;

public class BinaryDoubleConverter implements IDedicatedConverter
{
	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		if (expectedType.isAssignableFrom(IBinaryInputStream.class))
		{
			if (IDoubleInputStream.class.isAssignableFrom(sourceType))
			{
				return new DoubleToBinaryInputStream((IDoubleInputStream) value);
			}
			else if (IDoubleInputSource.class.isAssignableFrom(sourceType))
			{
				return new DoubleToBinaryInputStream(((IDoubleInputSource) value).deriveDoubleInputStream());
			}
			else if (double[].class.equals(sourceType))
			{
				return new DoubleToBinaryInputStream(new DoubleInMemoryInputStream((double[]) value));
			}
		}
		else if (expectedType.isAssignableFrom(IDoubleInputStream.class))
		{
			if (IDoubleInputSource.class.isAssignableFrom(sourceType))
			{
				return ((IDoubleInputSource) value).deriveDoubleInputStream();
			}
			else if (double[].class.equals(sourceType))
			{
				return new DoubleInMemoryInputStream((double[]) value);
			}
		}
		return null;
	}
}
