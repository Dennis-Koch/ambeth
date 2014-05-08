package de.osthus.ambeth.service;

public interface IServiceExtendable
{
	void registerService(Object service, String serviceName);

	void unregisterService(Object service, String serviceName);
}
