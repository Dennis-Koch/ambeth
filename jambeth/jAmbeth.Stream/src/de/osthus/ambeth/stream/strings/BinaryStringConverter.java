package de.osthus.ambeth.stream.strings;

import de.osthus.ambeth.stream.binary.IBinaryInputStream;
import de.osthus.ambeth.util.IDedicatedConverter;

public class BinaryStringConverter implements IDedicatedConverter
{
	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		if (expectedType.isAssignableFrom(IBinaryInputStream.class))
		{
			if (IStringInputStream.class.isAssignableFrom(sourceType))
			{
				return new StringToBinaryInputStream((IStringInputStream) value);
			}
			else if (IStringInputSource.class.isAssignableFrom(sourceType))
			{
				return new StringToBinaryInputStream(((IStringInputSource) value).deriveStringInputStream());
			}
			else if (String[].class.equals(sourceType))
			{
				return new StringToBinaryInputStream(new StringInMemoryInputStream((String[]) value));
			}
		}
		else if (expectedType.isAssignableFrom(IStringInputStream.class))
		{
			if (IStringInputSource.class.isAssignableFrom(sourceType))
			{
				return ((IStringInputSource) value).deriveStringInputStream();
			}
			else if (String[].class.equals(sourceType))
			{
				return new StringInMemoryInputStream((String[]) value);
			}
		}
		return null;
	}
}
