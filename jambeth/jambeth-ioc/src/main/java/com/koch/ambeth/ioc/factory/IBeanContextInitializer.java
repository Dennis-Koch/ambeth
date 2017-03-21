package com.koch.ambeth.ioc.factory;

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

import java.util.List;

import com.koch.ambeth.ioc.ServiceContext;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.util.collections.IList;

public interface IBeanContextInitializer
{
	void initializeBeanContext(ServiceContext beanContext, BeanContextFactory beanContextFactory);

	Object initializeBean(ServiceContext beanContext, BeanContextFactory beanContextFactory, IBeanConfiguration beanConfiguration, Object bean,
			List<IBeanConfiguration> beanConfHierarchy, boolean joinLifecycle);

	IList<IBeanConfiguration> fillParentHierarchyIfValid(ServiceContext beanContext, BeanContextFactory beanContextFactory, IBeanConfiguration beanConfiguration);

	Class<?> resolveTypeInHierarchy(List<IBeanConfiguration> beanConfigurations);

	Object instantiateBean(ServiceContext beanContext, BeanContextFactory beanContextFactory, IBeanConfiguration beanConfiguration, Class<?> beanType,
			List<IBeanConfiguration> beanConfHierarchy);
}
