package de.osthus.ambeth.persistence;

import java.util.Set;

import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.util.IAlreadyLinkedCache;
import de.osthus.ambeth.util.IAlreadyLoadedCache;
import de.osthus.ambeth.util.IInterningFeature;
import de.osthus.ambeth.util.ParamChecker;

public class ContextProvider implements IContextProvider, IInitializingBean
{
	protected Long currentTime;

	protected String currentUser;

	protected IAlreadyLoadedCache cache;

	protected IAlreadyLinkedCache alreadyLinkedCache;

	protected IInterningFeature stringIntern;

	protected Set<Object> alreadyHandledSet;

	public ContextProvider()
	{
		alreadyHandledSet = new IdentityHashSet<Object>();
	}

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(cache, "Cache");
		ParamChecker.assertNotNull(alreadyLinkedCache, "AlreadyLinkedCache");
	}

	public void setCache(IAlreadyLoadedCache cache)
	{
		this.cache = cache;
	}

	@Override
	public Long getCurrentTime()
	{
		return currentTime;
	}

	@Override
	public void setCurrentTime(Long currentTime)
	{
		this.currentTime = currentTime;
	}

	@Override
	public String getCurrentUser()
	{
		return currentUser;
	}

	@Override
	public void setCurrentUser(String currentUser)
	{
		IInterningFeature stringIntern = this.stringIntern;
		if (stringIntern != null)
		{
			currentUser = stringIntern.intern(currentUser);
		}
		this.currentUser = currentUser;
	}

	public void setStringIntern(IInterningFeature stringIntern)
	{
		this.stringIntern = stringIntern;
	}

	@Override
	public IAlreadyLoadedCache getCache()
	{
		return cache;
	}

	@Override
	public Set<Object> getAlreadyHandledSet()
	{
		return alreadyHandledSet;
	}

	public void setAlreadyHandledSet(Set<Object> alreadyHandledSet)
	{
		this.alreadyHandledSet = alreadyHandledSet;
	}

	public void setAlreadyLinkedCache(IAlreadyLinkedCache alreadyLinkedCache)
	{
		this.alreadyLinkedCache = alreadyLinkedCache;
	}

	@Override
	public void clear()
	{
		cache.clear();
		alreadyLinkedCache.clear();
		alreadyHandledSet.clear();
	}
}
