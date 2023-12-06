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
import com.koch.ambeth.cache.RootCache;
import com.koch.ambeth.cache.rootcachevalue.RootCacheValue;
import com.koch.ambeth.cache.util.IndirectValueHolderRef;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.util.IMultithreadingHelper;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IMergeServiceExtension;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.cache.AbstractCacheValue;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.compositeid.ICompositeIdFactory;
import com.koch.ambeth.merge.incremental.IIncrementalMergeState;
import com.koch.ambeth.merge.metadata.IObjRefFactory;
import com.koch.ambeth.merge.model.CreateOrUpdateContainerBuild;
import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.model.IChangeContainer;
import com.koch.ambeth.merge.model.ICreateOrUpdateContainer;
import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.merge.model.IOriCollection;
import com.koch.ambeth.merge.model.IPrimitiveUpdateItem;
import com.koch.ambeth.merge.model.IRelationUpdateItem;
import com.koch.ambeth.merge.model.RelationUpdateItemBuild;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.merge.proxy.PersistenceContextType;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.merge.server.change.CreateCommand;
import com.koch.ambeth.merge.server.change.DeleteCommand;
import com.koch.ambeth.merge.server.change.IChangeCommand;
import com.koch.ambeth.merge.server.change.ICreateCommand;
import com.koch.ambeth.merge.server.change.ILinkChangeCommand;
import com.koch.ambeth.merge.server.change.ITableChange;
import com.koch.ambeth.merge.server.change.LinkContainer;
import com.koch.ambeth.merge.server.change.LinkTableChange;
import com.koch.ambeth.merge.server.change.TableChange;
import com.koch.ambeth.merge.server.change.UpdateCommand;
import com.koch.ambeth.merge.transfer.CUDResult;
import com.koch.ambeth.merge.transfer.CreateContainer;
import com.koch.ambeth.merge.transfer.DeleteContainer;
import com.koch.ambeth.merge.transfer.DirectObjRef;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.merge.transfer.OriCollection;
import com.koch.ambeth.merge.transfer.UpdateContainer;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.merge.util.IPrefetchState;
import com.koch.ambeth.merge.util.OptimisticLockUtil;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.IDatabaseMetaData;
import com.koch.ambeth.persistence.api.IPrimaryKeyProvider;
import com.koch.ambeth.persistence.api.ITable;
import com.koch.ambeth.persistence.parallel.IModifyingDatabase;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.IValueObjectConfig;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.StringBuilderUtil;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.util.collections.IdentityLinkedSet;
import com.koch.ambeth.util.collections.InterfaceFastList;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.function.CheckedConsumer;
import com.koch.ambeth.util.function.CheckedRunnable;
import com.koch.ambeth.util.model.IMethodDescription;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import lombok.SneakyThrows;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

@PersistenceContext(PersistenceContextType.REQUIRED)
public class PersistenceMergeServiceExtension implements IMergeServiceExtension {
    @Autowired
    protected IServiceContext beanContext;
    @Autowired
    protected ICache cache;
    @Autowired
    protected ICompositeIdFactory compositeIdFactory;
    @Autowired
    protected IConversionHelper conversionHelper;
    @Autowired
    protected IDatabase database;
    @Autowired
    protected IDatabaseMetaData databaseMetaData;
    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;
    @Autowired
    protected IMultithreadingHelper multithreadingHelper;
    @Autowired
    protected IThreadLocalObjectCollector objectCollector;
    @Autowired
    protected IObjRefFactory objRefFactory;
    @Autowired
    protected IObjRefHelper objRefHelper;
    @Autowired
    protected IPrefetchHelper prefetchHelper;
    @Autowired
    protected IPrimaryKeyProvider primaryKeyProvider;
    @Autowired
    protected IRelationMergeService relationMergeService;
    @Autowired
    protected IRootCache rootCache;
    @Autowired
    protected ISecurityActivation securityActivation;
    @LogInstance
    private ILogger log;

    @Override
    public List<IEntityMetaData> getMetaData(List<Class<?>> entityTypes) {
        return entityMetaDataProvider.getMetaData(entityTypes);
    }

    @Override
    public IValueObjectConfig getValueObjectConfig(Class<?> valueType) {
        return entityMetaDataProvider.getValueObjectConfig(valueType);
    }

    protected IList<IChangeContainer> transformToBuildableCUDResult(ICUDResult cudResult, IIncrementalMergeState incrementalState,
            IMap<IChangeContainer, IChangeContainer> buildableToOriginalChangeContainerMap) {
        var allChanges = cudResult.getAllChanges();
        var directObjRefReplaceMap = new IdentityHashMap<IDirectObjRef, IDirectObjRef>();
        var buildableAllChanges = new ArrayList<IChangeContainer>(allChanges.size());
        for (int a = 0, size = allChanges.size(); a < size; a++) {
            var changeContainer = allChanges.get(a);
            if (changeContainer instanceof CreateOrUpdateContainerBuild) {
                buildableToOriginalChangeContainerMap.put(changeContainer, changeContainer);
                buildableAllChanges.add(changeContainer);
                // nothing to do
                continue;
            }
            if (changeContainer instanceof DeleteContainer) {
                buildableAllChanges.add(changeContainer);
                // nothing to do
                continue;
            }
            var objRef = replaceObjRefIfNecessary(changeContainer.getReference(), directObjRefReplaceMap);
            var buildableContainer = changeContainer instanceof CreateContainer ? incrementalState.newCreateContainer(objRef.getRealType()) : incrementalState.newUpdateContainer(objRef.getRealType());
            buildableAllChanges.add(buildableContainer);
            buildableToOriginalChangeContainerMap.put(buildableContainer, changeContainer);
            buildableContainer.setReference(objRef);
        }
        for (var entry : buildableToOriginalChangeContainerMap) {
            var buildableContainer = (CreateOrUpdateContainerBuild) entry.getKey();
            var changeContainer = entry.getValue();

            IPrimitiveUpdateItem[] puis = null;
            IRelationUpdateItem[] ruis = null;
            if (changeContainer instanceof CreateContainer) {
                puis = ((CreateContainer) changeContainer).getPrimitives();
                ruis = ((CreateContainer) changeContainer).getRelations();
            } else if (changeContainer instanceof UpdateContainer) {
                puis = ((UpdateContainer) changeContainer).getPrimitives();
                ruis = ((UpdateContainer) changeContainer).getRelations();
            }
            if (puis != null) {
                for (IPrimitiveUpdateItem pui : puis) {
                    buildableContainer.addPrimitive(pui);
                }
            }
            if (ruis == null) {
                continue;
            }
            for (IRelationUpdateItem rui : ruis) {
                var existingRui = buildableContainer.ensureRelation(rui.getMemberName());
                var addedORIs = rui.getAddedORIs();
                if (addedORIs != null) {
                    for (var addedObjRef : addedORIs) {
                        existingRui.addObjRef(replaceObjRefIfNecessary(addedObjRef, directObjRefReplaceMap));
                    }
                }
                var removedORIs = rui.getRemovedORIs();
                if (removedORIs != null) {
                    for (var removedObjRef : removedORIs) {
                        existingRui.removeObjRef(replaceObjRefIfNecessary(removedObjRef, directObjRefReplaceMap));
                    }
                }
            }
        }
        return buildableAllChanges;
    }

    protected IObjRef replaceObjRefIfNecessary(IObjRef objRef, IMap<IDirectObjRef, IDirectObjRef> directObjRefReplaceMap) {
        if (!(objRef instanceof IDirectObjRef)) {
            return objRef;
        }
        var directObjRef = (IDirectObjRef) objRef;
        var replacedObjRef = directObjRefReplaceMap.get(directObjRef);
        if (replacedObjRef == null) {
            replacedObjRef = new DirectObjRef(directObjRef.getRealType(), ((IDirectObjRef) objRef).getDirect());
            directObjRefReplaceMap.put(directObjRef, replacedObjRef);
        }
        return replacedObjRef;
    }

    @Override
    public ICUDResult evaluateImplictChanges(ICUDResult cudResult, final IIncrementalMergeState incrementalState) {
        try {
            var allChanges = cudResult.getAllChanges();
            var originalRefs = cudResult.getOriginalRefs();
            var buildableToOriginalChangeContainerMap = IdentityHashMap.<IChangeContainer, IChangeContainer>create(allChanges.size());
            var tableChangeMap = new HashMap<String, ITableChange>();
            var oriList = new ArrayList<IObjRef>(allChanges.size());

            var mockIdToObjRefMap = new HashMap<Long, IObjRef>();
            var objRefToEntityMap = new HashMap<IObjRef, Object>();
            LinkedHashMap<IObjRef, IChangeContainer> objRefToChangeContainerMap = new LinkedHashMap<IObjRef, IChangeContainer>() {
                @Override
                public IChangeContainer put(IObjRef key, IChangeContainer value) {
                    if (value instanceof LinkContainer) {
                        var linkCommand = ((LinkContainer) value).getCommand();
                        var linkMetaData = linkCommand.getDirectedLink().getMetaData();
                        var objRef = linkCommand.getReference();
                        var fromMember = linkMetaData.getMember();
                        if (fromMember != null) {
                            var changeContainer = get(objRef);
                            if (!(changeContainer instanceof DeleteContainer)) {
                                if (changeContainer == null) {
                                    changeContainer = incrementalState.newUpdateContainer(objRef.getRealType());
                                    changeContainer.setReference(objRef);
                                    put(objRef, changeContainer);
                                }
                                var rui = ((CreateOrUpdateContainerBuild) changeContainer).ensureRelation(fromMember.getName());
                                rui.addObjRefs(linkCommand.getRefsToLink());
                                rui.removeObjRefs(linkCommand.getRefsToUnlink());
                            }
                        }
                        var toMember = linkMetaData.getReverseLink().getMember();
                        if (toMember != null) {
                            var toMemberName = toMember.getName();
                            var refsToLink = linkCommand.getRefsToLink();
                            for (int a = refsToLink.size(); a-- > 0; ) {
                                var linkedObjRef = refsToLink.get(a);
                                var changeContainer = get(linkedObjRef);
                                if (changeContainer instanceof DeleteContainer) {
                                    continue;
                                }
                                if (changeContainer == null) {
                                    changeContainer = incrementalState.newUpdateContainer(linkedObjRef.getRealType());
                                    changeContainer.setReference(linkedObjRef);
                                    put(linkedObjRef, changeContainer);
                                }
                                ((CreateOrUpdateContainerBuild) changeContainer).ensureRelation(toMemberName).addObjRef(objRef);
                            }
                            var refsToUnlink = linkCommand.getRefsToUnlink();
                            for (int a = refsToUnlink.size(); a-- > 0; ) {
                                var linkedObjRef = refsToUnlink.get(a);
                                var changeContainer = get(linkedObjRef);
                                if (changeContainer instanceof DeleteContainer) {
                                    continue;
                                }
                                if (changeContainer == null) {
                                    changeContainer = incrementalState.newUpdateContainer(linkedObjRef.getRealType());
                                    changeContainer.setReference(linkedObjRef);
                                    put(linkedObjRef, changeContainer);
                                }
                                ((CreateOrUpdateContainerBuild) changeContainer).ensureRelation(toMemberName).removeObjRef(objRef);
                            }
                        }
                        return null;
                    }
                    var existingValue = super.get(key);
                    if (existingValue == value || existingValue instanceof DeleteContainer) {
                        return null;
                    }
                    return super.put(key, value);
                }
            };
            var buildableAllChanges = transformToBuildableCUDResult(cudResult, incrementalState, buildableToOriginalChangeContainerMap);

            for (int a = buildableAllChanges.size(); a-- > 0; ) {
                var changeContainer = buildableAllChanges.get(a);
                var objRef = changeContainer.getReference();
                var entity = originalRefs.get(a);

                objRefToChangeContainerMap.put(objRef, changeContainer);
                var allObjRefs = objRefHelper.entityToAllObjRefs(entity);
                for (int b = allObjRefs.size(); b-- > 0; ) {
                    objRefToChangeContainerMap.put(allObjRefs.get(b), changeContainer);
                }
                objRefToEntityMap.put(objRef, entity);
            }
            executeWithoutSecurity(buildableAllChanges, tableChangeMap, oriList, mockIdToObjRefMap, objRefToChangeContainerMap, incrementalState);

            var changeContainersSet = new IdentityLinkedSet<IChangeContainer>();

            for (var entry : objRefToChangeContainerMap) {
                var changeContainer = entry.getValue();
                changeContainersSet.add(changeContainer);
            }
            var changeContainers = new ArrayList<IChangeContainer>(changeContainersSet.size());
            for (int a = 0, size = buildableAllChanges.size(); a < size; a++) {
                var changeContainer = buildableAllChanges.get(a);
                changeContainersSet.remove(changeContainer);
                changeContainers.add(changeContainer);
            }
            changeContainers.addAll(changeContainersSet);
            var newAllObjects = new Object[changeContainers.size()];

            IObjRef[] objRefsToLoad = null;

            for (int a = changeContainers.size(); a-- > 0; ) {
                var changeContainer = changeContainers.get(a);
                var objRef = changeContainer.getReference();
                if (objRef instanceof IDirectObjRef) {
                    ((IDirectObjRef) objRef).setCreateContainerIndex(a);
                }
                if (changeContainer instanceof CreateOrUpdateContainerBuild) {
                    changeContainers.set(a, ((CreateOrUpdateContainerBuild) changeContainer).build());
                }
                var entity = objRefToEntityMap.get(objRef);
                if (entity == null) {
                    if (objRefsToLoad == null) {
                        objRefsToLoad = new IObjRef[changeContainers.size()];
                    }
                    objRefsToLoad[a] = objRef;
                    continue;
                }
                newAllObjects[a] = entity;
            }
            if (objRefsToLoad != null) {
                var entities = cache.getObjects(objRefsToLoad, CacheDirective.returnMisses());
                for (int a = entities.size(); a-- > 0; ) {
                    var entity = entities.get(a);
                    if (entity != null) {
                        newAllObjects[a] = entity;
                        continue;
                    }
                    var objRefToLoad = objRefsToLoad[a];
                    if (objRefToLoad == null) {
                        continue;
                    }
                    var anyVersion = cache.getObject(new ObjRef(objRefToLoad.getRealType(), objRefToLoad.getIdNameIndex(), objRefToLoad.getId(), null), CacheDirective.returnMisses());
                    if (anyVersion == null) {
                        throw OptimisticLockUtil.throwDeleted(objRefToLoad);
                    }
                    var objRefOfAnyVersion = objRefHelper.entityToObjRef(anyVersion);
                    throw OptimisticLockUtil.throwModified(objRefsToLoad[a], objRefOfAnyVersion.getVersion());
                }
            }
            return new CUDResult(changeContainers, new ArrayList<>(newAllObjects));
        } finally {
            database.getContextProvider().clearAfterMerge();
        }
    }

    protected void executeRunnables(List<CheckedRunnable> runnables) {
        while (!runnables.isEmpty()) {
            var runnableArray = runnables.toArray(CheckedRunnable[]::new);
            runnables.clear();
            for (var runnable : runnableArray) {
                CheckedRunnable.invoke(runnable);
            }
        }
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    protected void ensureCorrectIdIndexOfAllRelations(ICUDResult cudResult) {
        var runnables = new ArrayList<CheckedConsumer<IMap<IObjRef, Object>>>();
        var objRefsWithWrongIdIndex = new HashSet<IObjRef>();

        var allChanges = cudResult.getAllChanges();
        for (int a = allChanges.size(); a-- > 0; ) {
            var change = allChanges.get(a);
            if (!(change instanceof ICreateOrUpdateContainer)) {
                continue;
            }
            var fullRUIs = ((ICreateOrUpdateContainer) change).getFullRUIs();
            if (fullRUIs == null) {
                continue;
            }
            var table = databaseMetaData.getTableByType(change.getReference().getRealType());
            for (var rui : fullRUIs) {
                if (rui == null) {
                    continue;
                }
                var link = table.getLinkByMemberName(rui.getMemberName());
                var expectedIdIndex = link.getToIdIndex();
                ensureCorrectIdIndexOfRelation(rui.getAddedORIs(), expectedIdIndex, objRefsWithWrongIdIndex, runnables);
                ensureCorrectIdIndexOfRelation(rui.getRemovedORIs(), expectedIdIndex, objRefsWithWrongIdIndex, runnables);
            }
        }
        while (!runnables.isEmpty()) {
            var objRefsWithWrongIdIndexList = objRefsWithWrongIdIndex.toList();
            objRefsWithWrongIdIndex.clear();

            var objRefsList = new ArrayList<IObjRef>(objRefsWithWrongIdIndexList.size());
            for (int a = 0, size = objRefsWithWrongIdIndexList.size(); a < size; a++) {
                var objRef = objRefsWithWrongIdIndexList.get(a);
                if (objRef instanceof IDirectObjRef) {
                    objRefsList.add(null);
                    continue;
                }
                objRefsList.add(objRef);
            }

            var entities = rootCache.getObjects(objRefsList, EnumSet.of(CacheDirective.CacheValueResult, CacheDirective.ReturnMisses));

            var objRefToEntityMap = HashMap.<IObjRef, Object>create(entities.size());
            for (int a = entities.size(); a-- > 0; ) {
                var objRef = objRefsWithWrongIdIndexList.get(a);
                if (objRef instanceof IDirectObjRef) {
                    objRefToEntityMap.put(objRef, ((IDirectObjRef) objRef).getDirect());
                    continue;
                }
                objRefToEntityMap.put(objRef, entities.get(a));
            }
            var runnablesArray = runnables.toArray(CheckedConsumer[]::new);
            runnables.clear();
            for (var runnable : runnablesArray) {
                runnable.accept(objRefToEntityMap);
            }
        }
    }

    protected void ensureCorrectIdIndexOfRelation(final IObjRef[] objRefs, final int expectedIdIndex, ISet<IObjRef> objRefsWithWrongIdIndex, IList<CheckedConsumer<IMap<IObjRef, Object>>> runnables) {
        if (objRefs == null) {
            return;
        }
        boolean hasObjRefsWithWrongIdIndex = false;
        for (var objRef : objRefs) {
            // if (objRef instanceof IDirectObjRef && ((IDirectObjRef) objRef).getDirect() != null)
            // {
            // continue;
            // }
            if (objRef.getIdNameIndex() == expectedIdIndex) {
                continue;
            }
            objRefsWithWrongIdIndex.add(objRef);
            hasObjRefsWithWrongIdIndex = true;
        }
        if (!hasObjRefsWithWrongIdIndex) {
            return;
        }
        runnables.add(objRefToEntityMap -> {
            for (int a = objRefs.length; a-- > 0; ) {
                var objRef = objRefs[a];
                if (objRef.getIdNameIndex() == expectedIdIndex) {
                    continue;
                }
                var entity = objRefToEntityMap.get(objRef);
                var expectedObjRef = objRefHelper.entityToObjRef(entity, expectedIdIndex);
                objRefs[a] = expectedObjRef;
            }
        });
    }

    @Override
    public IOriCollection merge(ICUDResult cudResult, String[] causingUuids, IMethodDescription methodDescription) {
        var database = this.database.getCurrent();
        try {
            // ensureCorrectIdIndexOfAllRelations(cudResult);

            var allChanges = cudResult.getAllChanges();
            var tableChangeMap = new HashMap<String, ITableChange>();
            var oriList = new ArrayList<IObjRef>(allChanges.size());
            var objRefToChangeContainerMap = new HashMap<IObjRef, IChangeContainer>();

            for (int a = allChanges.size(); a-- > 0; ) {
                var changeContainer = allChanges.get(a);
                var objRef = changeContainer.getReference();
                objRefToChangeContainerMap.put(objRef, changeContainer);
            }

            executeWithoutSecurity(allChanges, tableChangeMap, oriList, null, objRefToChangeContainerMap, null);

            var objRefWithoutVersion = new ArrayList<IObjRef>();
            for (var entry : tableChangeMap) {
                var tableChange = entry.getValue();
                if (tableChange instanceof LinkTableChange) {
                    continue;
                }
                for (var rowEntry : ((TableChange) tableChange).getRowCommands()) {
                    var objRef = rowEntry.getKey();
                    if (objRef.getVersion() != null || rowEntry.getValue().getCommand() instanceof ICreateCommand) {
                        continue;
                    }
                    objRefWithoutVersion.add(objRef);
                }
            }
            if (!objRefWithoutVersion.isEmpty()) {
                var objects = cache.getObjects(objRefWithoutVersion, CacheDirective.returnMisses());
                for (int a = objects.size(); a-- > 0; ) {
                    var entity = objects.get(a);
                    var versionMember = ((IEntityMetaDataHolder) entity).get__EntityMetaData().getVersionMember();
                    if (versionMember != null) {
                        objRefWithoutVersion.get(a).setVersion(versionMember.getValue(entity));
                    }
                }
            }
            var changeAggregator = persistTableChanges(database, tableChangeMap);
            changeAggregator.createDataChange(causingUuids);

            for (int a = oriList.size(); a-- > 0; ) {
                var objRef = oriList.get(a);
                if (!(objRef instanceof IDirectObjRef)) {
                    continue;
                }
                oriList.set(a, objRefFactory.dup(objRef));
            }
            var oriCollection = new OriCollection(oriList);
            var contextProvider = database.getContextProvider();
            oriCollection.setChangedOn(contextProvider.getCurrentTime().longValue());
            oriCollection.setChangedBy(contextProvider.getCurrentUser());

            return oriCollection;
        } finally {
            database.getContextProvider().clearAfterMerge();
        }
    }

    protected void executeWithoutSecurity(List<IChangeContainer> allChanges, HashMap<String, ITableChange> tableChangeMap, List<IObjRef> oriList, IMap<Long, IObjRef> mockIdToObjRefMap,
            IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap, IIncrementalMergeState incrementalState) {
        var rollback = securityActivation.pushWithoutSecurity();
        try {
            var database = this.database.getCurrent();
            var rootCache = this.rootCache.getCurrentRootCache();

            var toDeleteMap = new HashMap<IObjRef, RootCacheValue>();
            var linkChangeCommands = new LinkedHashMap<ITableChange, IList<ILinkChangeCommand>>();
            var typeToIdlessReferenceMap = new LinkedHashMap<Class<?>, IList<IObjRef>>();
            var toLoadForDeletion = new ArrayList<IObjRef>();
            fillOriList(oriList, allChanges, toLoadForDeletion);

            loadEntitiesForDeletion(toLoadForDeletion, toDeleteMap, rootCache);

            convertChangeContainersToCommands(database, allChanges, tableChangeMap, typeToIdlessReferenceMap, linkChangeCommands, toDeleteMap, objRefToChangeContainerMap, rootCache, incrementalState);

            if (mockIdToObjRefMap == null) {
                aquireAndAssignIds(typeToIdlessReferenceMap);
            } else {
                mockAquireAndAssignIds(typeToIdlessReferenceMap, mockIdToObjRefMap);
            }
            processLinkChangeCommands(linkChangeCommands, tableChangeMap, rootCache);

            if (mockIdToObjRefMap != null) {
                undoMockIds(mockIdToObjRefMap);
            }
        } finally {
            rollback.rollback();
        }
    }

    protected void fillOriList(List<IObjRef> oriList, List<IChangeContainer> allChanges, IList<IObjRef> toLoadForDeletion) {
        for (int a = 0, size = allChanges.size(); a < size; a++) {
            IChangeContainer changeContainer = allChanges.get(a);
            if (changeContainer instanceof CreateContainer) {
                oriList.add(changeContainer.getReference());
                // ((IDirectObjRef) changeContainer.getReference()).setDirect(changeContainer);
            } else if (changeContainer instanceof UpdateContainer) {
                oriList.add(changeContainer.getReference());
            } else if (changeContainer instanceof DeleteContainer) {
                oriList.add(null);
                toLoadForDeletion.add(changeContainer.getReference());
            }
        }
    }

    protected ITable getEnsureTable(IDatabase database, Class<?> referenceClass) {
        referenceClass = entityMetaDataProvider.getMetaData(referenceClass).getEntityType();
        var table = database.getTableByType(referenceClass);
        if (table == null) {
            throw new RuntimeException("No table configured for entity '" + referenceClass + "'");
        }
        return table;
    }

    protected void loadEntitiesForDeletion(IList<IObjRef> toLoadForDeletion, IMap<IObjRef, RootCacheValue> toDeleteMap, IRootCache rootCache) {
        var conversionHelper = this.conversionHelper;
        var entityMetaDataProvider = this.entityMetaDataProvider;
        var objRefHelper = this.objRefHelper;
        var objects = rootCache.getObjects(toLoadForDeletion, RelationMergeService.cacheValueAndReturnMissesSet);
        for (int i = objects.size(); i-- > 0; ) {
            var object = objects.get(i);

            var oriToLoad = toLoadForDeletion.get(i);
            if (object == null) {
                throw OptimisticLockUtil.throwDeleted(oriToLoad);
            }
            var metaData = entityMetaDataProvider.getMetaData(oriToLoad.getRealType());
            var versionMember = metaData.getVersionMember();
            var expectedVersion = oriToLoad.getVersion();
            if (expectedVersion == null && versionMember != null) {
                throw new OptimisticLockException("Object " + oriToLoad + " is not specified with a version. You should know what you want to delete", null, object);
            }
            if (versionMember != null) {
                expectedVersion = conversionHelper.convertValueToType(versionMember.getRealType(), expectedVersion);
            }
            var references = objRefHelper.entityToAllObjRefs(object);
            if (!Objects.equals(expectedVersion, references.get(0).getVersion())) {
                throw OptimisticLockUtil.throwModified(references.get(0), expectedVersion, object);
            }
            for (int j = references.size(); j-- > 0; ) {
                var objRef = references.get(j);

                toDeleteMap.put(objRef, (RootCacheValue) object);
            }
        }
    }

    protected void convertChangeContainersToCommands(IDatabase database, List<IChangeContainer> allChanges, IMap<String, ITableChange> tableChangeMap,
            ILinkedMap<Class<?>, IList<IObjRef>> typeToIdlessReferenceMap, ILinkedMap<ITableChange, IList<ILinkChangeCommand>> linkChangeCommands, final IMap<IObjRef, RootCacheValue> toDeleteMap,
            final IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap, final IRootCache rootCache, IIncrementalMergeState incrementalState) {
        var relationMergeService = this.relationMergeService;

        var changeQueue = new InterfaceFastList<IChangeContainer>();

        changeQueue.pushAllFrom(allChanges);

        var previousParentToMovedOrisMap = new LinkedHashMap<CheckForPreviousParentKey, IList<IObjRef>>();
        var incomingRelationToReferenceMap = new LinkedHashMap<IncomingRelationKey, IList<IObjRef>>();
        var outgoingRelationToReferenceMap = new LinkedHashMap<OutgoingRelationKey, IList<IObjRef>>();
        var allAddedORIs = new HashSet<IObjRef>();
        var alreadyHandled = new HashSet<EntityLinkKey>();
        var alreadyPrefetched = new IdentityHashSet<RootCacheValue>();

        findAllNewlyReferencedORIs(allChanges, allAddedORIs);

        while (true) {
            while (!changeQueue.isEmpty()) {
                var changeContainer = changeQueue.popFirst().getElemValue();

                var reference = changeContainer.getReference();
                Object entityHandler;
                String entityHandlerName;
                if (!(changeContainer instanceof LinkContainer)) {
                    entityHandler = getEnsureTable(database, reference.getRealType());
                    entityHandlerName = ((ITable) entityHandler).getMetaData().getName();
                } else {
                    entityHandler = database.getTableByName(((LinkContainer) changeContainer).getTableName());
                    if (entityHandler != null) {
                        entityHandlerName = ((ITable) entityHandler).getMetaData().getName();
                    } else {
                        entityHandlerName = ((LinkContainer) changeContainer).getTableName();
                    }
                }

                var tableChange = relationMergeService.getTableChange(tableChangeMap, entityHandler, entityHandlerName);

                IChangeCommand changeCommand = null;
                if ((changeContainer instanceof CreateOrUpdateContainerBuild && ((CreateOrUpdateContainerBuild) changeContainer).isCreate()) || changeContainer instanceof CreateContainer) {
                    var createCommand = new CreateCommand(changeContainer.getReference());
                    createCommand.configureFromContainer(changeContainer, tableChange.getTable());
                    changeCommand = createCommand;
                    var ruis = ((ICreateOrUpdateContainer) changeContainer).getFullRUIs();
                    var newChanges =
                            relationMergeService.processCreateDependencies(reference, (ITable) entityHandler, ruis, previousParentToMovedOrisMap, allAddedORIs, objRefToChangeContainerMap, rootCache);
                    changeQueue.pushAllFrom(newChanges);

                    var realType = reference.getRealType();
                    var references = typeToIdlessReferenceMap.get(realType);
                    if (references == null) {
                        references = new ArrayList<>();
                        typeToIdlessReferenceMap.put(realType, references);
                    }
                    references.add(reference);
                } else if ((changeContainer instanceof CreateOrUpdateContainerBuild && ((CreateOrUpdateContainerBuild) changeContainer).isUpdate()) || changeContainer instanceof UpdateContainer) {
                    var updateCommand = new UpdateCommand(changeContainer.getReference());
                    updateCommand.configureFromContainer(changeContainer, tableChange.getTable());
                    changeCommand = updateCommand;
                    var ruis = ((ICreateOrUpdateContainer) changeContainer).getFullRUIs();
                    var newChanges =
                            relationMergeService.processUpdateDependencies(reference, (ITable) entityHandler, ruis, toDeleteMap, previousParentToMovedOrisMap, allAddedORIs, objRefToChangeContainerMap,
                                    rootCache);
                    changeQueue.pushAllFrom(newChanges);
                    relationMergeService.handleUpdateNotifications(reference.getRealType(), ruis, tableChangeMap);
                } else if (changeContainer instanceof DeleteContainer) {
                    if (reference.getIdNameIndex() != ObjRef.PRIMARY_KEY_INDEX) {
                        var entity = toDeleteMap.get(reference);
                        reference = objRefHelper.entityToObjRef(entity);
                        changeContainer.setReference(reference);
                        objRefToChangeContainerMap.put(reference, changeContainer);
                    }
                    var deleteCommand = new DeleteCommand(changeContainer.getReference());
                    deleteCommand.configureFromContainer(changeContainer, tableChange.getTable());
                    changeCommand = deleteCommand;
                    var newChanges = relationMergeService.processDeleteDependencies(reference, (ITable) entityHandler, toDeleteMap, outgoingRelationToReferenceMap, incomingRelationToReferenceMap,
                            previousParentToMovedOrisMap, allAddedORIs, objRefToChangeContainerMap, rootCache);
                    changeQueue.pushAllFrom(newChanges);
                } else if (changeContainer instanceof LinkContainer) {
                    // Link commands may be converted in updates of foreign key columns. Since new objects may
                    // not have
                    // an ID yet we have to process them later.
                    var changeCommands = linkChangeCommands.get(tableChange);
                    if (changeCommands == null) {
                        changeCommands = new ArrayList<>();
                        linkChangeCommands.put(tableChange, changeCommands);
                    }
                    var linkContainer = (LinkContainer) changeContainer;

                    changeCommands.add(linkContainer.getCommand());

                    // force bi-directional link-handling
                    objRefToChangeContainerMap.put(linkContainer.getReference(), linkContainer);
                    continue;
                }

                tableChange.addChangeCommand(changeCommand);
            }
            for (var entry : previousParentToMovedOrisMap) {
                var key = entry.getKey();
                var value = entry.getValue();
                var resultOfFork = relationMergeService.checkForPreviousParent(value, key.entityType, key.memberName, objRefToChangeContainerMap, incrementalState);
                changeQueue.pushAllFrom(resultOfFork);
            }
            previousParentToMovedOrisMap.clear();

            for (var entry : incomingRelationToReferenceMap) {
                var key = entry.getKey();
                var value = entry.getValue();
                var resultOfFork = relationMergeService.handleIncomingRelation(value, key.idIndex, key.table, key.link, toDeleteMap, objRefToChangeContainerMap, rootCache, incrementalState);
                changeQueue.pushAllFrom(resultOfFork);
            }
            incomingRelationToReferenceMap.clear();

            @SuppressWarnings("unused") var prefetchState = prefetchAllReferredMembers(outgoingRelationToReferenceMap, toDeleteMap, alreadyHandled, alreadyPrefetched, rootCache);

            for (var entry : outgoingRelationToReferenceMap) {
                var key = entry.getKey();
                var value = entry.getValue();
                var resultOfFork =
                        relationMergeService.handleOutgoingRelation(value, key.idIndex, key.table, key.link, toDeleteMap, alreadyHandled, alreadyPrefetched, objRefToChangeContainerMap, rootCache);
                changeQueue.pushAllFrom(resultOfFork);
            }
            outgoingRelationToReferenceMap.clear();

            if (changeQueue.isEmpty()) {
                break;
            }
        }
    }

    protected IPrefetchState prefetchAllReferredMembers(IMap<OutgoingRelationKey, IList<IObjRef>> outgoingRelationToReferenceMap, IMap<IObjRef, RootCacheValue> toDeleteMap,
            HashSet<EntityLinkKey> alreadyHandled, IdentityHashSet<RootCacheValue> alreadyPrefetched, IRootCache rootCache) {
        var toPrefetch = new ArrayList<IndirectValueHolderRef>();
        for (var entry : outgoingRelationToReferenceMap) {
            var references = entry.getValue();
            for (var reference : references) {
                var entity = toDeleteMap.get(reference);
                if (!alreadyPrefetched.add(entity)) {
                    continue;
                }
                var metaData = entity.get__EntityMetaData();
                var relationMembers = metaData.getRelationMembers();
                for (int a = relationMembers.length; a-- > 0; ) {
                    toPrefetch.add(new IndirectValueHolderRef(entity, relationMembers[a], (RootCache) rootCache));
                }
            }
        }
        if (toPrefetch.isEmpty()) {
            return null;
        }
        return prefetchHelper.prefetch(toPrefetch);
    }

    /**
     * Finds all ORIs referenced as 'added' in a RUI. Used to not cascade-delete moved entities.
     *
     * @param allChanges   All changes in the CUDResult.
     * @param allAddedORIs All ORIs referenced as 'added' in a RUI.
     */
    protected void findAllNewlyReferencedORIs(List<IChangeContainer> allChanges, HashSet<IObjRef> allAddedORIs) {
        for (int i = allChanges.size(); i-- > 0; ) {
            var changeContainer = allChanges.get(i);
            if (changeContainer instanceof DeleteContainer) {
                continue;
            }
            IRelationUpdateItem[] fullRUIs;
            if (changeContainer instanceof CreateOrUpdateContainerBuild) {
                fullRUIs = ((CreateOrUpdateContainerBuild) changeContainer).getFullRUIs();
            } else if (changeContainer instanceof CreateContainer) {
                fullRUIs = ((CreateContainer) changeContainer).getRelations();
            } else if (changeContainer instanceof UpdateContainer) {
                fullRUIs = ((UpdateContainer) changeContainer).getRelations();
            } else {
                throw new IllegalArgumentException("Unknown IChangeContainer implementation: '" + changeContainer.getClass().getName() + "'");
            }
            if (fullRUIs == null) {
                continue;
            }
            for (var rui : fullRUIs) {
                if (rui == null) {
                    continue;
                }
                var addedORIs = rui.getAddedORIs();
                if (addedORIs != null) {
                    allAddedORIs.addAll(addedORIs);
                }
            }
        }
    }

    protected void aquireAndAssignIds(ILinkedMap<Class<?>, IList<IObjRef>> typeToIdlessReferenceMap) {
        multithreadingHelper.invokeAndWait(typeToIdlessReferenceMap, entry -> {
            var entityType = entry.getKey();
            var metaData = entityMetaDataProvider.getMetaData(entityType);
            var table = databaseMetaData.getTableByType(metaData.getEntityType());
            if (table == null) {
                throw new RuntimeException("No table configured for entity '" + entityType + "'");
            }
            var idlessReferences = entry.getValue();

            primaryKeyProvider.acquireIds(table, idlessReferences);
        });
    }

    protected void mockAquireAndAssignIds(ILinkedMap<Class<?>, IList<IObjRef>> typeToIdlessReferenceMap, IMap<Long, IObjRef> mockIdToObjRefMap) {
        var idMock = -1;
        for (var entry : typeToIdlessReferenceMap) {
            var idlessReferences = entry.getValue();
            for (int i = idlessReferences.size(); i-- > 0; ) {
                var reference = idlessReferences.get(i);
                var value = new Long(idMock);
                reference.setId(value);
                reference.setIdNameIndex(ObjRef.PRIMARY_KEY_INDEX);
                idMock--;
                mockIdToObjRefMap.put(value, reference);
            }
        }
    }

    protected void undoMockIds(IMap<Long, IObjRef> mockIdToObjRefMap) {
        for (var entry : mockIdToObjRefMap) {
            entry.getValue().setId(null);
        }
    }

    protected void processLinkChangeCommands(ILinkedMap<ITableChange, IList<ILinkChangeCommand>> linkChangeCommands, IMap<String, ITableChange> tableChangeMap, IRootCache rootCache) {
        ICompositeIdFactory compositeIdFactory = this.compositeIdFactory;
        IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
        IRelationMergeService relationMergeService = this.relationMergeService;
        LinkedHashMap<Byte, IList<IObjRef>> toChange = new LinkedHashMap<>();
        for (Entry<ITableChange, IList<ILinkChangeCommand>> entry : linkChangeCommands) {
            IList<ILinkChangeCommand> changeCommands = entry.getValue();
            for (int i = changeCommands.size(); i-- > 0; ) {
                ILinkChangeCommand changeCommand = changeCommands.get(i);
                relationMergeService.handleUpdateNotifications(changeCommand, tableChangeMap);
                relationMergeService.checkForCorrectIdIndex(changeCommand, toChange);
            }
        }
        for (Entry<Byte, IList<IObjRef>> entry : toChange) {
            byte idIndex = entry.getKey().byteValue();
            IList<IObjRef> changeList = entry.getValue();
            for (int i = changeList.size(); i-- > 0; ) {
                IObjRef ori = changeList.get(i);
                IEntityMetaData metaData = entityMetaDataProvider.getMetaData(ori.getRealType());
                Object id = null;
                if (ori instanceof IDirectObjRef) {
                    String idPropertyName = metaData.getIdMemberByIdIndex(idIndex).getName();
                    Object directRef = ((IDirectObjRef) ori).getDirect();
                    if (directRef instanceof CreateContainer) {
                        CreateContainer container = (CreateContainer) directRef;
                        IPrimitiveUpdateItem[] updateItems = container.getPrimitives();
                        if (updateItems != null) {
                            for (int j = updateItems.length; j-- > 0; ) {
                                IPrimitiveUpdateItem updateItem = updateItems[j];
                                if (idPropertyName.equals(updateItem.getMemberName())) {
                                    id = updateItem.getNewValue();
                                    break;
                                }
                            }
                        }
                    } else if (metaData.getEntityType().isAssignableFrom(directRef.getClass())) {
                        id = compositeIdFactory.createIdFromEntity(metaData, idIndex, directRef);
                    } else {
                        throw new IllegalArgumentException(
                                "Type of '" + directRef.getClass().getName() + "' is not expected in DirectObjRef for entity type '" + metaData.getEntityType().getName() + "'");
                    }
                } else {
                    AbstractCacheValue entity = (AbstractCacheValue) rootCache.getObject(ori, CacheDirective.cacheValueResult());
                    if (idIndex == ObjRef.PRIMARY_KEY_INDEX) {
                        id = entity.getId();
                    } else {
                        id = compositeIdFactory.createIdFromPrimitives(metaData, idIndex, entity);
                    }
                }
                if (id == null) {
                    throw new IllegalArgumentException("Missing id value for relation");
                }
                ori.setId(id);
                ori.setIdNameIndex(idIndex);
            }
        }
        for (Entry<ITableChange, IList<ILinkChangeCommand>> entry : linkChangeCommands) {
            ITableChange tableChange = entry.getKey();
            IList<ILinkChangeCommand> changeCommands = entry.getValue();
            for (int i = changeCommands.size(); i-- > 0; ) {
                tableChange.addChangeCommand(changeCommands.get(i));
            }
        }
    }

    protected IChangeAggregator persistTableChanges(IDatabase database, IMap<String, ITableChange> tableChangeMap) {
        // Mark this database as modifying (e.g. to suppress later out-of-transaction parallel reads)
        var modifyingDatabase = database.getAutowiredBeanInContext(IModifyingDatabase.class);
        if (!modifyingDatabase.isModifyingAllowed()) {
            throw new PersistenceException("It is not allowed to modify anything while the transaction is in read-only mode");
        }
        modifyingDatabase.setModifyingDatabase(true);

        var changeAggregator = beanContext.registerBean(ChangeAggregator.class).finish();
        var tableChangeList = tableChangeMap.values();
        var start = System.currentTimeMillis();

        // Important to sort the table changes to deal with deadlock issues due to pessimistic locking
        Collections.sort(tableChangeList);
        try {
            RuntimeException primaryException = null;
            var rollback = database.disableConstraints();
            try {
                executeTableChanges(tableChangeList, changeAggregator);
            } catch (RuntimeException e) {
                primaryException = e;
            } finally {
                try {
                    rollback.rollback();
                } catch (RuntimeException e) {
                    if (primaryException == null) {
                        throw e;
                    }
                }
                if (primaryException != null) {
                    throw primaryException;
                }
            }
        } finally {
            var end = System.currentTimeMillis();
            if (log.isDebugEnabled()) {
                long spent = end - start;
                log.debug(StringBuilderUtil.concat(objectCollector, "Spent ", spent, " ms on JDBC execution"));
            }
        }

        return changeAggregator;
    }

    protected void executeTableChanges(List<ITableChange> tableChangeList, IChangeAggregator changeAggregator) {
        for (int i = tableChangeList.size(); i-- > 0; ) {
            var tableChange = tableChangeList.get(i);
            tableChange.execute(changeAggregator);
        }
    }

    @Override
    public String createMetaDataDOT() {
        throw new UnsupportedOperationException();
    }

    public class ReverseRelationRunnable implements CheckedRunnable {
        private final RelationMember reverseMember;
        private final IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap;
        private final IObjRef[] addedORIs;
        private final IObjRef[] removedORIs;
        private final IObjRef objRef;

        public ReverseRelationRunnable(RelationMember reverseMember, IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap, IObjRef[] addedORIs, IObjRef[] removedORIs, IObjRef objRef) {
            this.reverseMember = reverseMember;
            this.objRefToChangeContainerMap = objRefToChangeContainerMap;
            this.addedORIs = addedORIs;
            this.removedORIs = removedORIs;
            this.objRef = objRef;
        }

        @Override
        public void run() throws Exception {
            if (addedORIs != null) {
                for (IObjRef addedObjRef : addedORIs) {
                    CreateOrUpdateContainerBuild referredChangeContainer = (CreateOrUpdateContainerBuild) objRefToChangeContainerMap.get(addedObjRef);
                    RelationUpdateItemBuild existingRui = referredChangeContainer.ensureRelation(reverseMember.getName());
                    existingRui.addObjRef(objRef);
                }
            }
            if (removedORIs != null) {
                for (IObjRef removedObjRef : removedORIs) {
                    CreateOrUpdateContainerBuild referredChangeContainer = (CreateOrUpdateContainerBuild) objRefToChangeContainerMap.get(removedObjRef);
                    RelationUpdateItemBuild existingRui = referredChangeContainer.ensureRelation(reverseMember.getName());
                    existingRui.removeObjRef(objRef);
                }
            }
        }
    }
}
