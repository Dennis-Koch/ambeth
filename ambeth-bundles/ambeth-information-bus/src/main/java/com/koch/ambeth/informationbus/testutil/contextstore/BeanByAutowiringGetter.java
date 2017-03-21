package com.koch.ambeth.informationbus.testutil.contextstore;

/*-
 * #%L
 * jambeth-information-bus
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

public class BeanByAutowiringGetter implements IBeanGetter {
	private String contextName;

	private Class<?> beanType;

	public void setContextName(String contextName) {
		this.contextName = contextName;
	}

	public void setBeanType(Class<?> beanType) {
		this.beanType = beanType;
	}

	@Override
	public Object getBean(IServiceContextStore contextStore) {
		IServiceContext context = contextStore.getContext(contextName);
		if (context == null) {
			throw new IllegalStateException("Service context '" + contextName + "' not found");
		}
		Object bean = context.getService(beanType, true);
		return bean;
	}
}
