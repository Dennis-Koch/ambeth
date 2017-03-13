package com.koch.ambeth.mapping.ioc;

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
public class MappingModule implements IInitializingModule
{
	@Property(name = ServiceConfigurationConstants.GenericTransferMapping, defaultValue = "false")
	protected boolean genericTransferMapping;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		if (genericTransferMapping)
		{
			beanContextFactory.registerBean("valueObjectConfigReader", ValueObjectConfigReader.class);
			beanContextFactory.link("valueObjectConfigReader").to(IEventListenerExtendable.class).with(EntityMetaDataAddedEvent.class);

			beanContextFactory.registerBean("listTypeHelper", ListTypeHelper.class).autowireable(IListTypeHelper.class);
			beanContextFactory.registerBean("mapperServiceFactory", MapperServiceFactory.class).autowireable(IMapperServiceFactory.class);

			IBeanConfiguration expansionEntityMapper = beanContextFactory.registerBean(ExpansionEntityMapper.class).autowireable(
					IPropertyExpansionExtendable.class);
			beanContextFactory.link(expansionEntityMapper).to(IDedicatedMapperExtendable.class).with(Object.class);

			beanContextFactory.registerBean("mapperExtensionRegistry", ExtendableBean.class)
					.autowireable(IDedicatedMapperExtendable.class, IDedicatedMapperRegistry.class)
					.propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, IDedicatedMapperExtendable.class)
					.propertyValue(ExtendableBean.P_PROVIDER_TYPE, IDedicatedMapperRegistry.class).propertyValue("AllowMultiValue", true);
		}

		beanContextFactory.registerBean(PropertyExpansionProvider.class).autowireable(IPropertyExpansionProvider.class);
	}
}
