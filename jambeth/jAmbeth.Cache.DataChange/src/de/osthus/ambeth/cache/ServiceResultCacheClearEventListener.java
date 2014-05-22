package de.osthus.ambeth.cache;

import de.osthus.ambeth.event.IEventListener;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class ServiceResultCacheClearEventListener implements IEventListener
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceResultCache serviceResultCache;

	@Override
	public void handleEvent(Object eventObject, long dispatchTime, long sequenceId)
	{
		if (eventObject instanceof ClearAllCachesEvent)
		{
			serviceResultCache.invalidateAll();
		}
	}
}
