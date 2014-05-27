package de.osthus.ambeth.proxy;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.util.IDisposable;

public class TargetingInterceptor implements MethodInterceptor
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
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (CascadedInterceptor.finalizeMethod.equals(method))
		{
			return null;
		}
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