package de.osthus.ambeth.proxy;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.annotation.AnnotationCache;
import de.osthus.ambeth.annotation.Find;
import de.osthus.ambeth.annotation.Merge;
import de.osthus.ambeth.annotation.NoProxy;
import de.osthus.ambeth.annotation.Process;
import de.osthus.ambeth.annotation.Remove;

public abstract class AbstractInterceptor extends CascadedInterceptor
{
	protected static final AnnotationCache<Find> findCache = new AnnotationCache<Find>(Find.class)
	{
		@Override
		protected boolean annotationEquals(Find left, Find right)
		{
			return left.equals(right);
		}
	};

	protected static final AnnotationCache<Merge> mergeCache = new AnnotationCache<Merge>(Merge.class)
	{
		@Override
		protected boolean annotationEquals(Merge left, Merge right)
		{
			return left.equals(right);
		}
	};

	protected static final AnnotationCache<Remove> removeCache = new AnnotationCache<Remove>(Remove.class)
	{
		@Override
		protected boolean annotationEquals(Remove left, Remove right)
		{
			return left.equals(right);
		}
	};

	protected static final AnnotationCache<NoProxy> noProxyCache = new AnnotationCache<NoProxy>(NoProxy.class)
	{
		@Override
		protected boolean annotationEquals(NoProxy left, NoProxy right)
		{
			return left.equals(right);
		}
	};

	protected static final AnnotationCache<Process> processCache = new AnnotationCache<Process>(Process.class)
	{
		@Override
		protected boolean annotationEquals(Process left, Process right)
		{
			return left.equals(right);
		}
	};

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (CascadedInterceptor.finalizeMethod.equals(method))
		{
			return null;
		}
		if (noProxyCache.getAnnotation(method) != null)
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
		return intercept(obj, method, args, proxy, methodName, isAsyncBegin);
	}

	protected Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy, String methodName, Boolean isAsyncBegin) throws Throwable
	{
		if (processCache.getAnnotation(method) != null)
		{
			return interceptApplication(obj, method, args, proxy, isAsyncBegin);
		}
		if (mergeCache.getAnnotation(method) != null || methodName.startsWith("create") || methodName.startsWith("update") || methodName.startsWith("save")
				|| methodName.startsWith("merge") || methodName.startsWith("insert"))
		{
			return interceptMerge(method, args, isAsyncBegin);
		}
		if (removeCache.getAnnotation(method) != null || methodName.startsWith("delete") || methodName.startsWith("remove"))
		{
			return interceptDelete(method, args, isAsyncBegin);
		}
		if (findCache.getAnnotation(method) != null || methodName.startsWith("retrieve") || methodName.startsWith("read") || methodName.startsWith("find")
				|| methodName.startsWith("get"))
		{
			return interceptLoad(obj, method, args, proxy, isAsyncBegin);
		}
		if (methodName.equals("close") || methodName.equals("abort"))
		{
			// Intended blank
		}
		return interceptApplication(obj, method, args, proxy, isAsyncBegin);
	}

	protected Object interceptApplication(Object obj, Method method, Object[] args, MethodProxy proxy, Boolean isAsyncBegin) throws Throwable
	{
		return invokeTarget(obj, method, args, proxy);
	}

	protected Object interceptLoad(Object obj, Method method, Object[] args, MethodProxy proxy, Boolean isAsyncBegin) throws Throwable
	{
		Object returnValue = invokeTarget(obj, method, args, proxy);

		if (Boolean.TRUE.equals(isAsyncBegin))
		{
			throw new RuntimeException();
			// return (IAsyncResult)invocation.ReturnValue;
		}
		return interceptLoadIntern(method, args, isAsyncBegin, returnValue);
	}

	protected Object interceptMerge(Method method, Object[] args, Boolean isAsyncBegin) throws Throwable
	{
		if (Boolean.FALSE.equals(isAsyncBegin))
		{
			throw new RuntimeException();
			// return ((IAsyncResult)invocation.Arguments[0]).AsyncState;
		}
		return interceptMergeIntern(method, args, isAsyncBegin);
	}

	protected Object interceptDelete(Method method, Object[] args, Boolean isAsyncBegin) throws Throwable
	{
		if (Boolean.FALSE.equals(isAsyncBegin))
		{
			throw new RuntimeException();
			// return ((IAsyncResult)invocation.Arguments[0]).AsyncState;
		}
		return interceptDeleteIntern(method, args, isAsyncBegin);
	}

	protected Object interceptLoadIntern(Method method, Object[] arguments, Boolean isAsyncBegin, Object result) throws Throwable
	{
		return result;
	}

	protected abstract Object interceptMergeIntern(Method method, Object[] arguments, Boolean isAsyncBegin) throws Throwable;

	protected abstract Object interceptDeleteIntern(Method method, Object[] arguments, Boolean isAsyncBegin) throws Throwable;
}