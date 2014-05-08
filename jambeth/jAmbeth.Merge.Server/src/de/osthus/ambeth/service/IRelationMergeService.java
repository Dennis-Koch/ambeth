package de.osthus.ambeth.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.osthus.ambeth.change.ILinkChangeCommand;
import de.osthus.ambeth.change.ITableChange;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IRelationUpdateItem;
import de.osthus.ambeth.persistence.IDirectedLink;
import de.osthus.ambeth.persistence.ITable;

public interface IRelationMergeService
{
	ITableChange getTableChange(IMap<String, ITableChange> tableChangeMap, Object entityHandler, String entityHandlerName);

	IList<IChangeContainer> processCreateDependencies(IObjRef reference, ITable table, IRelationUpdateItem[] ruis,
			Map<CheckForPreviousParentKey, IList<IObjRef>> previousParentToMovedOrisMap);

	IList<IChangeContainer> processUpdateDependencies(IObjRef reference, ITable table, IRelationUpdateItem[] ruis, Map<IObjRef, Object> toDeleteMap,
			Map<CheckForPreviousParentKey, IList<IObjRef>> previousParentToMovedOrisMap);

	IList<IChangeContainer> processDeleteDependencies(IObjRef reference, ITable table, Map<IObjRef, Object> toDeleteMap,
			Map<OutgoingRelationKey, IList<IObjRef>> outgoingRelationToReferenceMap, Map<IncomingRelationKey, IList<IObjRef>> incomingRelationToReferenceMap,
			Map<CheckForPreviousParentKey, IList<IObjRef>> previousParentToMovedOrisMap);

	IList<IChangeContainer> checkForPreviousParent(List<IObjRef> oris, Class<?> entityType, String memberName);

	IList<IChangeContainer> handleOutgoingRelation(List<IObjRef> references, byte idIndex, ITable table, IDirectedLink link, Map<IObjRef, Object> toDeleteMap,
			Set<EntityLinkKey> alreadyHandled, Set<Object> alreadyPrefetched);

	IList<IChangeContainer> handleIncomingRelation(List<IObjRef> references, byte idIndex, ITable table, IDirectedLink link, Map<IObjRef, Object> toDeleteMap);

	void handleUpdateNotifications(Class<?> parentType, IRelationUpdateItem[] ruis, IMap<String, ITableChange> tableChangeMap);

	void handleUpdateNotifications(ILinkChangeCommand changeCommand, IMap<String, ITableChange> tableChangeMap);

	void checkForCorrectIdIndex(ILinkChangeCommand changeCommand, IMap<Byte, IList<IObjRef>> toChange);
}
