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

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.ServiceContext;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.log.config.Properties;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.collections.IdentityLinkedMap;
import com.koch.ambeth.util.collections.IdentityLinkedSet;

public class BeanContextInit {
	public Properties properties;

	public ServiceContext beanContext;

	public BeanContextFactory beanContextFactory;

	public IdentityLinkedMap<Object, IBeanConfiguration> objectToBeanConfigurationMap;

	public IdentityHashMap<Object, IBeanConfiguration> objectToHandledBeanConfigurationMap;

	public IdentityLinkedSet<Object> allLifeCycledBeansSet;

	public ArrayList<Object> initializedOrdering;

	public ArrayList<IDisposableBean> toDestroyOnError = new ArrayList<>();
}
