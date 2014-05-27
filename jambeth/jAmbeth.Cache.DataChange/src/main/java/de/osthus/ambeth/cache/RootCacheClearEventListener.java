package de.osthus.ambeth.cache;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.event.IEventListener;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.ParamChecker;

public class RootCacheClearEventListener implements IEventListener, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected IFirstLevelCacheManager firstLevelCacheManager;

	protected ISecondLevelCacheManager secondLevelCacheManager;

	protected IRootCache committedRootCache;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(firstLevelCacheManager, "FirstLevelCacheManager");
		ParamChecker.assertNotNull(secondLevelCacheManager, "SecondLevelCacheManager");
		ParamChecker.assertNotNull(committedRootCache, "CommittedRootCache");
	}

	public void setFirstLevelCacheManager(IFirstLevelCacheManager firstLevelCacheManager)
	{
		this.firstLevelCacheManager = firstLevelCacheManager;
	}

	public void setSecondLevelCacheManager(ISecondLevelCacheManager secondLevelCacheManager)
	{
		this.secondLevelCacheManager = secondLevelCacheManager;
	}

	public void setCommittedRootCache(IRootCache committedRootCache)
	{
		this.committedRootCache = committedRootCache;
	}

	@Override
	public void handleEvent(Object eventObject, long dispatchTime, long sequenceId)
	{
		if (!(eventObject instanceof ClearAllCachesEvent))
		{
			return;
		}
		committedRootCache.clear();
		IRootCache secondLevelCache = secondLevelCacheManager.selectSecondLevelCache();
		if (committedRootCache != secondLevelCache)
		{
			secondLevelCache.clear();
		}
		IList<IWritableCache> firstLevelCaches = firstLevelCacheManager.selectFirstLevelCaches();
		for (int a = firstLevelCaches.size(); a-- > 0;)
		{
			IWritableCache firstLevelCache = firstLevelCaches.get(a);
			firstLevelCache.clear();
		}
	}
}