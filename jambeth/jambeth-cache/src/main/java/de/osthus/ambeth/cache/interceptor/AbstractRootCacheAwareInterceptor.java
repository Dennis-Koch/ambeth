package de.osthus.ambeth.cache.interceptor;

import java.lang.reflect.Method;

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
import de.osthus.ambeth.proxy.AbstractSimpleInterceptor;
import de.osthus.ambeth.service.ICacheRetriever;
import de.osthus.ambeth.service.IOfflineListenerExtendable;
import de.osthus.ambeth.util.Lock;

public abstract class AbstractRootCacheAwareInterceptor extends AbstractSimpleInterceptor implements IThreadLocalCleanupBean
{
	protected static final Method clearMethod;

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

	@Autowired(optional = true)
	protected IOfflineListenerExtendable offlineListenerExtendable;

	@Autowired
	protected IServiceContext serviceContext;

	@Autowired
	protected ICacheRetriever storedCacheRetriever;

	protected IRootCache acquireRootCache(boolean privileged, ThreadLocal<RootCache> currentRootCacheTL)
	{
		return acquireRootCache(privileged, currentRootCacheTL, storedCacheRetriever, null, null);
	}

	protected IRootCache acquireRootCache(boolean privileged, ThreadLocal<RootCache> currentRootCacheTL, ICacheRetriever cacheRetriever, Lock readLock,
			Lock writeLock)
	{
		IBeanRuntime<RootCache> rootCacheBR = serviceContext.registerBean(RootCache.class).propertyValue("CacheRetriever", cacheRetriever);
		if (readLock != null)
		{
			rootCacheBR.propertyValue("ReadLock", readLock);
		}
		if (writeLock != null)
		{
			rootCacheBR.propertyValue("WriteLock", writeLock);
		}
		RootCache rootCache = postProcessRootCacheConfiguration(rootCacheBR).propertyValue("Privileged", Boolean.valueOf(privileged)).finish();

		if (offlineListenerExtendable != null)
		{
			offlineListenerExtendable.addOfflineListener(rootCache);
		}
		currentRootCacheTL.set(rootCache);
		return rootCache;
	}

	protected IBeanRuntime<RootCache> postProcessRootCacheConfiguration(IBeanRuntime<RootCache> rootCacheBR)
	{
		// Do not inject EventQueue because caches without foreign interest will never receive async DCEs
		return rootCacheBR.ignoreProperties("EventQueue").propertyValue("WeakEntries", Boolean.FALSE);
	}

	protected void disposeCurrentRootCache(ThreadLocal<RootCache> currentTL)
	{
		RootCache rootCache = currentTL.get();
		currentTL.remove();
		if (rootCache == null)
		{
			return;
		}
		if (offlineListenerExtendable != null)
		{
			offlineListenerExtendable.removeOfflineListener(rootCache);
		}
		// Cut reference to persistence layer
		rootCache.dispose();
	}
}