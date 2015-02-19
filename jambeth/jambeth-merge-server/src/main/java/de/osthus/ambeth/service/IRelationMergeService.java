package de.osthus.ambeth.service;

import de.osthus.ambeth.cache.IRootCache;
import de.osthus.ambeth.cache.rootcachevalue.RootCacheValue;
import de.osthus.ambeth.change.ILinkChangeCommand;
import de.osthus.ambeth.change.ITableChange;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.merge.incremental.IIncrementalMergeState;
import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IRelationUpdateItem;
import de.osthus.ambeth.persistence.IDirectedLink;
import de.osthus.ambeth.persistence.ITable;

public interface IRelationMergeService
{
	ITableChange getTableChange(IMap<String, ITableChange> tableChangeMap, Object entityHandler, String entityHandlerName);

	IList<IChangeContainer> processCreateDependencies(IObjRef reference, ITable table, IRelationUpdateItem[] ruis,
			IMap<CheckForPreviousParentKey, IList<IObjRef>> previousParentToMovedOrisMap, HashSet<IObjRef> allAddedORIs,
			IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap, IRootCache rootCache);

	IList<IChangeContainer> processUpdateDependencies(IObjRef reference, ITable table, IRelationUpdateItem[] ruis, IMap<IObjRef, RootCacheValue> toDeleteMap,
			IMap<CheckForPreviousParentKey, IList<IObjRef>> previousParentToMovedOrisMap, HashSet<IObjRef> allAddedORIs,
			IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap, IRootCache rootCache);

	IList<IChangeContainer> processDeleteDependencies(IObjRef reference, ITable table, IMap<IObjRef, RootCacheValue> toDeleteMap,
			IMap<OutgoingRelationKey, IList<IObjRef>> outgoingRelationToReferenceMap, IMap<IncomingRelationKey, IList<IObjRef>> incomingRelationToReferenceMap,
			IMap<CheckForPreviousParentKey, IList<IObjRef>> previousParentToMovedOrisMap, HashSet<IObjRef> allAddedORIs,
			IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap, IRootCache rootCache);

	IList<IChangeContainer> checkForPreviousParent(IList<IObjRef> oris, Class<?> entityType, String memberName,
			IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap, IIncrementalMergeState incrementalState);

	IList<IChangeContainer> handleOutgoingRelation(IList<IObjRef> references, byte idIndex, ITable table, IDirectedLink link,
			IMap<IObjRef, RootCacheValue> toDeleteMap, ISet<EntityLinkKey> alreadyHandled, ISet<RootCacheValue> alreadyPrefetched,
			IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap, IRootCache rootCache);

	IList<IChangeContainer> handleIncomingRelation(IList<IObjRef> references, byte idIndex, ITable table, IDirectedLink link,
			IMap<IObjRef, RootCacheValue> toDeleteMap, IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap, IRootCache rootCache,
			IIncrementalMergeState incrementalState);

	void handleUpdateNotifications(Class<?> parentType, IRelationUpdateItem[] ruis, IMap<String, ITableChange> tableChangeMap);

	void handleUpdateNotifications(ILinkChangeCommand changeCommand, IMap<String, ITableChange> tableChangeMap);

	void checkForCorrectIdIndex(ILinkChangeCommand changeCommand, IMap<Byte, IList<IObjRef>> toChange);
}
