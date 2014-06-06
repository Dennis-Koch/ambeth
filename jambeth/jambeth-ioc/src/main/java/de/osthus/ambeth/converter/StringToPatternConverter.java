package de.osthus.ambeth.converter;

import java.util.regex.Pattern;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.IDedicatedConverter;

public class StringToPatternConverter implements IDedicatedConverter
{
	private static final Pattern splitPattern = Pattern.compile(";");

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

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
