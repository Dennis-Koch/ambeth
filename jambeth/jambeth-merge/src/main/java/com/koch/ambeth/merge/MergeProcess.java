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

import com.koch.ambeth.datachange.model.DirectDataChangeEntry;
import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.datachange.transfer.DataChangeEvent;
import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.event.IEventListener;
import com.koch.ambeth.event.IEventListenerExtendable;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IDeepScanRecursion.EntityDelegate;
import com.koch.ambeth.merge.IDeepScanRecursion.Proceed;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.model.IChangeContainer;
import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.merge.model.IOriCollection;
import com.koch.ambeth.merge.model.IRelationUpdateItem;
import com.koch.ambeth.merge.service.IMergeService;
import com.koch.ambeth.merge.transfer.CreateContainer;
import com.koch.ambeth.merge.transfer.DeleteContainer;
import com.koch.ambeth.merge.transfer.RelationUpdateItem;
import com.koch.ambeth.merge.transfer.UpdateContainer;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.ParamHolder;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.function.CheckedSupplier;
import com.koch.ambeth.util.threading.IGuiThreadHelper;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class MergeProcess implements IMergeProcess {
    private static final ThreadLocal<Boolean> addNewlyPersistedEntitiesTL = new ThreadLocal<>();

    public static final Boolean getAddNewlyPersistedEntities() {
        return addNewlyPersistedEntitiesTL.get();
    }

    public static final boolean isAddNewlyPersistedEntities() {
        return !Boolean.FALSE.equals(addNewlyPersistedEntitiesTL.get());
    }

    public static final void setAddNewlyPersistedEntities(Boolean value) {
        addNewlyPersistedEntitiesTL.set(value);
    }

    @Autowired
    protected IServiceContext beanContext;
    @Autowired
    protected ICache cache;
    @Autowired
    protected ICacheFactory cacheFactory;
    @Autowired
    protected IDeepScanRecursion deepScanRecursion;
    @Autowired
    protected IEventDispatcher eventDispatcher;
    @Autowired
    protected IEventListenerExtendable eventListenerExtendable;
    @Autowired
    protected IGuiThreadHelper guiThreadHelper;
    @Autowired
    protected IMergeController mergeController;
    @Autowired
    protected IMergeService mergeService;
    @Autowired
    protected IObjRefHelper oriHelper;
    @Autowired
    protected IRevertChangesHelper revertChangesHelper;
    @Autowired(optional = true)
    protected ILightweightTransaction transaction;
    @Property(name = ServiceConfigurationConstants.NetworkClientMode, defaultValue = "false")
    protected boolean isNetworkClientMode;
    @LogInstance
    private ILogger log;

    protected void appendDeleteContainers(Object argument, List<IObjRef> oriList, List<IChangeContainer> allChanges, List<Object> originalRefs, List<Object> unpersistedObjectsToDelete) {
        for (int a = allChanges.size(); a-- > 0; ) {
            var changeContainer = allChanges.get(a);
            if (!(changeContainer instanceof DeleteContainer) || ((DeleteContainer) changeContainer).getReference().getId() != null) {
                continue;
            }
            // These are objects without an id but are marked as deleted. They will be deleted
            // locally
            // without transfer to the service
            allChanges.remove(a);
            originalRefs.remove(a);
            var objRef = changeContainer.getReference();
            unpersistedObjectsToDelete.add(((IDirectObjRef) objRef).getDirect());
        }
        if (argument instanceof Collection) {
            var iter = ((Collection<?>) argument).iterator();
            for (int a = 0, size = oriList.size(); a < size; a++) {
                var item = iter.next();

                var ori = oriList.get(a);
                if (ori.getId() == null) {
                    unpersistedObjectsToDelete.add(item);
                    continue;
                }
                var deleteContainer = new DeleteContainer();
                deleteContainer.setReference(ori);

                allChanges.add(deleteContainer);
                originalRefs.add(item);
            }
        } else if (argument.getClass().isArray()) {
            var array = (Object[]) argument;
            for (int a = 0, size = oriList.size(); a < size; a++) {
                var item = array[a];

                var ori = oriList.get(a);
                if (ori.getId() == null) {
                    unpersistedObjectsToDelete.add(item);
                    continue;
                }
                var deleteContainer = new DeleteContainer();
                deleteContainer.setReference(ori);

                allChanges.add(deleteContainer);
                originalRefs.add(item);
            }
        } else {
            var ori = oriList.get(0);
            if (ori.getId() == null) {
                unpersistedObjectsToDelete.add(argument);
                return;
            }
            var deleteContainer = new DeleteContainer();
            deleteContainer.setReference(ori);

            allChanges.add(deleteContainer);
            originalRefs.add(argument);
        }
    }

    protected void mergeOutOfGui(final Object objectToMerge, final Object objectToDelete, final ProceedWithMergeHook proceedHook, final DataChangeReceivedCallback dataChangeReceivedCallback,
            final MergeFinishedCallback mergeFinishedCallback, final boolean addNewEntitiesToCache, final boolean deepMerge) {
        var mergeHandle = beanContext.registerBean(MergeHandle.class)//
                                     .ignoreProperties("Cache", "PrivilegedCache")//
                                     .propertyValue("DeepMerge", deepMerge)//
                                     .finish();
        var cudResult = mergeController.mergeDeep(objectToMerge, mergeHandle);
        var unpersistedObjectsToDelete = new ArrayList<>();
        removeUnpersistedDeletedObjectsFromCudResult(cudResult.getAllChanges(), cudResult.getOriginalRefs(), unpersistedObjectsToDelete);
        if (objectToDelete != null) {
            var extractedObjectToDelete = new ArrayList<>();
            deepScanRecursion.handleDeep(objectToDelete, new EntityDelegate() {
                @Override
                public boolean visitEntity(Object entity, Proceed proceed) {
                    extractedObjectToDelete.add(entity);
                    return true;
                }
            });
            var oriList = oriHelper.extractObjRefList(extractedObjectToDelete, mergeHandle);

            appendDeleteContainers(extractedObjectToDelete, oriList, cudResult.getAllChanges(), cudResult.getOriginalRefs(), unpersistedObjectsToDelete);
        }
        var success = false;
        try {
            processCUDResult(objectToMerge, cudResult, unpersistedObjectsToDelete, proceedHook, dataChangeReceivedCallback, addNewEntitiesToCache);
            success = true;
        } finally {
            if (mergeFinishedCallback != null) {
                mergeFinishedCallback.invoke(success);
            }
        }
    }

    @Override
    public void process(Object objectsToMerge) {
        ensureMergeOutOfGui(objectsToMerge, null, null, null, null, true, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> void process(T objectsToMerge1, T... objectsToMerge2) {
        ensureMergeOutOfGui(new Object[] { objectsToMerge1, objectsToMerge2 }, null, null, null, null, true, true);
    }

    protected void ensureMergeOutOfGui(final Object objectToMerge, final Object objectToDelete, final ProceedWithMergeHook proceedHook, final DataChangeReceivedCallback dataChangeReceivedCallback,
            final MergeFinishedCallback mergeFinishedCallback, final boolean addNewEntitiesToCache, final boolean deepMerge) {
        if (guiThreadHelper.isInGuiThread()) {
            guiThreadHelper.invokeOutOfGui(() -> mergeOutOfGui(objectToMerge, objectToDelete, proceedHook, dataChangeReceivedCallback, mergeFinishedCallback, addNewEntitiesToCache, deepMerge));
        } else {
            mergeOutOfGui(objectToMerge, objectToDelete, proceedHook, dataChangeReceivedCallback, mergeFinishedCallback, addNewEntitiesToCache, deepMerge);
        }
    }

    protected void processCUDResult(Object objectToMerge, final ICUDResult cudResult, IList<Object> unpersistedObjectsToDelete, ProceedWithMergeHook proceedHook,
            DataChangeReceivedCallback dataChangeReceivedCallback, boolean addNewEntitiesToCache) {
        IDataChange dataChange = null;
        if (cudResult.getAllChanges().isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Service call skipped early because there is nothing to merge");
            }
        } else {
            if (proceedHook != null) {
                boolean proceed = proceedHook.checkToProceed(cudResult);
                if (!proceed) {
                    return;
                }
            }
            var uuid = UUID.randomUUID().toString();
            final CountDownLatch latch;
            var foreignThreadDCE = new ParamHolder<IDataChange>();
            final IEventListener listenOnce;
            if (isNetworkClientMode || dataChangeReceivedCallback != null) {
                if (isNetworkClientMode) {
                    latch = new CountDownLatch(1);
                } else {
                    latch = null;
                }
                listenOnce = new IEventListener() {
                    @Override
                    public void handleEvent(Object eventObject, long dispatchTime, long sequenceId) throws Exception {
                        // wait for the DCE corresponding to our merge process is dispatched
                        var dataChange = (IDataChange) eventObject;
                        var causingUUIDs = dataChange.getCausingUUIDs();
                        if (causingUUIDs == null) {
                            return;
                        }
                        for (var causingUUID : causingUUIDs) {
                            if (uuid.equals(causingUUID)) {
                                eventListenerExtendable.unregisterEventListener(this, IDataChange.class);
                                foreignThreadDCE.setValue(dataChange);
                                if (latch != null) {
                                    latch.countDown();
                                }
                                return;
                            }
                        }
                    }
                };
                eventListenerExtendable.registerEventListener(listenOnce, IDataChange.class);
            } else {
                latch = null;
                listenOnce = null;
            }
            final IOriCollection oriColl;
            eventDispatcher.enableEventQueue();
            try {
                eventDispatcher.pause(cache);
                try {
                    var oldNewlyPersistedEntities = addNewlyPersistedEntitiesTL.get();
                    addNewlyPersistedEntitiesTL.set(Boolean.valueOf(addNewEntitiesToCache));
                    try {
                        CheckedSupplier<IOriCollection> runnable = () -> {
                            var mergeResult = mergeService.merge(cudResult, new String[] { uuid }, null);
                            mergeController.applyChangesToOriginals(cudResult, mergeResult, null);
                            return mergeResult;
                        };
                        if (transaction == null || transaction.isActive()) {
                            oriColl = CheckedSupplier.invoke(runnable);
                        } else {
                            oriColl = transaction.runInLazyTransaction(runnable);
                        }
                    } finally {
                        addNewlyPersistedEntitiesTL.set(oldNewlyPersistedEntities);
                    }
                } finally {
                    eventDispatcher.resume(cache);
                }
            } finally {
                eventDispatcher.flushEventQueue();
            }
            if (latch != null) {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    throw RuntimeExceptionUtil.mask(e);
                }
            }
            if (foreignThreadDCE != null) {
                dataChange = foreignThreadDCE.getValue();
                if (dataChange != null) {
                    eventDispatcher.dispatchEvent(dataChange);
                }
            }
        }
        if (unpersistedObjectsToDelete != null && !unpersistedObjectsToDelete.isEmpty()) {
            // Create a DCE for all objects without an id but which should be deleted...
            // This is the case for newly created objects on client side, which should be
            // "cancelled". The DCE notifies all models which contain identity references to the
            // related objects to erase their existence in all controls. They are not relevant in
            // the previous server merge process
            var deleteDataChange = DataChangeEvent.create(0, 0, unpersistedObjectsToDelete.size());
            deleteDataChange.setLocalSource(true);
            var deletes = deleteDataChange.getDeletes();
            for (int a = unpersistedObjectsToDelete.size(); a-- > 0; ) {
                var unpersistedObject = unpersistedObjectsToDelete.get(a);
                deletes.add(new DirectDataChangeEntry(unpersistedObject));
            }
            eventDispatcher.dispatchEvent(deleteDataChange);
        }
        revertChangesHelper.revertChanges(objectToMerge);

        if (dataChange != null && dataChangeReceivedCallback != null) {
            dataChangeReceivedCallback.handleDataChange(dataChange);
        }
    }

    protected void removeUnpersistedDeletedObjectsFromCudResult(List<IChangeContainer> allChanges, List<Object> originalRefs, List<Object> unpersistedObjectsToDelete) {
        Set<IObjRef> removedDirectObjRefs = null;
        for (int a = allChanges.size(); a-- > 0; ) {
            var changeContainer = allChanges.get(a);
            var objRef = changeContainer.getReference();
            if (!(changeContainer instanceof DeleteContainer) || objRef.getId() != null) {
                continue;
            }
            if (removedDirectObjRefs == null) {
                removedDirectObjRefs = new IdentityHashSet<>();
            }
            var dirObjRef = (IDirectObjRef) objRef;
            // These are objects without an id but are marked as deleted. They will be deleted
            // locally without transfer to the service
            allChanges.remove(a);
            originalRefs.remove(a);
            unpersistedObjectsToDelete.add(dirObjRef.getDirect());
            removedDirectObjRefs.add(dirObjRef);
        }
        if (removedDirectObjRefs == null) {
            return;
        }
        // Scan all other changeContainer if they refer to the removed DeleteContainers of
        // unpersisted entities
        for (int a = allChanges.size(); a-- > 0; ) {
            var changeContainer = allChanges.get(a);
            IRelationUpdateItem[] relations;
            if (changeContainer instanceof CreateContainer) {
                relations = ((CreateContainer) changeContainer).getRelations();
            } else if (changeContainer instanceof UpdateContainer) {
                relations = ((UpdateContainer) changeContainer).getRelations();
            } else {
                // DeleteContainers can not refer anything beside themselves
                continue;
            }
            if (relations == null) {
                continue;
            }
            for (int b = relations.length; b-- > 0; ) {
                var childItem = relations[b];
                var addedOris = childItem.getAddedORIs();
                if (addedOris == null) {
                    continue;
                }
                for (int c = addedOris.length; c-- > 0; ) {
                    var addedOri = addedOris[c];
                    if (!removedDirectObjRefs.contains(addedOri)) {
                        continue;
                    }
                    if (addedOris.length == 1) {
                        if (childItem.getRemovedORIs() != null) {
                            ((RelationUpdateItem) childItem).setAddedORIs(null);
                        } else {
                            if (relations.length == 1) {
                                allChanges.remove(a);
                                originalRefs.remove(a);
                                relations = null;
                                break;
                            }
                            var newChildItems = new IRelationUpdateItem[relations.length - 1];
                            System.arraycopy(relations, 0, newChildItems, 0, b);
                            System.arraycopy(relations, b + 1, newChildItems, b, relations.length - b - 1);
                            relations = newChildItems;
                            if (changeContainer instanceof CreateContainer) {
                                ((CreateContainer) changeContainer).setRelations(relations);
                            } else {
                                ((UpdateContainer) changeContainer).setRelations(relations);
                            }
                        }
                        break;
                    }
                    var newAddedOris = new IObjRef[addedOris.length - 1];
                    System.arraycopy(addedOris, 0, newAddedOris, 0, c);
                    System.arraycopy(addedOris, c + 1, newAddedOris, c, addedOris.length - c - 1);
                    addedOris = newAddedOris;
                    ((RelationUpdateItem) childItem).setAddedORIs(addedOris);
                }
                if (relations == null) {
                    break;
                }
            }
        }
    }

    @Override
    public IMergeProcessStarted begin() {
        return new MergeProcessStarted(this);
    }
}
