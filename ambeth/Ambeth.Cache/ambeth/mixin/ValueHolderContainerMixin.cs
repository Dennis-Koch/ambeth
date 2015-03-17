using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Cache.Transfer;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Exceptions;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.ComponentModel;
using System.Threading;

namespace De.Osthus.Ambeth.Mixin
{
    public class ValueHolderContainerMixin
    {
        protected readonly QueueGroupKey<DirectValueHolderRef> loadAllPendingValueHoldersQGK;

        protected readonly HashMap<DirectValueHolderRef, List<Object>> vhRefToPendingEventHandlersMap = new HashMap<DirectValueHolderRef, List<Object>>();

        protected readonly HashMap<Type, PropertyChangedEventArgs[]> typeToPceArgsMap = new HashMap<Type, PropertyChangedEventArgs[]>();

        [Autowired]
        public ICacheHelper CacheHelper { protected get; set; }

        [Autowired]
        public ICacheModification CacheModification { protected get; set; }

        [Autowired(Optional = true)]
        public IGuiThreadHelper GuiThreadHelper { protected get; set; }

        [Autowired]
        public IObjRefHelper ObjRefHelper { protected get; set; }

        [Autowired]
        public IPropertyInfoProvider PropertyInfoProvider { protected get; set; }

        [Autowired]
        public IPrefetchHelper PrefetchHelper { protected get; set; }

        [Autowired]
        public IProxyHelper ProxyHelper { protected get; set; }

        [Autowired]
        public IThreadPool ThreadPool { protected get; set; }

        [Autowired(Optional = true)]
        public ILightweightTransaction Transaction { protected get; set; }

        public ValueHolderContainerMixin()
        {
            loadAllPendingValueHoldersQGK = new QueueGroupKey<DirectValueHolderRef>(100, false, LoadAllPendingValueHolders);
        }

        protected void LoadAllPendingValueHolders(IList<DirectValueHolderRef> vhRefs)
        {
            PrefetchHelper.Prefetch(vhRefs);
        }

        protected PropertyChangedEventArgs[] GetPceArgs(Type type)
        {
            PropertyChangedEventArgs[] pceArgs = typeToPceArgsMap.Get(type);
            if (pceArgs == null)
            {
                IPropertyInfo[] props = PropertyInfoProvider.GetProperties(type);
                pceArgs = new PropertyChangedEventArgs[props.Length];
                for (int a = props.Length; a-- > 0; )
                {
                    pceArgs[a] = new PropertyChangedEventArgs(props[a].Name);
                }
                typeToPceArgsMap.Put(type, pceArgs);
            }
            return pceArgs;
        }

        // TODO: This may be obsolete since Ambeth v2.0 ValueHolders
        protected void HandleValueSetAsynchronously(DirectValueHolderRef vhref, INotifyPropertyChanged value)
        {
            List<Object> pendingEventHandlers;
            PropertyChangedEventArgs[] pceArgs;
            lock (vhRefToPendingEventHandlersMap)
            {
                pendingEventHandlers = vhRefToPendingEventHandlersMap.Remove(vhref);
                if (pendingEventHandlers == null || pendingEventHandlers.Count == 0 || value == null)
                {
                    return;
                }
                Type type = ProxyHelper.GetRealType(value.GetType());
                pceArgs = GetPceArgs(type);
            }
            ICacheModification cacheModification = this.CacheModification;
            lock (this)
            {
                bool oldCacheModification = cacheModification.Active;
                cacheModification.Active = true;
                try
                {
                    INotifyCollectionChanged ncc = value is INotifyCollectionChanged ? (INotifyCollectionChanged)value : null;
                    List<Object> backupContentList = null;
                    if (ncc != null)
                    {
                        IList nccList = (IList)ncc;
                        backupContentList = new List<Object>(nccList.Count);
                        for (int a = 0, size = nccList.Count; a < size; a++)
                        {
                            backupContentList.Add(nccList[a]);
                        }
                        nccList.Clear();
                    }
                    for (int a = 0, size = pendingEventHandlers.Count; a < size; a++)
                    {
                        Object pendingEventHandler = pendingEventHandlers[a];
                        if (pendingEventHandler is PropertyChangedEventHandler)
                        {
                            // ToDo: In ManageLabelTemplate screen, we have cases where we delete a LabelTemplate and come to this point while value is null
                            //       => Nullcheck as workaround => Discuss with DeK
                            value.PropertyChanged += (PropertyChangedEventHandler)pendingEventHandler;
                        }
                        else
                        {
                            ncc.CollectionChanged += (NotifyCollectionChangedEventHandler)pendingEventHandler;
                        }
                    }
                    pendingEventHandlers = null; // free reference for GC
                    if (pceArgs != null && value is INotifyPropertyChangedSource)
                    {
                        INotifyPropertyChangedSource npcs = (INotifyPropertyChangedSource)value;
                        for (int a = pceArgs.Length; a-- > 0; )
                        {
                            npcs.OnPropertyChanged(pceArgs[a].PropertyName);
                        }
                    }
                    if (ncc != null)
                    {
                        // Rebuild the list because the collection changed listeners have been registered without knowing the current state of the collection
                        IList nccList = (IList)ncc;
                        for (int a = 0, size = backupContentList.Count; a < size; a++)
                        {
                            nccList.Add(backupContentList[a]);
                        }
                    }
                }
                finally
                {
                    Monitor.PulseAll(this);
                    cacheModification.Active = oldCacheModification;
                }
            }
        }

        public Object GetValue(IValueHolderContainer entity, RelationMember[] relationMembers, int relationIndex, ICacheIntern targetCache, IObjRef[] objRefs)
        {
            return GetValue(entity, relationIndex, relationMembers[relationIndex], targetCache, objRefs, CacheDirective.None);
        }

        public Object GetValue(IValueHolderContainer vhc, int relationIndex)
        {
            return GetValue(vhc, relationIndex, CacheDirective.None);
        }

        public Object GetValue(IObjRefContainer entity, int relationIndex, RelationMember relationMember, ICacheIntern targetCache, IObjRef[] objRefs, CacheDirective cacheDirective)
        {
            Object value;
            if (targetCache == null)
            {
                // This happens if an entity gets newly created and immediately called for relations (e.g. collections to add sth)
                value = CacheHelper.CreateInstanceOfTargetExpectedType(relationMember.RealType, relationMember.ElementType);
            }
            else
            {
                IList<Object> results;
                if (objRefs == null)
                {
                    IObjRelation self = GetSelf(entity, relationIndex);
                    List<IObjRelation> selfs = new List<IObjRelation>(1);
                    selfs.Add(self);

                    if (Transaction != null)
                    {
                        results = Transaction.RunInLazyTransaction(new IResultingBackgroundWorkerDelegate<IList<Object>>(delegate()
                        {
                            IList<IObjRelationResult> objRelResults = targetCache.GetObjRelations(selfs, targetCache, cacheDirective);
                            if (objRelResults.Count == 0)
                            {
                                return EmptyList.Empty<Object>();
                            }
                            else
                            {
                                IObjRelationResult objRelResult = objRelResults[0];
                                return targetCache.GetObjects(new List<IObjRef>(objRelResult.Relations), targetCache, cacheDirective);
                            }
                        }));
                    }
                    else
                    {
                        IList<IObjRelationResult> objRelResults = targetCache.GetObjRelations(selfs, targetCache, cacheDirective);
                        if (objRelResults.Count == 0)
                        {
                            results = EmptyList.Empty<Object>();
                        }
                        else
                        {
                            IObjRelationResult objRelResult = objRelResults[0];
                            results = targetCache.GetObjects(new List<IObjRef>(objRelResult.Relations), targetCache, cacheDirective);
                        }
                    }
                }
                else
                {
                    results = targetCache.GetObjects(new List<IObjRef>(objRefs), targetCache, cacheDirective);
                }
                value = CacheHelper.ConvertResultListToExpectedType(results, relationMember.RealType, relationMember.ElementType);
            }
            return value;
        }

        public Object GetValue(IValueHolderContainer vhc, int relationIndex, CacheDirective cacheDirectiveHint)
        {
            bool isInitialized = vhc.Is__Initialized(relationIndex);
            if (isInitialized)
            {
                return vhc.Get__ValueDirect(relationIndex);
            }
            ParamHolder<bool> lockAcquired = null;
            Monitor.Enter(this);
            try
            {
                // Double-checked locking pattern
                Object value = vhc.Get__ValueDirect(relationIndex);
                if (isInitialized)
                {
                    return value;
                }
                if (value != null && ValueHolderState.PENDING == vhc.Get__State(relationIndex))
                {
                    Monitor.Wait(this);
                    if (!isInitialized)
                    {
                        throw new LazyInitialiationException("Value holder is in invalid state. This is a fatal error");
                    }
                    return value;
                }
                lockAcquired = new ParamHolder<bool>(true);
                EnsureValue(cacheDirectiveHint, true, lockAcquired, (IValueHolderContainer)vhc, relationIndex, value);
            }
            finally
            {
                if (lockAcquired == null || lockAcquired.Value)
                {
                    Monitor.Exit(this);
                }
            }
            return vhc.Get__ValueDirect(relationIndex);
        }

        protected bool EnsureValue(CacheDirective cacheDirectiveHint, bool synchronousResultExpected, IParamHolder<bool> lockAcquired,
            IValueHolderContainer vhc, int relationIndex, Object value)
        {
            IGuiThreadHelper guiThreadHelper = this.GuiThreadHelper;
            bool isInGuiThread = guiThreadHelper != null && guiThreadHelper.IsInGuiThread();
            ValueHolderState state = vhc.Get__State(relationIndex);
            bool initPending = ValueHolderState.PENDING == state;
            if (isInGuiThread && initPending)
            {
                // Content is not really loaded, but instance is available to use (SOLELY for DataBinding in GUI Thread)
                if (value == null && synchronousResultExpected)
                {
                    throw new LazyInitialiationException("Value must be valid at this point");
                }
                return value != null;
            }
            RelationMember member = vhc.Get__EntityMetaData().RelationMembers[relationIndex];
            bool isInitialized = ValueHolderState.INIT == state;
            ICacheIntern targetCache = null;
            Type expectedType = member.RealType;
            Type elementType = member.ElementType;
            ICacheHelper cacheHelper = this.CacheHelper;
            IObjRef[] objRefs;
            lock (this)
            {
                // Double-checked locking pattern
                if (isInitialized)
                {
                    return value != null;
                }
                if (isInGuiThread && initPending)
                {
                    // Content is not really loaded, but instance is available to use (SOLELY for DataBinding in GUI Thread)
                    if (value == null && synchronousResultExpected)
                    {
                        throw new LazyInitialiationException("Value must be valid at this point");
                    }
                    return value != null;
                }
                ICacheModification cacheModification = this.CacheModification;
                bool cacheModificationActive = cacheModification.Active;
                if (isInGuiThread || cacheModificationActive)
                {
                    if (!cacheDirectiveHint.HasFlag(CacheDirective.None))
                    {
                        throw new LazyInitialiationException("It is not valid to access GetValue() with anything other than '" + CacheDirective.None + "'");
                    }

                    bool loadAsync = false;
                    objRefs = vhc.Get__ObjRefs(relationIndex);
                    if (objRefs == null)
                    {
                        IObjRelation self = vhc.Get__Self(relationIndex);
                        if (targetCache == null)
                        {
                            targetCache = vhc.__TargetCache;
                        }
                        IList<IObjRelationResult> temporaryObjRelResults = targetCache.GetObjRelations(new IObjRelation[] { self }, CacheDirective.FailInCacheHierarchy);

                        if (temporaryObjRelResults.Count == 0)
                        {
                            // ObjRelation could not be resolved. So we can not handle the value holder initialization
                            // synchronously. Queue asynchronous action
                            loadAsync = true;
                        }
                        else
                        {
                            objRefs = temporaryObjRelResults[0].Relations;
                            vhc.Set__ObjRefs(relationIndex, objRefs);
                        }
                    }
                    IList<Object> temporaryResults = null;
                    if (objRefs != null)
                    {
                        //bool oldFailEarlyMode = AbstractCache<Object>.FailEarlyModeActive;
                        //AbstractCache<Object>.FailEarlyModeActive = true;
                        //try
                        //{
                        if (objRefs.Length == 0)
                        {
                            temporaryResults = new List<Object>(0);
                        }
                        else
                        {
                            if (targetCache == null)
                            {
                                targetCache = vhc.__TargetCache;
                            }
                            temporaryResults = targetCache.GetObjects(objRefs, CacheDirective.ReturnMisses | CacheDirective.FailInCacheHierarchy);
                            //}
                            //finally
                            //{
                            //    AbstractCache<Object>.FailEarlyModeActive = oldFailEarlyMode;
                            //}
                            for (int a = temporaryResults.Count; a-- > 0; )
                            {
                                if (temporaryResults[a] == null)
                                {
                                    // ObjRef at index 'a' could not be resolved. So we can not handle the value holder initialization
                                    // synchronously. Queue asynchronous action
                                    loadAsync = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!loadAsync)
                    {
                        // It was possible to resolve all objects synchronously because they were all found in the cache already
                        // No need for threading overhead to queue value holder initialization
                        value = cacheHelper.ConvertResultListToExpectedType(temporaryResults, expectedType, elementType);
                        isInitialized = true;

                        member.SetValue(vhc, value);
                        //ProxyHelper.SetInitialized(vhc, member, value);
                        if (value is INotifyPropertyChanged)
                        {
                            HandleValueSetAsynchronously(new DirectValueHolderRef(vhc, member), (INotifyPropertyChanged)value);
                        }
                        return true;
                    }
                    if (cacheDirectiveHint.HasFlag(CacheDirective.FailEarly) || cacheDirectiveHint.HasFlag(CacheDirective.FailInCacheHierarchy))
                    {
                        return false;
                    }
                    if (typeof(IEnumerable).IsAssignableFrom(expectedType))
                    {
                        value = cacheHelper.ConvertResultListToExpectedType(null, expectedType, elementType);
                    }
                    else
                    {
                        // Empty value because the object instance itself will only be available after
                        // all of its (potential multiple) ids have been retrieved and it is possible
                        // for the cache to create ONE instance for all ids in an atomic step.
                        // this is due to the fact that multiple valueholders can refer to the same object
                        // instance by DIFFERENT objRefs (one for each type of unique id / primary id / alternate id)
                        value = null;
                    }
                    initPending = true;
                    vhc.Set__InitPending(relationIndex);
                    ThreadPool.Queue(loadAllPendingValueHoldersQGK, new DirectValueHolderRef(vhc, member));
                    if (value == null && synchronousResultExpected)
                    {
                        throw new LazyInitialiationException("Value must be valid at this point");
                    }
                    return value != null;
                }
            }
            if (lockAcquired != null && lockAcquired.Value)
            {
                // Release lock once more
                Monitor.Exit(this);
                lockAcquired.Value = false;
            }
            objRefs = vhc.Get__ObjRefs(relationIndex);
            // Release vh lock while calling the __TargetCache. This is important due to potential deadlocks between UI & workers
            IList<Object> results = null;
            if (objRefs == null)
            {
                IObjRelation self = GetSelf(vhc, relationIndex);
                if (targetCache == null)
                {
                    targetCache = vhc.__TargetCache;
                }
                IList<IObjRelationResult> objRelResults = targetCache.GetObjRelations(new IObjRelation[] { self }, cacheDirectiveHint);
                if (objRelResults.Count == 0)
                {
                    if (cacheDirectiveHint.HasFlag(CacheDirective.FailEarly) || cacheDirectiveHint.HasFlag(CacheDirective.FailInCacheHierarchy))
                    {
                        return false;
                    }
                }
                objRefs = objRelResults[0].Relations;
                vhc.Set__ObjRefs(relationIndex, objRefs);
            }
            if (objRefs != null)
            {
                if (objRefs.Length == 0)
                {
                    results = new List<Object>(0);
                }
                else
                {
                    if (targetCache == null)
                    {
                        targetCache = vhc.__TargetCache;
                    }
                    results = targetCache.GetObjects(objRefs, cacheDirectiveHint);
                }
            }
            lock (this)
            {
                // Check whether we are still in a valid state or whether our result is outdated
                if (!isInitialized)
                {
                    // We are still valid and are allowed to use our 'results'
                    value = cacheHelper.ConvertResultListToExpectedType(results, expectedType, elementType);
                    isInitialized = true;
                    member.SetValue(vhc, value);
                    if (value is INotifyPropertyChanged)
                    {
                        HandleValueSetAsynchronously(new DirectValueHolderRef(vhc, member), (INotifyPropertyChanged)value);
                    }
                }
                return value != null;
            }
        }

        public bool HasParent(Object parent, Object parentEntity)
        {
            return Object.ReferenceEquals(parentEntity, parent);
        }

        public IObjRelation GetSelf(Object entity, String memberName)
	    {
		    IList<IObjRef> allObjRefs = ObjRefHelper.EntityToAllObjRefs(entity);
		    return new ObjRelation(ListUtil.ToArray(allObjRefs), memberName);
	    }

        public IObjRelation GetSelf(IObjRefContainer entity, int relationIndex)
        {
            String memberName = entity.Get__EntityMetaData().RelationMembers[relationIndex].Name;
            IList<IObjRef> allObjRefs = ObjRefHelper.EntityToAllObjRefs(entity);
            return new ObjRelation(ListUtil.ToArray(allObjRefs), memberName);
        }
        //public class PendingValueHolderItem
        //{
        //    public DirectValueHolderRef vhref;

        //    public PendingValueHolderItem(DirectValueHolderRef vhref)
        //    {
        //        this.vhref = vhref;
        //    }
        //}

        //protected readonly QueueGroupKey<PendingValueHolderItem> loadAllPendingValueHoldersQGK;

        //protected readonly HashMap<DirectValueHolderRef, List<Object>> vhRefToPendingEventHandlersMap = new HashMap<DirectValueHolderRef, List<Object>>();

        //protected readonly HashMap<Type, PropertyChangedEventArgs[]> typeToPceArgsMap = new HashMap<Type, PropertyChangedEventArgs[]>();

        //[Autowired]
        //public ICacheHelper CacheHelper { protected get; set; }

        //[Autowired]
        //public ICacheModification CacheModification { protected get; set; }

        //[Autowired]
        //public IGuiThreadHelper GuiThreadHelper { protected get; set; }

        //[Autowired]
        //public IOriHelper OriHelper { protected get; set; }

        //[Autowired]
        //public IPropertyInfoProvider PropertyInfoProvider { protected get; set; }

        //[Autowired]
        //public IProxyHelper ProxyHelper { protected get; set; }

        //[Autowired]
        //public IThreadPool ThreadPool { protected get; set; }

        //[Autowired]
        //public IValueHolderFactory ValueHolderFactory { protected get; set; }

        //public ValueHolderContainerTemplate()
        //{
        //    loadAllPendingValueHoldersQGK = new QueueGroupKey<PendingValueHolderItem>(100, false, LoadAllPendingValueHolders);
        //}

        //protected void LoadAllPendingValueHolders(IList<PendingValueHolderItem> pendingValueHolderItems)
        //{
        //    HashMap<ICache, IList<IObjRef>> cacheToObjRefs = new HashMap<ICache, IList<IObjRef>>();

        //    IProxyHelper proxyHelper = this.ProxyHelper;
        //    IYieldingExecution yieldingExecution = this.ThreadPool;
        //    int objectCount = 0;
        //    for (int a = pendingValueHolderItems.Count; a-- > 0; )
        //    {
        //        PendingValueHolderItem pendingValueHolderItem = pendingValueHolderItems[a];
        //        DirectValueHolderRef vhref = pendingValueHolderItem.vhref;
        //        Object vhc = vhref.Vhc;
        //        IRelationInfoItem member = vhref.Member;
        //        ICacheIntern targetCache = GetTargetCache(vhref);
        //        IObjRef[] objRefs = proxyHelper.GetObjRefs(vhc, member);
        //        IList<IObjRef> allObjRefs = cacheToObjRefs.Get(targetCache);
        //        if (allObjRefs == null)
        //        {
        //            allObjRefs = new List<IObjRef>();
        //            cacheToObjRefs.Put(targetCache, allObjRefs);
        //        }
        //        objectCount += objRefs.Length;
        //        foreach (IObjRef objRef in objRefs)
        //        {
        //            allObjRefs.Add(objRef);
        //        }
        //    }
        //    IList<Object> loadedObjectsHardRef = new List<Object>(objectCount);
        //    foreach (Entry<ICache, IList<IObjRef>> entry in cacheToObjRefs)
        //    {
        //        ICache targetCache = entry.Key;
        //        IList<IObjRef> objRefs = entry.Value;
        //        IList<Object> cacheResult = targetCache.GetObjects(objRefs, CacheDirective.None);
        //        for (int a = cacheResult.Count; a-- > 0; )
        //        {
        //            loadedObjectsHardRef.Add(cacheResult[a]);
        //        }
        //    }
        //    HashMap<PendingValueHolderItem, IList<Object>> pendingItemToObjects = new HashMap<PendingValueHolderItem, IList<Object>>();
        //    for (int a = pendingValueHolderItems.Count; a-- > 0; )
        //    {
        //        PendingValueHolderItem pendingValueHolderItem = pendingValueHolderItems[a];
        //        DirectValueHolderRef vhref = pendingValueHolderItem.vhref;
        //        Object vhc = vhref.Vhc;
        //        IRelationInfoItem member = vhref.Member;
        //        ICacheIntern targetCache = GetTargetCache(vhref);
        //        IObjRef[] objRefs = proxyHelper.GetObjRefs(vhc, member);
        //        // Objects MUST already have been loaded before, if they were not found, the ObjRef itself has been invalid.
        //        // No need for another failing remote roundtrip, so FailEarly intended
        //        IList<Object> objects = targetCache.GetObjects(objRefs, CacheDirective.FailEarly);
        //        pendingItemToObjects.Put(pendingValueHolderItem, objects);
        //    }
        //    yieldingExecution.Queue(500, FillCollectionsWithDataYielding, pendingItemToObjects);
        //}

        //protected bool FillCollectionsWithDataYielding(IMap<PendingValueHolderItem, IList<Object>> pendingItemToObjects, IYieldingController yieldingController)
        //{
        //    bool finishByOwnDecision = true, atLeastOneActionDone = false;
        //    Object[] addMethodArgs = new Object[1];

        //    ICacheModification cacheModification = this.CacheModification;
        //    IProxyHelper proxyHelper = this.ProxyHelper;
        //    List<PendingValueHolderItem> removes = new List<PendingValueHolderItem>();
        //    try
        //    {
        //        foreach (Entry<PendingValueHolderItem, IList<Object>> entry in pendingItemToObjects)
        //        {
        //            PendingValueHolderItem pendingValueHolderItem = entry.Key;
        //            IList<Object> objects = entry.Value;
        //            if (atLeastOneActionDone && yieldingController.IsShouldYield)
        //            {
        //                finishByOwnDecision = false;
        //                break;
        //            }
        //            atLeastOneActionDone = true;
        //            DirectValueHolderRef vhref = pendingValueHolderItem.vhref;
        //            Object vhc = vhref.Vhc;
        //            IRelationInfoItem member = vhref.Member;
        //            Object value = null;
        //            bool oldCacheModificationActive = cacheModification.Active;
        //            cacheModification.Active = true;
        //            try
        //            {
        //                value = proxyHelper.GetValueDirect(vhc, vhref.Member);
        //                if (value == null || !vhref.Member.IsToMany)
        //                {
        //                    // to-one initialization
        //                    if (objects.Count == 1)
        //                    {
        //                        value = objects[0];
        //                        proxyHelper.SetValueDirect(vhc, member, value);
        //                    }
        //                    else if (objects.Count > 1)
        //                    {
        //                        throw new LazyInitialiationException("A to-one value holder must not refer to " + objects.Count + " entities");
        //                    }
        //                    finishByOwnDecision = true;
        //                    break;
        //                }
        //                MethodInfo addMethod = value.GetType().GetMethod("Add");

        //                for (int b = 0, sizeB = objects.Count; b < sizeB; b++)
        //                {
        //                    Object obj = objects[b];
        //                    if (obj == null)
        //                    {
        //                        // Object already processed
        //                        continue;
        //                    }
        //                    addMethodArgs[0] = obj;
        //                    addMethod.Invoke(value, addMethodArgs);
        //                    // We intentionally value NOT remove the index from the list because of its O(n) complexity
        //                    // And we do not iterate end-to-start because of the potential semantical order of ObjRefs
        //                    objects[b] = null;
        //                    if (b + 1 < sizeB && yieldingController.IsShouldYield)
        //                    {
        //                        // At least one more object, but we do not have any remaining time
        //                        // It is important to set only to false, if the list is NOT already fully processed
        //                        finishByOwnDecision = false;
        //                        break;
        //                    }
        //                }
        //            }
        //            finally
        //            {
        //                cacheModification.Active = oldCacheModificationActive;
        //                if (finishByOwnDecision)
        //                {
        //                    proxyHelper.SetInitialized(vhc, member, value);
        //                    // Remove from map if no item in the objects list is pending to be added
        //                    // This is to ensure in very large scenarios that the map decreases in size to ease further processing
        //                    // Nothing spectacular here
        //                    removes.Add(pendingValueHolderItem);
        //                    if (value is INotifyPropertyChanged)
        //                    {
        //                        HandleValueSetAsynchronously(vhref, (INotifyPropertyChanged)value);
        //                    }
        //                }
        //            }
        //            return true;
        //        }
        //        return finishByOwnDecision;
        //    }
        //    finally
        //    {
        //        foreach (PendingValueHolderItem remove in removes)
        //        {
        //            pendingItemToObjects.Remove(remove);
        //        }
        //    }
        //}

        //protected PropertyChangedEventArgs[] GetPceArgs(Type type)
        //{
        //    PropertyChangedEventArgs[] pceArgs = typeToPceArgsMap.Get(type);
        //    if (pceArgs == null)
        //    {
        //        IPropertyInfo[] props = PropertyInfoProvider.GetProperties(type);
        //        pceArgs = new PropertyChangedEventArgs[props.Length];
        //        for (int a = props.Length; a-- > 0; )
        //        {
        //            pceArgs[a] = new PropertyChangedEventArgs(props[a].Name);
        //        }
        //        typeToPceArgsMap.Put(type, pceArgs);
        //    }
        //    return pceArgs;
        //}

        //protected void HandleValueSetAsynchronously(DirectValueHolderRef vhref, INotifyPropertyChanged value)
        //{
        //    List<Object> pendingEventHandlers;
        //    PropertyChangedEventArgs[] pceArgs;
        //    lock (vhRefToPendingEventHandlersMap)
        //    {
        //        pendingEventHandlers = vhRefToPendingEventHandlersMap.Remove(vhref);
        //        if (pendingEventHandlers == null || pendingEventHandlers.Count == 0 || value == null)
        //        {
        //            return;
        //        }
        //        Type type = ProxyHelper.GetRealType(value.GetType());
        //        pceArgs = GetPceArgs(type);
        //    }
        //    ICacheModification cacheModification = this.CacheModification;
        //    lock (this)
        //    {
        //        bool oldCacheModification = cacheModification.Active;
        //        cacheModification.Active = true;
        //        try
        //        {
        //            INotifyCollectionChanged ncc = value is INotifyCollectionChanged ? (INotifyCollectionChanged)value : null;
        //            List<Object> backupContentList = null;
        //            if (ncc != null)
        //            {
        //                IList nccList = (IList)ncc;
        //                backupContentList = new List<Object>(nccList.Count);
        //                for (int a = 0, size = nccList.Count; a < size; a++)
        //                {
        //                    backupContentList.Add(nccList[a]);
        //                }
        //                nccList.Clear();
        //            }
        //            for (int a = 0, size = pendingEventHandlers.Count; a < size; a++)
        //            {
        //                Object pendingEventHandler = pendingEventHandlers[a];
        //                if (pendingEventHandler is PropertyChangedEventHandler)
        //                {
        //                    // ToDo: In ManageLabelTemplate screen, we have cases where we delete a LabelTemplate and come to this point while value is null
        //                    //       => Nullcheck as workaround => Discuss with DeK
        //                    value.PropertyChanged += (PropertyChangedEventHandler)pendingEventHandler;
        //                }
        //                else
        //                {
        //                    ncc.CollectionChanged += (NotifyCollectionChangedEventHandler)pendingEventHandler;
        //                }
        //            }
        //            pendingEventHandlers = null; // free reference for GC
        //            if (pceArgs != null && value is INotifyPropertyChangedSource)
        //            {
        //                INotifyPropertyChangedSource npcs = (INotifyPropertyChangedSource)value;
        //                for (int a = pceArgs.Length; a-- > 0; )
        //                {
        //                    npcs.OnPropertyChanged(pceArgs[a].PropertyName);
        //                }
        //            }
        //            if (ncc != null)
        //            {
        //                // Rebuild the list because the collection changed listeners have been registered without knowing the current state of the collection
        //                IList nccList = (IList)ncc;
        //                for (int a = 0, size = backupContentList.Count; a < size; a++)
        //                {
        //                    nccList.Add(backupContentList[a]);
        //                }
        //            }
        //        }
        //        finally
        //        {
        //            Monitor.PulseAll(this);
        //            cacheModification.Active = oldCacheModification;
        //        }
        //    }
        //}

        //public Object GetValue(Object vhc, IRelationInfoItem member)
        //{
        //    return GetValue(CacheDirective.None, vhc, member);
        //}

        //public Object GetValue(CacheDirective cacheDirectiveHint, Object vhc, IRelationInfoItem member)
        //{
        //    IProxyHelper proxyHelper = this.ProxyHelper;
        //    bool? isInitializedV = proxyHelper.IsInitialized(vhc, member);
        //    bool isInitialized = isInitializedV.HasValue ? isInitializedV.Value : false;
        //    if (isInitialized)
        //    {
        //        return proxyHelper.GetValueDirect(vhc, member);
        //    }
        //    ParamHolder<bool> lockAcquired = null;
        //    Monitor.Enter(this);
        //    try
        //    {
        //        // Double-checked locking pattern
        //        Object value = proxyHelper.GetValueDirect(vhc, member);
        //        if (isInitialized)
        //        {
        //            return value;
        //        }
        //        if (value != null && proxyHelper.GetInitPending(vhc, member))
        //        {
        //            Monitor.Wait(this);
        //            if (!isInitialized)
        //            {
        //                throw new LazyInitialiationException("Value holder is in invalid state. This is a fatal error");
        //            }
        //            return value;
        //        }
        //        lockAcquired = new ParamHolder<bool>(true);
        //        EnsureValue(cacheDirectiveHint, true, lockAcquired, new DirectValueHolderRef(vhc, member), value);
        //    }
        //    finally
        //    {
        //        if (lockAcquired == null || lockAcquired.Value)
        //        {
        //            Monitor.Exit(this);
        //        }
        //    }
        //    return proxyHelper.GetValueDirect(vhc, member);
        //}

        //protected Object GetParentEntity(DirectValueHolderRef vhref)
        //{
        //    Object vhc = vhref.Vhc;
        //    if (vhc is ValueHolder)
        //    {
        //        return ((ValueHolder)vhc).ParentEntity;
        //    }
        //    return vhc;
        //}

        //protected ICacheIntern GetTargetCache(DirectValueHolderRef vhref)
        //{
        //    Object vhc = vhref.Vhc;
        //    if (vhc is ValueHolder)
        //    {
        //        return ((ValueHolder)vhc).__TargetCache;
        //    }
        //    return ((IValueHolderContainer)vhc).__TargetCache;
        //}

        //protected bool EnsureValue(CacheDirective cacheDirectiveHint, bool synchronousResultExpected, IParamHolder<bool> lockAcquired,
        //    DirectValueHolderRef vhref, Object value)
        //{
        //    IGuiThreadHelper guiThreadHelper = this.GuiThreadHelper;
        //    IProxyHelper proxyHelper = this.ProxyHelper;
        //    Object vhc = vhref.Vhc;
        //    IRelationInfoItem member = vhref.Member;
        //    bool isInGuiThread = guiThreadHelper != null && guiThreadHelper.IsInGuiThread();
        //    bool initPending = proxyHelper.GetInitPending(vhc, member);
        //    if (isInGuiThread && initPending)
        //    {
        //        // Content is not really loaded, but instance is available to use (SOLELY for DataBinding in GUI Thread)
        //        if (value == null && synchronousResultExpected)
        //        {
        //            throw new LazyInitialiationException("Value must be valid at this point");
        //        }
        //        return value != null;
        //    }
        //    bool? isInitializedV = proxyHelper.IsInitialized(vhc, member);
        //    bool isInitialized = isInitializedV.HasValue ? isInitializedV.Value : false;
        //    ICacheIntern targetCache = null;
        //    Type expectedType = member.RealType;
        //    Type elementType = member.ElementType;
        //    ICacheHelper cacheHelper = this.CacheHelper;
        //    IObjRef[] objRefs;
        //    lock (this)
        //    {
        //        // Double-checked locking pattern
        //        if (isInitialized)
        //        {
        //            return value != null;
        //        }
        //        if (isInGuiThread && initPending)
        //        {
        //            // Content is not really loaded, but instance is available to use (SOLELY for DataBinding in GUI Thread)
        //            if (value == null && synchronousResultExpected)
        //            {
        //                throw new LazyInitialiationException("Value must be valid at this point");
        //            }
        //            return value != null;
        //        }
        //        ICacheModification cacheModification = this.CacheModification;
        //        bool cacheModificationActive = cacheModification.Active;
        //        if (isInGuiThread || cacheModificationActive)
        //        {
        //            if (!cacheDirectiveHint.HasFlag(CacheDirective.None))
        //            {
        //                throw new LazyInitialiationException("It is not valid to access GetValue() with anything other than '" + CacheDirective.None + "'");
        //            }

        //            bool loadAsync = false;
        //            objRefs = proxyHelper.GetObjRefs(vhc, member);
        //            if (objRefs == null)
        //            {
        //                Object parentEntity = GetParentEntity(vhref);
        //                IObjRelation self = GetSelf(parentEntity, member);
        //                if (targetCache == null)
        //                {
        //                    targetCache = GetTargetCache(vhref);
        //                }
        //                IList<IObjRelationResult> temporaryObjRelResults = targetCache.GetObjRelations(new IObjRelation[] { self }, CacheDirective.FailInCacheHierarchy);

        //                if (temporaryObjRelResults.Count == 0)
        //                {
        //                    // ObjRelation could not be resolved. So we can not handle the value holder initialization
        //                    // synchronously. Queue asynchronous action
        //                    loadAsync = true;
        //                }
        //                else
        //                {
        //                    objRefs = temporaryObjRelResults[0].Relations;
        //                    proxyHelper.SetObjRefs(vhc, member, objRefs);
        //                }
        //            }
        //            IList<Object> temporaryResults = null;
        //            if (objRefs != null)
        //            {
        //                //bool oldFailEarlyMode = AbstractCache<Object>.FailEarlyModeActive;
        //                //AbstractCache<Object>.FailEarlyModeActive = true;
        //                //try
        //                //{
        //                if (objRefs.Length == 0)
        //                {
        //                    temporaryResults = new List<Object>(0);
        //                }
        //                else
        //                {
        //                    if (targetCache == null)
        //                    {
        //                        targetCache = GetTargetCache(vhref);
        //                    }
        //                    temporaryResults = targetCache.GetObjects(objRefs, CacheDirective.ReturnMisses | CacheDirective.FailInCacheHierarchy);
        //                    //}
        //                    //finally
        //                    //{
        //                    //    AbstractCache<Object>.FailEarlyModeActive = oldFailEarlyMode;
        //                    //}
        //                    for (int a = temporaryResults.Count; a-- > 0; )
        //                    {
        //                        if (temporaryResults[a] == null)
        //                        {
        //                            // ObjRef at index 'a' could not be resolved. So we can not handle the value holder initialization
        //                            // synchronously. Queue asynchronous action
        //                            loadAsync = true;
        //                            break;
        //                        }
        //                    }
        //                }
        //            }
        //            if (!loadAsync)
        //            {
        //                // It was possible to resolve all objects synchronously because they were all found in the cache already
        //                // No need for threading overhead to queue value holder initialization
        //                value = cacheHelper.ConvertResultListToExpectedType(temporaryResults, expectedType, elementType);
        //                isInitialized = true;
        //                ProxyHelper.SetInitialized(vhc, member, value);
        //                if (value is INotifyPropertyChanged)
        //                {
        //                    HandleValueSetAsynchronously(vhref, (INotifyPropertyChanged)value);
        //                }
        //                return true;
        //            }
        //            if (cacheDirectiveHint.HasFlag(CacheDirective.FailEarly) || cacheDirectiveHint.HasFlag(CacheDirective.FailInCacheHierarchy))
        //            {
        //                return false;
        //            }
        //            if (typeof(IEnumerable).IsAssignableFrom(expectedType))
        //            {
        //                value = cacheHelper.ConvertResultListToExpectedType(null, expectedType, elementType);
        //            }
        //            else
        //            {
        //                // Empty value because the object instance itself will only be available after
        //                // all of its (potential multiple) ids have been retrieved and it is possible
        //                // for the cache to create ONE instance for all ids in an atomic step.
        //                // this is due to the fact that multiple valueholders can refer to the same object
        //                // instance by DIFFERENT objRefs (one for each type of unique id / primary id / alternate id)
        //                value = null;
        //            }
        //            initPending = true;
        //            ProxyHelper.SetInitPending(vhc, member);
        //            ThreadPool.Queue(loadAllPendingValueHoldersQGK, new PendingValueHolderItem(vhref));
        //            if (value == null && synchronousResultExpected)
        //            {
        //                throw new LazyInitialiationException("Value must be valid at this point");
        //            }
        //            return value != null;
        //        }
        //    }
        //    if (lockAcquired != null && lockAcquired.Value)
        //    {
        //        // Release lock once more
        //        Monitor.Exit(this);
        //        lockAcquired.Value = false;
        //    }
        //    objRefs = proxyHelper.GetObjRefs(vhc, member);
        //    // Release vh lock while calling the __TargetCache. This is important due to potential deadlocks between UI & workers
        //    IList<Object> results = null;
        //    if (objRefs == null)
        //    {
        //        Object parentEntity = GetParentEntity(vhref);
        //        IObjRelation self = GetSelf(parentEntity, member);
        //        if (targetCache == null)
        //        {
        //            targetCache = GetTargetCache(vhref);
        //        }
        //        IList<IObjRelationResult> objRelResults = targetCache.GetObjRelations(new IObjRelation[] { self }, cacheDirectiveHint);
        //        if (objRelResults.Count == 0)
        //        {
        //            if (cacheDirectiveHint.HasFlag(CacheDirective.FailEarly) || cacheDirectiveHint.HasFlag(CacheDirective.FailInCacheHierarchy))
        //            {
        //                return false;
        //            }
        //        }
        //        objRefs = objRelResults[0].Relations;
        //        proxyHelper.SetObjRefs(vhc, member, objRefs);
        //    }
        //    if (objRefs != null)
        //    {
        //        if (objRefs.Length == 0)
        //        {
        //            results = new List<Object>(0);
        //        }
        //        else
        //        {
        //            if (targetCache == null)
        //            {
        //                targetCache = GetTargetCache(vhref);
        //            }
        //            results = targetCache.GetObjects(objRefs, cacheDirectiveHint);
        //        }
        //    }
        //    lock (this)
        //    {
        //        // Check whether we are still in a valid state or whether our result is outdated
        //        if (!isInitialized)
        //        {
        //            // We are still valid and are allowed to use our 'results'
        //            value = cacheHelper.ConvertResultListToExpectedType(results, expectedType, elementType);
        //            isInitialized = true;
        //            proxyHelper.SetInitialized(vhc, member, value);
        //            if (value is INotifyPropertyChanged)
        //            {
        //                HandleValueSetAsynchronously(vhref, (INotifyPropertyChanged)value);
        //            }
        //        }
        //        return value != null;
        //    }
        //}

        //public bool HasParent(Object parent, Object parentEntity)
        //{
        //    return Object.ReferenceEquals(parentEntity, parent);
        //}

        //public IObjRelation GetSelf(Object vhc, IRelationInfoItem member)
        //{
        //    Object parentObj = vhc; // ProxyHelper.GetParentObj(vhc);
        //    IList<IObjRef> allObjRefs = OriHelper.EntityToAllOris(parentObj);
        //    return new ObjRelation(ListUtil.ToArray(allObjRefs), member.Name);
        //}

        //public IObjRelation GetSelf(IValueHolderContainer entity, String memberName)
        //{
        //    IList<IObjRef> allObjRefs = OriHelper.EntityToAllOris(entity);
        //    return new ObjRelation(ListUtil.ToArray(allObjRefs), memberName);
        //}

        //public Object GetValue(IValueHolderContainer entity, IRelationInfoItem[] relationMembers, int indexOfProperty, ICacheIntern targetCache, IObjRef[] objRefs)
        //{
        //    IRelationInfoItem relationMember = relationMembers[indexOfProperty];
        //    Object value;
        //    if (targetCache == null)
        //    {
        //        // This happens if an entity gets newly created and immediately called for relations (e.g. collections to add sth)
        //        value = CacheHelper.CreateInstanceOfTargetExpectedType(relationMember.RealType, relationMember.ElementType);
        //    }
        //    else
        //    {
        //        IValueHolder valueHolder = ValueHolderFactory.CreateValueHolder(entity, relationMember, targetCache, objRefs, false);
        //        value = valueHolder.GetValue();
        //    }
        //    return value;
        //}
    }
}