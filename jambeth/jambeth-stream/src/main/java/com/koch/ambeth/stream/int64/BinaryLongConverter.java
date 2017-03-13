package com.koch.ambeth.stream.int64;

import com.koch.ambeth.stream.binary.IBinaryInputStream;
import com.koch.ambeth.util.IDedicatedConverter;

public class BinaryLongConverter implements IDedicatedConverter
{
	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		if (expectedType.isAssignableFrom(IBinaryInputStream.class))
		{
			if (ILongInputStream.class.isAssignableFrom(sourceType))
			{
				return new LongToBinaryInputStream((ILongInputStream) value);
			}
			else if (ILongInputSource.class.isAssignableFrom(sourceType))
			{
				return new LongToBinaryInputStream(((ILongInputSource) value).deriveLongInputStream());
			}
			else if (long[].class.equals(sourceType))
			{
				return new LongToBinaryInputStream(new LongInMemoryInputStream((long[]) value));
			}
		}
		else if (expectedType.isAssignableFrom(ILongInputStream.class))
		{
			if (ILongInputSource.class.isAssignableFrom(sourceType))
			{
				return ((ILongInputSource) value).deriveLongInputStream();
			}
			else if (long[].class.equals(sourceType))
			{
				return new LongInMemoryInputStream((long[]) value);
			}
		}
		return null;
	}
}
