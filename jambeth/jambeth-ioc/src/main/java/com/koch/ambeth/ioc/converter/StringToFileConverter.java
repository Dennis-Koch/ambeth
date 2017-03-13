package com.koch.ambeth.ioc.converter;

import java.io.File;
import java.util.regex.Pattern;

import com.koch.ambeth.util.IDedicatedConverter;

public class StringToFileConverter implements IDedicatedConverter
{
	protected static final Pattern fileDelimiterPattern = Pattern.compile(";");

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation) throws Throwable
	{
		if (File[].class.equals(expectedType))
		{
			String[] split = fileDelimiterPattern.split((String) value);
			File[] files = new File[split.length];
			for (int a = split.length; a-- > 0;)
			{
				files[a] = new File(split[a]);
			}
			return files;
		}
		return new File((String) value);
	}
}
