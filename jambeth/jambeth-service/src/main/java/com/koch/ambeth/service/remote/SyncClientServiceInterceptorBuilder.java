package com.koch.ambeth.service.remote;

/*-
 * #%L
 * jambeth-service
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.service.log.interceptor.LogInterceptor;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.proxy.IProxyFactory;
import com.koch.ambeth.util.proxy.MethodInterceptor;
import com.koch.ambeth.util.proxy.TargetingInterceptor;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;

public class SyncClientServiceInterceptorBuilder implements IClientServiceInterceptorBuilder {
	@Autowired
	protected IClientServiceFactory clientServiceFactory;

	@Autowired
	protected IProxyFactory proxyFactory;

	@Override
	public MethodInterceptor createInterceptor(IServiceContext sourceBeanContext,
											   Class<?> syncLocalInterface, Class<?> syncRemoteInterface, Class<?> asyncRemoteInterface) {
		ParamChecker.assertParamNotNull(sourceBeanContext, "sourceBeanContext");
		if (syncRemoteInterface == null) {
			syncRemoteInterface = syncLocalInterface;
		}
		final Class<?> clientProviderType =
				clientServiceFactory.getTargetProviderType(syncRemoteInterface);

		final String serviceName = clientServiceFactory.getServiceName(syncRemoteInterface);

		final String logInterceptorName = "logInterceptor";
		final String remoteTargetProviderName = "remoteTargetProvider";
		final String interceptorName = "interceptor";

		IServiceContext childContext =
				sourceBeanContext.createService(new IBackgroundWorkerParamDelegate<IBeanContextFactory>() {
					@Override
					public void invoke(IBeanContextFactory bcf) {
						if (IRemoteTargetProvider.class.isAssignableFrom(clientProviderType)) {
							bcf.registerBean(remoteTargetProviderName, clientProviderType)
									.propertyValue(IRemoteTargetProvider.SERVICE_NAME_PROP, serviceName);
							clientServiceFactory.postProcessTargetProviderBean(remoteTargetProviderName, bcf);

							bcf.registerBean(interceptorName, TargetingInterceptor.class)
									.propertyRef(TargetingInterceptor.TARGET_PROVIDER_PROP, remoteTargetProviderName);
						}
						else if (IRemoteInterceptor.class.isAssignableFrom(clientProviderType)) {
							bcf.registerBean(interceptorName, clientProviderType)
									.propertyValue(IRemoteTargetProvider.SERVICE_NAME_PROP, serviceName);
							clientServiceFactory.postProcessTargetProviderBean(interceptorName, bcf);
						}
						else {
							throw new IllegalStateException(
									"ProviderType '" + clientProviderType + "' is not supported here");
						}
						bcf.registerBean(logInterceptorName, LogInterceptor.class).propertyRef("Target",
								interceptorName);
					}
				});

		return childContext.getService(logInterceptorName, MethodInterceptor.class);
	}
}
