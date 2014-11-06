package de.osthus.ambeth.service;

import de.osthus.ambeth.ioc.factory.IBeanContextFactory;

public interface IClientServiceFactory
{
	Class<?> getTargetProviderType(Class<?> clientInterface);

	Class<?> getSyncInterceptorType(Class<?> clientInterface);

	String getServiceName(Class<?> clientInterface);

	void postProcessTargetProviderBean(String targetProviderBeanName, IBeanContextFactory beanContextFactory);
}