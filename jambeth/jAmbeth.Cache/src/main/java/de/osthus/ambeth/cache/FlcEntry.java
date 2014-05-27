package de.osthus.ambeth.cache;

import java.lang.ref.Reference;

public class FlcEntry
{
	protected final Reference<IWritableCache> firstLevelCacheR;

	protected final Reference<Thread> owningThreadR;

	protected final CacheFactoryDirective cacheFactoryDirective;

	public FlcEntry(CacheFactoryDirective cacheFactoryDirective, Reference<IWritableCache> firstLevelCacheR, Reference<Thread> owningThreadR)
	{
		this.cacheFactoryDirective = cacheFactoryDirective;
		this.firstLevelCacheR = firstLevelCacheR;
		this.owningThreadR = owningThreadR;
	}

	public CacheFactoryDirective getCacheFactoryDirective()
	{
		return cacheFactoryDirective;
	}

	public IWritableCache getFirstLevelCache()
	{
		return firstLevelCacheR != null ? firstLevelCacheR.get() : null;
	}

	public Thread getOwningThread()
	{
		return owningThreadR != null ? owningThreadR.get() : null;
	}

	public boolean isForeignThreadAware()
	{
		return owningThreadR == null;
	}

	public boolean isInterestedInThread(Thread thread)
	{
		Reference<Thread> owningThreadR = this.owningThreadR;
		if (owningThreadR == null)
		{
			return true;
		}
		Thread owningThread = owningThreadR.get();
		return thread.equals(owningThread);
	}
}
