package com.koch.ambeth.ioc.converter;

import java.io.File;
import java.nio.file.Path;
import java.util.regex.Pattern;

import com.koch.ambeth.util.IDedicatedConverter;

public class FileToPathConverter implements IDedicatedConverter
{
	protected static final Pattern fileDelimiterPattern = Pattern.compile(";");

	@Override
	public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation) throws Throwable
	{
		if (Path.class.equals(expectedType))
		{
			return ((File) value).toPath();
		}
		if (File.class.equals(expectedType))
		{
			return ((Path) value).toFile();
		}
		if (File[].class.equals(expectedType))
		{
			Path[] paths = (Path[]) value;
			File[] files = new File[paths.length];
			for (int a = paths.length; a-- > 0;)
			{
				files[a] = paths[a].toFile();
			}
			return files;
		}
		if (Path[].class.equals(expectedType))
		{
			File[] files = (File[]) value;
			Path[] paths = new Path[files.length];
			for (int a = files.length; a-- > 0;)
			{
				paths[a] = files[a].toPath();
			}
			return paths;
		}
		return null;
	}
}
