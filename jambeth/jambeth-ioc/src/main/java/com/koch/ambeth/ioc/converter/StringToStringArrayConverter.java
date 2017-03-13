package com.koch.ambeth.ioc.converter;

import com.koch.ambeth.util.IDedicatedConverter;

public class StringToStringArrayConverter implements IDedicatedConverter
{
	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		if (String[].class.equals(expectedType))
		{
			return StringToPatternConverter.splitPattern.split((String) value);
		}
		StringBuilder sb = new StringBuilder();
		String[] array = (String[]) value;
		for (String item : array)
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
