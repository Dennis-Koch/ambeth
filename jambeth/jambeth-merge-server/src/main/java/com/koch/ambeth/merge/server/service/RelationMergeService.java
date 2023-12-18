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
import com.koch.ambeth.persistence.api.ITable;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IQuery;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.service.cache.ClearAllCachesEvent;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.collections.SmartCopyMap;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class RelationMergeService implements IRelationMergeService, IEventListener {
    public static final Set<CacheDirective> cacheValueAndReturnMissesSet = EnumSet.of(CacheDirective.CacheValueResult, CacheDirective.ReturnMisses);
    protected final SmartCopyMap<ParentChildQueryKey, QueryEntry> keyToParentChildQuery = new SmartCopyMap<>();
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
    @LogInstance
    private ILogger log;

    @Override
    public void handleEvent(Object eventObject, long dispatchTime, long sequenceId) throws Exception {
        if (!(eventObject instanceof IEntityMetaDataEvent) && !(eventObject instanceof ClearAllCachesEvent)) {
            return;
        }
        // meta data has changed so we clear all cached queries because they might have gone illegal now
        keyToParentChildQuery.clear();
    }

    @Override
    public ITableChange getTableChange(IMap<String, ITableChange> tableChangeMap, Object entityHandler, String entityHandlerName) {
        var tableChange = tableChangeMap.get(entityHandlerName);
        if (tableChange == null) {
            var tableChangeType = entityHandler != null ? TableChange.class : LinkTableChange.class;
            tableChange = beanContext.registerBean(tableChangeType)//
                                     .propertyValue("EntityHandlerName", entityHandlerName)//
                                     .propertyValue("Table", entityHandler)//
                                     .finish();
            tableChangeMap.put(entityHandlerName, tableChange);
        }
        return tableChange;
    }

    @Override
    public void handleUpdateNotifications(Class<?> parentType, IRelationUpdateItem[] ruis, IMap<String, ITableChange> tableChangeMap) {
        if (ruis == null) {
            return;
        }
        var database = this.database.getCurrent();
        var parentMetaData = entityMetaDataProvider.getMetaData(parentType);
        for (int i = ruis.length; i-- > 0; ) {
            var rui = ruis[i];
            if (rui == null) {
                continue;
            }
            var relationMethod = parentMetaData.getMemberByName(rui.getMemberName());
            var childType = relationMethod.getElementType();
            if (!parentMetaData.isRelatingToThis(childType)) {
                continue;
            }

            ITableChange tableChange = null;

            var added = rui.getAddedORIs();
            if (added != null && added.length > 0) {
                var otherTable = database.getTableByType(added[0].getRealType());
                tableChange = getTableChange(tableChangeMap, otherTable, otherTable.getMetaData().getName());
                createUpdateNotifications(tableChange, Arrays.asList(added));
            }

            var removed = rui.getRemovedORIs();
            if (removed != null && removed.length > 0) {
                if (tableChange == null) {
                    var otherTable = database.getTableByType(removed[0].getRealType());
                    tableChange = getTableChange(tableChangeMap, otherTable, otherTable.getMetaData().getName());
                }
                createUpdateNotifications(tableChange, Arrays.asList(removed));
            }
        }
    }

    @Override
    public void handleUpdateNotifications(ILinkChangeCommand changeCommand, IMap<String, ITableChange> tableChangeMap) {
        var fromLink = changeCommand.getDirectedLink();
        var toLink = fromLink.getReverseLink();

        var member = toLink.getMetaData().getMember();
        if (member != null) {
            var table = toLink.getFromTable();
            var tableChange = getTableChange(tableChangeMap, table, table.getMetaData().getName());
            createUpdateNotifications(tableChange, changeCommand.getRefsToLink());
            createUpdateNotifications(tableChange, changeCommand.getRefsToUnlink());
        }
    }

    protected void createUpdateNotifications(ITableChange tableChange, List<IObjRef> references) {
        for (int i = references.size(); i-- > 0; ) {
            var objRef = references.get(i);
            if (objRef instanceof IDirectObjRef) {
                // newly created entities can not have an update at the same time implied by updates from
                // foreign relations
                continue;
            }
            var command = new UpdateCommand(objRef);
            tableChange.addChangeCommand(command);
        }
    }

    @Override
    public List<IChangeContainer> processCreateDependencies(IObjRef reference, ITable table, IRelationUpdateItem[] ruis, IMap<CheckForPreviousParentKey, List<IObjRef>> previousParentToMovedOrisMap,
            Set<IObjRef> allAddedORIs, IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap, IRootCache rootCache) {
        return processInsertAndUpdateDependencies(reference, table, ruis, null, previousParentToMovedOrisMap, allAddedORIs, objRefToChangeContainerMap, rootCache);
    }

    @Override
    public List<IChangeContainer> processUpdateDependencies(IObjRef reference, ITable table, IRelationUpdateItem[] ruis, IMap<IObjRef, RootCacheValue> toDeleteMap,
            IMap<CheckForPreviousParentKey, List<IObjRef>> previousParentToMovedOrisMap, Set<IObjRef> allAddedORIs, IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap, IRootCache rootCache) {
        var links = table.getLinks();
        if (links.isEmpty() || ruis == null || ruis.length == 0) {
            return EmptyList.getInstance();
        }
        return processInsertAndUpdateDependencies(reference, table, ruis, toDeleteMap, previousParentToMovedOrisMap, allAddedORIs, objRefToChangeContainerMap, rootCache);
    }

    protected List<IChangeContainer> processInsertAndUpdateDependencies(IObjRef reference, ITable table, IRelationUpdateItem[] ruis, IMap<IObjRef, RootCacheValue> toDeleteMap,
            IMap<CheckForPreviousParentKey, List<IObjRef>> previousParentToMovedOrisMap, Set<IObjRef> allAddedORIs, IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap, IRootCache rootCache) {
        if (ruis == null || ruis.length == 0) {
            return EmptyList.getInstance();
        }
        var changeContainers = new ArrayList<IChangeContainer>();

        var entityMetaDataProvider = this.entityMetaDataProvider;
        var metaData = entityMetaDataProvider.getMetaData(reference.getRealType());
        for (int a = ruis.length; a-- > 0; ) {
            IRelationUpdateItem rui = ruis[a];
            if (rui == null) {
                continue;
            }
            var link = table.getLinkByMemberName(rui.getMemberName());
            if (link == null) {
                throw new RuntimeException("No link found for member '" + rui.getMemberName() + "' on entity '" + table.getMetaData().getEntityType() + "'");
            }
            var linkMD = link.getMetaData();
            if (!linkMD.isPersistingLink()) {
                continue;
            }
            var removedORIs = rui.getRemovedORIs();
            var addedORIs = rui.getAddedORIs();
            LinkContainer linkContainer = null;
            LinkChangeCommand command = null;
            if (removedORIs != null && removedORIs.length > 0) {
                if (linkMD.isCascadeDelete()) {
                    var objectsToDelete = rootCache.getObjects(removedORIs, RelationMergeService.cacheValueAndReturnMissesSet);
                    for (int b = objectsToDelete.size(); b-- > 0; ) {
                        var objectToDelete = objectsToDelete.get(b);
                        var removedORI = removedORIs[b];
                        if (allAddedORIs.contains(removedORI)) {
                            // Entity was not orphaned but moved
                            continue;
                        }
                        if (objectToDelete == null) {
                            throw new IllegalStateException("Entity could not be retrieved: " + removedORI);
                        }
                        var existingChangeContainer = objRefToChangeContainerMap.get(removedORI);
                        if (existingChangeContainer instanceof DeleteContainer) {
                            continue;
                        }
                        var cascadeDeleteContainer = new DeleteContainer();
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
                    List<IObjRef> movedOris = null;
                    for (IObjRef addedObjRef : addedORIs) {
                        if (addedObjRef.getId() == null) {
                            // this is a newly created entity which will never have a "previous parent"
                            continue;
                        }
                        if (movedOris == null) {
                            CheckForPreviousParentKey key = new CheckForPreviousParentKey(metaData.getEntityType(), rui.getMemberName());
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

    protected ILinkedMap<String, List<Object>> buildPropertyNameToIdsMap(List<IObjRef> oris, Class<?> entityType) {
        var metaData = entityMetaDataProvider.getMetaData(entityType);
        var propertyNameToIdsMap = new LinkedHashMap<String, List<Object>>();

        // Check for all oris and map the ids to their corresponding member name
        for (int a = oris.size(); a-- > 0; ) {
            var ori = oris.get(a);
            var id = ori.getId();
            if (id == null) {
                continue;
            }
            var idMember = metaData.getIdMemberByIdIndex(ori.getIdNameIndex());
            var idsList = propertyNameToIdsMap.get(idMember.getName());
            if (idsList == null) {
                idsList = new ArrayList<>();
                propertyNameToIdsMap.put(idMember.getName(), idsList);
            }
            idsList.add(id);
        }
        return propertyNameToIdsMap;
    }

    protected IQuery<?> buildParentChildQuery(IEntityMetaData metaData, String selectingMemberName, ILinkedMap<String, List<Object>> childMemberNameToIdsMap,
            IMap<String, ChildMember> childMemberNameToDataIndexMap) {
        if (childMemberNameToIdsMap.isEmpty()) {
            throw new IllegalArgumentException("Illegal map");
        }
        var childMemberNames = childMemberNameToIdsMap.keyList();
        var key = new ParentChildQueryKey(metaData.getEntityType(), selectingMemberName, childMemberNames.toArray(String[]::new));
        var queryEntry = keyToParentChildQuery.get(key);
        if (queryEntry != null) {
            childMemberNameToDataIndexMap.putAll(queryEntry.map);
            return queryEntry.query;
        }
        var selectingMember = metaData.getMemberByName(selectingMemberName);
        var selectingMetaData = entityMetaDataProvider.getMetaData(selectingMember.getElementType());
        var objectCollector = this.objectCollector.getCurrent();
        var qb = queryBuilderFactory.create(metaData.getEntityType());
        IOperand operand = null;
        var sb = objectCollector.create(StringBuilder.class);
        try {
            // Build IS IN clauses for each referred member name
            for (int a = 0, size = childMemberNames.size(); a < size; a++) {
                var childMemberName = childMemberNames.get(a);
                sb.setLength(0);
                var propertyName = sb.append(selectingMemberName).append('.').append(childMemberName).toString();
                var prop = qb.property(propertyName);
                var propIndex = qb.select(prop);
                childMemberNameToDataIndexMap.put(propertyName,
                        new ChildMember(propIndex, selectingMember, selectingMetaData.getMemberByName(childMemberName), selectingMetaData.getIdIndexByMemberName(childMemberName)));
                var inOperator = qb.let(prop).isIn(qb.parameterValue(propertyName));
                if (operand == null) {
                    operand = inOperator;
                } else {
                    operand = qb.or(operand, inOperator);
                }
            }
            var propIndex = qb.selectProperty(metaData.getIdMember().getName());
            childMemberNameToDataIndexMap.put(metaData.getIdMember().getName(), new ChildMember(propIndex, metaData.getIdMember(), null, ObjRef.UNDEFINED_KEY_INDEX));
            var versionMember = metaData.getVersionMember();
            if (versionMember != null) {
                var versionPropIndex = qb.selectProperty(versionMember.getName());
                childMemberNameToDataIndexMap.put(versionMember.getName(), new ChildMember(versionPropIndex, versionMember, null, ObjRef.UNDEFINED_KEY_INDEX));
            }
        } finally {
            objectCollector.dispose(sb);
        }
        var query = qb.build(operand);
        keyToParentChildQuery.put(key, new QueryEntry(query, new HashMap<>(childMemberNameToDataIndexMap)));
        return query;
    }

    protected IQuery<?> parameterizeParentChildQuery(IQuery<?> query, String selectingMemberName, ILinkedMap<String, List<Object>> childMemberNameToIdsMap) {
        var objectCollector = this.objectCollector.getCurrent();
        var sb = objectCollector.create(StringBuilder.class);
        try {
            // Parameterize query for each referred member name
            for (var entry : childMemberNameToIdsMap) {
                var childMemberName = entry.getKey();
                sb.setLength(0);
                var propertyName = sb.append(selectingMemberName).append('.').append(childMemberName).toString();
                query = query.param(propertyName, entry.getValue());
            }
            return query;
        } finally {
            objectCollector.dispose(sb);
        }
    }

    @Override
    public List<IChangeContainer> checkForPreviousParent(List<IObjRef> oris, Class<?> entityType, String memberName, IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap,
            IIncrementalMergeState incrementalState) {
        var entityMetaDataProvider = this.entityMetaDataProvider;
        var metaData = entityMetaDataProvider.getMetaData(entityType);
        var member = metaData.getMemberByName(memberName);

        var childMemberNameToDataIndexMap = new HashMap<String, ChildMember>();

        var relevantChangeContainers = new ArrayList<AbstractChangeContainer>();

        var childMemberNameToIdsMap = buildPropertyNameToIdsMap(oris, member.getElementType());
        var query = buildParentChildQuery(metaData, memberName, childMemberNameToIdsMap, childMemberNameToDataIndexMap);
        query = parameterizeParentChildQuery(query, memberName, childMemberNameToIdsMap);

        List<IChangeContainer> changeContainers = null;
        var cursor = query.retrieveAsData();
        try {
            var primaryIdIndex = childMemberNameToDataIndexMap.get(metaData.getIdMember().getName()).dataIndex;
            var versionIndex = metaData.getVersionMember() != null ? childMemberNameToDataIndexMap.get(metaData.getVersionMember().getName()).dataIndex : -1;

            var preparedObjRefFactory = objRefFactory.prepareObjRefFactory(metaData.getEntityType(), ObjRef.PRIMARY_KEY_INDEX);
            for (var item : cursor) {
                var id = item.getValue(primaryIdIndex);
                var version = versionIndex >= 0 ? item.getValue(versionIndex) : null;

                var objRef = preparedObjRefFactory.createObjRef(id, version);

                var changeContainer = objRefToChangeContainerMap.get(objRef);
                if (changeContainer != null) {
                    // DELETE: we have nothing to do
                    // UPDATE: our operation is redundant
                    // CREATE: can never occur because we just selected the key from the persistence layer
                    continue;
                }
                var updateContainer = incrementalState != null ? incrementalState.newUpdateContainer(objRef.getRealType()) : new UpdateContainer();
                updateContainer.setReference(objRef);
                if (changeContainers == null) {
                    changeContainers = new ArrayList<>();
                }
                changeContainers.add(updateContainer);
                objRefToChangeContainerMap.put(objRef, changeContainer);

                // do NOT write to the 'objRefToChangeContainerMap' because the current method can be
                // executed concurrently
            }
        } finally {
            cursor.dispose();
        }
        return changeContainers == null ? EmptyList.<IChangeContainer>getInstance() : changeContainers;
    }

    @Override
    public List<IChangeContainer> processDeleteDependencies(IObjRef reference, ITable table, IMap<IObjRef, RootCacheValue> toDeleteMap,
            IMap<OutgoingRelationKey, List<IObjRef>> outgoingRelationToReferenceMap, IMap<IncomingRelationKey, List<IObjRef>> incomingRelationToReferenceMap,
            IMap<CheckForPreviousParentKey, List<IObjRef>> previousParentToMovedOrisMap, Set<IObjRef> allAddedORIs, IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap, IRootCache rootCache) {
        var links = table.getLinks();
        if (links.isEmpty()) {
            return EmptyList.getInstance();
        }
        var changeContainers = new ArrayList<IChangeContainer>();
        for (int i = links.size(); i-- > 0; ) {
            var link = links.get(i);
            var linkMD = link.getMetaData();
            var reverseLinkMD = link.getReverseLink().getMetaData();

            boolean incomingHandled = false;
            if (reverseLinkMD.getMember() != null) {
                var member = reverseLinkMD.getMember();
                var entityType = reverseLinkMD.getFromTable().getEntityType();
                {
                    var key = new CheckForPreviousParentKey(entityType, member.getName());
                    var movedOris = previousParentToMovedOrisMap.get(key);
                    if (movedOris == null) {
                        movedOris = new ArrayList<>();
                        previousParentToMovedOrisMap.put(key, movedOris);
                    }
                    movedOris.add(reference);
                }
                var key = new IncomingRelationKey(reference.getIdNameIndex(), table, link);
                var movedOris = incomingRelationToReferenceMap.get(key);
                if (movedOris == null) {
                    movedOris = new ArrayList<>();
                    incomingRelationToReferenceMap.put(key, movedOris);
                }
                movedOris.add(reference);
                incomingHandled = true;
            }

            var cascadeDelete = linkMD.isCascadeDelete();
            var selfRelation = link.getToTable().equals(table);
            boolean removeRelations;
            if (selfRelation) {
                removeRelations = true;
            } else if (cascadeDelete) {
                removeRelations = linkMD.isStandaloneLink() && linkMD.getReverseLink().isStandaloneLink();
            } else {
                removeRelations = linkMD.isStandaloneLink();
            }
            if (!cascadeDelete && !removeRelations) {
                continue;
            }

            if (linkMD.getMember() != null) {
                var key = new OutgoingRelationKey(reference.getIdNameIndex(), table, link);
                var movedOris = outgoingRelationToReferenceMap.get(key);
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
                var key = new IncomingRelationKey(reference.getIdNameIndex(), table, link);
                var movedOris = incomingRelationToReferenceMap.get(key);
                if (movedOris == null) {
                    movedOris = new ArrayList<>();
                    incomingRelationToReferenceMap.put(key, movedOris);
                }
                movedOris.add(reference);
            }
        }
        return changeContainers;
    }

    protected List<IChangeContainer> handleOutgoingRelation(List<IObjRef> references, byte idIndex2, IDirectedLink link, boolean cascadeDelete, boolean removeRelations,
            IMap<IObjRef, RootCacheValue> toDeleteMap, ISet<EntityLinkKey> alreadyHandled, ISet<RootCacheValue> alreadyPrefetched, IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap,
            IRootCache rootCache) {
        var objRefFactory = this.objRefFactory;
        var linkMD = link.getMetaData();

        var metadata = entityMetaDataProvider.getMetaData(references.get(0).getRealType());
        var member = linkMD.getMember();
        var relationIndex = metadata.getIndexByRelation(member);

        var changeContainers = new ArrayList<IChangeContainer>();

        removeRelations &= linkMD.isNullable();

        var idIndex = linkMD.getFromIdIndex();

        for (var reference : references) {
            var entity = toDeleteMap.get(reference);
            var relatedObjRefs = entity.get__ObjRefs(relationIndex);

            if (relatedObjRefs.length == 0) {
                continue;
            }
            var relatedObjRefsWithVersion = new IObjRef[relatedObjRefs.length];
            var relatedEntities = rootCache.getObjects(relatedObjRefs, cacheValueAndReturnMissesSet);

            if (cascadeDelete) {
                for (int j = relatedObjRefs.length; j-- > 0; ) {
                    var relatedEntity = (RootCacheValue) relatedEntities.get(j);
                    if (relatedEntity == null) {
                        throw OptimisticLockUtil.throwDeleted(relatedObjRefs[j]);
                    }
                    var primaryObjRef = objRefFactory.createObjRef(relatedEntity, ObjRef.PRIMARY_KEY_INDEX);
                    var objRef = idIndex != ObjRef.PRIMARY_KEY_INDEX ? objRefFactory.createObjRef(relatedEntity, idIndex) : primaryObjRef;
                    relatedObjRefsWithVersion[j] = objRef; // use the alternate id objref matching to the link
                    if (!toDeleteMap.putIfNotExists(primaryObjRef, relatedEntity) || (primaryObjRef != objRef && !toDeleteMap.putIfNotExists(objRef, relatedEntity))) {
                        continue;
                    }
                    if (objRefToChangeContainerMap.get(primaryObjRef) instanceof DeleteContainer || (primaryObjRef != objRef && objRefToChangeContainerMap.get(objRef) instanceof DeleteContainer)) {
                        // nothing to do
                        continue;
                    }
                    var container = new DeleteContainer();
                    container.setReference(objRef);
                    changeContainers.add(container);
                    objRefToChangeContainerMap.put(container.getReference(), container);
                }
            } else {
                for (int j = relatedObjRefs.length; j-- > 0; ) {
                    var relatedEntity = (AbstractCacheValue) relatedEntities.get(j);
                    if (relatedEntity == null) {
                        throw OptimisticLockUtil.throwDeleted(relatedObjRefs[j]);
                    }
                    var objRef = objRefFactory.createObjRef(relatedEntity, idIndex);
                    relatedObjRefsWithVersion[j] = objRef; // use the alternate id objref matching to the link
                }
            }
            if (removeRelations) {
                var correctIndexReference = oriHelper.entityToObjRef(entity, idIndex);
                var fromOris = Arrays.asList(correctIndexReference);
                addLinkChangeContainer(changeContainers, link, fromOris, new ArrayList<IObjRef>(relatedObjRefsWithVersion));
            }
        }

        return changeContainers;
    }

    @Override
    public List<IChangeContainer> handleOutgoingRelation(List<IObjRef> references, byte idIndex, ITable table, IDirectedLink link, IMap<IObjRef, RootCacheValue> toDeleteMap,
            ISet<EntityLinkKey> alreadyHandled, ISet<RootCacheValue> alreadyPrefetched, IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap, IRootCache rootCache) {
        var linkMD = link.getMetaData();
        var cascadeDelete = linkMD.isCascadeDelete();
        var selfRelation = link.getToTable().equals(table);
        boolean removeRelations;
        if (selfRelation) {
            removeRelations = true;
        } else if (cascadeDelete) {
            removeRelations = linkMD.isStandaloneLink() && linkMD.getReverseLink().isStandaloneLink();
        } else {
            removeRelations = linkMD.isStandaloneLink();
        }
        if (!cascadeDelete && !removeRelations) {
            throw new IllegalStateException("Must never happen, because the queueing map would not have been filled with this state");
        }
        return handleOutgoingRelation(references, idIndex, link, cascadeDelete, removeRelations, toDeleteMap, alreadyHandled, alreadyPrefetched, objRefToChangeContainerMap, rootCache);
    }

    @Override
    public List<IChangeContainer> handleIncomingRelation(List<IObjRef> references, byte idIndex, ITable table, IDirectedLink link, IMap<IObjRef, RootCacheValue> toDeleteMap,
            IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap, IRootCache rootCache, IIncrementalMergeState incrementalState) {
        var linkMD = link.getMetaData();
        var cascadeDelete = linkMD.isCascadeDelete();
        var selfRelation = link.getToTable().equals(table);
        boolean removeRelations;
        if (selfRelation) {
            removeRelations = true;
        } else if (cascadeDelete) {
            removeRelations = linkMD.isStandaloneLink() && linkMD.getReverseLink().isStandaloneLink();
        } else {
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
            throw new IllegalStateException("Must never happen, because the queueing map would not have been filled with this state");
        }
        return handleIncomingRelation(references, idIndex, table, link, cascadeDelete, removeRelations, becauseOfSelfRelation, toDeleteMap, objRefToChangeContainerMap, rootCache, incrementalState);
    }

    protected List<IChangeContainer> handleIncomingRelation(List<IObjRef> references, byte srcIdIndex, ITable table, IDirectedLink link, boolean cascadeDelete, boolean removeRelations,
            boolean becauseOfSelfRelation, IMap<IObjRef, RootCacheValue> toDeleteMap, IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap, IRootCache rootCache,
            IIncrementalMergeState incrementalState) {
        var linkMD = link.getMetaData();
        var entityMetaDataProvider = this.entityMetaDataProvider;
        var oriHelper = this.oriHelper;
        var relatedMetaData = entityMetaDataProvider.getMetaData(link.getToTable().getMetaData().getEntityType());
        var relatedType = relatedMetaData.getEntityType();
        var member = becauseOfSelfRelation ? linkMD.getMember() : linkMD.getReverseLink().getMember();
        removeRelations &= becauseOfSelfRelation ? linkMD.isNullable() : linkMD.getReverseLink().isNullable();

        var childMemberNameToDataIndexMap = new HashMap<String, ChildMember>();

        var childMemberNameToIdsMap = buildPropertyNameToIdsMap(references, member.getElementType());
        var query = buildParentChildQuery(relatedMetaData, member.getName(), childMemberNameToIdsMap, childMemberNameToDataIndexMap);
        query = parameterizeParentChildQuery(query, member.getName(), childMemberNameToIdsMap);

        var relatingRefs = new ArrayList<IObjRef>();

        var changeContainers = new ArrayList<IChangeContainer>();

        if (cascadeDelete) {
            var criteriaObjRefs = new ArrayList<IObjRef>();
            var relatingEntities = retrieveCacheValues(query, relatedMetaData, childMemberNameToDataIndexMap, criteriaObjRefs, rootCache);
            var idIndex = becauseOfSelfRelation ? linkMD.getToField().getIdIndex() : linkMD.getReverseLink().getToField().getIdIndex();
            for (int j = 0; j < relatingEntities.size(); j++) {
                var relatingEntity = relatingEntities.get(j);
                var criteriaObjRef = criteriaObjRefs.get(j);

                var primaryObjRef = oriHelper.entityToObjRef(relatingEntity, idIndex, relatedMetaData);
                var relatingRef = idIndex != ObjRef.PRIMARY_KEY_INDEX ? oriHelper.entityToObjRef(relatingEntity, idIndex, relatedMetaData) : primaryObjRef;
                relatingRefs.add(relatingRef);

                if (linkMD.getReverseLink().getMember() != null) {
                    var changeContainer = objRefToChangeContainerMap.get(criteriaObjRef);
                    if (changeContainer == null) {
                        throw new IllegalStateException("Must never happen");
                    }
                    if (changeContainer instanceof CreateOrUpdateContainerBuild) {
                        var createOrUpdate = (CreateOrUpdateContainerBuild) changeContainer;
                        var criteriaRui = createOrUpdate.ensureRelation(linkMD.getReverseLink().getMember().getName());
                        criteriaRui.removeObjRef(primaryObjRef);
                    }
                }
                if (!toDeleteMap.putIfNotExists(primaryObjRef, relatingEntity) || (primaryObjRef != relatingRef && !toDeleteMap.putIfNotExists(relatingRef, relatingEntity))) {
                    continue;
                }
                if (objRefToChangeContainerMap.get(primaryObjRef) instanceof DeleteContainer ||
                        (primaryObjRef != relatingRef && objRefToChangeContainerMap.get(relatingRef) instanceof DeleteContainer)) {
                    // nothing to do
                    continue;
                }
                var container = new DeleteContainer();
                container.setReference(primaryObjRef);
                changeContainers.add(container);
                objRefToChangeContainerMap.put(container.getReference(), container);
            }
        } else {
            var cursor = query.retrieveAsVersions(false);
            try {
                var preparedObjRefFactory = objRefFactory.prepareObjRefFactory(relatedType, ObjRef.PRIMARY_KEY_INDEX);
                for (var versionItem : cursor) {
                    var objRef = preparedObjRefFactory.createObjRef(versionItem.getId(), versionItem.getVersion());
                    relatingRefs.add(objRef);

                    var changeContainer = objRefToChangeContainerMap.get(objRef);
                    if (changeContainer != null) {
                        // DELETE: we have nothing to do
                        // UPDATE: our operation is redundant
                        // CREATE: can never occur because we just selected the key from the persistence layer
                        continue;
                    }
                    var updateContainer = incrementalState != null ? incrementalState.newUpdateContainer(objRef.getRealType()) : new UpdateContainer();
                    updateContainer.setReference(objRef);
                    changeContainers.add(updateContainer);
                    objRefToChangeContainerMap.put(updateContainer.getReference(), updateContainer);
                }
            } finally {
                cursor.dispose();
            }
        }

        if (!relatingRefs.isEmpty()) {
            var directedLink = becauseOfSelfRelation ? link : link.getReverseLink();
            addLinkChangeContainer(changeContainers, directedLink, relatingRefs, references);
        }
        return changeContainers;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected List<RootCacheValue> retrieveCacheValues(IQuery<?> query, IEntityMetaData metaData, HashMap<String, ChildMember> childMemberNameToDataIndexMap, List<IObjRef> criteriaObjRefs,
            IRootCache rootCache) {
        var objRefs = new ArrayList<IObjRef>();

        var cursor = query.retrieveAsData();
        try {
            var primaryIdIndex = childMemberNameToDataIndexMap.get(metaData.getIdMember().getName()).dataIndex;
            var versionIndex = metaData.getVersionMember() != null ? childMemberNameToDataIndexMap.get(metaData.getVersionMember().getName()).dataIndex : -1;

            var dataIndices = new int[childMemberNameToDataIndexMap.size() - (versionIndex != -1 ? 2 : 1)];
            var dataIndexObjectRefFactories = new IPreparedObjRefFactory[dataIndices.length];
            var count = 0;
            for (var entry : childMemberNameToDataIndexMap) {
                var childMember = entry.getValue();
                var dataIndex = childMember.dataIndex;
                if (dataIndex == primaryIdIndex || dataIndex == versionIndex) {
                    continue;
                }
                dataIndices[count] = dataIndex;
                dataIndexObjectRefFactories[count] = objRefFactory.prepareObjRefFactory(childMember.member.getElementType(), childMember.idIndex);
                count++;
            }
            var preparedObjRefFactory = objRefFactory.prepareObjRefFactory(metaData.getEntityType(), ObjRef.PRIMARY_KEY_INDEX);
            for (var item : cursor) {
                var id = item.getValue(primaryIdIndex);
                var version = versionIndex >= 0 ? item.getValue(versionIndex) : null;

                var objRef = preparedObjRefFactory.createObjRef(id, version);
                objRefs.add(objRef);

                for (var dataIndex : dataIndices) {
                    var criteriaId = item.getValue(dataIndex);
                    if (criteriaId != null) {
                        var criteriaObjRef = dataIndexObjectRefFactories[dataIndex].createObjRef(criteriaId, null);
                        criteriaObjRefs.add(criteriaObjRef);
                        break;
                    }
                }
            }
        } finally {
            cursor.dispose();
        }
        var result = (List) rootCache.getObjects(objRefs, cacheValueAndReturnMissesSet);
        return result;
    }

    protected void addLinkChangeContainer(List<IChangeContainer> changeContainers, IDirectedLink link, List<IObjRef> fromOris, List<IObjRef> toOris) {
        var directedLink = link.getLink().getDirectedLink();
        if (!directedLink.equals(link)) {
            var temp = fromOris;
            fromOris = toOris;
            toOris = temp;
        }

        for (int i = fromOris.size(); i-- > 0; ) {
            var fromOri = fromOris.get(i);
            var command = new LinkChangeCommand(fromOri, directedLink);
            command.addRefsToUnlink(toOris);
            var linkContainer = new LinkContainer();
            linkContainer.setReference(fromOri);
            linkContainer.setCommand(command);
            changeContainers.add(linkContainer);
        }
    }

    @Override
    public void checkForCorrectIdIndex(ILinkChangeCommand changeCommand, IMap<Byte, List<IObjRef>> toChange) {
        var idIndex = changeCommand.getDirectedLink().getMetaData().getToIdIndex();
        checkForCorrectIdIndex(changeCommand.getRefsToLink(), idIndex, toChange);
        checkForCorrectIdIndex(changeCommand.getRefsToUnlink(), idIndex, toChange);
    }

    protected void checkForCorrectIdIndex(List<IObjRef> objRefs, byte idIndex, IMap<Byte, List<IObjRef>> toChange) {
        if (objRefs.isEmpty()) {
            return;
        }
        var toChangeList = toChange.get(idIndex);
        for (int i = objRefs.size(); i-- > 0; ) {
            var objRef = objRefs.get(i);
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
            } else {
                newOri = new ObjRef(objRef.getRealType(), objRef.getIdNameIndex(), objRef.getId(), objRef.getVersion());
                objRefs.set(i, newOri);
            }
            toChangeList.add(newOri);
        }
    }

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
}
