package de.osthus.ambeth.ioc;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.event.EntityMetaDataAddedEvent;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.extendable.ExtendableBean;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.mapping.IDedicatedMapperExtendable;
import de.osthus.ambeth.mapping.IDedicatedMapperRegistry;
import de.osthus.ambeth.mapping.IListTypeHelper;
import de.osthus.ambeth.mapping.IMapperServiceFactory;
import de.osthus.ambeth.mapping.ListTypeHelper;
import de.osthus.ambeth.mapping.MapperServiceFactory;
import de.osthus.ambeth.merge.config.ValueObjectConfigReader;
import de.osthus.ambeth.service.config.ConfigurationConstants;

@FrameworkModule
public class MappingModule implements IInitializingModule
{
	protected boolean genericTransferMapping;

	@Property(name = ConfigurationConstants.GenericTransferMapping, defaultValue = "false")
	public void setGenericTransferMapping(boolean genericTransferMapping)
	{
		this.genericTransferMapping = genericTransferMapping;
	}

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		if (genericTransferMapping)
		{
			beanContextFactory.registerBean("valueObjectConfigReader", ValueObjectConfigReader.class);
			beanContextFactory.link("valueObjectConfigReader").to(IEventListenerExtendable.class).with(EntityMetaDataAddedEvent.class);

			beanContextFactory.registerBean("listTypeHelper", ListTypeHelper.class).autowireable(IListTypeHelper.class);
			beanContextFactory.registerBean("mapperServiceFactory", MapperServiceFactory.class).autowireable(IMapperServiceFactory.class);

			beanContextFactory.registerBean("mapperExtensionRegistry", ExtendableBean.class)
					.autowireable(IDedicatedMapperExtendable.class, IDedicatedMapperRegistry.class)
					.propertyValue(ExtendableBean.P_EXTENDABLE_TYPE, IDedicatedMapperExtendable.class)
					.propertyValue(ExtendableBean.P_PROVIDER_TYPE, IDedicatedMapperRegistry.class).propertyValue("AllowMultiValue", true);
		}
	}
}
