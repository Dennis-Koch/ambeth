package de.osthus.ambeth.cache.interceptor;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import de.osthus.ambeth.cache.HandleContentDelegate;
import de.osthus.ambeth.cache.ICacheIntern;
import de.osthus.ambeth.cache.IRootCache;
import de.osthus.ambeth.cache.ISecondLevelCacheManager;
import de.osthus.ambeth.cache.ITransactionalRootCache;
import de.osthus.ambeth.cache.RootCache;
import de.osthus.ambeth.cache.rootcachevalue.RootCacheValue;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.security.ISecurityActivation;
import de.osthus.ambeth.security.config.SecurityConfigurationConstants;
import de.osthus.ambeth.service.ICacheRetriever;
import de.osthus.ambeth.threading.SensitiveThreadLocal;

public class TransactionalRootCacheInterceptor extends AbstractRootCacheAwareInterceptor implements MethodInterceptor, ITransactionalRootCache,
		ISecondLevelCacheManager
{
	// Important to load the foreign static field to this static field on startup because of potential unnecessary classloading issues on finalize()
	protected static final Method finalizeMethod = AbstractRootCacheAwareInterceptor.finalizeMethod;

	protected static final Method clearMethod = AbstractRootCacheAwareInterceptor.clearMethod;

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IRootCache committedRootCache;

	@Autowired(optional = true)
	protected ISecurityActivation securityActivation;

	@Property(name = SecurityConfigurationConstants.SecurityActive, defaultValue = "false")
	protected boolean securityActive;

	protected final ThreadLocal<RootCache> privilegedRootCacheTL = new SensitiveThreadLocal<RootCache>();

	protected final ThreadLocal<RootCache> rootCacheTL = new SensitiveThreadLocal<RootCache>();

	protected final ThreadLocal<Boolean> transactionalRootCacheActiveTL = new SensitiveThreadLocal<Boolean>();

	@Override
	public void cleanupThreadLocal()
	{
		transactionalRootCacheActiveTL.remove();
		disposeCurrentRootCache(privilegedRootCacheTL);
		disposeCurrentRootCache(rootCacheTL);
	}

	protected IRootCache getCurrentRootCache(boolean privileged)
	{
		IRootCache rootCache = privileged ? privilegedRootCacheTL.get() : rootCacheTL.get();
		if (rootCache != null)
		{
			return rootCache;
		}
		if (!Boolean.TRUE.equals(transactionalRootCacheActiveTL.get()))
		{
			// If no thread-bound root cache is active (which implies that no transaction is currently active
			// return the unbound root cache (which reads committed data)
			return committedRootCache;
		}
		// if we need a cache and security is active the privileged cache is a prerequisite in both cases
		IRootCache privilegedRootCache = privilegedRootCacheTL.get();
		if (privilegedRootCache == null)
		{
			// here we know that the non-privileged one could not have existed before, so we simply create the privileged one
			privilegedRootCache = acquireRootCache(privileged, privilegedRootCacheTL);
		}
		if (privileged)
		{
			// we need only the privilegedRootCache so we are finished
			return privilegedRootCache;
		}
		IRootCache nonPrivilegedRootCache = rootCacheTL.get();
		if (nonPrivilegedRootCache == null)
		{
			// share the locks from the privileged rootCache
			nonPrivilegedRootCache = acquireRootCache(privileged, rootCacheTL, (ICacheRetriever) privilegedRootCache, privilegedRootCache.getReadLock(),
					privilegedRootCache.getWriteLock());
		}
		return nonPrivilegedRootCache;
	}

	@Override
	public IRootCache selectSecondLevelCache()
	{
		return getCurrentRootCache(isCurrentPrivileged());
	}

	protected boolean isCurrentPrivileged()
	{
		return !securityActive || !securityActivation.isFilterActivated();
	}

	@Override
	public void acquireTransactionalRootCache()
	{
		if (privilegedRootCacheTL.get() != null || privilegedRootCacheTL.get() != null)
		{
			throw new IllegalStateException("Transactional root cache already acquired");
		}
		transactionalRootCacheActiveTL.set(Boolean.TRUE);
	}

	@Override
	public void disposeTransactionalRootCache(boolean success)
	{
		transactionalRootCacheActiveTL.remove();

		disposeCurrentRootCache(rootCacheTL);

		IRootCache rootCache = privilegedRootCacheTL.get();
		if (rootCache == null)
		{
			disposeCurrentRootCache(privilegedRootCacheTL);
			// This may happen if an exception occurs while committing and therefore calling a rollback
			return;
		}
		try
		{
			if (success)
			{
				final ArrayList<RootCacheValue> content = new ArrayList<RootCacheValue>();

				// Save information into second level cache for committed data
				rootCache.getContent(new HandleContentDelegate()
				{
					@Override
					public void invoke(Class<?> entityType, byte idIndex, Object id, Object value)
					{
						content.add((RootCacheValue) value);
					}
				});
				if (content.size() > 0)
				{
					rootCache.clear();
					committedRootCache.put(content);
				}
			}
		}
		finally
		{
			disposeCurrentRootCache(privilegedRootCacheTL);
		}
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
			IRootCache rootCache = privilegedRootCacheTL.get();
			if (rootCache != null)
			{
				rootCache.clear();
			}
			rootCache = rootCacheTL.get();
			if (rootCache != null)
			{
				rootCache.clear();
			}
			return null;
		}
		ICacheIntern requestingCache = null;
		for (Object arg : args)
		{
			if (arg instanceof ICacheIntern)
			{
				requestingCache = (ICacheIntern) arg;
				break;
			}
		}
		boolean privileged;
		if (requestingCache != null)
		{
			privileged = requestingCache.isPrivileged();
		}
		else
		{
			privileged = isCurrentPrivileged();
		}
		IRootCache rootCache = getCurrentRootCache(privileged);
		return proxy.invoke(rootCache, args);
	}
}
