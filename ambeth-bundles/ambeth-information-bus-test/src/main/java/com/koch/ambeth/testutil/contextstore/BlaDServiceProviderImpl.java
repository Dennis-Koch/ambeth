package com.koch.ambeth.testutil.contextstore;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

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
