using De.Osthus.Ambeth.Cache.Model;
using De.Osthus.Ambeth.Cache.Transfer;
using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Datachange.Model;
using De.Osthus.Ambeth.Datachange.Transfer;
using De.Osthus.Ambeth.Event;
using De.Osthus.Ambeth.Ioc.Annotation;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Merge;
using De.Osthus.Ambeth.Merge.Model;
using De.Osthus.Ambeth.Merge.Transfer;
using De.Osthus.Ambeth.Metadata;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Mixin;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Typeinfo;
using De.Osthus.Ambeth.Util;
using System;
using System.Collections;
using System.Collections.Generic;
using De.Osthus.Ambeth.Cache.Rootcachevalue;
using De.Osthus.Ambeth.Privilege;
using De.Osthus.Ambeth.Security;
using De.Osthus.Ambeth.Privilege.Model;

namespace De.Osthus.Ambeth.Cache
{
    public class CacheDataChangeListener : IEventListener, IEventTargetEventListener
    {
        protected static readonly CacheDirective cacheValueResultAndReturnMissesSet = CacheDirective.CacheValueResult | CacheDirective.ReturnMisses;

        protected static readonly CacheDirective failInCacheHierarchyAndCacheValueResultAndReturnMissesSet = CacheDirective.FailInCacheHierarchy |
			CacheDirective.CacheValueResult | CacheDirective.ReturnMisses;

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

        [Autowired(Optional = true)]
        public IPrivilegeProvider PrivilegeProvider { protected get; set; }

        [Autowired]
        public ISecondLevelCacheManager SecondLevelCacheManager { protected get; set; }

        [Autowired(Optional = true)]
        public ISecurityActivation SecurityActivation { protected get; set; }

        [Autowired]
        public ValueHolderContainerMixin ValueHolderContainerMixin { protected get; set; }

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
            CacheDependencyNode rootNode = BuildCacheDependency();

		    if (pausedEventTargets != null && pausedEventTargets.Count > 0)
		    {
			    IdentityHashSet<Object> collisionSet = BuildCollisionSet(rootNode);
			    if (collisionSet.ContainsAny(pausedEventTargets))
                {
                    // Without the current rootcache we can not handle the event now. We have to block till the rootCache and all childCaches get valid
                    EventDispatcher.WaitEventToResume(collisionSet, -1, delegate(IProcessResumeItem processResumeItem)
                    {
                        DataChangedIntern(dataChange, pausedEventTargets, processResumeItem, rootNode);
                    }, null);
                    return;
                }
            }
            DataChangedIntern(dataChange, pausedEventTargets, null, rootNode);
        }

        protected IdentityHashSet<Object> BuildCollisionSet(CacheDependencyNode node)
	    {
		    IdentityHashSet<Object> collisionSet = new IdentityHashSet<Object>();
		    BuildCollisionSetIntern(node, collisionSet);
		    return collisionSet;
	    }

	    protected void BuildCollisionSetIntern(CacheDependencyNode node, IdentityHashSet<Object> collisionSet)
	    {
		    collisionSet.Add(node.rootCache);
		    List<CacheDependencyNode> childNodes = node.childNodes;
		    for (int a = childNodes.Count; a-- > 0;)
		    {
			    BuildCollisionSetIntern(childNodes[a], collisionSet);
		    }
		    collisionSet.AddAll(node.directChildCaches);
	    }

	    protected void CleanupSecondLevelCaches(CacheDependencyNode node, IList<IObjRef> deletesList, IList<IDataChangeEntry> updates,
			    CHashSet<Type> occuringTypes)
	    {
		    List<IObjRef> objRefsRemovePriorVersions = new List<IObjRef>(updates.Count);
		    for (int a = updates.Count; a-- > 0;)
		    {
			    IDataChangeEntry updateEntry = updates[a];
			    Type entityType = updateEntry.EntityType;
			    occuringTypes.Add(entityType);
			    objRefsRemovePriorVersions.Add(new ObjRef(entityType, updateEntry.IdNameIndex, updateEntry.Id, updateEntry.Version));
		    }
		    CleanupSecondLevelCachesIntern(node, deletesList, objRefsRemovePriorVersions);
	    }

	    protected void CleanupSecondLevelCachesIntern(CacheDependencyNode node, IList<IObjRef> deletesList, IList<IObjRef> objRefsRemovePriorVersions)
	    {
		    IRootCache rootCache = node.rootCache;
		    Lock writeLock = rootCache.WriteLock;
		    writeLock.Lock();
		    try
		    {
			    rootCache.Remove(deletesList);
			    rootCache.RemovePriorVersions(objRefsRemovePriorVersions);
		    }
		    finally
		    {
			    writeLock.Unlock();
		    }
		    List<CacheDependencyNode> childNodes = node.childNodes;
		    for (int a = childNodes.Count; a-- > 0;)
		    {
			    CleanupSecondLevelCachesIntern(childNodes[a], deletesList, objRefsRemovePriorVersions);
		    }
	    }

	    protected void DataChangedIntern(IDataChange dataChange, IList<Object> pausedEventTargets, IProcessResumeItem processResumeItem, CacheDependencyNode rootNode)
	    {
		    try
		    {
			    bool isLocalSource = dataChange.IsLocalSource;
			    IList<IDataChangeEntry> deletes = dataChange.Deletes;
			    IList<IDataChangeEntry> updates = dataChange.Updates;
			    IList<IDataChangeEntry> inserts = dataChange.Inserts;

			    CHashSet<Type> occuringTypes = new CHashSet<Type>();
			    CHashSet<IObjRef> deletesSet = new CHashSet<IObjRef>();
			    CHashSet<Type> directRelatingTypes = new CHashSet<Type>();
			    bool acquirementSuccessful = rootNode.rootCache.AcquireHardRefTLIfNotAlready();
			    try
			    {
				    for (int a = deletes.Count; a-- > 0;)
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
				    bool cleanupSecondLevelCaches = false;
				    if (pausedEventTargets != null && (deletes.Count > 0 || updates.Count > 0) && !isLocalSource)
				    {
					    cleanupSecondLevelCaches = true;
				    }
				    else if (updates.Count > 0)
				    {
					    for (int a = updates.Count; a-- > 0;)
					    {
						    IDataChangeEntry updateEntry = updates[a];
						    Type entityType = updateEntry.EntityType;
						    occuringTypes.Add(entityType);
					    }
				    }
				    for (int a = inserts.Count; a-- > 0;)
				    {
					    IDataChangeEntry insertEntry = inserts[a];
					    Type entityType = insertEntry.EntityType;
					    occuringTypes.Add(entityType);
				    }
				    EnsureMetaDataIsLoaded(occuringTypes, directRelatingTypes);

				    if (cleanupSecondLevelCaches)
				    {
					    CleanupSecondLevelCaches(rootNode, deletesSet.ToList(), updates, occuringTypes);
				    }

				    BuildCacheChangeItems(rootNode, dataChange);

				    rootNode.AggregateAllCascadedObjRefs();

				    ISet<IObjRef> intermediateDeletes = rootNode.lookForIntermediateDeletes();

				    ChangeSecondLevelCache(rootNode);

				    if (rootNode.IsPendingChangeOnAnyChildCache())
				    {
					    GuiThreadHelper.InvokeInGuiAndWait(delegate()
						    {
							    bool oldFailEarlyModeActive = AbstractCache.FailInCacheHierarchyModeActive;
							    AbstractCache.FailInCacheHierarchyModeActive = true;
							    try
							    {
								    ChangeFirstLevelCaches(rootNode, intermediateDeletes);
							    }
							    finally
							    {
								    AbstractCache.FailInCacheHierarchyModeActive = oldFailEarlyModeActive;
							    }
						    });
				    }
			    }
			    finally
			    {
				    rootNode.rootCache.ClearHardRefs(acquirementSuccessful);
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

	    protected void ChangeSecondLevelCache(CacheDependencyNode node)
	    {
		    ChangeSecondLevelCacheIntern(node, cacheValueResultAndReturnMissesSet);
	    }

	    protected void ChangeSecondLevelCacheIntern(CacheDependencyNode node, CacheDirective cacheDirective)
	    {
            IRootCache rootCache = node.rootCache;
		    HashMap<IObjRef, CacheValueAndPrivilege> objRefToLoadContainerDict = node.objRefToCacheValueMap;
            CHashSet<IObjRef> objRefsToLoad = new CHashSet<IObjRef>(node.objRefsToLoad);

		    if (node.cacheChangeItems != null)
		    {
			    foreach (CacheChangeItem cci in node.cacheChangeItems)
			    {
				    if (cci == null)
				    {
					    continue;
				    }
				    objRefsToLoad.AddAll(cci.UpdatedObjRefs);
			    }
		    }
		    IList<IObjRef> objRefs = objRefsToLoad.ToList();
		    IList<Object> refreshResult = rootCache.GetObjects(objRefs, cacheDirective);

		    IList<IPrivilege> privileges = null;
            if (SecurityActivation != null && PrivilegeProvider != null && SecurityActivation.FilterActivated)
		    {
			    privileges = PrivilegeProvider.GetPrivilegesByObjRef(objRefs);
		    }
		    for (int a = refreshResult.Count; a-- > 0;)
		    {
                RootCacheValue cacheValue = (RootCacheValue)refreshResult[a];
                if (cacheValue == null)
                {
                    continue;
                }
                objRefToLoadContainerDict.Put(objRefs[a], new CacheValueAndPrivilege(cacheValue, privileges != null ? privileges[a] : null));
		    }
		    CheckCascadeRefreshNeeded(node);

		    CHashSet<IObjRef> cascadeRefreshObjRefsSet = node.cascadeRefreshObjRefsSet;
		    CHashSet<IObjRelation> cascadeRefreshObjRelationsSet = node.cascadeRefreshObjRelationsSet;
		    if (cascadeRefreshObjRelationsSet.Count > 0)
		    {
			    IList<IObjRelationResult> relationsResult = rootCache.GetObjRelations(cascadeRefreshObjRelationsSet.ToList(), cacheDirective);
			    for (int a = relationsResult.Count; a-- > 0;)
			    {
				    IObjRelationResult relationResult = relationsResult[a];
				    cascadeRefreshObjRefsSet.AddAll(relationResult.Relations);
			    }
			    // apply gathered information of unknown relations to the rootCache
			    rootCache.Put(relationsResult);
		    }
		    if (cascadeRefreshObjRefsSet.Count > 0)
		    {
			    IList<IObjRef> cascadeRefreshObjRefsSetList = cascadeRefreshObjRefsSet.ToList();
			    refreshResult = rootCache.GetObjects(cascadeRefreshObjRefsSetList, cacheDirective);
		    }
		    List<CacheDependencyNode> childNodes = node.childNodes;
		    for (int a = childNodes.Count; a-- > 0;)
		    {
			    ChangeSecondLevelCacheIntern(childNodes[a], failInCacheHierarchyAndCacheValueResultAndReturnMissesSet);
		    }
	    }

	    protected CacheDependencyNode BuildCacheDependency()
	    {
		    IRootCache privilegedSecondLevelCache = SecondLevelCacheManager.SelectPrivilegedSecondLevelCache(true);
		    IRootCache nonPrivilegedSecondLevelCache = SecondLevelCacheManager.SelectNonPrivilegedSecondLevelCache(false);
		    IList<IWritableCache> selectedFirstLevelCaches = FirstLevelCacheManager.SelectFirstLevelCaches();

		    IdentityHashMap<IRootCache, CacheDependencyNode> secondLevelCacheToNodeMap = new IdentityHashMap<IRootCache, CacheDependencyNode>();
		    if (privilegedSecondLevelCache != null)
		    {
			    CacheDependencyNodeFactory.AddRootCache(privilegedSecondLevelCache.CurrentRootCache, secondLevelCacheToNodeMap);
		    }
		    if (nonPrivilegedSecondLevelCache != null)
		    {
			    CacheDependencyNodeFactory.AddRootCache(nonPrivilegedSecondLevelCache.CurrentRootCache, secondLevelCacheToNodeMap);
		    }
		    for (int a = selectedFirstLevelCaches.Count; a-- > 0;)
		    {
			    ChildCache childCache = (ChildCache) selectedFirstLevelCaches[a];

			    IRootCache parent = ((IRootCache) childCache.Parent).CurrentRootCache;

			    CacheDependencyNode node = CacheDependencyNodeFactory.AddRootCache(parent, secondLevelCacheToNodeMap);
			    node.directChildCaches.Add(childCache);
		    }
		    return CacheDependencyNodeFactory.BuildRootNode(secondLevelCacheToNodeMap);
	    }

	    protected void BuildCacheChangeItems(CacheDependencyNode rootNode, IDataChange dataChange)
	    {
		    List<IDataChangeEntry> insertsAndUpdates = new List<IDataChangeEntry>();
		    IList<IDataChangeEntry> deletes = dataChange.Deletes;

		    insertsAndUpdates.AddRange(dataChange.Updates);
            insertsAndUpdates.AddRange(dataChange.Inserts);
		    List<IObjRef> changesToSearchInCache = new List<IObjRef>(insertsAndUpdates.Count);
		    List<IObjRef> changesWithVersion = new List<IObjRef>(insertsAndUpdates.Count);
		    List<IObjRef> deletesToSearchInCache = new List<IObjRef>(deletes.Count);
		    for (int a = deletes.Count; a-- > 0;)
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
		    for (int a = insertsAndUpdates.Count; a-- > 0;)
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
		    BuildCacheChangeItems(rootNode, deletesToSearchInCache, changesToSearchInCache, changesWithVersion);
	    }

        protected void BuildCacheChangeItems(CacheDependencyNode node, List<IObjRef> deletesToSearchInCache, List<IObjRef> changesToSearchInCache,
			    List<IObjRef> changesWithVersion)
	    {
            List<ChildCache> directChildCaches = node.directChildCaches;
		    for (int flcIndex = directChildCaches.Count; flcIndex-- > 0;)
		    {
			    ChildCache childCache = directChildCaches[flcIndex];
                
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
                        // Attach version to ORI. We can not do this before because then we would have had a
                        // cache miss in the childCache above. We need the version now because our second level cache
                        // has to refresh its entries
                        IObjRef objRefWithVersion = changesWithVersion[a];

                        node.hardRefObjRefsToLoad.Add(objRefWithVersion);

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

                        node.objRefsToLoad.Add(objRefWithVersion);
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
                node.PushPendingChangeOnAnyChildCache(flcIndex, cci);
            }
            List<CacheDependencyNode> childNodes = node.childNodes;
            for (int a = childNodes.Count; a-- > 0; )
            {
                BuildCacheChangeItems(childNodes[a], deletesToSearchInCache, changesToSearchInCache, changesWithVersion);
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

        protected void CheckCascadeRefreshNeeded(CacheDependencyNode node)
	    {
		    CacheChangeItem[] cacheChangeItems = node.cacheChangeItems;
		    if (cacheChangeItems == null)
		    {
			    return;
		    }
            HashMap<IObjRef, CacheValueAndPrivilege> objRefToCacheValueMap = node.objRefToCacheValueMap;
		    for (int c = cacheChangeItems.Length; c-- > 0;)
		    {
			    CacheChangeItem cci = cacheChangeItems[c];
			    if (cci == null)
			    {
				    continue;
			    }
                IList<IObjRef> objectRefsToUpdate = cci.UpdatedObjRefs;
                IList<Object> objectsToUpdate = cci.UpdatedObjects;

                for (int a = objectRefsToUpdate.Count; a-- > 0; )
                {
                    IObjRef objRefToUpdate = objectRefsToUpdate[a];
                    Object objectToUpdate = objectsToUpdate[a];
                    CacheValueAndPrivilege cacheValueAndPrivilege = objRefToCacheValueMap.Get(objRefToUpdate);
                    if (cacheValueAndPrivilege == null)
                    {
                        // Current value in childCache is not in our interest here
                        continue;
                    }
                    IEntityMetaData metaData = ((IEntityMetaDataHolder)objectToUpdate).Get__EntityMetaData();
                    RelationMember[] relationMembers = metaData.RelationMembers;
                    if (relationMembers.Length == 0)
                    {
                        continue;
                    }
                    RootCacheValue cacheValue = cacheValueAndPrivilege.cacheValue;
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
                        // may have been updated, too
                        BatchPendingRelations(cacheValue, relationMembers[relationIndex], cacheValue.GetRelation(relationIndex), node);
                    }
                }
            }
        }

        protected void BatchPendingRelations(RootCacheValue cacheValue, RelationMember member, IObjRef[] relationsOfMember, CacheDependencyNode node)
	    {
		    if (relationsOfMember == null)
		    {
                IObjRelation objRelation = ValueHolderContainerMixin.GetSelf(cacheValue, member.Name);
			    node.cascadeRefreshObjRelationsSet.Add(objRelation);
                IObjRef[] objRefs = objRelation.ObjRefs;
                for (int a = objRefs.Length; a-- > 0; )
                {
                    objRefs[a].Version = null;
                }
                ((ObjRelation)objRelation).Version = null;
		    }
		    else
		    {
			    node.cascadeRefreshObjRefsSet.AddAll(relationsOfMember);
		    }
	    }

	    protected void ChangeFirstLevelCaches(CacheDependencyNode node, ISet<IObjRef> intermediateDeletes)
	    {
		    List<IDataChangeEntry> deletes = new List<IDataChangeEntry>();
		    ICacheModification cacheModification = this.CacheModification;

		    bool oldCacheModificationValue = cacheModification.Active;
		    if (!oldCacheModificationValue)
		    {
			    cacheModification.Active = true;
		    }
		    try
		    {
			    ChangeFirstLevelCachesIntern(node, intermediateDeletes);
		    }
		    finally
		    {
			    if (!oldCacheModificationValue)
			    {
				    cacheModification.Active = oldCacheModificationValue;
			    }
		    }
		    if (deletes.Count > 0)
		    {
			    IDataChange dce = DataChangeEvent.Create(0, 0, deletes.Count);
                ListUtil.AddAll(dce.Deletes, dce.Deletes.Count, deletes);
			    GuiThreadHelper.InvokeOutOfGui(delegate()
				    {
					    EventDispatcher.DispatchEvent(dce);
				    });
		    }
	    }

        protected void ChangeFirstLevelCachesIntern(CacheDependencyNode node, ISet<IObjRef> intermediateDeletes)
	    {
			List<CacheDependencyNode> childNodes = node.childNodes;
            for (int a = childNodes.Count; a-- > 0; )
            {
                ChangeFirstLevelCachesIntern(childNodes[a], intermediateDeletes);
            }
		    CacheChangeItem[] cacheChangeItems = node.cacheChangeItems;
		    if (cacheChangeItems == null)
		    {
			    return;
		    }
		    IRootCache parentCache = node.rootCache;
		    // RootCache readlock must be acquired before individual writelock to the child caches due to deadlock reasons
		    Lock parentCacheReadLock = parentCache.ReadLock;

		    parentCacheReadLock.Lock();
		    try
		    {
                HashMap<IObjRef, CacheValueAndPrivilege> objRefToCacheValueMap = node.objRefToCacheValueMap;

			    for (int a = cacheChangeItems.Length; a-- > 0;)
			    {
				    CacheChangeItem cci = cacheChangeItems[a];
				    if (cci == null)
				    {
					    continue;
				    }
				    ChildCache childCache = node.directChildCaches[a];

                    IList<IObjRef> deletedObjRefs = cci.DeletedObjRefs;
                    IList<Object> objectsToUpdate = cci.UpdatedObjects;
                    IList<IObjRef> objRefsToUpdate = cci.UpdatedObjRefs;

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
							List<IObjRef> objRefsToForget = null;
                            for (int b = objectsToUpdate.Count; b-- > 0; )
                            {
                                Object objectInCache = objectsToUpdate[b];
                                IObjRef objRefInCache = objRefsToUpdate[b];
                                // Check if the objects still have their id. They may have lost them concurrently because this
                                // method here may be called from another thread (e.g. UI thread)
                                IEntityMetaData metaData = ((IEntityMetaDataHolder)objectInCache).Get__EntityMetaData();
                                Object id = metaData.IdMember.GetValue(objectInCache, false);
                                if (id == null)
                                {
                                    continue;
                                }
                                CacheValueAndPrivilege cacheValueP = objRefToCacheValueMap.Get(objRefInCache);
								if (cacheValueP == null)
								{
									if (objRefsToForget == null)
									{
										objRefsToForget = new List<IObjRef>();
									}
									objRefsToForget.Add(objRefInCache);

									foreach (PrimitiveMember member in metaData.PrimitiveMembers)
									{
										member.SetValue(objectInCache, null);
									}
									RelationMember[] relationMembers = metaData.RelationMembers;
									for (int relationIndex = relationMembers.Length; relationIndex-- > 0;)
									{
										((IValueHolderContainer) objectInCache).Set__Uninitialized(relationIndex, null);
									}
									continue;
								}
                                if (!parentCache.ApplyValues(objectInCache, childCache, cacheValueP.privilege))
                                {
                                    if (Log.WarnEnabled)
                                    {
                                        Log.Warn("No entry for object '" + objectInCache + "' found in second level cache");
                                    }
                                }
                            }
							if (objRefsToForget != null)
							{
								childCache.Remove(objRefsToForget);
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
                parentCacheReadLock.Unlock();
            }
        }
    }
}
