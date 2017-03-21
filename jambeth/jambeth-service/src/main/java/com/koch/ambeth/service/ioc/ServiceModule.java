package com.koch.ambeth.service.ioc;

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

import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.service.DefaultServiceUrlProvider;
import com.koch.ambeth.service.IOfflineListenerExtendable;
import com.koch.ambeth.service.IProcessService;
import com.koch.ambeth.service.IServiceByNameProvider;
import com.koch.ambeth.service.IServiceExtendable;
import com.koch.ambeth.service.IServiceUrlProvider;
import com.koch.ambeth.service.NoOpOfflineExtendable;
import com.koch.ambeth.service.ProcessService;
import com.koch.ambeth.service.ServiceByNameProvider;
import com.koch.ambeth.service.cache.IServiceResultProcessorExtendable;
import com.koch.ambeth.service.cache.IServiceResultProcessorRegistry;
import com.koch.ambeth.service.cache.ServiceResultProcessorRegistry;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.log.LoggingPostProcessor;
import com.koch.ambeth.service.typeinfo.TypeInfoProvider;
import com.koch.ambeth.service.typeinfo.TypeInfoProviderFactory;
import com.koch.ambeth.service.xml.IXmlTypeHelper;
import com.koch.ambeth.service.xml.XmlTypeHelper;
import com.koch.ambeth.util.typeinfo.ITypeInfoProvider;
import com.koch.ambeth.util.typeinfo.ITypeInfoProviderFactory;

@FrameworkModule
public class ServiceModule implements IInitializingModule {
	@Property(name = ServiceConfigurationConstants.NetworkClientMode, defaultValue = "false")
	protected boolean networkClientMode;

	@Property(name = ServiceConfigurationConstants.OfflineModeSupported, defaultValue = "false")
	protected boolean offlineModeSupported;

	@Property(name = ServiceConfigurationConstants.TypeInfoProviderType, mandatory = false)
	protected Class<?> typeInfoProviderType;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		if (typeInfoProviderType == null) {
			typeInfoProviderType = TypeInfoProvider.class;
		}
		if (networkClientMode) {
			// beanContextFactory.registerBean("serviceFactory", ServiceFactory.class);

			// beanContextFactory.registerBean<AsyncClientServiceInterceptorBuilder>("clientServiceInterceptorBuilder").autowireable<IClientServiceInterceptorBuilder>();
			// beanContextFactory.registerBean<SyncClientServiceInterceptorBuilder>"clientServiceInterceptorBuilder".autowireable<IClientServiceInterceptorBuilder>();

			if (!offlineModeSupported) {
				// Register default service url provider
				beanContextFactory.registerBean("serviceUrlProvider", DefaultServiceUrlProvider.class)
						.autowireable(IServiceUrlProvider.class, IOfflineListenerExtendable.class);
			}
		}
		else if (!offlineModeSupported) {
			beanContextFactory.registerBean(NoOpOfflineExtendable.class)
					.autowireable(IOfflineListenerExtendable.class);
		}
		beanContextFactory.registerBean("serviceByNameProvider", ServiceByNameProvider.class)
				.propertyValue("ParentServiceByNameProvider", null)
				.autowireable(IServiceByNameProvider.class, IServiceExtendable.class);

		beanContextFactory
				.registerBean("serviceResultProcessorRegistry", ServiceResultProcessorRegistry.class)
				.autowireable(IServiceResultProcessorRegistry.class,
						IServiceResultProcessorExtendable.class);

		beanContextFactory.registerBean("typeInfoProvider", typeInfoProviderType)
				.autowireable(ITypeInfoProvider.class);

		beanContextFactory.registerBean("typeInfoProviderFactory", TypeInfoProviderFactory.class)
				.propertyValue("TypeInfoProviderType", typeInfoProviderType)
				.autowireable(ITypeInfoProviderFactory.class);

		beanContextFactory.registerBean("loggingPostProcessor", LoggingPostProcessor.class);

		beanContextFactory.registerBean("processService", ProcessService.class)
				.autowireable(IProcessService.class);

		beanContextFactory.registerBean("xmlTypeHelper", XmlTypeHelper.class)
				.autowireable(IXmlTypeHelper.class);
	}
}
