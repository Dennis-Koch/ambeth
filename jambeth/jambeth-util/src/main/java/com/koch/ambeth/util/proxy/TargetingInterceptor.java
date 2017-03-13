package com.koch.ambeth.util.proxy;

import java.lang.reflect.Method;

import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

import net.sf.cglib.proxy.MethodProxy;

public class TargetingInterceptor extends AbstractSimpleInterceptor
{
	protected ITargetProvider targetProvider;

	public void setTargetProvider(ITargetProvider targetProvider)
	{
		this.targetProvider = targetProvider;
	}

	public ITargetProvider getTargetProvider()
	{
		return targetProvider;
	}

	@Override
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (obj instanceof IDisposable && method.getName().equals("dispose") && method.getParameterTypes().length == 0)
		{
			return null;
		}
		Object target = targetProvider.getTarget();
		if (target == null)
		{
			throw new NullPointerException("Object reference has to be valid. TargetProvider: " + targetProvider);
		}
		try
		{
			return proxy.invoke(target, args);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e, method.getExceptionTypes());
		}
	}
}