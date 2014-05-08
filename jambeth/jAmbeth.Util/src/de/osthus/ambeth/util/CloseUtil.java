package de.osthus.ambeth.util;

import java.io.Closeable;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public final class CloseUtil
{
	public static void close(Closeable closeable)
	{
		if (closeable == null)
		{
			return;
		}
		try
		{
			closeable.close();
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	private CloseUtil()
	{
		// Intended blank
	}
}
