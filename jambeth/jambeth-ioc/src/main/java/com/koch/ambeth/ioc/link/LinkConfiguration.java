package com.koch.ambeth.ioc.link;

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

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.config.BeanConfiguration;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.proxy.IProxyFactory;

public class LinkConfiguration<D> extends BeanConfiguration implements ILinkRegistryNeededConfiguration<D>, ILinkConfigWithOptional, ILinkConfigOptional
{
	public LinkConfiguration(Class<?> beanType, IProxyFactory proxyFactory, IProperties props)
	{
		super(beanType, null, proxyFactory, props);
	}

	@Override
	public ILinkConfigOptional with(Object... arguments)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_ARGUMENTS, arguments);
		return this;
	}

	@Override
	public void optional()
	{
		propertyValue(AbstractLinkContainer.PROPERTY_OPTIONAL, true);
	}

	@Override
	public ILinkConfigWithOptional to(Class<?> autowiredRegistryClass)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_TYPE, autowiredRegistryClass);
		return this;
	}

	@Override
	public ILinkConfigWithOptional to(Object registry, IEventDelegate<D> eventDelegate)
	{
		return to(registry, eventDelegate.getEventName());
	}

	@Override
	public ILinkConfigWithOptional to(Object registry, Class<?> registryClass)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY, registry);
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_TYPE, registryClass);
		return this;
	}

	@Override
	public ILinkConfigWithOptional to(Object registry, String propertyName)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY, registry);
		propertyValue(AbstractLinkContainer.PROPERTY_REGISTRY_PROPERTY_NAME, propertyName);
		return this;
	}

	@Override
	public ILinkRegistryNeededConfiguration<D> toContext(IServiceContext beanContext)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_FOREIGN_BEAN_CONTEXT, beanContext);
		return this;
	}

	@Override
	public ILinkRegistryNeededConfiguration<D> toContext(String nameOfBeanContext)
	{
		propertyValue(AbstractLinkContainer.PROPERTY_FOREIGN_BEAN_CONTEXT_NAME, nameOfBeanContext);
		return this;
	}
}
