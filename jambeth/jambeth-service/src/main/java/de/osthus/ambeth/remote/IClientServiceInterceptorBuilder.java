package de.osthus.ambeth.remote;

import net.sf.cglib.proxy.MethodInterceptor;
import de.osthus.ambeth.ioc.IServiceContext;

public interface IClientServiceInterceptorBuilder
{
	MethodInterceptor createInterceptor(IServiceContext sourceBeanContext, Class<?> syncLocalInterface, Class<?> syncRemoteInterface,
			Class<?> asyncRemoteInterface);
}
