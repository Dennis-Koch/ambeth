using System;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Util;
using System.Collections.Generic;
using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Cache
{
    public class RootCacheClearEventListener : IEventListener, IInitializingBean
    {
        [LogInstance]
	    public ILogger Log { private get; set; }

        public IFirstLevelCacheManager FirstLevelCacheManager { protected get; set; }

        public ISecondLevelCacheManager SecondLevelCacheManager { protected get; set; }

        public IRootCache CommittedRootCache { protected get; set; }

	    public void AfterPropertiesSet()
	    {
            ParamChecker.AssertNotNull(FirstLevelCacheManager, "FirstLevelCacheManager");
            ParamChecker.AssertNotNull(SecondLevelCacheManager, "SecondLevelCacheManager");
            ParamChecker.AssertNotNull(CommittedRootCache, "CommittedRootCache");
        }

	    public void HandleEvent(Object eventObject, DateTime dispatchTime, long sequenceId)
	    {
            if (!(eventObject is ClearAllCachesEvent))
		    {
			    return;
		    }
            CommittedRootCache.Clear();
            IRootCache privilegedSecondLevelCache = SecondLevelCacheManager.SelectPrivilegedSecondLevelCache(false);
            if (privilegedSecondLevelCache != null && !Object.ReferenceEquals(privilegedSecondLevelCache, CommittedRootCache))
            {
                privilegedSecondLevelCache.Clear();
            }
            IRootCache nonPrivilegedSecondLevelCache = SecondLevelCacheManager.SelectNonPrivilegedSecondLevelCache(false);
            if (nonPrivilegedSecondLevelCache != null && !Object.ReferenceEquals(nonPrivilegedSecondLevelCache, CommittedRootCache)
                    && !Object.ReferenceEquals(nonPrivilegedSecondLevelCache, privilegedSecondLevelCache))
            {
                nonPrivilegedSecondLevelCache.Clear();
            }
            IList<IWritableCache> firstLevelCaches = FirstLevelCacheManager.SelectFirstLevelCaches();
            for (int a = firstLevelCaches.Count; a-- > 0; )
            {
                IWritableCache firstLevelCache = firstLevelCaches[a];
                firstLevelCache.Clear();
            }
	    }
    }
}
