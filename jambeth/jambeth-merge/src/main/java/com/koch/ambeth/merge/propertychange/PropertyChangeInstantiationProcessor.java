package com.koch.ambeth.merge.propertychange;

/*-
 * #%L
 * jambeth-merge
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

import com.koch.ambeth.ioc.IBeanInstantiationProcessor;
import com.koch.ambeth.ioc.ServiceContext;
import com.koch.ambeth.ioc.accessor.IAccessorTypeProvider;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.bytecode.IBytecodeEnhancer;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.BeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.annotation.PropertyChangeAspect;

public class PropertyChangeInstantiationProcessor implements IBeanInstantiationProcessor {
	public static interface CreateDelegate {
		Object create();
	}

	@LogInstance
	private ILogger log;

	@Autowired
	protected IAccessorTypeProvider accessorTypeProvider;

	@Override
	public Object instantiateBean(BeanContextFactory beanContextFactory, ServiceContext beanContext,
			IBeanConfiguration beanConfiguration, Class<?> beanType,
			List<IBeanConfiguration> beanConfHierarchy) {
		if (!beanType.isAnnotationPresent(PropertyChangeAspect.class)) {
			return null;
		}
		IBytecodeEnhancer bytecodeEnhancer = beanContext.getService(IBytecodeEnhancer.class);
		beanType = bytecodeEnhancer.getEnhancedType(beanType,
				PropertyChangeEnhancementHint.PropertyChangeEnhancementHint);
		return accessorTypeProvider.getConstructorType(CreateDelegate.class, beanType).create();
	}
}
