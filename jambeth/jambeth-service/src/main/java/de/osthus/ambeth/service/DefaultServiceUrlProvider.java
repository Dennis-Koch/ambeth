package de.osthus.ambeth.service;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.StringBuilderUtil;

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
