package com.koch.ambeth.merge;

import com.koch.ambeth.ioc.DefaultExtendableContainer;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.extendable.ClassExtendableContainer;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.cache.CacheFactoryDirective;
import com.koch.ambeth.merge.cache.ICacheContext;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.merge.cache.IDisposableCache;
import com.koch.ambeth.merge.incremental.IncrementalMergeState;
import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.model.IChangeContainer;
import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.merge.model.IOriCollection;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.merge.security.SecurityDirective;
import com.koch.ambeth.merge.service.IMergeService;
import com.koch.ambeth.merge.transfer.CUDResult;
import com.koch.ambeth.merge.transfer.DeleteContainer;
import com.koch.ambeth.merge.transfer.OriCollection;
import com.koch.ambeth.service.config.ServiceConfigurationConstants;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.IValueObjectConfig;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.ParamHolder;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.function.CheckedSupplier;
import com.koch.ambeth.util.model.IMethodDescription;
import com.koch.ambeth.util.proxy.Factory;
import com.koch.ambeth.util.proxy.ICascadedInterceptor;
import com.koch.ambeth.util.proxy.MethodInterceptor;
import com.koch.ambeth.util.state.IStateRollback;
import com.koch.ambeth.util.state.StateRollback;
import com.koch.ambeth.util.threading.IGuiThreadHelper;

import java.io.StringWriter;
import java.util.List;
import java.util.Map.Entry;

public class MergeServiceRegistry implements IMergeService, IMergeServiceExtensionExtendable, IMergeListenerExtendable, IMergeTimeProvider, IThreadLocalCleanupBean {
    protected final ClassExtendableContainer<IMergeServiceExtension> mergeServiceExtensions = new ClassExtendableContainer<>("mergeServiceExtension", "entityType");
    protected final DefaultExtendableContainer<IMergeListener> mergeListeners = new DefaultExtendableContainer<>(IMergeListener.class, "mergeListener");
    @Forkable
    protected final ThreadLocal<Long> startTimeTL = new ThreadLocal<>();
    @Autowired
    protected ICacheContext cacheContext;
    @Autowired
    protected ICacheFactory cacheFactory;
    @Autowired
    protected ICUDResultApplier cudResultApplier;
    @Autowired
    protected ICUDResultComparer cudResultComparer;
    @Autowired(optional = true)
    protected ICUDResultPrinter cudResultPrinter;
    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;
    @Autowired
    protected IGuiThreadHelper guiThreadHelper;
    @Autowired
    protected IMergeController mergeController;
    @Autowired(optional = true)
    protected IMergeSecurityManager mergeSecurityManager;
    @Autowired(optional = true)
    protected ISecurityActivation securityActivation;
    @Autowired(optional = true)
    protected ILightweightTransaction transaction;
    @Property(name = ServiceConfigurationConstants.NetworkClientMode, defaultValue = "false")
    protected boolean isNetworkClientMode;
    @LogInstance
    private ILogger log;

    @Override
    public void cleanupThreadLocal() {
        // intended blank. Interface is just needed to make the @Forkable annotation work
    }

    @Override
    public IOriCollection merge(final ICUDResult cudResult, final String[] causingUuids, final IMethodDescription methodDescription) {
        ParamChecker.assertParamNotNull(cudResult, "cudResult");
        var startTime = startTimeTL.get();
        var rollback = StateRollback.empty();
        if (startTime == null) {
            startTime = Long.valueOf(System.currentTimeMillis());
            startTimeTL.set(startTime);
            rollback = () -> startTimeTL.set(null);
        }
        try {
            if (transaction == null || transaction.isActive()) {
                return mergeIntern(cudResult, causingUuids, methodDescription);
            }
            return transaction.runInLazyTransaction(() -> mergeIntern(cudResult, causingUuids, methodDescription));
        } finally {
            rollback.rollback();
        }
    }

    protected IOriCollection mergeIntern(final ICUDResult cudResultOriginal, final String[] causingUuids, final IMethodDescription methodDescription) {
        CheckedSupplier<IOriCollection> runnable = () -> mergeInternRunnable(cudResultOriginal, causingUuids, methodDescription);
        var rollback = securityActivation != null ? securityActivation.pushWithoutFiltering() : StateRollback.empty();
        try {
            return CheckedSupplier.invoke(runnable);
        } finally {
            rollback.rollback();
        }
    }

    private IOriCollection mergeInternRunnable(ICUDResult cudResultOriginal, String[] causingUuids, IMethodDescription methodDescription) {
        IncrementalMergeState state = null;
        IDisposableCache childCache = null;
        try {
            ICUDResult cudResultOfCache;
            if (MergeProcess.isAddNewlyPersistedEntities() || (log.isDebugEnabled() && cudResultPrinter != null)) {
                childCache = cacheFactory.createPrivileged(CacheFactoryDirective.SubscribeTransactionalDCE, false, Boolean.FALSE, "MergeServiceRegistry.STATE");
                state = (IncrementalMergeState) cudResultApplier.acquireNewState(childCache);
                cudResultOfCache = cudResultApplier.applyCUDResultOnEntitiesOfCache(cudResultOriginal, true, state);
            } else {
                cudResultOfCache = cudResultOriginal;
            }
            if (log.isDebugEnabled()) {
                if (cudResultPrinter != null) {
                    log.debug("Initial merge [" + System.identityHashCode(state != null ? state : cudResultOfCache) + "]:\n" + cudResultPrinter.printCUDResult(cudResultOfCache, state));
                } else {
                    log.debug("Initial merge [" + System.identityHashCode(state != null ? state : cudResultOfCache) + "]. No Details available");
                }
            }
            IList<MergeOperation> mergeOperationSequence;
            final ICUDResult extendedCudResult;
            if (cudResultOfCache != cudResultOriginal && !isNetworkClientMode) {
                mergeOperationSequence = new ArrayList<>();
                extendedCudResult = whatIfMerged(cudResultOfCache, methodDescription, mergeOperationSequence, state);
            } else {
                extendedCudResult = cudResultOfCache;
                var sortedChanges = bucketSortChanges(cudResultOfCache.getAllChanges());
                mergeOperationSequence = createMergeOperationSequence(sortedChanges);
            }
            if (log.isDebugEnabled()) {
                log.debug("Merge finished [" + System.identityHashCode(state != null ? state : cudResultOfCache) + "]");
            }
            if (mergeSecurityManager != null) {
                var rollback = securityActivation.pushWithSecurityDirective(SecurityDirective.enableEntity());
                try {
                    mergeSecurityManager.checkMergeAccess(extendedCudResult, methodDescription);
                } finally {
                    rollback.rollback();
                }
            }
            var originalRefsOfCache = new ArrayList<>(cudResultOfCache.getOriginalRefs());
            var originalRefsExtended = new ArrayList<>(extendedCudResult.getOriginalRefs());
            var oriCollExtended = intern(causingUuids, extendedCudResult, methodDescription, mergeOperationSequence, state);

            var allChangesOriginal = cudResultOriginal.getAllChanges();
            var allChangedObjRefsExtended = oriCollExtended.getAllChangeORIs();
            var allChangedObjRefsResult = new IObjRef[allChangesOriginal.size()];

            var originalRefOfCacheToIndexMap = new IdentityHashMap<Object, Integer>();
            for (int a = originalRefsOfCache.size(); a-- > 0; ) {
                originalRefOfCacheToIndexMap.put(originalRefsOfCache.get(a), Integer.valueOf(a));
            }
            for (int a = originalRefsExtended.size(); a-- > 0; ) {
                var indexOfCache = originalRefOfCacheToIndexMap.get(originalRefsExtended.get(a));
                if (indexOfCache == null) {
                    // this is a change implied by a rule or an persistence-implicit change
                    // we do not know about it in the outer original CUDResult
                    continue;
                }
                var objRefExtended = allChangedObjRefsExtended.get(a);
                var objRefOriginal = allChangesOriginal.get(indexOfCache.intValue()).getReference();
                if (objRefExtended == null) {
                    // entity has been deleted
                    objRefOriginal.setVersion(null);
                } else {
                    objRefOriginal.setId(objRefExtended.getId());
                    objRefOriginal.setVersion(objRefExtended.getVersion());
                }
                if (objRefOriginal instanceof IDirectObjRef) {
                    ((IDirectObjRef) objRefOriginal).setDirect(null);
                }
                allChangedObjRefsResult[indexOfCache.intValue()] = objRefOriginal;
            }
            var oriCollection = new OriCollection(new ArrayList<IObjRef>(allChangedObjRefsResult));
            if (state != null) {
                state.notifyMergePipelineFinishHooks(true);
            }
            return oriCollection;
        } catch (Throwable e) {
            if (state != null) {
                state.notifyMergePipelineFinishHooks(false);
            }
            throw e;
        } finally {
            if (childCache != null) {
                childCache.dispose();
            }
        }
    }

    protected ICUDResult whatIfMerged(ICUDResult cudResult, final IMethodDescription methodDescription, IList<MergeOperation> mergeOperationSequence, final IncrementalMergeState incrementalState) {
        IList<MergeOperation> lastMergeOperationSequence;
        while (true) {
            var sortedChanges = bucketSortChanges(cudResult.getAllChanges());
            lastMergeOperationSequence = createMergeOperationSequence(sortedChanges);

            var hasAtLeastOneImplicitChange = new ParamHolder<>(Boolean.FALSE);
            IStateRollback rollback = cacheContext.pushCache(incrementalState.getStateCache());
            try {
                for (int a = 0, size = lastMergeOperationSequence.size(); a < size; a++) {
                    var mergeOperation = lastMergeOperationSequence.get(a);
                    var mergeServiceExtension = mergeOperation.getMergeServiceExtension();

                    var explAndImplCudResult = mergeServiceExtension.evaluateImplictChanges(cudResult, incrementalState);
                    cudResult = mergeCudResult(cudResult, explAndImplCudResult, mergeServiceExtension, hasAtLeastOneImplicitChange, incrementalState);
                }
            } finally {
                rollback.rollback();
            }
            for (var mergeListener : mergeListeners.getExtensionsShared()) {
                var explAndImplCudResult = mergeListener.preMerge(cudResult, incrementalState);
                cudResult = mergeCudResult(cudResult, explAndImplCudResult, mergeListener, hasAtLeastOneImplicitChange, incrementalState);
            }
            if (!Boolean.TRUE.equals(hasAtLeastOneImplicitChange.getValue())) {
                break;
            }
        }
        mergeOperationSequence.addAll(lastMergeOperationSequence);
        return cudResult;
    }

    protected IOriCollection intern(String[] causingUuids, ICUDResult cudResult, IMethodDescription methodDescription, IList<MergeOperation> mergeOperationSequence,
            IncrementalMergeState incrementalState) {
        var allChanges = cudResult.getAllChanges();
        var originalRefs = cudResult.getOriginalRefs();
        var changeToChangeIndexDict = new IdentityHashMap<IChangeContainer, Integer>();

        for (int a = allChanges.size(); a-- > 0; ) {
            changeToChangeIndexDict.put(allChanges.get(a), a);
        }
        var objRefs = new IObjRef[allChanges.size()];
        var allChangedOn = new Long[allChanges.size()];
        var allChangedBy = new String[allChanges.size()];

        var changedOnSet = new HashSet<Long>();
        var changedBySet = new HashSet<String>();

        for (int a = 0, size = mergeOperationSequence.size(); a < size; a++) {
            var mergeOperation = mergeOperationSequence.get(a);
            var mergeServiceExtension = mergeOperation.getMergeServiceExtension();

            var changesForMergeService = mergeOperation.getChangeContainer();
            var msCudResult = buildCUDResult(changesForMergeService, changeToChangeIndexDict, originalRefs);

            var msOriCollection = mergeServiceExtension.merge(msCudResult, causingUuids, methodDescription);

            mergeController.applyChangesToOriginals(msCudResult, msOriCollection, incrementalState != null ? incrementalState.getStateCache() : null);

            var allChangeORIs = msOriCollection.getAllChangeORIs();

            var msDefaultChangedOn = msOriCollection.getChangedOn();
            var msDefaultChangedBy = msOriCollection.getChangedBy();

            var msAllChangedOn = msOriCollection.getAllChangedOn();
            var msAllChangedBy = msOriCollection.getAllChangedBy();
            for (int b = changesForMergeService.size(); b-- > 0; ) {
                int index = changeToChangeIndexDict.get(changesForMergeService.get(b));
                objRefs[index] = allChangeORIs.get(b);

                if (msAllChangedOn != null) {
                    var msChangedOn = msAllChangedOn[b];
                    allChangedOn[index] = msChangedOn;
                    changedOnSet.add(msChangedOn);
                } else {
                    allChangedOn[index] = msDefaultChangedOn;
                }
                if (msAllChangedBy != null) {
                    var msChangedBy = msAllChangedBy[b];
                    allChangedBy[index] = msChangedBy;
                    changedBySet.add(msChangedBy);
                } else {
                    allChangedBy[index] = msDefaultChangedBy;
                }
            }
            if (msDefaultChangedOn != null) {
                changedOnSet.add(msDefaultChangedOn);
            }
            if (msDefaultChangedBy != null) {
                changedBySet.add(msDefaultChangedBy);
            }
        }
        var oriCollection = new OriCollection();
        oriCollection.setAllChangeORIs(new ArrayList<>(objRefs));

        if (changedBySet.size() == 1) {
            oriCollection.setChangedBy(changedBySet.iterator().next());
        } else {
            oriCollection.setAllChangedBy(allChangedBy);
        }
        if (changedOnSet.size() == 1) {
            oriCollection.setChangedOn(changedOnSet.iterator().next());
        } else {
            oriCollection.setAllChangedOn(allChangedOn);
        }
        for (var mergeListener : mergeListeners.getExtensionsShared()) {
            mergeListener.postMerge(cudResult, objRefs, incrementalState);
        }
        if (originalRefs != null) {
            // Set each original ref to null in order to suppress a post-processing in a potentially
            // calling IMergeProcess
            for (int a = originalRefs.size(); a-- > 0; ) {
                originalRefs.set(a, null);
            }
        }
        // TODO DCE must be fired HERE <---
        return oriCollection;
    }

    protected ICUDResult mergeCudResult(ICUDResult cudResult, ICUDResult explAndImplCudResult, Object implyingHandle, ParamHolder<Boolean> hasAtLeastOneImplicitChange, IncrementalMergeState state) {
        if (explAndImplCudResult == null || cudResult == explAndImplCudResult) {
            return cudResult;
        }
        ICUDResult diffCUDResult = cudResultComparer.diffCUDResult(cudResult, explAndImplCudResult);
        if (diffCUDResult == null) {
            return cudResult;
        }
        hasAtLeastOneImplicitChange.setValue(Boolean.TRUE);
        cudResultApplier.applyCUDResultOnEntitiesOfCache(diffCUDResult, false, state);
        if (log.isDebugEnabled()) {
            Object currHandle = implyingHandle;
            if (currHandle instanceof Factory) {
                var interceptor = (MethodInterceptor) ((Factory) currHandle).getCallbacks()[0];
                while (interceptor instanceof ICascadedInterceptor) {
                    var target = ((ICascadedInterceptor) interceptor).getTarget();
                    if (target instanceof MethodInterceptor) {
                        interceptor = ((MethodInterceptor) target);
                        continue;
                    }
                    currHandle = target;
                    break;
                }
            }
            if (currHandle == null) {
                currHandle = implyingHandle;
            }
            if (cudResultPrinter != null) {
                log.debug("Incremental merge [" + System.identityHashCode(state) + "] (" + currHandle.getClass().getSimpleName() + "):\n" + cudResultPrinter.printCUDResult(diffCUDResult, state));
            } else {
                log.debug("Incremental merge [" + System.identityHashCode(state) + "]  (" + currHandle.getClass().getSimpleName() + "). No Details printable");
            }
        }
        return explAndImplCudResult;
    }

    protected ICUDResult buildCUDResult(IList<IChangeContainer> changesForMergeService, IMap<IChangeContainer, Integer> changeToChangeIndexDict, List<Object> originalRefs) {
        Object[] msOriginalRefs = new Object[changesForMergeService.size()];
        for (int b = changesForMergeService.size(); b-- > 0; ) {
            int index = changeToChangeIndexDict.get(changesForMergeService.get(b)).intValue();
            if (originalRefs != null) {
                msOriginalRefs[b] = originalRefs.get(index);
            }
        }
        return new CUDResult(changesForMergeService, new ArrayList<>(msOriginalRefs));
    }

    @Override
    public List<IEntityMetaData> getMetaData(List<Class<?>> entityTypes) {
        IdentityHashMap<IMergeServiceExtension, List<Class<?>>> mseToEntityTypes = new IdentityHashMap<>();

        for (int a = entityTypes.size(); a-- > 0; ) {
            Class<?> entityType = entityTypes.get(a);
            IMergeServiceExtension mergeServiceExtension = mergeServiceExtensions.getExtension(entityType);
            if (mergeServiceExtension == null) {
                continue;
            }
            List<Class<?>> groupedEntityTypes = mseToEntityTypes.get(mergeServiceExtension);
            if (groupedEntityTypes == null) {
                groupedEntityTypes = new ArrayList<>();
                mseToEntityTypes.put(mergeServiceExtension, groupedEntityTypes);
            }
            groupedEntityTypes.add(entityType);
        }
        ArrayList<IEntityMetaData> metaDataResult = new ArrayList<>(entityTypes.size());
        for (Entry<IMergeServiceExtension, List<Class<?>>> entry : mseToEntityTypes) {
            List<IEntityMetaData> groupedMetaData = entry.getKey().getMetaData(entry.getValue());
            if (groupedMetaData != null) {
                metaDataResult.addAll(groupedMetaData);
            }
        }
        return metaDataResult;
    }

    @Override
    public String createMetaDataDOT() {
        try (StringWriter writer = new StringWriter()) {
            entityMetaDataProvider.toDotGraph(writer);
            return writer.toString();
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    @Override
    public IValueObjectConfig getValueObjectConfig(Class<?> valueType) {
        return entityMetaDataProvider.getValueObjectConfig(valueType);
    }

    protected IMergeServiceExtension getServiceForType(Class<?> type) {
        return getServiceForType(type, false);
    }

    protected IMergeServiceExtension getServiceForType(Class<?> type, boolean tryOnly) {
        if (type == null) {
            return null;
        }
        IMergeServiceExtension mse = mergeServiceExtensions.getExtension(type);
        if (mse == null && !tryOnly) {
            throw new IllegalArgumentException("No " + IMergeServiceExtension.class.getSimpleName() + " found to handle entity type '" + type.getName() + "'");
        }
        return mse;
    }

    protected IMap<Class<?>, IList<IChangeContainer>> bucketSortChanges(List<IChangeContainer> allChanges) {
        IMap<Class<?>, IList<IChangeContainer>> sortedChanges = new HashMap<>();

        for (int i = allChanges.size(); i-- > 0; ) {
            IChangeContainer changeContainer = allChanges.get(i);
            IObjRef objRef = changeContainer.getReference();
            Class<?> type = objRef.getRealType();
            IList<IChangeContainer> changeContainers = sortedChanges.get(type);
            if (changeContainers == null) {
                changeContainers = new ArrayList<>();
                if (!sortedChanges.putIfNotExists(type, changeContainers)) {
                    throw new IllegalStateException("Key already exists " + type);
                }
            }
            changeContainers.add(changeContainer);
        }
        return sortedChanges;
    }

    protected IList<MergeOperation> createMergeOperationSequence(IMap<Class<?>, IList<IChangeContainer>> sortedChanges) {
        Class<?>[] entityPersistOrder = entityMetaDataProvider.getEntityPersistOrder();
        final IList<MergeOperation> mergeOperations = new ArrayList<>();

        if (entityPersistOrder != null) {
            for (int a = entityPersistOrder.length; a-- > 0; ) {
                Class<?> orderedEntityType = entityPersistOrder[a];
                IList<IChangeContainer> changes = sortedChanges.get(orderedEntityType);
                if (changes == null) {
                    // No changes of current type found. Nothing to do here
                    continue;
                }
                IList<IChangeContainer> removes = new ArrayList<>(changes.size());
                IList<IChangeContainer> insertsAndUpdates = new ArrayList<>(changes.size());
                for (int b = changes.size(); b-- > 0; ) {
                    IChangeContainer change = changes.get(b);
                    if (change instanceof DeleteContainer) {
                        removes.add(change);
                    } else {
                        insertsAndUpdates.add(change);
                    }
                }
                if (removes.isEmpty()) {
                    // Nothing to do. Ordering is not necessary here
                    continue;
                }
                if (insertsAndUpdates.isEmpty()) {
                    sortedChanges.remove(orderedEntityType);
                } else {
                    sortedChanges.put(orderedEntityType, insertsAndUpdates);
                }
                IMergeServiceExtension mergeServiceExtension = getServiceForType(orderedEntityType);
                MergeOperation mergeOperation = new MergeOperation();
                mergeOperation.setMergeServiceExtension(mergeServiceExtension);
                mergeOperation.setChangeContainer(removes);

                mergeOperations.add(mergeOperation);
            }
            for (int a = 0, size = entityPersistOrder.length; a < size; a++) {
                Class<?> orderedEntityType = entityPersistOrder[a];
                IList<IChangeContainer> changes = sortedChanges.get(orderedEntityType);
                if (changes == null) {
                    // No changes of current type found. Nothing to do here
                    continue;
                }
                boolean containsNew = false;
                for (int b = changes.size(); b-- > 0; ) {
                    if (changes.get(b).getReference().getId() == null) {
                        containsNew = true;
                        break;
                    }
                }
                if (!containsNew) {
                    // Nothing to do. Ordering is not necessary here
                    continue;
                }
                // Remove batch of changes where at least 1 new entity occured
                // and
                // this type of entity has to be inserted in a global order
                sortedChanges.remove(orderedEntityType);
                IMergeServiceExtension mergeServiceExtension = getServiceForType(orderedEntityType);
                MergeOperation mergeOperation = new MergeOperation();
                mergeOperation.setMergeServiceExtension(mergeServiceExtension);
                mergeOperation.setChangeContainer(changes);

                mergeOperations.add(mergeOperation);
            }
        }

        // Everything which is left in the sortedChanges map can be merged
        // without global order, so batch together as much as possible

        for (Entry<Class<?>, IList<IChangeContainer>> entry : sortedChanges) {
            Class<?> type = entry.getKey();
            IList<IChangeContainer> unorderedChanges = entry.getValue();
            IMergeServiceExtension mergeServiceExtension = getServiceForType(type, true);

            if (mergeServiceExtension == null) {
                throw new IllegalStateException("No extension found to merge entities of type '" + type.getName() + "'");
            }
            boolean cont = false;
            for (MergeOperation existingMergeOperation : mergeOperations) {
                if (existingMergeOperation.getMergeServiceExtension() == mergeServiceExtension) {
                    IList<IChangeContainer> orderedChanges = existingMergeOperation.getChangeContainer();
                    for (int b = unorderedChanges.size(); b-- > 0; ) {
                        orderedChanges.add(unorderedChanges.get(b));
                    }
                    cont = true;
                    break;
                }
            }
            if (cont) {
                continue;
            }
            MergeOperation mergeOperation = new MergeOperation();
            mergeOperation.setMergeServiceExtension(mergeServiceExtension);
            mergeOperation.setChangeContainer(unorderedChanges);

            mergeOperations.add(mergeOperation);
        }
        return mergeOperations;
    }

    @Override
    public long getStartTime() {
        Long startTime = startTimeTL.get();
        if (startTime == null) {
            throw new IllegalStateException("No merge process is currently active");
        }
        return startTime.longValue();
    }

    @Override
    public void registerMergeServiceExtension(IMergeServiceExtension mergeServiceExtension, Class<?> entityType) {
        mergeServiceExtensions.register(mergeServiceExtension, entityType);
    }

    @Override
    public void unregisterMergeServiceExtension(IMergeServiceExtension mergeServiceExtension, Class<?> entityType) {
        mergeServiceExtensions.unregister(mergeServiceExtension, entityType);
    }

    @Override
    public void registerMergeListener(IMergeListener mergeListener) {
        mergeListeners.register(mergeListener);
    }

    @Override
    public void unregisterMergeListener(IMergeListener mergeListener) {
        mergeListeners.unregister(mergeListener);
    }

    public static class MergeOperation {
        protected IMergeServiceExtension mergeServiceExtension;

        protected IList<IChangeContainer> changeContainer;

        public IMergeServiceExtension getMergeServiceExtension() {
            return mergeServiceExtension;
        }

        public void setMergeServiceExtension(IMergeServiceExtension mergeServiceExtension) {
            this.mergeServiceExtension = mergeServiceExtension;
        }

        public IList<IChangeContainer> getChangeContainer() {
            return changeContainer;
        }

        public void setChangeContainer(IList<IChangeContainer> changeContainer) {
            this.changeContainer = changeContainer;
        }
    }
}
