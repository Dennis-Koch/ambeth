﻿using De.Osthus.Ambeth.Cache.Collections;
using De.Osthus.Ambeth.Cache.Config;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.CompositeId;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Threading;

namespace De.Osthus.Ambeth.Cache
{
    public abstract class AbstractCache<V> : ICache, IInitializingBean, IDisposable
    {
        protected static readonly CacheKey[] emptyCacheKeyArray = new CacheKey[0];

        private static readonly ILogger LOG = LoggerFactory.GetLogger<AbstractCache<Object>>();

        protected static readonly ThreadLocal<bool> failEarlyModeActiveTL = new ThreadLocal<bool>();

        private static readonly ThreadLocal<IdentityHashSet<Object>> hardRefTL = new ThreadLocal<IdentityHashSet<Object>>();

        public static bool FailEarlyModeActive
        {
            get
            {
                return failEarlyModeActiveTL.Value;
            }
            set
            {
                failEarlyModeActiveTL.Value = value;
            }
        }

        protected CacheHashMap keyToCacheValueDict;

        [Autowired]
        public ICacheHelper CacheHelper { protected get; set; }

        [Autowired]
        public ICacheMapEntryTypeProvider CacheMapEntryTypeProvider { protected get; set; }

        [Autowired]
        public ICompositeIdFactory CompositeIdFactory { protected get; set; }

        [Autowired]
        public IConversionHelper ConversionHelper { protected get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        [Autowired(Optional = true)]
        public IGuiThreadHelper GuiThreadHelper { protected get; set; }

        [Autowired]
        public IProxyHelper ProxyHelper { protected get; set; }

        [Property(CacheConfigurationConstants.CacheReferenceCleanupInterval, DefaultValue = "60000")]
        public TimeSpan ReferenceCleanupInterval { protected get; set; }

        public virtual bool WeakEntries { protected get; set; }

        protected DateTime lastCleanupTime = DateTime.Now;

        protected int changeVersion = 1;

        protected Lock readLock;

        protected Lock writeLock;

        public AbstractCache()
        {
            ReadWriteLock rwLock = new ReadWriteLock();
            readLock = rwLock.ReadLock;
            writeLock = rwLock.WriteLock;
        }

        public virtual void AfterPropertiesSet()
        {
            keyToCacheValueDict = new CacheHashMap(CacheMapEntryTypeProvider);
        }

        public virtual void Dispose()
        {
            CacheHelper = null;
            CacheMapEntryTypeProvider = null;
            CompositeIdFactory = null;
            ConversionHelper = null;
            EntityMetaDataProvider = null;
            GuiThreadHelper = null;
            ProxyHelper = null;
            keyToCacheValueDict = null;
        }

        protected void PrepareCacheKeyss(IEntityMetaData metaData, CacheKey cacheKey, IObjRef objRef)
        {
            sbyte idNameIndex = objRef.IdNameIndex;
            cacheKey.EntityType = metaData.EntityType;
            cacheKey.IdNameIndex = idNameIndex;
            ITypeInfoItem idMember = metaData.GetIdMemberByIdIndex(idNameIndex);
            cacheKey.Id = ConversionHelper.ConvertValueToType(idMember.RealType, objRef.Id);
        }

        protected void PrepareCacheKeyss(IEntityMetaData metaData, CacheKey cacheKey, sbyte idNameIndex, Object id)
        {
            ParamChecker.AssertParamNotNull(id, "id");
            cacheKey.EntityType = metaData.EntityType;
            cacheKey.IdNameIndex = idNameIndex;
            ITypeInfoItem idMember = metaData.GetIdMemberByIdIndex(idNameIndex);
            cacheKey.Id = ConversionHelper.ConvertValueToType(idMember.RealType, id);
        }

        [Property(Mandatory = false)]
        public Lock ReadLock
        {
            get
            {
                return readLock;
            }
            set
            {
                readLock = value;
            }
        }

        [Property(Mandatory = false)]
        public Lock WriteLock
        {
            get
            {
                return writeLock;
            }
            set
            {
                writeLock = value;
            }
        }

        protected void CheckNotDisposed()
        {
            if (ConversionHelper == null)
            {
                throw new Exception("Cache already disposed");
            }
        }

        public bool AcquireHardRefTLIfNotAlready()
        {
            return AcquireHardRefTLIfNotAlready(0);
        }

        public bool AcquireHardRefTLIfNotAlready(int sizeHint)
        {
            if (!WeakEntries)
            {
                return false;
            }
            IdentityHashSet<Object> hardRefSet = hardRefTL.Value;
            if (hardRefSet != null)
            {
                return false;
            }
            hardRefSet = sizeHint > 0 ? IdentityHashSet<Object>.Create(sizeHint) : new IdentityHashSet<Object>();
            hardRefTL.Value = hardRefSet;
            return true;
        }

        public static void AddHardRefTL(Object obj)
        {
            if (obj == null)
            {
                return;
            }
            IdentityHashSet<Object> hardRefSet = hardRefTL.Value;
            if (hardRefSet == null)
            {
                return;
            }
            hardRefSet.Add(obj);
        }

        public void ClearHardRefs(bool acquirementSuccessful)
        {
            if (!acquirementSuccessful)
            {
                return;
            }
            hardRefTL.Value = null;
        }

        /**
         * Checks if an entity with a given type and ID and at least the given version exists in cache.
         * 
         * @param ori
         *            Object reference.
         * @return True if a request for the referenced object could be satisfied, otherwise false.
         */
        protected bool Exists(IObjRef ori)
        {
            return ExistsValue(ori) != null;
        }

        protected V GetCacheValueFromReference(Object reference)
        {
            if (reference == null)
            {
                return default(V);
            }
            if (WeakEntries)
            {
                return (V)((WeakReference)reference).Target;
            }
            return (V)reference;
        }

        protected V ExistsValue(IObjRef ori)
        {
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(ori.RealType);
            ITypeInfoItem idMember = metaData.GetIdMemberByIdIndex(ori.IdNameIndex);
            Object id = ConversionHelper.ConvertValueToType(idMember.RealType, ori.Id);
            Lock readLock = ReadLock;
            readLock.Lock();
            try
            {
                Object cacheValueR = keyToCacheValueDict.Get(metaData.EntityType, ori.IdNameIndex, id);
                V cacheValue = GetCacheValueFromReference(cacheValueR);
                if (cacheValue == null)
                {
                    return default(V);
                }
                ITypeInfoItem versionMember = metaData.VersionMember;
                if (versionMember == null)
                {
                    if (WeakEntries)
                    {
                        AddHardRefTL(cacheValue);
                    }
                    // without a versionMember each cache hit is a valid hit
                    return cacheValue;
                }
                Object cacheVersion = GetVersionOfCacheValue(metaData, cacheValue);
                // Compare operation only works on identical operand types
                Object requestedVersion = ConversionHelper.ConvertValueToType(versionMember.ElementType, ori.Version);

                if (requestedVersion == null || cacheVersion == null || ((IComparable)cacheVersion).CompareTo(requestedVersion) >= 0)
                {
                    if (WeakEntries)
                    {
                        AddHardRefTL(cacheValue);
                    }
                    // requested version is lower or equal than cached version
                    return cacheValue;
                }
                return default(V);
            }
            finally
            {
                readLock.Unlock();
            }
        }

        protected CacheKey[] ExtractAlternateCacheKeys(IEntityMetaData metaData, Object obj)
        {
            int alternateIdCount = metaData.GetAlternateIdCount();
            if (alternateIdCount == 0)
            {
                return emptyCacheKeyArray;
            }
            CacheKey[] alternateCacheKeys = new CacheKey[alternateIdCount];
            ExtractAlternateCacheKeys(metaData, obj, alternateCacheKeys);
            return alternateCacheKeys;
        }

        protected void ExtractAlternateCacheKeys(IEntityMetaData metaData, Object obj, CacheKey[] alternateCacheKeys)
        {
            if (alternateCacheKeys.Length == 0)
            {
                return;
            }
            Type entityType = metaData.EntityType;
            for (int idIndex = metaData.GetAlternateIdCount(); idIndex-- > 0; )
            {
                Object alternateId;
                if (obj is Object[])
                {
                    alternateId = CompositeIdFactory.CreateIdFromPrimitives(metaData, idIndex, (Object[])obj);
                }
                else
                {
                    alternateId = CompositeIdFactory.CreateIdFromPrimitives(metaData, idIndex, (AbstractCacheValue)obj);
                }
                CacheKey alternateCacheKey = alternateCacheKeys[idIndex];
                if (alternateId == null)
                {
                    if (alternateCacheKey != null)
                    {
                        alternateCacheKeys[idIndex] = null;
                    }
                    continue;
                }
                if (alternateCacheKey == null)
                {
                    alternateCacheKey = new CacheKey();
                    alternateCacheKeys[idIndex] = alternateCacheKey;
                }
                alternateCacheKey.EntityType = entityType;
                alternateCacheKey.Id = alternateId;
                alternateCacheKey.IdNameIndex = (sbyte)idIndex;
            }
        }

        protected abstract CacheKey[] GetAlternateCacheKeysFromCacheValue(IEntityMetaData metaData, V cacheValue);

        public void Remove(Type type, Object id)
        {
            IEntityMetaData metaData = this.EntityMetaDataProvider.GetMetaData(type);
            RemoveCacheValueFromCacheCascade(metaData, ObjRef.PRIMARY_KEY_INDEX, id);
        }

        public void Remove(IObjRef ori)
        {
            IEntityMetaData metaData = this.EntityMetaDataProvider.GetMetaData(ori.RealType);
            RemoveCacheValueFromCacheCascade(metaData, ori.IdNameIndex, ori.Id);
        }

        public void Remove(IList<IObjRef> oris)
        {
            for (int a = oris.Count; a-- > 0; )
            {
                IObjRef ori = oris[a];
                Remove(ori);
            }
        }

        public virtual void RemovePriorVersions(IObjRef ori)
        {
            if (ori.Version != null)
            {
                CacheKey cacheKey = new CacheKey();
                if (ExistsValue(ori) != null)
                {
                    // if there is a object in the cache with the requested version it
                    // has already been refreshed
                    return;
                }
            }
            Remove(ori);
        }

        public virtual void RemovePriorVersions(IList<IObjRef> oris)
        {
            for (int a = oris.Count; a-- > 0; )
            {
                IObjRef ori = oris[a];
                RemovePriorVersions(ori);
            }
        }

        protected void RemoveCacheValueFromCacheCascade(IEntityMetaData metaData, sbyte idIndex, Object id)
        {
            Type entityType = metaData.EntityType;
            ITypeInfoItem idMember = metaData.GetIdMemberByIdIndex(idIndex);
            id = ConversionHelper.ConvertValueToType(idMember.RealType, id);
            Lock writeLock = WriteLock;
            writeLock.Lock();
            try
            {
                Object cacheValueR = RemoveKeyFromCache(entityType, idIndex, id);
                V cacheValue = GetCacheValueFromReference(cacheValueR);
                if (cacheValue == null)
                {
                    return;
                }
                CacheValueHasBeenRemoved(entityType, idIndex, id, cacheValue);
                Object primaryId = GetIdOfCacheValue(metaData, cacheValue);
                if (primaryId != null)
                {
                    RemoveCacheValueFromCacheSingle(metaData, ObjRef.PRIMARY_KEY_INDEX, GetIdOfCacheValue(metaData, cacheValue));
                }
                CacheKey[] alternateCacheKeys = GetAlternateCacheKeysFromCacheValue(metaData, cacheValue);
                for (int a = alternateCacheKeys.Length; a-- > 0; )
                {
                    CacheKey alternateCacheKey = alternateCacheKeys[a];
                    RemoveKeyFromCache(alternateCacheKey);
                }
                IncreaseVersion();
            }
            finally
            {
                writeLock.Unlock();
            }
        }

        protected Object RemoveKeyFromCache(CacheKey cacheKey)
        {
            if (cacheKey == null)
            {
                return null;
            }
            return RemoveKeyFromCache(cacheKey.EntityType, cacheKey.IdNameIndex, cacheKey.Id);
        }

        protected virtual Object RemoveKeyFromCache(Type entityType, sbyte idIndex, Object id)
        {
            if (entityType == null)
            {
                return null;
            }
            return this.keyToCacheValueDict.Remove(entityType, idIndex, id);
        }

        public abstract Object CreateCacheValueInstance(IEntityMetaData metaData, Object obj);

        protected abstract Object GetIdOfCacheValue(IEntityMetaData metaData, V cacheValue);

        protected abstract void SetIdOfCacheValue(IEntityMetaData metaData, V cacheValue, Object id);

        protected abstract Object GetVersionOfCacheValue(IEntityMetaData metaData, V cacheValue);

        protected abstract void SetVersionOfCacheValue(IEntityMetaData metaData, V cacheValue, Object version);

        protected virtual void SetRelationsOfCacheValue(IEntityMetaData metaData, V cacheValue, Object[] primitives, IObjRef[][] relations)
        {
            // Intended blank
        }

        protected void IncreaseVersion()
        {
            if (++changeVersion == Int32.MaxValue)
            {
                changeVersion = 1;
            }
        }

        protected void RemoveCacheValueFromCacheSingle(IEntityMetaData metaData, sbyte idIndex, Object id)
        {
            ITypeInfoItem idMember = metaData.GetIdMemberByIdIndex(idIndex);
            id = ConversionHelper.ConvertValueToType(idMember.RealType, id);
            RemoveKeyFromCache(metaData.EntityType, idIndex, id);
        }

        protected void RemoveAlternateCacheKeysFromCache(IEntityMetaData metaData, CacheKey[] alternateCacheKeys)
        {
            if (alternateCacheKeys == null)
            {
                return;
            }
            for (int a = alternateCacheKeys.Length; a-- > 0; )
            {
                RemoveKeyFromCache(alternateCacheKeys[a]);
            }
        }

        protected Object CreateReference(V obj)
        {
            if (!WeakEntries)
            {
                return obj;
            }
            return new WeakReference(obj);
        }

        public IList<Object> Put(Object objectToCache)
        {
            HashSet<IObjRef> cascadeNeededORIs = new HashSet<IObjRef>();
            IdentityHashSet<Object> alreadyHandledSet = new IdentityHashSet<Object>();
            List<Object> hardRefsToCacheValue = new List<Object>();
            bool success = AcquireHardRefTLIfNotAlready();
            Lock writeLock = WriteLock;
            writeLock.Lock();
            try
            {
                PutIntern(objectToCache, hardRefsToCacheValue, alreadyHandledSet, cascadeNeededORIs);
                IncreaseVersion();
                return hardRefsToCacheValue;
            }
            finally
            {
                writeLock.Unlock();
                ClearHardRefs(success);
            }
        }

        protected virtual Type GetEntityTypeOfObject(Object obj)
        {
            return obj.GetType();
        }

        protected virtual Object GetIdOfObject(IEntityMetaData metaData, Object obj)
        {
            return metaData.IdMember.GetValue(obj, false);
        }

        protected virtual Object GetVersionOfObject(IEntityMetaData metaData, Object obj)
        {
            ITypeInfoItem versionMember = metaData.VersionMember;
            return versionMember != null ? versionMember.GetValue(obj, false) : null;
        }

        protected virtual Object[] ExtractPrimitives(IEntityMetaData metaData, Object obj)
        {
            return CacheHelper.ExtractPrimitives(metaData, obj);
        }

        protected virtual IObjRef[][] ExtractRelations(IEntityMetaData metaData, Object obj, IList<Object> relationValues)
        {
            return CacheHelper.ExtractRelations(metaData, obj, relationValues);
        }

        protected abstract void PutInternObjRelation(V cacheValue, IEntityMetaData metaData, IObjRelation objRelation, IObjRef[] relationsOfMember);

        protected void PutIntern(Object objectToCache, List<Object> hardRefsToCacheValue, IdentityHashSet<Object> alreadyHandledSet, HashSet<IObjRef> cascadeNeededORIs)
        {
            if (objectToCache == null || !alreadyHandledSet.Add(objectToCache))
            {
                return;
            }
            if (objectToCache.GetType().IsArray)
            {
                Array array = (Array)objectToCache;
                for (int a = array.Length; a-- > 0; )
                {
                    PutIntern(array.GetValue(a), hardRefsToCacheValue, alreadyHandledSet, cascadeNeededORIs);
                }
                return;
            }
            if (objectToCache is IList)
            {
                IList list = (IList)objectToCache;
                for (int a = list.Count; a-- > 0; )
                {
                    PutIntern(list[a], hardRefsToCacheValue, alreadyHandledSet, cascadeNeededORIs);
                }
                return;
            }
            if (objectToCache is IEnumerable)
            {
                foreach (Object item in (IEnumerable)objectToCache)
                {
                    PutIntern(item, hardRefsToCacheValue, alreadyHandledSet, cascadeNeededORIs);
                }
                return;
            }
            if (objectToCache is IObjRelationResult)
            {
                IObjRelationResult objRelationResult = (IObjRelationResult)objectToCache;
                IObjRelation objRelation = objRelationResult.Reference;
                IObjRef objRef = objRelation.ObjRefs[0];
                IEntityMetaData metaData2 = EntityMetaDataProvider.GetMetaData(objRef.RealType);

                Object cacheValueR = GetCacheValueR(metaData2, objRef.IdNameIndex, objRef.Id);
                V cacheValue = GetCacheValueFromReference(cacheValueR);
                if (cacheValue == null)
                {
                    return;
                }
                PutInternObjRelation(cacheValue, metaData2, objRelation, objRelationResult.Relations);
                return;
            }
       		if (objectToCache is ILoadContainer)
		    {
			    PutIntern((ILoadContainer) objectToCache);
                return;
		    }
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(GetEntityTypeOfObject(objectToCache));
            Object key = GetIdOfObject(metaData, objectToCache);

            IList<Object> relationValues = new List<Object>();
            IObjRef[][] relations = ExtractRelations(metaData, objectToCache, relationValues);
            if (key != null)
            {
                Object version = GetVersionOfObject(metaData, objectToCache);

                Object cacheValueR = GetCacheValueR(metaData, ObjRef.PRIMARY_KEY_INDEX, key);
                V cacheValue = GetCacheValueFromReference(cacheValueR);
                bool objectItselfIsUpToDate = false;
                if (cacheValue != null && GetIdOfCacheValue(metaData, cacheValue) != null)
                {
                    // Similar object already cached. Let's see how the version
                    // compares...
                    Object cachedVersion = GetVersionOfCacheValue(metaData, cacheValue);
                    if (cachedVersion != null && cachedVersion.Equals(version))
                    {
                        // Object has even already the same version, so there is
                        // absolutely nothing to do here
                        objectItselfIsUpToDate = true;
                    }
                }
                if (!objectItselfIsUpToDate)
                {
                    Object[] primitives = ExtractPrimitives(metaData, objectToCache);
                    CacheKey[] alternateCacheKeys = ExtractAlternateCacheKeys(metaData, primitives);
                    Object hardRef = PutIntern(metaData, objectToCache, key, version, alternateCacheKeys, primitives, relations);
                    hardRefsToCacheValue.Add(hardRef);
                }
                else
                {
                    hardRefsToCacheValue.Add(cacheValue);
                }
            }

            // Even if it has no id we look for its relations and cache them
            for (int a = relationValues.Count; a-- > 0; )
            {
                PutIntern(relationValues[a], hardRefsToCacheValue, alreadyHandledSet, cascadeNeededORIs);
            }
        }

        protected virtual bool AllowCacheValueReplacement()
        {
            return false;
        }

        protected abstract void PutIntern(ILoadContainer loadContainer);

        protected V PutIntern(IEntityMetaData metaData, Object obj, Object id, Object version, CacheKey[] alternateCacheKeys, Object[] primitives,
                IObjRef[][] relations)
        {
            sbyte idIndex = ObjRef.PRIMARY_KEY_INDEX;
            Object cacheValueR = GetCacheValueR(metaData, idIndex, id);
            V cacheValue = GetCacheValueFromReference(cacheValueR);
            if (cacheValue == null)
            {
                Type entityType = metaData.EntityType;
                cacheValue = (V)CreateCacheValueInstance(metaData, obj);
                cacheValueR = CreateReference(cacheValue);
                id = ConversionHelper.ConvertValueToType(metaData.IdMember.ElementType, id);
                SetIdOfCacheValue(metaData, cacheValue, id);

                keyToCacheValueDict.Put(entityType, idIndex, id, cacheValueR);
                CacheValueHasBeenAdded(idIndex, id, metaData, primitives, relations, cacheValueR);
            }
            else if (obj != null && !Object.ReferenceEquals(cacheValue, obj) && !AllowCacheValueReplacement())
            {
                // If the cache does not allow replacements, do nothing with this put-request
                return cacheValue;
            }
            else
            {
                CacheKey[] oldAlternateIds = ExtractAlternateCacheKeys(metaData, primitives);
                for (int a = oldAlternateIds.Length; a-- > 0; )
                {
                    RemoveKeyFromCache(oldAlternateIds[a]);
                }
                CacheValueHasBeenUpdated(metaData, primitives, relations, cacheValueR);
            }
            CacheValueHasBeenRead(cacheValueR);

            // Create cache entry for the primary id and all alternate ids
            PutAlternateCacheKeysToCache(metaData, alternateCacheKeys, cacheValueR);

            SetVersionOfCacheValue(metaData, cacheValue, version);
            SetRelationsOfCacheValue(metaData, cacheValue, primitives, relations);
            return cacheValue;
        }

        protected void PutAlternateCacheKeysToCache(IEntityMetaData metaData, CacheKey[] alternateCacheKeys, Object cacheValueR)
        {
            for (int a = alternateCacheKeys.Length; a-- > 0; )
            {
                CacheKey alternateCacheKey = alternateCacheKeys[a];
                if (alternateCacheKey != null)
                {
                    keyToCacheValueDict.Put(alternateCacheKey.EntityType, alternateCacheKey.IdNameIndex, alternateCacheKey.Id, cacheValueR);
                }
            }
        }

        protected V GetCacheValue(IEntityMetaData metaData, IObjRef objRef, bool checkVersion)
        {
            Object cacheValueR = GetCacheValueR(metaData, objRef.IdNameIndex, objRef.Id);
            V cacheValue = GetCacheValueFromReference(cacheValueR);
            if (cacheValue == null)
            {
                return default(V);
            }
            if (checkVersion && metaData.VersionMember != null && objRef.Version != null)
            {
                Object cacheVersion = GetVersionOfCacheValue(metaData, cacheValue);
                // Compare operation only works on identical operand types
                Object requestedVersion = ConversionHelper.ConvertValueToType(metaData.VersionMember.ElementType, objRef.Version);

                if (cacheVersion != null && ((IComparable)cacheVersion).CompareTo(requestedVersion) < 0)
                {
                    // requested version is higher than cached version. So this is a cache miss because of outdated information
                    return default(V);
                }
            }
            CacheValueHasBeenRead(cacheValueR);
            return cacheValue;
        }

        protected Object GetCacheValueR(IEntityMetaData metaData, sbyte idIndex, Object id)
        {
            ITypeInfoItem idMember = metaData.GetIdMemberByIdIndex(idIndex);
            id = ConversionHelper.ConvertValueToType(idMember.RealType, id);
            Object cacheValueR = keyToCacheValueDict.Get(metaData.EntityType, idIndex, id);
            CacheValueHasBeenRead(cacheValueR);
            return cacheValueR;
        }

        protected V GetCacheValue(IEntityMetaData metaData, sbyte idNameIndex, Object id)
        {
            Object cacheValueR = GetCacheValueR(metaData, idNameIndex, id);
            return GetCacheValueFromReference(cacheValueR);
        }

        protected virtual void CacheValueHasBeenAdded(sbyte idIndex, Object id, IEntityMetaData metaData, Object[] primitives, IObjRef[][] relations, Object cacheValueR)
        {
            CheckForCleanup();
        }

        protected virtual void CacheValueHasBeenRead(Object cacheValueR)
        {
            if (WeakEntries)
            {
                AddHardRefTL(GetCacheValueFromReference(cacheValueR));
            }
        }

        protected virtual void CacheValueHasBeenUpdated(IEntityMetaData metaData, Object[] primitives, IObjRef[][] relations, Object cacheValueR)
        {
            CheckForCleanup();
        }

        protected virtual void CacheValueHasBeenRemoved(Type entityType, sbyte idIndex, Object id, V cacheValue)
        {
            CheckForCleanup();
        }

        public E GetObject<E>(Object id)
        {
            return (E)GetObject(typeof(E), id);
        }

        public Object GetObject(Type entityType, Object id)
        {
            return GetObject(new ObjRef(entityType, ObjRef.PRIMARY_KEY_INDEX, id, null), CacheDirective.None);
        }

        public Object GetObject(Type entityType, String idName, Object id)
        {
            IEntityMetaData metaData = this.EntityMetaDataProvider.GetMetaData(entityType);
            sbyte idIndex = metaData.GetIdIndexByMemberName(idName);
            return GetObject(new ObjRef(metaData.EntityType, idIndex, id, null), CacheDirective.None);
        }

        public Object GetObject(Type entityType, Object id, CacheDirective cacheDirective)
        {
            return GetObject(new ObjRef(entityType, ObjRef.PRIMARY_KEY_INDEX, id, null), cacheDirective);
        }

        public Object GetObject(Type entityType, String idName, Object id, CacheDirective cacheDirective)
        {
            IEntityMetaData metaData = this.EntityMetaDataProvider.GetMetaData(entityType);
            sbyte idIndex = metaData.GetIdIndexByMemberName(idName);
            return GetObject(new ObjRef(metaData.EntityType, idIndex, id, null), cacheDirective);
        }

        public E GetObject<E>(String idName, Object id)
        {
            return (E)GetObject(typeof(E), idName, id);
        }

        public E GetObject<E>(String idName, Object id, CacheDirective cacheDirective)
        {
            return (E)GetObject(typeof(E), idName, id, cacheDirective);
        }

        public Object GetObject(IObjRef oriToGet, CacheDirective cacheDirective)
        {
            if (oriToGet == null)
            {
                return null;
            }
            IList<IObjRef> orisToGet = new List<IObjRef>(1);
            orisToGet.Add(oriToGet);
            IList<Object> objects = GetObjects(orisToGet, cacheDirective);
            if (objects.Count == 0)
            {
                return null;
            }
            return objects[0];
        }

        public IList<E> GetObjects<E>(params Object[] ids)
        {
            IList<IObjRef> orisToGet = new List<IObjRef>(ids.Length);
            for (int a = 0, size = ids.Length; a < size; a++)
            {
                orisToGet.Add(new ObjRef(typeof(E), ObjRef.PRIMARY_KEY_INDEX, ids[a], null));
            }
            IList<Object> result = GetObjects(orisToGet, CacheDirective.None);
            List<E> realResult = new List<E>(result.Count);
            for (int a = 0, size = result.Count; a < size; a++)
            {
                realResult.Add((E)result[a]);
            }
            return realResult;
        }

        public IList<Object> GetObjects(Type entityType, params Object[] ids)
        {
            IList<IObjRef> orisToGet = new List<IObjRef>(ids.Length);
            for (int a = 0, size = ids.Length; a < size; a++)
            {
                orisToGet.Add(new ObjRef(entityType, ObjRef.PRIMARY_KEY_INDEX, ids[a], null));
            }
            IList<Object> result = GetObjects(orisToGet, CacheDirective.None);
            List<Object> realResult = new List<Object>(result.Count);
            for (int a = 0, size = result.Count; a < size; a++)
            {
                realResult.Add(result[a]);
            }
            return realResult;
        }

        public IList<E> GetObjects<E>(IList<Object> ids)
        {
            IList<IObjRef> orisToGet = new List<IObjRef>(ids.Count);
            for (int a = 0, size = ids.Count; a < size; a++)
            {
                orisToGet.Add(new ObjRef(typeof(E), ObjRef.PRIMARY_KEY_INDEX, ids[a], null));
            }
            IList<Object> result = GetObjects(orisToGet, CacheDirective.None);
            List<E> realResult = new List<E>(result.Count);
            for (int a = 0, size = result.Count; a < size; a++)
            {
                realResult.Add((E)result[a]);
            }
            return realResult;
        }

        public IList<Object> GetObjects(Type entityType, IList<Object> ids)
        {
            IList<IObjRef> orisToGet = new List<IObjRef>(ids.Count);
            for (int a = 0, size = ids.Count; a < size; a++)
            {
                orisToGet.Add(new ObjRef(entityType, ObjRef.PRIMARY_KEY_INDEX, ids[a], null));
            }
            IList<Object> result = GetObjects(orisToGet, CacheDirective.None);
            List<Object> realResult = new List<Object>(result.Count);
            for (int a = 0, size = result.Count; a < size; a++)
            {
                realResult.Add(result[a]);
            }
            return realResult;
        }

        public IList<Object> GetObjects(IObjRef[] orisToGetArray, CacheDirective cacheDirective)
        {
            IList<IObjRef> orisToGet = new List<IObjRef>(orisToGetArray.Length);
            for (int a = 0, size = orisToGetArray.Length; a < size; a++)
            {
                orisToGet.Add(orisToGetArray[a]);
            }
            return GetObjects(orisToGet, cacheDirective);
        }

        public abstract IList<Object> GetObjects(IList<IObjRef> orisToGet, CacheDirective cacheDirective);

        public abstract IList<IObjRelationResult> GetObjRelations(IList<IObjRelation> objRels, CacheDirective cacheDirective);

        protected void CheckForCleanup()
        {
            if (!WeakEntries || ReferenceCleanupInterval.Ticks == 0 || DateTime.Now - lastCleanupTime < ReferenceCleanupInterval)
            {
                return;
            }
            CleanUpIntern(true);
        }

        public void CleanUp()
        {
            if (!WeakEntries)
            {
                return;
            }
            Lock writeLock = WriteLock;
            writeLock.Lock();
            try
            {
                CleanUpIntern(false);
            }
            finally
            {
                writeLock.Unlock();
            }
        }

        protected void CleanUpIntern(bool checkNecessity)
        {
            if (checkNecessity && DateTime.Now - lastCleanupTime < ReferenceCleanupInterval)
            {
                // Another thread might have already done a cleanup on this map
                return;
            }
            DoCleanUpIntern();
            lastCleanupTime = DateTime.Now;
        }

        protected virtual void DoCleanUpIntern()
        {
            List<CacheKey> pendingKeysToRemove = new List<CacheKey>();

            foreach (CacheMapEntry entry in keyToCacheValueDict)
            {
                if (GetCacheValueFromReference(entry.GetValue()) == null)
                {
                    CacheKey cacheKey = new CacheKey();
                    cacheKey.Id = entry.Id;
                    cacheKey.IdNameIndex = entry.IdIndex;
                    cacheKey.EntityType = entry.EntityType;
                    pendingKeysToRemove.Add(cacheKey);
                }
            }
            for (int a = pendingKeysToRemove.Count; a-- > 0; )
            {
                CacheKey pendingKeyToRemove = pendingKeysToRemove[a];
                IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(pendingKeyToRemove.EntityType);
                RemoveCacheValueFromCacheCascade(metaData, pendingKeyToRemove.IdNameIndex, pendingKeyToRemove.Id);
            }
        }

        public int Size()
        {
            Lock readLock = ReadLock;
            readLock.Lock();
            try
            {
                return this.keyToCacheValueDict.Count;
            }
            finally
            {
                readLock.Unlock();
            }
        }

        public void Clear()
        {
            Lock writeLock = WriteLock;
            writeLock.Lock();
            try
            {
                ClearIntern();
                IncreaseVersion();
            }
            finally
            {
                writeLock.Unlock();
            }
        }

        protected virtual void ClearIntern()
        {
            this.keyToCacheValueDict.Clear();
        }

        public virtual void GetContent(HandleContentDelegate handleContentDelegate)
        {
            throw new NotSupportedException("Not implemented");
        }

        public virtual void CascadeLoadPath(Type entityType, String cascadeLoadPath)
        {
            throw new NotSupportedException("Not implemented");
        }

        abstract public int CacheId { get; set; }
    }
}
