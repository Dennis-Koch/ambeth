package de.osthus.ambeth.ioc.proxy;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.proxy.CascadedInterceptor;

public final class EmptyInterceptor implements MethodInterceptor
{
	// Important to load the foreign static field to this static field on startup because of potential unnecessary classloading issues on finalize()
	private static final Method finalizeMethod = CascadedInterceptor.finalizeMethod;

	public static final MethodInterceptor INSTANCE = new EmptyInterceptor();

	private EmptyInterceptor()
	{
		// Intended blank
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (finalizeMethod.equals(method))
		{
			return null;
		}
		throw new UnsupportedOperationException("Should never be called");
	}
}
