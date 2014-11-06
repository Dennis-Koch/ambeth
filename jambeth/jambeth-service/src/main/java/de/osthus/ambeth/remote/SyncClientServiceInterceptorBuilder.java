package de.osthus.ambeth.remote;

import net.sf.cglib.proxy.MethodInterceptor;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.RegisterPhaseDelegate;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.interceptor.LogInterceptor;
import de.osthus.ambeth.proxy.IProxyFactory;
import de.osthus.ambeth.proxy.TargetingInterceptor;
import de.osthus.ambeth.service.IClientServiceFactory;
import de.osthus.ambeth.util.ParamChecker;

public class SyncClientServiceInterceptorBuilder implements IClientServiceInterceptorBuilder
{
	@Autowired
	protected IClientServiceFactory clientServiceFactory;

	@Autowired
	protected IProxyFactory proxyFactory;

	@Override
	public MethodInterceptor createInterceptor(IServiceContext sourceBeanContext, Class<?> syncLocalInterface, Class<?> syncRemoteInterface,
			Class<?> asyncRemoteInterface)
	{
		ParamChecker.assertParamNotNull(sourceBeanContext, "sourceBeanContext");
		if (syncRemoteInterface == null)
		{
			syncRemoteInterface = syncLocalInterface;
		}
		final Class<?> clientProviderType = clientServiceFactory.getTargetProviderType(syncRemoteInterface);

		final String serviceName = clientServiceFactory.getServiceName(syncRemoteInterface);

		final String logInterceptorName = "logInterceptor";
		final String remoteTargetProviderName = "remoteTargetProvider";
		final String interceptorName = "interceptor";

		IServiceContext childContext = sourceBeanContext.createService(new RegisterPhaseDelegate()
		{
			@Override
			public void invoke(IBeanContextFactory bcf)
			{
				if (IRemoteTargetProvider.class.isAssignableFrom(clientProviderType))
				{
					bcf.registerBean(remoteTargetProviderName, clientProviderType).propertyValue("ServiceName", serviceName);
					clientServiceFactory.postProcessTargetProviderBean(remoteTargetProviderName, bcf);

					bcf.registerBean(interceptorName, TargetingInterceptor.class).propertyRef("TargetProvider", remoteTargetProviderName);
				}
				else if (IRemoteInterceptor.class.isAssignableFrom(clientProviderType))
				{
					bcf.registerBean(interceptorName, clientProviderType).propertyValue("ServiceName", serviceName);
					clientServiceFactory.postProcessTargetProviderBean(interceptorName, bcf);
				}
				else
				{
					throw new RuntimeException("ProviderType '" + clientProviderType + "' is not supported here");
				}
				bcf.registerBean(logInterceptorName, LogInterceptor.class).propertyRef("Target", interceptorName);
			}
		});

		return childContext.getService(logInterceptorName, MethodInterceptor.class);
	}
}
