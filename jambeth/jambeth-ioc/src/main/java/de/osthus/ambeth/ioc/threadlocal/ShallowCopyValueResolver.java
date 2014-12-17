package de.osthus.ambeth.ioc.threadlocal;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.util.ReflectUtil;

public class ShallowCopyValueResolver implements IForkedValueResolver
{
	protected final Object forkedValue;

	public ShallowCopyValueResolver(Object forkedValue)
	{
		this.forkedValue = forkedValue;
	}

	@Override
	public Object getForkedValue()
	{
		try
		{
			return ReflectUtil.getDeclaredMethod(false, forkedValue.getClass(), null, "clone", new Class<?>[0]).invoke(forkedValue);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
