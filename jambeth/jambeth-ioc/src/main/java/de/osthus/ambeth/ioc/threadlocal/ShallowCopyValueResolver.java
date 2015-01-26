package de.osthus.ambeth.ioc.threadlocal;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.util.ReflectUtil;

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
