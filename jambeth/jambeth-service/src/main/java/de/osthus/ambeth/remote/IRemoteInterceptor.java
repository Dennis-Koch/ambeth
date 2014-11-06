package de.osthus.ambeth.remote;

import net.sf.cglib.proxy.MethodInterceptor;

public interface IRemoteInterceptor extends MethodInterceptor
{
	String getServiceName();

	void setServiceName(String serviceName);
}