using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Datachange.Model;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Template;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections;
using System.Collections.Generic;

namespace De.Osthus.Ambeth.Cache
{
    public class CacheDataChangeListener : IEventListener, IEventTargetEventListener
    {
        protected static readonly CacheDirective cacheValueResultAndReturnMissesSet = CacheDirective.CacheValueResult | CacheDirective.ReturnMisses;

        [LogInstance]
        public ILogger Log { private get; set; }

        [Autowired]
        public ICacheModification CacheModification { protected get; set; }

        [Autowired]
        public IEntityMetaDataProvider EntityMetaDataProvider { protected get; set; }

        [Autowired]
        public IEventDispatcher EventDispatcher { protected get; set; }

        [Autowired]
        public IFirstLevelCacheManager FirstLevelCacheManager { protected get; set; }

        [Autowired]
        public IGuiThreadHelper GuiThreadHelper { protected get; set; }
        
        [Autowired]
        public ISecondLevelCacheManager SecondLevelCacheManager { protected get; set; }

        [Autowired]
        public ValueHolderContainerTemplate ValueHolderContainerTemplate { protected get; set; }

        public void HandleEvent(Object eventObject, DateTime dispatchTime, long sequenceId)
        {
            DataChanged((IDataChange)eventObject, null, null, dispatchTime, sequenceId);
        }

        public void HandleEvent(Object eventObject, Object eventTarget, IList<Object> pausedEventTargets, DateTime dispatchTime, long sequenceId)
        {
            DataChanged((IDataChange)eventObject, eventTarget, pausedEventTargets, dispatchTime, sequenceId);
        }

        protected void DataChanged(IDataChange dataChange, Object eventTarget, IList<Object> pausedEventTargets, DateTime dispatchTime, long sequenceId)
        {
            IRootCache rootCache = SecondLevelCacheManager.SelectSecondLevelCache();
            IList<IWritableCache> selectedFirstLevelCaches = FirstLevelCacheManager.SelectFirstLevelCaches();

            if (pausedEventTargets != null && pausedEventTargets.Count > 0)
            {
                IdentityHashSet<Object> collisionSet = new IdentityHashSet<Object>();
                collisionSet.Add(rootCache);
                collisionSet.AddAll(selectedFirstLevelCaches);
                collisionSet.RetainAll(new IdentityHashSet<Object>(pausedEventTargets));
                if (collisionSet.Count > 0)
                {
                    // Restore ALL necessary items to handle this DCE
                    collisionSet.Clear();
                    collisionSet.Add(rootCache);
                    collisionSet.AddAll(selectedFirstLevelCaches);
                    // Without the current rootcache we can not handle the event now. We have to block till the rootCache and all childCaches get valid
                    EventDispatcher.WaitEventToResume(collisionSet, -1, delegate(IProcessResumeItem processResumeItem)
                    {
                        DataChangedIntern(dataChange, pausedEventTargets, processResumeItem, rootCache, selectedFirstLevelCaches);
                    }, null);
                    return;
                }
            }
            DataChangedIntern(dataChange, pausedEventTargets, null, rootCache, selectedFirstLevelCaches);
        }

        protected void DataChangedIntern(IDataChange dataChange, IList<Object> pausedEventTargets, IProcessResumeItem processResumeItem, IRootCache rootCache,
            IList<IWritableCache> selectedFirstLevelCaches)
        {
            try
            {
                bool isLocalSource = dataChange.IsLocalSource;
                IList<IDataChangeEntry> deletes = dataChange.Deletes;
                IList<IDataChangeEntry> updates = dataChange.Updates;
                IList<IDataChangeEntry> inserts = dataChange.Inserts;

                ISet<Type> occuringTypes = new HashSet<Type>();
                ISet<IObjRef> intermediateDeletes = new HashSet<IObjRef>();
                ISet<IObjRef> deletesSet = new HashSet<IObjRef>();
                IDictionary<IObjRef, ILoadContainer> objRefToLoadContainerDict = new Dictionary<IObjRef, ILoadContainer>();
                ISet<Type> directRelatingTypes = new HashSet<Type>();
                IList<CacheChangeItem> cacheChangeItems = new List<CacheChangeItem>();
                bool acquirementSuccessful = rootCache.AcquireHardRefTLIfNotAlready();
                try
                {
                    for (int a = deletes.Count; a-- > 0; )
                    {
                        IDataChangeEntry deleteEntry = deletes[a];
                        Type entityType = deleteEntry.EntityType;
                        occuringTypes.Add(entityType);
                        if (deleteEntry is DirectDataChangeEntry)
                        {
                            // Ignore delete entries of unpersisted objects here
                            continue;
                        }
                        ObjRef tempORI = new ObjRef(entityType, deleteEntry.IdNameIndex, deleteEntry.Id, deleteEntry.Version);
                        deletesSet.Add(tempORI);
                    }

                    // Remove items from the cache only if they are really deleted/updates by a remote event
                    // And not 'simulated' by a local source
                    if (pausedEventTargets != null && (deletes.Count > 0 || updates.Count > 0) && !isLocalSource)
                    {
                        Lock rootCacheWriteLock = rootCache.WriteLock;

                            IList<IObjRef> deletesList = ListUtil.ToList(deletesSet);
					    List<IObjRef> objRefsRemovePriorVersions = new List<IObjRef>(updates.Count);
					    for (int a = updates.Count; a-- > 0;)
					    {
						    IDataChangeEntry updateEntry = updates[a];
						    Type entityType = updateEntry.EntityType;
						    occuringTypes.Add(entityType);
						    objRefsRemovePriorVersions.Add(new ObjRef(entityType, updateEntry.IdNameIndex, updateEntry.Id, updateEntry.Version));
					    }
                        rootCacheWriteLock.Lock();
                        try
                        {
                            rootCache.Remove(deletesList);
                            rootCache.RemovePriorVersions(objRefsRemovePriorVersions);
                        }
                        finally
                        {
                            rootCacheWriteLock.Unlock();
                        }
                    }
                    else if (updates.Count > 0)
                    {
                        for (int a = updates.Count; a-- > 0; )
                        {
                            IDataChangeEntry updateEntry = updates[a];
                            Type entityType = updateEntry.EntityType;
                            occuringTypes.Add(entityType);
                        }
                    }
                    for (int a = inserts.Count; a-- > 0; )
                    {
                        IDataChangeEntry insertEntry = inserts[a];
                        Type entityType = insertEntry.EntityType;
                        occuringTypes.Add(entityType);
                    }
                    EnsureMetaDataIsLoaded(occuringTypes, directRelatingTypes);

                    ISet<IObjRef> orisToLoad = new HashSet<IObjRef>();
                    ISet<IObjRef> hardRefOrisToLoad = new HashSet<IObjRef>();
                    ISet<Object> hardRefChildren = new IdentityHashSet<Object>();

                    SelectFirstLevelChanges(selectedFirstLevelCaches, hardRefChildren, directRelatingTypes, hardRefOrisToLoad,
                        dataChange, cacheChangeItems, orisToLoad, deletesSet, rootCache);

                    IList<IObjRef> hardRefOrisToLoadList = ListUtil.ToList(hardRefOrisToLoad);
                    // Hold cache values as hard ref to prohibit cache loss due to GC
                    IList<Object> hardRefResult = rootCache.GetObjects(hardRefOrisToLoadList, cacheValueResultAndReturnMissesSet);
                    for (int a = hardRefResult.Count; a-- > 0; )
                    {
                        Object hardRef = hardRefResult[a];
                        if (hardRef != null)
                        {
                            continue;
                        }
                        // Objects are marked as UPDATED in the DCE, but could not be newly retrieved from the server
                        // This occurs if a fast DELETE event on the server happened but has not been processed, yet
                        IObjRef hardRefOriToLoad = hardRefOrisToLoadList[a];
                        intermediateDeletes.Add(hardRefOriToLoad);
                        orisToLoad.Remove(hardRefOriToLoad);
                    }
                    IList<Object> refreshResult = rootCache.GetObjects(ListUtil.ToList(orisToLoad), CacheDirective.LoadContainerResult);
                    foreach (ILoadContainer loadContainer in refreshResult)
                    {
                        objRefToLoadContainerDict[loadContainer.Reference] = loadContainer;
                    }
                    CHashSet<IObjRef> cascadeRefreshObjRefsSet = new CHashSet<IObjRef>();
                    CHashSet<IObjRelation> cascadeRefreshObjRelationsSet = new CHashSet<IObjRelation>();
                    CheckCascadeRefreshNeeded(cacheChangeItems, objRefToLoadContainerDict, cascadeRefreshObjRefsSet, cascadeRefreshObjRelationsSet);
                    if (cascadeRefreshObjRelationsSet.Count > 0)
                    {
                        IList<IObjRelationResult> relationsResult = rootCache.GetObjRelations(cascadeRefreshObjRelationsSet.ToList(), CacheDirective.LoadContainerResult);
                        foreach (IObjRelationResult relationResult in relationsResult)
                        {
                            cascadeRefreshObjRefsSet.AddAll(relationResult.Relations);
                        }
                        // apply gathered information of unknown relations to the rootCache
                        rootCache.Put(relationsResult);
                    }
                    if (cascadeRefreshObjRefsSet.Count > 0)
                    {
                        refreshResult = rootCache.GetObjects(cascadeRefreshObjRefsSet.ToList(), CacheDirective.LoadContainerResult);
                    }
                    if (cacheChangeItems.Count > 0)
                    {
                        GuiThreadHelper.InvokeInGuiAndWait(delegate()
                        {
                            bool oldFailEarlyModeActive = AbstractCache<Object>.FailEarlyModeActive;
                            try
                            {
                                AbstractCache<Object>.FailEarlyModeActive = true;
                                ChangeFirstLevelCaches(rootCache, occuringTypes, directRelatingTypes, cacheChangeItems, intermediateDeletes);
                            }
                            finally
                            {
                                AbstractCache<Object>.FailEarlyModeActive = oldFailEarlyModeActive;
                            }
                        });
                    }
                }
                finally
                {
                    rootCache.ClearHardRefs(acquirementSuccessful);
                }
            }
            finally
            {
                if (processResumeItem != null)
                {
                    processResumeItem.ResumeProcessingFinished();
                }
            }
        }

        protected void SelectFirstLevelChanges(IList<IWritableCache> firstLevelCaches,
            ISet<Object> hardRefChildren, ISet<Type> directRelatingTypes, ISet<IObjRef> hardRefOrisToLoad, IDataChange dataChange,
            IList<CacheChangeItem> cciList, ISet<IObjRef> orisToLoad, ISet<IObjRef> deletedSet, IRootCache rootCache)
        {
            List<IDataChangeEntry> insertsAndUpdates = new List<IDataChangeEntry>();
            IList<IDataChangeEntry> deletes = dataChange.Deletes;

            insertsAndUpdates.AddRange(dataChange.Updates);
            insertsAndUpdates.AddRange(dataChange.Inserts);
            List<IObjRef> changesToSearchInCache = new List<IObjRef>(insertsAndUpdates.Count);
            List<IObjRef> changesWithVersion = new List<IObjRef>(insertsAndUpdates.Count);
            List<IObjRef> deletesToSearchInCache = new List<IObjRef>(deletes.Count);
            for (int a = deletes.Count; a-- > 0; )
            {
                IDataChangeEntry deleteEntry = deletes[a];
                Object id = deleteEntry.Id;
                if (id == null)
                {
                    deletesToSearchInCache.Add(null);
                    continue;
                }
                deletesToSearchInCache.Add(new ObjRef(deleteEntry.EntityType, deleteEntry.IdNameIndex, id, null));
            }
            for (int a = insertsAndUpdates.Count; a-- > 0; )
            {
                IDataChangeEntry updateEntry = insertsAndUpdates[a];
                Object id = updateEntry.Id;
                if (id == null)
                {
                    changesToSearchInCache.Add(null);
                    continue;
                }
                changesToSearchInCache.Add(new ObjRef(updateEntry.EntityType, updateEntry.IdNameIndex, id, null));
                changesWithVersion.Add(new ObjRef(updateEntry.EntityType, updateEntry.IdNameIndex, id, updateEntry.Version));
            }

            for (int flcIndex = firstLevelCaches.Count; flcIndex-- > 0; )
            {
                IWritableCache childCache = firstLevelCaches[flcIndex];
                insertsAndUpdates.Clear();
                //childCache.GetContent(delegate(Type entityType, sbyte idIndex, Object id, Object value)
                //    {
                //        // This entity will be refreshed with (potential) new information about the objects which
                //        // have been specified as changed in the DCE
                //        // This is a robustness feature because normally a DCE contains ALL information about object
                //        // changes. And if object relations have been changed they are (supposed to) always been changed
                //        // for both sides of the relation and therefore imply a change on both entities
                //        // We store a hard ref to this object to ensure that the GC does not cut the reference in between
                //        hardRefChildren.Add(value);
                //        if (!directRelatingTypes.Contains(entityType))
                //        {
                //            return;
                //        }
                //        tempORI.Init(entityType, idIndex, id, null);
                //        if (deletedSet.Contains(tempORI) || !hardRefOrisToLoad.Add(tempORI))
                //        {
                //            // intended blank
                //        }
                //        else
                //        {
                //            tempORI = new ObjRef();
                //        }
                //    });

                List<IObjRef> objectRefsToDelete = new List<IObjRef>();
                List<IObjRef> objectRefsToUpdate = new List<IObjRef>();
                List<Object> objectsToUpdate = new List<Object>();

                Lock readLock = childCache.ReadLock;
                readLock.Lock();
                try
                {
                    IList<Object> deletesInCache = childCache.GetObjects(deletesToSearchInCache, CacheDirective.FailEarly | CacheDirective.ReturnMisses);
                    for (int a = deletesToSearchInCache.Count; a-- > 0; )
                    {
                        Object result = deletesInCache[a];
                        if (result == null)
                        {
                            // not in this cache
                            continue;
                        }
                        objectRefsToDelete.Add(deletesToSearchInCache[a]);
                    }
                    IList<Object> changesInCache = childCache.GetObjects(changesToSearchInCache, CacheDirective.FailEarly | CacheDirective.ReturnMisses);
                    for (int a = changesToSearchInCache.Count; a-- > 0; )
                    {
                        Object result = changesInCache[a];
                        if (result == null)
                        {
                            // not in this cache
                            continue;
                        }
                        IObjRef objRefWithVersion = changesWithVersion[a];
                        hardRefOrisToLoad.Add(objRefWithVersion);

                        if (result is IDataObject)
                        {
                            IDataObject dataObject = (IDataObject)result;
                            if (dataObject.ToBeUpdated || dataObject.ToBeDeleted)
                            {
                                continue;
                            }
                        }
                        if (objRefWithVersion.Version != null)
                        {
                            IEntityMetaData metaData = ((IEntityMetaDataHolder)result).Get__EntityMetaData();
                            Object versionInCache = metaData.VersionMember != null ? metaData.VersionMember.GetValue(result, false) : null;
                            if (versionInCache != null && ((IComparable)objRefWithVersion.Version).CompareTo(versionInCache) <= 0)
                            {
                                continue;
                            }
                        }
                        objectsToUpdate.Add(result);
                        orisToLoad.Add(objRefWithVersion);
                        // scanForInitializedObjects(result, alreadyScannedObjects, hardRefOrisToLoad);
                        objectRefsToUpdate.Add(objRefWithVersion);
                    }
                }
                finally
                {
                    readLock.Unlock();
                }
                if (objectRefsToDelete.Count == 0 && objectsToUpdate.Count == 0)
                {
                    continue;
                }
                CacheChangeItem cci = new CacheChangeItem();
                cci.Cache = childCache;
                cci.DeletedObjRefs = objectRefsToDelete;
                cci.UpdatedObjRefs = objectRefsToUpdate;
                cci.UpdatedObjects = objectsToUpdate;
                cciList.Add(cci);
            }
        }

        protected void ScanForInitializedObjects(Object obj, ISet<Object> alreadyScannedObjects, ISet<IObjRef> objRefs)
        {
            if (obj == null || !alreadyScannedObjects.Add(obj))
            {
                return;
            }
            if (obj is IList)
            {
                IList list = (IList)obj;
                for (int a = list.Count; a-- > 0; )
                {
                    Object item = list[a];
                    ScanForInitializedObjects(item, alreadyScannedObjects, objRefs);
                }
                return;
            }
            if (obj is IEnumerable)
            {
                foreach (Object item in (IEnumerable)obj)
                {
                    ScanForInitializedObjects(item, alreadyScannedObjects, objRefs);
                }
                return;
            }
            IEntityMetaData metaData = ((IEntityMetaDataHolder)obj).Get__EntityMetaData();
            Object id = metaData.IdMember.GetValue(obj, false);
            if (id == null)
            {
                // This may happen if a normally retrieved object gets deleted and therefore has lost its primary id
                // TODO: The object is still contained in the cache, maybe this should be reviewed
                return;
            }
            ObjRef objRef = new ObjRef(metaData.EntityType, ObjRef.PRIMARY_KEY_INDEX, id, null);
            objRefs.Add(objRef);
            RelationMember[] relationMembers = metaData.RelationMembers;
            if (relationMembers.Length == 0)
            {
                return;
            }
            IObjRefContainer vhc = (IObjRefContainer)obj;
            for (int relationIndex = relationMembers.Length; relationIndex-- > 0; )
            {
                RelationMember relationMember = relationMembers[relationIndex];
                if (ValueHolderState.INIT != vhc.Get__State(relationIndex))
                {
                    continue;
                }
                Object value = relationMember.GetValue(obj, false);
                ScanForInitializedObjects(value, alreadyScannedObjects, objRefs);
            }
        }

        protected virtual void EnsureMetaDataIsLoaded(ISet<Type> occuringTypes, ISet<Type> directRelatingTypes)
        {
            ISet<Type> wholeRelatedTypes = new HashSet<Type>(occuringTypes);
            List<Type> additionalTypes = new List<Type>();
            {
                // Own code scope
                IList<IEntityMetaData> occuringMetaData = EntityMetaDataProvider.GetMetaData(ListUtil.ToList(occuringTypes));
                foreach (IEntityMetaData metaData in occuringMetaData)
                {
                    Type[] typesRelatingToThis = metaData.TypesRelatingToThis;
                    for (int a = typesRelatingToThis.Length; a-- > 0; )
                    {
                        Type type = typesRelatingToThis[a];
                        directRelatingTypes.Add(type);
                        if (wholeRelatedTypes.Add(type))
                        {
                            // Additional related type in this whole datachange
                            additionalTypes.Add(type);
                        }
                    }
                }
            }
            while (additionalTypes.Count > 0)
            {
                IList<IEntityMetaData> additionalMetaData = EntityMetaDataProvider.GetMetaData(additionalTypes);
                additionalTypes.Clear();
                foreach (IEntityMetaData metaData in additionalMetaData)
                {
                    Type[] typesRelatingToThis = metaData.TypesRelatingToThis;
                    for (int a = typesRelatingToThis.Length; a-- > 0; )
                    {
                        Type type = typesRelatingToThis[a];
                        if (wholeRelatedTypes.Add(type))
                        {
                            // Additional related type in this whole datachange
                            additionalTypes.Add(type);
                        }
                    }
                }
            }
        }

        protected virtual void CheckCascadeRefreshNeeded(IList<CacheChangeItem> cacheChangeItems,
            IDictionary<IObjRef, ILoadContainer> objRefToLoadContainerDict, IISet<IObjRef> cascadeRefreshObjRefsSet,
            ISet<IObjRelation> cascadeRefreshObjRelationsSet)
        {
            foreach (CacheChangeItem cci in cacheChangeItems)
            {
                IWritableCache childCache = cci.Cache;
                IList<IObjRef> objectRefsToUpdate = cci.UpdatedObjRefs;
                IList<Object> objectsToUpdate = cci.UpdatedObjects;

                for (int a = objectRefsToUpdate.Count; a-- > 0; )
                {
                    IObjRef objRefToUpdate = objectRefsToUpdate[a];
                    Object objectToUpdate = objectsToUpdate[a];
                    ILoadContainer loadContainer = DictionaryExtension.ValueOrDefault(objRefToLoadContainerDict, objRefToUpdate);
                    if (loadContainer == null)
                    {
                        // Current value in childCache is not in our interest here
                        continue;
                    }
                    IObjRef[][] relations = loadContainer.Relations;
                    IEntityMetaData metaData = ((IEntityMetaDataHolder)objectToUpdate).Get__EntityMetaData();
                    RelationMember[] relationMembers = metaData.RelationMembers;
                    if (relationMembers.Length == 0)
                    {
                        continue;
                    }
                    IObjRefContainer vhc = (IObjRefContainer)objectToUpdate;
                    for (int relationIndex = relationMembers.Length; relationIndex-- > 0; )
                    {
                        if (ValueHolderState.INIT != vhc.Get__State(relationIndex))
                        {
                            continue;
                        }
                        // the object which has to be updated has initialized relations. So we have to ensure
                        // that these relations are in the RootCache at the time the target object will be updated.
                        // This is because initialized relations have to remain initialized after update but the relations
                        // may have updated, too
                        BatchPendingRelations(vhc, relationMembers[relationIndex], relations[relationIndex],
                                cascadeRefreshObjRefsSet, cascadeRefreshObjRelationsSet);
                    }
                }
            }
        }

        protected void BatchPendingRelations(IObjRefContainer entity, RelationMember member, IObjRef[] relationsOfMember, IISet<IObjRef> cascadeRefreshObjRefsSet,
            ISet<IObjRelation> cascadeRefreshObjRelationsSet)
        {
            if (relationsOfMember == null)
            {
                IObjRelation objRelation = ValueHolderContainerTemplate.GetSelf(entity, member.Name);
                cascadeRefreshObjRelationsSet.Add(objRelation);
            }
            else
            {
                cascadeRefreshObjRefsSet.AddAll(relationsOfMember);
            }
        }

        protected virtual void ChangeFirstLevelCaches(IRootCache rootCache, ISet<Type> occuringTypes, ISet<Type> directRelatingTypes, IList<CacheChangeItem> cacheChangeItems,
            ISet<IObjRef> intermediateDeletes)
        {
            Lock rootCacheReadLock = rootCache.ReadLock;
            bool oldCacheModificationValue = CacheModification.Active;
            if (!oldCacheModificationValue)
            {
                CacheModification.Active = true;
            }
            // RootCache readlock must be acquired before individual writelock to the child caches due to deadlock reasons
            rootCacheReadLock.Lock();
            try
            {
                for (int a = cacheChangeItems.Count; a-- > 0; )
                {
                    CacheChangeItem cci = cacheChangeItems[a];
                    ChildCache childCache = (ChildCache)cci.Cache;

                    IList<IObjRef> deletedObjRefs = cci.DeletedObjRefs;
                    IList<Object> objectsToUpdate = cci.UpdatedObjects;

                    IRootCache parent = ((IRootCache)childCache.Parent).CurrentRootCache;

                    Lock writeLock = childCache.WriteLock;
                    writeLock.Lock();
                    try
                    {
                        if (deletedObjRefs != null && deletedObjRefs.Count > 0)
                        {
                            childCache.Remove(deletedObjRefs);
                        }
                        foreach (IObjRef intermediateDeleteObjRef in intermediateDeletes)
                        {
                            childCache.Remove(intermediateDeleteObjRef);
                        }
                        if (objectsToUpdate != null && objectsToUpdate.Count > 0)
                        {
                            foreach (Object objectInCache in objectsToUpdate)
                            {
                                // Check if the objects still have their id. They may have lost them concurrently because this
                                // method here may be called from another thread (e.g. UI thread)
                                IEntityMetaData metaData = ((IEntityMetaDataHolder)objectInCache).Get__EntityMetaData();
                                Object id = metaData.IdMember.GetValue(objectInCache, false);
                                if (id == null)
                                {
                                    continue;
                                }
                                if (!parent.ApplyValues(objectInCache, childCache))
                                {
                                    if (Log.WarnEnabled)
                                    {
                                        Log.Warn("No entry for object '" + objectInCache + "' found in second level cache");
                                    }
                                }
                            }
                        }
                    }
                    finally
                    {
                        writeLock.Unlock();
                    }
                }
            }
            finally
            {
                rootCacheReadLock.Unlock();
                if (!oldCacheModificationValue)
                {
                    CacheModification.Active = oldCacheModificationValue;
                }
            }
        }
    }
}
