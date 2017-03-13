package com.koch.ambeth.util.proxy;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public abstract class CascadedInterceptor extends AbstractSimpleInterceptor implements ICascadedInterceptor
{
	protected Object target;

	@Override
	public Object getTarget()
	{
		return target;
	}

	@Override
	public void setTarget(Object obj)
	{
		target = obj;
	}

	protected Object invokeTarget(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (target instanceof MethodInterceptor)
		{
			return ((MethodInterceptor) target).intercept(obj, method, args, proxy);
		}
		return proxy.invoke(target, args);
	}
}