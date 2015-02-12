package de.osthus.ambeth.proxy;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public abstract class AbstractSimpleInterceptor implements MethodInterceptor
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

	@Override
	public final Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (finalizeMethod.equals(method))
		{
			// Do nothing. This is to prevent unnecessary exceptions in tomcat in REDEPLOY scenarios
			return null;
		}
		if (equalsMethod.equals(method) && args[0] == obj)
		{
			// Do nothing. This is to prevent unnecessary exceptions in tomcat in REDEPLOY scenarios
			return Boolean.TRUE;
		}
		try
		{
			return interceptIntern(obj, method, args, proxy);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e, method.getExceptionTypes());
		}
	}

	protected abstract Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable;
}