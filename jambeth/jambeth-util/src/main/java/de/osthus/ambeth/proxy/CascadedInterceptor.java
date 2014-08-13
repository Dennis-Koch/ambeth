package de.osthus.ambeth.proxy;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public abstract class CascadedInterceptor implements ICascadedInterceptor
{
	public static final Method finalizeMethod;

	public static final Method equalsMethod;

	static
	{
		try
		{
			equalsMethod = Object.class.getDeclaredMethod("equals", Object.class);
			finalizeMethod = Object.class.getDeclaredMethod("finalize");
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

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
		if (finalizeMethod.equals(method))
		{
			// Do nothing. This is to prevent unnecessary exceptions in tomcat in REDEPLOY scenarios
			return null;
		}
		if (equalsMethod.equals(method))
		{
			if (obj == args[0])
			{
				return Boolean.TRUE;
			}
		}
		if (target instanceof MethodInterceptor)
		{
			return ((MethodInterceptor) target).intercept(obj, method, args, proxy);
		}
		return proxy.invoke(target, args);
	}
}