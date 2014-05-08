package de.osthus.ambeth.cache;

import de.osthus.ambeth.event.IEventListener;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.ParamChecker;

public class ServiceResultCacheClearEventListener implements IEventListener, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance(ServiceResultCacheClearEventListener.class)
	private ILogger log;

	protected IServiceResultCache serviceResultCache;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(serviceResultCache, "serviceResultCache");
	}

	public void setServiceResultCache(IServiceResultCache serviceResultCache)
	{
		this.serviceResultCache = serviceResultCache;
	}

	@Override
	public void handleEvent(Object eventObject, long dispatchTime, long sequenceId)
	{
		if (eventObject instanceof ClearAllCachesEvent)
		{
			serviceResultCache.invalidateAll();
		}
	}
}
