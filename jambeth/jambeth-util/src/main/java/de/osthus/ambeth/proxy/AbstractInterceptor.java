package de.osthus.ambeth.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.annotation.Find;
import de.osthus.ambeth.annotation.Merge;
import de.osthus.ambeth.annotation.NoProxy;
import de.osthus.ambeth.annotation.Process;
import de.osthus.ambeth.annotation.Remove;

public abstract class AbstractInterceptor extends CascadedInterceptor
{
	protected abstract Annotation getMethodLevelBehavior(Method method);

	@Override
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		Annotation annotation = getMethodLevelBehavior(method);
		if (annotation instanceof NoProxy)
		{
			return invokeTarget(obj, method, args, proxy);
		}
		String methodName = method.getName().toLowerCase();
		Boolean isAsyncBegin = null;
		if (methodName.startsWith("begin"))
		{
			isAsyncBegin = Boolean.TRUE;
			methodName = methodName.substring(5);
		}
		else if (methodName.startsWith("end"))
		{
			isAsyncBegin = Boolean.FALSE;
			methodName = methodName.substring(3);
		}
		return intercept(obj, method, args, proxy, annotation, methodName, isAsyncBegin);
	}

	protected Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy, Annotation annotation, String methodName, Boolean isAsyncBegin)
			throws Throwable
	{
		if (annotation instanceof Process)
		{
			return interceptApplication(obj, method, args, proxy, annotation, isAsyncBegin);
		}
		if (annotation instanceof Merge || methodName.startsWith("create") || methodName.startsWith("update") || methodName.startsWith("save")
				|| methodName.startsWith("merge") || methodName.startsWith("insert"))
		{
			return interceptMerge(method, args, annotation, isAsyncBegin);
		}
		if (annotation instanceof Remove || methodName.startsWith("delete") || methodName.startsWith("remove"))
		{
			return interceptDelete(method, args, annotation, isAsyncBegin);
		}
		if (annotation instanceof Find || methodName.startsWith("retrieve") || methodName.startsWith("read") || methodName.startsWith("find")
				|| methodName.startsWith("get"))
		{
			return interceptLoad(obj, method, args, proxy, annotation, isAsyncBegin);
		}
		if (methodName.equals("close") || methodName.equals("abort"))
		{
			// Intended blank
		}
		return interceptApplication(obj, method, args, proxy, annotation, isAsyncBegin);
	}

	protected Object interceptApplication(Object obj, Method method, Object[] args, MethodProxy proxy, Annotation annotation, Boolean isAsyncBegin)
			throws Throwable
	{
		return invokeTarget(obj, method, args, proxy);
	}

	protected Object interceptLoad(Object obj, Method method, Object[] args, MethodProxy proxy, Annotation annotation, Boolean isAsyncBegin) throws Throwable
	{
		Object returnValue = invokeTarget(obj, method, args, proxy);

		if (Boolean.TRUE.equals(isAsyncBegin))
		{
			throw new RuntimeException();
			// return (IAsyncResult)invocation.ReturnValue;
		}
		return interceptLoadIntern(method, args, annotation, isAsyncBegin, returnValue);
	}

	protected Object interceptMerge(Method method, Object[] args, Annotation annotation, Boolean isAsyncBegin) throws Throwable
	{
		if (Boolean.FALSE.equals(isAsyncBegin))
		{
			throw new RuntimeException();
			// return ((IAsyncResult)invocation.Arguments[0]).AsyncState;
		}
		return interceptMergeIntern(method, args, annotation, isAsyncBegin);
	}

	protected Object interceptDelete(Method method, Object[] args, Annotation annotation, Boolean isAsyncBegin) throws Throwable
	{
		if (Boolean.FALSE.equals(isAsyncBegin))
		{
			throw new RuntimeException();
			// return ((IAsyncResult)invocation.Arguments[0]).AsyncState;
		}
		return interceptDeleteIntern(method, args, annotation, isAsyncBegin);
	}

	protected Object interceptLoadIntern(Method method, Object[] arguments, Annotation annotation, Boolean isAsyncBegin, Object result) throws Throwable
	{
		return result;
	}

	protected abstract Object interceptMergeIntern(Method method, Object[] arguments, Annotation annotation, Boolean isAsyncBegin) throws Throwable;

	protected abstract Object interceptDeleteIntern(Method method, Object[] arguments, Annotation annotation, Boolean isAsyncBegin) throws Throwable;
}