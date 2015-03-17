using De.Osthus.Ambeth.Cache;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge.Incremental;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections.Generic;
using System.Threading;

namespace De.Osthus.Ambeth.Merge
{
    public class CUDResultApplier : ICUDResultApplier
    {
        protected class CloneState
        {
            public readonly IdentityHashMap<IObjRef, StateEntry> newObjRefToStateEntryMap;

            public readonly IncrementalMergeState incrementalState;

            public CloneState(IdentityHashMap<IObjRef, StateEntry> newObjRefToStateEntryMap,
                    IncrementalMergeState incrementalState)
            {
                this.newObjRefToStateEntryMap = newObjRefToStateEntryMap;
                this.incrementalState = incrementalState;
            }
        }

        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public IServiceContext BeanContext { protected get; set; }

        [Autowired]
        public ICacheContext CacheContext { protected get; set; }

        [Autowired]
        public IEntityFactory EntityFactory { protected get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        [Autowired]
        public IPrefetchHelper PrefetchHelper { protected get; set; }

        [Autowired]
        public IObjRefHelper ObjRefHelper { protected get; set; }

        protected readonly ThreadLocal<CloneState> cloneStateTL = new ThreadLocal<CloneState>();

        public IIncrementalMergeState AcquireNewState(ICache stateCache)
        {
            return BeanContext.RegisterBean<IncrementalMergeState>()//
                    .PropertyValue("StateCache", stateCache)//
                    .Finish();
        }

        public ICUDResult ApplyCUDResultOnEntitiesOfCache(ICUDResult cudResult, bool checkBaseState, IIncrementalMergeState incrementalState)
        {
            ICache cache = incrementalState.GetStateCache().CurrentCache;
            if (cache.CurrentCache == cache)
            {
                // given cache is already the current cache
                return ApplyIntern(cudResult, checkBaseState, (IncrementalMergeState)incrementalState);
            }
            return CacheContext.ExecuteWithCache(cache, new IResultingBackgroundWorkerParamDelegate<ICUDResult, ICUDResult>(delegate(ICUDResult state)
                {
                    return ApplyIntern(state, checkBaseState, (IncrementalMergeState)incrementalState);
                }), cudResult);
        }

        protected IList<Object> GetAllExistingObjectsFromCache(ICache cache, IList<IChangeContainer> allChanges)
        {
            List<IObjRef> existingObjRefs = new List<IObjRef>(allChanges.Count);
            for (int a = 0, size = allChanges.Count; a < size; a++)
            {
                IChangeContainer changeContainer = allChanges[a];
                if (changeContainer is CreateContainer)
                {
                    existingObjRefs.Add(null);
                    continue;
                }
                if (changeContainer.Reference is IDirectObjRef)
                {
                    throw new Exception();
                }
                existingObjRefs.Add(changeContainer.Reference);
            }
            return cache.GetObjects(existingObjRefs, CacheDirective.ReturnMisses);
        }

        protected ICUDResult ApplyIntern(ICUDResult cudResult, bool checkBaseState, IncrementalMergeState incrementalState)
        {
            ICache stateCache = incrementalState.GetStateCache();
            IList<IChangeContainer> allChanges = cudResult.AllChanges;
            IList<Object> originalRefs = cudResult.GetOriginalRefs();
            IList<Object> allObjects = GetAllExistingObjectsFromCache(stateCache, allChanges);
            List<Object> hardRefs = new List<Object>();
            hardRefs.Add(allObjects); // add list as item intended. adding each item of the source is NOT needed

            List<IObjRef> toFetchFromCache = new List<IObjRef>();
            List<DirectValueHolderRef> toPrefetch = new List<DirectValueHolderRef>();
            List<IBackgroundWorkerDelegate> runnables = new List<IBackgroundWorkerDelegate>();

            IEntityFactory entityFactory = this.EntityFactory;

            IdentityHashMap<IObjRef, StateEntry> newObjRefToStateEntryMap = new IdentityHashMap<IObjRef, StateEntry>();
            IdentityHashMap<IChangeContainer, IChangeContainer> alreadyClonedMap = new IdentityHashMap<IChangeContainer, IChangeContainer>();

            List<IChangeContainer> newAllChanges = new List<IChangeContainer>(allChanges.Count);

            for (int a = 0, size = allChanges.Count; a < size; a++)
            {
                IChangeContainer changeContainer = allChanges[a];
                Object originalEntity = originalRefs[a];

                StateEntry stateEntry = incrementalState.entityToStateMap.Get(originalEntity);

                IChangeContainer newChangeContainer;
                if (changeContainer is CreateContainer)
                {
                    newChangeContainer = new CreateContainer();
                }
                else if (changeContainer is UpdateContainer)
                {
                    newChangeContainer = new UpdateContainer();
                }
                else
                {
                    newChangeContainer = new DeleteContainer();
                }
                newAllChanges.Add(newChangeContainer);
                alreadyClonedMap.Put(changeContainer, newChangeContainer);

                if (!(changeContainer is CreateContainer))
                {
                    Object stateCacheEntity2 = allObjects[a];
                    stateEntry = incrementalState.entityToStateMap.Get(stateCacheEntity2);
                    if (stateEntry == null)
                    {
                        stateEntry = new StateEntry(stateCacheEntity2, changeContainer.Reference, incrementalState.entityToStateMap.Count + 1);

                        incrementalState.entityToStateMap.Put(stateCacheEntity2, stateEntry);
                        incrementalState.objRefToStateMap.Put(stateEntry.objRef, stateEntry);
                    }
                    // delete & update do not need further handling
                    continue;
                }
                Type realType = changeContainer.Reference.RealType;

                Object stateCacheEntity;
                if (stateEntry == null)
                {
                    stateCacheEntity = entityFactory.CreateEntity(realType);

                    DirectObjRef directObjRef = new DirectObjRef(realType, stateCacheEntity);
                    directObjRef.CreateContainerIndex = a;

                    stateEntry = new StateEntry(stateCacheEntity, directObjRef, incrementalState.entityToStateMap.Count + 1);

                    incrementalState.entityToStateMap.Put(stateCacheEntity, stateEntry);
                    incrementalState.objRefToStateMap.Put(stateEntry.objRef, stateEntry);
                    newObjRefToStateEntryMap.Put(changeContainer.Reference, stateEntry);
                }
                else
                {
                    stateCacheEntity = stateEntry.entity;
                }
                allObjects[a] = stateCacheEntity;
            }
            cloneStateTL.Value = new CloneState(newObjRefToStateEntryMap, incrementalState);
            try
            {
                for (int a = allChanges.Count; a-- > 0; )
                {
                    IChangeContainer changeContainer = allChanges[a];
                    IObjRefContainer entity = (IObjRefContainer)allObjects[a];

                    changeContainer = FillClonedChangeContainer(changeContainer, alreadyClonedMap);

                    IPrimitiveUpdateItem[] puis;
                    IRelationUpdateItem[] ruis;
                    if (changeContainer is CreateContainer)
                    {
                        CreateContainer createContainer = (CreateContainer)changeContainer;
                        puis = createContainer.Primitives;
                        ruis = createContainer.Relations;
                    }
                    else if (changeContainer is UpdateContainer)
                    {
                        UpdateContainer updateContainer = (UpdateContainer)changeContainer;
                        puis = updateContainer.Primitives;
                        ruis = updateContainer.Relations;
                    }
                    else
                    {
                        ((IDataObject)entity).ToBeDeleted = true;
                        continue;
                    }
                    IEntityMetaData metaData = ((IEntityMetaDataHolder)entity).Get__EntityMetaData();
                    ApplyPrimitiveUpdateItems(entity, puis, metaData);

                    if (ruis != null)
                    {
                        bool isUpdate = changeContainer is UpdateContainer;
                        foreach (IRelationUpdateItem rui in ruis)
                        {
                            ApplyRelationUpdateItem(entity, rui, isUpdate, metaData, toPrefetch, toFetchFromCache, checkBaseState, runnables);
                        }
                    }
                }
                while (toPrefetch.Count > 0 || toFetchFromCache.Count > 0 || runnables.Count > 0)
                {
                    if (toPrefetch.Count > 0)
                    {
                        PrefetchHelper.Prefetch(toPrefetch);
                        toPrefetch.Clear();
                    }
                    if (toFetchFromCache.Count > 0)
                    {
                        IList<Object> fetchedObjects = stateCache.GetObjects(toFetchFromCache, CacheDirective.None);
                        hardRefs.Add(fetchedObjects); // add list as item intended. adding each item of the source is NOT needed
                        toFetchFromCache.Clear();
                    }
                    IBackgroundWorkerDelegate[] runnableArray = runnables.ToArray();
                    runnables.Clear();
                    foreach (IBackgroundWorkerDelegate runnable in runnableArray)
                    {
                        runnable();
                    }
                }
                List<Object> newObjects = new List<Object>(allObjects.Count);
                List<DirectValueHolderRef> changedRelationRefs = new List<DirectValueHolderRef>();
                for (int a = allObjects.Count; a-- > 0; )
                {
                    IChangeContainer newChange = newAllChanges[a];
                    IRelationUpdateItem[] ruis = null;
                    Object entity = allObjects[a];
                    if (newChange is CreateContainer)
                    {
                        newObjects.Add(entity);
                        ruis = ((CreateContainer)newChange).Relations;
                    }
                    else if (newChange is UpdateContainer)
                    {
                        ruis = ((UpdateContainer)newChange).Relations;
                    }
                    if (ruis == null)
                    {
                        continue;
                    }
                    IEntityMetaData metaData = EntityMetaDataProvider.GetMetaData(entity.GetType());
                    foreach (IRelationUpdateItem rui in ruis)
                    {
                        Member member = metaData.GetMemberByName(rui.MemberName);
                        changedRelationRefs.Add(new DirectValueHolderRef((IObjRefContainer)entity, (RelationMember)member));
                    }
                }
                if (newObjects.Count > 0)
                {
                    ((IWritableCache)stateCache).Put(newObjects);
                }
                if (changedRelationRefs.Count > 0)
                {
                    PrefetchHelper.Prefetch(changedRelationRefs);
                }
                return new CUDResult(newAllChanges, allObjects);
            }
            finally
            {
                cloneStateTL.Value = null;
            }
        }

        protected IChangeContainer FillClonedChangeContainer(IChangeContainer original, IdentityHashMap<IChangeContainer, IChangeContainer> alreadyClonedMap)
        {
            IChangeContainer clone = alreadyClonedMap.Get(original);
            if (clone is CreateContainer)
            {
                ((CreateContainer)clone).Primitives = ClonePrimitives(((CreateContainer)original).Primitives);
                ((CreateContainer)clone).Relations = CloneRelations(((CreateContainer)original).Relations);
            }
            else if (clone is UpdateContainer)
            {
                ((UpdateContainer)clone).Primitives = ClonePrimitives(((UpdateContainer)original).Primitives);
                ((UpdateContainer)clone).Relations = CloneRelations(((UpdateContainer)original).Relations);
            }
            ((AbstractChangeContainer)clone).Reference = CloneObjRef(original.Reference, true);
            return clone;
        }

        protected IRelationUpdateItem[] CloneRelations(IRelationUpdateItem[] original)
        {
            if (original == null)
            {
                return null;
            }
            IRelationUpdateItem[] clone = new IRelationUpdateItem[original.Length];
            for (int a = original.Length; a-- > 0; )
            {
                clone[a] = CloneRelation(original[a]);
            }
            return clone;
        }

        protected IPrimitiveUpdateItem[] ClonePrimitives(IPrimitiveUpdateItem[] original)
        {
            // no need to clone PUIs. even the array is assumed to be never modified
            return original;
        }

        protected IRelationUpdateItem CloneRelation(IRelationUpdateItem original)
        {
            RelationUpdateItem clone = new RelationUpdateItem();
            clone.MemberName = original.MemberName;
            clone.AddedORIs = CloneObjRefs(original.AddedORIs);
            clone.RemovedORIs = CloneObjRefs(original.RemovedORIs);
            return clone;
        }

        protected IObjRef[] CloneObjRefs(IObjRef[] original)
        {
            if (original == null || original.Length == 0)
            {
                return original;
            }
            IObjRef[] clone = new IObjRef[original.Length];
            for (int a = original.Length; a-- > 0; )
            {
                clone[a] = CloneObjRef(original[a], true);
            }
            return clone;
        }

        protected IObjRef CloneObjRef(IObjRef original, bool fromChangeContainer)
        {
            CloneState cloneState = cloneStateTL.Value;
            return ResolveObjRefOfCache(original, cloneState);
        }

        protected void ApplyPrimitiveUpdateItems(Object entity, IPrimitiveUpdateItem[] puis, IEntityMetaData metadata)
        {
            if (puis == null)
            {
                return;
            }

            foreach (IPrimitiveUpdateItem pui in puis)
            {
                String memberName = pui.MemberName;
                Object newValue = pui.NewValue;
                Member member = metadata.GetMemberByName(memberName);
                member.SetValue(entity, newValue);
            }
        }

        protected void ApplyRelationUpdateItem(IObjRefContainer entity, IRelationUpdateItem rui, bool isUpdate,
                IEntityMetaData metaData, IList<DirectValueHolderRef> toPrefetch, List<IObjRef> toFetchFromCache, bool checkBaseState,
                IList<IBackgroundWorkerDelegate> runnables)
        {
            IObjRefHelper objRefHelper = this.ObjRefHelper;
            String memberName = rui.MemberName;
            int relationIndex = metaData.GetIndexByRelationName(memberName);
            RelationMember relationMember = metaData.RelationMembers[relationIndex];
            IObjRef[] existingORIs;
            if (entity.Is__Initialized(relationIndex))
            {
                existingORIs = ListUtil.ToArray(ObjRefHelper.ExtractObjRefList(relationMember.GetValue(entity), null));
            }
            else
            {
                existingORIs = entity.Get__ObjRefs(relationIndex);
                if (existingORIs == null)
                {
                    toPrefetch.Add(new DirectValueHolderRef(entity, relationMember, true));
                    runnables.Add(new IBackgroundWorkerDelegate(delegate()
                        {
                            ApplyRelationUpdateItem(entity, rui, isUpdate, metaData, toPrefetch, toFetchFromCache, checkBaseState, runnables);
                        }));
                    return;
                }
            }
            IObjRef[] addedORIs = rui.AddedORIs;
            IObjRef[] removedORIs = rui.RemovedORIs;

            IObjRef[] newORIs;
            if (existingORIs.Length == 0)
            {
                if (checkBaseState && removedORIs != null)
                {
                    throw new Exception("Removing from empty member");
                }
                newORIs = addedORIs != null ? (IObjRef[])addedORIs.Clone() : ObjRef.EMPTY_ARRAY;
                for (int a = newORIs.Length; a-- > 0; )
                {
                    newORIs[a] = CloneObjRef(newORIs[a], false);
                }
            }
            else
            {
                // Set to efficiently remove entries
                LinkedHashSet<IObjRef> existingORIsSet = new LinkedHashSet<IObjRef>(existingORIs);
                if (removedORIs != null)
                {
                    foreach (IObjRef removedORI in removedORIs)
                    {
                        IObjRef clonedObjRef = CloneObjRef(removedORI, false);
                        if (existingORIsSet.Remove(clonedObjRef) || !checkBaseState)
                        {
                            continue;
                        }
                        throw OptimisticLockUtil.ThrowModified(objRefHelper.EntityToObjRef(entity), null, entity);
                    }
                }
                if (addedORIs != null)
                {
                    foreach (IObjRef addedORI in addedORIs)
                    {
                        IObjRef clonedObjRef = CloneObjRef(addedORI, false);
                        if (existingORIsSet.Add(clonedObjRef) || !checkBaseState)
                        {
                            continue;
                        }
                        throw OptimisticLockUtil.ThrowModified(objRefHelper.EntityToObjRef(entity), null, entity);
                    }
                }
                if (existingORIsSet.Count == 0)
                {
                    newORIs = ObjRef.EMPTY_ARRAY;
                }
                else
                {
                    newORIs = existingORIsSet.ToArray();
                }
            }
            if (!entity.Is__Initialized(relationIndex))
            {
                entity.Set__ObjRefs(relationIndex, newORIs);
                return;
            }
            toFetchFromCache.AddRange(newORIs);
            runnables.Add(new IBackgroundWorkerDelegate(delegate()
                {
                    ICache stateCache = cloneStateTL.Value.incrementalState.GetStateCache();
                    IList<Object> objects = stateCache.GetObjects(newORIs, CacheDirective.FailEarly);
                    Object value;
                    if (relationMember.IsToMany)
                    {
                        // To-many relation
                        Object coll = ListUtil.CreateObservableCollectionOfType(relationMember.RealType, objects.Count);
                        ListUtil.FillList(coll, objects);
                        value = coll;
                    }
                    else
                    {
                        // To-one relation
                        value = objects.Count > 0 ? objects[0] : null;
                    }
                    relationMember.SetValue(entity, value);
                }));
        }

        protected IObjRef ResolveObjRefOfCache(IObjRef objRef, CloneState cloneState)
        {
            StateEntry stateEntry = cloneState.incrementalState.objRefToStateMap.Get(objRef);
            if (stateEntry != null)
            {
                return stateEntry.objRef;
            }
            stateEntry = cloneState.newObjRefToStateEntryMap.Get(objRef);
            if (stateEntry != null)
            {
                return stateEntry.objRef;
            }
            return objRef;
        }
    }
}