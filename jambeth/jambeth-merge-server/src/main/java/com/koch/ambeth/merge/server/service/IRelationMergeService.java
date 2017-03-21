package com.koch.ambeth.merge.server.service;

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

import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.cache.rootcachevalue.RootCacheValue;
import com.koch.ambeth.merge.incremental.IIncrementalMergeState;
import com.koch.ambeth.merge.model.IChangeContainer;
import com.koch.ambeth.merge.model.IRelationUpdateItem;
import com.koch.ambeth.merge.server.change.ILinkChangeCommand;
import com.koch.ambeth.merge.server.change.ITableChange;
import com.koch.ambeth.persistence.api.IDirectedLink;
import com.koch.ambeth.persistence.api.ITable;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.ISet;

public interface IRelationMergeService {
	ITableChange getTableChange(IMap<String, ITableChange> tableChangeMap, Object entityHandler,
			String entityHandlerName);

	IList<IChangeContainer> processCreateDependencies(IObjRef reference, ITable table,
			IRelationUpdateItem[] ruis,
			IMap<CheckForPreviousParentKey, IList<IObjRef>> previousParentToMovedOrisMap,
			HashSet<IObjRef> allAddedORIs, IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap,
			IRootCache rootCache);

	IList<IChangeContainer> processUpdateDependencies(IObjRef reference, ITable table,
			IRelationUpdateItem[] ruis, IMap<IObjRef, RootCacheValue> toDeleteMap,
			IMap<CheckForPreviousParentKey, IList<IObjRef>> previousParentToMovedOrisMap,
			HashSet<IObjRef> allAddedORIs, IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap,
			IRootCache rootCache);

	IList<IChangeContainer> processDeleteDependencies(IObjRef reference, ITable table,
			IMap<IObjRef, RootCacheValue> toDeleteMap,
			IMap<OutgoingRelationKey, IList<IObjRef>> outgoingRelationToReferenceMap,
			IMap<IncomingRelationKey, IList<IObjRef>> incomingRelationToReferenceMap,
			IMap<CheckForPreviousParentKey, IList<IObjRef>> previousParentToMovedOrisMap,
			HashSet<IObjRef> allAddedORIs, IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap,
			IRootCache rootCache);

	IList<IChangeContainer> checkForPreviousParent(IList<IObjRef> oris, Class<?> entityType,
			String memberName, IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap,
			IIncrementalMergeState incrementalState);

	IList<IChangeContainer> handleOutgoingRelation(IList<IObjRef> references, byte idIndex,
			ITable table, IDirectedLink link, IMap<IObjRef, RootCacheValue> toDeleteMap,
			ISet<EntityLinkKey> alreadyHandled, ISet<RootCacheValue> alreadyPrefetched,
			IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap, IRootCache rootCache);

	IList<IChangeContainer> handleIncomingRelation(IList<IObjRef> references, byte idIndex,
			ITable table, IDirectedLink link, IMap<IObjRef, RootCacheValue> toDeleteMap,
			IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap, IRootCache rootCache,
			IIncrementalMergeState incrementalState);

	void handleUpdateNotifications(Class<?> parentType, IRelationUpdateItem[] ruis,
			IMap<String, ITableChange> tableChangeMap);

	void handleUpdateNotifications(ILinkChangeCommand changeCommand,
			IMap<String, ITableChange> tableChangeMap);

	void checkForCorrectIdIndex(ILinkChangeCommand changeCommand,
			IMap<Byte, IList<IObjRef>> toChange);
}
