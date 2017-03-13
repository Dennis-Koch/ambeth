package com.koch.ambeth.service;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.util.StringBuilderUtil;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public class DefaultServiceUrlProvider implements IServiceUrlProvider, IOfflineListenerExtendable
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Property(name = ServiceConfigurationConstants.ServiceBaseUrl, defaultValue = "http://localhost:8000")
	protected String serviceBaseUrl;

	@Override
	public String getServiceURL(Class<?> serviceInterface, String serviceName)
	{
		return StringBuilderUtil.concat(objectCollector.getCurrent(), serviceBaseUrl, "/", serviceName, "/");
	}

	@Override
	public boolean isOffline()
	{
		return false;
	}

	@Override
	public void setOffline(boolean isOffline)
	{
		throw new UnsupportedOperationException("This " + IServiceUrlProvider.class.getSimpleName() + " does not support this operation");
	}

	@Override
	public void lockForRestart(boolean offlineAfterRestart)
	{
		throw new UnsupportedOperationException("This " + IServiceUrlProvider.class.getSimpleName() + " does not support this operation");
	}

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
