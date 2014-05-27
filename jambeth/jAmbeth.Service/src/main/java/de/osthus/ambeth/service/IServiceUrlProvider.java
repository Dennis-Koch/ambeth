package de.osthus.ambeth.service;

public interface IServiceUrlProvider
{
	String getServiceURL(Class<?> serviceInterface, String serviceName);

	boolean isOffline();

	void setOffline(boolean isOffline);

	void lockForRestart(boolean offlineAfterRestart);
}