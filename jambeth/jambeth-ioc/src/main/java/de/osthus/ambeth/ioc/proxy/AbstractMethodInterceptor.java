package de.osthus.ambeth.ioc.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.proxy.AbstractSimpleInterceptor;

/**
 * the interceptor for register abstract class as bean, the abstract method always should be intercepted before this intercptIntern method call.
 */
public final class AbstractMethodInterceptor extends AbstractSimpleInterceptor
{
	public static final MethodInterceptor INSTANCE = new AbstractMethodInterceptor();

	private AbstractMethodInterceptor()
	{
		// Intended blank
	}

	@Override
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (Modifier.isAbstract(method.getModifiers()))
		{
			throw new UnsupportedOperationException("Should never be called, Because this method[" + method + "] should be intercept by some Intercepter");
		}
		return proxy.invokeSuper(obj, args);
	}
}
