package de.osthus.ambeth.cache.interceptor;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.cache.IRootCache;
import de.osthus.ambeth.cache.RootCache;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.SensitiveThreadLocal;

public class ThreadLocalRootCacheInterceptor extends AbstractRootCacheAwareInterceptor implements MethodInterceptor
{
	// Important to load the foreign static field to this static field on startup because of potential unnecessary classloading issues on finalize()
	protected static final Method finalizeMethod = AbstractRootCacheAwareInterceptor.finalizeMethod;

	protected static final Method clearMethod = AbstractRootCacheAwareInterceptor.clearMethod;

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(defaultValue = "false")
	protected boolean privileged;

	protected final ThreadLocal<RootCache> rootCacheTL = new SensitiveThreadLocal<RootCache>();

	protected IRootCache getCurrentRootCache()
	{
		IRootCache rootCache = getCurrentRootCacheIfValid();
		if (rootCache == null)
		{
			rootCache = acquireRootCache(privileged, rootCacheTL);
		}
		return rootCache;
	}

	protected IRootCache getCurrentRootCacheIfValid()
	{
		return rootCacheTL.get();
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (finalizeMethod.equals(method))
		{
			return null;
		}
		if (clearMethod.equals(method))
		{
			IRootCache rootCache = getCurrentRootCacheIfValid();
			if (rootCache == null)
			{
				// Nothing to do
				return null;
			}
		}
		IRootCache rootCache = getCurrentRootCache();
		return proxy.invoke(rootCache, args);
	}

	@Override
	public void cleanupThreadLocal()
	{
		disposeCurrentRootCache(rootCacheTL);
	}
}