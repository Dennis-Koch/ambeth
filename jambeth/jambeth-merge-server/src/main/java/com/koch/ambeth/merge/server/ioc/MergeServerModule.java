package com.koch.ambeth.merge.server.ioc;

/*-
 * #%L
 * jambeth-merge-server
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

import com.koch.ambeth.cache.ioc.CacheModule;
import com.koch.ambeth.datachange.model.IDataChangeOfSession;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.ioc.IInitializingModule;
import com.koch.ambeth.ioc.annotation.FrameworkModule;
import com.koch.ambeth.ioc.config.IBeanConfiguration;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.IFimExtensionExtendable;
import com.koch.ambeth.merge.event.IEntityMetaDataEvent;
import com.koch.ambeth.merge.server.DatabaseFimExtension;
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
public class MergeServerModule implements IInitializingModule {
	public static final String MERGE_SERVICE_SERVER = "mergeservice.server";

	@Override
	public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
		beanContextFactory.registerBean(DatabaseToEntityMetaData.class)
				.propertyRef(DatabaseToEntityMetaData.P_PersistenceMergeServiceExtension,
						MERGE_SERVICE_SERVER)
				.propertyRef(DatabaseToEntityMetaData.P_PersistenceCacheRetriever,
						CacheModule.DEFAULT_CACHE_RETRIEVER);

		IBeanConfiguration databaseFimExtension = beanContextFactory
				.registerBean(DatabaseFimExtension.class);
		beanContextFactory.link(databaseFimExtension).to(IFimExtensionExtendable.class);

		IBeanConfiguration relationMergeService = beanContextFactory
				.registerBean(RelationMergeService.class).autowireable(IRelationMergeService.class);
		beanContextFactory.link(relationMergeService).to(IEventListenerExtendable.class)
				.with(IEntityMetaDataEvent.class);
		beanContextFactory.link(relationMergeService).to(IEventListenerExtendable.class)
				.with(ClearAllCachesEvent.class);

		beanContextFactory.registerBean(MERGE_SERVICE_SERVER, PersistenceMergeServiceExtension.class);

		IBeanConfiguration localToPublicDispatcher = beanContextFactory
				.registerBean(LocalToPublicDispatcher.class);

		beanContextFactory.link(localToPublicDispatcher).to(IEventListenerExtendable.class)
				.with(IDataChangeOfSession.class);
		beanContextFactory.link(localToPublicDispatcher).to(IEventListenerExtendable.class)
				.with(DatabaseAcquireEvent.class);
		beanContextFactory.link(localToPublicDispatcher).to(IEventListenerExtendable.class)
				.with(DatabaseCommitEvent.class);
		beanContextFactory.link(localToPublicDispatcher).to(IEventListenerExtendable.class)
				.with(DatabaseFailEvent.class);
	}
}
