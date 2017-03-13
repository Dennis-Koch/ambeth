package com.koch.ambeth.merge.server.ioc;

import com.koch.ambeth.cache.ioc.CacheModule;
import com.koch.ambeth.datachange.model.IDataChangeOfSession;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.event.IEntityMetaDataEvent;
import com.koch.ambeth.merge.server.DatabaseToEntityMetaData;
import com.koch.ambeth.merge.server.event.LocalToPublicDispatcher;
import com.koch.ambeth.merge.server.service.IRelationMergeService;
import com.koch.ambeth.merge.server.service.PersistenceMergeServiceExtension;
import com.koch.ambeth.merge.server.service.RelationMergeService;
import com.koch.ambeth.persistence.event.DatabaseAcquireEvent;
import com.koch.ambeth.persistence.event.DatabaseCommitEvent;
import com.koch.ambeth.persistence.event.DatabaseFailEvent;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;

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
		beanContextFactory.registerBean(DatabaseToEntityMetaData.class).propertyRef("PersistenceMergeServiceExtension", MERGE_SERVICE_SERVER)
				.propertyRef("PersistenceCacheRetriever", CacheModule.DEFAULT_CACHE_RETRIEVER);

		IBeanConfiguration relationMergeService = beanContextFactory.registerBean(RelationMergeService.class).autowireable(IRelationMergeService.class);
		beanContextFactory.link(relationMergeService).to(IEventListenerExtendable.class).with(IEntityMetaDataEvent.class);
		beanContextFactory.link(relationMergeService).to(IEventListenerExtendable.class).with(ClearAllCachesEvent.class);

		beanContextFactory.registerBean(MERGE_SERVICE_SERVER, PersistenceMergeServiceExtension.class);

		IBeanConfiguration localToPublicDispatcher = beanContextFactory.registerBean(LocalToPublicDispatcher.class);

		beanContextFactory.link(localToPublicDispatcher).to(IEventListenerExtendable.class).with(IDataChangeOfSession.class);
		beanContextFactory.link(localToPublicDispatcher).to(IEventListenerExtendable.class).with(DatabaseAcquireEvent.class);
		beanContextFactory.link(localToPublicDispatcher).to(IEventListenerExtendable.class).with(DatabaseCommitEvent.class);
		beanContextFactory.link(localToPublicDispatcher).to(IEventListenerExtendable.class).with(DatabaseFailEvent.class);
	}
}
