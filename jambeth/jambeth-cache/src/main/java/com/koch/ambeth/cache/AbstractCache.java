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

import com.koch.ambeth.cache.collections.CacheHashMap;
import com.koch.ambeth.cache.collections.ICacheMapEntryTypeProvider;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.merge.IProxyHelper;
import com.koch.ambeth.merge.cache.AbstractCacheValue;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.HandleContentDelegate;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.compositeid.ICompositeIdFactory;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.merge.util.ICacheHelper;
import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.DebugMode;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.IDisposable;
import com.koch.ambeth.util.Lock;
import com.koch.ambeth.util.ReadWriteLock;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.util.config.UtilConfigurationConstants;
import com.koch.ambeth.util.threading.IGuiThreadHelper;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class AbstractCache<V> implements ICache, IInitializingBean, IDisposable {
    public static final String P_WEAK_ENTRIES = "WeakEntries";

    protected static final CacheKey[] emptyCacheKeyArray = new CacheKey[0];

    protected static final ThreadLocal<Boolean> failInCacheHierarchyModeActiveTL = new SensitiveThreadLocal<>();

    private static final ThreadLocal<IdentityHashSet<Object>> hardRefTL = new SensitiveThreadLocal<>();

    public static boolean isFailInCacheHierarchyModeActive() {
        return Boolean.TRUE.equals(failInCacheHierarchyModeActiveTL.get());
    }

    public static void setFailInCacheHierarchyModeActive(boolean failInCacheHierarchyModeActive) {
        failInCacheHierarchyModeActiveTL.set(Boolean.valueOf(failInCacheHierarchyModeActive));
    }

    public static void addHardRefTL(Object obj) {
        if (obj == null) {
            return;
        }
        IdentityHashSet<Object> hardRefSet = hardRefTL.get();
        if (hardRefSet == null) {
            return;
        }
        hardRefSet.add(obj);
    }

    protected final ReferenceQueue<V> referenceQueue = new ReferenceQueue<>();
    protected CacheHashMap keyToCacheValueDict;
    @Autowired
    protected ICacheMapEntryTypeProvider cacheMapEntryTypeProvider;
    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;
    @Autowired
    protected ICompositeIdFactory compositeIdFactory;
    @Autowired
    protected IConversionHelper conversionHelper;
    @Autowired
    protected ICacheHelper cacheHelper;
    @Autowired(optional = true)
    protected IGuiThreadHelper guiThreadHelper;
    @Autowired
    protected IProxyHelper proxyHelper;
    @Property(mandatory = false)
    protected Thread boundThread;
    protected boolean weakEntries;
    protected volatile int changeVersion = 1;
    @Property(mandatory = false)
    protected Lock readLock;
    @Property(mandatory = false)
    protected Lock writeLock;
    @Property(name = UtilConfigurationConstants.DebugMode, defaultValue = "false")
    protected boolean debugMode;

    @Override
    public void afterPropertiesSet() {
        if (readLock == null && writeLock == null) {
            DebugMode debugModeEnum = debugMode ? DebugMode.TRUE : DebugMode.FALSE;
            ReadWriteLock rwLock = new ReadWriteLock(debugModeEnum);
            readLock = rwLock.getReadLock();
            writeLock = rwLock.getWriteLock();
        }
        keyToCacheValueDict = new CacheHashMap(cacheMapEntryTypeProvider);
    }

    @Override
    public void dispose() {
        cacheHelper = null;
        cacheMapEntryTypeProvider = null;
        compositeIdFactory = null;
        conversionHelper = null;
        boundThread = null;
        entityMetaDataProvider = null;
        guiThreadHelper = null;
        proxyHelper = null;
        keyToCacheValueDict = null;
    }

    public void setWeakEntries(boolean weakEntries) {
        this.weakEntries = weakEntries;
    }

    @Override
    public ICache getCurrentCache() {
        return this;
    }

    @Override
    public Lock getReadLock() {
        return readLock;
    }

    @Override
    public Lock getWriteLock() {
        return writeLock;
    }

    protected void checkNotDisposed() {
        if (conversionHelper == null) {
            throw new IllegalStateException("Cache already disposed");
        }
    }

    public boolean acquireHardRefTLIfNotAlready() {
        return acquireHardRefTLIfNotAlready(0);
    }

    public boolean acquireHardRefTLIfNotAlready(int sizeHint) {
        if (!weakEntries) {
            return false;
        }
        IdentityHashSet<Object> hardRefSet = hardRefTL.get();
        if (hardRefSet != null) {
            return false;
        }
        hardRefSet = sizeHint > 0 ? IdentityHashSet.create(sizeHint) : new IdentityHashSet<>();
        hardRefTL.set(hardRefSet);
        return true;
    }

    public void clearHardRefs(boolean acquirementSuccessful) {
        if (!acquirementSuccessful) {
            return;
        }
        hardRefTL.remove();
    }

    /**
     * Checks if an entity with a given type and ID and at least the given version exists in cache.
     *
     * @param ori Object reference.
     * @return True if a request for the referenced object could be satisfied, otherwise false.
     */
    protected boolean exists(IObjRef ori) {
        return existsValue(ori) != null;
    }

    @SuppressWarnings("unchecked")
    protected V getCacheValueFromReference(Object reference) {
        if (reference == null) {
            return null;
        }
        if (weakEntries) {
            return ((Reference<V>) reference).get();
        }
        return (V) reference;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected V existsValue(IObjRef ori) {
        var metaData = this.entityMetaDataProvider.getMetaData(ori.getRealType());
        var idMember = metaData.getIdMemberByIdIndex(ori.getIdNameIndex());
        var id = conversionHelper.convertValueToType(idMember.getRealType(), ori.getId());
        var readLock = getReadLock();
        readLock.lock();
        try {
            var cacheValueR = getCacheValueRDirect(metaData, ori.getIdNameIndex(), id);
            var cacheValue = getCacheValueFromReference(cacheValueR);
            if (cacheValue == null) {
                return null;
            }
            var versionMember = metaData.getVersionMember();
            if (versionMember == null) {
                if (weakEntries) {
                    addHardRefTL(cacheValue);
                }

                // without a versionMember each cache hit is a valid hit
                return cacheValue;
            }
            var cacheVersion = getVersionOfCacheValue(metaData, cacheValue);
            // Compare operation only works on identical operand types
            var requestedVersion = conversionHelper.convertValueToType(versionMember.getElementType(), ori.getVersion());

            if (requestedVersion == null || cacheVersion == null || ((Comparable) cacheVersion).compareTo(requestedVersion) >= 0) {
                if (weakEntries) {
                    addHardRefTL(cacheValue);
                }
                // requested version is lower or equal than cached
                // version
                return cacheValue;
            }
            return null;
        } finally {
            readLock.unlock();
        }
    }

    protected CacheKey[] extractAlternateCacheKeys(IEntityMetaData metaData, Object obj) {
        int alternateIdCount = metaData.getAlternateIdCount();
        if (alternateIdCount == 0) {
            return emptyCacheKeyArray;
        }
        CacheKey[] alternateCacheKeys = new CacheKey[alternateIdCount];
        extractAlternateCacheKeys(metaData, obj, alternateCacheKeys);
        return alternateCacheKeys;
    }

    protected void extractAlternateCacheKeys(IEntityMetaData metaData, Object obj, CacheKey[] alternateCacheKeys) {
        if (alternateCacheKeys.length == 0) {
            return;
        }
        Class<?> entityType = metaData.getEntityType();
        for (int idIndex = metaData.getAlternateIdCount(); idIndex-- > 0; ) {
            Object alternateId;
            if (obj instanceof Object[]) {
                alternateId = compositeIdFactory.createIdFromPrimitives(metaData, idIndex, (Object[]) obj);
            } else {
                alternateId = compositeIdFactory.createIdFromPrimitives(metaData, idIndex, (AbstractCacheValue) obj);
            }
            CacheKey alternateCacheKey = alternateCacheKeys[idIndex];
            if (alternateId == null) {
                if (alternateCacheKey != null) {
                    alternateCacheKeys[idIndex] = null;
                }
                continue;
            }
            if (alternateCacheKey == null) {
                alternateCacheKey = new CacheKey();
                alternateCacheKeys[idIndex] = alternateCacheKey;
            }
            alternateCacheKey.setEntityType(entityType);
            alternateCacheKey.setId(alternateId);
            alternateCacheKey.setIdIndex((byte) idIndex);
        }
    }

    protected abstract CacheKey[] getAlternateCacheKeysFromCacheValue(IEntityMetaData metaData, V cacheValue);

    public void remove(Class<?> type, Object id) {
        checkNotDisposed();
        IEntityMetaData metaData = entityMetaDataProvider.getMetaData(type);
        removeCacheValueFromCacheCascade(metaData, ObjRef.PRIMARY_KEY_INDEX, id, true);
    }

    public void remove(IObjRef ori) {
        checkNotDisposed();
        IEntityMetaData metaData = entityMetaDataProvider.getMetaData(ori.getRealType());
        removeCacheValueFromCacheCascade(metaData, ori.getIdNameIndex(), ori.getId(), true);
    }

    public void remove(List<IObjRef> oris) {
        checkNotDisposed();
        if (oris.isEmpty()) {
            return;
        }
        var entityMetaDataProvider = this.entityMetaDataProvider;
        var writeLock = getWriteLock();
        writeLock.lock();
        try {
            for (int a = oris.size(); a-- > 0; ) {
                var ori = oris.get(a);
                var metaData = entityMetaDataProvider.getMetaData(ori.getRealType());
                removeCacheValueFromCacheCascade(metaData, ori.getIdNameIndex(), ori.getId(), true);
            }
        } finally {
            writeLock.unlock();
        }
    }

    public void removePriorVersions(IObjRef ori) {
        checkNotDisposed();
        var writeLock = getWriteLock();
        writeLock.lock();
        try {
            if (ori.getVersion() != null && existsValue(ori) != null) {
                // if there is a object in the cache with the requested version
                // it
                // has already been refreshed
                return;
            }
            remove(ori);
        } finally {
            writeLock.unlock();
        }
    }

    public void removePriorVersions(List<IObjRef> oris) {
        checkNotDisposed();
        if (oris.isEmpty()) {
            return;
        }
        Lock writeLock = getWriteLock();
        writeLock.lock();
        try {
            ArrayList<IObjRef> reallyToRemove = new ArrayList<>(oris.size());
            for (int a = oris.size(); a-- > 0; ) {
                IObjRef ori = oris.get(a);
                if (ori.getVersion() != null && existsValue(ori) != null) {
                    // if there is a object in the cache with the requested version
                    // it has already been refreshed
                    continue;
                }
                reallyToRemove.add(ori);
            }
            remove(reallyToRemove);
        } finally {
            writeLock.unlock();
        }
    }

    protected void removeCacheValueFromCacheCascade(IEntityMetaData metaData, byte idIndex, Object id, boolean checkCleanUpOnMiss) {
        var entityType = metaData.getEntityType();
        var idMember = metaData.getIdMemberByIdIndex(idIndex);
        id = conversionHelper.convertValueToType(idMember.getRealType(), id);
        var writeLock = getWriteLock();
        writeLock.lock();
        try {
            var cacheValueR = removeKeyFromCache(entityType, idIndex, id);
            V cacheValue = getCacheValueFromReference(cacheValueR);
            if (cacheValue == null) {
                if (checkCleanUpOnMiss) {
                    checkForCleanup();
                }
                return;
            }
            var primaryId = getIdOfCacheValue(metaData, cacheValue);
            if (primaryId != null) {
                cacheValueHasBeenRemoved(metaData, ObjRef.PRIMARY_KEY_INDEX, primaryId, cacheValue);
                removeCacheValueFromCacheSingle(metaData, ObjRef.PRIMARY_KEY_INDEX, primaryId);
            } else {
                cacheValueHasBeenRemoved(metaData, idIndex, id, cacheValue);
            }
            var alternateCacheKeys = getAlternateCacheKeysFromCacheValue(metaData, cacheValue);
            for (int a = alternateCacheKeys.length; a-- > 0; ) {
                removeKeyFromCache(alternateCacheKeys[a]);
            }
            increaseVersion();
        } finally {
            writeLock.unlock();
        }
    }

    protected Object removeKeyFromCache(CacheKey cacheKey) {
        if (cacheKey == null) {
            return null;
        }
        return removeKeyFromCache(cacheKey.getEntityType(), cacheKey.getIdIndex(), cacheKey.getId());
    }

    protected Object removeKeyFromCache(Class<?> entityType, byte idIndex, Object id) {
        if (entityType == null) {
            return null;
        }
        return this.keyToCacheValueDict.remove(entityType, idIndex, id);
    }

    public abstract V createCacheValueInstance(IEntityMetaData metaData, Object obj);

    protected abstract Object getIdOfCacheValue(IEntityMetaData metaData, V cacheValue);

    protected abstract void setIdOfCacheValue(IEntityMetaData metaData, V cacheValue, Object id);

    protected abstract Object getVersionOfCacheValue(IEntityMetaData metaData, V cacheValue);

    protected abstract void setVersionOfCacheValue(IEntityMetaData metaData, V cacheValue, Object version);

    protected void setRelationsOfCacheValue(IEntityMetaData metaData, V cacheValue, Object[] primitives, IObjRef[][] relations) {
        // Intended blank
    }

    protected void increaseVersion() {
        if (++changeVersion == Integer.MAX_VALUE) {
            changeVersion = 1;
        }
    }

    protected void removeCacheValueFromCacheSingle(IEntityMetaData metaData, byte idIndex, Object id) {
        Member idMember = metaData.getIdMemberByIdIndex(idIndex);
        id = conversionHelper.convertValueToType(idMember.getRealType(), id);
        removeKeyFromCache(metaData.getEntityType(), idIndex, id);
    }

    protected void removeAlternateCacheKeysFromCache(IEntityMetaData metaData, CacheKey[] alternateCacheKeys) {
        if (alternateCacheKeys == null) {
            return;
        }
        for (int a = alternateCacheKeys.length; a-- > 0; ) {
            removeKeyFromCache(alternateCacheKeys[a]);
        }
    }

    protected Object createReference(V obj, Class<?> entityType, byte idIndex, Object id) {
        if (weakEntries) {
            return new CacheWeakReference<>(obj, referenceQueue);
        }
        return obj;
    }

    public List<Object> put(Object objectToCache) {
        HashSet<IObjRef> cascadeNeededORIs = new HashSet<>();
        IdentityHashSet<Object> alreadyHandledSet = new IdentityHashSet<>();
        ArrayList<Object> hardRefsToCacheValue = new ArrayList<>();
        boolean success = acquireHardRefTLIfNotAlready();
        Lock writeLock = getWriteLock();
        writeLock.lock();
        try {
            putIntern(objectToCache, hardRefsToCacheValue, alreadyHandledSet, cascadeNeededORIs);
            increaseVersion();
            return hardRefsToCacheValue;
        } finally {
            writeLock.unlock();
            clearHardRefs(success);
        }
    }

    protected Class<?> getEntityTypeOfObject(Object obj) {
        return obj.getClass();
    }

    protected Object getIdOfObject(IEntityMetaData metaData, Object obj) {
        return metaData.getIdMember().getValue(obj, false);
    }

    protected Object getVersionOfObject(IEntityMetaData metaData, Object obj) {
        Member versionMember = metaData.getVersionMember();
        return versionMember != null ? versionMember.getValue(obj, false) : null;
    }

    protected Object[] extractPrimitives(IEntityMetaData metaData, Object obj) {
        return cacheHelper.extractPrimitives(metaData, obj);
    }

    protected IObjRef[][] extractRelations(IEntityMetaData metaData, Object obj, List<Object> relationValues) {
        return cacheHelper.extractRelations(metaData, obj, relationValues);
    }

    protected abstract void putInternObjRelation(V cacheValue, IEntityMetaData metaData, IObjRelation objRelation, IObjRef[] relationsOfMember);

    protected void putIntern(Object objectToCache, ArrayList<Object> hardRefsToCacheValue, IdentityHashSet<Object> alreadyHandledSet, HashSet<IObjRef> cascadeNeededORIs) {
        if (objectToCache == null || !alreadyHandledSet.add(objectToCache)) {
            return;
        }
        if (objectToCache.getClass().isArray()) {
            int length = Array.getLength(objectToCache);
            for (int a = length; a-- > 0; ) {
                putIntern(Array.get(objectToCache, a), hardRefsToCacheValue, alreadyHandledSet, cascadeNeededORIs);
            }
            return;
        }
        if (objectToCache instanceof List) {
            List<?> list = (List<?>) objectToCache;
            for (int a = list.size(); a-- > 0; ) {
                putIntern(list.get(a), hardRefsToCacheValue, alreadyHandledSet, cascadeNeededORIs);
            }
            return;
        }
        if (objectToCache instanceof Collection) {
            for (Object item : (Collection<?>) objectToCache) {
                putIntern(item, hardRefsToCacheValue, alreadyHandledSet, cascadeNeededORIs);
            }
            return;
        }
        if (objectToCache instanceof IObjRelationResult) {
            IObjRelationResult objRelationResult = (IObjRelationResult) objectToCache;
            IObjRelation objRelation = objRelationResult.getReference();
            IObjRef objRef = objRelation.getObjRefs()[0];
            IEntityMetaData metaData2 = entityMetaDataProvider.getMetaData(objRef.getRealType());

            Object cacheValueR = getCacheValueR(metaData2, objRef.getIdNameIndex(), objRef.getId());
            V cacheValue = getCacheValueFromReference(cacheValueR);
            if (cacheValue == null) {
                return;
            }
            putInternObjRelation(cacheValue, metaData2, objRelation, objRelationResult.getRelations());
            return;
        }
        if (objectToCache instanceof ILoadContainer) {
            putIntern((ILoadContainer) objectToCache);
            return;
        }
        IEntityMetaData metaData = entityMetaDataProvider.getMetaData(getEntityTypeOfObject(objectToCache));
        Object key = getIdOfObject(metaData, objectToCache);

        ArrayList<Object> relationValues = new ArrayList<>();
        IObjRef[][] relations = extractRelations(metaData, objectToCache, relationValues);

        if (key != null) {
            // Object itself can only be cached with a primary key
            Object version = getVersionOfObject(metaData, objectToCache);

            Object cacheValueR = getCacheValueR(metaData, ObjRef.PRIMARY_KEY_INDEX, key);
            V cacheValue = getCacheValueFromReference(cacheValueR);
            boolean objectItselfIsUpToDate = false;
            if (cacheValue != null && getIdOfCacheValue(metaData, cacheValue) != null) {
                // Similar object already cached. Let's see how the version
                // compares...
                Object cachedVersion = getVersionOfCacheValue(metaData, cacheValue);
                if (cachedVersion != null && cachedVersion.equals(version)) {
                    // Object has even already the same version, so there is
                    // absolutely nothing to do here
                    objectItselfIsUpToDate = true;
                }
            }
            if (!objectItselfIsUpToDate) {
                Object[] primitives = extractPrimitives(metaData, objectToCache);
                CacheKey[] alternateCacheKeys = extractAlternateCacheKeys(metaData, primitives);
                Object hardRef = putIntern(metaData, objectToCache, key, version, alternateCacheKeys, primitives, relations);
                hardRefsToCacheValue.add(hardRef);
            } else {
                hardRefsToCacheValue.add(cacheValue);
            }
            putInternPersistedEntity(objectToCache);
        } else {
            putInternUnpersistedEntity(objectToCache);
        }

        // Even if it has no id we look for its relations and cache them
        for (int a = relationValues.size(); a-- > 0; ) {
            putIntern(relationValues.get(a), hardRefsToCacheValue, alreadyHandledSet, cascadeNeededORIs);
        }
    }

    protected void putInternPersistedEntity(Object entity) {
        // Intended blank
    }

    protected void putInternUnpersistedEntity(Object entity) {
        // Intended blank
    }

    protected boolean allowCacheValueReplacement() {
        return false;
    }

    protected abstract void putIntern(ILoadContainer loadContainer);

    protected V putIntern(IEntityMetaData metaData, Object obj, Object id, Object version, CacheKey[] alternateCacheKeys, Object[] primitives, IObjRef[][] relations) {
        byte idIndex = ObjRef.PRIMARY_KEY_INDEX;
        Object cacheValueR = getCacheValueR(metaData, idIndex, id);
        V cacheValue = getCacheValueFromReference(cacheValueR);
        if (cacheValue == null) {
            Class<?> entityType = metaData.getEntityType();
            cacheValue = createCacheValueInstance(metaData, obj);
            cacheValueR = createReference(cacheValue, entityType, idIndex, id);
            id = conversionHelper.convertValueToType(metaData.getIdMember().getRealType(), id);
            setIdOfCacheValue(metaData, cacheValue, id);

            keyToCacheValueDict.put(entityType, idIndex, id, cacheValueR);
            cacheValueHasBeenAdded(idIndex, id, metaData, primitives, relations, cacheValueR);
        } else if (obj != null && cacheValue != obj && !allowCacheValueReplacement()) {
            // If the cache does not allow replacements, do nothing with this
            // put-request
            return cacheValue;
        } else {
            CacheKey[] oldAlternateIds = extractAlternateCacheKeys(metaData, primitives);
            for (int a = oldAlternateIds.length; a-- > 0; ) {
                removeKeyFromCache(oldAlternateIds[a]);
            }
            cacheValueHasBeenUpdated(metaData, primitives, relations, cacheValueR);
        }
        cacheValueHasBeenRead(cacheValueR);

        // Create cache entry for the primary id and all alternate ids
        putAlternateCacheKeysToCache(metaData, alternateCacheKeys, cacheValueR);

        setVersionOfCacheValue(metaData, cacheValue, version);
        setRelationsOfCacheValue(metaData, cacheValue, primitives, relations);
        return cacheValue;
    }

    protected void putAlternateCacheKeysToCache(IEntityMetaData metaData, CacheKey[] alternateCacheKeys, Object cacheValueR) {
        for (int a = alternateCacheKeys.length; a-- > 0; ) {
            CacheKey alternateCacheKey = alternateCacheKeys[a];
            if (alternateCacheKey != null) {
                keyToCacheValueDict.put(alternateCacheKey.getEntityType(), alternateCacheKey.getIdIndex(), alternateCacheKey.getId(), cacheValueR);
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected V getCacheValue(IEntityMetaData metaData, IObjRef objRef, boolean checkVersion) {
        Object cacheValueR = getCacheValueR(metaData, objRef.getIdNameIndex(), objRef.getId());
        V cacheValue = getCacheValueFromReference(cacheValueR);
        if (cacheValue == null) {
            return null;
        }
        Member versionMember = metaData.getVersionMember();
        if (checkVersion && versionMember != null && objRef.getVersion() != null) {
            Object cacheVersion = getVersionOfCacheValue(metaData, cacheValue);
            // Compare operation only works on identical operand types
            Object requestedVersion = conversionHelper.convertValueToType(versionMember.getElementType(), objRef.getVersion());

            if (cacheVersion != null && ((Comparable) cacheVersion).compareTo(requestedVersion) < 0) {
                // requested version is higher than cached version. So this is a cache miss because of
                // outdated information
                return null;
            }
        }
        return cacheValue;
    }

    protected Object getCacheValueR(IEntityMetaData metaData, byte idIndex, Object id) {
        var idMember = metaData.getIdMemberByIdIndex(idIndex);
        id = conversionHelper.convertValueToType(idMember.getRealType(), id);
        return getCacheValueRDirect(metaData, idIndex, id);
    }

    protected Object getCacheValueRDirect(IEntityMetaData metaData, byte idIndex, Object id) {
        var cacheValueR = keyToCacheValueDict.get(metaData.getEntityType(), idIndex, id);
        cacheValueHasBeenRead(cacheValueR);
        return cacheValueR;
    }

    protected V getCacheValue(IEntityMetaData metaData, byte idIndex, Object id) {
        Object cacheValueR = getCacheValueR(metaData, idIndex, id);
        return getCacheValueFromReference(cacheValueR);
    }

    protected void cacheValueHasBeenAdded(byte idIndex, Object id, IEntityMetaData metaData, Object[] primitives, IObjRef[][] relations, Object cacheValueR) {
        checkForCleanup();
    }

    protected void cacheValueHasBeenRead(Object cacheValueR) {
        if (weakEntries) {
            addHardRefTL(getCacheValueFromReference(cacheValueR));
        }
    }

    protected void cacheValueHasBeenUpdated(IEntityMetaData metaData, Object[] primitives, IObjRef[][] relations, Object cacheValueR) {
        checkForCleanup();
    }

    protected void cacheValueHasBeenRemoved(IEntityMetaData metaData, byte idIndex, Object id, V cacheValue) {
        checkForCleanup();
    }

    @Override
    public <E> E getObject(Class<E> type, Object id) {
        return getObject(type, id, Collections.<CacheDirective>emptySet());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> E getObject(Class<E> type, Object... compositeIdParts) {
        IEntityMetaData metaData = entityMetaDataProvider.getMetaData(type);
        Object id = compositeIdFactory.createCompositeId(metaData, metaData.getIdMember(), compositeIdParts);
        ObjRef objRef = new ObjRef(metaData.getEntityType(), id, null);
        return (E) getObject(objRef, CacheDirective.none());
    }

    @Override
    public <E> E getObject(Class<E> type, String idName, Object id) {
        return getObject(type, idName, id, Collections.<CacheDirective>emptySet());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E getObject(Class<E> type, Object id, Set<CacheDirective> cacheDirective) {
        IEntityMetaData metaData = entityMetaDataProvider.getMetaData(type);
        ObjRef objRef = new ObjRef(metaData.getEntityType(), id, null);
        return (E) getObject(objRef, cacheDirective);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> E getObject(Class<E> type, String idName, Object id, Set<CacheDirective> cacheDirective) {
        IEntityMetaData metaData = entityMetaDataProvider.getMetaData(type);
        ObjRef objRef = new ObjRef(metaData.getEntityType(), metaData.getIdIndexByMemberName(idName), id, null);
        return (E) getObject(objRef, cacheDirective);
    }

    @Override
    public Object getObject(IObjRef oriToGet, Set<CacheDirective> cacheDirective) {
        if (oriToGet == null) {
            return null;
        }
        ArrayList<IObjRef> orisToGet = new ArrayList<>(1);
        orisToGet.add(oriToGet);
        List<Object> objects = getObjects(orisToGet, cacheDirective);
        if (objects.isEmpty()) {
            return null;
        }
        return objects.get(0);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> IList<E> getObjects(Class<E> type, Object... ids) {
        ArrayList<IObjRef> orisToGet = new ArrayList<>(ids.length);
        for (int a = 0, size = ids.length; a < size; a++) {
            Object id = ids[a];
            ObjRef objRef = new ObjRef(type, ObjRef.PRIMARY_KEY_INDEX, id, null);
            orisToGet.add(objRef);
        }
        return (IList<E>) getObjects(orisToGet, Collections.<CacheDirective>emptySet());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> IList<E> getObjects(Class<E> type, List<?> ids) {
        ArrayList<IObjRef> orisToGet = new ArrayList<>(ids.size());
        for (int a = 0, size = ids.size(); a < size; a++) {
            orisToGet.add(new ObjRef(type, ObjRef.PRIMARY_KEY_INDEX, ids.get(a), null));
        }
        return (IList<E>) getObjects(orisToGet, Collections.<CacheDirective>emptySet());
    }

    @Override
    public IList<Object> getObjects(IObjRef[] orisToGetArray, Set<CacheDirective> cacheDirective) {
        ArrayList<IObjRef> orisToGet = new ArrayList<>(orisToGetArray);
        return getObjects(orisToGet, cacheDirective);
    }

    @Override
    public abstract IList<Object> getObjects(List<IObjRef> orisToGet, Set<CacheDirective> cacheDirective);

    @Override
    public abstract IList<IObjRelationResult> getObjRelations(List<IObjRelation> objRels, Set<CacheDirective> cacheDirective);

    protected void checkForCleanup() {
        if (!weakEntries) {
            return;
        }
        doCleanUpIntern();
    }

    public void cleanUp() {
        if (!weakEntries) {
            return;
        }
        Lock writeLock = getWriteLock();
        writeLock.lock();
        try {
            doCleanUpIntern();
        } finally {
            writeLock.unlock();
        }
    }

    protected int doCleanUpIntern() {
        var cleanupCount = 0;
        ICacheReference cacheValueR;
        while ((cacheValueR = (ICacheReference) referenceQueue.poll()) != null) {
            var entityType = cacheValueR.getEntityType();
            var idIndex = cacheValueR.getIdIndex();
            var id = cacheValueR.getId();
            var metaData = entityMetaDataProvider.getMetaData(entityType);

            cleanupCount++;
            var existingCacheValueR = getCacheValueR(metaData, idIndex, id);
            if (existingCacheValueR != cacheValueR) {
                // new entry is already another instance reflecting the same entity
                continue;
            }
            removeCacheValueFromCacheCascade(metaData, idIndex, id, false);
        }
        return cleanupCount;
    }

    public int size() {
        Lock readLock = getReadLock();
        readLock.lock();
        try {
            return keyToCacheValueDict.size();
        } finally {
            readLock.unlock();
        }
    }

    public void clear() {
        Lock writeLock = getWriteLock();
        writeLock.lock();
        try {
            clearIntern();
            increaseVersion();
        } finally {
            writeLock.unlock();
        }
    }

    protected void clearIntern() {
        keyToCacheValueDict.clear();
    }

    @Override
    public void getContent(HandleContentDelegate handleContentDelegate) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void cascadeLoadPath(Class<?> entityType, String cascadeLoadPath) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
