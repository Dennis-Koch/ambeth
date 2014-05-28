package de.osthus.ambeth.service;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class NoOpOfflineExtendable implements IOfflineListenerExtendable
{
	@SuppressWarnings("unused")
	@LogInstance(NoOpOfflineExtendable.class)
	private ILogger log;

	@Override
	public void addOfflineListener(IOfflineListener offlineListener)
	{
		// Intended NoOp!
	}

	@Override
	public void removeOfflineListener(IOfflineListener offlineListener)
	{
		// Intended NoOp!
	}
}
