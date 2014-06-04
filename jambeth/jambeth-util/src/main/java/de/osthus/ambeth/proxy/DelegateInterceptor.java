package de.osthus.ambeth.proxy;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.collections.HashMap;

public class DelegateInterceptor implements MethodInterceptor
{
	// Important to load the foreign static field to this static field on startup because of potential unnecessary classloading issues on finalize()
	private static final Method finalizeMethod = CascadedInterceptor.finalizeMethod;

	protected final Object target;

	protected final HashMap<Method, Method> methodMap;

	public DelegateInterceptor(Object target, HashMap<Method, Method> methodMap)
	{
		this.target = target;
		this.methodMap = methodMap;
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (finalizeMethod.equals(method))
		{
			// Do nothing. This is to prevent unnecessary exceptions in tomcat in REDEPLOY scenarios
			return null;
		}
		Method mappedMethod = methodMap.get(method);
		if (mappedMethod == null)
		{
			return method.invoke(target, args);
		}
		return mappedMethod.invoke(target, args);
	}
}
