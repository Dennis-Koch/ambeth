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

import com.koch.ambeth.ioc.IFrameworkModule;
import com.koch.ambeth.ioc.annotation.Autowired;
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
import com.koch.ambeth.service.auth.AuthenticationHolder;
import com.koch.ambeth.service.auth.IAuthenticationHolder;
import com.koch.ambeth.service.cache.IServiceResultProcessorExtendable;
import com.koch.ambeth.service.cache.IServiceResultProcessorRegistry;
import com.koch.ambeth.service.cache.ServiceResultProcessorRegistry;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.log.LoggingPostProcessor;
import com.koch.ambeth.service.remote.ClientServiceBean;
import com.koch.ambeth.service.remote.IClientServiceFactory;
import com.koch.ambeth.service.remote.IClientServiceInterceptorBuilder;
import com.koch.ambeth.service.remote.SyncClientServiceInterceptorBuilder;
import com.koch.ambeth.service.typeinfo.TypeInfoProvider;
import com.koch.ambeth.service.typeinfo.TypeInfoProviderFactory;
import com.koch.ambeth.service.xml.IXmlTypeHelper;
import com.koch.ambeth.service.xml.XmlTypeHelper;
import com.koch.ambeth.util.typeinfo.ITypeInfoProvider;
import com.koch.ambeth.util.typeinfo.ITypeInfoProviderFactory;
import io.toolisticon.spiap.api.SpiService;

@SpiService(IFrameworkModule.class)
@FrameworkModule
public class ServiceModule implements IFrameworkModule {
    @Autowired(optional = true)
    protected IAuthenticationHolder authenticationHolder;

    @Property(name = ServiceConfigurationConstants.AuthenticationHolderType, mandatory = false)
    protected Class<?> authenticationHolderType;

    @Property(name = ServiceConfigurationConstants.NetworkClientMode, defaultValue = "false")
    protected boolean networkClientMode;

    @Property(name = ServiceConfigurationConstants.ProcessServiceBeanActive, defaultValue = "true")
    protected boolean isProcessServiceBeanActive;

    @Property(name = ServiceConfigurationConstants.OfflineModeSupported, defaultValue = "false")
    protected boolean offlineModeSupported;

    @Property(name = ServiceConfigurationConstants.ClientServiceFactoryType, mandatory = false)
    protected Class<? extends IClientServiceFactory> clientServiceFactoryType;

    @Property(name = ServiceConfigurationConstants.TypeInfoProviderType, mandatory = false)
    protected Class<?> typeInfoProviderType;

    @Property(name = ServiceConfigurationConstants.ServiceRemoteInterceptorType, mandatory = false)
    protected Class<? extends IClientServiceInterceptorBuilder> serviceRemoteInterceptorType;

    @Override
    public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
        if (authenticationHolder == null) {
            if (authenticationHolderType == null) {
                authenticationHolderType = AuthenticationHolder.class;
            }
            if (!Object.class.equals(authenticationHolderType)) {
                beanContextFactory.registerBean(authenticationHolderType).autowireable(IAuthenticationHolder.class);
            }
        }
        if (typeInfoProviderType == null) {
            typeInfoProviderType = TypeInfoProvider.class;
        }
        if (networkClientMode) {
            if (clientServiceFactoryType != null) {
                beanContextFactory.registerBean(clientServiceFactoryType).autowireable(IClientServiceFactory.class);
            }
            if (serviceRemoteInterceptorType == null) {
                serviceRemoteInterceptorType = SyncClientServiceInterceptorBuilder.class;
            }
            beanContextFactory.registerBean("clientServiceInterceptorBuilder", serviceRemoteInterceptorType).autowireable(IClientServiceInterceptorBuilder.class);

            if (!offlineModeSupported) {
                // Register default service url provider
                beanContextFactory.registerBean("serviceUrlProvider", DefaultServiceUrlProvider.class).autowireable(IServiceUrlProvider.class, IOfflineListenerExtendable.class);
            }
        } else if (!offlineModeSupported) {
            beanContextFactory.registerBean(NoOpOfflineExtendable.class).autowireable(IOfflineListenerExtendable.class);
        }
        beanContextFactory.registerBean(ServiceByNameProvider.class).propertyValue("ParentServiceByNameProvider", null).autowireable(IServiceByNameProvider.class, IServiceExtendable.class);

        beanContextFactory.registerBean("serviceResultProcessorRegistry", ServiceResultProcessorRegistry.class)
                          .autowireable(IServiceResultProcessorRegistry.class, IServiceResultProcessorExtendable.class);

        beanContextFactory.registerBean("typeInfoProvider", typeInfoProviderType).autowireable(ITypeInfoProvider.class);

        beanContextFactory.registerBean("typeInfoProviderFactory", TypeInfoProviderFactory.class)
                          .propertyValue("TypeInfoProviderType", typeInfoProviderType)
                          .autowireable(ITypeInfoProviderFactory.class);

        beanContextFactory.registerBean("loggingPostProcessor", LoggingPostProcessor.class);

        if (networkClientMode && isProcessServiceBeanActive) {
            beanContextFactory.registerBean("processService", ClientServiceBean.class).propertyValue(ClientServiceBean.INTERFACE_PROP_NAME, IProcessService.class).autowireable(IProcessService.class);
        } else {
            beanContextFactory.registerBean("processService", ProcessService.class).autowireable(IProcessService.class);
        }

        beanContextFactory.registerBean("xmlTypeHelper", XmlTypeHelper.class).autowireable(IXmlTypeHelper.class);
    }
}
