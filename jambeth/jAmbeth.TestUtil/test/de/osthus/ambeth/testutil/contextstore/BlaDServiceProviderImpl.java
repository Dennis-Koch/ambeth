package de.osthus.ambeth.testutil.contextstore;

import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class BlaDServiceProviderImpl implements BlaDServiceProvider
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	private BlaDServicePortType service;

	@Override
	public BlaDServicePortType getService()
	{
		return service;
	}

	public void setService(BlaDServicePortType service)
	{
		this.service = service;
	}
}
