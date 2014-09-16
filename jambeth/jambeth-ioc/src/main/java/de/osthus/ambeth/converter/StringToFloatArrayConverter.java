package de.osthus.ambeth.converter;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.IDedicatedConverter;

public class StringToFloatArrayConverter implements IDedicatedConverter
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		if (float[].class.equals(expectedType))
		{
			String[] split = StringToPatternConverter.splitPattern.split((String) value);
			float[] result = new float[split.length];
			for (int a = split.length; a-- > 0;)
			{
				result[a] = Float.parseFloat(split[a]);
			}
			return result;
		}
		StringBuilder sb = new StringBuilder();
		float[] array = (float[]) value;
		for (float item : array)
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