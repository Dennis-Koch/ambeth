﻿using System;
using System.Collections;
using System.Collections.Generic;
using System.Reflection;
using System.Threading;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Cache.Config;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Cache.Transfer;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;

#if SILVERLIGHT
using System.Runtime.Serialization;
#else
using System.Runtime.Serialization.Formatters.Binary;
#endif
using System.IO;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Copy;
using De.Osthus.Ambeth.Exceptions;
using De.Osthus.Ambeth.Cache.Collections;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Mixin;
using De.Osthus.Ambeth.Cache.Rootcachevalue;
using De.Osthus.Ambeth.Privilege;
using De.Osthus.Ambeth.Security;
using De.Osthus.Ambeth.Privilege.Model;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Merge.Config;
using De.Osthus.Ambeth.Audit;

namespace De.Osthus.Ambeth.Cache
{
    public class RootCache : AbstractCache<RootCacheValue>, IRootCache, IOfflineListener, ICacheRetriever
    {
		public class RelationObjRefs : HashMap<IObjRef, int>
		{
			private readonly RootCache parent;

			public RelationObjRefs(RootCache parent)
			{
				this.parent = parent;
			}

			protected override bool IsResizeNeeded()
			{
				if (!base.IsResizeNeeded())
				{
					return false;
				}
				parent.DoRelationObjRefsRefresh();
				return base.IsResizeNeeded();
			}
		}

        protected static readonly IDictionary<Type, Array> typeToEmptyArray = new Dictionary<Type, Array>();

        protected static readonly CacheDirective failEarlyCacheValueResultSet = CacheDirective.FailEarly | CacheDirective.CacheValueResult;

#if !SILVERLIGHT
        [ThreadStatic]
        protected static BinaryFormatter formatter = null;
#endif

        static RootCache()
        {
            List<Type> types = new List<Type>();
            ImmutableTypeSet.AddImmutableTypesTo(types);
            types.Add(typeof(Object));
            foreach (Type type in types)
            {
                if (!typeof(void).Equals(type))
                {
                    CreateEmptyArrayEntry(type);
                }
            }
        }

        protected static void CreateEmptyArrayEntry(Type componentType)
        {
            typeToEmptyArray.Add(componentType, Array.CreateInstance(componentType, 0));
        }

        [LogInstance]
        public ILogger Log { private get; set; }

        protected readonly HashMap<IObjRef, int> relationOris;

        protected readonly HashSet<IObjRef> currentPendingKeys = new HashSet<IObjRef>();

        protected readonly InterfaceFastList<RootCacheValue> lruList = new InterfaceFastList<RootCacheValue>();

        protected readonly Lock lruLock = new ReadWriteLock().WriteLock;

        [Autowired]
        public ICacheFactory CacheFactory { protected get; set; }

        [Autowired]
        public ICacheModification CacheModification { protected get; set; }

        [Autowired(Optional = true)]
        public ICacheRetriever CacheRetriever { protected get; set; }

        [Autowired(Optional = true)]
        public IEventQueue EventQueue { protected get; set; }

        [Autowired(Optional = true)]
        public IObjectCopier ObjectCopier { protected get; set; }

        [Autowired]
        public IObjRefFactory ObjRefFactory { protected get; set; }

        [Autowired]
        public IObjRefHelper OriHelper { protected get; set; }

        [Autowired]
        public IPrefetchHelper PrefetchHelper { protected get; set; }

        [Autowired]
        public IRootCacheValueFactory RootCacheValueFactory { protected get; set; }

        [Autowired(Optional = true)]
	    public ISecurityActivation SecurityActivation { protected get; set; }

	    [Autowired(Optional = true)]
	    public ISecurityScopeProvider SecurityScopeProvider { protected get; set; }

        [Autowired(Optional = true)]
		public IPrivilegeProvider PrivilegeProvider { protected get; set; }

		[Autowired(Optional = true)]
		public IVerifyOnLoad VerifyOnLoad { protected get; set; }

        [Property(Mandatory = false)]
        public override bool Privileged { get; set; }

		[Property(CacheConfigurationConstants.CacheLruThreshold, DefaultValue = "0")]
		public int LruThreshold { protected get; set; }

		[Property(MergeConfigurationConstants.SecurityActive, DefaultValue = "false")]
		public bool SecurityActive { protected get; set; }

		[Property(ServiceConfigurationConstants.NetworkClientMode, DefaultValue = "false")]
		public bool IsClientMode { protected get; set; }

		protected long relationObjRefsRefreshThrottleOnGC = 60000; // throttle refresh to at most 1 time per minute

		protected long lastRelationObjRefsRefreshTime;

        protected readonly Lock pendingKeysReadLock, pendingKeysWriteLock;

	    public IRootCache Parent
	    {
            get
            {
		        return CacheRetriever is IRootCache ? (IRootCache) CacheRetriever : null;
            }
	    }

        public override int CacheId { get { return -1; } set { throw new NotSupportedException(); } }

        public IRootCache CurrentRootCache { get { return this; } }

        public RootCache()
        {
            ReadWriteLock pendingKeysRwLock = new ReadWriteLock();
            pendingKeysReadLock = pendingKeysRwLock.ReadLock;
            pendingKeysWriteLock = pendingKeysRwLock.WriteLock;
			relationOris = new RelationObjRefs(this);
        }

        public override void Dispose()
        {
            CacheFactory = null;
            CacheModification = null;
            CacheRetriever = null;
            EventQueue = null;
            ObjectCopier = null;
            OriHelper = null;
            PrefetchHelper = null;

            base.Dispose();
        }

        [Property(CacheConfigurationConstants.SecondLevelCacheWeakActive, DefaultValue = "true")]
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

        protected override bool AllowCacheValueReplacement()
        {
            return true;
        }

        public override Object CreateCacheValueInstance(IEntityMetaData metaData, Object obj)
        {
            return RootCacheValueFactory.CreateRootCacheValue(metaData);
        }

        protected override Object GetIdOfCacheValue(IEntityMetaData metaData, RootCacheValue cacheValue)
        {
            return cacheValue.Id;
        }

        protected override void SetIdOfCacheValue(IEntityMetaData metaData, RootCacheValue cacheValue, Object id)
        {
            cacheValue.Id = id;
        }

        protected override Object GetVersionOfCacheValue(IEntityMetaData metaData, RootCacheValue cacheValue)
        {
            return cacheValue.Version;
        }

        protected override void SetVersionOfCacheValue(IEntityMetaData metaData, RootCacheValue cacheValue, Object version)
        {
            PrimitiveMember versionMember = metaData.VersionMember;
            if (versionMember == null)
            {
                return;
            }
            version = ConversionHelper.ConvertValueToType(versionMember.RealType, version);
            cacheValue.Version = version;
        }

        protected override void SetRelationsOfCacheValue(IEntityMetaData metaData, RootCacheValue cacheValue, Object[] primitives, IObjRef[][] relations)
        {
            cacheValue.SetPrimitives(primitives);
            cacheValue.SetRelations(relations);
        }

        protected bool IsCacheRetrieverCallAllowed(CacheDirective cacheDirective)
	    {
		    if (CacheRetriever == null)
		    {
			    // without a valid cacheRetriever a call is never allowed
			    return false;
		    }
		    if (cacheDirective.HasFlag(CacheDirective.FailEarly))
		    {
			    // with FailEarly a cascading call is never allowed
			    return false;
		    }
		    if (cacheDirective.HasFlag(CacheDirective.FailInCacheHierarchy) && !(CacheRetriever is IRootCache))
		    {
			    // with FailInCacheHierarchy a cascading call is only allowed if the cacheRetriever is itself an instance of IRootCache
			    return false;
		    }
		    // in the end a call is only allowed if it is not forbidden for the current thread
		    return !AbstractCache.FailInCacheHierarchyModeActive;
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
            CheckNotDisposed();
            if (orisToGet == null || orisToGet.Count == 0)
            {
                return new List<Object>(0);
            }
            if (cacheDirective.HasFlag(CacheDirective.NoResult)
                || cacheDirective.HasFlag(CacheDirective.LoadContainerResult)
                || cacheDirective.HasFlag(CacheDirective.CacheValueResult))
            {
                return GetObjects(orisToGet, null, cacheDirective);
            }
            ICacheIntern targetCache;
            if (Privileged && !SecurityActivation.FilterActivated)
            {
                targetCache = (ICacheIntern)CacheFactory.CreatePrivileged(CacheFactoryDirective.SubscribeTransactionalDCE, "RootCache.ADHOC");
            }
            else
            {
                targetCache = (ICacheIntern)CacheFactory.Create(CacheFactoryDirective.SubscribeTransactionalDCE, "RootCache.ADHOC");
            }
            return GetObjects(orisToGet, targetCache, cacheDirective);
        }

        public IList<Object> GetObjects(IList<IObjRef> orisToGet, ICacheIntern targetCache, CacheDirective cacheDirective)
        {
			IVerifyOnLoad verifyOnLoad = this.VerifyOnLoad;
			if (verifyOnLoad == null)
			{
				return GetObjectsIntern(orisToGet, targetCache, cacheDirective);
			}
			return verifyOnLoad.VerifyEntitiesOnLoad(delegate()
			{
				return GetObjectsIntern(orisToGet, targetCache, cacheDirective);
			});
		}

		protected IList<Object> GetObjectsIntern(IList<IObjRef> orisToGet, ICacheIntern targetCache, CacheDirective cacheDirective)
		{
            CheckNotDisposed();
            if (orisToGet == null || orisToGet.Count == 0)
            {
                return new List<Object>(0);
            }
            bool isCacheRetrieverCallAllowed = IsCacheRetrieverCallAllowed(cacheDirective);
            IEventQueue eventQueue = EventQueue;
            if (eventQueue != null)
            {
                eventQueue.Pause(this);
            }
            try
            {
                Lock readLock = ReadLock;
                Lock writeLock = WriteLock;
                ICacheModification cacheModification = this.CacheModification;
                bool oldCacheModificationValue = cacheModification.Active;
                bool acquireSuccess = AcquireHardRefTLIfNotAlready(orisToGet.Count);
                if (!oldCacheModificationValue)
                {
                    cacheModification.Active = true;
                }
                try
                {
                    if (!isCacheRetrieverCallAllowed)
                    {
                        // if the cascading call is not allowed we need no pre-scanning for cache-misses
                        // we have to do our best while we create the result directly
                        readLock.Lock();
                        try
                        {
                            return CreateResult(orisToGet, null, cacheDirective, targetCache, true, null);
                        }
                        finally
                        {
                            readLock.Unlock();
                        }
                    }
                    LockState lockState = writeLock.ReleaseAllLocks();
                    try
                    {
                        while (true)
                        {
                            bool doAnotherRetry;
                            LinkedHashSet<IObjRef> neededObjRefs = new LinkedHashSet<IObjRef>();
                            List<DirectValueHolderRef> pendingValueHolders = new List<DirectValueHolderRef>();
                            IList<Object> result = GetObjectsRetry(orisToGet, targetCache, cacheDirective, out doAnotherRetry, neededObjRefs, pendingValueHolders);
                            while (neededObjRefs.Count > 0)
                            {
                                IList<IObjRef> objRefsToGetCascade = neededObjRefs.ToList();
                                neededObjRefs.Clear();
                                GetObjectsRetry(objRefsToGetCascade, targetCache, cacheDirective, out doAnotherRetry, neededObjRefs, pendingValueHolders);
                            }
                            if (doAnotherRetry)
                            {
                                continue;
                            }
                            if (pendingValueHolders.Count > 0)
                            {
                                PrefetchHelper.Prefetch(pendingValueHolders);
                                continue;
                            }
                            return result;
                        }
                    }
                    finally
                    {
                        writeLock.ReacquireLocks(lockState);
                    }
                }
                finally
                {
                    if (!oldCacheModificationValue)
                    {
                        cacheModification.Active = oldCacheModificationValue;
                    }
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

        protected IList<Object> GetObjectsRetry(IList<IObjRef> orisToGet, ICacheIntern targetCache, CacheDirective cacheDirective, out bool doAnotherRetry,
            LinkedHashSet<IObjRef> neededObjRefs, List<DirectValueHolderRef> pendingValueHolders)
        {
            doAnotherRetry = false;
            List<IObjRef> orisToLoad = new List<IObjRef>();

            Lock readLock = ReadLock;
            Lock writeLock = WriteLock;

			int cacheVersionBeforeLongTimeAction;
			if (BoundThread == null)
			{
				RootCacheValue[] rootCacheValuesToGet = new RootCacheValue[orisToGet.Count];
				cacheVersionBeforeLongTimeAction = WaitForConcurrentReadFinish(orisToGet, rootCacheValuesToGet, orisToLoad);
				if (orisToLoad.Count == 0)
				{
					// Everything found in the cache. We STILL hold the readlock so we can immediately create the result
					// We already even checked the version. So we do not bother with versions anymore here
					try
					{
						return CreateResult(orisToGet, rootCacheValuesToGet, cacheDirective, targetCache, false, null);
					}
					finally
					{
						readLock.Unlock();
					}
				}
			}
			else
			{
				readLock.Lock();
			try
			{
				IList<Object> result = CreateResult(orisToGet, null, cacheDirective, targetCache, false, null);
				if (orisToLoad.Count == 0)
				{
					return result;
				}
				cacheVersionBeforeLongTimeAction = changeVersion;
			}
			finally
			{
				readLock.Unlock();
			}
			}
            int cacheVersionAfterLongTimeAction;
            bool releaseWriteLock = false;
            try
            {
                bool loadSuccess = false;
                try
                {
                    IList<ILoadContainer> loadedEntities;
                    if (Privileged && SecurityActivation != null && SecurityActivation.Secured)
				    {
						loadedEntities = SecurityActivation.ExecuteWithoutSecurity(delegate()
						{
    						return CacheRetriever.GetEntities(orisToLoad);
						});
				    }
				    else
				    {
					    loadedEntities = CacheRetriever.GetEntities(orisToLoad);
				    }

                    // Acquire write lock and mark this state. In the finally-Block the writeLock
                    // has to be released in a deterministic way
                    writeLock.Lock();
                    releaseWriteLock = true;

                    cacheVersionAfterLongTimeAction = this.changeVersion;
                    LoadObjects(loadedEntities, neededObjRefs, pendingValueHolders);

                    loadSuccess = true;

                    ClearPendingKeysOfCurrentThread(orisToLoad);
                    orisToLoad.Clear();

                    if (neededObjRefs.Count > 0 || pendingValueHolders.Count > 0)
                    {
                        writeLock.Unlock();
                        releaseWriteLock = false;
                        return null;
                    }
                }
                finally
                {
                    if (!loadSuccess)
                    {
                        ClearPendingKeysOfCurrentThread(orisToLoad);
                    }
                }
                if (cacheVersionAfterLongTimeAction != cacheVersionBeforeLongTimeAction)
                {
                    // Another thread did some changes (possibly DataChange-Remove actions)
                    // We have to ensure that our result-scope is still valid
                    // We return null to allow a further full retry of getObjects()
                    doAnotherRetry = true;
                    return null;
                }
                // write lock may be acquired already. But this is ok with our custom R/W lock implementation
                readLock.Lock();
                try
                {
					return CreateResult(orisToGet, null, cacheDirective, targetCache, false, orisToLoad);
                }
                finally
                {
                    readLock.Unlock();
                }
            }
            finally
            {
                if (releaseWriteLock)
                {
                    writeLock.Unlock();
                }
            }
        }

        public override IList<IObjRelationResult> GetObjRelations(IList<IObjRelation> objRels, CacheDirective cacheDirective)
        {
            return GetObjRelations(objRels, null, cacheDirective);
        }

        public IList<IObjRelationResult> GetObjRelations(IList<IObjRelation> objRels, ICacheIntern targetCache, CacheDirective cacheDirective)
        {
			IVerifyOnLoad verifyOnLoad = this.VerifyOnLoad;
			if (verifyOnLoad == null)
			{
				return GetObjRelationsIntern(objRels, targetCache, cacheDirective);
			}
			return verifyOnLoad.VerifyEntitiesOnLoad(delegate()
				{
					return GetObjRelationsIntern(objRels, targetCache, cacheDirective);
				});
		}

		protected IList<IObjRelationResult> GetObjRelationsIntern(IList<IObjRelation> objRels, ICacheIntern targetCache, CacheDirective cacheDirective)
		{
            CheckNotDisposed();
            bool isCacheRetrieverCallAllowed = IsCacheRetrieverCallAllowed(cacheDirective);
            bool returnMisses = cacheDirective.HasFlag(CacheDirective.ReturnMisses);
            IList<IObjRelationResult> objRelResults = new List<IObjRelationResult>(objRels.Count);

            IEventQueue eventQueue = EventQueue;
            if (eventQueue != null)
            {
                eventQueue.Pause(this);
            }
            try
            {
                Lock readLock = ReadLock;
                IList<IObjRelation> objRelMisses = new List<IObjRelation>();
                Dictionary<IObjRelation, IObjRelationResult> objRelToResultMap = new Dictionary<IObjRelation, IObjRelationResult>();
                IdentityDictionary<IObjRef, IObjRef> alreadyClonedObjRefs = new IdentityDictionary<IObjRef, IObjRef>();

                ICacheModification cacheModification = this.CacheModification;
                IProxyHelper proxyHelper = this.ProxyHelper;
                bool oldCacheModificationValue = cacheModification.Active;
                bool acquireSuccess = AcquireHardRefTLIfNotAlready(objRels.Count);
                cacheModification.Active = true;
                try
                {
                    IList<IObjRelationResult> result = null;
                    readLock.Lock();
                    try
                    {
                        for (int a = 0, size = objRels.Count; a < size; a++)
                        {
                            IObjRelation objRel = objRels[a];
                            if (targetCache != null && targetCache != this)
                            {
                                IList<Object> cacheResult = targetCache.GetObjects(objRel.ObjRefs, CacheDirective.FailEarly);
                                if (cacheResult.Count > 0)
                                {
                                    IObjRefContainer item = (IObjRefContainer)cacheResult[0]; // Only one hit is necessary of given group of objRefs
                                    int relationIndex = item.Get__EntityMetaData().GetIndexByRelationName(objRel.MemberName);
                                    if (ValueHolderState.INIT == item.Get__State(relationIndex) || item.Get__ObjRefs(relationIndex) != null)
                                    {
                                        continue;
                                    }
                                }
                            }
                            IObjRelationResult selfResult = GetObjRelationIfValid(objRel, targetCache, null, alreadyClonedObjRefs);
                            if (selfResult == null && isCacheRetrieverCallAllowed)
                            {
                                objRelMisses.Add(objRel);
                            }
                        }
                        if (objRelMisses.Count == 0)
                        {
                            // Create result WITHOUT releasing the readlock in the meantime
                            result = CreateResult(objRels, targetCache, null, alreadyClonedObjRefs, returnMisses);
                        }
                    }
                    finally
                    {
                        readLock.Unlock();
                    }
                    if (objRelMisses.Count > 0)
                    {
                        IList<IObjRelationResult> loadedObjectRelations;
                        if (Privileged && SecurityActivation != null && SecurityActivation.Secured)
					    {
							loadedObjectRelations = SecurityActivation.ExecuteWithoutSecurity(delegate()
							{
    							return CacheRetriever.GetRelations(objRelMisses);
							});
					    }
					    else
					    {
						    loadedObjectRelations = CacheRetriever.GetRelations(objRelMisses);
					    }

                        LoadObjects(loadedObjectRelations, objRelToResultMap);
                        readLock.Lock();
                        try
                        {
                            result = CreateResult(objRels, targetCache, objRelToResultMap, alreadyClonedObjRefs, returnMisses);
                        }
                        finally
                        {
                            readLock.Unlock();
                        }
                    }
                    if (IsFilteringNecessary(targetCache))
				    {
                        Lock writeLock = WriteLock;
					    writeLock.Lock();
					    try
					    {
						    result = FilterObjRelResult(result, targetCache);
					    }
					    finally
					    {
						    writeLock.Unlock();
					    }
				    }
                    return result;
                }
                finally
                {
                    cacheModification.Active = oldCacheModificationValue;
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

        protected IObjRelationResult GetObjRelationIfValid(IObjRelation objRel, ICacheIntern targetCache, Dictionary<IObjRelation, IObjRelationResult> objRelToResultMap,
            IdentityDictionary<IObjRef, IObjRef> alreadyClonedObjRefs)
        {
            IList<Object> cacheValues = GetObjects(objRel.ObjRefs, targetCache, failEarlyCacheValueResultSet);
            if (cacheValues.Count == 0)
            {
                if (objRelToResultMap != null)
                {
                    return DictionaryExtension.ValueOrDefault(objRelToResultMap, objRel);
                }
                return null;
            }
            RootCacheValue cacheValue = (RootCacheValue)cacheValues[0];
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(objRel.RealType);
            int index = metaData.GetIndexByRelationName(objRel.MemberName);
            IObjRef[] objRefs = cacheValue.GetRelation(index);

            if (objRefs == null)
            {
                return null;
            }
            ObjRelationResult objRelResult = new ObjRelationResult();
            objRelResult.Reference = objRel;
            objRelResult.Relations = CloneObjectRefArray(objRefs, alreadyClonedObjRefs);
            return objRelResult;
        }

        protected IList<IObjRelationResult> CreateResult(IList<IObjRelation> objRels, ICacheIntern targetCache,
                Dictionary<IObjRelation, IObjRelationResult> objRelToResultMap, IdentityDictionary<IObjRef, IObjRef> alreadyClonedObjRefs, bool returnMisses)
        {
            IObjRefHelper oriHelper = this.OriHelper;
            IList<IObjRelationResult> objRelResults = new List<IObjRelationResult>(objRels.Count);

            for (int a = 0, size = objRels.Count; a < size; a++)
            {
                IObjRelation objRel = objRels[a];
                IList<Object> cacheResult = null;
                if (targetCache != null && targetCache != this)
                {
                    cacheResult = targetCache.GetObjects(objRel.ObjRefs, CacheDirective.FailEarly);
                }
                if (cacheResult == null || cacheResult.Count == 0)
                {
                    IObjRelationResult selfResult = GetObjRelationIfValid(objRel, targetCache, objRelToResultMap, alreadyClonedObjRefs);
                    if (selfResult != null || returnMisses)
                    {
                        objRelResults.Add(selfResult);
                    }
                    continue;
                }
                IObjRefContainer item = (IObjRefContainer)cacheResult[0]; // Only first hit is needed
                IEntityMetaData metaData = item.Get__EntityMetaData();
                int relationIndex = metaData.GetIndexByRelationName(objRel.MemberName);
                RelationMember member = metaData.RelationMembers[relationIndex];

                if (ValueHolderState.INIT != item.Get__State(relationIndex))
                {
                    IObjRef[] objRefs = item.Get__ObjRefs(relationIndex);
                    if (objRefs != null)
                    {
                        ObjRelationResult selfResult = new ObjRelationResult();
                        selfResult.Reference = objRel;
                        selfResult.Relations = CloneObjectRefArray(objRefs, alreadyClonedObjRefs);
                        objRelResults.Add(selfResult);
                    }
                    else
                    {
                        IObjRelationResult selfResult = GetObjRelationIfValid(objRel, targetCache, objRelToResultMap, alreadyClonedObjRefs);
                        if (selfResult != null || returnMisses)
                        {
                            objRelResults.Add(selfResult);
                        }
                    }
                    continue;
                }
                Object memberValue = member.GetValue(item);
                if (memberValue == null)
                {
                    if (returnMisses)
                    {
                        objRelResults.Add(null);
                    }
                    continue;
                }
                IList<IObjRef> oriList = oriHelper.ExtractObjRefList(memberValue, null);

                ObjRelationResult selfResult2 = new ObjRelationResult();
                selfResult2.Reference = objRel;
                selfResult2.Relations = ListUtil.ToArray(oriList);
                objRelResults.Add(selfResult2);
            }
            return objRelResults;
        }

        protected IList<IObjRelationResult> FilterObjRelResult(IList<IObjRelationResult> objRelResults, ICacheIntern targetCache)
	    {
            if (objRelResults.Count == 0 || !IsFilteringNecessary(targetCache))
            {
                return objRelResults;
            }
		    List<IObjRef> permittedObjRefs = new List<IObjRef>(objRelResults.Count);
		    for (int a = 0, size = objRelResults.Count; a < size; a++)
		    {
			    IObjRelationResult objRelResult = objRelResults[a];
			    if (objRelResult == null)
			    {
				    permittedObjRefs.Add(null);
				    continue;
			    }
			    IObjRef[] objRefsOfReference = objRelResult.Reference.ObjRefs;
			    IObjRef primaryObjRef = objRefsOfReference[0];
			    foreach (IObjRef objRefOfReference in objRefsOfReference)
			    {
				    if (objRefOfReference.IdNameIndex == ObjRef.PRIMARY_KEY_INDEX)
				    {
					    primaryObjRef = objRefOfReference;
					    break;
				    }
			    }
			    permittedObjRefs.Add(primaryObjRef);
		    }
            IPrivilege[] privileges = GetPrivilegesByObjRefWithoutReadLock(permittedObjRefs);
		    HashMap<IObjRef, List<int>> relatedObjRefs = new HashMap<IObjRef, List<int>>();
		    for (int index = permittedObjRefs.Count; index-- > 0;)
		    {
			    IPrivilege privilege = privileges[index];
			    if (privilege == null || !privilege.ReadAllowed)
			    {
				    permittedObjRefs[index] = null;
				    continue;
			    }
			    IObjRelationResult objRelResult = objRelResults[index];
			    IObjRef[] relations = objRelResult.Relations;
			    foreach (IObjRef relation in relations)
			    {
				    List<int> intArrayList = relatedObjRefs.Get(relation);
				    if (intArrayList == null)
				    {
                        intArrayList = new List<int>();
					    relatedObjRefs.Put(relation, intArrayList);
				    }
				    intArrayList.Add(index);
			    }
		    }
		    IList<IObjRef> relatedObjRefKeys = relatedObjRefs.KeySet().ToList();
            privileges = GetPrivilegesByObjRefWithoutReadLock(relatedObjRefKeys);
		    for (int a = 0, size = relatedObjRefKeys.Count; a < size; a++)
		    {
			    IPrivilege privilege = privileges[a];
			    if (privilege.ReadAllowed)
			    {
				    continue;
			    }
			    IObjRef relatedObjRefKey = relatedObjRefKeys[a];
			    List<int> intArrayList = relatedObjRefs.Get(relatedObjRefKey);
			    for (int b = 0, sizeB = intArrayList.Count; b < sizeB; b++)
			    {
				    int index = intArrayList[b];
				    IObjRelationResult objRelResult = objRelResults[index];
				    IObjRef[] relations = objRelResult.Relations;
				    bool found = false;
				    for (int c = relations.Length; c-- > 0;)
				    {
					    if (relations[c] != relatedObjRefKey)
					    {
						    continue;
					    }
					    relations[c] = null;
					    found = true;
					    break;
				    }
				    if (!found)
				    {
					    throw new Exception("Must never happen");
				    }
			    }
		    }
		    for (int a = objRelResults.Count; a-- > 0;)
		    {
			    IObjRelationResult objRelResult = objRelResults[a];
			    if (objRelResult == null)
			    {
				    continue;
			    }
			    IObjRef[] relations = objRelResult.Relations;
			    int count = 0;
			    for (int b = relations.Length; b-- > 0;)
			    {
				    if (relations[b] != null)
				    {
					    count++;
				    }
			    }
                if (count == relations.Length)
			    {
				    continue;
			    }
			    IObjRef[] filteredRelations = count > 0 ? new IObjRef[count] : ObjRef.EMPTY_ARRAY;
			    int index = 0;
			    for (int b = relations.Length; b-- > 0;)
			    {
				    IObjRef relation = relations[b];
				    if (relation != null)
				    {
					    filteredRelations[index++] = relation;
				    }
			    }
			    if (index != count)
			    {
				    throw new Exception("Must never happen");
			    }
			    ((ObjRelationResult) objRelResult).Relations = filteredRelations;
		    }
		    return objRelResults;
	    }

        protected IObjRef[] CloneObjectRefArray(IObjRef[] objRefs, IdentityDictionary<IObjRef, IObjRef> alreadyClonedObjRefs)
        {
            if (objRefs == null || objRefs.Length == 0)
            {
                return objRefs;
            }
            // Deep clone of the ObjRefs is important
            // Deep clone of the ObjRefs is important
            IObjRef[] objRefsClone = new IObjRef[objRefs.Length];
            for (int b = objRefs.Length; b-- > 0; )
            {
                IObjRef objRef = objRefs[b];
                if (objRef == null)
                {
                    continue;
                }
                IObjRef objRefClone = DictionaryExtension.ValueOrDefault(alreadyClonedObjRefs, objRef);
                if (objRefClone == null)
                {
                    objRefClone = ObjRefFactory.Dup(objRef);
                    alreadyClonedObjRefs.Add(objRef, objRefClone);
                }
                objRefsClone[b] = objRefClone;
            }
            return objRefsClone;
        }

        protected void LoadObjects(IList<IObjRelationResult> loadedObjectRelations, IDictionary<IObjRelation, IObjRelationResult> objRelToResultMap)
        {
            IEntityMetaDataProvider entityMetaDataProvider = this.EntityMetaDataProvider;
            Lock writeLock = WriteLock;
            writeLock.Lock();
            try
            {
                for (int a = 0, size = loadedObjectRelations.Count; a < size; a++)
                {
                    IObjRelationResult objRelResult = loadedObjectRelations[a];
                    IObjRelation objRel = objRelResult.Reference;

                    objRelToResultMap.Add(objRel, objRelResult);

                    IList<Object> cacheValues = GetObjects(objRel.ObjRefs, CacheDirective.CacheValueResult);
                    if (cacheValues.Count == 0)
                    {
                        continue;
                    }
                    RootCacheValue cacheValue = (RootCacheValue)cacheValues[0]; // Only first hit needed
                    
                    IEntityMetaData metaData = entityMetaDataProvider.GetMetaData(objRel.RealType);
                    int index = metaData.GetIndexByRelationName(objRel.MemberName);
                    UnregisterRelations(cacheValue.GetRelation(index));
                    IObjRef[] relationsOfMember = objRelResult.Relations;
                    if (relationsOfMember.Length == 0)
                    {
                        relationsOfMember = ObjRef.EMPTY_ARRAY;
                    }
                    cacheValue.SetRelation(index, relationsOfMember);
                    RegisterRelations(relationsOfMember);
                }
            }
            finally
            {
                writeLock.Unlock();
            }
        }

        protected int WaitForConcurrentReadFinish(IList<IObjRef> orisToGet, RootCacheValue[] rootCacheValuesToGet, IList<IObjRef> orisToLoad)
        {
            Lock readLock = ReadLock;
            Lock pendingKeysReadLock = this.pendingKeysReadLock;
            HashSet<IObjRef> currentPendingKeys = this.currentPendingKeys;
            IGuiThreadHelper guiThreadHelper = this.GuiThreadHelper;
            while (true)
            {
                bool concurrentPendingItems = false;
                bool releaseReadLock = true;
                readLock.Lock();
                pendingKeysReadLock.Lock();
                try
                {
                    for (int a = 0, size = orisToGet.Count; a < size; a++)
                    {
                        IObjRef oriToGet = orisToGet[a];
                        if (oriToGet == null)
                        {
                            continue;
                        }
                        RootCacheValue cacheValue = ExistsValue(oriToGet);
                        if (cacheValue != null)
                        {
                            rootCacheValuesToGet[a] = cacheValue;
                            continue;
                        }
                        if (currentPendingKeys.Contains(oriToGet))
                        {
                            concurrentPendingItems = true;
                            orisToLoad.Clear();
                            break;
                        }
                        orisToLoad.Add(oriToGet);
                    }
                    if (!concurrentPendingItems && orisToLoad.Count == 0)
                    {
                        // Do not release the readlock, to prohibit concurrent DCEs
                        releaseReadLock = false;
                        return changeVersion;
                    }
                }
                finally
                {
                    pendingKeysReadLock.Unlock();
                    if (releaseReadLock)
                    {
                        readLock.Unlock();
                    }
                }
                if (!concurrentPendingItems)
                {
                    Lock pendingKeysWriteLock = this.pendingKeysWriteLock;
                    pendingKeysWriteLock.Lock();
                    try
                    {
                        foreach (IObjRef objRef in orisToLoad)
                        {
                            currentPendingKeys.Add(objRef);
                        }
                        return changeVersion;
                    }
                    finally
                    {
                        pendingKeysWriteLock.Unlock();
                    }
                }
                if (guiThreadHelper != null && guiThreadHelper.IsInGuiThread())
                {
                    throw new NotSupportedException("It is not allowed to call to method while within specified"
                            + " synchronisation context. If this error currently occurs on client side maybe you are calling from a GUI thread?");
                }
                lock (currentPendingKeys)
                {
                    Monitor.Wait(currentPendingKeys, 5000);
                }
            }
        }

        protected void LoadObjects(IList<ILoadContainer> loadedEntities, ISet<IObjRef> neededORIs, IList<DirectValueHolderRef> pendingValueHolders)
        {
            IEntityMetaDataProvider entityMetaDataProvider = this.EntityMetaDataProvider;
            Lock writeLock = WriteLock;
            writeLock.Lock();
            try
            {
                for (int a = 0, size = loadedEntities.Count; a < size; a++)
                {
                    LoadObject(loadedEntities[a], neededORIs, pendingValueHolders);
                }
            }
            finally
            {
                writeLock.Unlock();
            }
        }

        protected void LoadObject(ILoadContainer loadContainer, ISet<IObjRef> neededORIs, IList<DirectValueHolderRef> pendingValueHolders)
        {
            IObjRef reference = loadContainer.Reference;

            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(reference.RealType);
            Object[] primitives = loadContainer.Primitives;
            CacheKey[] alternateCacheKeys = ExtractAlternateCacheKeys(metaData, primitives);

            RootCacheValue cacheValue = PutIntern(metaData, null, reference.Id, reference.Version, alternateCacheKeys, primitives, loadContainer.Relations);
            if (WeakEntries)
            {
                AddHardRefTL(cacheValue);
            }
            if (pendingValueHolders != null)
            {
                EnsureRelationsExist(cacheValue, metaData, neededORIs, pendingValueHolders);
            }
        }

	    protected override void PutIntern(ILoadContainer loadContainer)
	    {
		    LoadObject(loadContainer, null, null);
	    }

        protected void ClearPendingKeysOfCurrentThread(List<IObjRef> cacheKeysToRemove)
        {
            if (cacheKeysToRemove.Count == 0)
            {
                return;
            }
            Lock pendingKeysWriteLock = this.pendingKeysWriteLock;
            pendingKeysWriteLock.Lock();
            try
            {
                foreach (IObjRef objRef in cacheKeysToRemove)
                {
                    currentPendingKeys.Remove(objRef);
                }
            }
            finally
            {
                pendingKeysWriteLock.Unlock();
            }
            lock (currentPendingKeys)
            {
                Monitor.PulseAll(currentPendingKeys);
            }
        }

        protected bool IsFilteringNecessary(ICacheIntern targetCache)
        {
            return SecurityActive && (Privileged && targetCache != null && !targetCache.Privileged)
                    || (targetCache == null && SecurityActivation != null && SecurityActivation.FilterActivated);
        }

        protected IList<Object> CreateResult(IList<IObjRef> objRefsToGet, RootCacheValue[] rootCacheValuesToGet, CacheDirective cacheDirective, ICacheIntern targetCache, bool checkVersion,
			IList<IObjRef> objRefsToLoad)
        {
            bool loadContainerResult = cacheDirective.HasFlag(CacheDirective.LoadContainerResult);
            bool cacheValueResult = cacheDirective.HasFlag(CacheDirective.CacheValueResult);
            if (targetCache == null && !loadContainerResult && !cacheValueResult)
            {
                return null;
            }
            bool returnMisses = cacheDirective.HasFlag(CacheDirective.ReturnMisses);
            bool targetCacheAccess = !loadContainerResult && !cacheValueResult;
            bool filteringNecessary = IsFilteringNecessary(targetCache);
            int getCount = objRefsToGet.Count;
            IPrivilege[] privilegesOfObjRefsToGet = null;
            if (filteringNecessary)
            {
                IPrivilege[] privileges = GetPrivilegesByObjRefWithoutReadLock(objRefsToGet);
                List<IObjRef> filteredObjRefsToGet = new List<IObjRef>(objRefsToGet.Count);
                privilegesOfObjRefsToGet = new IPrivilege[objRefsToGet.Count];
                RootCacheValue[] filteredRootCacheValuesToGet = rootCacheValuesToGet != null ? new RootCacheValue[objRefsToGet.Count] : null;
                getCount = 0;
                for (int a = 0, size = objRefsToGet.Count; a < size; a++)
                {
                    IPrivilege privilege = privileges[a];
                    if (privilege != null && privilege.ReadAllowed)
                    {
                        getCount++;
                        filteredObjRefsToGet.Add(objRefsToGet[a]);
                        privilegesOfObjRefsToGet[a] = privilege;
                        if (rootCacheValuesToGet != null)
                        {
                            filteredRootCacheValuesToGet[a] = rootCacheValuesToGet[a];
                        }
                    }
                    else
                    {
                        filteredObjRefsToGet.Add(null);
                    }
                }
                rootCacheValuesToGet = filteredRootCacheValuesToGet;
                objRefsToGet = filteredObjRefsToGet;
            }
            if (getCount == 0)
            {
                return new List<Object>(0);
            }
            IEventQueue eventQueue = this.EventQueue;
            if (targetCacheAccess && eventQueue != null)
            {
                eventQueue.Pause(targetCache);
            }
            try
            {
                List<Object> result = new List<Object>(objRefsToGet.Count);
                List<IBackgroundWorkerParamDelegate<IdentityHashSet<IObjRef>>> runnables = null;
                List<IObjRef> tempObjRefList = null;
                IdentityDictionary<IObjRef, IObjRef> alreadyClonedObjRefs = null;
                IdentityHashSet<IObjRef> greyListObjRefs = null;
                for (int a = 0, size = objRefsToGet.Count; a < size; a++)
                {
                    IObjRef objRefToGet = objRefsToGet[a];
                    IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(objRefToGet.RealType);

                    RootCacheValue cacheValue = GetCacheValue(metaData, objRefToGet, checkVersion);
                    if (cacheValue == null) // Cache miss
                    {
                        if (targetCacheAccess)
                        {
                            Object cacheHitObject = targetCache.GetObject(objRefToGet, CacheDirective.FailEarly);
                            if (cacheHitObject != null)
                            {
                                result.Add(cacheHitObject);
                                continue;
                            }
                        }
                        if (returnMisses)
                        {
                            result.Add(null);
                        }
						if (objRefsToLoad != null)
						{
							objRefsToLoad.Add(objRefToGet);
						}
                        // But we already loaded before so we can do nothing now
                        continue;
                    }
                    if (loadContainerResult)
                    {
                        IObjRef[][] relations = cacheValue.GetRelations();
                        LoadContainer loadContainer = new LoadContainer();
                        loadContainer.Reference = ObjRefFactory.CreateObjRef(cacheValue);
                        loadContainer.Primitives = cacheValue.GetPrimitives();
                        
                        if (relations.Length == 0 || !filteringNecessary)
					    {
						    loadContainer.Relations = relations;
						    result.Add(loadContainer);
						    continue;
					    }
					    if (runnables == null)
					    {
						    runnables = new List<IBackgroundWorkerParamDelegate<IdentityHashSet<IObjRef>>>(size);
						    greyListObjRefs = new IdentityHashSet<IObjRef>();
                            alreadyClonedObjRefs = new IdentityDictionary<IObjRef, IObjRef>();
						    tempObjRefList = new List<IObjRef>();
					    }
					    ScanForAllKnownRelations(relations, greyListObjRefs);

					    List<IObjRef> fTempObjRefList = tempObjRefList;
					    IdentityDictionary<IObjRef, IObjRef> fAlreadyClonedObjRefs = alreadyClonedObjRefs;
					    runnables.Add(new IBackgroundWorkerParamDelegate<IdentityHashSet<IObjRef>>(delegate(IdentityHashSet<IObjRef> whiteListObjRefs)
						    {
							    IObjRef[][] whiteListedRelations = FilterRelations(relations, whiteListObjRefs, fTempObjRefList);
							    for (int b = whiteListedRelations.Length; b-- > 0;)
							    {
								    whiteListedRelations[b] = CloneObjectRefArray(whiteListedRelations[b], fAlreadyClonedObjRefs);
							    }
							    loadContainer.Relations = whiteListedRelations;
						    }));
                        result.Add(loadContainer);
                    }
                    else if (cacheValueResult)
                    {
                        result.Add(cacheValue);
                    }
                    else
                    {
                        if (tempObjRefList == null)
                        {
                            tempObjRefList = new List<IObjRef>(1);
                            tempObjRefList.Add(new ObjRef());
                        }
                        Object cacheHitObject = CreateObjectFromScratch(metaData, cacheValue, targetCache, tempObjRefList, filteringNecessary,
                            privilegesOfObjRefsToGet != null ? privilegesOfObjRefsToGet[a] : null);
                        result.Add(cacheHitObject);
                    }
                }
                if (runnables != null)
                {
                    IdentityHashSet<IObjRef> whiteListObjRefs = BuildWhiteListedObjRefs(greyListObjRefs);
                    for (int a = runnables.Count; a-- > 0; )
                    {
                        IBackgroundWorkerParamDelegate<IdentityHashSet<IObjRef>> runnable = runnables[a];
                        runnable(whiteListObjRefs);
                    }
                }
                return result;
            }
            finally
            {
                if (targetCacheAccess && eventQueue != null)
                {
                    eventQueue.Resume(targetCache);
                }
            }
        }

        protected Object CreateObjectFromScratch(IEntityMetaData metaData, RootCacheValue cacheValue, ICacheIntern targetCache,
            List<IObjRef> tempObjRefList, bool filteringNecessary, IPrivilege privilegeOfObjRef)
        {
            Type entityType = cacheValue.EntityType;

            IObjRef tempObjRef = tempObjRefList[0];
            tempObjRef.Id = cacheValue.Id;
            tempObjRef.IdNameIndex = ObjRef.PRIMARY_KEY_INDEX;
            tempObjRef.RealType = entityType;

            Lock targetWriteLock = targetCache.WriteLock;
            targetWriteLock.Lock();
            try
            {
                Object cacheObject = targetCache.GetObjects(tempObjRefList, CacheDirective.FailEarly | CacheDirective.ReturnMisses)[0];
                if (cacheObject != null)
                {
                    return cacheObject;
                }
                cacheObject = targetCache.CreateCacheValueInstance(metaData, null);
                IPropertyChangeConfigurable pcc = null;
			    if (cacheObject is IPropertyChangeConfigurable)
			    {
				    // we deactivate the current PCE processing because we just created the entity
				    // we know that there is no property change listener that might handle the initial PCEs
				    pcc = (IPropertyChangeConfigurable) cacheObject;
				    pcc.Set__PropertyChangeActive(false);
			    }
                UpdateExistingObject(metaData, cacheValue, cacheObject, targetCache, filteringNecessary, privilegeOfObjRef);
                if (pcc != null)
                {
                    pcc.Set__PropertyChangeActive(true);
                }
                metaData.PostLoad(cacheObject);
                return cacheObject;
            }
            finally
            {
                targetWriteLock.Unlock();
            }
        }

        protected void UpdateExistingObject(IEntityMetaData metaData, RootCacheValue cacheValue, Object obj, ICacheIntern targetCache, bool filteringNecessary, IPrivilege privilegeOfObjRef)
        {
            IConversionHelper conversionHelper = ConversionHelper;
            IObjectCopier objectCopier = ObjectCopier;
            IPrivilegeProvider privilegeProvider = PrivilegeProvider;
            Object id = cacheValue.Id;
            Object version = cacheValue.Version;
            metaData.IdMember.SetValue(obj, id);
            if (obj is IParentCacheValueHardRef)
            {
                ((IParentCacheValueHardRef)obj).ParentCacheValueHardRef = cacheValue;
            }
            PrimitiveMember versionMember = metaData.VersionMember;
            if (versionMember != null)
            {
                versionMember.SetValue(obj, version);
            }
            PrimitiveMember[] primitiveMembers = metaData.PrimitiveMembers;

            for (int primitiveIndex = primitiveMembers.Length; primitiveIndex-- > 0; )
            {
                PrimitiveMember primitiveMember = primitiveMembers[primitiveIndex];

                Object primitiveTemplate = null;
			    if (!filteringNecessary)
			    {
				    primitiveTemplate = cacheValue.GetPrimitive(primitiveIndex);
			    }
			    else
			    {
				    if (privilegeOfObjRef == null)
				    {
					    privilegeOfObjRef = privilegeProvider.GetPrivilegeByObjRef(new ObjRef(metaData.EntityType, ObjRef.PRIMARY_KEY_INDEX, id, version));
				    }
				    if (privilegeOfObjRef.GetPrimitivePropertyPrivilege(primitiveIndex).ReadAllowed)
				    {
					    // current user has no permission to read the property of the given entity
					    // so we treat this case as if the property is null/empty anyway
					    // effectively we handle user-specific data-blinding this way
					    primitiveTemplate = cacheValue.GetPrimitive(primitiveIndex);
				    }
			    }

			    if (primitiveTemplate != null && filteringNecessary)
			    {
				    if (privilegeOfObjRef == null)
				    {
					    privilegeOfObjRef = GetPrivilegeByObjRefWithoutReadLock(new ObjRef(metaData.EntityType, ObjRef.PRIMARY_KEY_INDEX, id, version));
				    }
				    if (!privilegeOfObjRef.GetPrimitivePropertyPrivilege(primitiveIndex).ReadAllowed)
				    {
					    // current user has no permission to read the property of the given entity
					    // so we treat this case as if the property is null/empty anyway
					    // effectively we handle user-specific data-blinding this way
					    primitiveTemplate = null;
				    }
			    }
			    Object primitive = null;

			    Type memberType = primitiveMember.RealType;

			    if (ListUtil.IsCollection(memberType))
			    {
				    Object existingCollection = (Object) primitiveMember.GetValue(obj, false);
				    if (existingCollection != null)
				    {
                        ListUtil.ClearList(existingCollection);
					    if (primitiveTemplate == null)
					    {
						    // intended blank
					    }
					    else if (objectCopier != null)
					    {
						    primitive = objectCopier.Clone(primitiveTemplate);
						    primitive = conversionHelper.ConvertValueToType(memberType, primitive);
                            ListUtil.FillList(existingCollection, (IEnumerable) primitive);
					    }
					    else
					    {
						    primitive = CreatePrimitiveFromTemplate(memberType, primitiveTemplate);
                            ListUtil.FillList(existingCollection, (IEnumerable) primitive);
					    }
					    primitive = existingCollection;
				    }
			    }
			    if (primitive == null)
			    {
				    if (primitiveTemplate == null)
				    {
					    if (ListUtil.IsCollection(memberType))
					    {
						    primitive = ListUtil.CreateObservableCollectionOfType(memberType, 0);
					    }
					    else
					    {
						    primitive = null;
					    }
				    }
				    else if (objectCopier != null)
				    {
					    primitive = objectCopier.Clone(primitiveTemplate);
					    primitive = conversionHelper.ConvertValueToType(memberType, primitive);
				    }
				    else
				    {
					    primitive = CreatePrimitiveFromTemplate(memberType, primitiveTemplate);
				    }
				    primitiveMember.SetValue(obj, primitive);
			    }
                if (primitive is IParentEntityAware)
			    {
				    ((IParentEntityAware) primitive).SetParentEntity(obj, primitiveMember);
			    }
            }
            IObjRef[][] relations = cacheValue.GetRelations();
            relations = FilterRelations(relations, filteringNecessary);
            targetCache.AddDirect(metaData, id, version, obj, cacheValue, relations);
        }

        protected IPrivilege GetPrivilegeByObjRefWithoutReadLock(IObjRef objRef)
	    {
            Lock readLock = ReadLock;
            LockState lockState = default(LockState);
            if (Privileged && !readLock.IsWriteLockHeld && readLock.IsReadLockHeld)
            {
                // release the read lock because the PrivilegeProvider MAY request write lock on the privileged cache during rule evaluation
                lockState = readLock.ReleaseAllLocks();
            }
            try
            {
                return PrivilegeProvider.GetPrivilegeByObjRef(objRef);
            }
            finally
            {
                if (lockState.readLockCount > 0 || lockState.writeLockCount > 0)
                {
                    readLock.ReacquireLocks(lockState);
                }
            }
        }

	    protected IPrivilege[] GetPrivilegesByObjRefWithoutReadLock<V>(IList<V> objRefs) where V : IObjRef
	    {
		    Lock readLock = ReadLock;
            LockState lockState = default(LockState);
		    if (Privileged && !readLock.IsWriteLockHeld && readLock.IsReadLockHeld)
		    {
			    // release the read lock because the PrivilegeProvider MAY request write lock on the privileged cache during rule evaluation
			    lockState = readLock.ReleaseAllLocks();
		    }
		    try
		    {
			    return PrivilegeProvider.GetPrivilegesByObjRef(objRefs).GetPrivileges();
		    }
		    finally
		    {
			    if (lockState.readLockCount > 0 || lockState.writeLockCount > 0)
			    {
				    readLock.ReacquireLocks(lockState);
			    }
		    }
	    }

        protected void ScanForAllKnownRelations(IObjRef[][] relations, IdentityHashSet<IObjRef> allKnownRelations)
	    {
		    for (int a = relations.Length; a-- > 0;)
		    {
			    IObjRef[] relationsOfMember = relations[a];
			    if (relationsOfMember == null)
			    {
				    continue;
			    }
			    foreach (IObjRef relationOfMember in relationsOfMember)
			    {
				    if (relationOfMember == null)
				    {
					    continue;
				    }
				    allKnownRelations.Add(relationOfMember);
			    }
		    }
	    }

	    protected IdentityHashSet<IObjRef> BuildWhiteListedObjRefs(IdentityHashSet<IObjRef> greyListObjRefs)
	    {
			IList<IObjRef> greyList = greyListObjRefs.ToList();
			IdentityHashSet<IObjRef> whiteListObjRefs = IdentityHashSet<IObjRef>.Create(greyList.Count);
			IPrivilege[] privileges = GetPrivilegesByObjRefWithoutReadLock(greyList);
		    for (int a = privileges.Length; a-- > 0;)
		    {
			    IPrivilege privilege = privileges[a];
			    if (privilege.ReadAllowed)
			    {
					whiteListObjRefs.Add(greyList[a]);
			    }
		    }
		    return whiteListObjRefs;
	    }

	    protected IObjRef[][] FilterRelations(IObjRef[][] relations, bool filteringNecessary)
	    {
		    if (relations.Length == 0 || !filteringNecessary)
		    {
			    return relations;
		    }
		    IdentityHashSet<IObjRef> allKnownRelations = new IdentityHashSet<IObjRef>();
		    ScanForAllKnownRelations(relations, allKnownRelations);
		    if (allKnownRelations.Count == 0)
		    {
			    // nothing to filter
			    return relations;
		    }
		    IdentityHashSet<IObjRef> whiteListObjRefs = BuildWhiteListedObjRefs(allKnownRelations);
		    return FilterRelations(relations, whiteListObjRefs, null);
	    }

        protected IObjRef[][] FilterRelations(IObjRef[][] relations, IdentityHashSet<IObjRef> whiteListObjRefs, List<IObjRef> tempList)
	    {
            IObjRef[][] filteredRelations = new IObjRef[relations.Length][];
            
            if (tempList == null)
		    {
			    tempList = new List<IObjRef>();
		    }
		    // reuse list instance for performance reasons
            for (int a = relations.Length; a-- > 0; )
            {
                IObjRef[] relationsOfMember = relations[a];
                if (relationsOfMember == null)
                {
                    continue;
                }
                tempList.Clear();
                foreach (IObjRef relationOfMember in relationsOfMember)
                {
                    if (relationOfMember == null)
                    {
                        continue;
                    }
                    if (whiteListObjRefs.Contains(relationOfMember))
                    {
                        tempList.Add(relationOfMember);
                    }
                }
                filteredRelations[a] = tempList.Count > 0 ? tempList.ToArray() : ObjRef.EMPTY_ARRAY;
            }
            return filteredRelations;
        }

        protected Object CreatePrimitiveFromTemplate(Type expectedType, Object primitiveTemplate)
        {
            if (expectedType.IsArray)
            {
                // Deep clone non-empty arrays because they are not immutable like other primitive items
                Type componentType = expectedType.GetElementType();
                if (primitiveTemplate == null)
                {
                    return CreateArray(componentType, 0);
                }
                else if (primitiveTemplate.GetType().IsArray)
                {
                    Array array = (Array)primitiveTemplate;
                    int length = array.Length;
                    if (length == 0)
                    {
                        if (primitiveTemplate.GetType().GetElementType().Equals(componentType))
                        {
                            // At this point an 'immutable' empty array template may be returned directly
                            return primitiveTemplate;
                        }
                        else
                        {
                            return CreateArray(componentType, 0);
                        }
                    }
                    return CopyByValue(primitiveTemplate);
                }
                Array primitive = Array.CreateInstance(componentType, 1);
                primitive.SetValue(primitiveTemplate, 0);
                return primitive;
            }
            else if (primitiveTemplate != null && expectedType.IsAssignableFrom(primitiveTemplate.GetType()))
            {
                // The template itself matches with the expected type. All we have to do is clone the template
                return CopyByValue(primitiveTemplate);
            }
            else if (typeof(IEnumerable).IsAssignableFrom(expectedType) && !typeof(String).Equals(expectedType))
            {
                // Deep clone collections because they are not immutable like other primitive items
                if (primitiveTemplate == null)
                {
                    return ListUtil.CreateObservableCollectionOfType(expectedType, 0);
                }
                MethodInfo addMethod;
                Object[] args = new Object[1];
                if (primitiveTemplate.GetType().IsArray)
                {
                    Array array = (Array)primitiveTemplate;
                    int length = array.Length;
                    Object primitive = ListUtil.CreateObservableCollectionOfType(expectedType, length);
                    if (length == 0)
                    {
                        return primitive;
                    }
                    addMethod = primitive.GetType().GetMethod("Add");
                    // Clone template to access its ITEMS by REFERENCE
                    primitiveTemplate = CopyByValue(primitiveTemplate);
                    for (int a = 0; a < length; a++)
                    {
                        Object item = array.GetValue(a);
                        args[0] = item;
                        addMethod.Invoke(primitive, args);
                    }
                    return primitive;
                }
                else if (primitiveTemplate is IEnumerable && !(primitiveTemplate is String))
                {
                    Object primitive = ListUtil.CreateObservableCollectionOfType(expectedType);
                    addMethod = primitive.GetType().GetMethod("Add");
                    // Clone template to access its ITEMS by REFERENCE
                    primitiveTemplate = CopyByValue(primitiveTemplate);
                    if (primitiveTemplate is IList)
                    {
                        IList listPrimitiveTemplate = (IList)primitiveTemplate;
                        for (int a = 0, size = listPrimitiveTemplate.Count; a < size; a++)
                        {
                            Object item = listPrimitiveTemplate[a];
                            args[0] = item;
                            addMethod.Invoke(primitive, args);
                        }
                    }
                    else
                    {
                        foreach (Object item in (IEnumerable)primitiveTemplate)
                        {
                            args[0] = item;
                            addMethod.Invoke(primitive, args);
                        }
                    }
                    return primitive;
                }
                Object primitiveOne = ListUtil.CreateObservableCollectionOfType(expectedType, 1);
                addMethod = primitiveOne.GetType().GetMethod("Add");
                args[0] = CopyByValue(primitiveTemplate);
                addMethod.Invoke(primitiveOne, args);
                return primitiveOne;
            }
            else if (primitiveTemplate == null)
            {
                return null;
            }
            Object convertedPrimitiveTemplate = ConversionHelper.ConvertValueToType(expectedType, primitiveTemplate);
            // To be sure, that the conversion has really no relation with the original at all, we clone it
            return CopyByValue(convertedPrimitiveTemplate);
        }

        protected Object CreateArray(Type componentType, int size)
        {
            if (size == 0)
            {
                Object array = DictionaryExtension.ValueOrDefault(typeToEmptyArray, componentType);
                if (array == null)
                {
                    array = Array.CreateInstance(componentType, 0);
                }
                return array;
            }
            return Array.CreateInstance(componentType, size);
        }

        protected Object CopyByValue(Object obj)
        {
            if (obj == null)
            {
                return obj;
            }
            Type type = obj.GetType();
            if (ImmutableTypeSet.IsImmutableType(type))
            {
                return obj;
            }
#if SILVERLIGHT
            DataContractSerializer dcs = new DataContractSerializer(type);

            using (MemoryStream ms = new MemoryStream())
            {
                dcs.WriteObject(ms, obj);
                ms.Position = 0;
                return dcs.ReadObject(ms);
            }
#else
            if (formatter == null)
            {
                formatter = new BinaryFormatter();
            }
            using (Stream stream = new MemoryStream())
            {
                formatter.Serialize(stream, obj);
                stream.Seek(0, SeekOrigin.Begin);
                return formatter.Deserialize(stream);
            }
#endif
        }

        protected Object CreateCollectionIfRequested(Type expectedType, int size)
        {
            Type genericType = null;
            if (expectedType.IsGenericType)
            {
                expectedType = expectedType.GetGenericTypeDefinition();
                genericType = expectedType.GetGenericArguments()[0];
            }
            if (typeof(ISet<>).Equals(expectedType))
            {
                Type collectionType = typeof(HashSet<>).MakeGenericType(genericType);
                return Activator.CreateInstance(collectionType);
            }
            else if (typeof(ICollection<>).Equals(expectedType) || typeof(IList<>).Equals(expectedType))
            {
                Type collectionType = typeof(List<>).MakeGenericType(genericType);
                return Activator.CreateInstance(collectionType, size);
            }
            return null;
        }

        public void AddDirect(IEntityMetaData metaData, Object id, Object version, Object primitiveFilledObject, Object parentCacheValueOrArray, IObjRef[][] relations)
        {
            throw new NotSupportedException("Not implemented");
        }

        protected override void CacheValueHasBeenAdded(sbyte idIndex, Object id, IEntityMetaData metaData, Object[] primitives, IObjRef[][] relations, Object cacheValueR)
        {
            base.CacheValueHasBeenAdded(idIndex, id, metaData, primitives, relations, cacheValueR);

            RegisterAllRelations(relations);
        }

        protected override void CacheValueHasBeenRead(Object cacheValueR)
        {
            base.CacheValueHasBeenRead(cacheValueR);
            if (this.LruThreshold == 0)
            {
                // LRU handling disabled
                return;
            }
            RootCacheValue cacheValue = GetCacheValueFromReference(cacheValueR);
            if (cacheValue == null)
            {
                return;
            }
            lruLock.Lock();
            try
            {
                InterfaceFastList<RootCacheValue> lruList = this.lruList;
                // Item in lru list
                lruList.Remove(cacheValue);
                lruList.PushFirst(cacheValue);
                while (lruList.Count > this.LruThreshold)
                {
                    lruList.PopLast(); // Ignore result
                }
            }
            finally
            {
                lruLock.Unlock();
            }
        }

        protected override void CacheValueHasBeenUpdated(IEntityMetaData metaData, Object[] primitives, IObjRef[][] relations, Object cacheValueR)
        {
            base.CacheValueHasBeenUpdated(metaData, primitives, relations, cacheValueR);

			RelationMember[] relationMembers = metaData.RelationMembers;
			RootCacheValue cacheValue = GetCacheValueFromReference(cacheValueR);
			for (int relationIndex = relationMembers.Length; relationIndex-- > 0; )
			{
				UnregisterRelations(cacheValue.GetRelation(relationIndex));
			}
            RegisterAllRelations(relations);
        }

		protected override void CacheValueHasBeenRemoved(IEntityMetaData metaData, sbyte idIndex, Object id, RootCacheValue cacheValue)
        {
			RelationMember[] relationMembers = metaData.RelationMembers;
			for (int relationIndex = relationMembers.Length; relationIndex-- > 0; )
			{
				UnregisterRelations(cacheValue.GetRelation(relationIndex));
			}
            if (this.LruThreshold == 0)
            {
                // LRU handling disabled
                return;
            }
            Lock lruLock = this.lruLock;
            lruLock.Lock();
            try
            {
                // Item in lru list
                this.lruList.Remove(cacheValue);
            }
            finally
            {
                lruLock.Unlock();
            }
			base.CacheValueHasBeenRemoved(metaData, idIndex, id, cacheValue);
        }

	    public override void RemovePriorVersions(IList<IObjRef> oris)
	    {
            Lock writeLock = WriteLock;
            writeLock.Lock();
		    try
		    {
			    for (int a = oris.Count; a-- > 0;)
			    {
				    IObjRef ori = oris[a];
				    base.RemovePriorVersions(ori);
				    UpdateReferenceVersion(ori);
			    }
		    }
		    finally
		    {
			    writeLock.Unlock();
		    }
	    }

	    public override void RemovePriorVersions(IObjRef ori)
	    {
            Lock writeLock = WriteLock;
            writeLock.Lock();
            try
            {
                base.RemovePriorVersions(ori);

                UpdateReferenceVersion(ori);
            }
            finally
            {
                writeLock.Unlock();
            }
        }

        protected void RegisterAllRelations(IObjRef[][] relations)
        {
            if (relations == null)
            {
                return;
            }
            foreach (IObjRef[] methodRelations in relations)
            {
                RegisterRelations(methodRelations);
            }
        }

        protected void RegisterRelations(IObjRef[] relations)
        {
            if (relations == null)
            {
                return;
            }
            HashMap<IObjRef, int> relationOris = this.relationOris;
            for (int i = relations.Length; i-- > 0; )
            {
                IObjRef related = relations[i];
                IObjRef existing = relationOris.GetKey(related);
                if (existing != null)
                {
                    int count = relationOris.Get(existing);
                    relationOris.Put(existing, count + 1);

                    relations[i] = existing;
                }
                else
                {
                    relationOris.Put(related, 1);
                }
            }
        }

        protected void UnregisterAllRelations(IObjRef[][] relations)
        {
            if (relations == null)
            {
                return;
            }
            foreach (IObjRef[] methodRelations in relations)
            {
                UnregisterRelations(methodRelations);
            }
        }

        protected void UnregisterRelations(IObjRef[] relations)
        {
            if (relations == null)
            {
                return;
            }
            HashMap<IObjRef, int> relationOris = this.relationOris;
            for (int i = relations.Length; i-- > 0; )
            {
                IObjRef related = relations[i];
                int count = relationOris.Get(related);
                if (count == 1)
                {
                    relationOris.Remove(related);
                }
                else
                {
                    relationOris.Put(related, count - 1);
                }
            }
        }

        protected void UpdateReferenceVersion(IObjRef ori)
        {
            Object version = ori.Version;
            if (version == null)
            {
                return;
            }
            IObjRef existing = relationOris.GetKey(ori);
            if (existing == null)
            {
                return;
            }

            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(ori.RealType);
            PrimitiveMember versionMember = metaData.VersionMember;
            if (versionMember == null)
            {
                return;
            }
            Object cacheVersion = ConversionHelper.ConvertValueToType(versionMember.ElementType, existing.Version);
            Object currentVersion = ConversionHelper.ConvertValueToType(versionMember.ElementType, version);

            if (cacheVersion == null || ((IComparable)cacheVersion).CompareTo(currentVersion) < 0)
            {
                existing.Version = currentVersion;
            }
        }

        protected override CacheKey[] GetAlternateCacheKeysFromCacheValue(IEntityMetaData metaData, RootCacheValue cacheValue)
        {
            return ExtractAlternateCacheKeys(metaData, cacheValue);
        }

        protected void EnsureRelationsExist(RootCacheValue cacheValue, IEntityMetaData metaData, ISet<IObjRef> cascadeNeededORIs,
            IList<DirectValueHolderRef> pendingValueHolders)
        {
            RelationMember[] relationMembers = metaData.RelationMembers;
            IObjRef[][] relations = cacheValue.GetRelations();
            for (int a = relations.Length; a-- > 0; )
            {
                IObjRef[] relationsOfMember = relations[a];

                RelationMember relationMember = relationMembers[a];

                CascadeLoadMode loadCascadeMode = relationMember.CascadeLoadMode;
                switch (loadCascadeMode)
                {
                    case CascadeLoadMode.DEFAULT:
                    case CascadeLoadMode.LAZY:
                        {
                            break;
                        }
                    case CascadeLoadMode.EAGER:
                        {
                            // Ensure the related RootCacheValues will be loaded - we do not bother here if the relations are known or not yet
                            pendingValueHolders.Add(new IndirectValueHolderRef(cacheValue, relationMember, this));
                            break;
                        }
                    case CascadeLoadMode.EAGER_VERSION:
                        {
                            if (relationsOfMember != null)
                            {
                                // ObjRefs already loaded. Nothing to do
                                continue;
                            }
                            pendingValueHolders.Add(new IndirectValueHolderRef(cacheValue, relationMember, this, true));
                            break;
                        }
                    default:
                        throw RuntimeExceptionUtil.CreateEnumNotSupportedException(loadCascadeMode);
                }
            }
        }

        public bool ApplyValues(Object targetObject, ICacheIntern targetCache, IPrivilege privilege)
        {
            if (targetObject == null)
            {
                return false;
            }
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(targetObject.GetType());
            Object id = metaData.IdMember.GetValue(targetObject, false);
            RootCacheValue cacheValue = GetCacheValue(metaData, ObjRef.PRIMARY_KEY_INDEX, id);
            if (cacheValue == null) // Cache miss
            {
                return false;
            }
            UpdateExistingObject(metaData, cacheValue, targetObject, targetCache, IsFilteringNecessary(targetCache), privilege);
            return true;
        }

        public override void GetContent(HandleContentDelegate handleContentDelegate)
        {
            Lock readLock = this.ReadLock;
            readLock.Lock();
            try
            {
                foreach (CacheMapEntry entry in keyToCacheValueDict)
                {
                    RootCacheValue cacheValue = GetCacheValueFromReference(entry.GetValue());
                    if (cacheValue == null)
                    {
                        return;
                    }
                    handleContentDelegate(entry.EntityType, entry.IdIndex, entry.Id, cacheValue);
                }
            }
            finally
            {
                readLock.Unlock();
            }
        }

        protected override Type GetEntityTypeOfObject(Object obj)
        {
            if (obj is RootCacheValue)
            {
                return ((RootCacheValue)obj).EntityType;
            }
            return base.GetEntityTypeOfObject(obj);
        }

        protected override Object GetIdOfObject(IEntityMetaData metaData, Object obj)
        {
            if (obj is RootCacheValue)
            {
                return ((RootCacheValue)obj).Id;
            }
            return base.GetIdOfObject(metaData, obj);
        }

        protected override Object GetVersionOfObject(IEntityMetaData metaData, Object obj)
        {
            if (obj is RootCacheValue)
            {
                return ((RootCacheValue)obj).Version;
            }
            return base.GetVersionOfObject(metaData, obj);
        }

        protected override Object[] ExtractPrimitives(IEntityMetaData metaData, Object obj)
        {
            if (obj is RootCacheValue)
            {
                return ((RootCacheValue)obj).GetPrimitives();
            }
            return base.ExtractPrimitives(metaData, obj);
        }

        protected override IObjRef[][] ExtractRelations(IEntityMetaData metaData, Object obj, IList<Object> relationValues)
        {
            if (obj is RootCacheValue)
            {
                return ((RootCacheValue)obj).GetRelations();
            }
            return base.ExtractRelations(metaData, obj, relationValues);
        }

        protected override void ClearIntern()
        {
            base.ClearIntern();
            relationOris.Clear();
            lruLock.Lock();
            try
            {
                this.lruList.Clear();
            }
            finally
            {
                lruLock.Unlock();
            }
        }

		protected override int DoCleanUpIntern()
		{
			int cleanupCount = base.DoCleanUpIntern();
			if (cleanupCount > 0 && DateTimeUtil.CurrentTimeMillis() - lastRelationObjRefsRefreshTime >= relationObjRefsRefreshThrottleOnGC)
			{
				DoRelationObjRefsRefresh();
			}
			return cleanupCount;
		}

        protected override void PutInternObjRelation(RootCacheValue cacheValue, IEntityMetaData metaData, IObjRelation objRelation, IObjRef[] relationsOfMember)
        {
            int relationIndex = metaData.GetIndexByRelationName(objRelation.MemberName);
            if (relationsOfMember.Length == 0)
            {
                relationsOfMember = ObjRef.EMPTY_ARRAY;
            }
            cacheValue.SetRelation(relationIndex, relationsOfMember);
        }

        public IList<ILoadContainer> GetEntities(IList<IObjRef> orisToLoad)
        {
            IList<Object> result = GetObjects(orisToLoad, CacheDirective.LoadContainerResult);
            List<ILoadContainer> typedResult = new List<ILoadContainer>(result.Count);
            for (int a = 0, size = result.Count; a < size; a++)
            {
                typedResult.Add((ILoadContainer)result[a]);
            }
            return typedResult;
        }

        public IList<IObjRelationResult> GetRelations(IList<IObjRelation> objRelations)
        {
            return GetObjRelations(objRelations, CacheDirective.None);
        }

        public void BeginOnline()
        {
            Clear();
        }

        public void HandleOnline()
        {
            // Intended blank
        }

        public void EndOnline()
        {
            // Intended blank
        }

        public void BeginOffline()
        {
            Clear();
        }

        public void HandleOffline()
        {
            // Intended blank
        }

        public void EndOffline()
        {
            // Intended blank
        }

        public void AssignEntityToCache(Object entity)
        {
            throw new NotSupportedException();
        }

		protected void DoRelationObjRefsRefresh()
		{
			lastRelationObjRefsRefreshTime = DateTimeUtil.CurrentTimeMillis();
			if (!WeakEntries)
			{
				return;
			}
			int zero = 0;
			foreach (Entry<IObjRef, int> entry in relationOris)
			{
				entry.Value = zero;
			}
			IdentityHashSet<RootCacheValue> alreadyHandledSet = IdentityHashSet<RootCacheValue>.Create(keyToCacheValueDict.Count);
			GetContent(delegate(Type entityType, sbyte idIndex, Object id, Object value)
				{
					RootCacheValue cacheValue = (RootCacheValue) value;
					if (!alreadyHandledSet.Add(cacheValue))
					{
						return;
					}
					IEntityMetaData metaData = cacheValue.Get__EntityMetaData();
					for (int relationIndex = metaData.RelationMembers.Length; relationIndex-- > 0;)
					{
						RegisterRelations(cacheValue.GetRelation(relationIndex));
					}
				});

			Iterator<Entry<IObjRef, int>> iter = relationOris.Iterator();
			while (iter.MoveNext())
			{
				Entry<IObjRef, int> entry = iter.Current;
				if (entry.Value == zero)
				{
					iter.Remove();
				}
			}
		}
    }
}
