package com.koch.ambeth.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JREVersionProvider
{
	private static final Pattern versionExtractPattern = Pattern.compile("(\\d+\\.\\d+).*");

	private static final double version;

	static
	{
		String versionProperty = System.getProperty("java.version");
		Matcher versionMatcher = versionExtractPattern.matcher(versionProperty);
		if (versionMatcher.find())
		{
			version = Double.parseDouble(versionMatcher.group(1));
		}
		else
		{
			version = 1.6;
		}
	}

	public static double getVersion()
	{
		return version;
	}
}
