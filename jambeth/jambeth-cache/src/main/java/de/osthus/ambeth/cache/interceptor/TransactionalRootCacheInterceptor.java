package de.osthus.ambeth.cache.interceptor;

import de.osthus.ambeth.cache.HandleContentDelegate;
import de.osthus.ambeth.cache.IRootCache;
import de.osthus.ambeth.cache.ISecondLevelCacheManager;
import de.osthus.ambeth.cache.ITransactionalRootCache;
import de.osthus.ambeth.cache.rootcachevalue.RootCacheValue;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.threading.SensitiveThreadLocal;

public class TransactionalRootCacheInterceptor extends ThreadLocalRootCacheInterceptor implements ITransactionalRootCache, ISecondLevelCacheManager
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IRootCache committedRootCache;

	protected final ThreadLocal<Boolean> transactionalRootCacheActiveTL = new SensitiveThreadLocal<Boolean>();

	@Override
	public void cleanupThreadLocal()
	{
		transactionalRootCacheActiveTL.remove();
		super.cleanupThreadLocal();
	}

	@Override
	protected IRootCache getCurrentRootCacheIfValid()
	{
		IRootCache rootCache = rootCacheTL.get();
		if (rootCache == null && Boolean.TRUE.equals(transactionalRootCacheActiveTL.get()))
		{
			// Lazy init of transactional rootcache
			rootCache = super.acquireCurrentRootCache();
		}
		// If no thread-bound root cache is active (which implies that no transaction is currently active
		// return the unbound root cache (which reads uncommitted data)
		return rootCache != null ? rootCache : committedRootCache;
	}

	@Override
	public IRootCache selectSecondLevelCache()
	{
		return getCurrentRootCacheIfValid();
	}

	@Override
	public void acquireTransactionalRootCache()
	{
		IRootCache rootCache = rootCacheTL.get();
		if (rootCache != null)
		{
			throw new IllegalStateException("Transactional root cache already acquired");
		}
		transactionalRootCacheActiveTL.set(Boolean.TRUE);
	}

	@Override
	public void disposeTransactionalRootCache(boolean success)
	{
		transactionalRootCacheActiveTL.remove();

		IRootCache rootCache = rootCacheTL.get();
		if (rootCache == null)
		{
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
			disposeCurrentRootCache();
		}
	}
}
