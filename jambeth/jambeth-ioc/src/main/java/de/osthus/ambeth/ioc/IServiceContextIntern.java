package de.osthus.ambeth.ioc;

import de.osthus.ambeth.ioc.hierarchy.SearchType;

public interface IServiceContextIntern extends IServiceContext
{
	void childContextDisposed(IServiceContext childContext);

	Object getDirectBean(String beanName);

	Object getDirectBean(Class<?> serviceType);

	<T> T getServiceIntern(Class<T> serviceType, SearchType searchType);

	<T> T getServiceIntern(String serviceName, Class<T> serviceType, SearchType searchType);
}
