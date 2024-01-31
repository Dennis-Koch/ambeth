package com.koch.ambeth.ioc;

/*-
 * #%L
 * jambeth-ioc
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
import com.koch.ambeth.ioc.factory.IBeanContextFactoryIntern;
import com.koch.ambeth.ioc.hierarchy.SearchType;
import com.koch.ambeth.ioc.link.ILinkContainer;

import java.util.List;

public interface IServiceContextIntern extends IServiceContext {
	void childContextDisposed(IServiceContext childContext);

	Object getDirectBean(String beanName);

	Object getDirectBean(Class<?> serviceType);

	<T> T getServiceIntern(Class<T> serviceType, SearchType searchType);

	<T> T getServiceIntern(String serviceName, Class<T> serviceType, SearchType searchType);

    IBeanContextFactoryIntern getBeanContextFactory();

    List<IBeanInstantiationProcessor> getInstantiationProcessors();

    IExternalServiceContext getExternalServiceContext();

    void addInstantiationProcessor(IBeanInstantiationProcessor bean);

    void addPreProcessor(IBeanPreProcessor bean);

    void addPostProcessor(IBeanPostProcessor bean);

    void addLinkContainer(ILinkContainer bean);

    List<IBeanPreProcessor> getPreProcessors();

    List<IBeanPostProcessor> getPostProcessors();

    IBeanConfiguration getBeanConfiguration(IBeanContextFactoryIntern beanContextFactory, String beanName);

    void addNamedBean(String beanName, Object bean);

    void addAutowiredBean(Class<?> autowireableType, Object bean);

    List<ILinkContainer> getLinkContainers();

    void setRunning();
}
