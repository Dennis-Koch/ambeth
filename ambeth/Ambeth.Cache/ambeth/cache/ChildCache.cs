﻿using System;
using System.Collections;
using System.Collections.Generic;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Cache.Collections;
using De.Osthus.Ambeth.Cache.Config;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Garbageproxy;
using De.Osthus.Ambeth.Security;
using De.Osthus.Ambeth.Merge.Config;

namespace De.Osthus.Ambeth.Cache
{
    public class ChildCache : AbstractCache<Object>, ICacheIntern, IWritableCache, IDisposableCache
    {
        [LogInstance]
        public ILogger Log { private get; set; }

        protected CacheHashMap keyToAlternateIdsMap;

        [Autowired]
        public ICacheModification CacheModification { protected get; set; }

        [Autowired]
        public ICachePathHelper CachePathHelper { protected get; set; }

        [Autowired]
        public IEntityFactory EntityFactory { protected get; set; }

        [Autowired(Optional = true)]
        public IEventQueue EventQueue { protected get; set; }

        [Autowired]
        public IFirstLevelCacheExtendable FirstLevelCacheExtendable { protected get; set; }

        [Autowired]
        public IGarbageProxyFactory GarbageProxyFactory { protected get; set; }

        [Autowired]
        public ICacheIntern Parent { get; set; }

		[Autowired(Optional = true)]
		public ISecurityActivation SecurityActivation { protected get; set; }

		[Property(MergeConfigurationConstants.SecurityActive, DefaultValue = "false")]
		public bool SecurityActive { protected get; set; }

        [Property(CacheConfigurationConstants.ValueholderOnEmptyToOne, DefaultValue = "false")]
        public bool ValueholderOnEmptyToOne { protected get; set; }

        [Property(CacheConfigurationConstants.OverwriteToManyRelationsInChildCache, DefaultValue = "true")]
        public bool OverwriteToManyRelations { protected get; set; }

        [Property]
        public override bool Privileged { get; set; }

        [Property(Mandatory = false)]
	    public String Name { protected get; set; }
        
        protected ICacheIntern gcProxy;

        protected int cacheId;

        [Property(CacheConfigurationConstants.FirstLevelCacheWeakActive, DefaultValue = "true")]
        public override bool WeakEntries
        {
            protected get
            {
                return base.WeakEntries;
            }
            set
            {
                base.WeakEntries = value;
            }
        }

        public override int CacheId
        {
            get
            {
                return cacheId;
            }
            set
            {
                if (this.cacheId != 0 && value != 0)
                {
                    throw new NotSupportedException();
                }
                this.cacheId = value;
            }
        }

        public override void AfterPropertiesSet()
        {
            base.AfterPropertiesSet();

            keyToAlternateIdsMap = new CacheHashMap(CacheMapEntryTypeProvider);

            gcProxy = GarbageProxyFactory.CreateGarbageProxy<ICacheIntern>(this, (IDisposable) null, typeof(IWritableCache));
        }

        public override void Dispose()
        {
            if (CacheId != 0)
            {
                FirstLevelCacheExtendable.UnregisterFirstLevelCache(this, CacheFactoryDirective.NoDCE, false, Name);
            }
			CacheModification = null;
			CachePathHelper = null;
            EntityFactory = null;
            FirstLevelCacheExtendable = null;
			GarbageProxyFactory = null;
			Log = null;
            Parent = null;
            keyToAlternateIdsMap = null;
			SecurityActivation = null;
            base.Dispose();
        }

        protected override void PutIntern(ILoadContainer loadContainer)
	    {
		    throw new NotSupportedException();
	    }

        protected override void CacheValueHasBeenAdded(sbyte idIndex, Object id, IEntityMetaData metaData, Object[] primitives, IObjRef[][] relations, Object cacheValueR)
        {
            base.CacheValueHasBeenAdded(idIndex, id, metaData, primitives, relations, cacheValueR);

            Type entityType = metaData.EntityType;
            CacheKey[] oldAlternateCacheKeys = (CacheKey[])keyToAlternateIdsMap.Get(entityType, idIndex, id);
            if (oldAlternateCacheKeys != null)
            {
                for (int a = oldAlternateCacheKeys.Length; a-- > 0; )
                {
                    CacheKey alternateCacheKey = oldAlternateCacheKeys[a];
                    if (alternateCacheKey != null)
                    {
                        RemoveKeyFromCache(alternateCacheKey);
                    }
                }
            }
            CacheKey[] newAlternateCacheKeys = oldAlternateCacheKeys;
            if (newAlternateCacheKeys == null)
            {
                // Allocate new array to hold alternate ids
                newAlternateCacheKeys = ExtractAlternateCacheKeys(metaData, primitives);
                if (newAlternateCacheKeys.Length > 0)
                {
                    keyToAlternateIdsMap.Put(entityType, idIndex, id, newAlternateCacheKeys);
                }
            }
            else
            {
                // reuse existing array for new alternate id-values
                ExtractAlternateCacheKeys(metaData, primitives, newAlternateCacheKeys);
            }
            PutAlternateCacheKeysToCache(metaData, newAlternateCacheKeys, cacheValueR);
        }

		protected override void CacheValueHasBeenRemoved(IEntityMetaData metaData, sbyte idIndex, object id, object cacheValue)
		{
			((IValueHolderContainer) cacheValue).__TargetCache = null;
			base.CacheValueHasBeenRemoved(metaData, idIndex, id, cacheValue);
		}

        public override Object CreateCacheValueInstance(IEntityMetaData metaData, Object obj)
        {
            if (obj != null)
            {
                return obj;
            }
            if (EntityFactory != null)
            {
                return EntityFactory.CreateEntity(metaData);
            }
            return Activator.CreateInstance(metaData.EntityType);
        }

        protected override Object GetIdOfCacheValue(IEntityMetaData metaData, Object cacheValue)
        {
            return metaData.IdMember.GetValue(cacheValue, false);
        }

        protected override void SetIdOfCacheValue(IEntityMetaData metaData, Object cacheValue, Object id)
        {
            metaData.IdMember.SetValue(cacheValue, id);
        }

        protected override Object GetVersionOfCacheValue(IEntityMetaData metaData, Object cacheValue)
        {
            PrimitiveMember versionMember = metaData.VersionMember;
            if (versionMember == null)
            {
                return null;
            }
            return versionMember.GetValue(cacheValue, false);
        }

        protected override void SetVersionOfCacheValue(IEntityMetaData metaData, Object cacheValue, Object version)
        {
            PrimitiveMember versionMember = metaData.VersionMember;
            if (versionMember == null)
            {
                return;
            }
            version = ConversionHelper.ConvertValueToType(versionMember.RealType, version);
            versionMember.SetValue(cacheValue, version);
        }

        public Object GetObject(IObjRef oriToGet, ICacheIntern targetCache, CacheDirective cacheDirective)
        {
            CheckNotDisposed();
            if (oriToGet == null)
            {
                return null;
            }
            List<IObjRef> orisToGet = new List<IObjRef>(1);
            orisToGet.Add(oriToGet);
            IList<Object> objects = GetObjects(orisToGet, targetCache, cacheDirective);
            if (objects.Count == 0)
            {
                return null;
            }
            return objects[0];
        }

        public override IList<Object> GetObjects(IList<IObjRef> orisToGet, CacheDirective cacheDirective)
        {
            return GetObjects(orisToGet, this, cacheDirective);
        }

        public IList<Object> GetObjects(IList<IObjRef> orisToGet, ICacheIntern targetCache, CacheDirective cacheDirective)
        {
            CheckNotDisposed();
            if (orisToGet == null || orisToGet.Count == 0)
            {
                return new List<Object>(0);
            }
            IEventQueue eventQueue = EventQueue;
            if (eventQueue != null)
            {
                eventQueue.Pause(this);
            }
            try
            {
                bool oldCacheModificationValue = CacheModification.Active;
                bool acquireSuccess = AcquireHardRefTLIfNotAlready(orisToGet.Count);
                CacheModification.Active = true;
                try
                {
                    if (cacheDirective.HasFlag(CacheDirective.LoadContainerResult) || cacheDirective.HasFlag(CacheDirective.CacheValueResult))
                    {
                        return Parent.GetObjects(orisToGet, this, cacheDirective);
                    }
                    bool doAnotherRetry;
                    while (true)
                    {
                        doAnotherRetry = false;
                        IList<Object> result = GetObjectsRetry(orisToGet, cacheDirective, out doAnotherRetry);
                        if (!doAnotherRetry)
                        {
                            return result;
                        }
                    }
                }
                finally
                {
                    CacheModification.Active = oldCacheModificationValue;
                    ClearHardRefs(acquireSuccess);
                }
            }
            finally
            {
                if (eventQueue != null)
                {
                    eventQueue.Resume(this);
                }
            }
        }

        protected IList<Object> GetObjectsRetry(IList<IObjRef> orisToGet, CacheDirective cacheDirective, out bool doAnotherRetry)
        {
            doAnotherRetry = false;
            Lock readLock = ReadLock;
            if (cacheDirective.HasFlag(CacheDirective.FailEarly))
            {
                readLock.Lock();
                try
                {
                    return CreateResult(orisToGet, cacheDirective, true);
                }
                finally
                {
                    readLock.Unlock();
                }
            }
            List<IObjRef> orisToLoad = new List<IObjRef>();
            int cacheVersionBeforeLongTimeAction = WaitForConcurrentReadFinish(orisToGet, orisToLoad);
            if (orisToLoad.Count == 0)
            {
                // Everything found in the cache. We STILL hold the readlock so we can immediately create the result
                // We already even checked the version. So we do not bother version anymore here
                try
                {
                    return CreateResult(orisToGet, cacheDirective, false);
                }
                finally
                {
                    readLock.Unlock();
                }
            }
            CacheDirective parentCacheDirective = CacheDirective.None;
            if (cacheDirective.HasFlag(CacheDirective.FailInCacheHierarchy))
            {
                parentCacheDirective = CacheDirective.FailEarly;
            }
            Parent.GetObjects(orisToLoad, this, parentCacheDirective);
            // Objects do not have to be put, because their were already
            // added by the parent to this cache
            readLock.Lock();
            try
            {
                int cacheVersionAfterLongTimeAction = changeVersion;
                if (cacheVersionAfterLongTimeAction != cacheVersionBeforeLongTimeAction)
                {
                    // Another thread did some changes (possibly DataChange-Remove actions)
                    // We have to ensure that our result-scope is still valid
                    // We return null to allow a further full retry of getObjects()
                    doAnotherRetry = true;
                    return null;
                }
                return CreateResult(orisToGet, cacheDirective, false);
            }
            finally
            {
                readLock.Unlock();
            }
        }

        protected int WaitForConcurrentReadFinish(IList<IObjRef> orisToGet, IList<IObjRef> orisToLoad)
        {
            Lock readLock = ReadLock;
            bool releaseReadLock = true;
            HashSet<IObjRef> objRefsAlreadyQueried = null;
            readLock.Lock();
            try
            {
                for (int a = 0, size = orisToGet.Count; a < size; a++)
                {
                    IObjRef oriToGet = orisToGet[a];
                    if (oriToGet == null || (oriToGet is IDirectObjRef && ((IDirectObjRef)oriToGet).Direct != null))
                    {
                        continue;
                    }
                    Object cacheValue = ExistsValue(oriToGet);
                    if (cacheValue != null)
                    {
                        // Cache hit, but not relevant at this step, so we continue
                        continue;
                    }
                    if (objRefsAlreadyQueried == null)
                    {
                        objRefsAlreadyQueried = new HashSet<IObjRef>();
                    }
                    if (!objRefsAlreadyQueried.Add(oriToGet))
                    {
                        // Object has been already queried from parent
                        // It makes no sense to query it multiple times
                        continue;
                    }
                    orisToLoad.Add(oriToGet);
                }
                if (orisToLoad.Count == 0)
                {
                    releaseReadLock = false;
                }
                return changeVersion;
            }
            finally
            {
                if (releaseReadLock)
                {
                    readLock.Unlock();
                }
            }
        }

        protected IList<Object> CreateResult(IList<IObjRef> orisToGet, CacheDirective cacheDirective, bool checkVersion)
        {
            List<Object> result = new List<Object>(orisToGet.Count);

            bool returnMisses = cacheDirective.HasFlag(CacheDirective.ReturnMisses);

            for (int a = 0, size = orisToGet.Count; a < size; a++)
            {
                IObjRef oriToGet = orisToGet[a];
                if (oriToGet == null)
                {
                    if (returnMisses)
                    {
                        result.Add(null);
                    }
                    continue;
                }
                if (oriToGet is IDirectObjRef)
                {
                    IDirectObjRef dori = (IDirectObjRef)oriToGet;
                    Object entity = dori.Direct;
                    if (entity != null)
                    {
                        result.Add(entity);
                        continue;
                    }
                }
                IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(oriToGet.RealType);
                Object cacheValue = GetCacheValue(metaData, oriToGet, checkVersion);
                if (cacheValue != null || returnMisses)
                {
                    result.Add(cacheValue);
                }
            }
            return result;
        }

        public override IList<IObjRelationResult> GetObjRelations(IList<IObjRelation> objRels, CacheDirective cacheDirective)
        {
            return GetObjRelations(objRels, this, cacheDirective);
        }

        public IList<IObjRelationResult> GetObjRelations(IList<IObjRelation> objRels, ICacheIntern targetCache, CacheDirective cacheDirective)
        {
            CheckNotDisposed();
            IEventQueue eventQueue = EventQueue;
            if (eventQueue != null)
            {
                eventQueue.Pause(this);
            }
            try
            {
                bool oldCacheModificationValue = CacheModification.Active;
                bool acquireSuccess = AcquireHardRefTLIfNotAlready(objRels.Count);
                CacheModification.Active = true;
                try
                {
					if (SecurityActive && ((targetCache == null && Privileged) || (targetCache != null && targetCache.Privileged)//
						&& SecurityActivation != null && SecurityActivation.FilterActivated))
					{
						return SecurityActivation.ExecuteWithoutFiltering(delegate()
						{
							return Parent.GetObjRelations(objRels, targetCache, cacheDirective);
						});
					}
					else
					{
						return Parent.GetObjRelations(objRels, targetCache, cacheDirective);
					}
                }
                finally
                {
                    CacheModification.Active = oldCacheModificationValue;
                    ClearHardRefs(acquireSuccess);
                }
            }
            finally
            {
                if (eventQueue != null)
                {
                    eventQueue.Resume(this);
                }
            }
        }

        public void AddDirect(IEntityMetaData metaData, Object id, Object version, Object primitiveFilledObject, Object parentCacheValueOrArray, IObjRef[][] relations)
        {
            if (id == null)
            {
                throw new Exception("Key must be valid: " + primitiveFilledObject);
            }
            Type entityType = metaData.EntityType;
            sbyte idIndex = ObjRef.PRIMARY_KEY_INDEX;
            CacheKey[] oldAlternateCacheKeys = null;
            Object cacheValue;
            Lock writeLock = WriteLock;
            writeLock.Lock();
            try
            {
                Object cacheValueR = GetCacheValueR(metaData, idIndex, id);
                cacheValue = GetCacheValueFromReference(cacheValueR);

                oldAlternateCacheKeys = (CacheKey[])keyToAlternateIdsMap.Get(entityType, idIndex, id);
                if (oldAlternateCacheKeys != null)
                {
                    for (int a = oldAlternateCacheKeys.Length; a-- > 0; )
                    {
                        RemoveKeyFromCache(oldAlternateCacheKeys[a]);
                    }
                }
                if (cacheValue != null)
                {
                    if (cacheValue != primitiveFilledObject)
                    {
                        throw new Exception("There is already another instance of the same entity in this cache. This is a fatal state");
                    }
                    // Object (same instance) already in cache. Nothing to do here
                }
                else
                {
                    cacheValue = primitiveFilledObject;
                    cacheValueR = CreateReference(cacheValue);

                    this.keyToCacheValueDict.Put(entityType, idIndex, id, cacheValueR);
                }
                CacheKey[] newAlternateCacheKeys = oldAlternateCacheKeys;
                if (newAlternateCacheKeys == null)
                {
                    // Allocate new array to hold alternate ids
                    newAlternateCacheKeys = ExtractAlternateCacheKeys(metaData, parentCacheValueOrArray);
                }
                else
                {
                    // reuse existing array for new alternate id-values
                    ExtractAlternateCacheKeys(metaData, parentCacheValueOrArray, newAlternateCacheKeys);
                }
                if (newAlternateCacheKeys.Length > 0)
                {
                    keyToAlternateIdsMap.Put(entityType, idIndex, id, newAlternateCacheKeys);
                    PutAlternateCacheKeysToCache(metaData, newAlternateCacheKeys, cacheValueR);
                }
            }
            finally
            {
                writeLock.Unlock();
            }
            if (WeakEntries)
            {
                AddHardRefTL(cacheValue);
            }
            AssignEntityToCache(primitiveFilledObject);
            if (relations != null && relations.Length > 0)
            {
                HandleValueHolderContainer((IValueHolderContainer)primitiveFilledObject, metaData.RelationMembers, relations);
            }
            if (primitiveFilledObject is IDataObject)
            {
                ((IDataObject)primitiveFilledObject).ToBeUpdated = false;
            }
        }

        protected void HandleValueHolderContainer(IValueHolderContainer vhc, RelationMember[] relationMembers, IObjRef[][] relations)
        {
            ICacheHelper cacheHelper = this.CacheHelper;
            ICacheIntern parent = this.Parent;
            IProxyHelper proxyHelper = this.ProxyHelper;
            for (int relationIndex = relationMembers.Length; relationIndex-- > 0; )
            {
                RelationMember relationMember = relationMembers[relationIndex];
                IObjRef[] relationsOfMember = relations[relationIndex];

                if (!CascadeLoadMode.EAGER.Equals(relationMember.CascadeLoadMode))
                {
                    if (ValueHolderState.INIT != vhc.Get__State(relationIndex))
                    {
                        // Update ObjRef information within the entity and do nothing else
                        vhc.Set__ObjRefs(relationIndex, relationsOfMember);
                        continue;
                    }
                }
                // We can safely access to relation if we want to
                if (relationsOfMember == null)
                {
                    // Reset value holder state because we do not know the content currently
                    vhc.Set__Uninitialized(relationIndex, null);
                    continue;
                }
                Object relationValue = relationMember.GetValue(vhc);
                if (relationsOfMember.Length == 0)
                {
                    if (!relationMember.IsToMany)
                    {
                        if (relationValue != null)
                        {
                            // Relation has to be flushed
                            relationMember.SetValue(vhc, null);
                        }
                    }
                    else
                    {
                        if (relationValue != null)
                        {
                            // Reuse existing collection
                            ListUtil.ClearList(relationValue);
                        }
                        else
                        {
                            // We have to create a new empty collection
                            relationValue = cacheHelper.CreateInstanceOfTargetExpectedType(relationMember.RealType, relationMember.ElementType);
                            relationMember.SetValue(vhc, relationValue);
                        }
                    }
                    continue;
                }
                // So we know the new content (which is not empty) and we know that the current content is already initialized
                // Now we have to refresh the current content eagerly

                // load entities as if we were an "eager valueholder" here
                IList<Object> potentialNewItems = parent.GetObjects(new List<IObjRef>(relationsOfMember), this, CacheDirective.None);
                if (OverwriteToManyRelations)
                {
                    Object newRelationValue = cacheHelper.ConvertResultListToExpectedType(potentialNewItems, relationMember.RealType,
                            relationMember.ElementType);
                    // Set new to-many-relation, even if there has not changed anything in its item content
                    relationMember.SetValue(vhc, newRelationValue);
                    continue;
                }
                IList<Object> relationItems = ListUtil.AnyToList(relationValue);

                bool diff = (relationItems.Count != potentialNewItems.Count);
                if (!diff)
                {
                    for (int b = potentialNewItems.Count; b-- > 0; )
                    {
                        if (!Object.ReferenceEquals(potentialNewItems[b], relationItems[b]))
                        {
                            diff = true;
                            break;
                        }
                    }
                }
                if (!diff)
                {
                    // Nothing to do
                    continue;
                }
                if (relationValue != null && relationMember.IsToMany)
                {
                    // Reuse existing collection
                    ListUtil.ClearAndFillList(relationValue, relationItems);
                }
                else
                {
                    // We have to create a new empty collection or replace the to-one value
                    Object newRelationValue = cacheHelper.ConvertResultListToExpectedType(potentialNewItems, relationMember.RealType,
                            relationMember.ElementType);
                    relationMember.SetValue(vhc, newRelationValue);
                }
            }
        }

        protected bool IsNotNullRelationValue(Object relationValue)
        {
            return (relationValue != null && (!(relationValue is IDefaultCollection) || !((IDefaultCollection)relationValue).HasDefaultState));
        }

        protected override void ClearIntern()
        {
            base.ClearIntern();
            this.keyToAlternateIdsMap.Clear();
        }

        protected override Object RemoveKeyFromCache(Type entityType, sbyte idIndex, Object id)
        {
            if (entityType == null)
            {
                return null;
            }
            Object cacheValueR = base.RemoveKeyFromCache(entityType, idIndex, id);
            CacheKey[] alternateCacheKeys = (CacheKey[])keyToAlternateIdsMap.Remove(entityType, idIndex, id);
            if (alternateCacheKeys != null)
            {
                for (int a = alternateCacheKeys.Length; a-- > 0; )
                {
                    RemoveKeyFromCache(alternateCacheKeys[a]);
                }
            }
            return cacheValueR;
        }

        protected override CacheKey[] GetAlternateCacheKeysFromCacheValue(IEntityMetaData metaData, Object cacheValue)
        {
            return emptyCacheKeyArray;
        }

        public void HandleChildCaches(HandleChildCachesDelegate handleChildCachesDelegate)
        {
            throw new NotSupportedException("Not implemented");
        }

        public override void GetContent(HandleContentDelegate handleContentDelegate)
        {
            CheckNotDisposed();
            CacheHashMap keyToInstanceMap = new CacheHashMap(CacheMapEntryTypeProvider);
            Lock writeLock = this.WriteLock;
            writeLock.Lock();
            try
            {
                foreach (CacheMapEntry entry in keyToCacheValueDict)
                {
                    Object cacheValue = GetCacheValueFromReference(entry.GetValue());
                    if (cacheValue == null)
                    {
                        continue;
                    }
                    keyToInstanceMap.Put(entry.EntityType, entry.IdIndex, entry.Id, cacheValue);
                }
                foreach (CacheMapEntry entry in keyToInstanceMap)
                {
                    sbyte idIndex = entry.IdIndex;
                    if (idIndex == ObjRef.PRIMARY_KEY_INDEX)
                    {
                        handleContentDelegate(entry.EntityType, idIndex, entry.Id, entry.GetValue());
                    }
                }
            }
            finally
            {
                writeLock.Unlock();
            }
        }
        
        protected override void PutInternObjRelation(Object cacheValue, IEntityMetaData metaData, IObjRelation objRelation, IObjRef[] relationsOfMember)
        {
            int relationIndex = metaData.GetIndexByRelationName(objRelation.MemberName);
            IObjRefContainer vhc = (IObjRefContainer)cacheValue;
            if (ValueHolderState.INIT == vhc.Get__State(relationIndex))
            {
                // It is not allowed to set ObjRefs for an already initialized relation
                return;
            }
            vhc.Set__ObjRefs(relationIndex, relationsOfMember);
        }

        protected override void PutInternUnpersistedEntity(Object entity)
        {
            AssignEntityToCache(entity);		    
            base.PutInternUnpersistedEntity(entity);
        }

        public void AssignEntityToCache(Object entity)
        {
            ((IValueHolderContainer)entity).__TargetCache = gcProxy;
        }
        
	    public override String ToString()
	    {
		    if (Name != null)
		    {
                return Name + " " + base.ToString();
		    }
		    return base.ToString();
	    }
    }
}
