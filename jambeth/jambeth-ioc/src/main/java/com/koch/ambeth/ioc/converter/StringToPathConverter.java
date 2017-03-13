package com.koch.ambeth.ioc.converter;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.koch.ambeth.util.IDedicatedConverter;

public class StringToPathConverter implements IDedicatedConverter
{
	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation) throws Throwable
	{
		if (Path[].class.equals(expectedType))
		{
			String[] split = StringToFileConverter.fileDelimiterPattern.split((String) value);
			Path[] files = new Path[split.length];
			for (int a = split.length; a-- > 0;)
			{
				files[a] = Paths.get(split[a]);
			}
			return files;
		}
		return Paths.get((String) value);
	}
}
