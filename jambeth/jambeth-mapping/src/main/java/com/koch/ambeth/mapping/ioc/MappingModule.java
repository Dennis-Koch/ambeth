package com.koch.ambeth.mapping.ioc;

/*-
 * #%L
 * jambeth-mapping
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

import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.extendable.ExtendableBean;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.mapping.ExpansionEntityMapper;
import com.koch.ambeth.mapping.IDedicatedMapperExtendable;
import com.koch.ambeth.mapping.IDedicatedMapperRegistry;
import com.koch.ambeth.mapping.IListTypeHelper;
import com.koch.ambeth.mapping.IMapperServiceFactory;
import com.koch.ambeth.mapping.IPropertyExpansionExtendable;
import com.koch.ambeth.mapping.IPropertyExpansionProvider;
import com.koch.ambeth.mapping.ListTypeHelper;
import com.koch.ambeth.mapping.MapperServiceFactory;
import com.koch.ambeth.mapping.PropertyExpansionProvider;
import com.koch.ambeth.merge.config.ValueObjectConfigReader;
import com.koch.ambeth.merge.event.EntityMetaDataAddedEvent;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;

@FrameworkModule
public class MappingModule implements IInitializingModule {
	@Property(name = ServiceConfigurationConstants.GenericTransferMapping, defaultValue = "false")
	protected boolean genericTransferMapping;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		if (genericTransferMapping) {
			beanContextFactory.registerBean("valueObjectConfigReader", ValueObjectConfigReader.class);
			beanContextFactory.link("valueObjectConfigReader").to(IEventListenerExtendable.class)
					.with(EntityMetaDataAddedEvent.class);

			beanContextFactory.registerBean("listTypeHelper", ListTypeHelper.class)
					.autowireable(IListTypeHelper.class);
			beanContextFactory.registerBean("mapperServiceFactory", MapperServiceFactory.class)
					.autowireable(IMapperServiceFactory.class);

			IBeanConfiguration expansionEntityMapper =
					beanContextFactory.registerBean(ExpansionEntityMapper.class)
							.autowireable(IPropertyExpansionExtendable.class);
			beanContextFactory.link(expansionEntityMapper).to(IDedicatedMapperExtendable.class)
					.with(Object.class);

			ExtendableBean
					.registerExtendableBean(beanContextFactory, "mapperExtensionRegistry",
							IDedicatedMapperRegistry.class, IDedicatedMapperExtendable.class,
							IDedicatedMapperRegistry.class.getClassLoader())
					.propertyValue("AllowMultiValue", true);
		}

		beanContextFactory.registerBean(PropertyExpansionProvider.class)
				.autowireable(IPropertyExpansionProvider.class);
	}
}
