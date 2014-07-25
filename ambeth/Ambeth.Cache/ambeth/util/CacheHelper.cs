﻿using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Cache.Rootcachevalue;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Template;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Typeinfo;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Reflection;
using System.Threading;

namespace De.Osthus.Ambeth.Util
{
    public class CacheHelper : ICacheHelper, ICachePathHelper, IPrefetchHelper
    {
        protected static readonly Object[] emptyObjectArray = new Object[0];

        private static readonly CacheDirective failEarlyReturnMisses = CacheDirective.FailEarly | CacheDirective.ReturnMisses;

        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IServiceContext BeanContext { protected get; set; }

        [Autowired]
        public ICacheModification CacheModification { protected get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        [Autowired]
        public IGuiThreadHelper GuiThreadHelper { protected get; set; }

        [Autowired]
        public IObjRefHelper OriHelper { protected get; set; }
        
        [Autowired]
        public ITypeInfoProvider TypeInfoProvider { protected get; set; }

        [Autowired]
        public ValueHolderContainerTemplate ValueHolderContainerTemplate { protected get; set; }

        protected readonly ThreadLocal<HashSet<AlreadyHandledItem>> alreadyHandledSetTL = new ThreadLocal<HashSet<AlreadyHandledItem>>();

        public void BuildCachePath(Type entityType, String memberToInitialize, IList<CachePath> cachePaths)
        {
            String[] path = memberToInitialize.Split('.');
            CachePath currentCachePath = null;
            Type currentType = entityType;
            foreach (String pathItem in path)
            {
                if (currentCachePath == null)
                {
                    currentCachePath = GetOrCreateCachePath(cachePaths, currentType, pathItem);
                }
                else
                {
                    if (currentCachePath.children == null)
                    {
                        currentCachePath.children = new List<CachePath>();
                    }
                    currentCachePath = GetOrCreateCachePath(currentCachePath.children, currentType, pathItem);
                }
                currentType = currentCachePath.memberType;
            }
        }

        protected IList<CachePath> BuildCachePath(Type entityType, IList<String> membersToInitialize)
        {
            IList<CachePath> cachePaths = new List<CachePath>();
            foreach (String memberName in membersToInitialize)
            {
                BuildCachePath(entityType, memberName, cachePaths);
            }
            return cachePaths;
        }

        protected CachePath GetOrCreateCachePath(IList<CachePath> cachePaths, Type entityType, String memberName)
        {
            for (int a = cachePaths.Count; a-- > 0; )
            {
                CachePath cachePath = cachePaths[a];
                if (memberName.Equals(cachePath.memberName))
                {
                    return cachePath;
                }
            }
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(entityType);

            ITypeInfoItem member = metaData.GetMemberByName(memberName);
            if (member == null)
            {
                throw new Exception("Member " + entityType.FullName + "." + memberName + " not found");
            }
            CachePath newCachePath = new CachePath();
            newCachePath.memberType = member.ElementType;
            newCachePath.memberIndex = metaData.GetIndexByRelation((IRelationInfoItem)member);
            newCachePath.memberName = memberName;
            cachePaths.Add(newCachePath);
            return newCachePath;
        }

        public IPrefetchConfig CreatePrefetch()
        {
            return BeanContext.RegisterAnonymousBean<PrefetchConfig>().Finish();
        }

        public IPrefetchState Prefetch(Object objects)
        {
            return EnsureInitializedRelationsIntern<IList<CachePath>>(objects, null);
        }

        public IPrefetchState Prefetch(Object objects, IDictionary<Type, IList<String>> typeToMembersToInitialize)
        {
            if (GuiThreadHelper.IsInGuiThread())
            {
                throw new Exception("it is not allowed to call Prefetch from the GUI thread");
            }
            if (objects == null || typeToMembersToInitialize == null || typeToMembersToInitialize.Count == 0)
            {
                return null;
            }
            IDictionary<Type, IList<CachePath>> typeToCachePathsDict = new Dictionary<Type, IList<CachePath>>();
            DictionaryExtension.Loop(typeToMembersToInitialize, delegate(Type entityType, IList<String> membersToInitialize)
            {
                typeToCachePathsDict[entityType] = BuildCachePath(entityType, membersToInitialize);
            });
            return EnsureInitializedRelations(objects, typeToCachePathsDict);
        }

        public IPrefetchState EnsureInitializedRelations<V>(Object objects, IDictionary<Type, V> typeToMembersToInitialize) where V : IList<CachePath>
        {
            if (objects == null || typeToMembersToInitialize == null || typeToMembersToInitialize.Count == 0)
            {
                return null;
            }
            return EnsureInitializedRelationsIntern(objects, typeToMembersToInitialize);
        }

        protected IList<CachePath> MergeCachePaths<V>(Type entityType, IList<CachePath> baseCachePath, IDictionary<Type, V> typeToMembersToInitialize) where V : IList<CachePath>
        {
            if (typeToMembersToInitialize == null)
            {
                return baseCachePath;
            }
            IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(entityType, true);
            if (metaData == null)
            {
                return baseCachePath;
            }
            IList<CachePath> cachePathsOfType = DictionaryExtension.ValueOrDefault(typeToMembersToInitialize, metaData.EntityType);
            if (cachePathsOfType == null)
            {
                return baseCachePath;
            }
            if (baseCachePath == null)
            {
                return cachePathsOfType;
            }
            List<CachePath> cachePaths = new List<CachePath>(baseCachePath);
            cachePaths.AddRange(cachePathsOfType);
            return cachePaths;
        }

        protected IPrefetchState EnsureInitializedRelationsIntern<V>(Object objects, IDictionary<Type, V> typeToMembersToInitialize) where V : IList<CachePath>
        {
            if (objects == null)
            {
                return null;
            }
            bool setCreated = false;
            try
            {
                HashSet<AlreadyHandledItem> alreadyHandledSet = alreadyHandledSetTL.Value;
                if (alreadyHandledSet == null)
                {
                    alreadyHandledSet = new HashSet<AlreadyHandledItem>();
                    alreadyHandledSetTL.Value = alreadyHandledSet;
                    setCreated = true;
                }
                IEntityMetaDataProvider entityMetaDataProvider = this.EntityMetaDataProvider;
                ValueHolderContainerTemplate valueHolderContainerTemplate = this.ValueHolderContainerTemplate;
                IdentityLinkedMap<ICacheIntern, IISet<IObjRef>> cacheToOrisLoadedHistory = new IdentityLinkedMap<ICacheIntern, IISet<IObjRef>>();
                IdentityLinkedMap<ICacheIntern, IISet<IObjRelation>> cacheToOrelsLoadedHistory = new IdentityLinkedMap<ICacheIntern, IISet<IObjRelation>>();
                IdentityLinkedMap<ICacheIntern, IISet<IObjRef>> cacheToOrisToLoad = new IdentityLinkedMap<ICacheIntern, IISet<IObjRef>>();
                IdentityLinkedMap<ICacheIntern, IISet<IObjRelation>> cacheToOrelsToLoad = new IdentityLinkedMap<ICacheIntern, IISet<IObjRelation>>();
                List<CascadeLoadItem> loadItems = new List<CascadeLoadItem>();

                PrefetchState prefetchState = null;
                bool terminate = false;
                GuiThreadHelper.InvokeInGuiAndWait(delegate()
                {
                    ICacheModification cacheModification = CacheModification;
                    bool oldActive = cacheModification.Active;
                    if (!oldActive)
                    {
                        cacheModification.Active = true;
                    }
                    try
                    {
                        if (objects is IEnumerable)
                        {
                            foreach (Object item in (IEnumerable)objects)
                            {
                                if (item == null)
                                {
                                    continue;
                                }
                                IList<CachePath> cachePaths = null;
                                if (typeToMembersToInitialize != null)
                                {
                                    IEntityMetaData metaData = entityMetaDataProvider.GetMetaData(item.GetType(), true);
                                    if (metaData != null)
                                    {
                                        cachePaths = DictionaryExtension.ValueOrDefault(typeToMembersToInitialize, metaData.EntityType);
                                        if (cachePaths == null)
                                        {
                                            continue;
                                        }
                                    }
                                }
                                EnsureInitializedRelationsIntern(item, cachePaths, cacheToOrisToLoad, cacheToOrelsToLoad, cacheToOrisLoadedHistory,
                                        cacheToOrelsLoadedHistory, alreadyHandledSet, loadItems);
                            }
                        }
                        else
                        {
                            IList<CachePath> cachePaths = null;
                            if (typeToMembersToInitialize != null)
                            {
                                IEntityMetaData metaData = ((IEntityMetaDataHolder) objects).Get__EntityMetaData();
                                cachePaths = DictionaryExtension.ValueOrDefault(typeToMembersToInitialize, metaData.EntityType);

                                if (cachePaths == null)
                                {
                                    if (setCreated)
                                    {
                                        prefetchState = new PrefetchState(alreadyHandledSet);
                                    }
                                    terminate = true;
                                    return;
                                }
                            }
                            EnsureInitializedRelationsIntern(objects, cachePaths, cacheToOrisToLoad, cacheToOrelsToLoad, cacheToOrisLoadedHistory,
                                    cacheToOrelsLoadedHistory, alreadyHandledSet, loadItems);
                        }
                    }
                    finally
                    {
                        if (!oldActive)
                        {
                            cacheModification.Active = false;
                        }
                    }
                });
                if (terminate)
                {
                    return prefetchState;
                }
                // Remove all oris which have already been tried to load before
                if (cacheToOrisToLoad.Count == 0 && cacheToOrelsToLoad.Count == 0)
                {
                    // No ori remaining which makes sense to try to load
                    if (setCreated)
                    {
                        return new PrefetchState(alreadyHandledSet);
                    }
                    return null;
                }
                List<Object> hardRefList = new List<Object>();
                // Store hard-ref-list to global hard ref
                alreadyHandledSet.Add(new AlreadyHandledItem(hardRefList, null));

                LoadAndAddOrels(cacheToOrelsToLoad, hardRefList, cacheToOrelsLoadedHistory, cacheToOrisToLoad);
                LoadAndAddOris(cacheToOrisToLoad, hardRefList, cacheToOrisLoadedHistory);

                while (loadItems.Count > 0)
                {
                    CascadeLoadItem[] cascadeLoadItems = loadItems.ToArray();
                    // Clear the items to be ready for cascaded items in new batch recursion step
                    loadItems.Clear();
                    GuiThreadHelper.InvokeInGuiAndWait(delegate()
                    {
                        ICacheModification cacheModification = CacheModification;
                        bool oldActive = cacheModification.Active;
                        if (!oldActive)
                        {
                            cacheModification.Active = true;
                        }
                        try
                        {
                            foreach (CascadeLoadItem cascadeLoadItem in cascadeLoadItems)
                            {
                                Object valueHolder = cascadeLoadItem.valueHolder;
                                IList<CachePath> cachePaths = cascadeLoadItem.cachePaths;

                                // Merge the root prefetch path with the relative prefetch path
                                cachePaths = MergeCachePaths(cascadeLoadItem.realType, cachePaths, typeToMembersToInitialize);

                                IObjRefContainer vhc;
						        ICacheIntern targetCache;
						        IRelationInfoItem member;
						        bool doSetValue = false;
						        Object obj;
						        if (valueHolder is IndirectValueHolderRef)
						        {
							        IndirectValueHolderRef valueHolderKey = (IndirectValueHolderRef) valueHolder;
							        vhc = valueHolderKey.Vhc;
							        targetCache = valueHolderKey.RootCache;
							        member = valueHolderKey.Member;
						        }
						        else
						        {
							        DirectValueHolderRef valueHolderKey = (DirectValueHolderRef) valueHolder;
							        IValueHolderContainer vhcTemp = (IValueHolderContainer) valueHolderKey.Vhc;
							        vhc = vhcTemp;
                                    targetCache = vhcTemp.__TargetCache;
							        member = valueHolderKey.Member;
							        doSetValue = true;
						        }
						        int relationIndex = vhc.Get__EntityMetaData().GetIndexByRelation(member);
						        IObjRef[] objRefs = vhc.Get__ObjRefs(relationIndex);
						        obj = valueHolderContainerTemplate.GetValue(vhc, relationIndex, member, targetCache, objRefs, CacheDirective.FailEarly);
						        if (doSetValue && obj != null)
						        {
							        member.SetValue(vhc, obj);
						        }
                                EnsureInitializedRelationsIntern(obj, cachePaths, cacheToOrisToLoad, cacheToOrelsToLoad, cacheToOrisLoadedHistory,
                                        cacheToOrelsLoadedHistory, alreadyHandledSet, loadItems);
                            }
                        }
                        finally
                        {
                            if (!oldActive)
                            {
                                cacheModification.Active = false;
                            }
                        }
                    });
                    // Remove all oris which have already been tried to load before
                    if (cacheToOrisToLoad.Count == 0 && cacheToOrelsToLoad.Count == 0)
                    {
                        // No ori remaining which makes sense to try to load
                        if (setCreated)
                        {
                            return new PrefetchState(alreadyHandledSet);
                        }
                        return null;
                    }
                    LoadAndAddOrels(cacheToOrelsToLoad, hardRefList, cacheToOrelsLoadedHistory, cacheToOrisToLoad);
                    LoadAndAddOris(cacheToOrisToLoad, hardRefList, cacheToOrisLoadedHistory);
                }
                if (setCreated)
                {
                    return new PrefetchState(alreadyHandledSet);
                }
                return null;
            }
            finally
            {
                if (setCreated)
                {
                    alreadyHandledSetTL.Value = null;
                }
            }
        }

        protected void LoadAndAddOris(IMap<ICacheIntern, IISet<IObjRef>> cacheToOrisToLoad, IList<Object> hardRefList,
            IMap<ICacheIntern, IISet<IObjRef>> cacheToOrisLoadedHistory)
        {
            Iterator<Entry<ICacheIntern, IISet<IObjRef>>> iter = cacheToOrisToLoad.Iterator();
            while (iter.MoveNext())
            {
                Entry<ICacheIntern, IISet<IObjRef>> entry = iter.Current;
                ICacheIntern cache = entry.Key;
                IISet<IObjRef> orisToLoad = entry.Value;
                iter.Remove();

                LoadAndAddOris(cache, orisToLoad, hardRefList, cacheToOrisLoadedHistory);
            }
        }

        protected void LoadAndAddOris(ICacheIntern cache, ISet<IObjRef> orisToLoad, IList<Object> hardRefList,
            IMap<ICacheIntern, IISet<IObjRef>> cacheToOrisLoadedHistory)
        {
            IList<Object> result = cache.GetObjects(ListUtil.ToList(orisToLoad), CacheDirective.None);
            hardRefList.Add(result);
            IISet<IObjRef> orisLoadedHistory = cacheToOrisLoadedHistory.Get(cache);
            if (orisLoadedHistory == null)
            {
                orisLoadedHistory = new CHashSet<IObjRef>();
                cacheToOrisLoadedHistory.Put(cache, orisLoadedHistory);
            }
            orisLoadedHistory.AddAll(orisToLoad);
        }

        protected void LoadAndAddOrels(ILinkedMap<ICacheIntern, IISet<IObjRelation>> cacheToOrelsToLoad, IList<Object> hardRefList,
             IMap<ICacheIntern, IISet<IObjRelation>> cacheToOrelsLoadedHistory, ILinkedMap<ICacheIntern, IISet<IObjRef>> cacheToOrisToLoad)
        {
            Iterator<Entry<ICacheIntern, IISet<IObjRelation>>> iter = cacheToOrelsToLoad.Iterator();
            while (iter.MoveNext())
            {
                Entry<ICacheIntern, IISet<IObjRelation>> entry = iter.Current;
                ICacheIntern cache = entry.Key;
                IISet<IObjRelation> orelsToLoad = entry.Value;
                iter.Remove();

                LoadAndAddOrels(cache, orelsToLoad, hardRefList, cacheToOrelsLoadedHistory, cacheToOrisToLoad);
            }
        }

        protected void LoadAndAddOrels(ICacheIntern cache, IISet<IObjRelation> orelsToLoad, IList<Object> hardRefList,
            IMap<ICacheIntern, IISet<IObjRelation>> cacheToOrelsLoadedHistory, ILinkedMap<ICacheIntern, IISet<IObjRef>> cacheToOrisToLoad)
        {
            IList<IObjRelationResult> objRelResults = cache.GetObjRelations(orelsToLoad.ToList(), cache, CacheDirective.None);

            IISet<IObjRef> orisToLoad = null;
            for (int a = 0, size = objRelResults.Count; a < size; a++)
            {
                IObjRelationResult objRelResult = objRelResults[a];
                foreach (IObjRef objRef in objRelResult.Relations)
                {
                    if (orisToLoad == null)
                    {
                        orisToLoad = cacheToOrisToLoad.Get(cache);
                        if (orisToLoad == null)
                        {
                            orisToLoad = new CHashSet<IObjRef>();
                            cacheToOrisToLoad.Put(cache, orisToLoad);
                        }
                    }
                    orisToLoad.Add(objRef);
                }
            }
            IISet<IObjRelation> orelsLoadedHistory = cacheToOrelsLoadedHistory.Get(cache);
            if (orelsLoadedHistory == null)
            {
                orelsLoadedHistory = new CHashSet<IObjRelation>();
                cacheToOrelsLoadedHistory.Put(cache, orelsLoadedHistory);
            }
            orelsLoadedHistory.AddAll(orelsToLoad);
        }

        protected void EnsureInitializedRelationsIntern(Object obj, IList<CachePath> cachePaths,
            IMap<ICacheIntern, IISet<IObjRef>> cacheToOrisToLoad, IMap<ICacheIntern, IISet<IObjRelation>> cacheToOrelsToLoad,
            IMap<ICacheIntern, IISet<IObjRef>> cacheToOrisLoadedHistory, IMap<ICacheIntern, IISet<IObjRelation>> cacheToOrelsLoadedHistory,
            ISet<AlreadyHandledItem> alreadyHandledSet, IList<CascadeLoadItem> cascadeLoadItems)
        {
            if (obj == null)
            {
                return;
            }
            AlreadyHandledItem alreadyHandledItem = new AlreadyHandledItem(obj, cachePaths);
            if (!alreadyHandledSet.Add(alreadyHandledItem))
            {
                return;
            }
            if (obj is IndirectValueHolderRef)
            {
                IndirectValueHolderRef vhk = (IndirectValueHolderRef)obj;
                HandleValueHolder(vhk, cachePaths, cacheToOrisToLoad, cacheToOrelsToLoad, cacheToOrisLoadedHistory, cacheToOrelsLoadedHistory,
                        alreadyHandledSet, cascadeLoadItems);
                // Do nothing because this is only to prefetch RootCache entries
                return;
            }
            else if (obj is DirectValueHolderRef)
            {
                DirectValueHolderRef vhk = (DirectValueHolderRef)obj;
                if (!HandleValueHolder(vhk, cachePaths, cacheToOrisToLoad, cacheToOrelsToLoad, cacheToOrisLoadedHistory, cacheToOrelsLoadedHistory,
                        alreadyHandledSet, cascadeLoadItems))
                {
                    return;
                }
                // force valueholder init. at this point we know that all related items are already in the cache. there will be no roundtrip
                // to the server
                if (vhk.Member == null)
                {
                    obj = vhk.Vhc;
                }
                else
                {
                    obj = vhk.Member.GetValue(vhk.Vhc);
                }
            }
            if (obj == null)
            {
                // this check is necessary because even if we create only instances of DirectValueHolderRef in cases where there is a not initalized relation
                // even then it might be possible that a concurrent thread initializes the valueholder to null (e.g. an empty to-one relation)
                return;
            }
            if (obj is IEnumerable)
            {
                var items = new List<Object>();
                foreach (Object item in (IEnumerable)obj)
                {
                    if (item == null)
                    {
                        continue;
                    }
                    items.Add(item);
                }
                foreach (Object item in items)
                {
                    EnsureInitializedRelationsIntern(item, cachePaths, cacheToOrisToLoad, cacheToOrelsToLoad,
                        cacheToOrisLoadedHistory, cacheToOrelsLoadedHistory, alreadyHandledSet, cascadeLoadItems);
                }
                return;
            }
            if (cachePaths == null)
            {
                return;
            }
            IEntityMetaData metaData = ((IEntityMetaDataHolder)obj).Get__EntityMetaData();
            IRelationInfoItem[] relationMembers = metaData.RelationMembers;
            if (relationMembers.Length == 0)
            {
                return;
            }
            IValueHolderContainer vhc = (IValueHolderContainer)obj;
            for (int a = cachePaths.Count; a-- > 0; )
            {
                CachePath path = cachePaths[a];

                int relationIndex = path.memberIndex;
                IRelationInfoItem member = relationMembers[relationIndex];

                if (ValueHolderState.INIT != vhc.Get__State(relationIndex))
                {
                    DirectValueHolderRef vhk = new DirectValueHolderRef(vhc, member);
                    EnsureInitializedRelationsIntern(vhk, path.children, cacheToOrisToLoad, cacheToOrelsToLoad, cacheToOrisLoadedHistory,
                            cacheToOrelsLoadedHistory, alreadyHandledSet, cascadeLoadItems);
                    continue;
                }
                Object memberValue = member.GetValue(obj);
                if (memberValue == null)
                {
                    continue;
                }
                EnsureInitializedRelationsIntern(memberValue, path.children, cacheToOrisToLoad, cacheToOrelsToLoad,
                    cacheToOrisLoadedHistory, cacheToOrelsLoadedHistory, alreadyHandledSet, cascadeLoadItems);
            }
        }

        protected bool HandleValueHolder(DirectValueHolderRef vhr, IList<CachePath> cachePaths, IMap<ICacheIntern, IISet<IObjRef>> cacheToOrisToLoad,
            IMap<ICacheIntern, IISet<IObjRelation>> cacheToOrelsToLoad, IMap<ICacheIntern, IISet<IObjRef>> cacheToOrisLoadedHistory,
            IMap<ICacheIntern, IISet<IObjRelation>> cacheToOrelsLoadedHistory, ISet<AlreadyHandledItem> alreadyHandledSet, IList<CascadeLoadItem> cascadeLoadItems)
        {
            IRelationInfoItem member = vhr.Member;
            bool newOriToLoad = false;
            if (vhr is IndirectValueHolderRef)
            {
                RootCacheValue rcv = (RootCacheValue)vhr.Vhc;
                ICacheIntern rootCache = ((IndirectValueHolderRef)vhr).RootCache;
                IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(rcv.EntityType);
                int relationIndex = metaData.GetIndexByRelation(member);
                IObjRef[] rcvObjRefs = rcv.GetRelation(relationIndex);
                if (rcvObjRefs == null)
                {
                    IObjRelation self = ValueHolderContainerTemplate.GetSelf(rcv, member.Name);
                    ISet<IObjRelation> orelsLoadedHistory = cacheToOrelsLoadedHistory.Get(rootCache);
                    if (orelsLoadedHistory == null || !orelsLoadedHistory.Contains(self))
                    {
                        IISet<IObjRelation> orelsToLoad = cacheToOrelsToLoad.Get(rootCache);
                        if (orelsToLoad == null)
                        {
                            orelsToLoad = new CHashSet<IObjRelation>();
                            cacheToOrelsToLoad.Put(rootCache, orelsToLoad);
                        }
                        orelsToLoad.Add(self);

                        CascadeLoadItem cascadeLoadItem = new CascadeLoadItem(member.ElementType, vhr, cachePaths);
                        cascadeLoadItems.Add(cascadeLoadItem);
                    }
                    return false;
                }
                else if (rcvObjRefs.Length > 0)
                {
                    ISet<IObjRef> orisLoadedHistory = cacheToOrisLoadedHistory.Get(rootCache);
                    for (int b = rcvObjRefs.Length; b-- > 0; )
                    {
                        IObjRef ori = rcvObjRefs[b];
                        if (orisLoadedHistory != null && orisLoadedHistory.Contains(ori))
                        {
                            // Object has been tried to load before but it is obviously not in the cache
                            // So the load must have been failed somehow. It is assumed that the entity
                            // is not persisted in the database anymore (deleted before) so the ORI is illegal.
                            // We cleanup the ValueHolder so that future calls will not lead to
                            // another unnecessary roundtrip to the server
                            rcvObjRefs[b] = null;
                            continue;
                        }
                        IISet<IObjRef> orisToLoad = cacheToOrisToLoad.Get(rootCache);
                        if (orisToLoad == null)
                        {
                            orisToLoad = new CHashSet<IObjRef>();
                            cacheToOrisToLoad.Put(rootCache, orisToLoad);
                        }
                        orisToLoad.Add(ori);
                        newOriToLoad = true;
                    }
                    if (newOriToLoad)
                    {
                        CascadeLoadItem cascadeLoadItem = new CascadeLoadItem(member.ElementType, vhr, cachePaths);
                        cascadeLoadItems.Add(cascadeLoadItem);
                    }
                }
                return false;
            }
            IValueHolderContainer vhc = (IValueHolderContainer)vhr.Vhc;
            int relationIndex2 = vhc.Get__EntityMetaData().GetIndexByRelationName(member.Name);

            if (ValueHolderState.INIT == vhc.Get__State(relationIndex2))
            {
                return true;
            }
            ICacheIntern cache = vhc.__TargetCache;
            IObjRef[] objRefs = vhc.Get__ObjRefs(relationIndex2);
            if (objRefs == null)
            {
                IObjRelation self = vhc.Get__Self(relationIndex2);
                List<IObjRelation> orels = new List<IObjRelation>();
                orels.Add(self);
                IList<IObjRelationResult> orelResults = cache.GetObjRelations(orels, cache, failEarlyReturnMisses);
                IObjRelationResult orelResult = orelResults[0];
                if (orelResult == null)
                {
                    ISet<IObjRelation> orelsLoadedHistory = cacheToOrelsLoadedHistory.Get(cache);
                    if (orelsLoadedHistory == null || !orelsLoadedHistory.Contains(self))
                    {
                        IISet<IObjRelation> orelsToLoad = cacheToOrelsToLoad.Get(cache);
                        if (orelsToLoad == null)
                        {
                            orelsToLoad = new CHashSet<IObjRelation>();
                            cacheToOrelsToLoad.Put(cache, orelsToLoad);
                        }
                        orelsToLoad.Add(self);

                        CascadeLoadItem cascadeLoadItem = new CascadeLoadItem(member.ElementType, vhr, cachePaths);
                        cascadeLoadItems.Add(cascadeLoadItem);
                    }
                    return false;
                }
                objRefs = orelResult.Relations;
                if (objRefs != null)
                {
                    vhc.Set__ObjRefs(relationIndex2, objRefs);
                }
            }
            if (objRefs != null && objRefs.Length > 0)
            {
                IList<Object> loadedObjects = cache.GetObjects(new List<IObjRef>(objRefs), cache, failEarlyReturnMisses);
                try
                {
                    for (int b = objRefs.Length; b-- > 0; )
                    {
                        IObjRef ori = objRefs[b];
                        Object loadedObject = loadedObjects[b];
                        if (loadedObject != null)
                        {
                            continue;
                        }
                        ISet<IObjRef> orisLoadedHistory = cacheToOrisLoadedHistory.Get(cache);
                        if (orisLoadedHistory != null && orisLoadedHistory.Contains(ori))
                        {
                            // Object has been tried to load before but it is obviously not in the cache
                            // So the load must have been failed somehow. It is assumed that the entity
                            // is not persisted in the database anymore (deleted before) so the ORI is illegal.
                            // We cleanup the ValueHolder so that future calls will not lead to
                            // another unnecessary roundtrip to the server
                            objRefs[b] = null;
                            continue;
                        }
                        IISet<IObjRef> orisToLoad = cacheToOrisToLoad.Get(cache);
                        if (orisToLoad == null)
                        {
                            orisToLoad = new CHashSet<IObjRef>();
                            cacheToOrisToLoad.Put(cache, orisToLoad);
                        }
                        orisToLoad.Add(ori);
                        newOriToLoad = true;
                    }
                }
                finally
                {
                    loadedObjects.Clear();
                    loadedObjects = null;
                }
            }
            if (objRefs == null || newOriToLoad)
            {
                CascadeLoadItem cascadeLoadItem = new CascadeLoadItem(member.ElementType, vhr, cachePaths);
                cascadeLoadItems.Add(cascadeLoadItem);
                return false;
            }
            return true;
        }

        public Object CreateInstanceOfTargetExpectedType(Type expectedType, Type elementType)
        {
            if (typeof(IEnumerable).IsAssignableFrom(expectedType) && !typeof(String).Equals(expectedType)) // OneToMany or ManyToMany Relationship
            {
                if (expectedType.IsInterface)
                {
                    return Activator.CreateInstance(ListUtil.obsListType.MakeGenericType(elementType));
                }
                return Activator.CreateInstance(expectedType);
            }
            return null;
        }

        public Object ConvertResultListToExpectedType(IList<Object> resultList, Type expectedType, Type elementType)
        {
            if (typeof(IEnumerable).IsAssignableFrom(expectedType)) // OneToMany or ManyToMany Relationship
            {
                Object targetCollection = CreateInstanceOfTargetExpectedType(expectedType, elementType);

                if (resultList != null)
                {
                    MethodInfo addMethod = targetCollection.GetType().GetMethod("Add");
                    Object[] addMethodArgs = new Object[1];

                    for (int b = 0, sizeB = resultList.Count; b < sizeB; b++)
                    {
                        addMethodArgs[0] = resultList[b];
                        addMethod.Invoke(targetCollection, addMethodArgs);
                    }
                }
                return targetCollection;
            }

            if (resultList != null && resultList.Count > 0)
            {
                return resultList[0];
            }
            return null;
        }

        public Object[] ExtractPrimitives(IEntityMetaData metaData, Object obj)
        {
            ITypeInfoItem[] primitiveMembers = metaData.PrimitiveMembers;
            Object[] primitives;

            if (primitiveMembers.Length == 0)
            {
                primitives = emptyObjectArray;
            }
            else
            {
                primitives = new Object[primitiveMembers.Length];
                for (int a = primitiveMembers.Length; a-- > 0; )
                {
                    ITypeInfoItem primitiveMember = primitiveMembers[a];

                    Object primitiveValue = primitiveMember.GetValue(obj, false);

                    primitives[a] = primitiveValue;
                }
            }

            return primitives;
        }

        public IObjRef[][] ExtractRelations(IEntityMetaData metaData, Object obj)
        {
            return ExtractRelations(metaData, obj, null);
        }

        public IObjRef[][] ExtractRelations(IEntityMetaData metaData, Object obj, IList<Object> relationValues)
        {
            IRelationInfoItem[] relationMembers = metaData.RelationMembers;

            if (relationMembers.Length == 0)
            {
                return ObjRef.EMPTY_ARRAY_ARRAY;
            }
            IValueHolderContainer vhc = (IValueHolderContainer)obj;
            IObjRef[][] relations = new IObjRef[relationMembers.Length][];

            IObjRefHelper oriHelper = this.OriHelper;
            for (int relationIndex = relationMembers.Length; relationIndex-- > 0; )
            {
                IRelationInfoItem relationMember = relationMembers[relationIndex];

                if (ValueHolderState.INIT != vhc.Get__State(relationIndex))
                {
                    relations[relationIndex] = vhc.Get__ObjRefs(relationIndex);
                    continue;
                }
                Object relationValue = relationMember.GetValue(obj, false);
                if (relationValue == null)
                {
                    relations[relationIndex] = ObjRef.EMPTY_ARRAY;
                    continue;
                }
                IList<IObjRef> oris = oriHelper.ExtractObjRefList(relationValue, null);
                relations[relationIndex] = oris != null ? ListUtil.ToArray<IObjRef>(oris) : null;

                if (relationValues != null)
                {
                    relationValues.Add(relationValue);
                }
            }
            return relations;
        }

        public ICollection<T> ExtractTargetEntities<T, S>(IEnumerable<S> sourceEntities, String sourceToTargetEntityPropertyPath)
        {
            // Einen Accessor ermitteln, der die gesamte Hierachie aus dem propertyPath („A.B.C“) selbstständig traversiert
            ITypeInfoItem member = TypeInfoProvider.GetHierarchicMember(typeof(S), sourceToTargetEntityPropertyPath);

            // MetaDaten der Ziel-Entity ermitteln, da wir (generisch) den PK brauchen, um damit ein DISTINCT-Behavior durch eine Map als Zwischenstruktur zu
            // erreichen
            IEntityMetaData targetMetaData = EntityMetaDataProvider.GetMetaData(member.ElementType);
            ITypeInfoItem targetIdMember = targetMetaData.IdMember;

            // Damit bei der Traversion keine Initialisierungen mit DB-Roundtrips entstehen, machen wir vorher eine Prefetch passend zum PropertyPath auf allen
            // übergebenen Quell-Entities
            // Dadurch entstehen maximal 2 gebatchte SELECTs, egal wie groß die Liste ist
            IPrefetchHandle prefetch = CreatePrefetch().Add(typeof(S), sourceToTargetEntityPropertyPath).Build();
            // Speichere das State-Result unbenutzt - wichtig für concurrent GC Aktivitäten, um Verluste an Entity-Referenzen zu verhindern
            IPrefetchState state = prefetch.Prefetch(sourceEntities);

            IDictionary<Object, T> targetDistinctMap = new Dictionary<Object, T>();
            // Danach traversieren, wobei wir jetzt wissen, dass uns das keine Roundtrips kostet
            foreach (S sourceEntity in sourceEntities)
            {
                if (sourceEntity == null)
                {
                    continue;
                }
                Object targetEntities = member.GetValue(sourceEntity);
                if (targetEntities == null)
                {
                    continue;
                }
                // Ergebnismenge flexibel (bei *-To-Many) verarbeiten oder so lassen (bei *-To-One)
                if (targetEntities is IEnumerable)
                {
                    foreach (Object targetEntity in (IEnumerable)targetEntities)
                    {
                        if (targetEntity == null)
                        {
                            continue;
                        }
                        Object targetId = targetIdMember.GetValue(targetEntity);
                        if (targetId == null)
                        {
                            // Falls die Entity keine ID hat, speichern wir sie ausnahmsweise selbst als Key
                            targetId = targetEntity;
                        }
                        targetDistinctMap[targetId] = (T)targetEntity;
                    }
                }
                else
                {
                    Object targetId = targetIdMember.GetValue(targetEntities);
                    if (targetId == null)
                    {
                        // Falls die Entity keine ID hat, speichern wir sie ausnahmsweise selbst als Key
                        targetId = targetEntities;
                    }
                    targetDistinctMap[targetId] = (T)targetEntities;
                }
            }
            // Alle values sind unsere eindeutigen Target Entities ohne Duplikate
            return targetDistinctMap.Values;
        }
    }
}