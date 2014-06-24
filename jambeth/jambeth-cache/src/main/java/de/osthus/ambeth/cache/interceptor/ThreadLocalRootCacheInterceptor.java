package de.osthus.ambeth.cache.interceptor;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.cache.IRootCache;
import de.osthus.ambeth.cache.IWritableCache;
import de.osthus.ambeth.cache.RootCache;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IBeanRuntime;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.proxy.CascadedInterceptor;
import de.osthus.ambeth.security.ISecurityActivation;
import de.osthus.ambeth.service.ICacheRetriever;
import de.osthus.ambeth.service.IOfflineListenerExtendable;
import de.osthus.ambeth.threading.SensitiveThreadLocal;

public class ThreadLocalRootCacheInterceptor implements MethodInterceptor, IThreadLocalCleanupBean
{
	// Important to load the foreign static field to this static field on startup because of potential unnecessary classloading issues on finalize()
	private static final Method finalizeMethod = CascadedInterceptor.finalizeMethod;

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

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final ThreadLocal<RootCache> rootCacheTL = new SensitiveThreadLocal<RootCache>();

	protected final ThreadLocal<RootCache> privilegedRootCacheTL = new SensitiveThreadLocal<RootCache>();

	@Autowired(optional = true)
	protected IOfflineListenerExtendable offlineListenerExtendable;

	@Autowired(optional = true)
	protected ISecurityActivation securityActivation;

	@Autowired
	protected IServiceContext serviceContext;

	@Autowired
	protected ICacheRetriever storedCacheRetriever;

	protected IRootCache getCurrentRootCache()
	{
		IRootCache rootCache = getCurrentRootCacheIfValid();
		if (rootCache == null)
		{
			if (isPrivilegedMode())
			{
				rootCache = acquireCurrentPrivilegedRootCache();
			}
			else
			{
				rootCache = acquireCurrentRootCache();
			}
		}
		return rootCache;
	}

	protected IRootCache acquireCurrentRootCache()
	{
		IBeanRuntime<RootCache> rootCacheBR = serviceContext.registerAnonymousBean(RootCache.class).propertyValue("CacheRetriever", storedCacheRetriever);
		RootCache rootCache = postProcessRootCacheConfiguration(rootCacheBR).finish();

		if (offlineListenerExtendable != null)
		{
			offlineListenerExtendable.addOfflineListener(rootCache);
		}
		rootCacheTL.set(rootCache);
		return rootCache;
	}

	protected IRootCache acquireCurrentPrivilegedRootCache()
	{
		IBeanRuntime<RootCache> rootCacheBR = serviceContext.registerAnonymousBean(RootCache.class).propertyValue("CacheRetriever", storedCacheRetriever);
		RootCache rootCache = postProcessRootCacheConfiguration(rootCacheBR).ignoreProperties("PrivilegeProvider", "SecurityActivation",
				"SecurityScopeProvider").finish();

		if (offlineListenerExtendable != null)
		{
			offlineListenerExtendable.addOfflineListener(rootCache);
		}
		privilegedRootCacheTL.set(rootCache);
		return rootCache;
	}

	protected IBeanRuntime<RootCache> postProcessRootCacheConfiguration(IBeanRuntime<RootCache> rootCacheBR)
	{
		// Do not inject EventQueue because caches without foreign interest will never receive async DCEs
		return rootCacheBR.ignoreProperties("EventQueue").propertyValue("WeakEntries", Boolean.FALSE);
	}

	protected IRootCache getCurrentRootCacheIfValid()
	{
		if (isPrivilegedMode())
		{
			return privilegedRootCacheTL.get();
		}
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
		disposeCurrentRootCache();
	}

	protected boolean isPrivilegedMode()
	{
		return securityActivation == null || !securityActivation.isFilterActivated();
	}

	protected void disposeCurrentRootCache()
	{
		disposeCurrentRootCache(privilegedRootCacheTL);
		disposeCurrentRootCache(rootCacheTL);
	}

	protected void disposeCurrentRootCache(ThreadLocal<RootCache> currentTL)
	{
		RootCache rootCache = currentTL.get();
		if (rootCache == null)
		{
			return;
		}
		currentTL.remove();
		if (offlineListenerExtendable != null)
		{
			offlineListenerExtendable.removeOfflineListener(rootCache);
		}
		// Cut reference to persistence layer
		rootCache.dispose();
	}
}