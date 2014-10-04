package de.osthus.ambeth.ioc;

import de.osthus.ambeth.cache.ClearAllCachesEvent;
import de.osthus.ambeth.datachange.model.IDataChangeOfSession;
import de.osthus.ambeth.event.DatabaseAcquireEvent;
import de.osthus.ambeth.event.DatabaseCommitEvent;
import de.osthus.ambeth.event.DatabaseFailEvent;
import de.osthus.ambeth.event.IEntityMetaDataEvent;
import de.osthus.ambeth.event.IEventListenerExtendable;
import de.osthus.ambeth.event.LocalToPublicDispatcher;
import de.osthus.ambeth.ioc.annotation.FrameworkModule;
import de.osthus.ambeth.ioc.config.IBeanConfiguration;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.DatabaseToEntityMetaData;
import de.osthus.ambeth.merge.IMergeListenerExtendable;
import de.osthus.ambeth.service.IRelationMergeService;
import de.osthus.ambeth.service.PersistenceMergeServiceExtension;
import de.osthus.ambeth.service.RelationMergeService;

@FrameworkModule
public class MergeServerModule implements IInitializingModule
{
	public static final String MERGE_SERVICE_SERVER = "mergeservice.server";

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
	{
		beanContextFactory.registerAnonymousBean(DatabaseToEntityMetaData.class).propertyRef("PersistenceMergeServiceExtension", MERGE_SERVICE_SERVER)
				.propertyRef("PersistenceCacheRetriever", CacheModule.DEFAULT_CACHE_RETRIEVER);

		IBeanConfiguration relationMergeService = beanContextFactory.registerAnonymousBean(RelationMergeService.class)
				.autowireable(IRelationMergeService.class);
		beanContextFactory.link(relationMergeService).to(IEventListenerExtendable.class).with(IEntityMetaDataEvent.class);
		beanContextFactory.link(relationMergeService).to(IEventListenerExtendable.class).with(ClearAllCachesEvent.class);

		beanContextFactory.registerBean(MERGE_SERVICE_SERVER, PersistenceMergeServiceExtension.class).autowireable(IMergeListenerExtendable.class);

		IBeanConfiguration localToPublicDispatcher = beanContextFactory.registerAnonymousBean(LocalToPublicDispatcher.class);

		beanContextFactory.link(localToPublicDispatcher).to(IEventListenerExtendable.class).with(IDataChangeOfSession.class);
		beanContextFactory.link(localToPublicDispatcher).to(IEventListenerExtendable.class).with(DatabaseAcquireEvent.class);
		beanContextFactory.link(localToPublicDispatcher).to(IEventListenerExtendable.class).with(DatabaseCommitEvent.class);
		beanContextFactory.link(localToPublicDispatcher).to(IEventListenerExtendable.class).with(DatabaseFailEvent.class);
	}
}
