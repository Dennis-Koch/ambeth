package de.osthus.ambeth.service;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.extendable.MapExtendableContainer;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.util.ParamChecker;

public class ServiceByNameProvider implements IServiceByNameProvider, IServiceExtendable, IInitializingBean
{
	@SuppressWarnings("unused")
	@LogInstance(ServiceByNameProvider.class)
	private ILogger log;

	protected MapExtendableContainer<String, Object> serviceNameToObjectMap;

	protected IServiceByNameProvider parentServiceByNameProvider;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		if (parentServiceByNameProvider != null)
		{
			ParamChecker.assertTrue(parentServiceByNameProvider != this, "parentServiceByNameProvider");
		}

		serviceNameToObjectMap = new MapExtendableContainer<String, Object>("serviceName", "service");
	}

	public void setParentServiceByNameProvider(IServiceByNameProvider parentServiceByNameProvider)
	{
		this.parentServiceByNameProvider = parentServiceByNameProvider;
	}

	@Override
	public void registerService(Object service, String serviceName)
	{
		serviceNameToObjectMap.register(service, serviceName);
	}

	@Override
	public void unregisterService(Object service, String serviceName)
	{
		serviceNameToObjectMap.unregister(service, serviceName);
	}

	@Override
	public Object getService(String serviceName)
	{
		Object service = serviceNameToObjectMap.getExtension(serviceName);
		if (service == null && parentServiceByNameProvider != null)
		{
			service = parentServiceByNameProvider.getService(serviceName);
		}
		return service;
	}
}
