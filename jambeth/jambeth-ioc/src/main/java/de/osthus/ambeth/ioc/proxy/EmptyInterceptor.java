package de.osthus.ambeth.ioc.proxy;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.proxy.AbstractSimpleInterceptor;

public final class EmptyInterceptor extends AbstractSimpleInterceptor
{
	public static final MethodInterceptor INSTANCE = new EmptyInterceptor();

	private EmptyInterceptor()
	{
		// Intended blank
	}

	@Override
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (Object.class.equals(method.getDeclaringClass()))
		{
			return proxy.invoke(this, args);
		}
		throw new UnsupportedOperationException("Should never be called");
	}
}
