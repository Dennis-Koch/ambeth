package de.osthus.ambeth.cache.interceptor;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.Stack;

import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ICacheContext;
import de.osthus.ambeth.cache.ICacheProvider;
import de.osthus.ambeth.cache.ICacheProviderExtendable;
import de.osthus.ambeth.cache.IRootCache;
import de.osthus.ambeth.cache.ISingleCacheParamRunnable;
import de.osthus.ambeth.cache.ISingleCacheRunnable;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.AbstractSimpleInterceptor;
import de.osthus.ambeth.threading.SensitiveThreadLocal;
import de.osthus.ambeth.util.ParamChecker;

public class CacheProviderInterceptor extends AbstractSimpleInterceptor implements ICacheProviderExtendable, ICacheProvider, ICacheContext,
		IThreadLocalCleanupBean
{
	public static class SingleCacheProvider implements ICacheProvider
	{
		protected final ICache cache;

		public SingleCacheProvider(ICache cache)
		{
			this.cache = cache;
		}

		@Override
		public ICache getCurrentCache()
		{
			return cache;
		}

		@Override
		public boolean isNewInstanceOnCall()
		{
			return false;
		}
	}

	private static final Set<Method> methodsDirectlyToRootCache = new HashSet<Method>();

	static
	{
		try
		{
			methodsDirectlyToRootCache.add(ICache.class.getMethod("getReadLock"));
			methodsDirectlyToRootCache.add(ICache.class.getMethod("getWriteLock"));
		}
		catch (NoSuchMethodException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final Stack<ICacheProvider> cacheProviderStack = new Stack<ICacheProvider>();

	protected final ThreadLocal<Stack<ICacheProvider>> cacheProviderStackTL = new SensitiveThreadLocal<Stack<ICacheProvider>>();

	@Autowired
	protected ICacheProvider threadLocalCacheProvider;

	@Autowired
	protected IRootCache rootCache;

	@Override
	public void cleanupThreadLocal()
	{
		cacheProviderStackTL.remove();
	}

	@Override
	public void registerCacheProvider(ICacheProvider cacheProvider)
	{
		ParamChecker.assertParamNotNull(cacheProvider, "cacheProvider");
		cacheProviderStack.push(cacheProvider);
	}

	@Override
	public void unregisterCacheProvider(ICacheProvider cacheProvider)
	{
		ParamChecker.assertParamNotNull(cacheProvider, "cacheProvider");
		if (cacheProviderStack.peek() != cacheProvider)
		{
			throw new IllegalStateException("The current cacheProvider is not the one specified to unregister");
		}
		cacheProviderStack.pop();
	}

	public ICacheProvider getCurrentCacheProvider()
	{
		Stack<ICacheProvider> stack = cacheProviderStackTL.get();
		if (stack != null && stack.size() > 0)
		{
			return stack.peek();
		}
		return cacheProviderStack.peek();
	}

	@Override
	public ICache getCurrentCache()
	{
		return getCurrentCacheProvider().getCurrentCache();
	}

	@Override
	public boolean isNewInstanceOnCall()
	{
		return getCurrentCacheProvider().isNewInstanceOnCall();
	}

	@Override
	public <R> R executeWithCache(ISingleCacheRunnable<R> runnable) throws Throwable
	{
		return executeWithCache(threadLocalCacheProvider, runnable);
	}

	@Override
	public <R, T> R executeWithCache(ISingleCacheParamRunnable<R, T> runnable, T state) throws Throwable
	{
		return executeWithCache(threadLocalCacheProvider, runnable, state);
	}

	@Override
	public <R> R executeWithCache(ICacheProvider cacheProvider, ISingleCacheRunnable<R> runnable) throws Throwable
	{
		ParamChecker.assertParamNotNull(cacheProvider, "cacheProvider");
		ParamChecker.assertParamNotNull(runnable, "runnable");
		ICache cache = cacheProvider.getCurrentCache();
		return executeWithCache(cache, runnable);
	}

	@Override
	public <R, T> R executeWithCache(ICacheProvider cacheProvider, ISingleCacheParamRunnable<R, T> runnable, T state) throws Throwable
	{
		ParamChecker.assertParamNotNull(cacheProvider, "cacheProvider");
		ParamChecker.assertParamNotNull(runnable, "runnable");
		ICache cache = cacheProvider.getCurrentCache();
		return executeWithCache(cache, runnable, state);
	}

	@Override
	public <R> R executeWithCache(final ICache cache, ISingleCacheRunnable<R> runnable) throws Throwable
	{
		ParamChecker.assertParamNotNull(cache, "cache");
		ParamChecker.assertParamNotNull(runnable, "runnable");
		ICacheProvider singletonCacheProvider = new SingleCacheProvider(cache);

		Stack<ICacheProvider> stack = cacheProviderStackTL.get();
		if (stack == null)
		{
			stack = new Stack<ICacheProvider>();
			cacheProviderStackTL.set(stack);
		}
		stack.push(singletonCacheProvider);
		try
		{
			return runnable.run();
		}
		finally
		{
			if (stack.pop() != singletonCacheProvider)
			{
				throw new IllegalStateException("Must never happen");
			}
		}
	}

	@Override
	public <R, T> R executeWithCache(ICache cache, ISingleCacheParamRunnable<R, T> runnable, T state) throws Throwable
	{
		ParamChecker.assertParamNotNull(cache, "cache");
		ParamChecker.assertParamNotNull(runnable, "runnable");
		ICacheProvider singletonCacheProvider = new SingleCacheProvider(cache);

		Stack<ICacheProvider> stack = cacheProviderStackTL.get();
		if (stack == null)
		{
			stack = new Stack<ICacheProvider>();
			cacheProviderStackTL.set(stack);
		}
		stack.push(singletonCacheProvider);
		try
		{
			return runnable.run(state);
		}
		finally
		{
			if (stack.pop() != singletonCacheProvider)
			{
				throw new IllegalStateException("Must never happen");
			}
		}
	}

	@Override
	protected Object interceptIntern(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		ICacheProvider cacheProvider = getCurrentCacheProvider();
		if (method.getDeclaringClass().equals(ICacheProvider.class))
		{
			return proxy.invoke(cacheProvider, args);
		}
		Object target;
		if (!cacheProvider.isNewInstanceOnCall() || !methodsDirectlyToRootCache.contains(method))
		{
			target = cacheProvider.getCurrentCache();
		}
		else
		{
			target = rootCache;
		}
		return proxy.invoke(target, args);
	}
}
