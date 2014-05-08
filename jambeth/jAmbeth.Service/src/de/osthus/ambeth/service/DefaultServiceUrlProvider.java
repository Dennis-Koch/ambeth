package de.osthus.ambeth.service;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.config.ServiceConfigurationConstants;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.StringBuilderUtil;

public class DefaultServiceUrlProvider implements IServiceUrlProvider, IInitializingBean, IOfflineListenerExtendable
{
	@SuppressWarnings("unused")
	@LogInstance(DefaultServiceUrlProvider.class)
	private ILogger log;

	protected String serviceBaseUrl;

	protected IThreadLocalObjectCollector objectCollector;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		ParamChecker.assertNotNull(objectCollector, "objectCollector");
	}

	public void setObjectCollector(IThreadLocalObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	@Property(name = ServiceConfigurationConstants.ServiceBaseUrl)
	public void setServiceBaseUrl(String serviceBaseUrl)
	{
		this.serviceBaseUrl = serviceBaseUrl;
	}

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
