package com.koch.ambeth.merge;

/*-
 * #%L
 * jambeth-merge
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

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheContext;
import com.koch.ambeth.merge.cache.IWritableCache;
import com.koch.ambeth.merge.incremental.IIncrementalMergeState;
import com.koch.ambeth.merge.incremental.IncrementalMergeState;
import com.koch.ambeth.merge.incremental.IncrementalMergeState.StateEntry;
import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.model.IChangeContainer;
import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.merge.model.IPrimitiveUpdateItem;
import com.koch.ambeth.merge.model.IRelationUpdateItem;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.merge.transfer.AbstractChangeContainer;
import com.koch.ambeth.merge.transfer.CUDResult;
import com.koch.ambeth.merge.transfer.CreateContainer;
import com.koch.ambeth.merge.transfer.DeleteContainer;
import com.koch.ambeth.merge.transfer.DirectObjRef;
import com.koch.ambeth.merge.transfer.RelationUpdateItem;
import com.koch.ambeth.merge.transfer.UpdateContainer;
import com.koch.ambeth.merge.util.DirectValueHolderRef;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.merge.util.OptimisticLockUtil;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.ListUtil;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.function.CheckedRunnable;
import com.koch.ambeth.util.model.IDataObject;
import com.koch.ambeth.util.state.IStateRollback;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CUDResultApplier implements ICUDResultApplier {
    protected final ThreadLocal<CloneState> cloneStateTL = new ThreadLocal<>();
    @Autowired
    protected IServiceContext beanContext;

    @Autowired
    protected ICacheContext cacheContext;

    @Autowired
    protected IEntityFactory entityFactory;

    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;

    @Autowired
    protected IPrefetchHelper prefetchHelper;

    @Autowired
    protected IObjRefHelper objRefHelper;

    @Override
    public IIncrementalMergeState acquireNewState(ICache stateCache) {
        return beanContext.registerBean(IncrementalMergeState.class)//
                          .propertyValue("StateCache", stateCache)//
                          .finish();
    }

    @Override
    public ICUDResult applyCUDResultOnEntitiesOfCache(ICUDResult cudResult, final boolean checkBaseState, final IIncrementalMergeState incrementalState) {
        ICache cache = incrementalState.getStateCache().getCurrentCache();
        if (cache.getCurrentCache() == cache) {
            // given cache is already the current cache
            return applyIntern(cudResult, checkBaseState, (IncrementalMergeState) incrementalState);
        }
        IStateRollback rollback = cacheContext.pushCache(cache);
        try {
            return applyIntern(cudResult, checkBaseState, (IncrementalMergeState) incrementalState);
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        } finally {
            rollback.rollback();
        }
    }

    protected IList<Object> getAllExistingObjectsFromCache(ICache cache, List<IChangeContainer> allChanges) {
        ArrayList<IObjRef> existingObjRefs = new ArrayList<>(allChanges.size());
        for (int a = 0, size = allChanges.size(); a < size; a++) {
            IChangeContainer changeContainer = allChanges.get(a);
            if (changeContainer instanceof CreateContainer) {
                existingObjRefs.add(null);
                continue;
            }
            if (changeContainer.getReference() instanceof IDirectObjRef) {
                throw new IllegalStateException();
            }
            existingObjRefs.add(changeContainer.getReference());
        }
        return cache.getObjects(existingObjRefs, CacheDirective.returnMisses());
    }

    protected ICUDResult applyIntern(ICUDResult cudResult, boolean checkBaseState, IncrementalMergeState incrementalState) {
        ICache stateCache = incrementalState.getStateCache();
        List<IChangeContainer> allChanges = cudResult.getAllChanges();
        List<Object> originalRefs = cudResult.getOriginalRefs();
        IList<Object> allObjects = getAllExistingObjectsFromCache(stateCache, allChanges);
        ArrayList<Object> hardRefs = new ArrayList<>();
        hardRefs.add(allObjects); // add list as item intended. adding each item of the source is NOT
        // needed

        ArrayList<IObjRef> toFetchFromCache = new ArrayList<>();
        ArrayList<DirectValueHolderRef> toPrefetch = new ArrayList<>();
        ArrayList<CheckedRunnable> runnables = new ArrayList<>();

        IEntityFactory entityFactory = this.entityFactory;

        IdentityHashMap<IObjRef, StateEntry> newObjRefToStateEntryMap = new IdentityHashMap<>();
        IdentityHashMap<IChangeContainer, IChangeContainer> alreadyClonedMap = new IdentityHashMap<>();

        ArrayList<IChangeContainer> newAllChanges = new ArrayList<>(allChanges.size());

        for (int a = 0, size = allChanges.size(); a < size; a++) {
            IChangeContainer changeContainer = allChanges.get(a);
            Object originalEntity = originalRefs != null ? originalRefs.get(a) : null;

            StateEntry stateEntry = originalEntity != null ? incrementalState.entityToStateMap.get(originalEntity) : null;

            IChangeContainer newChangeContainer;
            if (changeContainer instanceof CreateContainer) {
                newChangeContainer = new CreateContainer();
            } else if (changeContainer instanceof UpdateContainer) {
                newChangeContainer = new UpdateContainer();
            } else {
                newChangeContainer = new DeleteContainer();
            }
            newAllChanges.add(newChangeContainer);
            alreadyClonedMap.put(changeContainer, newChangeContainer);

            if (!(changeContainer instanceof CreateContainer)) {
                Object stateCacheEntity = allObjects.get(a);
                stateEntry = incrementalState.entityToStateMap.get(stateCacheEntity);
                if (stateEntry == null) {
                    stateEntry = new StateEntry(stateCacheEntity, changeContainer.getReference(), incrementalState.entityToStateMap.size() + 1);

                    incrementalState.entityToStateMap.put(stateCacheEntity, stateEntry);
                    incrementalState.objRefToStateMap.put(stateEntry.objRef, stateEntry);
                }
                // delete & update do not need further handling
                continue;
            }
            Class<?> realType = changeContainer.getReference().getRealType();

            Object stateCacheEntity;
            if (stateEntry == null) {
                stateCacheEntity = entityFactory.createEntity(realType);

                DirectObjRef directObjRef = new DirectObjRef(realType, stateCacheEntity);
                directObjRef.setCreateContainerIndex(a);

                stateEntry = new StateEntry(stateCacheEntity, directObjRef, incrementalState.entityToStateMap.size() + 1);

                incrementalState.entityToStateMap.put(stateCacheEntity, stateEntry);
                incrementalState.objRefToStateMap.put(stateEntry.objRef, stateEntry);
                newObjRefToStateEntryMap.put(changeContainer.getReference(), stateEntry);
            } else {
                stateCacheEntity = stateEntry.entity;
            }
            allObjects.set(a, stateCacheEntity);
        }
        cloneStateTL.set(new CloneState(newObjRefToStateEntryMap, incrementalState));
        try {
            for (int a = allChanges.size(); a-- > 0; ) {
                var changeContainer = allChanges.get(a);
                var entity = (IObjRefContainer) allObjects.get(a);
                if (entity == null) {
                    continue;
                }
                changeContainer = fillClonedChangeContainer(changeContainer, alreadyClonedMap);

                IPrimitiveUpdateItem[] puis;
                IRelationUpdateItem[] ruis;
                if (changeContainer instanceof CreateContainer) {
                    CreateContainer createContainer = (CreateContainer) changeContainer;
                    puis = createContainer.getPrimitives();
                    ruis = createContainer.getRelations();
                } else if (changeContainer instanceof UpdateContainer) {
                    UpdateContainer updateContainer = (UpdateContainer) changeContainer;
                    puis = updateContainer.getPrimitives();
                    ruis = updateContainer.getRelations();
                } else {
                    ((IDataObject) entity).setToBeDeleted(true);
                    continue;
                }
                var metaData = ((IEntityMetaDataHolder) entity).get__EntityMetaData();
                applyPrimitiveUpdateItems(entity, puis, metaData);

                if (ruis != null) {
                    boolean isUpdate = changeContainer instanceof UpdateContainer;
                    for (IRelationUpdateItem rui : ruis) {
                        applyRelationUpdateItem(entity, rui, isUpdate, metaData, toPrefetch, toFetchFromCache, checkBaseState, runnables);
                    }
                }
            }
            while (!toPrefetch.isEmpty() || !toFetchFromCache.isEmpty() || !runnables.isEmpty()) {
                if (!toPrefetch.isEmpty()) {
                    prefetchHelper.prefetch(toPrefetch);
                    toPrefetch.clear();
                }
                if (!toFetchFromCache.isEmpty()) {
                    IList<Object> fetchedObjects = stateCache.getObjects(toFetchFromCache, CacheDirective.none());
                    hardRefs.add(fetchedObjects); // add list as item intended. adding each item of the source
                    // is NOT needed
                    toFetchFromCache.clear();
                }
                var runnableArray = runnables.toArray(CheckedRunnable.class);
                runnables.clear();
                for (var runnable : runnableArray) {
                    CheckedRunnable.invoke(runnable);
                }
            }
            var newObjects = new ArrayList<>(allObjects.size());
            var changedRelationRefs = new ArrayList<DirectValueHolderRef>();
            for (int a = allObjects.size(); a-- > 0; ) {
                var newChange = newAllChanges.get(a);
                IRelationUpdateItem[] ruis = null;
                var entity = allObjects.get(a);
                if (newChange instanceof CreateContainer) {
                    newObjects.add(entity);
                    ruis = ((CreateContainer) newChange).getRelations();
                } else if (newChange instanceof UpdateContainer) {
                    ruis = ((UpdateContainer) newChange).getRelations();
                }
                if (ruis == null) {
                    continue;
                }
                var metaData = entityMetaDataProvider.getMetaData(entity.getClass());
                for (var rui : ruis) {
                    var member = metaData.getMemberByName(rui.getMemberName());
                    changedRelationRefs.add(new DirectValueHolderRef((IObjRefContainer) entity, (RelationMember) member));
                }
            }
            if (!newObjects.isEmpty()) {
                ((IWritableCache) stateCache).put(newObjects);
            }
            if (!changedRelationRefs.isEmpty()) {
                prefetchHelper.prefetch(changedRelationRefs);
            }
            return new CUDResult(newAllChanges, allObjects);
        } finally {
            cloneStateTL.set(null);
        }
    }

    protected IChangeContainer fillClonedChangeContainer(IChangeContainer original, IdentityHashMap<IChangeContainer, IChangeContainer> alreadyClonedMap) {
        var clone = alreadyClonedMap.get(original);
        if (clone instanceof CreateContainer) {
            ((CreateContainer) clone).setPrimitives(clonePrimitives(((CreateContainer) original).getPrimitives()));
            ((CreateContainer) clone).setRelations(cloneRelations(((CreateContainer) original).getRelations()));
        } else if (clone instanceof UpdateContainer) {
            ((UpdateContainer) clone).setPrimitives(clonePrimitives(((UpdateContainer) original).getPrimitives()));
            ((UpdateContainer) clone).setRelations(cloneRelations(((UpdateContainer) original).getRelations()));
        }
        ((AbstractChangeContainer) clone).setReference(cloneObjRef(original.getReference(), true));
        return clone;
    }

    protected IRelationUpdateItem[] cloneRelations(IRelationUpdateItem[] original) {
        if (original == null) {
            return null;
        }
        var clone = new IRelationUpdateItem[original.length];
        for (int a = original.length; a-- > 0; ) {
            clone[a] = cloneRelation(original[a]);
        }
        return clone;
    }

    protected IPrimitiveUpdateItem[] clonePrimitives(IPrimitiveUpdateItem[] original) {
        // no need to clone PUIs. even the array is assumed to be never modified
        return original;
    }

    protected IRelationUpdateItem cloneRelation(IRelationUpdateItem original) {
        var clone = new RelationUpdateItem();
        clone.setMemberName(original.getMemberName());
        clone.setAddedORIs(cloneObjRefs(original.getAddedORIs()));
        clone.setRemovedORIs(cloneObjRefs(original.getRemovedORIs()));
        return clone;
    }

    protected IObjRef[] cloneObjRefs(IObjRef[] original) {
        if (original == null || original.length == 0) {
            return original;
        }
        var clone = new IObjRef[original.length];
        for (int a = original.length; a-- > 0; ) {
            clone[a] = cloneObjRef(original[a], true);
        }
        return clone;
    }

    protected IObjRef cloneObjRef(IObjRef original, boolean fromChangeContainer) {
        var cloneState = cloneStateTL.get();
        return resolveObjRefOfCache(original, cloneState);

    }

    protected void applyPrimitiveUpdateItems(Object entity, IPrimitiveUpdateItem[] puis, IEntityMetaData metadata) {
        if (puis == null) {
            return;
        }

        for (var pui : puis) {
            var memberName = pui.getMemberName();
            var newValue = pui.getNewValue();
            var member = metadata.getMemberByName(memberName);
            if (Optional.class.isAssignableFrom(member.getRealType())) {
                newValue = Optional.ofNullable(newValue);
            }
            member.setValue(entity, newValue);
        }
    }

    protected void applyRelationUpdateItem(final IObjRefContainer entity, final IRelationUpdateItem rui, final boolean isUpdate, final IEntityMetaData metaData,
            final IList<DirectValueHolderRef> toPrefetch, final IList<IObjRef> toFetchFromCache, final boolean checkBaseState, final IList<CheckedRunnable> runnables) {
        var objRefHelper = this.objRefHelper;
        var memberName = rui.getMemberName();
        var relationIndex = metaData.getIndexByRelationName(memberName);
        var relationMember = metaData.getRelationMembers()[relationIndex];
        IObjRef[] existingORIs;
        if (entity.is__Initialized(relationIndex)) {
            existingORIs = objRefHelper.extractObjRefList(relationMember.getValue(entity), null).toArray(IObjRef.class);
        } else {
            existingORIs = entity.get__ObjRefs(relationIndex);
            if (existingORIs == null) {
                toPrefetch.add(new DirectValueHolderRef(entity, relationMember, true));
                runnables.add(() -> applyRelationUpdateItem(entity, rui, isUpdate, metaData, toPrefetch, toFetchFromCache, checkBaseState, runnables));
                return;
            }
        }
        var addedORIs = rui.getAddedORIs();
        var removedORIs = rui.getRemovedORIs();

        final IObjRef[] newORIs;
        if (existingORIs.length == 0) {
            if (checkBaseState && removedORIs != null) {
                throw new IllegalArgumentException("Removing from empty member");
            }
            newORIs = addedORIs != null ? Arrays.copyOf(addedORIs, addedORIs.length) : IObjRef.EMPTY_ARRAY;
            for (int a = newORIs.length; a-- > 0; ) {
                newORIs[a] = cloneObjRef(newORIs[a], false);
            }
        } else {
            // Set to efficiently remove entries
            LinkedHashSet<IObjRef> existingORIsSet = new LinkedHashSet<>(existingORIs);
            if (removedORIs != null) {
                for (IObjRef removedORI : removedORIs) {
                    IObjRef clonedObjRef = cloneObjRef(removedORI, false);
                    if (existingORIsSet.remove(clonedObjRef) || !checkBaseState) {
                        continue;
                    }
                    throw OptimisticLockUtil.throwModified(objRefHelper.entityToObjRef(entity), null, entity);
                }
            }
            if (addedORIs != null) {
                for (IObjRef addedORI : addedORIs) {
                    IObjRef clonedObjRef = cloneObjRef(addedORI, false);
                    if (existingORIsSet.add(clonedObjRef) || !checkBaseState) {
                        continue;
                    }
                    throw OptimisticLockUtil.throwModified(objRefHelper.entityToObjRef(entity), null, entity);
                }
            }
            if (existingORIsSet.isEmpty()) {
                newORIs = IObjRef.EMPTY_ARRAY;
            } else {
                newORIs = existingORIsSet.toArray(IObjRef.class);
            }
        }
        if (!entity.is__Initialized(relationIndex)) {
            entity.set__ObjRefs(relationIndex, newORIs);
            return;
        }
        toFetchFromCache.addAll(newORIs);
        runnables.add(() -> {
            var stateCache = cloneStateTL.get().incrementalState.getStateCache();
            var objects = stateCache.getObjects(newORIs, CacheDirective.failEarly());
            Object value;
            if (relationMember.isToMany()) {
                // To-many relation
                var coll = ListUtil.createObservableCollectionOfType(relationMember.getRealType(), objects.size());
                coll.addAll(objects);
                value = coll;
            } else {
                // To-one relation
                value = !objects.isEmpty() ? objects.get(0) : null;
            }
            relationMember.setValue(entity, value);
        });
    }

    protected IObjRef resolveObjRefOfCache(IObjRef objRef, CloneState cloneState) {
        var stateEntry = cloneState.incrementalState.objRefToStateMap.get(objRef);
        if (stateEntry != null) {
            return stateEntry.objRef;
        }
        stateEntry = cloneState.newObjRefToStateEntryMap.get(objRef);
        if (stateEntry != null) {
            return stateEntry.objRef;
        }
        return objRef;
    }

    private static class CloneState {
        public final IdentityHashMap<IObjRef, StateEntry> newObjRefToStateEntryMap;

        public final IncrementalMergeState incrementalState;

        public CloneState(IdentityHashMap<IObjRef, StateEntry> newObjRefToStateEntryMap, IncrementalMergeState incrementalState) {
            this.newObjRefToStateEntryMap = newObjRefToStateEntryMap;
            this.incrementalState = incrementalState;
        }
    }
}
