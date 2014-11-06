package de.osthus.ambeth.remote;

import net.sf.cglib.proxy.MethodInterceptor;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.RegisterPhaseDelegate;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.interceptor.LogInterceptor;
import de.osthus.ambeth.proxy.ProxyFactory;
import de.osthus.ambeth.proxy.TargetingInterceptor;
import de.osthus.ambeth.service.IClientServiceFactory;
import de.osthus.ambeth.util.ParamChecker;

public class AsyncClientServiceInterceptorBuilder implements IClientServiceInterceptorBuilder
{
	@Autowired
	protected IClientServiceFactory clientServiceFactory;

	@Autowired
	protected ProxyFactory proxyFactory;

	@Override
	public MethodInterceptor createInterceptor(IServiceContext sourceBeanContext, Class<?> syncLocalInterface, Class<?> syncRemoteInterface,
			Class<?> asyncRemoteInterface)
	{
		ParamChecker.assertParamNotNull(sourceBeanContext, "sourceBeanContext");
		Class<?> syncInterceptorType = null;
		if (syncRemoteInterface == null)
		{
			syncRemoteInterface = syncLocalInterface;
		}
		else
		{
			syncInterceptorType = clientServiceFactory.getSyncInterceptorType(syncRemoteInterface);
		}

		if (asyncRemoteInterface == null)
		{
			asyncRemoteInterface = syncRemoteInterface;
		}

		final Class<?> asyncRemoteInterfaceReadOnly = asyncRemoteInterface;

		final Class<?> clientProviderType = clientServiceFactory.getTargetProviderType(asyncRemoteInterface);

		final String serviceName = clientServiceFactory.getServiceName(syncRemoteInterface);

		String syncRemoteInterceptorName = "syncRemoteInterceptor";
		final String syncCallInterceptorName = "syncCallInterceptor";
		final String targetProviderName = "targetProvider";
		final String targetingInterceptorName = "targetingInterceptor";
		final String asyncProxyName = "asyncProxy";

		IServiceContext childContext = sourceBeanContext.createService(new RegisterPhaseDelegate()
		{
			@Override
			public void invoke(IBeanContextFactory bcf)
			{
				if (IRemoteTargetProvider.class.isAssignableFrom(clientProviderType))
				{
					bcf.registerBean(targetProviderName, clientProviderType).propertyValue("ServiceName", serviceName);
					clientServiceFactory.postProcessTargetProviderBean(targetProviderName, bcf);

					// TargetProvider and target have to be set up manually here
					bcf.registerBean(targetingInterceptorName, TargetingInterceptor.class).propertyRef("TargetProvider", targetProviderName);

					LogInterceptor logInterceptor = (LogInterceptor) bcf.registerBean("logInterceptor", LogInterceptor.class)
							.propertyRef("Target", targetingInterceptorName).getInstance();

					Object asyncProxy = proxyFactory.createProxy(asyncRemoteInterfaceReadOnly, logInterceptor);
					bcf.registerExternalBean(asyncProxyName, asyncProxy);

					// TODO port SyncCallInterceptor to Java
					// bcf.registerBean(syncCallInterceptorName, SyncCallInterceptor.class).propertyRef("AsyncService", asyncProxyName)
					// .propertyValue("AsyncServiceInterface", asyncRemoteInterfaceReadOnly);
					throw new RuntimeException("This functionality is not yet ported from .Net!");
				}
				else
				{
					throw new RuntimeException("ProviderType '" + clientProviderType + "' is not supported here");
				}
			}
		});

		return childContext.getService(syncRemoteInterceptorName, MethodInterceptor.class);
	}
}
