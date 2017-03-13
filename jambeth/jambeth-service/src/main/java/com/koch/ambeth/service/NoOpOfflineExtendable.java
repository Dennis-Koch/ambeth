package com.koch.ambeth.service;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

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
