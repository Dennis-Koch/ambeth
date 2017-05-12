using De.Osthus.Ambeth.Ioc;
using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Util;
using System.Threading;

namespace De.Osthus.Ambeth.Cache
{
    public class FlcEntry
    {
	    protected readonly WeakReference firstLevelCacheR;

        protected readonly WeakReference owningThreadR;

        protected readonly CacheFactoryDirective cacheFactoryDirective;

        public FlcEntry(CacheFactoryDirective cacheFactoryDirective, WeakReference firstLevelCacheR, WeakReference owningThreadR)
	    {
		    this.cacheFactoryDirective = cacheFactoryDirective;
		    this.firstLevelCacheR = firstLevelCacheR;
		    this.owningThreadR = owningThreadR;
	    }

	    public CacheFactoryDirective GetCacheFactoryDirective()
	    {
		    return cacheFactoryDirective;
	    }

	    public IWritableCache GetFirstLevelCache()
	    {
		    return firstLevelCacheR != null ? (IWritableCache)firstLevelCacheR.Target : null;
	    }

	    public Thread GetOwningThread()
	    {
		    return owningThreadR != null ? (Thread)owningThreadR.Target : null;
	    }

        public bool IsForeignThreadAware()
        {
            return owningThreadR == null;
        }

	    public bool IsInterestedInThread(Thread thread)
	    {
		    WeakReference owningThreadR = this.owningThreadR;
		    if (owningThreadR == null)
		    {
			    return true;
		    }
		    Thread owningThread = (Thread)owningThreadR.Target;
		    return thread.Equals(owningThread);
	    }
    }
}
