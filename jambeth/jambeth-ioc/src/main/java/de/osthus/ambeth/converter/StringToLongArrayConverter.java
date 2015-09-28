package de.osthus.ambeth.converter;

import de.osthus.ambeth.util.IDedicatedConverter;

public class StringToLongArrayConverter implements IDedicatedConverter
{
	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		if (long[].class.equals(expectedType))
		{
			String[] split = StringToPatternConverter.splitPattern.split((String) value);
			long[] result = new long[split.length];
			for (int a = split.length; a-- > 0;)
			{
				result[a] = Long.parseLong(split[a]);
			}
			return result;
		}
		StringBuilder sb = new StringBuilder();
		long[] array = (long[]) value;
		for (long item : array)
		{
			if (sb.length() > 0)
			{
				sb.append(StringToPatternConverter.splitPattern.pattern());
			}
			sb.append(item);
		}
		return sb.toString();
	}
}
