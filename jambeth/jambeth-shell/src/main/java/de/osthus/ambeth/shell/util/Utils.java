package de.osthus.ambeth.shell.util;

import java.io.IOException;
import java.io.InputStream;

public class Utils
{
	public static String stringPadEnd(String string, int minLength, char padChar)
	{
		if (string.length() >= minLength)
		{
			return string;
		}
		StringBuilder sb = new StringBuilder(minLength);
		sb.append(string);
		for (int i = string.length(); i < minLength; i++)
		{
			sb.append(padChar);
		}
		return sb.toString();
	}

	public static String readInputStream(InputStream is) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];
		int amountRead = -1;
		while ((amountRead = is.read(buffer)) != -1)
		{
			sb.append(new String(buffer, 0, amountRead));
		}
		return sb.toString();
	}
}
