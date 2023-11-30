package com.koch.ambeth.service.remote;

import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;

public class InMemoryClientServiceFactory implements IClientServiceFactory {
    @Override
    public Class<?> getTargetProviderType(Class<?> clientInterface) {
        return InMemoryClientServiceInterceptor.class;
    }

    @Override
    public Class<?> getSyncInterceptorType(Class<?> clientInterface) {
        return null;
    }

    @Override
    public void postProcessTargetProviderBean(String serviceName, IBeanConfiguration bean, IBeanContextFactory beanContextFactory) {
        // intended blank
    }
}
