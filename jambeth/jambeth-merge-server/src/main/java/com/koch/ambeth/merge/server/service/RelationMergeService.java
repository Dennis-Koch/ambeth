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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.cache.rootcachevalue.RootCacheValue;
import com.koch.ambeth.event.IEventListener;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.cache.AbstractCacheValue;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.event.IEntityMetaDataEvent;
import com.koch.ambeth.merge.incremental.IIncrementalMergeState;
import com.koch.ambeth.merge.metadata.IObjRefFactory;
import com.koch.ambeth.merge.metadata.IPreparedObjRefFactory;
import com.koch.ambeth.merge.model.CreateOrUpdateContainerBuild;
import com.koch.ambeth.merge.model.IChangeContainer;
import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.merge.model.IRelationUpdateItem;
import com.koch.ambeth.merge.model.RelationUpdateItemBuild;
import com.koch.ambeth.merge.server.change.ILinkChangeCommand;
import com.koch.ambeth.merge.server.change.ITableChange;
import com.koch.ambeth.merge.server.change.LinkChangeCommand;
import com.koch.ambeth.merge.server.change.LinkContainer;
import com.koch.ambeth.merge.server.change.LinkTableChange;
import com.koch.ambeth.merge.server.change.TableChange;
import com.koch.ambeth.merge.server.change.UpdateCommand;
import com.koch.ambeth.merge.transfer.AbstractChangeContainer;
import com.koch.ambeth.merge.transfer.DeleteContainer;
import com.koch.ambeth.merge.transfer.DirectObjRef;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.merge.transfer.UpdateContainer;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.merge.util.OptimisticLockUtil;
import com.koch.ambeth.persistence.IServiceUtil;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.IDirectedLink;
import com.koch.ambeth.persistence.api.IDirectedLinkMetaData;
import com.koch.ambeth.persistence.api.ITable;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.query.persistence.IDataCursor;
import com.koch.ambeth.query.persistence.IDataItem;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.query.persistence.IVersionItem;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.PrimitiveMember;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.collections.SmartCopyMap;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

public class RelationMergeService implements IRelationMergeService, IEventListener {
	public static class QueryEntry {
		public final IQuery<?> query;

		public final HashMap<String, ChildMember> map;

		public QueryEntry(IQuery<?> query, HashMap<String, ChildMember> map) {
			this.query = query;
			this.map = map;
		}
	}

	public static class ChildMember {
		public final Member member;

		public final Member identifierMember;

		public final int dataIndex;

		public final int idIndex;

		public ChildMember(int dataIndex, Member member, Member identifierMember, int idIndex) {
			this.dataIndex = dataIndex;
			this.member = member;
			this.identifierMember = identifierMember;
			this.idIndex = idIndex;
		}
	}

	public static final Set<CacheDirective> cacheValueAndReturnMissesSet =
			EnumSet.of(CacheDirective.CacheValueResult, CacheDirective.ReturnMisses);

	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IDatabase database;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IObjRefFactory objRefFactory;

	@Autowired
	protected IObjRefHelper oriHelper;

	@Autowired
	protected IPrefetchHelper prefetchHelper;

	@Autowired
	protected IQueryBuilderFactory queryBuilderFactory;

	@Autowired
	protected IServiceUtil serviceUtil;

	protected final SmartCopyMap<ParentChildQueryKey, QueryEntry> keyToParentChildQuery =
			new SmartCopyMap<>();

	@Override
	public void handleEvent(Object eventObject, long dispatchTime, long sequenceId) throws Exception {
		if (!(eventObject instanceof IEntityMetaDataEvent)
				&& !(eventObject instanceof ClearAllCachesEvent)) {
			return;
		}
		// meta data has changed so we clear all cached queries because they might have gone illegal now
		keyToParentChildQuery.clear();
	}

	@Override
	public ITableChange getTableChange(IMap<String, ITableChange> tableChangeMap,
			Object entityHandler, String entityHandlerName) {
		ITableChange tableChange = tableChangeMap.get(entityHandlerName);
		if (tableChange == null) {
			Class<? extends ITableChange> tableChangeType =
					entityHandler != null ? TableChange.class : LinkTableChange.class;
			tableChange = beanContext.registerBean(tableChangeType)//
					.propertyValue("EntityHandlerName", entityHandlerName)//
					.propertyValue("Table", entityHandler)//
					.finish();
			tableChangeMap.put(entityHandlerName, tableChange);
		}
		return tableChange;
	}

	@Override
	public void handleUpdateNotifications(Class<?> parentType, IRelationUpdateItem[] ruis,
			IMap<String, ITableChange> tableChangeMap) {
		if (ruis == null) {
			return;
		}
		IDatabase database = this.database.getCurrent();
		IEntityMetaData parentMetaData = entityMetaDataProvider.getMetaData(parentType);
		for (int i = ruis.length; i-- > 0;) {
			IRelationUpdateItem rui = ruis[i];
			if (rui == null) {
				continue;
			}
			Member relationMethod = parentMetaData.getMemberByName(rui.getMemberName());
			Class<?> childType = relationMethod.getElementType();
			if (!parentMetaData.isRelatingToThis(childType)) {
				continue;
			}

			ITableChange tableChange = null;

			IObjRef[] added = rui.getAddedORIs();
			if (added != null && added.length > 0) {
				ITable otherTable = database.getTableByType(added[0].getRealType());
				tableChange =
						getTableChange(tableChangeMap, otherTable, otherTable.getMetaData().getName());
				createUpdateNotifications(tableChange, Arrays.asList(added));
			}

			IObjRef[] removed = rui.getRemovedORIs();
			if (removed != null && removed.length > 0) {
				if (tableChange == null) {
					ITable otherTable = database.getTableByType(removed[0].getRealType());
					tableChange =
							getTableChange(tableChangeMap, otherTable, otherTable.getMetaData().getName());
				}
				createUpdateNotifications(tableChange, Arrays.asList(removed));
			}
		}
	}

	@Override
	public void handleUpdateNotifications(ILinkChangeCommand changeCommand,
			IMap<String, ITableChange> tableChangeMap) {
		IDirectedLink fromLink = changeCommand.getDirectedLink();
		IDirectedLink toLink = fromLink.getReverseLink();

		RelationMember member = toLink.getMetaData().getMember();
		if (member != null) {
			ITable table = toLink.getFromTable();
			ITableChange tableChange =
					getTableChange(tableChangeMap, table, table.getMetaData().getName());
			createUpdateNotifications(tableChange, changeCommand.getRefsToLink());
			createUpdateNotifications(tableChange, changeCommand.getRefsToUnlink());
		}
	}

	protected void createUpdateNotifications(ITableChange tableChange, List<IObjRef> references) {
		for (int i = references.size(); i-- > 0;) {
			IObjRef objRef = references.get(i);
			if (objRef instanceof IDirectObjRef) {
				// newly created entities can not have an update at the same time implied by updates from
				// foreign relations
				continue;
			}
			UpdateCommand command = new UpdateCommand(objRef);
			tableChange.addChangeCommand(command);
		}
	}

	@Override
	public IList<IChangeContainer> processCreateDependencies(IObjRef reference, ITable table,
			IRelationUpdateItem[] ruis,
			IMap<CheckForPreviousParentKey, IList<IObjRef>> previousParentToMovedOrisMap,
			HashSet<IObjRef> allAddedORIs, IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap,
			IRootCache rootCache) {
		return processInsertAndUpdateDependencies(reference, table, ruis, null,
				previousParentToMovedOrisMap, allAddedORIs, objRefToChangeContainerMap, rootCache);
	}

	@Override
	public IList<IChangeContainer> processUpdateDependencies(IObjRef reference, ITable table,
			IRelationUpdateItem[] ruis, IMap<IObjRef, RootCacheValue> toDeleteMap,
			IMap<CheckForPreviousParentKey, IList<IObjRef>> previousParentToMovedOrisMap,
			HashSet<IObjRef> allAddedORIs, IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap,
			IRootCache rootCache) {
		List<IDirectedLink> links = table.getLinks();
		if (links.isEmpty() || ruis == null || ruis.length == 0) {
			return EmptyList.getInstance();
		}
		return processInsertAndUpdateDependencies(reference, table, ruis, toDeleteMap,
				previousParentToMovedOrisMap, allAddedORIs, objRefToChangeContainerMap, rootCache);
	}

	protected IList<IChangeContainer> processInsertAndUpdateDependencies(IObjRef reference,
			ITable table, IRelationUpdateItem[] ruis, IMap<IObjRef, RootCacheValue> toDeleteMap,
			IMap<CheckForPreviousParentKey, IList<IObjRef>> previousParentToMovedOrisMap,
			HashSet<IObjRef> allAddedORIs, IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap,
			IRootCache rootCache) {
		if (ruis == null || ruis.length == 0) {
			return EmptyList.getInstance();
		}
		ArrayList<IChangeContainer> changeContainers = new ArrayList<>();

		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(reference.getRealType());
		for (int a = ruis.length; a-- > 0;) {
			IRelationUpdateItem rui = ruis[a];
			if (rui == null) {
				continue;
			}
			IDirectedLink link = table.getLinkByMemberName(rui.getMemberName());
			if (link == null) {
				throw new RuntimeException("No link found for member '" + rui.getMemberName()
						+ "' on entity '" + table.getMetaData().getEntityType() + "'");
			}
			IDirectedLinkMetaData linkMD = link.getMetaData();
			if (!linkMD.isPersistingLink()) {
				continue;
			}
			IObjRef[] removedORIs = rui.getRemovedORIs();
			IObjRef[] addedORIs = rui.getAddedORIs();
			LinkContainer linkContainer = null;
			LinkChangeCommand command = null;
			if (removedORIs != null && removedORIs.length > 0) {
				if (linkMD.isCascadeDelete()) {
					IList<Object> objectsToDelete =
							rootCache.getObjects(removedORIs, RelationMergeService.cacheValueAndReturnMissesSet);
					for (int b = objectsToDelete.size(); b-- > 0;) {
						Object objectToDelete = objectsToDelete.get(b);
						IObjRef removedORI = removedORIs[b];
						if (allAddedORIs.contains(removedORI)) {
							// Entity was not orphaned but moved
							continue;
						}
						if (objectToDelete == null) {
							throw new IllegalStateException("Entity could not be retrieved: " + removedORI);
						}
						IChangeContainer existingChangeContainer = objRefToChangeContainerMap.get(removedORI);
						if (existingChangeContainer instanceof DeleteContainer) {
							continue;
						}
						DeleteContainer cascadeDeleteContainer = new DeleteContainer();
						cascadeDeleteContainer.setReference(removedORI);
						changeContainers.add(cascadeDeleteContainer);

						toDeleteMap.put(removedORI, (RootCacheValue) objectToDelete);

						objRefToChangeContainerMap.put(removedORI, cascadeDeleteContainer);
					}
				}

				command = new LinkChangeCommand(reference, link);
				linkContainer = new LinkContainer();
				linkContainer.setReference(reference);
				linkContainer.setCommand(command);

				command.addRefsToUnlink(removedORIs);
			}
			if (addedORIs != null && addedORIs.length > 0) {
				if (!linkMD.getReverseLink().isStandaloneLink()) {
					IList<IObjRef> movedOris = null;
					for (IObjRef addedObjRef : addedORIs) {
						if (addedObjRef.getId() == null) {
							// this is a newly created entity which will never have a "previous parent"
							continue;
						}
						if (movedOris == null) {
							CheckForPreviousParentKey key =
									new CheckForPreviousParentKey(metaData.getEntityType(), rui.getMemberName());
							movedOris = previousParentToMovedOrisMap.get(key);
							if (movedOris == null) {
								movedOris = new ArrayList<>();
								previousParentToMovedOrisMap.put(key, movedOris);
							}
						}
						movedOris.add(addedObjRef);
					}
				}
				if (command == null) {
					command = new LinkChangeCommand(reference, link);
				}
				if (linkContainer == null) {
					linkContainer = new LinkContainer();
					linkContainer.setReference(reference);
					linkContainer.setCommand(command);
				}
				command.addRefsToLink(addedORIs);
			}
			if (linkContainer != null) {
				changeContainers.add(linkContainer);
				objRefToChangeContainerMap.put(linkContainer.getReference(), linkContainer);
			}
		}
		return changeContainers;
	}

	protected ILinkedMap<String, IList<Object>> buildPropertyNameToIdsMap(List<IObjRef> oris,
			Class<?> entityType) {
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
		LinkedHashMap<String, IList<Object>> propertyNameToIdsMap =
				new LinkedHashMap<>();

		// Check for all oris and map the ids to their corresponding member name
		for (int a = oris.size(); a-- > 0;) {
			IObjRef ori = oris.get(a);
			Object id = ori.getId();
			if (id == null) {
				continue;
			}
			Member idMember = metaData.getIdMemberByIdIndex(ori.getIdNameIndex());
			IList<Object> idsList = propertyNameToIdsMap.get(idMember.getName());
			if (idsList == null) {
				idsList = new ArrayList<>();
				propertyNameToIdsMap.put(idMember.getName(), idsList);
			}
			idsList.add(id);
		}
		return propertyNameToIdsMap;
	}

	protected IQuery<?> buildParentChildQuery(IEntityMetaData metaData, String selectingMemberName,
			ILinkedMap<String, IList<Object>> childMemberNameToIdsMap,
			IMap<String, ChildMember> childMemberNameToDataIndexMap) {
		if (childMemberNameToIdsMap.isEmpty()) {
			throw new IllegalArgumentException("Illegal map");
		}
		IList<String> childMemberNames = childMemberNameToIdsMap.keyList();
		ParentChildQueryKey key = new ParentChildQueryKey(metaData.getEntityType(), selectingMemberName,
				childMemberNames.toArray(String.class));
		QueryEntry queryEntry = keyToParentChildQuery.get(key);
		if (queryEntry != null) {
			childMemberNameToDataIndexMap.putAll(queryEntry.map);
			return queryEntry.query;
		}
		Member selectingMember = metaData.getMemberByName(selectingMemberName);
		IEntityMetaData selectingMetaData =
				entityMetaDataProvider.getMetaData(selectingMember.getElementType());
		IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
		IQueryBuilder<?> qb = queryBuilderFactory.create(metaData.getEntityType());
		IOperand operand = null;
		StringBuilder sb = objectCollector.create(StringBuilder.class);
		try {
			// Build IS IN clauses for each referred member name
			for (int a = 0, size = childMemberNames.size(); a < size; a++) {
				String childMemberName = childMemberNames.get(a);
				sb.setLength(0);
				String propertyName =
						sb.append(selectingMemberName).append('.').append(childMemberName).toString();
				IOperand prop = qb.property(propertyName);
				int propIndex = qb.select(prop);
				childMemberNameToDataIndexMap.put(propertyName,
						new ChildMember(propIndex, selectingMember,
								selectingMetaData.getMemberByName(childMemberName),
								selectingMetaData.getIdIndexByMemberName(childMemberName)));
				IOperand inOperator = qb.let(prop).isIn(qb.valueName(propertyName));
				if (operand == null) {
					operand = inOperator;
				}
				else {
					operand = qb.or(operand, inOperator);
				}
			}
			int propIndex = qb.selectProperty(metaData.getIdMember().getName());
			childMemberNameToDataIndexMap.put(metaData.getIdMember().getName(),
					new ChildMember(propIndex, metaData.getIdMember(), null, ObjRef.UNDEFINED_KEY_INDEX));
			PrimitiveMember versionMember = metaData.getVersionMember();
			if (versionMember != null) {
				int versionPropIndex = qb.selectProperty(versionMember.getName());
				childMemberNameToDataIndexMap.put(versionMember.getName(),
						new ChildMember(versionPropIndex, versionMember, null, ObjRef.UNDEFINED_KEY_INDEX));
			}
		}
		finally {
			objectCollector.dispose(sb);
		}
		IQuery<?> query = qb.build(operand);
		keyToParentChildQuery.put(key,
				new QueryEntry(query, new HashMap<>(childMemberNameToDataIndexMap)));
		return query;
	}

	protected IQuery<?> parameterizeParentChildQuery(IQuery<?> query, String selectingMemberName,
			ILinkedMap<String, IList<Object>> childMemberNameToIdsMap) {
		IThreadLocalObjectCollector objectCollector = this.objectCollector.getCurrent();
		StringBuilder sb = objectCollector.create(StringBuilder.class);
		try {
			// Parameterize query for each referred member name
			for (Entry<String, IList<Object>> entry : childMemberNameToIdsMap) {
				String childMemberName = entry.getKey();
				sb.setLength(0);
				String propertyName =
						sb.append(selectingMemberName).append('.').append(childMemberName).toString();
				query = query.param(propertyName, entry.getValue());
			}
			return query;
		}
		finally {
			objectCollector.dispose(sb);
		}
	}

	@Override
	public IList<IChangeContainer> checkForPreviousParent(IList<IObjRef> oris, Class<?> entityType,
			String memberName, IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap,
			IIncrementalMergeState incrementalState) {
		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
		Member member = metaData.getMemberByName(memberName);

		HashMap<String, ChildMember> childMemberNameToDataIndexMap = new HashMap<>();

		ArrayList<AbstractChangeContainer> relevantChangeContainers =
				new ArrayList<>();

		ILinkedMap<String, IList<Object>> childMemberNameToIdsMap =
				buildPropertyNameToIdsMap(oris, member.getElementType());
		IQuery<?> query = buildParentChildQuery(metaData, memberName, childMemberNameToIdsMap,
				childMemberNameToDataIndexMap);
		query = parameterizeParentChildQuery(query, memberName, childMemberNameToIdsMap);

		IList<IChangeContainer> changeContainers = null;
		IDataCursor cursor = query.retrieveAsData();
		try {
			int primaryIdIndex =
					childMemberNameToDataIndexMap.get(metaData.getIdMember().getName()).dataIndex;
			int versionIndex = metaData.getVersionMember() != null
					? childMemberNameToDataIndexMap.get(metaData.getVersionMember().getName()).dataIndex
					: -1;

			IPreparedObjRefFactory preparedObjRefFactory =
					objRefFactory.prepareObjRefFactory(metaData.getEntityType(), ObjRef.PRIMARY_KEY_INDEX);
			for (IDataItem item : cursor) {
				Object id = item.getValue(primaryIdIndex);
				Object version = versionIndex >= 0 ? item.getValue(versionIndex) : null;

				IObjRef objRef = preparedObjRefFactory.createObjRef(id, version);

				IChangeContainer changeContainer = objRefToChangeContainerMap.get(objRef);
				if (changeContainer != null) {
					// DELETE: we have nothing to do
					// UPDATE: our operation is redundant
					// CREATE: can never occur because we just selected the key from the persistence layer
					continue;
				}
				AbstractChangeContainer updateContainer = incrementalState != null
						? incrementalState.newUpdateContainer(objRef.getRealType())
						: new UpdateContainer();
				updateContainer.setReference(objRef);
				if (changeContainers == null) {
					changeContainers = new ArrayList<>();
				}
				changeContainers.add(updateContainer);
				objRefToChangeContainerMap.put(objRef, changeContainer);

				// do NOT write to the 'objRefToChangeContainerMap' because the current method can be
				// executed concurrently
			}
		}
		finally {
			cursor.dispose();
		}
		return changeContainers == null ? EmptyList.<IChangeContainer>getInstance() : changeContainers;
	}

	@Override
	public IList<IChangeContainer> processDeleteDependencies(IObjRef reference, ITable table,
			IMap<IObjRef, RootCacheValue> toDeleteMap,
			IMap<OutgoingRelationKey, IList<IObjRef>> outgoingRelationToReferenceMap,
			IMap<IncomingRelationKey, IList<IObjRef>> incomingRelationToReferenceMap,
			IMap<CheckForPreviousParentKey, IList<IObjRef>> previousParentToMovedOrisMap,
			HashSet<IObjRef> allAddedORIs, IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap,
			IRootCache rootCache) {
		List<IDirectedLink> links = table.getLinks();
		if (links.isEmpty()) {
			return EmptyList.getInstance();
		}
		ArrayList<IChangeContainer> changeContainers = new ArrayList<>();
		for (int i = links.size(); i-- > 0;) {
			IDirectedLink link = links.get(i);
			IDirectedLinkMetaData linkMD = link.getMetaData();
			IDirectedLinkMetaData reverseLinkMD = link.getReverseLink().getMetaData();

			boolean incomingHandled = false;
			if (reverseLinkMD.getMember() != null) {
				RelationMember member = reverseLinkMD.getMember();
				Class<?> entityType = reverseLinkMD.getFromTable().getEntityType();
				{
					CheckForPreviousParentKey key =
							new CheckForPreviousParentKey(entityType, member.getName());
					IList<IObjRef> movedOris = previousParentToMovedOrisMap.get(key);
					if (movedOris == null) {
						movedOris = new ArrayList<>();
						previousParentToMovedOrisMap.put(key, movedOris);
					}
					movedOris.add(reference);
				}
				IncomingRelationKey key = new IncomingRelationKey(reference.getIdNameIndex(), table, link);
				IList<IObjRef> movedOris = incomingRelationToReferenceMap.get(key);
				if (movedOris == null) {
					movedOris = new ArrayList<>();
					incomingRelationToReferenceMap.put(key, movedOris);
				}
				movedOris.add(reference);
				incomingHandled = true;
			}

			boolean cascadeDelete = linkMD.isCascadeDelete();
			boolean selfRelation = link.getToTable().equals(table);
			boolean removeRelations;
			if (selfRelation) {
				removeRelations = true;
			}
			else if (cascadeDelete) {
				removeRelations = linkMD.isStandaloneLink() && linkMD.getReverseLink().isStandaloneLink();
			}
			else {
				removeRelations = linkMD.isStandaloneLink();
			}
			if (!cascadeDelete && !removeRelations) {
				continue;
			}

			if (linkMD.getMember() != null) {
				OutgoingRelationKey key = new OutgoingRelationKey(reference.getIdNameIndex(), table, link);
				IList<IObjRef> movedOris = outgoingRelationToReferenceMap.get(key);
				if (movedOris == null) {
					movedOris = new ArrayList<>();
					outgoingRelationToReferenceMap.put(key, movedOris);
				}
				movedOris.add(reference);
			}
			if (incomingHandled) {
				continue;
			}
			Boolean becauseOfSelfRelation = null;
			if (linkMD.getReverseLink().getMember() != null) {
				becauseOfSelfRelation = Boolean.FALSE;
			}
			if (selfRelation && linkMD.getMember() != null) {
				becauseOfSelfRelation = Boolean.TRUE;
			}
			if (becauseOfSelfRelation != null) {
				IncomingRelationKey key = new IncomingRelationKey(reference.getIdNameIndex(), table, link);
				IList<IObjRef> movedOris = incomingRelationToReferenceMap.get(key);
				if (movedOris == null) {
					movedOris = new ArrayList<>();
					incomingRelationToReferenceMap.put(key, movedOris);
				}
				movedOris.add(reference);
			}
		}
		return changeContainers;
	}

	protected IList<IChangeContainer> handleOutgoingRelation(IList<IObjRef> references, byte idIndex2,
			IDirectedLink link, boolean cascadeDelete, boolean removeRelations,
			IMap<IObjRef, RootCacheValue> toDeleteMap, ISet<EntityLinkKey> alreadyHandled,
			ISet<RootCacheValue> alreadyPrefetched,
			IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap, IRootCache rootCache) {
		IObjRefFactory objRefFactory = this.objRefFactory;
		IDirectedLinkMetaData linkMD = link.getMetaData();

		IEntityMetaData metadata = entityMetaDataProvider.getMetaData(references.get(0).getRealType());
		RelationMember member = linkMD.getMember();
		int relationIndex = metadata.getIndexByRelation(member);

		ArrayList<IChangeContainer> changeContainers = new ArrayList<>();

		removeRelations &= linkMD.isNullable();

		byte idIndex = linkMD.getFromIdIndex();

		for (IObjRef reference : references) {
			RootCacheValue entity = toDeleteMap.get(reference);
			IObjRef[] relatedObjRefs = entity.get__ObjRefs(relationIndex);

			if (relatedObjRefs.length == 0) {
				continue;
			}
			IObjRef[] relatedObjRefsWithVersion = new IObjRef[relatedObjRefs.length];
			IList<Object> relatedEntities =
					rootCache.getObjects(relatedObjRefs, cacheValueAndReturnMissesSet);

			if (cascadeDelete) {
				for (int j = relatedObjRefs.length; j-- > 0;) {
					RootCacheValue relatedEntity = (RootCacheValue) relatedEntities.get(j);
					if (relatedEntity == null) {
						throw OptimisticLockUtil.throwDeleted(relatedObjRefs[j]);
					}
					IObjRef primaryObjRef =
							objRefFactory.createObjRef(relatedEntity, ObjRef.PRIMARY_KEY_INDEX);
					IObjRef objRef = idIndex != ObjRef.PRIMARY_KEY_INDEX
							? objRefFactory.createObjRef(relatedEntity, idIndex)
							: primaryObjRef;
					relatedObjRefsWithVersion[j] = objRef; // use the alternate id objref matching to the link
					if (!toDeleteMap.putIfNotExists(primaryObjRef, relatedEntity)
							|| (primaryObjRef != objRef && !toDeleteMap.putIfNotExists(objRef, relatedEntity))) {
						continue;
					}
					if (objRefToChangeContainerMap.get(primaryObjRef) instanceof DeleteContainer
							|| (primaryObjRef != objRef
									&& objRefToChangeContainerMap.get(objRef) instanceof DeleteContainer)) {
						// nothing to do
						continue;
					}
					DeleteContainer container = new DeleteContainer();
					container.setReference(objRef);
					changeContainers.add(container);
					objRefToChangeContainerMap.put(container.getReference(), container);
				}
			}
			else {
				for (int j = relatedObjRefs.length; j-- > 0;) {
					AbstractCacheValue relatedEntity = (AbstractCacheValue) relatedEntities.get(j);
					if (relatedEntity == null) {
						throw OptimisticLockUtil.throwDeleted(relatedObjRefs[j]);
					}
					IObjRef objRef = objRefFactory.createObjRef(relatedEntity, idIndex);
					relatedObjRefsWithVersion[j] = objRef; // use the alternate id objref matching to the link
				}
			}
			if (removeRelations) {
				IObjRef correctIndexReference = oriHelper.entityToObjRef(entity, idIndex);
				List<IObjRef> fromOris = Arrays.asList(correctIndexReference);
				addLinkChangeContainer(changeContainers, link, fromOris,
						new ArrayList<IObjRef>(relatedObjRefsWithVersion));
			}
		}

		return changeContainers;
	}

	@Override
	public IList<IChangeContainer> handleOutgoingRelation(IList<IObjRef> references, byte idIndex,
			ITable table, IDirectedLink link, IMap<IObjRef, RootCacheValue> toDeleteMap,
			ISet<EntityLinkKey> alreadyHandled, ISet<RootCacheValue> alreadyPrefetched,
			IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap, IRootCache rootCache) {
		IDirectedLinkMetaData linkMD = link.getMetaData();
		boolean cascadeDelete = linkMD.isCascadeDelete();
		boolean selfRelation = link.getToTable().equals(table);
		boolean removeRelations;
		if (selfRelation) {
			removeRelations = true;
		}
		else if (cascadeDelete) {
			removeRelations = linkMD.isStandaloneLink() && linkMD.getReverseLink().isStandaloneLink();
		}
		else {
			removeRelations = linkMD.isStandaloneLink();
		}
		if (!cascadeDelete && !removeRelations) {
			throw new IllegalStateException(
					"Must never happen, because the queueing map would not have been filled with this state");
		}
		return handleOutgoingRelation(references, idIndex, link, cascadeDelete, removeRelations,
				toDeleteMap, alreadyHandled, alreadyPrefetched, objRefToChangeContainerMap, rootCache);
	}

	@Override
	public IList<IChangeContainer> handleIncomingRelation(IList<IObjRef> references, byte idIndex,
			ITable table, IDirectedLink link, IMap<IObjRef, RootCacheValue> toDeleteMap,
			IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap, IRootCache rootCache,
			IIncrementalMergeState incrementalState) {
		IDirectedLinkMetaData linkMD = link.getMetaData();
		boolean cascadeDelete = linkMD.isCascadeDelete();
		boolean selfRelation = link.getToTable().equals(table);
		boolean removeRelations;
		if (selfRelation) {
			removeRelations = true;
		}
		else if (cascadeDelete) {
			removeRelations = linkMD.isStandaloneLink() && linkMD.getReverseLink().isStandaloneLink();
		}
		else {
			removeRelations = linkMD.isStandaloneLink();
		}
		Boolean becauseOfSelfRelation = null;
		if (linkMD.getReverseLink().getMember() != null) {
			becauseOfSelfRelation = Boolean.FALSE;
		}
		if (selfRelation && linkMD.getMember() != null) {
			becauseOfSelfRelation = Boolean.TRUE;
		}
		if (becauseOfSelfRelation == null) {
			throw new IllegalStateException(
					"Must never happen, because the queueing map would not have been filled with this state");
		}
		return handleIncomingRelation(references, idIndex, table, link, cascadeDelete, removeRelations,
				becauseOfSelfRelation, toDeleteMap, objRefToChangeContainerMap, rootCache,
				incrementalState);
	}

	protected IList<IChangeContainer> handleIncomingRelation(IList<IObjRef> references,
			byte srcIdIndex, ITable table, IDirectedLink link, boolean cascadeDelete,
			boolean removeRelations, boolean becauseOfSelfRelation,
			IMap<IObjRef, RootCacheValue> toDeleteMap,
			IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap, IRootCache rootCache,
			IIncrementalMergeState incrementalState) {
		IDirectedLinkMetaData linkMD = link.getMetaData();
		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		IObjRefHelper oriHelper = this.oriHelper;
		IEntityMetaData relatedMetaData =
				entityMetaDataProvider.getMetaData(link.getToTable().getMetaData().getEntityType());
		Class<?> relatedType = relatedMetaData.getEntityType();
		Member member =
				becauseOfSelfRelation ? linkMD.getMember() : linkMD.getReverseLink().getMember();
		removeRelations &=
				becauseOfSelfRelation ? linkMD.isNullable() : linkMD.getReverseLink().isNullable();

		HashMap<String, ChildMember> childMemberNameToDataIndexMap = new HashMap<>();

		ILinkedMap<String, IList<Object>> childMemberNameToIdsMap =
				buildPropertyNameToIdsMap(references, member.getElementType());
		IQuery<?> query = buildParentChildQuery(relatedMetaData, member.getName(),
				childMemberNameToIdsMap, childMemberNameToDataIndexMap);
		query = parameterizeParentChildQuery(query, member.getName(), childMemberNameToIdsMap);

		ArrayList<IObjRef> relatingRefs = new ArrayList<>();

		ArrayList<IChangeContainer> changeContainers = new ArrayList<>();

		if (cascadeDelete) {
			ArrayList<IObjRef> criteriaObjRefs = new ArrayList<>();
			IList<RootCacheValue> relatingEntities = retrieveCacheValues(query, relatedMetaData,
					childMemberNameToDataIndexMap, criteriaObjRefs, rootCache);
			byte idIndex = becauseOfSelfRelation ? linkMD.getToField().getIdIndex()
					: linkMD.getReverseLink().getToField().getIdIndex();
			for (int j = 0; j < relatingEntities.size(); j++) {
				RootCacheValue relatingEntity = relatingEntities.get(j);
				IObjRef criteriaObjRef = criteriaObjRefs.get(j);

				final IObjRef primaryObjRef =
						oriHelper.entityToObjRef(relatingEntity, idIndex, relatedMetaData);
				IObjRef relatingRef = idIndex != ObjRef.PRIMARY_KEY_INDEX
						? oriHelper.entityToObjRef(relatingEntity, idIndex, relatedMetaData)
						: primaryObjRef;
				relatingRefs.add(relatingRef);

				if (linkMD.getReverseLink().getMember() != null) {
					IChangeContainer changeContainer = objRefToChangeContainerMap.get(criteriaObjRef);
					if (changeContainer == null) {
						throw new IllegalStateException("Must never happen");
					}
					if (changeContainer instanceof CreateOrUpdateContainerBuild) {
						CreateOrUpdateContainerBuild createOrUpdate =
								(CreateOrUpdateContainerBuild) changeContainer;
						RelationUpdateItemBuild criteriaRui =
								createOrUpdate.ensureRelation(linkMD.getReverseLink().getMember().getName());
						criteriaRui.removeObjRef(primaryObjRef);
					}
				}
				if (!toDeleteMap.putIfNotExists(primaryObjRef, relatingEntity)
						|| (primaryObjRef != relatingRef
								&& !toDeleteMap.putIfNotExists(relatingRef, relatingEntity))) {
					continue;
				}
				if (objRefToChangeContainerMap.get(primaryObjRef) instanceof DeleteContainer
						|| (primaryObjRef != relatingRef
								&& objRefToChangeContainerMap.get(relatingRef) instanceof DeleteContainer)) {
					// nothing to do
					continue;
				}
				DeleteContainer container = new DeleteContainer();
				container.setReference(primaryObjRef);
				changeContainers.add(container);
				objRefToChangeContainerMap.put(container.getReference(), container);
			}
		}
		else {
			IVersionCursor cursor = query.retrieveAsVersions(false);
			try {
				IPreparedObjRefFactory preparedObjRefFactory =
						objRefFactory.prepareObjRefFactory(relatedType, ObjRef.PRIMARY_KEY_INDEX);
				for (IVersionItem versionItem : cursor) {
					IObjRef objRef =
							preparedObjRefFactory.createObjRef(versionItem.getId(), versionItem.getVersion());
					relatingRefs.add(objRef);

					IChangeContainer changeContainer = objRefToChangeContainerMap.get(objRef);
					if (changeContainer != null) {
						// DELETE: we have nothing to do
						// UPDATE: our operation is redundant
						// CREATE: can never occur because we just selected the key from the persistence layer
						continue;
					}
					AbstractChangeContainer updateContainer = incrementalState != null
							? incrementalState.newUpdateContainer(objRef.getRealType())
							: new UpdateContainer();
					updateContainer.setReference(objRef);
					changeContainers.add(updateContainer);
					objRefToChangeContainerMap.put(updateContainer.getReference(), updateContainer);
				}
			}
			finally {
				cursor.dispose();
			}
		}

		if (!relatingRefs.isEmpty())

		{
			IDirectedLink directedLink = becauseOfSelfRelation ? link : link.getReverseLink();
			addLinkChangeContainer(changeContainers, directedLink, relatingRefs, references);
		}
		return changeContainers;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	protected IList<RootCacheValue> retrieveCacheValues(IQuery<?> query, IEntityMetaData metaData,
			HashMap<String, ChildMember> childMemberNameToDataIndexMap, IList<IObjRef> criteriaObjRefs,
			IRootCache rootCache) {
		ArrayList<IObjRef> objRefs = new ArrayList<>();

		IDataCursor cursor = query.retrieveAsData();
		try {
			int primaryIdIndex =
					childMemberNameToDataIndexMap.get(metaData.getIdMember().getName()).dataIndex;
			int versionIndex = metaData.getVersionMember() != null
					? childMemberNameToDataIndexMap.get(metaData.getVersionMember().getName()).dataIndex
					: -1;

			int[] dataIndices =
					new int[childMemberNameToDataIndexMap.size() - (versionIndex != -1 ? 2 : 1)];
			IPreparedObjRefFactory[] dataIndexObjectRefFactories =
					new IPreparedObjRefFactory[dataIndices.length];
			int count = 0;
			for (Entry<String, ChildMember> entry : childMemberNameToDataIndexMap) {
				ChildMember childMember = entry.getValue();
				int dataIndex = childMember.dataIndex;
				if (dataIndex == primaryIdIndex || dataIndex == versionIndex) {
					continue;
				}
				dataIndices[count] = dataIndex;
				dataIndexObjectRefFactories[count] = objRefFactory
						.prepareObjRefFactory(childMember.member.getElementType(), childMember.idIndex);
				count++;
			}
			IPreparedObjRefFactory preparedObjRefFactory =
					objRefFactory.prepareObjRefFactory(metaData.getEntityType(), ObjRef.PRIMARY_KEY_INDEX);
			for (IDataItem item : cursor) {
				Object id = item.getValue(primaryIdIndex);
				Object version = versionIndex >= 0 ? item.getValue(versionIndex) : null;

				IObjRef objRef = preparedObjRefFactory.createObjRef(id, version);
				objRefs.add(objRef);

				for (int dataIndex : dataIndices) {
					Object criteriaId = item.getValue(dataIndex);
					if (criteriaId != null) {
						IObjRef criteriaObjRef =
								dataIndexObjectRefFactories[dataIndex].createObjRef(criteriaId, null);
						criteriaObjRefs.add(criteriaObjRef);
						break;
					}
				}
			}
		}
		finally {
			cursor.dispose();
		}
		IList<RootCacheValue> result =
				(IList) rootCache.getObjects(objRefs, cacheValueAndReturnMissesSet);

		return result;
	}

	protected void addLinkChangeContainer(IList<IChangeContainer> changeContainers,
			IDirectedLink link, List<IObjRef> fromOris, List<IObjRef> toOris) {
		IDirectedLink directedLink = link.getLink().getDirectedLink();
		if (!directedLink.equals(link)) {
			List<IObjRef> temp = fromOris;
			fromOris = toOris;
			toOris = temp;
		}

		for (int i = fromOris.size(); i-- > 0;) {
			IObjRef fromOri = fromOris.get(i);
			LinkChangeCommand command = new LinkChangeCommand(fromOri, directedLink);
			command.addRefsToUnlink(toOris);
			LinkContainer linkContainer = new LinkContainer();
			linkContainer.setReference(fromOri);
			linkContainer.setCommand(command);
			changeContainers.add(linkContainer);
		}
	}

	@Override
	public void checkForCorrectIdIndex(ILinkChangeCommand changeCommand,
			IMap<Byte, IList<IObjRef>> toChange) {
		byte idIndex = changeCommand.getDirectedLink().getMetaData().getToIdIndex();
		checkForCorrectIdIndex(changeCommand.getRefsToLink(), idIndex, toChange);
		checkForCorrectIdIndex(changeCommand.getRefsToUnlink(), idIndex, toChange);
	}

	protected void checkForCorrectIdIndex(List<IObjRef> objRefs, byte idIndex,
			IMap<Byte, IList<IObjRef>> toChange) {
		if (objRefs.isEmpty()) {
			return;
		}
		IList<IObjRef> toChangeList = toChange.get(idIndex);
		for (int i = objRefs.size(); i-- > 0;) {
			IObjRef objRef = objRefs.get(i);
			if (objRef.getIdNameIndex() == idIndex) {
				continue;
			}
			if (toChangeList == null) {
				toChangeList = new ArrayList<>();
				toChange.put(Byte.valueOf(idIndex), toChangeList);
			}
			IObjRef newOri;
			if (objRef instanceof IDirectObjRef) {
				newOri = new DirectObjRef(objRef.getRealType(), ((IDirectObjRef) objRef).getDirect());
			}
			else {
				newOri = new ObjRef(objRef.getRealType(), objRef.getIdNameIndex(), objRef.getId(),
						objRef.getVersion());
				objRefs.set(i, newOri);
			}
			toChangeList.add(newOri);
		}
	}
}
