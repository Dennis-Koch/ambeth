package com.koch.ambeth.service.rest;

/*-
 * #%L
 * jambeth-service-rest
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

import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.service.IOfflineListenerExtendable;
import com.koch.ambeth.service.remote.IClientServiceFactory;

public class RESTClientServiceFactory implements IClientServiceFactory {
    @Override
    public Class<?> getSyncInterceptorType(Class<?> clientInterface) {
        return null;
    }

    @Override
    public Class<?> getTargetProviderType(Class<?> clientInterface) {
        return RESTClientInterceptor.class;
    }

    @Override
    public void postProcessTargetProviderBean(String serviceName, IBeanConfiguration bean, IBeanContextFactory beanContextFactory) {
        beanContextFactory.link(bean.getName()).to(IOfflineListenerExtendable.class);
    }
}
