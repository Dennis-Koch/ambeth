package com.koch.ambeth.ioc.converter;

import com.koch.ambeth.util.IDedicatedConverter;

public class StringToDoubleArrayConverter implements IDedicatedConverter
{
	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		if (double[].class.equals(expectedType))
		{
			String[] split = StringToPatternConverter.splitPattern.split((String) value);
			double[] result = new double[split.length];
			for (int a = split.length; a-- > 0;)
			{
				result[a] = Double.parseDouble(split[a]);
			}
			return result;
		}
		StringBuilder sb = new StringBuilder();
		double[] array = (double[]) value;
		for (double item : array)
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
