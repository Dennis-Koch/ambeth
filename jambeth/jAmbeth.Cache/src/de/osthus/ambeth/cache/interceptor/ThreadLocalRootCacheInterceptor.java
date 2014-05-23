package de.osthus.ambeth.cache.interceptor;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.cache.IRootCache;
import de.osthus.ambeth.cache.IWritableCache;
import de.osthus.ambeth.cache.RootCache;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IBeanRuntime;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.CascadedInterceptor;
import de.osthus.ambeth.service.ICacheRetriever;
import de.osthus.ambeth.service.IOfflineListenerExtendable;
import de.osthus.ambeth.threading.SensitiveThreadLocal;
import de.osthus.ambeth.util.ParamChecker;

public class ThreadLocalRootCacheInterceptor implements IInitializingBean, MethodInterceptor, IThreadLocalCleanupBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	private static final Method clearMethod;

	static
	{
		try
		{
			clearMethod = IWritableCache.class.getMethod("clear");
		}
		catch (SecurityException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		catch (NoSuchMethodException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected final ThreadLocal<RootCache> rootCacheTL = new SensitiveThreadLocal<RootCache>();

	protected ICacheRetriever storedCacheRetriever;

	protected IOfflineListenerExtendable offlineListenerExtendable;

	protected IServiceContext serviceContext;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(serviceContext, "ServiceContext");
		ParamChecker.assertNotNull(storedCacheRetriever, "StoredCacheRetriever");
	}

	public void setOfflineListenerExtendable(IOfflineListenerExtendable offlineListenerExtendable)
	{
		this.offlineListenerExtendable = offlineListenerExtendable;
	}

	public void setServiceContext(IServiceContext serviceContext)
	{
		this.serviceContext = serviceContext;
	}

	public void setStoredCacheRetriever(ICacheRetriever storedCacheRetriever)
	{
		this.storedCacheRetriever = storedCacheRetriever;
	}

	protected IRootCache getCurrentRootCache()
	{
		IRootCache rootCache = getCurrentRootCacheIfValid();
		if (rootCache == null)
		{
			rootCache = acquireCurrentRootCache();
		}
		return rootCache;
	}

	protected IRootCache acquireCurrentRootCache()
	{
		IBeanRuntime<RootCache> rootCacheBR = serviceContext.registerAnonymousBean(RootCache.class).propertyValue("CacheRetriever", storedCacheRetriever);
		// Do not inject EventQueue because caches without foreign interest will never receive async DCEs
		rootCacheBR.ignoreProperties("EventQueue");
		rootCacheBR.propertyValue("WeakEntries", Boolean.FALSE);
		RootCache rootCache = rootCacheBR.finish();

		if (offlineListenerExtendable != null)
		{
			offlineListenerExtendable.addOfflineListener(rootCache);
		}
		rootCacheTL.set(rootCache);
		return rootCache;
	}

	protected IRootCache getCurrentRootCacheIfValid()
	{
		return rootCacheTL.get();
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
	{
		if (CascadedInterceptor.finalizeMethod.equals(method))
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
		disposeCurrentRootCache();
	}

	protected void disposeCurrentRootCache()
	{
		RootCache rootCache = rootCacheTL.get();
		if (rootCache == null)
		{
			return;
		}
		rootCacheTL.remove();

		if (offlineListenerExtendable != null)
		{
			offlineListenerExtendable.removeOfflineListener(rootCache);
		}
		// Cut reference to persistence layer
		rootCache.dispose();
	}
}