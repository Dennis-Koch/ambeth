package de.osthus.ambeth.converter;

import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.IDedicatedConverter;

public class StringToClassArrayConverter implements IDedicatedConverter
{
	@Autowired
	protected IConversionHelper conversionHelper;

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation)
	{
		if (Class[].class.equals(expectedType))
		{
			String[] split = StringToPatternConverter.splitPattern.split((String) value);
			Class<?>[] result = new Class<?>[split.length];
			for (int a = split.length; a-- > 0;)
			{
				result[a] = conversionHelper.convertValueToType(Class.class, split[a]);
			}
			return result;
		}
		StringBuilder sb = new StringBuilder();
		Class<?>[] array = (Class<?>[]) value;
		for (Class<?> item : array)
		{
			if (sb.length() > 0)
			{
				sb.append(StringToPatternConverter.splitPattern.pattern());
			}
			sb.append(item.getName());
		}
		return sb.toString();
	}
}
