package com.koch.ambeth.ioc.threadlocal;

import com.koch.ambeth.util.ReflectUtil;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class ShallowCopyValueResolver implements IForkedValueResolver
{
	private final Object originalValue;

	public ShallowCopyValueResolver(Object originalValue)
	{
		this.originalValue = originalValue;
	}

	@Override
	public Object createForkedValue()
	{
		try
		{
			return ReflectUtil.getDeclaredMethod(false, originalValue.getClass(), null, "clone", new Class<?>[0]).invoke(originalValue);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	public Object getOriginalValue()
	{
		return originalValue;
	}
}
