package com.koch.ambeth.cache;

/*-
 * #%L
 * jambeth-cache
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

import com.koch.ambeth.cache.audit.IVerifyOnLoad;
import com.koch.ambeth.cache.config.CacheConfigurationConstants;
import com.koch.ambeth.cache.proxy.IPropertyChangeConfigurable;
import com.koch.ambeth.cache.rootcachevalue.IRootCacheValueFactory;
import com.koch.ambeth.cache.rootcachevalue.RootCacheValue;
import com.koch.ambeth.cache.service.ICacheRetriever;
import com.koch.ambeth.cache.transfer.LoadContainer;
import com.koch.ambeth.cache.transfer.ObjRelationResult;
import com.koch.ambeth.cache.util.IndirectValueHolderRef;
import com.koch.ambeth.event.IEventQueue;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.util.IImmutableTypeSet;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.CacheFactoryDirective;
import com.koch.ambeth.merge.cache.HandleContentDelegate;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.merge.cache.ICacheModification;
import com.koch.ambeth.merge.cache.ValueHolderState;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.merge.copy.IObjectCopier;
import com.koch.ambeth.merge.metadata.IObjRefFactory;
import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.merge.security.ISecurityScopeProvider;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.merge.util.DirectValueHolderRef;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.security.privilege.IPrivilegeProviderIntern;
import com.koch.ambeth.security.privilege.model.IPrivilege;
import com.koch.ambeth.service.IOfflineListener;
import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.ListUtil;
import com.koch.ambeth.util.LockState;
import com.koch.ambeth.util.ParamHolder;
import com.koch.ambeth.util.collections.AbstractHashSet;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IResizeMapCallback;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.util.collections.IntArrayList;
import com.koch.ambeth.util.collections.InterfaceFastList;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.factory.IEmptyArrayFactory;
import com.koch.ambeth.util.function.CheckedConsumer;
import com.koch.ambeth.util.io.FastByteArrayOutputStream;
import com.koch.ambeth.util.model.IDataObject;
import com.koch.ambeth.util.state.StateRollback;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class RootCache extends AbstractCache<RootCacheValue> implements IRootCache, IOfflineListener, ICacheRetriever {
    public static final String P_EVENT_QUEUE = "EventQueue";
    public static final Set<CacheDirective> failEarlyCacheValueResultSet = EnumSet.of(CacheDirective.FailEarly, CacheDirective.CacheValueResult);
    protected final HashMap<IObjRef, Integer> relationOris = new HashMap<>();
    protected final InterfaceFastList<RootCacheValue> lruList = new InterfaceFastList<>();
    protected final ReentrantLock lruLock = new ReentrantLock();
    @Autowired
    protected ICacheFactory cacheFactory;
    @Autowired
    protected ICacheModification cacheModification;
    @Autowired(optional = true)
    protected ICacheRetriever cacheRetriever;
    @Autowired
    protected IEmptyArrayFactory emptyArrayFactory;
    @Autowired(optional = true)
    protected IEventQueue eventQueue;
    @Autowired
    protected IImmutableTypeSet immutableTypeSet;
    @Autowired(optional = true)
    protected IObjectCopier objectCopier;
    @Autowired
    protected IObjRefFactory objRefFactory;
    @Autowired
    protected IObjRefHelper oriHelper;
    @Autowired
    protected IPrefetchHelper prefetchHelper;
    @Autowired(optional = true)
    protected IPrivilegeProviderIntern privilegeProvider;
    @Autowired
    protected IRootCacheValueFactory rootCacheValueFactory;
    @Autowired(optional = true)
    protected ISecurityActivation securityActivation;
    @Autowired(optional = true)
    protected ISecurityScopeProvider securityScopeProvider;
    @Autowired(optional = true)
    protected IVerifyOnLoad verifyOnLoad;
    @Property(name = CacheConfigurationConstants.CacheLruThreshold, defaultValue = "0")
    protected int lruThreshold;
    @Property(name = MergeConfigurationConstants.SecurityActive, defaultValue = "false")
    protected boolean securityActive;
    @Property(mandatory = false)
    protected boolean privileged;
    protected long relationObjRefsRefreshThrottleOnGC = 60000; // throttle refresh to at most 1 time
    protected long lastRelationObjRefsRefreshTime;
    @LogInstance
    private ILogger log;
    // per minute

    public RootCache() {
        relationOris.setResizeMapCallback(new DoRelationObjRefsRefreshOnResize(this));
    }

    @Override
    public void dispose() {
        cacheFactory = null;
        cacheModification = null;
        cacheRetriever = null;
        eventQueue = null;
        objectCopier = null;
        oriHelper = null;
        prefetchHelper = null;
        privilegeProvider = null;

        super.dispose();
    }

    @Override
    public boolean isPrivileged() {
        return privileged;
    }

    @Override
    public IRootCache getCurrentRootCache() {
        return this;
    }

    @Override
    public IRootCache getParent() {
        return cacheRetriever instanceof IRootCache ? (IRootCache) cacheRetriever : null;
    }

    @Override
    public int getCacheId() {
        return -1;
    }

    @Override
    public void setCacheId(int cacheId) {
        throw new UnsupportedOperationException();
    }

    @Property(name = CacheConfigurationConstants.SecondLevelCacheWeakActive, defaultValue = "true")
    @Override
    public void setWeakEntries(boolean weakEntries) {
        super.setWeakEntries(weakEntries);
    }

    @Override
    protected boolean allowCacheValueReplacement() {
        return true;
    }

    @Override
    public RootCacheValue createCacheValueInstance(IEntityMetaData metaData, Object obj) {
        return rootCacheValueFactory.createRootCacheValue(metaData);
    }

    @Override
    protected Object getIdOfCacheValue(IEntityMetaData metaData, RootCacheValue cacheValue) {
        return cacheValue.getId();
    }

    @Override
    protected void setIdOfCacheValue(IEntityMetaData metaData, RootCacheValue cacheValue, Object id) {
        cacheValue.setId(id);
    }

    @Override
    protected Object getVersionOfCacheValue(IEntityMetaData metaData, RootCacheValue cacheValue) {
        return cacheValue.getVersion();
    }

    @Override
    protected void setVersionOfCacheValue(IEntityMetaData metaData, RootCacheValue cacheValue, Object version) {
        Member versionMember = metaData.getVersionMember();
        if (versionMember == null) {
            return;
        }
        version = conversionHelper.convertValueToType(versionMember.getRealType(), version);
        cacheValue.setVersion(version);
    }

    @Override
    protected void setRelationsOfCacheValue(IEntityMetaData metaData, RootCacheValue cacheValue, Object[] primitives, IObjRef[][] relations) {
        cacheValue.setPrimitives(primitives);
        cacheValue.setRelations(relations);
    }

    protected boolean isCacheRetrieverCallAllowed(Set<CacheDirective> cacheDirective) {
        if (cacheRetriever == null) {
            // without a valid cacheRetriever a call is never allowed
            return false;
        }
        if (cacheDirective.contains(CacheDirective.FailEarly)) {
            // with FailEarly a cascading call is never allowed
            return false;
        }
        if (cacheDirective.contains(CacheDirective.FailInCacheHierarchy) && !(cacheRetriever instanceof IRootCache)) {
            // with FailInCacheHierarchy a cascading call is only allowed if the cacheRetriever is itself
            // an instance of IRootCache
            return false;
        }
        // in the end a call is only allowed if it is not forbidden for the current thread
        return !AbstractCache.isFailInCacheHierarchyModeActive();
    }

    @Override
    public IList<Object> getObjects(List<IObjRef> orisToGet, Set<CacheDirective> cacheDirective) {
        checkNotDisposed();
        if (orisToGet == null || orisToGet.isEmpty()) {
            return EmptyList.getInstance();
        }
        if (cacheDirective.contains(CacheDirective.NoResult) || cacheDirective.contains(CacheDirective.LoadContainerResult) || cacheDirective.contains(CacheDirective.CacheValueResult)) {
            return getObjects(orisToGet, null, cacheDirective);
        }
        ICacheIntern targetCache;
        if (privileged && securityActivation != null && !securityActivation.isFilterActivated()) {
            targetCache = (ICacheIntern) cacheFactory.createPrivileged(CacheFactoryDirective.SubscribeTransactionalDCE, "RootCache.ADHOC");
        } else {
            targetCache = (ICacheIntern) cacheFactory.create(CacheFactoryDirective.SubscribeTransactionalDCE, "RootCache.ADHOC");
        }
        return getObjects(orisToGet, targetCache, cacheDirective);
    }

    @Override
    public Object getObject(IObjRef oriToGet, ICacheIntern targetCache, Set<CacheDirective> cacheDirective) {
        checkNotDisposed();
        if (oriToGet == null) {
            return null;
        }
        var orisToGet = new ArrayList<IObjRef>(1);
        orisToGet.add(oriToGet);
        var objects = getObjects(orisToGet, targetCache, cacheDirective);
        if (objects.isEmpty()) {
            return null;
        }
        return objects.get(0);
    }

    @Override
    public IList<Object> getObjects(final List<IObjRef> orisToGet, final ICacheIntern targetCache, final Set<CacheDirective> cacheDirective) {
        var verifyOnLoad = this.verifyOnLoad;
        if (verifyOnLoad == null) {
            return getObjectsIntern(orisToGet, targetCache, cacheDirective);
        }
        return verifyOnLoad.verifyEntitiesOnLoad(() -> getObjectsIntern(orisToGet, targetCache, cacheDirective));
    }

    protected IList<Object> getObjectsIntern(List<IObjRef> orisToGet, ICacheIntern targetCache, Set<CacheDirective> cacheDirective) {
        checkNotDisposed();
        if (orisToGet == null || orisToGet.isEmpty()) {
            return EmptyList.getInstance();
        }
        if (cacheDirective == null) {
            cacheDirective = Collections.<CacheDirective>emptySet();
        }
        var isCacheRetrieverCallAllowed = isCacheRetrieverCallAllowed(cacheDirective);
        var eventQueue = this.eventQueue;
        var rollback = StateRollback.empty();
        if (eventQueue != null) {
            rollback = eventQueue.pause(this);
        }
        try {
            var readLock = getReadLock();
            var writeLock = getWriteLock();
            var cacheModification = this.cacheModification;
            var oldCacheModificationValue = cacheModification.isActive();
            var acquireSuccess = acquireHardRefTLIfNotAlready(orisToGet.size());
            if (!oldCacheModificationValue) {
                cacheModification.setActive(true);
            }
            try {
                if (!isCacheRetrieverCallAllowed) {
                    // if the cascading call is not allowed we need no pre-scanning for cache-misses
                    // we have to do our best while we create the result directly
                    readLock.lock();
                    try {
                        return createResult(orisToGet, null, cacheDirective, targetCache, true, null);
                    } finally {
                        readLock.unlock();
                    }
                }

                var lockState = writeLock.releaseAllLocks();
                var doAnotherRetry = new ParamHolder<Boolean>();
                try {
                    while (true) {
                        doAnotherRetry.setValue(Boolean.FALSE);
                        var neededObjRefs = new LinkedHashSet<IObjRef>();
                        var pendingValueHolders = new ArrayList<DirectValueHolderRef>();
                        var result = getObjectsRetry(orisToGet, targetCache, cacheDirective, doAnotherRetry, neededObjRefs, pendingValueHolders);
                        while (!neededObjRefs.isEmpty()) {
                            var objRefsToGetCascade = neededObjRefs.toList();
                            neededObjRefs.clear();
                            getObjectsRetry(objRefsToGetCascade, targetCache, cacheDirective, doAnotherRetry, neededObjRefs, pendingValueHolders);
                        }
                        if (Boolean.TRUE.equals(doAnotherRetry.getValue())) {
                            continue;
                        }
                        if (!pendingValueHolders.isEmpty()) {
                            prefetchHelper.prefetch(pendingValueHolders);
                            continue;
                        }
                        return result;
                    }
                } finally {
                    writeLock.reacquireLocks(lockState);
                }
            } finally {
                if (!oldCacheModificationValue) {
                    cacheModification.setActive(oldCacheModificationValue);
                }
                clearHardRefs(acquireSuccess);
            }
        } finally {
            rollback.rollback();
        }
    }

    protected IList<Object> getObjectsRetry(List<IObjRef> orisToGet, ICacheIntern targetCache, Set<CacheDirective> cacheDirective, ParamHolder<Boolean> doAnotherRetry,
            LinkedHashSet<IObjRef> neededObjRefs, ArrayList<DirectValueHolderRef> pendingValueHolders) {
        var orisToLoad = new ArrayList<IObjRef>();
        var readLock = getReadLock();
        var writeLock = getWriteLock();

        int cacheVersionBeforeLongTimeAction;
        readLock.lock();
        try {
            var result = createResult(orisToGet, null, cacheDirective, targetCache, false, orisToLoad);
            if (orisToLoad.isEmpty()) {
                return result;
            }
            cacheVersionBeforeLongTimeAction = changeVersion;
        } finally {
            readLock.unlock();
        }
        // }
        int cacheVersionAfterLongTimeAction;
        var releaseWriteLock = false;
        try {
            List<ILoadContainer> loadedEntities;
            var rollback = StateRollback.empty();
            if (privileged && securityActivation != null && securityActivation.isFilterActivated()) {
                rollback = securityActivation.pushWithoutFiltering();
            }
            try {
                loadedEntities = cacheRetriever.getEntities(orisToLoad);
            } finally {
                rollback.rollback();
            }

            // Acquire write lock and mark this state. In the finally-Block the writeLock
            // has to be released in a deterministic way
            var releasedState = writeLock.releaseAllLocks();
            try {
                writeLock.lock();
                releaseWriteLock = true;

                cacheVersionAfterLongTimeAction = changeVersion;
                loadObjects(loadedEntities, neededObjRefs, pendingValueHolders);

                orisToLoad.clear();

                if (!neededObjRefs.isEmpty() || !pendingValueHolders.isEmpty()) {
                    writeLock.unlock();
                    releaseWriteLock = false;
                    return null;
                }
            } finally {
                writeLock.reacquireLocks(releasedState);
            }
            if (cacheVersionAfterLongTimeAction != cacheVersionBeforeLongTimeAction) {
                // Another thread did some changes (possibly DataChange-Remove actions)
                // We have to ensure that our result-scope is still valid
                // We return null to allow a further full retry of getObjects()
                doAnotherRetry.setValue(Boolean.TRUE);
                return null;
            }
            // write lock may be acquired already. But this is ok with our custom R/W lock implementation
            readLock.lock();
            try {
                return createResult(orisToGet, null, cacheDirective, targetCache, false, null);
            } finally {
                readLock.unlock();
            }
        } finally {
            if (releaseWriteLock) {
                writeLock.unlock();
            }
        }
    }

    @Override
    public IList<IObjRelationResult> getObjRelations(List<IObjRelation> objRels, Set<CacheDirective> cacheDirective) {
        return getObjRelations(objRels, null, cacheDirective);
    }

    @Override
    public IList<IObjRelationResult> getObjRelations(final List<IObjRelation> objRels, final ICacheIntern targetCache, final Set<CacheDirective> cacheDirective) {
        IVerifyOnLoad verifyOnLoad = this.verifyOnLoad;
        if (verifyOnLoad == null) {
            return getObjRelationsIntern(objRels, targetCache, cacheDirective);
        }
        return verifyOnLoad.verifyEntitiesOnLoad(() -> getObjRelationsIntern(objRels, targetCache, cacheDirective));
    }

    protected IList<IObjRelationResult> getObjRelationsIntern(List<IObjRelation> objRels, ICacheIntern targetCache, Set<CacheDirective> cacheDirective) {
        checkNotDisposed();
        var isCacheRetrieverCallAllowed = isCacheRetrieverCallAllowed(cacheDirective);
        var returnMisses = cacheDirective.contains(CacheDirective.ReturnMisses);

        var eventQueue = this.eventQueue;
        var rollback = StateRollback.empty();
        if (eventQueue != null) {
            rollback = eventQueue.pause(this);
        }
        try {
            var readLock = getReadLock();
            var objRelMisses = new ArrayList<IObjRelation>();
            var objRelToResultMap = new HashMap<IObjRelation, IObjRelationResult>();
            var alreadyClonedObjRefs = new IdentityHashMap<IObjRef, IObjRef>();

            var cacheModification = this.cacheModification;
            var oldCacheModificationValue = cacheModification.isActive();
            var acquireSuccess = acquireHardRefTLIfNotAlready(objRels.size());
            cacheModification.setActive(true);
            try {
                IList<IObjRelationResult> result = null;
                readLock.lock();
                try {
                    for (int a = 0, size = objRels.size(); a < size; a++) {
                        var objRel = objRels.get(a);
                        if (targetCache != null && targetCache != this) {
                            var cacheResult = targetCache.getObjects(objRel.getObjRefs(), CacheDirective.failEarly());
                            if (!cacheResult.isEmpty()) {
                                IObjRefContainer item = (IObjRefContainer) cacheResult.get(0); // Only one hit is
                                // necessary of
                                // given group of
                                // objRefs
                                var relationIndex = item.get__EntityMetaData().getIndexByRelationName(objRel.getMemberName());
                                if (ValueHolderState.INIT == item.get__State(relationIndex) || item.get__ObjRefs(relationIndex) != null) {
                                    continue;
                                }
                            }
                        }
                        var selfResult = getObjRelationIfValid(objRel, targetCache, null, alreadyClonedObjRefs);
                        if (selfResult == null && isCacheRetrieverCallAllowed) {
                            objRelMisses.add(objRel);
                        }
                    }
                    if (objRelMisses.isEmpty()) {
                        // Create result WITHOUT releasing the readlock in the meantime
                        result = createResult(objRels, targetCache, null, alreadyClonedObjRefs, returnMisses);
                    }
                } finally {
                    readLock.unlock();
                }
                if (!objRelMisses.isEmpty()) {
                    List<IObjRelationResult> loadedObjectRelations;
                    var rollback2 = StateRollback.empty();
                    if (privileged && securityActivation != null && securityActivation.isFilterActivated()) {
                        rollback2 = securityActivation.pushWithoutFiltering();
                    }
                    try {
                        loadedObjectRelations = cacheRetriever.getRelations(objRelMisses);
                    } finally {
                        rollback2.rollback();
                    }
                    loadObjects(loadedObjectRelations, objRelToResultMap);
                    readLock.lock();
                    try {
                        result = createResult(objRels, targetCache, objRelToResultMap, alreadyClonedObjRefs, returnMisses);
                    } finally {
                        readLock.unlock();
                    }
                }
                if (isFilteringNecessary(targetCache)) {
                    writeLock.lock();
                    try {
                        result = filterObjRelResult(result, targetCache);
                    } finally {
                        writeLock.unlock();
                    }
                }
                return result;
            } finally {
                cacheModification.setActive(oldCacheModificationValue);
                clearHardRefs(acquireSuccess);
            }
        } finally {
            rollback.rollback();
        }
    }

    protected IObjRelationResult getObjRelationIfValid(IObjRelation objRel, ICacheIntern targetCache, HashMap<IObjRelation, IObjRelationResult> objRelToResultMap,
            IdentityHashMap<IObjRef, IObjRef> alreadyClonedObjRefs) {
        var cacheValues = getObjects(new ArrayList<IObjRef>(objRel.getObjRefs()), targetCache, failEarlyCacheValueResultSet);
        if (cacheValues.isEmpty()) {
            if (objRelToResultMap != null) {
                return objRelToResultMap.get(objRel);
            }
            return null;
        }
        var cacheValue = (RootCacheValue) cacheValues.get(0); // Only first hit is needed
        var metaData = entityMetaDataProvider.getMetaData(objRel.getRealType());
        var index = metaData.getIndexByRelationName(objRel.getMemberName());
        var objRefs = cacheValue.getRelation(index);

        if (objRefs == null) {
            return null;
        }
        var objRelResult = new ObjRelationResult();
        objRelResult.setReference(objRel);
        objRelResult.setRelations(cloneObjectRefArray(objRefs, alreadyClonedObjRefs));
        return objRelResult;
    }

    protected IList<IObjRelationResult> createResult(List<IObjRelation> objRels, ICacheIntern targetCache, HashMap<IObjRelation, IObjRelationResult> objRelToResultMap,
            IdentityHashMap<IObjRef, IObjRef> alreadyClonedObjRefs, boolean returnMisses) {
        var oriHelper = this.oriHelper;
        var objRelResults = new ArrayList<IObjRelationResult>(objRels.size());

        for (int a = 0, size = objRels.size(); a < size; a++) {
            var objRel = objRels.get(a);
            IList<Object> cacheResult = null;
            if (targetCache != null && targetCache != this) {
                cacheResult = targetCache.getObjects(objRel.getObjRefs(), CacheDirective.failEarly());
            }
            if (cacheResult == null || cacheResult.isEmpty()) {
                IObjRelationResult selfResult = getObjRelationIfValid(objRel, targetCache, objRelToResultMap, alreadyClonedObjRefs);
                if (selfResult != null || returnMisses) {
                    objRelResults.add(selfResult);
                }
                continue;
            }
            var item = (IObjRefContainer) cacheResult.get(0); // Only first hit is needed
            var metaData = item.get__EntityMetaData();
            var relationIndex = metaData.getIndexByRelationName(objRel.getMemberName());
            var member = metaData.getRelationMembers()[relationIndex];

            if (ValueHolderState.INIT != item.get__State(relationIndex)) {
                var objRefs = item.get__ObjRefs(relationIndex);
                if (objRefs != null) {
                    var selfResult = new ObjRelationResult();
                    selfResult.setReference(objRel);
                    selfResult.setRelations(cloneObjectRefArray(objRefs, alreadyClonedObjRefs));
                    objRelResults.add(selfResult);
                } else {
                    var selfResult = getObjRelationIfValid(objRel, targetCache, objRelToResultMap, alreadyClonedObjRefs);
                    if (selfResult != null) {
                        var relations = selfResult.getRelations();
                        item.set__ObjRefs(relationIndex, relations);
                        objRelResults.add(selfResult);
                    } else if (returnMisses) {
                        objRelResults.add(null);
                    }
                }
                continue;
            }
            var memberValue = member.getValue(item);
            if (memberValue == null) {
                if (returnMisses) {
                    objRelResults.add(null);
                }
                continue;
            }
            var oriList = oriHelper.extractObjRefList(memberValue, null);

            var selfResult = new ObjRelationResult();
            selfResult.setReference(objRel);
            selfResult.setRelations(oriList.toArray(IObjRef.class));
            objRelResults.add(selfResult);
        }
        return objRelResults;
    }

    protected IList<IObjRelationResult> filterObjRelResult(IList<IObjRelationResult> objRelResults, ICacheIntern targetCache) {
        if (objRelResults.isEmpty() || !isFilteringNecessary(targetCache)) {
            return objRelResults;
        }
        var permittedObjRefs = new ArrayList<IObjRef>(objRelResults.size());
        for (int a = 0, size = objRelResults.size(); a < size; a++) {
            var objRelResult = objRelResults.get(a);
            if (objRelResult == null) {
                permittedObjRefs.add(null);
                continue;
            }
            var objRefsOfReference = objRelResult.getReference().getObjRefs();
            var primaryObjRef = objRefsOfReference[0];
            for (var objRefOfReference : objRefsOfReference) {
                if (objRefOfReference.getIdNameIndex() == ObjRef.PRIMARY_KEY_INDEX) {
                    primaryObjRef = objRefOfReference;
                    break;
                }
            }
            permittedObjRefs.add(primaryObjRef);
        }
        var privileges = getPrivilegesByObjRefWithoutReadLock(permittedObjRefs);
        var relatedObjRefs = new HashMap<IObjRef, IntArrayList>();
        for (int index = permittedObjRefs.size(); index-- > 0; ) {
            var privilege = privileges[index];
            if (privilege == null || !privilege.isReadAllowed()) {
                permittedObjRefs.set(index, null);
                continue;
            }
            var objRelResult = objRelResults.get(index);
            var relations = objRelResult.getRelations();
            for (var relation : relations) {
                var intArrayList = relatedObjRefs.get(relation);
                if (intArrayList == null) {
                    intArrayList = new IntArrayList();
                    relatedObjRefs.put(relation, intArrayList);
                }
                intArrayList.add(index);
            }
        }
        var relatedObjRefKeys = relatedObjRefs.keySet().toList();
        privileges = getPrivilegesByObjRefWithoutReadLock(relatedObjRefKeys);
        for (int a = 0, size = relatedObjRefKeys.size(); a < size; a++) {
            var privilege = privileges[a];
            if (privilege.isReadAllowed()) {
                continue;
            }
            var relatedObjRefKey = relatedObjRefKeys.get(a);
            var intArrayList = relatedObjRefs.get(relatedObjRefKey);
            for (int b = 0, sizeB = intArrayList.size; b < sizeB; b++) {
                var index = intArrayList.array[b];
                var objRelResult = objRelResults.get(index);
                var relations = objRelResult.getRelations();
                var found = false;
                for (int c = relations.length; c-- > 0; ) {
                    if (relations[c] != relatedObjRefKey) {
                        continue;
                    }
                    relations[c] = null;
                    found = true;
                    break;
                }
                if (!found) {
                    throw new IllegalStateException("Must never happen");
                }
            }
        }
        for (int a = objRelResults.size(); a-- > 0; ) {
            var objRelResult = objRelResults.get(a);
            if (objRelResult == null) {
                continue;
            }
            var relations = objRelResult.getRelations();
            int count = 0;
            for (int b = relations.length; b-- > 0; ) {
                if (relations[b] != null) {
                    count++;
                }
            }
            if (count == relations.length) {
                continue;
            }
            var filteredRelations = count > 0 ? new IObjRef[count] : IObjRef.EMPTY_ARRAY;
            int index = 0;
            for (int b = relations.length; b-- > 0; ) {
                var relation = relations[b];
                if (relation != null) {
                    filteredRelations[index++] = relation;
                }
            }
            if (index != count) {
                throw new IllegalStateException("Must never happen");
            }
            ((ObjRelationResult) objRelResult).setRelations(filteredRelations);
        }
        return objRelResults;
    }

    protected IObjRef[] cloneObjectRefArray(IObjRef[] objRefs, IdentityHashMap<IObjRef, IObjRef> alreadyClonedObjRefs) {
        if (objRefs == null || objRefs.length == 0) {
            return objRefs;
        }
        // Deep clone of the ObjRefs is important
        var objRefsClone = new IObjRef[objRefs.length];
        for (int b = objRefs.length; b-- > 0; ) {
            var objRef = objRefs[b];
            if (objRef == null) {
                continue;
            }
            var objRefClone = alreadyClonedObjRefs.get(objRef);
            if (objRefClone == null) {
                objRefClone = objRefFactory.dup(objRef);
                alreadyClonedObjRefs.put(objRef, objRefClone);
            }
            objRefsClone[b] = objRefClone;
        }
        return objRefsClone;
    }

    protected void loadObjects(List<IObjRelationResult> loadedObjectRelations, HashMap<IObjRelation, IObjRelationResult> objRelToResultMap) {
        var entityMetaDataProvider = this.entityMetaDataProvider;
        var writeLock = getWriteLock();
        writeLock.lock();
        try {
            for (int a = 0, size = loadedObjectRelations.size(); a < size; a++) {
                var objRelResult = loadedObjectRelations.get(a);
                var objRel = objRelResult.getReference();

                objRelToResultMap.put(objRel, objRelResult);

                var cacheValues = getObjects(objRel.getObjRefs(), CacheDirective.cacheValueResult());
                if (cacheValues.isEmpty()) {
                    continue;
                }
                var cacheValue = (RootCacheValue) cacheValues.get(0); // Only first hit needed

                var metaData = entityMetaDataProvider.getMetaData(objRel.getRealType());
                var index = metaData.getIndexByRelationName(objRel.getMemberName());
                unregisterRelations(cacheValue.getRelation(index), cacheValue);
                var relationsOfMember = objRelResult.getRelations();
                if (relationsOfMember.length == 0) {
                    relationsOfMember = IObjRef.EMPTY_ARRAY;
                }
                cacheValue.setRelation(index, relationsOfMember);
                registerRelations(relationsOfMember);
            }
        } finally {
            writeLock.unlock();
        }
    }

    protected void loadObjects(List<ILoadContainer> loadedEntities, LinkedHashSet<IObjRef> neededORIs, ArrayList<DirectValueHolderRef> pendingValueHolders) {
        var writeLock = getWriteLock();
        writeLock.lock();
        try {
            for (int a = 0, size = loadedEntities.size(); a < size; a++) {
                loadObject(loadedEntities.get(a), neededORIs, pendingValueHolders);
            }
        } finally {
            writeLock.unlock();
        }
    }

    protected void loadObject(ILoadContainer loadContainer, LinkedHashSet<IObjRef> neededORIs, ArrayList<DirectValueHolderRef> pendingValueHolders) {
        var reference = loadContainer.getReference();

        var metaData = entityMetaDataProvider.getMetaData(reference.getRealType());
        var primitives = loadContainer.getPrimitives();
        var alternateCacheKeys = extractAlternateCacheKeys(metaData, primitives);

        var cacheValue = putIntern(metaData, null, reference.getId(), reference.getVersion(), alternateCacheKeys, primitives, loadContainer.getRelations());
        if (weakEntries) {
            addHardRefTL(cacheValue);
        }
        if (pendingValueHolders != null) {
            ensureRelationsExist(cacheValue, metaData, neededORIs, pendingValueHolders);
        }
    }

    @Override
    protected void putIntern(ILoadContainer loadContainer) {
        loadObject(loadContainer, null, null);
    }

    protected boolean isFilteringNecessary(ICacheIntern targetCache) {
        return privilegeProvider != null && securityActive &&
                ((isPrivileged() && targetCache != null && !targetCache.isPrivileged()) || (targetCache == null && securityActivation != null && securityActivation.isFilterActivated()));
    }

    protected IList<Object> createResult(List<IObjRef> objRefsToGet, RootCacheValue[] rootCacheValuesToGet, Set<CacheDirective> cacheDirective, ICacheIntern targetCache, boolean checkVersion,
            List<IObjRef> objRefsToLoad) {
        var loadContainerResult = cacheDirective.contains(CacheDirective.LoadContainerResult);
        var cacheValueResult = cacheDirective.contains(CacheDirective.CacheValueResult) || targetCache == this;
        if (targetCache == null && !loadContainerResult && !cacheValueResult) {
            return null;
        }
        var entityMetaDataProvider = this.entityMetaDataProvider;
        var returnMisses = cacheDirective.contains(CacheDirective.ReturnMisses);

        var targetCacheAccess = !loadContainerResult && !cacheValueResult;
        var filteringNecessary = isFilteringNecessary(targetCache);
        var getCount = objRefsToGet.size();
        IPrivilege[] privilegesOfObjRefsToGet = null;
        if (filteringNecessary) {
            var privileges = getPrivilegesByObjRefWithoutReadLock(objRefsToGet);
            var filteredObjRefsToGet = new ArrayList<IObjRef>(objRefsToGet.size());
            privilegesOfObjRefsToGet = new IPrivilege[objRefsToGet.size()];
            var filteredRootCacheValuesToGet = rootCacheValuesToGet != null ? new RootCacheValue[objRefsToGet.size()] : null;
            getCount = 0;
            for (int a = 0, size = objRefsToGet.size(); a < size; a++) {
                var privilege = privileges[a];
                if (privilege != null && privilege.isReadAllowed()) {
                    getCount++;
                    filteredObjRefsToGet.add(objRefsToGet.get(a));
                    privilegesOfObjRefsToGet[a] = privilege;
                    if (rootCacheValuesToGet != null) {
                        filteredRootCacheValuesToGet[a] = rootCacheValuesToGet[a];
                    }
                } else {
                    filteredObjRefsToGet.add(null);
                }
            }
            rootCacheValuesToGet = filteredRootCacheValuesToGet;
            objRefsToGet = filteredObjRefsToGet;
        }
        if (getCount == 0) {
            return new ArrayList<>(0);
        }
        var eventQueue = this.eventQueue;
        var rollback = StateRollback.empty();
        if (targetCacheAccess && eventQueue != null) {
            rollback = eventQueue.pause(targetCache);
        }
        try {
            var result = new ArrayList<>(objRefsToGet.size());
            ArrayList<CheckedConsumer<IdentityHashSet<IObjRef>>> runnables = null;
            ArrayList<IObjRef> tempObjRefList = null;
            IdentityHashMap<IObjRef, IObjRef> alreadyClonedObjRefs = null;
            IdentityHashSet<IObjRef> greyListObjRefs = null;
            for (int a = 0, size = objRefsToGet.size(); a < size; a++) {
                var objRefToGet = objRefsToGet.get(a);
                if (objRefToGet == null) {
                    if (returnMisses) {
                        result.add(null);
                    }
                    continue;
                }
                var metaData = entityMetaDataProvider.getMetaData(objRefToGet.getRealType());

                var cacheValue = rootCacheValuesToGet != null ? rootCacheValuesToGet[a] : getCacheValue(metaData, objRefToGet, checkVersion);
                if (cacheValue == null) {
                    // Cache miss
                    if (targetCacheAccess) {
                        var cacheHitObject = targetCache.getObject(objRefToGet, targetCache, CacheDirective.failEarly());
                        if (cacheHitObject != null) {
                            result.add(cacheHitObject);
                            continue;
                        }
                    }
                    if (returnMisses) {
                        result.add(null);
                    }
                    if (objRefsToLoad != null && !(objRefToGet instanceof IDirectObjRef)) {
                        objRefsToLoad.add(objRefToGet);
                    }
                    // But we already loaded before so we can do nothing now
                    continue;
                }
                if (loadContainerResult) {
                    var relations = cacheValue.getRelations();
                    var loadContainer = new LoadContainer();
                    loadContainer.setReference(objRefFactory.createObjRef(cacheValue));
                    loadContainer.setPrimitives(cacheValue.getPrimitives());

                    if (relations.length == 0 || !filteringNecessary) {
                        loadContainer.setRelations(relations);
                        result.add(loadContainer);
                        continue;
                    }
                    if (runnables == null) {
                        runnables = new ArrayList<>(size);
                        greyListObjRefs = new IdentityHashSet<>();
                        alreadyClonedObjRefs = new IdentityHashMap<>();
                        tempObjRefList = new ArrayList<>();
                    }
                    scanForAllKnownRelations(relations, greyListObjRefs);

                    var fTempObjRefList = tempObjRefList;
                    var fAlreadyClonedObjRefs = alreadyClonedObjRefs;
                    runnables.add(whiteListObjRefs -> {
                        var whiteListedRelations = filterRelations(relations, whiteListObjRefs, fTempObjRefList);
                        for (int b = whiteListedRelations.length; b-- > 0; ) {
                            whiteListedRelations[b] = cloneObjectRefArray(whiteListedRelations[b], fAlreadyClonedObjRefs);
                        }
                        loadContainer.setRelations(whiteListedRelations);
                    });
                    result.add(loadContainer);
                } else if (cacheValueResult) {
                    result.add(cacheValue);
                } else {
                    if (tempObjRefList == null) {
                        tempObjRefList = new ArrayList<>(1);
                        tempObjRefList.add(new ObjRef());
                    }
                    Object cacheHitObject =
                            createObjectFromScratch(metaData, cacheValue, targetCache, tempObjRefList, filteringNecessary, privilegesOfObjRefsToGet != null ? privilegesOfObjRefsToGet[a] : null);
                    result.add(cacheHitObject);
                }
            }
            if (runnables != null) {
                var whiteListObjRefs = buildWhiteListedObjRefs(greyListObjRefs);
                for (int a = runnables.size(); a-- > 0; ) {
                    var runnable = runnables.get(a);
                    CheckedConsumer.invoke(runnable, whiteListObjRefs);
                }
            }
            return result;
        } finally {
            rollback.rollback();
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Object createObjectFromScratch(IEntityMetaData metaData, RootCacheValue cacheValue, ICacheIntern targetCache, ArrayList<IObjRef> tempObjRefList, boolean filteringNecessary,
            IPrivilege privilegeOfObjRef) {
        var entityType = cacheValue.getEntityType();

        var tempObjRef = tempObjRefList.get(0);
        tempObjRef.setId(cacheValue.getId());
        tempObjRef.setIdNameIndex(ObjRef.PRIMARY_KEY_INDEX);
        tempObjRef.setRealType(entityType);

        var targetWriteLock = targetCache.getWriteLock();
        targetWriteLock.lock();
        try {
            var cacheObject = targetCache.getObjects(tempObjRefList, CacheDirective.failEarlyAndReturnMisses()).get(0);
            if (cacheObject != null) {
                // this flag is used from the CacheDataChangeListener to give the cache layers a hint that
                // their are currently in "DCE-processing" mode
                if (!AbstractCache.isFailInCacheHierarchyModeActive() && !((IDataObject) cacheObject).hasPendingChanges()) {
                    var secondLevelVersion = getVersionOfCacheValue(metaData, cacheValue);
                    var versionMember = metaData.getVersionMember();
                    var firstLevelVersion = versionMember != null ? versionMember.getValue(cacheObject) : null;

                    // the secondLevelVersion (childCache) must not be ==null if the firstLevelVersion is
                    // !=null. So we intentionally provoke an NPE here
                    if (firstLevelVersion == null || ((Comparable) secondLevelVersion).compareTo(firstLevelVersion) > 0) {
                        updateExistingObject(metaData, cacheValue, cacheObject, targetCache, filteringNecessary, privilegeOfObjRef);
                    }
                }
                return cacheObject;
            }
            cacheObject = targetCache.createCacheValueInstance(metaData, null);
            IPropertyChangeConfigurable pcc = null;
            if (cacheObject instanceof IPropertyChangeConfigurable) {
                // we deactivate the current PCE processing because we just created the entity
                // we know that there is no property change listener that might handle the initial PCEs
                pcc = (IPropertyChangeConfigurable) cacheObject;
                pcc.set__PropertyChangeActive(false);
            }
            updateExistingObject(metaData, cacheValue, cacheObject, targetCache, filteringNecessary, privilegeOfObjRef);
            if (pcc != null) {
                pcc.set__PropertyChangeActive(true);
            }
            metaData.postLoad(cacheObject);
            return cacheObject;
        } finally {
            targetWriteLock.unlock();
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void updateExistingObject(IEntityMetaData metaData, RootCacheValue cacheValue, Object obj, ICacheIntern targetCache, boolean filteringNecessary, IPrivilege privilegeOfObjRef) {
        var conversionHelper = this.conversionHelper;
        var objectCopier = this.objectCopier;
        var privilegeProvider = this.privilegeProvider;
        var id = cacheValue.getId();
        var version = cacheValue.getVersion();
        metaData.getIdMember().setValue(obj, id);
        if (obj instanceof IParentCacheValueHardRef) {
            ((IParentCacheValueHardRef) obj).setParentCacheValueHardRef(cacheValue);
        }
        var versionMember = metaData.getVersionMember();
        if (versionMember != null) {
            versionMember.setValue(obj, version);
        }
        var primitiveMembers = metaData.getPrimitiveMembers();

        for (int primitiveIndex = primitiveMembers.length; primitiveIndex-- > 0; ) {
            var primitiveMember = primitiveMembers[primitiveIndex];

            Object primitiveTemplate = null;
            if (!filteringNecessary) {
                primitiveTemplate = cacheValue.getPrimitive(primitiveIndex);
            } else {
                if (privilegeOfObjRef == null) {
                    privilegeOfObjRef = privilegeProvider.getPrivilegeByObjRef(new ObjRef(metaData.getEntityType(), ObjRef.PRIMARY_KEY_INDEX, id, version));
                }
                if (privilegeOfObjRef.getPrimitivePropertyPrivilege(primitiveIndex).isReadAllowed()) {
                    // current user has no permission to read the property of the given entity
                    // so we treat this case as if the property is null/empty anyway
                    // effectively we handle user-specific data-blinding this way
                    primitiveTemplate = cacheValue.getPrimitive(primitiveIndex);
                }
            }

            if (primitiveTemplate != null && filteringNecessary) {
                if (privilegeOfObjRef == null) {
                    privilegeOfObjRef = getPrivilegeByObjRefWithoutReadLock(new ObjRef(metaData.getEntityType(), ObjRef.PRIMARY_KEY_INDEX, id, version));
                }
                if (!privilegeOfObjRef.getPrimitivePropertyPrivilege(primitiveIndex).isReadAllowed()) {
                    // current user has no permission to read the property of the given entity
                    // so we treat this case as if the property is null/empty anyway
                    // effectively we handle user-specific data-blinding this way
                    primitiveTemplate = null;
                }
            }
            Object primitive = null;

            var memberType = primitiveMember.getRealType();
            if (Collection.class.isAssignableFrom(memberType)) {
                Collection existingCollection = (Collection) primitiveMember.getValue(obj, false);
                if (existingCollection != null) {
                    existingCollection.clear();
                    if (primitiveTemplate == null) {
                        // intended blank
                    } else if (objectCopier != null) {
                        primitive = objectCopier.clone(primitiveTemplate);
                        primitive = conversionHelper.convertValueToType(memberType, primitive);
                        existingCollection.addAll((Collection) primitive);
                    } else {
                        primitive = createPrimitiveFromTemplate(memberType, primitiveTemplate);
                        existingCollection.addAll((Collection) primitive);
                    }
                    primitive = existingCollection;
                }
            }
            if (Optional.class.isAssignableFrom(memberType)) {
                if (primitiveTemplate == null) {
                    // intended blank
                } else if (objectCopier != null) {
                    primitive = objectCopier.clone(primitiveTemplate);
                    primitive = conversionHelper.convertValueToType(memberType, primitive);
                } else {
                    primitive = createPrimitiveFromTemplate(memberType, primitiveTemplate);
                }
                if (primitive == null) {
                    primitive = Optional.empty();
                }
                primitiveMember.setValue(obj, primitive);
            } else if (primitive == null) {
                if (primitiveTemplate == null) {
                    if (Collection.class.isAssignableFrom(memberType)) {
                        primitive = ListUtil.createObservableCollectionOfType(memberType, 0);
                    } else {
                        primitive = null;
                    }
                } else if (objectCopier != null) {
                    primitive = objectCopier.clone(primitiveTemplate);
                    primitive = conversionHelper.convertValueToType(memberType, primitive);
                } else {
                    primitive = createPrimitiveFromTemplate(memberType, primitiveTemplate);
                }
                primitiveMember.setValue(obj, primitive);
            }
            if (primitive instanceof IParentEntityAware) {
                ((IParentEntityAware) primitive).setParentEntity(obj, primitiveMember);
            }
        }
        var relations = cacheValue.getRelations();
        relations = filterRelations(relations, filteringNecessary);
        targetCache.addDirect(metaData, id, version, obj, cacheValue, relations);
    }

    protected IPrivilege getPrivilegeByObjRefWithoutReadLock(IObjRef objRef) {
        var readLock = getReadLock();
        LockState lockState = null;
        if (privileged && !readLock.isWriteLockHeld() && readLock.isReadLockHeld()) {
            // release the read lock because the PrivilegeProvider MAY request write lock on the
            // privileged cache during rule evaluation
            lockState = readLock.releaseAllLocks();
        }
        try {
            return privilegeProvider.getPrivilegeByObjRef(objRef);
        } finally {
            if (lockState != null) {
                readLock.reacquireLocks(lockState);
            }
        }
    }

    protected IPrivilege[] getPrivilegesByObjRefWithoutReadLock(List<? extends IObjRef> objRefs) {
        var readLock = getReadLock();
        LockState lockState = null;
        if (privileged && !readLock.isWriteLockHeld() && readLock.isReadLockHeld()) {
            // release the read lock because the PrivilegeProvider MAY request write lock on the
            // privileged cache during rule evaluation
            lockState = readLock.releaseAllLocks();
        }
        try {
            return privilegeProvider.getPrivilegesByObjRef(objRefs).getPrivileges();
        } finally {
            if (lockState != null) {
                readLock.reacquireLocks(lockState);
            }
        }
    }

    protected void scanForAllKnownRelations(IObjRef[][] relations, IdentityHashSet<IObjRef> allKnownRelations) {
        for (int a = relations.length; a-- > 0; ) {
            var relationsOfMember = relations[a];
            if (relationsOfMember == null) {
                continue;
            }
            for (var relationOfMember : relationsOfMember) {
                if (relationOfMember == null) {
                    continue;
                }
                allKnownRelations.add(relationOfMember);
            }
        }
    }

    protected IdentityHashSet<IObjRef> buildWhiteListedObjRefs(IdentityHashSet<IObjRef> greyListObjRefs) {
        var greyList = greyListObjRefs.toList();
        var whiteListObjRefs = IdentityHashSet.<IObjRef>create(greyList.size());
        var privileges = getPrivilegesByObjRefWithoutReadLock(greyList);
        for (int a = privileges.length; a-- > 0; ) {
            var privilege = privileges[a];
            if (privilege.isReadAllowed()) {
                whiteListObjRefs.add(greyList.get(a));
            }
        }
        return whiteListObjRefs;
    }

    protected IObjRef[][] filterRelations(IObjRef[][] relations, boolean filteringNecessary) {
        if (relations.length == 0 || !filteringNecessary) {
            return relations;
        }
        var allKnownRelations = new IdentityHashSet<IObjRef>();
        scanForAllKnownRelations(relations, allKnownRelations);
        if (allKnownRelations.isEmpty()) {
            // nothing to filter
            return relations;
        }
        var whiteListObjRefs = buildWhiteListedObjRefs(allKnownRelations);
        return filterRelations(relations, whiteListObjRefs, null);
    }

    protected IObjRef[][] filterRelations(IObjRef[][] relations, IdentityHashSet<IObjRef> whiteListObjRefs, ArrayList<IObjRef> tempList) {
        var filteredRelations = new IObjRef[relations.length][];

        if (tempList == null) {
            tempList = new ArrayList<>();
        }
        // reuse list instance for performance reasons
        for (int a = relations.length; a-- > 0; ) {
            var relationsOfMember = relations[a];
            if (relationsOfMember == null) {
                continue;
            }
            tempList.clear();
            for (var relationOfMember : relationsOfMember) {
                if (relationOfMember == null) {
                    continue;
                }
                if (whiteListObjRefs.contains(relationOfMember)) {
                    tempList.add(relationOfMember);
                }
            }
            filteredRelations[a] = !tempList.isEmpty() ? tempList.toArray(IObjRef.class) : IObjRef.EMPTY_ARRAY;
        }
        return filteredRelations;
    }

    protected Object createPrimitiveFromTemplate(Class<?> expectedType, Object primitiveTemplate) {
        if (expectedType.isArray()) {
            // Deep clone non-empty arrays because they are not immutable like other primitive items
            var componentType = expectedType.getComponentType();
            if (primitiveTemplate == null) {
                return createArray(componentType, 0);
            } else if (primitiveTemplate.getClass().isArray()) {
                var length = Array.getLength(primitiveTemplate);
                if (length == 0) {
                    if (primitiveTemplate.getClass().getComponentType().equals(componentType)) {
                        // At this point an 'immutable' empty array template may be returned directly
                        return primitiveTemplate;
                    } else {
                        return createArray(componentType, 0);
                    }
                }
                return copyByValue(primitiveTemplate);
            }
            var primitive = Array.newInstance(componentType, 1);
            Array.set(primitive, 0, primitiveTemplate);
            return primitive;
        } else if (primitiveTemplate != null && expectedType.isAssignableFrom(primitiveTemplate.getClass())) {
            // The template itself matches with the expected type. All we have to do is clone the template
            return copyByValue(primitiveTemplate);
        } else if (Optional.class.isAssignableFrom(expectedType)) {
            return Optional.ofNullable(primitiveTemplate);
        } else if (Collection.class.isAssignableFrom(expectedType)) {
            // Deep clone collections because they are not immutable like other primitive items
            if (primitiveTemplate == null) {
                return ListUtil.createCollectionOfType(expectedType, 0);
            }
            if (primitiveTemplate.getClass().isArray()) {
                var length = Array.getLength(primitiveTemplate);
                var primitive = ListUtil.createObservableCollectionOfType(expectedType, length);
                if (length == 0) {
                    return primitive;
                }
                // Clone template to access its ITEMS by REFERENCE
                primitiveTemplate = copyByValue(primitiveTemplate);
                for (int a = 0; a < length; a++) {
                    var item = Array.get(primitiveTemplate, a);
                    primitive.add(item);
                }
                return primitive;
            } else if (primitiveTemplate instanceof Collection) {
                var length = ((Collection<?>) primitiveTemplate).size();
                var primitive = ListUtil.createCollectionOfType(expectedType, length);
                if (length == 0) {
                    return primitive;
                }
                // Clone template to access its ITEMS by REFERENCE
                primitiveTemplate = copyByValue(primitiveTemplate);
                if (primitiveTemplate instanceof List) {
                    var listPrimitiveTemplate = (List<?>) primitiveTemplate;
                    for (int a = 0; a < length; a++) {
                        var item = listPrimitiveTemplate.get(a);
                        primitive.add(item);
                    }
                } else {
                    primitive.addAll((Collection<?>) primitiveTemplate);
                }
                return primitive;
            }
            var primitive = ListUtil.createCollectionOfType(expectedType, 1);
            primitive.add(copyByValue(primitiveTemplate));
            return primitive;
        } else if (primitiveTemplate == null) {
            return null;
        }
        var convertedPrimitiveTemplate = conversionHelper.convertValueToType(expectedType, primitiveTemplate);
        if (convertedPrimitiveTemplate != primitiveTemplate) {
            return convertedPrimitiveTemplate;
        }
        // To be sure, that the conversion has really no relation with the original at all, we clone it
        return copyByValue(convertedPrimitiveTemplate);
    }

    protected Object createArray(Class<?> componentType, int size) {
        if (size == 0) {
            return emptyArrayFactory.createSharedEmptyArray(componentType);
        }
        return Array.newInstance(componentType, size);
    }

    @SneakyThrows
    protected Object copyByValue(Object obj) {
        var type = obj.getClass();
        if (immutableTypeSet.isImmutableType(type)) {
            return obj;
        }
        // VERY SLOW fallback if no IObjectCopier implementation provided
        var bos = new FastByteArrayOutputStream();
        @SuppressWarnings("resource") var oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.flush();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.getRawByteArray(), 0, bos.size());
        ObjectInputStream ois = new ObjectInputStream(bis);
        return ois.readObject();
    }

    protected Collection<Object> createCollectionIfRequested(Class<?> expectedType, int size) {
        if (Set.class.isAssignableFrom(expectedType)) {
            return new HashSet<>((int) (size / AbstractHashSet.DEFAULT_LOAD_FACTOR) + 1, AbstractHashSet.DEFAULT_LOAD_FACTOR);
        } else if (Collection.class.isAssignableFrom(expectedType)) {
            return new ArrayList<>(size);
        }
        return null;
    }

    @Override
    public void addDirect(IEntityMetaData metaData, Object id, Object version, Object primitiveFilledObject, Object parentCacheValueOrArray, IObjRef[][] relations) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    protected void cacheValueHasBeenAdded(byte idIndex, Object id, IEntityMetaData metaData, Object[] primitives, IObjRef[][] relations, Object cacheValueR) {
        super.cacheValueHasBeenAdded(idIndex, id, metaData, primitives, relations, cacheValueR);

        registerAllRelations(relations);
    }

    @Override
    protected void cacheValueHasBeenRead(Object cacheValueR) {
        super.cacheValueHasBeenRead(cacheValueR);
        var lruThreshold = this.lruThreshold;
        if (lruThreshold == 0) {
            // LRU handling disabled
            return;
        }
        var cacheValue = getCacheValueFromReference(cacheValueR);
        if (cacheValue == null) {
            return;
        }
        var lruList = this.lruList;
        var lruLock = this.lruLock;
        lruLock.lock();
        try {
            lruList.remove(cacheValue);
            lruList.pushFirst(cacheValue);
            while (lruList.size() > lruThreshold) {
                lruList.popLast(); // Ignore result
            }
        } finally {
            lruLock.unlock();
        }
    }

    @Override
    protected void cacheValueHasBeenUpdated(IEntityMetaData metaData, Object[] primitives, IObjRef[][] relations, Object cacheValueR) {
        super.cacheValueHasBeenUpdated(metaData, primitives, relations, cacheValueR);

        var relationMembers = metaData.getRelationMembers();
        var cacheValue = getCacheValueFromReference(cacheValueR);
        for (int relationIndex = relationMembers.length; relationIndex-- > 0; ) {
            unregisterRelations(cacheValue.getRelation(relationIndex), cacheValue);
        }
        registerAllRelations(relations);
    }

    @Override
    protected void cacheValueHasBeenRemoved(IEntityMetaData metaData, byte idIndex, Object id, RootCacheValue cacheValue) {
        var relationMembers = metaData.getRelationMembers();
        for (int relationIndex = relationMembers.length; relationIndex-- > 0; ) {
            unregisterRelations(cacheValue.getRelation(relationIndex), cacheValue);
        }
        if (lruThreshold == 0) {
            // LRU handling disabled
            return;
        }
        var lruLock = this.lruLock;
        lruLock.lock();
        try {
            // Item in lru list
            lruList.remove(cacheValue);
        } finally {
            lruLock.unlock();
        }
        super.cacheValueHasBeenRemoved(metaData, idIndex, id, cacheValue);
    }

    @Override
    public void removePriorVersions(List<IObjRef> oris) {
        var writeLock = getWriteLock();
        writeLock.lock();
        try {
            for (int a = oris.size(); a-- > 0; ) {
                var ori = oris.get(a);
                super.removePriorVersions(ori);
                updateReferenceVersion(ori);
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void removePriorVersions(IObjRef ori) {
        var writeLock = getWriteLock();
        writeLock.lock();
        try {
            super.removePriorVersions(ori);

            updateReferenceVersion(ori);
        } finally {
            writeLock.unlock();
        }
    }

    protected void registerAllRelations(IObjRef[][] relations) {
        if (relations == null) {
            return;
        }
        for (IObjRef[] methodRelations : relations) {
            registerRelations(methodRelations);
        }
    }

    protected void registerRelations(IObjRef[] relations) {
        if (relations == null) {
            return;
        }
        var relationOris = this.relationOris;
        for (int i = relations.length; i-- > 0; ) {
            var related = relations[i];
            if (related == null) {
                continue;
            }
            var existing = relationOris.getKey(related);
            if (existing != null) {
                var count = relationOris.get(existing);
                relationOris.put(existing, Integer.valueOf(count.intValue() + 1));

                relations[i] = existing;
            } else {
                relationOris.put(related, Integer.valueOf(1));
            }
        }
    }

    protected void unregisterAllRelations(IObjRef[][] relations) {
        if (relations == null) {
            return;
        }
        for (var methodRelations : relations) {
            unregisterRelations(methodRelations, null);
        }
    }

    protected void unregisterRelations(IObjRef[] relations, RootCacheValue cacheValue) {
        if (relations == null) {
            return;
        }
        var relationOris = this.relationOris;
        for (int i = relations.length; i-- > 0; ) {
            var related = relations[i];
            var count = relationOris.get(related);
            if (count == null) {
                if (log.isWarnEnabled()) {
                    log.warn("Potential inconsistency in RootCache: ObjRef unknown: '" + related + "' used in '" + cacheValue + "'");
                }
                continue;
            }
            if (count == 1) {
                relationOris.remove(related);
            } else {
                relationOris.put(related, Integer.valueOf(count.intValue() - 1));
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void updateReferenceVersion(IObjRef ori) {
        var version = ori.getVersion();
        if (version == null) {
            return;
        }
        var existing = relationOris.getKey(ori);
        if (existing == null) {
            return;
        }

        var metaData = entityMetaDataProvider.getMetaData(ori.getRealType());
        var versionMember = metaData.getVersionMember();
        if (versionMember == null) {
            return;
        }
        var conversionHelper = this.conversionHelper;
        var cacheVersion = conversionHelper.convertValueToType(versionMember.getElementType(), existing.getVersion());
        var currentVersion = conversionHelper.convertValueToType(versionMember.getElementType(), version);

        if (cacheVersion == null || ((Comparable) cacheVersion).compareTo(currentVersion) < 0) {
            existing.setVersion(currentVersion);
        }
    }

    @Override
    protected CacheKey[] getAlternateCacheKeysFromCacheValue(IEntityMetaData metaData, RootCacheValue cacheValue) {
        return extractAlternateCacheKeys(metaData, cacheValue);
    }

    protected void ensureRelationsExist(RootCacheValue cacheValue, IEntityMetaData metaData, LinkedHashSet<IObjRef> cascadeNeededORIs, ArrayList<DirectValueHolderRef> pendingValueHolders) {
        var relationMembers = metaData.getRelationMembers();
        var relations = cacheValue.getRelations();
        for (int a = relations.length; a-- > 0; ) {
            var relationsOfMember = relations[a];

            var relationMember = relationMembers[a];

            var loadCascadeMode = relationMember.getCascadeLoadMode();
            switch (loadCascadeMode) {
                case DEFAULT:
                case LAZY:
                    break;
                case EAGER: {
                    // Ensure the related RootCacheValues will be loaded - we do not bother here if the
                    // relations are
                    // known or not yet
                    pendingValueHolders.add(new IndirectValueHolderRef(cacheValue, relationMember, this));
                    break;
                }
                case EAGER_VERSION: {
                    if (relationsOfMember != null) {
                        // ObjRefs already loaded. Nothing to do
                        continue;
                    }
                    pendingValueHolders.add(new IndirectValueHolderRef(cacheValue, relationMember, this, true));
                    break;
                }
                default:
                    throw RuntimeExceptionUtil.createEnumNotSupportedException(loadCascadeMode);
            }
        }
    }

    @Override
    public boolean applyValues(Object targetObject, ICacheIntern targetCache, IPrivilege privilege) {
        if (targetObject == null) {
            return false;
        }
        var metaData = ((IEntityMetaDataHolder) targetObject).get__EntityMetaData();
        var id = metaData.getIdMember().getValue(targetObject, false);
        var cacheValue = getCacheValue(metaData, ObjRef.PRIMARY_KEY_INDEX, id);
        if (cacheValue == null) // Cache miss
        {
            return false;
        }
        updateExistingObject(metaData, cacheValue, targetObject, targetCache, isFilteringNecessary(targetCache), privilege);
        return true;
    }

    @Override
    public void getContent(final HandleContentDelegate handleContentDelegate) {
        var writeLock = getWriteLock();
        writeLock.lock();
        try {
            for (var entry : keyToCacheValueDict) {
                var cacheValue = getCacheValueFromReference(entry.getValue());
                if (cacheValue == null) {
                    continue;
                }
                handleContentDelegate.invoke(entry.getEntityType(), entry.getIdIndex(), entry.getId(), cacheValue);
            }
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    protected Class<?> getEntityTypeOfObject(Object obj) {
        if (obj instanceof RootCacheValue) {
            return ((RootCacheValue) obj).getEntityType();
        }
        return super.getEntityTypeOfObject(obj);
    }

    @Override
    protected Object getIdOfObject(IEntityMetaData metaData, Object obj) {
        if (obj instanceof RootCacheValue) {
            return ((RootCacheValue) obj).getId();
        }
        return super.getIdOfObject(metaData, obj);
    }

    @Override
    protected Object getVersionOfObject(IEntityMetaData metaData, Object obj) {
        if (obj instanceof RootCacheValue) {
            return ((RootCacheValue) obj).getVersion();
        }
        return super.getVersionOfObject(metaData, obj);
    }

    @Override
    protected Object[] extractPrimitives(IEntityMetaData metaData, Object obj) {
        if (obj instanceof RootCacheValue) {
            return ((RootCacheValue) obj).getPrimitives();
        }
        return super.extractPrimitives(metaData, obj);
    }

    @Override
    protected IObjRef[][] extractRelations(IEntityMetaData metaData, Object obj, List<Object> relationValues) {
        if (obj instanceof RootCacheValue) {
            return ((RootCacheValue) obj).getRelations();
        }
        return super.extractRelations(metaData, obj, relationValues);
    }

    @Override
    protected void clearIntern() {
        super.clearIntern();
        relationOris.clear();
        var lruLock = this.lruLock;
        lruLock.lock();
        try {
            lruList.clear();
        } finally {
            lruLock.unlock();
        }
    }

    @Override
    protected int doCleanUpIntern() {
        var cleanupCount = super.doCleanUpIntern();
        if (cleanupCount > 0 && System.currentTimeMillis() - lastRelationObjRefsRefreshTime >= relationObjRefsRefreshThrottleOnGC) {
            doRelationObjRefsRefresh();
        }
        return cleanupCount;
    }

    @Override
    protected void putInternObjRelation(RootCacheValue cacheValue, IEntityMetaData metaData, IObjRelation objRelation, IObjRef[] relationsOfMember) {
        var relationIndex = metaData.getIndexByRelationName(objRelation.getMemberName());
        if (relationsOfMember.length == 0) {
            relationsOfMember = IObjRef.EMPTY_ARRAY;
        }
        cacheValue.setRelation(relationIndex, relationsOfMember);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public List<ILoadContainer> getEntities(List<IObjRef> orisToLoad) {
        IList result = getObjects(orisToLoad, CacheDirective.loadContainerResult());
        return result;
    }

    @Override
    public List<IObjRelationResult> getRelations(List<IObjRelation> objRelations) {
        return getObjRelations(objRelations, CacheDirective.none());
    }

    @Override
    public void beginOnline() {
        clear();
    }

    @Override
    public void handleOnline() {
        // Intended blank
    }

    @Override
    public void endOnline() {
        // Intended blank
    }

    @Override
    public void beginOffline() {
        clear();
    }

    @Override
    public void handleOffline() {
        // Intended blank
    }

    @Override
    public void endOffline() {
        // Intended blank
    }

    @Override
    public void assignEntityToCache(Object entity) {
        throw new UnsupportedOperationException();
    }

    protected void doRelationObjRefsRefresh() {
        lastRelationObjRefsRefreshTime = System.currentTimeMillis();
        if (!weakEntries) {
            return;
        }
        var zero = Integer.valueOf(0);
        for (Entry<IObjRef, Integer> entry : relationOris) {
            entry.setValue(zero);
        }
        var alreadyHandledSet = IdentityHashSet.<RootCacheValue>create(keyToCacheValueDict.size());
        getContent(new HandleContentDelegate() {
            @Override
            public void invoke(Class<?> entityType, byte idIndex, Object id, Object value) {
                var cacheValue = (RootCacheValue) value;
                if (!alreadyHandledSet.add(cacheValue)) {
                    return;
                }
                var metaData = cacheValue.get__EntityMetaData();
                for (int relationIndex = metaData.getRelationMembers().length; relationIndex-- > 0; ) {
                    registerRelations(cacheValue.getRelation(relationIndex));
                }
            }
        });

        var iter = relationOris.iterator();
        while (iter.hasNext()) {
            var entry = iter.next();
            if (entry.getValue() == zero) {
                iter.remove();
            }
        }
    }

    static class DoRelationObjRefsRefreshOnResize implements IResizeMapCallback {
        private final RootCache rootCache;

        protected boolean alreadyRefreshed = false;

        DoRelationObjRefsRefreshOnResize(RootCache rootCache) {
            this.rootCache = rootCache;
        }

        @Override
        public void resizeMapRequested(Object map) {
            var alreadyRefreshed = this.alreadyRefreshed;
            if (alreadyRefreshed) {
                return;
            }
            this.alreadyRefreshed = true;
            try {
                rootCache.doRelationObjRefsRefresh();
            } finally {
                this.alreadyRefreshed = false;
            }
        }
    }
}
