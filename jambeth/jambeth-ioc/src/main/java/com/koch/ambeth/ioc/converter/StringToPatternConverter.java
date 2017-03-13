package com.koch.ambeth.ioc.converter;

import java.util.regex.Pattern;

import com.koch.ambeth.util.IDedicatedConverter;

public class StringToPatternConverter implements IDedicatedConverter
{
	public static final Pattern splitPattern = Pattern.compile(";");

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		if (Pattern.class.equals(expectedType))
		{
			return Pattern.compile((String) value);
		}
		else if (Pattern[].class.equals(expectedType))
		{
			String[] split = splitPattern.split((String) value);
			Pattern[] patterns = new Pattern[split.length];
			for (int a = split.length; a-- > 0;)
			{
				patterns[a] = Pattern.compile(split[a]);
			}
			return patterns;
		}
		else
		{
			if (Pattern.class.equals(sourceType))
			{
				return ((Pattern) value).pattern();
			}
			else
			{
				StringBuilder sb = new StringBuilder();
				Pattern[] patterns = (Pattern[]) value;
				for (Pattern pattern : patterns)
				{
					if (sb.length() > 0)
					{
						sb.append(splitPattern.pattern());
					}
					sb.append(pattern.pattern());
				}
				return sb.toString();
			}
		}
	}
}
