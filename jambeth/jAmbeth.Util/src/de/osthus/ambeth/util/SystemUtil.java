package de.osthus.ambeth.util;

public class SystemUtil
{
	protected SystemUtil()
	{
	}

	public static final String lineSeparator()
	{
		return System.getProperty("line.separator");
		// In JRE/JDK 1.7+ it may be System.lineSeparator()
	}
}
