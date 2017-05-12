using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Cache.Rootcachevalue;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Mixin;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Typeinfo;
using System;
using System.Collections;
using System.Collections.Generic;
using System.Reflection;
using System.Threading;
using De.Osthus.Ambeth.Config;
using De.Osthus.Ambeth.Merge.Config;

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
        public IMemberTypeProvider MemberTypeProvider { protected get; set; }

        [Autowired]
        public IObjRefHelper ObjRefHelper { protected get; set; }

		[Autowired]
		public IPrioMembersProvider PrioMembersProvider { protected get; set; }

		[Autowired(Optional=true)]
		public ILightweightTransaction Transaction { protected get; set; }

        [Autowired]
        public ValueHolderContainerMixin ValueHolderContainerMixin { protected get; set; }

		[Property(MergeConfigurationConstants.PrefetchInLazyTransactionActive, DefaultValue = "true")]
		public bool LazyTransactionActive { protected get; set; }

		protected readonly ThreadLocal<AlreadyHandledSet> alreadyHandledSetTL = new ThreadLocal<AlreadyHandledSet>();

        public void BuildCachePath(Type entityType, String memberToInitialize, CHashSet<AppendableCachePath> cachePaths)
	    {
		    Type currentType = entityType;
		    String requestedMemberName = memberToInitialize;
		    AppendableCachePath currentCachePath = null;
            CHashSet<AppendableCachePath> currentCachePaths = cachePaths;

		    while (true)
		    {
			    IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(currentType);
			    Member widenedMember = metaData.GetWidenedMatchingMember(requestedMemberName);
			    if (widenedMember == null)
			    {
				    throw new ArgumentException("No member found to resolve path " + entityType.FullName + "." + memberToInitialize);
			    }
			    String widenedMemberName = widenedMember.Name;
			    if (widenedMember is PrimitiveMember)
			    {
				    if (widenedMemberName.Equals(memberToInitialize))
				    {
					    // this member does not need to be prefetched
					    return;
				    }
				    // widened member has been found but not the full path of the requested member name
				    throw new ArgumentException("No member found to resolve path " + entityType.FullName + "." + memberToInitialize);
			    }
			    AppendableCachePath childCachePath = null;
			    if (currentCachePaths == null)
			    {
                    currentCachePaths = new CHashSet<AppendableCachePath>();
				    currentCachePath.children = currentCachePaths;
			    }
			    foreach (AppendableCachePath cachePath in currentCachePaths)
			    {
				    if (widenedMemberName.Equals(cachePath.memberName))
				    {
					    childCachePath = cachePath;
					    break;
				    }
			    }
			    if (childCachePath == null)
			    {
				    int relationIndex = metaData.GetIndexByRelation(widenedMember);
				    childCachePath = new AppendableCachePath(widenedMember.ElementType, relationIndex, widenedMemberName);
				    currentCachePaths.Add(childCachePath);
			    }
			    if (widenedMemberName.Equals(requestedMemberName))
			    {
				    // we have travered the full path of the requested member name
				    return;
			    }
			    requestedMemberName = requestedMemberName.Substring(widenedMemberName.Length + 1);
			    currentCachePath = childCachePath;
			    currentType = currentCachePath.memberType;
			    currentCachePaths = currentCachePath.children;
		    }
	    }

        public IPrefetchConfig CreatePrefetch()
        {
            return BeanContext.RegisterBean<PrefetchConfig>().Finish();
        }
        
        public IPrefetchState EnsureInitializedRelations(Object objects, ILinkedMap<Type, PrefetchPath[]> entityTypeToPrefetchSteps)
	    {
		    if (objects == null || entityTypeToPrefetchSteps == null || entityTypeToPrefetchSteps.Count == 0)
		    {
			    return null;
		    }
		    return EnsureInitializedRelationsIntern(objects, entityTypeToPrefetchSteps);
	    }

        public IPrefetchState Prefetch(Object objects)
	    {
		    return EnsureInitializedRelationsIntern(objects, null);
	    }

        protected IPrefetchState EnsureInitializedRelationsIntern(Object objects, ILinkedMap<Type, PrefetchPath[]> entityTypeToPrefetchPaths)
		{
			if (objects == null)
			{
				return null;
			}
			if (!LazyTransactionActive || Transaction == null || Transaction.Active)
			{
				return EnsureInitializedRelationsIntern2(objects, entityTypeToPrefetchPaths);
			}
			return Transaction.RunInLazyTransaction(delegate()
			{
				return EnsureInitializedRelationsIntern2(objects, entityTypeToPrefetchPaths);
			});
		}

		protected IPrefetchState EnsureInitializedRelationsIntern2(Object objects, ILinkedMap<Type, PrefetchPath[]> entityTypeToPrefetchPath)
		{
			bool setCreated = false;
			try
			{
				AlreadyHandledSet alreadyHandledSet = alreadyHandledSetTL.Value;
				if (alreadyHandledSet == null)
				{
					alreadyHandledSet = new AlreadyHandledSet();
					alreadyHandledSetTL.Value = alreadyHandledSet;
					setCreated = true;
				}
				IEntityMetaDataProvider entityMetaDataProvider = this.EntityMetaDataProvider;
				ValueHolderContainerMixin valueHolderContainerMixin = this.ValueHolderContainerMixin;
				IdentityLinkedMap<ICacheIntern, IISet<IObjRef>> cacheToOrisLoadedHistory = new IdentityLinkedMap<ICacheIntern, IISet<IObjRef>>();
				IdentityLinkedMap<ICacheIntern, IISet<IObjRelation>> cacheToOrelsLoadedHistory = new IdentityLinkedMap<ICacheIntern, IISet<IObjRelation>>();
				IdentityLinkedMap<ICacheIntern, IISet<IObjRef>> cacheToOrisToLoad = new IdentityLinkedMap<ICacheIntern, IISet<IObjRef>>();
				IdentityLinkedMap<ICacheIntern, IMap<IObjRelation, bool>> cacheToOrelsToLoad = new IdentityLinkedMap<ICacheIntern, IMap<IObjRelation, bool>>();
				List<PrefetchCommand> loadItems = new List<PrefetchCommand>();

				HandleObjects(objects, entityTypeToPrefetchPath, alreadyHandledSet, cacheToOrisLoadedHistory, cacheToOrelsLoadedHistory, cacheToOrisToLoad,
					cacheToOrelsToLoad, loadItems);
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
				alreadyHandledSet.Put(hardRefList, null, true);

				ProcessPendingOrelsAndObjRefs(entityTypeToPrefetchPath, alreadyHandledSet, cacheToOrisLoadedHistory, cacheToOrelsLoadedHistory, cacheToOrisToLoad,
						cacheToOrelsToLoad, loadItems, hardRefList);
				// No ori remaining which makes sense to try to load
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

		protected void HandleObjects(Object objects, ILinkedMap<Type, PrefetchPath[]> entityTypeToPrefetchPath,
			AlreadyHandledSet alreadyHandledSet, IdentityLinkedMap<ICacheIntern, IISet<IObjRef>> cacheToOrisLoadedHistory,
			IdentityLinkedMap<ICacheIntern, IISet<IObjRelation>> cacheToOrelsLoadedHistory, IdentityLinkedMap<ICacheIntern, IISet<IObjRef>> cacheToOrisToLoad,
			IdentityLinkedMap<ICacheIntern, IMap<IObjRelation, Boolean>> cacheToOrelsToLoad, List<PrefetchCommand> loadItems)
		{
			if (objects is IEnumerable && !objects.GetType().Equals(typeof(String)))
			{
				foreach (Object item in (IEnumerable) objects)
				{
					if (item == null)
					{
						continue;
					}
					HandleObjects(item, entityTypeToPrefetchPath, alreadyHandledSet, cacheToOrisLoadedHistory, cacheToOrelsLoadedHistory,
							cacheToOrisToLoad, cacheToOrelsToLoad, loadItems);
				}
				return;
			}
			PrefetchPath[] cachePaths = null;
			if (entityTypeToPrefetchPath != null)
			{
				IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(objects.GetType());

				cachePaths = entityTypeToPrefetchPath.Get(metaData.EntityType);

				if (cachePaths == null || cachePaths.Length == 0)
				{
					return;
				}
			}
			EnsureInitializedRelationsIntern3(objects, cachePaths, entityTypeToPrefetchPath, cacheToOrisToLoad, cacheToOrelsToLoad, cacheToOrisLoadedHistory,
					cacheToOrelsLoadedHistory, alreadyHandledSet, loadItems);
		}

		protected void ProcessPendingOrelsAndObjRefs(ILinkedMap<Type, PrefetchPath[]> entityTypeToPrefetchPath,
			AlreadyHandledSet alreadyHandledSet, IdentityLinkedMap<ICacheIntern, IISet<IObjRef>> cacheToOrisLoadedHistory,
			IdentityLinkedMap<ICacheIntern, IISet<IObjRelation>> cacheToOrelsLoadedHistory, IdentityLinkedMap<ICacheIntern, IISet<IObjRef>> cacheToOrisToLoad,
			IdentityLinkedMap<ICacheIntern, IMap<IObjRelation, bool>> cacheToOrelsToLoad, List<PrefetchCommand> pendingPrefetchCommands,
			List<Object> hardRefList)
		{
			// all relation members where at least one instance of the owning entity type needs a prefetch on this member in the immediate next step
			MergePrefetchPathsCache mergePrefetchPathsCache = new MergePrefetchPathsCache(EntityMetaDataProvider);

			IdentityLinkedSet<Member> prioMembers = PrioMembersProvider.GetPrioMembers(entityTypeToPrefetchPath, pendingPrefetchCommands, mergePrefetchPathsCache);

			LoadAndAddOrels(cacheToOrelsToLoad, hardRefList, cacheToOrelsLoadedHistory, cacheToOrisToLoad, prioMembers);
			LoadAndAddOris(cacheToOrisToLoad, hardRefList, cacheToOrisLoadedHistory);

			while (pendingPrefetchCommands.Count > 0)
			{
				PrefetchCommand[] currentPrefetchCommands = pendingPrefetchCommands.ToArray();
				// Clear the items to be ready for cascaded items in new batch recursion step
				pendingPrefetchCommands.Clear();
				if (prioMembers.Count > 0)
				{
					for (int a = 0, size = currentPrefetchCommands.Length; a < size; a++)
					{
						PrefetchCommand prefetchCommand = currentPrefetchCommands[a];
						DirectValueHolderRef valueHolder = prefetchCommand.valueHolder;
						if (!prioMembers.Contains(valueHolder.Member))
						{
							currentPrefetchCommands[a] = null;
							pendingPrefetchCommands.Add(prefetchCommand);
						}
					}
				}
				GuiThreadHelper.InvokeInGuiAndWait(delegate()
				{
					ICacheModification cacheModification = CacheModification;
					ValueHolderContainerMixin valueHolderContainerMixin = ValueHolderContainerMixin;
					bool oldActive = cacheModification.Active;
					if (!oldActive)
					{
						cacheModification.Active = true;
					}
					try
					{
						foreach (PrefetchCommand prefetchCommand in currentPrefetchCommands)
						{
							if (prefetchCommand == null)
							{
								continue;
							}
							DirectValueHolderRef valueHolder = prefetchCommand.valueHolder;
							PrefetchPath[] cachePaths = prefetchCommand.prefetchPaths;

							RelationMember member = valueHolder.Member;
							// Merge the root prefetch path with the relative prefetch path
							cachePaths = mergePrefetchPathsCache.MergePrefetchPaths(member.ElementType, cachePaths, entityTypeToPrefetchPath);

							IObjRefContainer vhc = valueHolder.Vhc;
							ICacheIntern targetCache;
							bool doSetValue = false;
							if (valueHolder is IndirectValueHolderRef)
							{
								IndirectValueHolderRef valueHolderKey = (IndirectValueHolderRef)valueHolder;
								targetCache = valueHolderKey.RootCache;
							}
							else
							{
								targetCache = ((IValueHolderContainer)vhc).__TargetCache;
								doSetValue = true;
							}
							int relationIndex = vhc.Get__EntityMetaData().GetIndexByRelation(member);
							IObjRef[] objRefs = vhc.Get__ObjRefs(relationIndex);
							Object obj = valueHolderContainerMixin.GetValue(vhc, relationIndex, member, targetCache, objRefs, CacheDirective.FailEarly);
							if (doSetValue && obj != null)
							{
								member.SetValue(vhc, obj);
							}
							EnsureInitializedRelationsIntern3(obj, cachePaths, entityTypeToPrefetchPath, cacheToOrisToLoad, cacheToOrelsToLoad, cacheToOrisLoadedHistory,
									cacheToOrelsLoadedHistory, alreadyHandledSet, pendingPrefetchCommands);
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
				if (cacheToOrisToLoad.Count == 0 && cacheToOrelsToLoad.Count == 0 && pendingPrefetchCommands.Count == 0)
				{
					return;
				}
				prioMembers = PrioMembersProvider.GetPrioMembers(entityTypeToPrefetchPath, pendingPrefetchCommands, mergePrefetchPathsCache);
				LoadAndAddOrels(cacheToOrelsToLoad, hardRefList, cacheToOrelsLoadedHistory, cacheToOrisToLoad, prioMembers);
				LoadAndAddOris(cacheToOrisToLoad, hardRefList, cacheToOrisLoadedHistory);
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

        protected void LoadAndAddOrels(ILinkedMap<ICacheIntern, IMap<IObjRelation, bool>> cacheToOrelsToLoad, IList<Object> hardRefList,
            IMap<ICacheIntern, IISet<IObjRelation>> cacheToOrelsLoadedHistory, ILinkedMap<ICacheIntern, IISet<IObjRef>> cacheToOrisToLoad,
			IdentityLinkedSet<Member> prioMembers)
        {
            Iterator<Entry<ICacheIntern, IMap<IObjRelation, bool>>> iter = cacheToOrelsToLoad.Iterator();
            while (iter.MoveNext())
            {
                Entry<ICacheIntern, IMap<IObjRelation, bool>> entry = iter.Current;
                ICacheIntern cache = entry.Key;
                IMap<IObjRelation, bool> orelsToLoad = entry.Value;

				LoadAndAddOrels(cache, orelsToLoad, hardRefList, cacheToOrelsLoadedHistory, cacheToOrisToLoad, prioMembers);
				if (orelsToLoad.Count == 0)
				{
					iter.Remove();
				}
            }
        }

        protected void LoadAndAddOrels(ICacheIntern cache, IMap<IObjRelation, bool> orelsToLoad, IList<Object> hardRefList,
            IMap<ICacheIntern, IISet<IObjRelation>> cacheToOrelsLoadedHistory, ILinkedMap<ICacheIntern, IISet<IObjRef>> cacheToOrisToLoad,
			IdentityLinkedSet<Member> prioMembers)
        {
            IList<IObjRelation> objRelList;
			if (prioMembers.Count > 0)
			{
				objRelList = new List<IObjRelation>(orelsToLoad.Count);
				IEntityMetaDataProvider entityMetaDataProvider = this.EntityMetaDataProvider;
				foreach (Entry<IObjRelation, Boolean> entry in orelsToLoad)
				{
					IObjRelation objRel = entry.Key;
					IEntityMetaData metaData = entityMetaDataProvider.GetMetaData(objRel.RealType);
					RelationMember memberByName = (RelationMember) metaData.GetMemberByName(objRel.MemberName);
					if (!prioMembers.Contains(memberByName))
					{
						continue;
					}
					objRelList.Add(objRel);
				}
			}
			else
			{
				objRelList = orelsToLoad.KeyList();
			}
            IList<IObjRelationResult> objRelResults = cache.GetObjRelations(objRelList, cache, CacheDirective.ReturnMisses);

            IISet<IObjRef> orisToLoad = null;
            for (int a = 0, size = objRelResults.Count; a < size; a++)
            {
                IObjRelation objRel = objRelList[a];
				IObjRelationResult objRelResult = objRelResults[a];
				bool objRefsOnly = orelsToLoad.Remove(objRel);
				if (objRelResult == null)
				{
					continue;
				}
                if (orelsToLoad.Get(objRel))
                {
                    // fetch only the objRefs, not the objects themselves
                    continue;
                }
				IObjRef[] relations = objRelResult.Relations;

				if (relations.Length == 0 || objRefsOnly)
				{
					// fetch only the objRefs, not the objects themselves
					continue;
				}
				if (orisToLoad == null)
				{
					orisToLoad = cacheToOrisToLoad.Get(cache);
					if (orisToLoad == null)
					{
						orisToLoad = new CHashSet<IObjRef>();
						cacheToOrisToLoad.Put(cache, orisToLoad);
					}
				}
				orisToLoad.AddAll(relations);
            }
            IISet<IObjRelation> orelsLoadedHistory = cacheToOrelsLoadedHistory.Get(cache);
            if (orelsLoadedHistory == null)
            {
                orelsLoadedHistory = new CHashSet<IObjRelation>();
                cacheToOrelsLoadedHistory.Put(cache, orelsLoadedHistory);
            }
            orelsLoadedHistory.AddAll(objRelList);
        }

        protected void EnsureInitializedRelationsIntern3(Object obj, PrefetchPath[] cachePaths, ILinkedMap<Type, PrefetchPath[]> entityTypeToPrefetchPaths,
            IMap<ICacheIntern, IISet<IObjRef>> cacheToOrisToLoad, IMap<ICacheIntern, IMap<IObjRelation, bool>> cacheToOrelsToLoad,
            IMap<ICacheIntern, IISet<IObjRef>> cacheToOrisLoadedHistory, IMap<ICacheIntern, IISet<IObjRelation>> cacheToOrelsLoadedHistory,
			AlreadyHandledSet alreadyHandledSet, IList<PrefetchCommand> cascadeLoadItems)
        {
            if (obj == null)
            {
                return;
            }
			if (!alreadyHandledSet.PutIfNotExists(obj, cachePaths, true))
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
            if ((cachePaths == null || cachePaths.Length == 0) && entityTypeToPrefetchPaths == null)
            {
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
					EnsureInitializedRelationsIntern3(item, cachePaths, entityTypeToPrefetchPaths, cacheToOrisToLoad, cacheToOrelsToLoad,
                        cacheToOrisLoadedHistory, cacheToOrelsLoadedHistory, alreadyHandledSet, cascadeLoadItems);
                }
                return;
            }
            IEntityMetaData metaData = ((IEntityMetaDataHolder)obj).Get__EntityMetaData();
			if (cachePaths == null || cachePaths.Length == 0)
			{
				if (entityTypeToPrefetchPaths != null)
				{
					cachePaths = entityTypeToPrefetchPaths.Get(metaData.EntityType);
				}
				if (cachePaths == null || cachePaths.Length == 0)
				{
					return;
				}
			}
            RelationMember[] relationMembers = metaData.RelationMembers;
            if (relationMembers.Length == 0)
            {
                return;
            }
            IValueHolderContainer vhc = (IValueHolderContainer)obj;
            for (int a = cachePaths.Length; a-- > 0; )
            {
                PrefetchPath path = cachePaths[a];

                int relationIndex = path.memberIndex;
                RelationMember member = relationMembers[relationIndex];

                if (ValueHolderState.INIT != vhc.Get__State(relationIndex))
                {
                    DirectValueHolderRef vhk = new DirectValueHolderRef(vhc, member);
					EnsureInitializedRelationsIntern3(vhk, path.children, entityTypeToPrefetchPaths, cacheToOrisToLoad, cacheToOrelsToLoad, cacheToOrisLoadedHistory,
                            cacheToOrelsLoadedHistory, alreadyHandledSet, cascadeLoadItems);
                    continue;
                }
                Object memberValue = member.GetValue(obj);
                if (memberValue == null)
                {
                    continue;
                }
				EnsureInitializedRelationsIntern3(memberValue, path.children, entityTypeToPrefetchPaths, cacheToOrisToLoad, cacheToOrelsToLoad,
                    cacheToOrisLoadedHistory, cacheToOrelsLoadedHistory, alreadyHandledSet, cascadeLoadItems);
            }
        }

        protected bool HandleValueHolder(DirectValueHolderRef vhr, PrefetchPath[] cachePaths, IMap<ICacheIntern, IISet<IObjRef>> cacheToOrisToLoad,
            IMap<ICacheIntern, IMap<IObjRelation, bool>> cacheToOrelsToLoad, IMap<ICacheIntern, IISet<IObjRef>> cacheToOrisLoadedHistory,
			IMap<ICacheIntern, IISet<IObjRelation>> cacheToOrelsLoadedHistory, AlreadyHandledSet alreadyHandledSet, IList<PrefetchCommand> cascadeLoadItems)
        {
            RelationMember member = vhr.Member;
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
                    IObjRelation self = ValueHolderContainerMixin.GetSelf(rcv, member.Name);
                    ISet<IObjRelation> orelsLoadedHistory = cacheToOrelsLoadedHistory.Get(rootCache);
                    if (orelsLoadedHistory == null || !orelsLoadedHistory.Contains(self))
                    {
                        IMap<IObjRelation, bool> orelsToLoad = cacheToOrelsToLoad.Get(rootCache);
                        if (orelsToLoad == null)
                        {
                            orelsToLoad = new HashMap<IObjRelation, bool>();
                            cacheToOrelsToLoad.Put(rootCache, orelsToLoad);
                        }
                        orelsToLoad.Put(self, vhr.ObjRefsOnly);
                        AddCascadeLoadItem(vhr, cachePaths, cascadeLoadItems);
                    }
                    return false;
                }
                else if (!vhr.ObjRefsOnly && rcvObjRefs.Length > 0)
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
                        AddCascadeLoadItem(vhr, cachePaths, cascadeLoadItems);
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
                        IMap<IObjRelation, bool> orelsToLoad = cacheToOrelsToLoad.Get(cache);
                        if (orelsToLoad == null)
                        {
                            orelsToLoad = new HashMap<IObjRelation, bool>();
                            cacheToOrelsToLoad.Put(cache, orelsToLoad);
                        }
                        orelsToLoad.Put(self, vhr.ObjRefsOnly);
                        AddCascadeLoadItem(vhr, cachePaths, cascadeLoadItems);
                    }
                    return false;
                }
                objRefs = orelResult.Relations;
                if (objRefs != null)
                {
                    vhc.Set__ObjRefs(relationIndex2, objRefs);
                }
            }
            if (!vhr.ObjRefsOnly && objRefs != null && objRefs.Length > 0)
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
                AddCascadeLoadItem(vhr, cachePaths, cascadeLoadItems);
                return false;
            }
            return true;
        }

        protected void AddCascadeLoadItem(DirectValueHolderRef vhr, PrefetchPath[] cachePaths, IList<PrefetchCommand> cascadeLoadItems)
        {
            if (cachePaths != null || !vhr.ObjRefsOnly)
            {
				PrefetchCommand cascadeLoadItem = new PrefetchCommand(vhr, cachePaths);
                cascadeLoadItems.Add(cascadeLoadItem);
            }
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
            PrimitiveMember[] primitiveMembers = metaData.PrimitiveMembers;
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
                    PrimitiveMember primitiveMember = primitiveMembers[a];

                    Object primitiveValue = primitiveMember.GetValue(obj, true);

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
            RelationMember[] relationMembers = metaData.RelationMembers;

            if (relationMembers.Length == 0)
            {
                return ObjRef.EMPTY_ARRAY_ARRAY;
            }
            IValueHolderContainer vhc = (IValueHolderContainer)obj;
            IObjRef[][] relations = new IObjRef[relationMembers.Length][];

			IObjRefHelper objRefHelper = this.ObjRefHelper;
            for (int relationIndex = relationMembers.Length; relationIndex-- > 0; )
            {
                RelationMember relationMember = relationMembers[relationIndex];

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
				IList<IObjRef> oris = objRefHelper.ExtractObjRefList(relationValue, null);
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
            Member member = MemberTypeProvider.GetMember(typeof(S), sourceToTargetEntityPropertyPath);

            // MetaDaten der Ziel-Entity ermitteln, da wir (generisch) den PK brauchen, um damit ein DISTINCT-Behavior durch eine Map als Zwischenstruktur zu
            // erreichen
            IEntityMetaData targetMetaData = EntityMetaDataProvider.GetMetaData(member.ElementType);
            PrimitiveMember targetIdMember = targetMetaData.IdMember;

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

        public AppendableCachePath CopyCachePathToAppendable(PrefetchPath cachePath)
        {
            PrefetchPath[] children = cachePath.children;
            CHashSet<AppendableCachePath> clonedChildren = null;
            if (children != null)
            {
                clonedChildren = CHashSet<AppendableCachePath>.Create(children.Length);
                for (int a = children.Length; a-- > 0; )
                {
                    clonedChildren.Add(CopyCachePathToAppendable(children[a]));
                }
            }
            AppendableCachePath clonedCachePath = new AppendableCachePath(cachePath.memberType, cachePath.memberIndex, cachePath.memberName);
            clonedCachePath.children = clonedChildren;
            return clonedCachePath;
        }

        public PrefetchPath[] CopyAppendableToCachePath(CHashSet<AppendableCachePath> children)
        {
            if (children == null)
            {
                return null;
            }
            PrefetchPath[] clonedChildren = new PrefetchPath[children.Count];
            int index = 0;
            foreach (AppendableCachePath child in children)
            {
                clonedChildren[index] = CopyAppendableToCachePath(child);
                index++;
            }
            return clonedChildren;
        }

        public PrefetchPath CopyAppendableToCachePath(AppendableCachePath cachePath)
        {
            PrefetchPath[] clonedChildren = CopyAppendableToCachePath(cachePath.children);
			if (clonedChildren == null)
			{
				return new PrefetchPath(cachePath.memberType, cachePath.memberIndex, cachePath.memberName, clonedChildren, new Type[0]);
			}
			CHashSet<Type> memberTypesOnDescendants = new CHashSet<Type>();
			foreach (PrefetchPath clonedChild in clonedChildren)
			{
				memberTypesOnDescendants.Add(clonedChild.memberType);
				memberTypesOnDescendants.AddAll(clonedChild.memberTypesOnDescendants);
			}
			return new PrefetchPath(cachePath.memberType, cachePath.memberIndex, cachePath.memberName, clonedChildren,
					memberTypesOnDescendants.ToArray());
        }

        public void UnionCachePath(AppendableCachePath cachePath, AppendableCachePath other)
        {
            CHashSet<AppendableCachePath> otherChildren = other.children;
            if (otherChildren == null)
            {
                // fast case 1
                return;
            }
            CHashSet<AppendableCachePath> children = cachePath.children;
            if (children == null)
            {
                // fast case 2
                cachePath.children = otherChildren;
                return;
            }
            foreach (AppendableCachePath otherCachePath in otherChildren)
            {
                if (children.Add(otherCachePath))
                {
                    continue;
                }
                UnionCachePath(children.Get(otherCachePath), otherCachePath);
            }
        }
    }
}