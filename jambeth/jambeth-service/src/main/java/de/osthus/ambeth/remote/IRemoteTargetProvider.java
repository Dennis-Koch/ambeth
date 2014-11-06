package de.osthus.ambeth.remote;

import de.osthus.ambeth.proxy.ITargetProvider;

public interface IRemoteTargetProvider extends ITargetProvider
{
	String getServiceName();

	void setServiceName(String serviceName);
}
