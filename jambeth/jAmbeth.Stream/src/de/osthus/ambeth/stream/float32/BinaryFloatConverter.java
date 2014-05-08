package de.osthus.ambeth.stream.float32;

import de.osthus.ambeth.stream.binary.IBinaryInputStream;
import de.osthus.ambeth.util.IDedicatedConverter;

public class BinaryFloatConverter implements IDedicatedConverter
{
	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		if (expectedType.isAssignableFrom(IBinaryInputStream.class))
		{
			if (IFloatInputStream.class.isAssignableFrom(sourceType))
			{
				return new FloatToBinaryInputStream((IFloatInputStream) value);
			}
			else if (IFloatInputSource.class.isAssignableFrom(sourceType))
			{
				return new FloatToBinaryInputStream(((IFloatInputSource) value).deriveFloatInputStream());
			}
			else if (float[].class.equals(sourceType))
			{
				return new FloatToBinaryInputStream(new FloatInMemoryInputStream((float[]) value));
			}
		}
		else if (expectedType.isAssignableFrom(IFloatInputStream.class))
		{
			if (IFloatInputSource.class.isAssignableFrom(sourceType))
			{
				return ((IFloatInputSource) value).deriveFloatInputStream();
			}
			else if (float[].class.equals(sourceType))
			{
				return new FloatInMemoryInputStream((float[]) value);
			}
		}
		return null;
	}
}
