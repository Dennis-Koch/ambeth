package de.osthus.ambeth.rest;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.service.IClientServiceFactory;
import de.osthus.ambeth.service.IOfflineListenerExtendable;

public class RESTClientServiceFactory implements IClientServiceFactory, IInitializingBean
{
	@Override
	public void afterPropertiesSet()
	{
		// Intended blank
	}

	@Override
	public Class<?> getSyncInterceptorType(Class<?> clientInterface)
	{
		return null;
	}

	@Override
	public Class<?> getTargetProviderType(Class<?> clientInterface)
	{
		return RESTClientInterceptor.class;
	}

	@Override
	public String getServiceName(Class<?> clientInterface)
	{
		String name = clientInterface.getSimpleName();
		if (name.endsWith("Client"))
		{
			name = name.substring(0, name.length() - 6) + "Service";
		}
		else if (name.endsWith("WCF"))
		{
			name = name.substring(0, name.length() - 3);
		}
		if (name.startsWith("I"))
		{
			return name.substring(1);
		}
		return name;
	}

	@Override
	public void postProcessTargetProviderBean(String targetProviderBeanName, IBeanContextFactory beanContextFactory)
	{
		beanContextFactory.link(targetProviderBeanName).to(IOfflineListenerExtendable.class);
	}
}
