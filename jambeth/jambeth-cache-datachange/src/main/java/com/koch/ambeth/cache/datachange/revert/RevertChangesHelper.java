package com.koch.ambeth.cache.datachange.revert;

import com.koch.ambeth.cache.AbstractCache;
import com.koch.ambeth.cache.ICacheIntern;
import com.koch.ambeth.cache.IFirstLevelCacheManager;
import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.cache.RootCache;
import com.koch.ambeth.cache.rootcachevalue.RootCacheValue;
import com.koch.ambeth.cache.util.IndirectValueHolderRef;
import com.koch.ambeth.datachange.model.DirectDataChangeEntry;
import com.koch.ambeth.datachange.model.IDataChangeEntry;
import com.koch.ambeth.datachange.transfer.DataChangeEntry;
import com.koch.ambeth.datachange.transfer.DataChangeEvent;
import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.event.IProcessResumeItem;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.util.IImmutableTypeSet;
import com.koch.ambeth.merge.IMergeController;
import com.koch.ambeth.merge.IProxyHelper;
import com.koch.ambeth.merge.IRevertChangesHelper;
import com.koch.ambeth.merge.IRevertChangesSavepoint;
import com.koch.ambeth.merge.RevertChangesFinishedCallback;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICacheModification;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.merge.util.DirectValueHolderRef;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.merge.util.ValueHolderRef;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.Arrays;
import com.koch.ambeth.util.IParamHolder;
import com.koch.ambeth.util.ParamHolder;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.util.collections.IdentityLinkedMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.function.CheckedConsumer;
import com.koch.ambeth.util.model.IDataObject;
import com.koch.ambeth.util.state.StateRollback;
import com.koch.ambeth.util.threading.IGuiThreadHelper;
import com.koch.ambeth.util.typeinfo.ITypeInfoProvider;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public class RevertChangesHelper implements IRevertChangesHelper {
    @Autowired
    protected IServiceContext beanContext;

    @Autowired
    protected ICacheModification cacheModification;

    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;

    @Autowired
    protected IEventDispatcher eventDispatcher;

    @Autowired
    protected IFirstLevelCacheManager firstLevelCacheManager;

    @Autowired
    protected IGuiThreadHelper guiThreadHelper;

    @Autowired
    protected IImmutableTypeSet immutableTypeSet;

    @Autowired
    protected IMergeController mergeController;

    @Autowired
    protected IPrefetchHelper prefetchHelper;

    @Autowired
    protected IProxyHelper proxyHelper;

    @Autowired
    protected IRootCache rootCache;

    @Autowired
    protected ITypeInfoProvider typeInfoProvider;

    protected void backupObjects(Object obj, IMap<Object, IBackup> originalToValueBackup) {
        if (obj == null) {
            return;
        }
        var objType = proxyHelper.getRealType(obj.getClass());
        if (immutableTypeSet.isImmutableType(objType) || originalToValueBackup.containsKey(obj)) {
            return;
        }
        if (obj.getClass().isArray()) {
            var elementType = obj.getClass().getComponentType();
            var length = Array.getLength(obj);
            var preparedArrayGet = Arrays.prepareGet(obj);
            var clone = Array.newInstance(elementType, length);
            System.arraycopy(objType, 0, clone, 0, length);
            var arrayBackup = new ArrayBackup(clone);
            originalToValueBackup.put(obj, arrayBackup);
            if (!immutableTypeSet.isImmutableType(elementType)) {
                for (int a = length; a-- > 0; ) {
                    var arrayItem = preparedArrayGet.get(a);
                    backupObjects(arrayItem, originalToValueBackup);
                }
            }
            return;
        }
        if (obj instanceof List) {
            var list = (List<?>) obj;
            var array = list.toArray(new Object[list.size()]);
            var listBackup = ListBackup.create(array);
            originalToValueBackup.put(obj, listBackup);
            for (var item : array) {
                backupObjects(item, originalToValueBackup);
            }
            return;
        } else if (obj instanceof Collection) {
            var coll = (Collection<?>) obj;
            var array = coll.toArray(new Object[coll.size()]);
            var collBackup = CollectionBackup.create(array);
            originalToValueBackup.put(obj, collBackup);
            for (var item : array) {
                backupObjects(item, originalToValueBackup);
            }
            return;
        }
        var typeInfo = typeInfoProvider.getTypeInfo(objType);
        var metaData = entityMetaDataProvider.getMetaData(objType, true);

        var members = typeInfo.getMembers();
        var originalValues = new Object[members.length];
        var objBackup = new ObjectBackup(members, originalValues);
        originalToValueBackup.put(obj, objBackup);

        for (int b = members.length; b-- > 0; ) {
            var typeInfoMember = members[b];
            if (metaData != null) {
                var member = metaData.getMemberByName(typeInfoMember.getName());
                if (member instanceof RelationMember) {
                    var relationIndex = metaData.getIndexByRelation(member);
                    var state = ((IObjRefContainer) obj).get__State(relationIndex);
                    switch (state) {
                        case INIT: {
                            // nothing to do
                            break;
                        }
                        case LAZY: {
                            var objRefs = ((IObjRefContainer) obj).get__ObjRefs(relationIndex);
                            originalValues[b] = ObjRefBackup.create(objRefs, relationIndex);
                            continue;
                        }
                        case PENDING: {
                            // TODO: wait till pending relation is fetched now
                            throw RuntimeExceptionUtil.createEnumNotSupportedException(state);
                        }
                        default:
                            throw RuntimeExceptionUtil.createEnumNotSupportedException(state);
                    }
                }
            }
            var originalValue = typeInfoMember.getValue(obj);
            originalValues[b] = originalValue;

            backupObjects(originalValue, originalToValueBackup);
        }
    }

    private void callWaitEventToResumeInGui(List<IDataChangeEntry> directObjectDeletes, List<IObjRef> orisToRevert, ISet<Object> persistedObjectsToRevert, List<Object> objectsToRevert,
            List<Object> rootCacheValues, IParamHolder<Boolean> success1, IParamHolder<Boolean> success2, IParamHolder<Boolean> success3, RevertChangesFinishedCallback revertChangesFinishedCallback) {
        guiThreadHelper.invokeInGui(() -> {
            waitEventToResume(processResumeItem -> {
                try {
                    var firstLevelCaches = firstLevelCacheManager.selectFirstLevelCaches();

                    var relationsToPrefetch = new HashSet<DirectValueHolderRef>();

                    var runnables = new ArrayList<Runnable>();

                    var rootCache = (RootCache) RevertChangesHelper.this.rootCache.getCurrentRootCache();

                    for (var firstLevelCache : firstLevelCaches) {
                        var persistedObjectsInThisCache = firstLevelCache.getObjects(orisToRevert, CacheDirective.failEarlyAndReturnMisses());

                        for (int a = persistedObjectsInThisCache.size(); a-- > 0; ) {
                            var persistedObjectInThisCache = (IObjRefContainer) persistedObjectsInThisCache.get(a);
                            if (persistedObjectInThisCache == null) {
                                continue;
                            }
                            if (!persistedObjectsToRevert.contains(persistedObjectInThisCache)) {
                                continue;
                            }
                            var rootCacheValue = (RootCacheValue) rootCacheValues.get(a);
                            if (rootCacheValue != null) {
                                var relationMembers = persistedObjectInThisCache.get__EntityMetaData().getRelationMembers();
                                for (int relationIndex = relationMembers.length; relationIndex-- > 0; ) {
                                    if (persistedObjectInThisCache.is__Initialized(relationIndex)) {
                                        relationsToPrefetch.add(new IndirectValueHolderRef(rootCacheValue, relationMembers[relationIndex], rootCache));
                                    }
                                }
                            }
                            runnables.add(() -> rootCache.applyValues(persistedObjectInThisCache, (ICacheIntern) firstLevelCache, null));
                        }
                    }
                    // need a hard GC ref to the given collection during asynchronous
                    // processing
                    var hardRefs = new ArrayList<>();
                    hardRefs.add(rootCacheValues);
                    hardRefs.add(prefetchHelper.prefetch(relationsToPrefetch));

                    var rollback = StateRollback.chain(chain -> {
                        chain.append(cacheModification.pushActive());
                        chain.append(AbstractCache.pushFailInCacheHierarchyModeActive());
                    });
                    try {
                        for (int a = 0, size = runnables.size(); a < size; a++) {
                            runnables.get(a).run();
                        }

                        for (int a = objectsToRevert.size(); a-- > 0; ) {
                            var objectToRevert = objectsToRevert.get(a);
                            if (objectToRevert instanceof IDataObject dataObject) {
                                // Objects which are specified to be reverted loose their flags
                                dataObject.setToBeDeleted(false);
                            }
                        }
                        if (directObjectDeletes.isEmpty()) {
                            success2.setValue(Boolean.TRUE);
                            return;
                        }
                    } finally {
                        rollback.rollback();
                    }
                } finally {
                    if (processResumeItem != null) {
                        processResumeItem.resumeProcessingFinished();
                    }
                }
                success3.setValue(Boolean.FALSE);
                guiThreadHelper.invokeOutOfGui(() -> {
                    try {
                        var dataChange = DataChangeEvent.create(0, 0, 0);
                        dataChange.setDeletes(directObjectDeletes);

                        eventDispatcher.dispatchEvent(dataChange, System.currentTimeMillis(), -1);
                        success3.setValue(Boolean.TRUE);
                    } finally {
                        if (revertChangesFinishedCallback != null) {
                            revertChangesFinishedCallback.invoke(success3.getValue());
                        }
                    }
                });
                success2.setValue(Boolean.TRUE);
            }, state -> {
                if (revertChangesFinishedCallback != null && success3.getValue() == null) {
                    revertChangesFinishedCallback.invoke(success2.getValue());
                }
            });
            success1.setValue(Boolean.TRUE);
        });
    }

    @Override
    public IRevertChangesSavepoint createSavepoint(Object source) {
        if (source == null) {
            return null;
        }
        var objList = mergeController.scanForInitializedObjects(source, false, false, null, null, null, null, null);
        return createSavepointIntern(objList);
    }

    @Override
    public IRevertChangesSavepoint createSavepoint(Object... sources) {
        if (sources == null || sources.length == 0) {
            return null;
        }
        var objList = mergeController.scanForInitializedObjects(sources, false, false, null, null, null, null, null);
        return createSavepointIntern(objList);
    }

    protected IRevertChangesSavepoint createSavepointIntern(List<Object> objList) {
        var originalToValueBackup = new IdentityLinkedMap<Object, IBackup>();

        // Iterate manually through the list because the list itself should not be 'backuped'
        for (int a = 0, size = objList.size(); a < size; a++) {
            backupObjects(objList.get(a), originalToValueBackup);
        }
        var iter = originalToValueBackup.iterator();
        while (iter.hasNext()) {
            var entry = iter.next();
            var backup = entry.getValue();
            if (backup == null) {
                iter.remove();
            }
        }
        if (originalToValueBackup.isEmpty()) {
            return null;
        }
        return beanContext.registerBean(RevertChangesSavepoint.class).propertyValue(RevertChangesSavepoint.P_CHANGES, originalToValueBackup).finish();
    }

    @Override
    public void revertChanges(Object objectsToRevert) {
        revertChanges(objectsToRevert, null, false, true);
    }

    @Override
    public void revertChanges(Object objectsToRevert, boolean recursive) {
        revertChanges(objectsToRevert, null, recursive, true);
    }

    @Override
    public void revertChanges(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback) {
        revertChanges(objectsToRevert, revertChangesFinishedCallback, false, true);
    }

    @Override
    public void revertChanges(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback, boolean recursive, boolean dirtyOnly) {
        if (objectsToRevert == null) {
            return;
        }
        var objList = mergeController.scanForInitializedObjects(objectsToRevert, recursive, dirtyOnly, null, null, null, null, null);
        revertChangesIntern(objList, dirtyOnly, false, revertChangesFinishedCallback);
    }

    @Override
    public void revertChangesGlobally(Object objectsToRevert) {
        revertChangesGlobally(objectsToRevert, null, true, false);
    }

    @Override
    public void revertChangesGlobally(Object objectsToRevert, boolean recursive) {
        revertChangesGlobally(objectsToRevert, null, true, recursive);
    }

    @Override
    public void revertChangesGlobally(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback) {
        revertChangesGlobally(objectsToRevert, revertChangesFinishedCallback, false, true);
    }

    @Override
    public void revertChangesGlobally(Object objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback, boolean recursive, boolean dirtyOnly) {
        if (objectsToRevert == null) {
            return;
        }
        var objList = mergeController.scanForInitializedObjects(objectsToRevert, recursive, dirtyOnly, null, null, null, null, null);
        revertChangesIntern(objList, dirtyOnly, true, revertChangesFinishedCallback);
    }

    protected void revertChangesIntern(List<Object> objectsToRevert, boolean dirtyOnly, boolean globally, RevertChangesFinishedCallback revertChangesFinishedCallback) {
        // Store the RevertChangesFinishedCallback from this thread on the stack and set the
        // property null (for following calls):
        if (objectsToRevert == null || objectsToRevert.isEmpty()) {
            if (revertChangesFinishedCallback != null) {
                revertChangesFinishedCallback.invoke(true);
            }
            return;
        }
        guiThreadHelper.invokeOutOfGui(() -> {
            if (globally) {
                revertChangesInternOutOfGuiGlobally(objectsToRevert, revertChangesFinishedCallback);
                return;
            }
            revertChangesInternOutOfGui(objectsToRevert, dirtyOnly, revertChangesFinishedCallback);
        });
    }

    protected void revertChangesInternOutOfGui(List<Object> objectsToRevert, boolean dirtyOnly, RevertChangesFinishedCallback revertChangesFinishedCallback) {
        var success1 = new ParamHolder<Boolean>();
        var success2 = new ParamHolder<Boolean>();
        var success3 = new ParamHolder<Boolean>();
        try {
            var directObjectDeletes = new ArrayList<IDataChangeEntry>();
            var objRefs = new ArrayList<IObjRef>();
            var privilegedObjRefs = new ArrayList<IObjRef>();
            var valueHolderKeys = new ArrayList<ValueHolderRef>();
            var initializedObjects = mergeController.scanForInitializedObjects(objectsToRevert, false, dirtyOnly, null, objRefs, privilegedObjRefs, valueHolderKeys, null);

            var orisToRevert = new ArrayList<IObjRef>();
            var persistedObjectsToRevert = new IdentityHashSet<>();
            for (int a = initializedObjects.size(); a-- > 0; ) {
                var objectToRevert = initializedObjects.get(a);
                var metaData = ((IEntityMetaDataHolder) objectToRevert).get__EntityMetaData();
                var id = metaData.getIdMember().getValue(objectToRevert, false);

                if (id == null) {
                    directObjectDeletes.add(new DirectDataChangeEntry(objectToRevert));
                    continue;
                }
                persistedObjectsToRevert.add(objectToRevert);
                orisToRevert.add(new ObjRef(metaData.getEntityType(), ObjRef.PRIMARY_KEY_INDEX, id, null));
            }
            var hardRefsToRootCacheValues = rootCache.getObjects(orisToRevert, EnumSet.of(CacheDirective.CacheValueResult, CacheDirective.ReturnMisses));

            for (int a = orisToRevert.size(); a-- > 0; ) {
                if (hardRefsToRootCacheValues.get(a) == null) {
                    // Object could not be loaded/retrieved any more. So the ori refers to an
                    // invalid object. We can not revert invalid objects and currently ignore them.
                    // They will raise exceptions if they will be tried to persist in a merge
                    // process any time in the future
                    orisToRevert.remove(a);
                }
            }
            // We do nothing with the hardRef-list from the RootCache. It is only necessary to keep
            // track of the instance reference on the stack
            // To prohibit GC any potential WeakReferences in the meantime....
            success2.setValue(Boolean.FALSE);
            callWaitEventToResumeInGui(directObjectDeletes, orisToRevert, persistedObjectsToRevert, objectsToRevert, hardRefsToRootCacheValues, success1, success2, success3,
                    revertChangesFinishedCallback);
        } finally {
            if (revertChangesFinishedCallback != null && success2 == null && success3 == null) {
                revertChangesFinishedCallback.invoke(success1);
            }
        }
    }

    protected void revertChangesInternOutOfGuiGlobally(List<Object> objectsToRevert, RevertChangesFinishedCallback revertChangesFinishedCallback) {
        var success = false;
        try {
            var dataChange = DataChangeEvent.create(0, -1, -1);

            for (int a = objectsToRevert.size(); a-- > 0; ) {
                var objectToRevert = objectsToRevert.get(a);
                var metaData = ((IEntityMetaDataHolder) objectToRevert).get__EntityMetaData();
                var id = metaData.getIdMember().getValue(objectToRevert, false);

                if (id == null) {
                    dataChange.getDeletes().add(new DirectDataChangeEntry(objectToRevert));
                    continue;
                }
                dataChange.getUpdates().add(new DataChangeEntry(metaData.getEntityType(), ObjRef.PRIMARY_KEY_INDEX, id, null));
            }

            eventDispatcher.dispatchEvent(dataChange, System.currentTimeMillis(), -1);
            success = true;
        } finally {
            if (revertChangesFinishedCallback != null) {
                revertChangesFinishedCallback.invoke(success);
            }
        }
    }

    protected void waitEventToResume(CheckedConsumer<IProcessResumeItem> resumeDelegate, CheckedConsumer<Throwable> errorDelegate) {
        var rootCache = this.rootCache;
        var selectedFirstLevelCaches = firstLevelCacheManager.selectFirstLevelCaches();

        var collisionSet = new IdentityHashSet<>();
        collisionSet.add(rootCache);
        for (int a = selectedFirstLevelCaches.size(); a-- > 0; ) {
            collisionSet.add(selectedFirstLevelCaches.get(a));
        }
        // Without the current rootcache we can not handle the event now. We have to block till the
        // rootCache and all childCaches get valid
        eventDispatcher.waitEventToResume(collisionSet, -1, resumeDelegate, errorDelegate);
    }
}
