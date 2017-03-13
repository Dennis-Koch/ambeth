package com.koch.ambeth.ioc.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.koch.ambeth.util.proxy.AbstractSimpleInterceptor;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

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
		else if (!Modifier.isAbstract(method.getModifiers()))
		{
			return proxy.invokeSuper(obj, args);
		}
		throw new UnsupportedOperationException("Should never be called");
	}
}
