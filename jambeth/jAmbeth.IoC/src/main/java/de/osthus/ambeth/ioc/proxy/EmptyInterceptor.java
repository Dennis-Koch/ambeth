package de.osthus.ambeth.ioc.proxy;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.proxy.CascadedInterceptor;

public final class EmptyInterceptor implements MethodInterceptor
{
	public static final MethodInterceptor INSTANCE = new EmptyInterceptor();

	private EmptyInterceptor()
	{
		// Intended blank
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (CascadedInterceptor.finalizeMethod.equals(method))
		{
			return null;
		}
		throw new UnsupportedOperationException("Should never be called");
	}
}
