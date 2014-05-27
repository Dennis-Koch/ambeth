package de.osthus.ambeth.io;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public final class ByteBufferUtil
{
	public static void clean(ByteBuffer bb)
	{
		if (bb == null)
		{
			return;
		}
		try
		{
			Field cleanerField = bb.getClass().getDeclaredField("cleaner");
			cleanerField.setAccessible(true);
			Object cleaner = cleanerField.get(bb);
			if (cleaner == null)
			{
				return;
			}
			Method cleanMethod = cleaner.getClass().getMethod("clean");
			cleanMethod.invoke(cleaner);
		}
		catch (NoSuchFieldException e)
		{
			return;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	private ByteBufferUtil()
	{
		// Intended blank
	}
}
