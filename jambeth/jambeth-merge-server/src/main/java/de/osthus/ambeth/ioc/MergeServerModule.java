package de.osthus.ambeth.ioc;

import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.event.DatabaseAcquireEvent;
import de.osthus.ambeth.event.DatabaseCommitEvent;
import de.osthus.ambeth.event.DatabaseFailEvent;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.event.LocalToPublicDispatcher;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.EntityMetaDataServer;
import de.osthus.ambeth.merge.IEntityMetaDataExtendable;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IValueObjectConfigExtendable;
import de.osthus.ambeth.merge.ValueObjectMap;
import de.osthus.ambeth.merge.event.LocalDataChangeEvent;
import de.osthus.ambeth.service.IRelationMergeService;
import de.osthus.ambeth.service.PersistenceMergeServiceExtension;
import de.osthus.ambeth.service.RelationMergeService;
import de.osthus.ambeth.service.config.ConfigurationConstants;

@FrameworkModule
public class MergeServerModule implements IInitializingModule
{
	public static final String MERGE_SERVICE_SERVER = "mergeservice.server";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Property(name = ConfigurationConstants.GenericTransferMapping, defaultValue = "false")
	protected boolean genericTransferMapping;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		IBeanConfiguration valueObjectMap = beanContextFactory.registerAnonymousBean(ValueObjectMap.class);
		IBeanConfiguration beanConfig = beanContextFactory.registerBean("entityMetaDataProvider", EntityMetaDataServer.class)
				.propertyRef("ValueObjectMap", valueObjectMap).propertyRef("PersistenceMergeServiceExtension", MERGE_SERVICE_SERVER)
				.autowireable(IEntityMetaDataProvider.class, IEntityMetaDataExtendable.class);
		if (genericTransferMapping)
		{
			beanConfig.autowireable(IValueObjectConfigExtendable.class);
		}

		beanContextFactory.registerBean("relationMergeService", RelationMergeService.class).autowireable(IRelationMergeService.class);
		beanContextFactory.registerBean(MERGE_SERVICE_SERVER, PersistenceMergeServiceExtension.class);

		beanContextFactory.registerBean("localToPublicDispatcher", LocalToPublicDispatcher.class);

		beanContextFactory.link("localToPublicDispatcher").to(IEventListenerExtendable.class).with(LocalDataChangeEvent.class);
		beanContextFactory.link("localToPublicDispatcher").to(IEventListenerExtendable.class).with(DatabaseAcquireEvent.class);
		beanContextFactory.link("localToPublicDispatcher").to(IEventListenerExtendable.class).with(DatabaseCommitEvent.class);
		beanContextFactory.link("localToPublicDispatcher").to(IEventListenerExtendable.class).with(DatabaseFailEvent.class);
	}
}
