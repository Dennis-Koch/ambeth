package de.osthus.ambeth.cache;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import de.osthus.ambeth.cache.AbstractCache;
import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.ChildCache;
import de.osthus.ambeth.cache.ICacheModification;
import de.osthus.ambeth.cache.IFirstLevelCacheManager;
import de.osthus.ambeth.cache.IRootCache;
import de.osthus.ambeth.cache.ISecondLevelCacheManager;
import de.osthus.ambeth.cache.IWritableCache;
import de.osthus.ambeth.cache.ValueHolderState;
import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.datachange.model.DirectDataChangeEntry;
import de.osthus.ambeth.datachange.model.IDataChange;
import de.osthus.ambeth.datachange.model.IDataChangeEntry;
import de.osthus.ambeth.datachange.transfer.DataChangeEvent;
import de.osthus.ambeth.event.IEventDispatcher;
import de.osthus.ambeth.event.IEventListener;
import de.osthus.ambeth.event.IEventTargetEventListener;
import de.osthus.ambeth.event.IProcessResumeItem;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.metadata.RelationMember;
import de.osthus.ambeth.model.IDataObject;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.proxy.IEntityMetaDataHolder;
import de.osthus.ambeth.proxy.IObjRefContainer;
import de.osthus.ambeth.template.ValueHolderContainerTemplate;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;
import de.osthus.ambeth.threading.IGuiThreadHelper;
import de.osthus.ambeth.util.Lock;

public class CacheDataChangeListener implements IEventListener, IEventTargetEventListener
{
	@LogInstance
	private ILogger log;

	protected static final Set<CacheDirective> cacheValueResultAndReturnMissesSet = EnumSet.of(CacheDirective.CacheValueResult, CacheDirective.ReturnMisses);

	protected static final Set<CacheDirective> failInCacheHierarchyAndCacheValueResultAndReturnMissesSet = EnumSet.of(CacheDirective.FailInCacheHierarchy,
			CacheDirective.CacheValueResult, CacheDirective.ReturnMisses);

	protected static final Set<CacheDirective> failInCacheHierarchyAndLoadContainerResultSet = EnumSet.of(CacheDirective.FailInCacheHierarchy,
			CacheDirective.LoadContainerResult);

	@Autowired
	protected ICacheModification cacheModification;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IEventDispatcher eventDispatcher;

	@Autowired
	protected IFirstLevelCacheManager firstLevelCacheManager;

	@Autowired
	protected IGuiThreadHelper guiThreadHelper;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected ISecondLevelCacheManager secondLevelCacheManager;

	@Autowired
	protected ValueHolderContainerTemplate valueHolderContainerTemplate;

	@Override
	public void handleEvent(Object eventObject, long dispatchTime, long sequenceId)
	{
		dataChanged((IDataChange) eventObject, null, null, dispatchTime, sequenceId);
	}

	@Override
	public void handleEvent(Object eventObject, Object resumedEventTarget, List<Object> pausedEventTargets, long dispatchTime, long sequenceId)
	{
		dataChanged((IDataChange) eventObject, resumedEventTarget, pausedEventTargets, dispatchTime, sequenceId);
	}

	protected void dataChanged(final IDataChange dataChange, Object resumedEventTarget, final List<Object> pausedEventTargets, long dispatchTime,
			long sequenceId)
	{
		final CacheDependencyNode rootNode = buildCacheDependency();

		if (pausedEventTargets != null && pausedEventTargets.size() > 0)
		{
			IdentityHashSet<Object> collisionSet = buildCollisionSet(rootNode);
			if (collisionSet.containsAny(pausedEventTargets))
			{
				// Without the current rootcache we can not handle the event now. We have to block till the rootCache and all childCaches get valid
				eventDispatcher.waitEventToResume(collisionSet, -1, new IBackgroundWorkerParamDelegate<IProcessResumeItem>()
				{
					@Override
					public void invoke(IProcessResumeItem processResumeItem) throws Throwable
					{
						dataChangedIntern(dataChange, pausedEventTargets, processResumeItem, rootNode);
					}
				}, null);
				return;
			}
		}
		dataChangedIntern(dataChange, pausedEventTargets, null, rootNode);
	}

	protected IdentityHashSet<Object> buildCollisionSet(CacheDependencyNode node)
	{
		IdentityHashSet<Object> collisionSet = new IdentityHashSet<Object>();
		buildCollisionSetIntern(node, collisionSet);
		return collisionSet;
	}

	protected void buildCollisionSetIntern(CacheDependencyNode node, IdentityHashSet<Object> collisionSet)
	{
		collisionSet.add(node.rootCache);
		ArrayList<CacheDependencyNode> childNodes = node.childNodes;
		for (int a = childNodes.size(); a-- > 0;)
		{
			buildCollisionSetIntern(childNodes.get(a), collisionSet);
		}
		collisionSet.addAll(node.directChildCaches);
	}

	protected void cleanupSecondLevelCaches(CacheDependencyNode node, IList<IObjRef> deletesList, List<IDataChangeEntry> updates,
			HashSet<Class<?>> occuringTypes)
	{
		ArrayList<IObjRef> objRefsRemovePriorVersions = new ArrayList<IObjRef>(updates.size());
		for (int a = updates.size(); a-- > 0;)
		{
			IDataChangeEntry updateEntry = updates.get(a);
			Class<?> entityType = updateEntry.getEntityType();
			occuringTypes.add(entityType);
			objRefsRemovePriorVersions.add(new ObjRef(entityType, updateEntry.getIdNameIndex(), updateEntry.getId(), updateEntry.getVersion()));
		}
		cleanupSecondLevelCachesIntern(node, deletesList, objRefsRemovePriorVersions);
	}

	protected void cleanupSecondLevelCachesIntern(CacheDependencyNode node, IList<IObjRef> deletesList, ArrayList<IObjRef> objRefsRemovePriorVersions)
	{
		IRootCache rootCache = node.rootCache;
		Lock writeLock = rootCache.getWriteLock();
		writeLock.lock();
		try
		{
			rootCache.remove(deletesList);
			rootCache.removePriorVersions(objRefsRemovePriorVersions);
		}
		finally
		{
			writeLock.unlock();
		}
		ArrayList<CacheDependencyNode> childNodes = node.childNodes;
		for (int a = childNodes.size(); a-- > 0;)
		{
			cleanupSecondLevelCachesIntern(childNodes.get(a), deletesList, objRefsRemovePriorVersions);
		}
	}

	protected void dataChangedIntern(IDataChange dataChange, List<Object> pausedEventTargets, IProcessResumeItem processResumeItem,
			final CacheDependencyNode rootNode)
	{
		try
		{
			boolean isLocalSource = dataChange.isLocalSource();
			List<IDataChangeEntry> deletes = dataChange.getDeletes();
			List<IDataChangeEntry> updates = dataChange.getUpdates();
			List<IDataChangeEntry> inserts = dataChange.getInserts();

			final HashSet<Class<?>> occuringTypes = new HashSet<Class<?>>();
			HashSet<IObjRef> deletesSet = new HashSet<IObjRef>();
			final HashSet<Class<?>> directRelatingTypes = new HashSet<Class<?>>();
			boolean acquirementSuccessful = rootNode.rootCache.acquireHardRefTLIfNotAlready();
			try
			{
				for (int a = deletes.size(); a-- > 0;)
				{
					IDataChangeEntry deleteEntry = deletes.get(a);
					Class<?> entityType = deleteEntry.getEntityType();
					occuringTypes.add(entityType);
					if (deleteEntry instanceof DirectDataChangeEntry)
					{
						// Ignore delete entries of unpersisted objects here
						continue;
					}
					ObjRef tempORI = new ObjRef(entityType, deleteEntry.getIdNameIndex(), deleteEntry.getId(), deleteEntry.getVersion());
					deletesSet.add(tempORI);
				}
				// Remove items from the cache only if they are really deleted/updates by a remote event
				// And not 'simulated' by a local source
				boolean cleanupSecondLevelCaches = false;
				if (pausedEventTargets != null && (deletes.size() > 0 || updates.size() > 0) && !isLocalSource)
				{
					cleanupSecondLevelCaches = true;
				}
				else if (updates.size() > 0)
				{
					for (int a = updates.size(); a-- > 0;)
					{
						IDataChangeEntry updateEntry = updates.get(a);
						Class<?> entityType = updateEntry.getEntityType();
						occuringTypes.add(entityType);
					}
				}
				for (int a = inserts.size(); a-- > 0;)
				{
					IDataChangeEntry insertEntry = inserts.get(a);
					Class<?> entityType = insertEntry.getEntityType();
					occuringTypes.add(entityType);
				}
				ensureMetaDataIsLoaded(occuringTypes, directRelatingTypes);

				if (cleanupSecondLevelCaches)
				{
					cleanupSecondLevelCaches(rootNode, deletesSet.toList(), updates, occuringTypes);
				}

				buildCacheChangeItems(rootNode, dataChange);

				rootNode.aggregateAllCascadedObjRefs();

				final ISet<IObjRef> intermediateDeletes = rootNode.lookForIntermediateDeletes();

				changeSecondLevelCache(rootNode);

				if (rootNode.isPendingChangeOnAnyChildCache())
				{
					guiThreadHelper.invokeInGuiAndWait(new IBackgroundWorkerDelegate()
					{
						@Override
						public void invoke() throws Throwable
						{
							boolean oldFailEarlyModeActive = AbstractCache.isFailInCacheHierarchyModeActive();
							AbstractCache.setFailInCacheHierarchyModeActive(true);
							try
							{
								changeFirstLevelCaches(rootNode, intermediateDeletes);
							}
							finally
							{
								AbstractCache.setFailInCacheHierarchyModeActive(oldFailEarlyModeActive);
							}
						}
					});
				}
			}
			finally
			{
				rootNode.rootCache.clearHardRefs(acquirementSuccessful);
			}
		}
		finally
		{
			if (processResumeItem != null)
			{
				processResumeItem.resumeProcessingFinished();
			}
		}
	}

	protected void changeSecondLevelCache(CacheDependencyNode node)
	{
		changeSecondLevelCacheIntern(node, CacheDirective.loadContainerResult());
	}

	protected void changeSecondLevelCacheIntern(CacheDependencyNode node, Set<CacheDirective> cacheDirective)
	{
		IRootCache rootCache = node.rootCache;
		HashMap<IObjRef, ILoadContainer> objRefToLoadContainerDict = node.objRefToLoadContainerMap;
		IList<Object> refreshResult = rootCache.getObjects(node.objRefsToLoad.toList(), cacheDirective);
		for (int a = refreshResult.size(); a-- > 0;)
		{
			ILoadContainer loadContainer = (ILoadContainer) refreshResult.get(a);
			objRefToLoadContainerDict.put(loadContainer.getReference(), loadContainer);
		}
		checkCascadeRefreshNeeded(node);

		HashSet<IObjRef> cascadeRefreshObjRefsSet = node.cascadeRefreshObjRefsSet;
		HashSet<IObjRelation> cascadeRefreshObjRelationsSet = node.cascadeRefreshObjRelationsSet;
		if (cascadeRefreshObjRelationsSet.size() > 0)
		{
			IList<IObjRelationResult> relationsResult = rootCache.getObjRelations(cascadeRefreshObjRelationsSet.toList(), cacheDirective);
			for (int a = relationsResult.size(); a-- > 0;)
			{
				IObjRelationResult relationResult = relationsResult.get(a);
				cascadeRefreshObjRefsSet.addAll(relationResult.getRelations());
			}
			// apply gathered information of unknown relations to the rootCache
			rootCache.put(relationsResult);
		}
		if (cascadeRefreshObjRefsSet.size() > 0)
		{
			IList<IObjRef> cascadeRefreshObjRefsSetList = cascadeRefreshObjRefsSet.toList();
			refreshResult = rootCache.getObjects(cascadeRefreshObjRefsSetList, cacheDirective);
		}
		ArrayList<CacheDependencyNode> childNodes = node.childNodes;
		for (int a = childNodes.size(); a-- > 0;)
		{
			changeSecondLevelCacheIntern(childNodes.get(a), failInCacheHierarchyAndLoadContainerResultSet);
		}
	}

	protected CacheDependencyNode buildCacheDependency()
	{
		IRootCache privilegedSecondLevelCache = secondLevelCacheManager.selectPrivilegedSecondLevelCache(true);
		IRootCache nonPrivilegedSecondLevelCache = secondLevelCacheManager.selectNonPrivilegedSecondLevelCache(false);
		IList<IWritableCache> selectedFirstLevelCaches = firstLevelCacheManager.selectFirstLevelCaches();

		IdentityHashMap<IRootCache, CacheDependencyNode> secondLevelCacheToNodeMap = new IdentityHashMap<IRootCache, CacheDependencyNode>();
		if (privilegedSecondLevelCache != null)
		{
			CacheDependencyNodeFactory.addRootCache(privilegedSecondLevelCache.getCurrentRootCache(), secondLevelCacheToNodeMap);
		}
		if (nonPrivilegedSecondLevelCache != null)
		{
			CacheDependencyNodeFactory.addRootCache(nonPrivilegedSecondLevelCache.getCurrentRootCache(), secondLevelCacheToNodeMap);
		}
		for (int a = selectedFirstLevelCaches.size(); a-- > 0;)
		{
			ChildCache childCache = (ChildCache) selectedFirstLevelCaches.get(a);

			IRootCache parent = ((IRootCache) childCache.getParent()).getCurrentRootCache();

			CacheDependencyNode node = CacheDependencyNodeFactory.addRootCache(parent, secondLevelCacheToNodeMap);
			node.directChildCaches.add(childCache);
		}
		return CacheDependencyNodeFactory.buildRootNode(secondLevelCacheToNodeMap);
	}

	protected void buildCacheChangeItems(CacheDependencyNode rootNode, IDataChange dataChange)
	{
		ArrayList<IDataChangeEntry> insertsAndUpdates = new ArrayList<IDataChangeEntry>();
		List<IDataChangeEntry> deletes = dataChange.getDeletes();

		insertsAndUpdates.addAll(dataChange.getUpdates());
		insertsAndUpdates.addAll(dataChange.getInserts());
		ArrayList<IObjRef> changesToSearchInCache = new ArrayList<IObjRef>(insertsAndUpdates.size());
		ArrayList<IObjRef> changesWithVersion = new ArrayList<IObjRef>(insertsAndUpdates.size());
		ArrayList<IObjRef> deletesToSearchInCache = new ArrayList<IObjRef>(deletes.size());
		for (int a = deletes.size(); a-- > 0;)
		{
			IDataChangeEntry deleteEntry = deletes.get(a);
			Object id = deleteEntry.getId();
			if (id == null)
			{
				deletesToSearchInCache.add(null);
				continue;
			}
			deletesToSearchInCache.add(new ObjRef(deleteEntry.getEntityType(), deleteEntry.getIdNameIndex(), id, null));
		}
		for (int a = insertsAndUpdates.size(); a-- > 0;)
		{
			IDataChangeEntry updateEntry = insertsAndUpdates.get(a);
			Object id = updateEntry.getId();
			if (id == null)
			{
				changesToSearchInCache.add(null);
				continue;
			}
			changesToSearchInCache.add(new ObjRef(updateEntry.getEntityType(), updateEntry.getIdNameIndex(), id, null));
			changesWithVersion.add(new ObjRef(updateEntry.getEntityType(), updateEntry.getIdNameIndex(), id, updateEntry.getVersion()));
		}
		buildCacheChangeItems(rootNode, deletesToSearchInCache, changesToSearchInCache, changesWithVersion);
	}

	@SuppressWarnings("unchecked")
	protected void buildCacheChangeItems(CacheDependencyNode node, ArrayList<IObjRef> deletesToSearchInCache, ArrayList<IObjRef> changesToSearchInCache,
			ArrayList<IObjRef> changesWithVersion)
	{
		ArrayList<ChildCache> directChildCaches = node.directChildCaches;
		for (int flcIndex = directChildCaches.size(); flcIndex-- > 0;)
		{
			ChildCache childCache = directChildCaches.get(flcIndex);

			ArrayList<IObjRef> objectRefsToDelete = new ArrayList<IObjRef>();
			ArrayList<IObjRef> objectRefsToUpdate = new ArrayList<IObjRef>();
			ArrayList<Object> objectsToUpdate = new ArrayList<Object>();

			Lock readLock = childCache.getReadLock();
			readLock.lock();
			try
			{
				List<Object> deletesInCache = childCache.getObjects(deletesToSearchInCache, CacheDirective.failEarlyAndReturnMisses());
				for (int a = deletesToSearchInCache.size(); a-- > 0;)
				{
					Object result = deletesInCache.get(a);
					if (result == null)
					{
						// not in this cache
						continue;
					}
					objectRefsToDelete.add(deletesToSearchInCache.get(a));
				}
				List<Object> changesInCache = childCache.getObjects(changesToSearchInCache, CacheDirective.failEarlyAndReturnMisses());
				for (int a = changesToSearchInCache.size(); a-- > 0;)
				{
					Object result = changesInCache.get(a);
					if (result == null)
					{
						// not in this cache
						continue;
					}
					// Attach version to ORI. We can not do this before because then we would have had a
					// cache miss in the childCache above. We need the version now because our second level cache
					// has to refresh its entries
					IObjRef objRefWithVersion = changesWithVersion.get(a);

					node.hardRefObjRefsToLoad.add(objRefWithVersion);

					if (result instanceof IDataObject)
					{
						IDataObject dataObject = (IDataObject) result;
						if (dataObject.isToBeUpdated() || dataObject.isToBeDeleted())
						{
							continue;
						}
					}
					if (objRefWithVersion.getVersion() != null)
					{
						IEntityMetaData metaData = ((IEntityMetaDataHolder) result).get__EntityMetaData();
						Object versionInCache = metaData.getVersionMember() != null ? metaData.getVersionMember().getValue(result, false) : null;
						if (versionInCache != null && ((Comparable<Object>) objRefWithVersion.getVersion()).compareTo(versionInCache) <= 0)
						{
							continue;
						}
					}
					objectsToUpdate.add(result);

					node.objRefsToLoad.add(objRefWithVersion);
					objectRefsToUpdate.add(objRefWithVersion);
				}
			}
			finally
			{
				readLock.unlock();
			}
			if (objectRefsToDelete.size() == 0 && objectsToUpdate.size() == 0)
			{
				continue;
			}
			CacheChangeItem cci = new CacheChangeItem();
			cci.cache = childCache;
			cci.deletedObjRefs = objectRefsToDelete;
			cci.updatedObjRefs = objectRefsToUpdate;
			cci.updatedObjects = objectsToUpdate;
			node.pushPendingChangeOnAnyChildCache(flcIndex, cci);
		}
		ArrayList<CacheDependencyNode> childNodes = node.childNodes;
		for (int a = childNodes.size(); a-- > 0;)
		{
			buildCacheChangeItems(childNodes.get(a), deletesToSearchInCache, changesToSearchInCache, changesWithVersion);
		}
	}

	protected void scanForInitializedObjects(Object obj, Set<Object> alreadyScannedObjects, Set<IObjRef> objRefs)
	{
		if (obj == null || !alreadyScannedObjects.add(obj))
		{
			return;
		}
		if (obj instanceof List)
		{
			List<?> list = (List<?>) obj;
			for (int a = list.size(); a-- > 0;)
			{
				Object item = list.get(a);
				scanForInitializedObjects(item, alreadyScannedObjects, objRefs);
			}
			return;
		}
		if (obj instanceof Collection)
		{
			for (Object item : (Collection<?>) obj)
			{
				scanForInitializedObjects(item, alreadyScannedObjects, objRefs);
			}
			return;
		}
		IEntityMetaData metaData = ((IEntityMetaDataHolder) obj).get__EntityMetaData();
		Object id = metaData.getIdMember().getValue(obj, false);
		if (id == null)
		{
			// This may happen if a normally retrieved object gets deleted and therefore has lost its primary id
			// TODO: The object is still contained in the cache, maybe this should be reviewed
			return;
		}
		ObjRef objRef = new ObjRef(metaData.getEntityType(), ObjRef.PRIMARY_KEY_INDEX, id, null);
		objRefs.add(objRef);
		RelationMember[] relationMembers = metaData.getRelationMembers();
		if (relationMembers.length == 0)
		{
			return;
		}
		IObjRefContainer vhc = (IObjRefContainer) obj;
		for (int relationIndex = relationMembers.length; relationIndex-- > 0;)
		{
			if (ValueHolderState.INIT != vhc.get__State(relationIndex))
			{
				continue;
			}
			Object value = relationMembers[relationIndex].getValue(obj, false);
			scanForInitializedObjects(value, alreadyScannedObjects, objRefs);
		}
	}

	protected void ensureMetaDataIsLoaded(ISet<Class<?>> occuringTypes, ISet<Class<?>> directRelatingTypes)
	{
		HashSet<Class<?>> wholeRelatedTypes = new HashSet<Class<?>>(occuringTypes);
		ArrayList<Class<?>> additionalTypes = new ArrayList<Class<?>>();
		{
			// Own code scope
			IList<Class<?>> occuringTypesList = occuringTypes.toList();
			IList<IEntityMetaData> occuringMetaData = entityMetaDataProvider.getMetaData(occuringTypesList);

			for (int a = 0, size = occuringMetaData.size(); a < size; a++)
			{
				IEntityMetaData metaData = occuringMetaData.get(a);
				for (Class<?> type : metaData.getTypesRelatingToThis())
				{
					directRelatingTypes.add(type);
					if (wholeRelatedTypes.add(type))
					{
						// Additional related type in this whole datachange
						additionalTypes.add(type);
					}
				}
			}
		}
		while (additionalTypes.size() > 0)
		{
			IList<IEntityMetaData> additionalMetaData = entityMetaDataProvider.getMetaData(additionalTypes);
			additionalTypes.clear();
			for (IEntityMetaData metaData : additionalMetaData)
			{
				for (Class<?> type : metaData.getTypesRelatingToThis())
				{
					if (wholeRelatedTypes.add(type))
					{
						// Additional related type in this whole datachange
						additionalTypes.add(type);
					}
				}
			}
		}
	}

	protected void checkCascadeRefreshNeeded(CacheDependencyNode node)
	{
		CacheChangeItem[] cacheChangeItems = node.cacheChangeItems;
		if (cacheChangeItems == null)
		{
			return;
		}
		HashMap<IObjRef, ILoadContainer> objRefToLoadContainerMap = node.objRefToLoadContainerMap;
		for (int c = cacheChangeItems.length; c-- > 0;)
		{
			CacheChangeItem cci = cacheChangeItems[c];
			if (cci == null)
			{
				continue;
			}
			IList<IObjRef> objectRefsToUpdate = cci.updatedObjRefs;
			IList<Object> objectsToUpdate = cci.updatedObjects;

			for (int a = objectRefsToUpdate.size(); a-- > 0;)
			{
				IObjRef objRefToUpdate = objectRefsToUpdate.get(a);
				Object objectToUpdate = objectsToUpdate.get(a);
				ILoadContainer loadContainer = objRefToLoadContainerMap.get(objRefToUpdate);
				if (loadContainer == null)
				{
					// Current value in childCache is not in our interest here
					continue;
				}
				IObjRef[][] relations = loadContainer.getRelations();
				IEntityMetaData metaData = ((IEntityMetaDataHolder) objectToUpdate).get__EntityMetaData();
				RelationMember[] relationMembers = metaData.getRelationMembers();
				if (relationMembers.length == 0)
				{
					continue;
				}
				IObjRefContainer vhc = (IObjRefContainer) objectToUpdate;
				for (int relationIndex = relationMembers.length; relationIndex-- > 0;)
				{
					if (ValueHolderState.INIT != vhc.get__State(b))
					{
						continue;
					}
					// the object which has to be updated has initialized relations. So we have to ensure
					// that these relations are in the RootCache at the time the target object will be updated.
					// This is because initialized relations have to remain initialized after update but the relations
					// may have been updated, too
					batchPendingRelations(vhc, relationMembers[relationIndex], relations[relationIndex], node);
				}
			}
		}
	}

	protected void batchPendingRelations(IObjRefContainer entity, RelationMember member, IObjRef[] relationsOfMember, CacheDependencyNode node)
	{
		if (relationsOfMember == null)
		{
			IObjRelation objRelation = valueHolderContainerTemplate.getSelf(entity, member.getName());
			node.cascadeRefreshObjRelationsSet.add(objRelation);
		}
		else
		{
			node.cascadeRefreshObjRefsSet.addAll(relationsOfMember);
		}
	}

	protected void changeFirstLevelCaches(CacheDependencyNode node, ISet<IObjRef> intermediateDeletes)
	{
		ArrayList<IDataChangeEntry> deletes = new ArrayList<IDataChangeEntry>();
		ICacheModification cacheModification = this.cacheModification;

		boolean oldCacheModificationValue = cacheModification.isActive();
		if (!oldCacheModificationValue)
		{
			cacheModification.setActive(true);
		}
		try
		{
			changeFirstLevelCachesIntern(node, intermediateDeletes);
		}
		finally
		{
			if (!oldCacheModificationValue)
			{
				cacheModification.setActive(oldCacheModificationValue);
			}
		}
		if (deletes.size() > 0)
		{
			final IDataChange dce = DataChangeEvent.create(0, 0, deletes.size());
			dce.getDeletes().addAll(deletes);
			guiThreadHelper.invokeOutOfGui(new IBackgroundWorkerDelegate()
			{
				@Override
				public void invoke() throws Throwable
				{
					eventDispatcher.dispatchEvent(dce);
				}
			});
		}
	}

	protected void changeFirstLevelCachesIntern(CacheDependencyNode node, ISet<IObjRef> intermediateDeletes)
	{
		CacheChangeItem[] cacheChangeItems = node.cacheChangeItems;
		if (cacheChangeItems == null)
		{
			return;
		}
		IRootCache parentCache = node.rootCache;
		// RootCache readlock must be acquired before individual writelock to the child caches due to deadlock reasons
		Lock parentCacheReadLock = parentCache.getReadLock();

		parentCacheReadLock.lock();
		try
		{
			for (int a = cacheChangeItems.length; a-- > 0;)
			{
				CacheChangeItem cci = cacheChangeItems[a];
				if (cci == null)
				{
					continue;
				}
				ChildCache childCache = node.directChildCaches.get(a);

				IList<IObjRef> deletedObjRefs = cci.deletedObjRefs;
				IList<Object> objectsToUpdate = cci.updatedObjects;

				de.osthus.ambeth.util.Lock writeLock = childCache.getWriteLock();

				writeLock.lock();
				try
				{
					if (deletedObjRefs != null && deletedObjRefs.size() > 0)
					{
						IList<Object> deletedObjects = childCache.getObjects(cci.deletedObjRefs, CacheDirective.failEarly());
						childCache.remove(cci.deletedObjRefs);
						for (int b = deletedObjects.size(); b-- > 0;)
						{
							Object deletedObject = deletedObjects.get(b);
							IEntityMetaData metaData = ((IEntityMetaDataHolder) deletedObject).get__EntityMetaData();
							metaData.getIdMember().setValue(deletedObject, null);
							if (metaData.getVersionMember() != null)
							{
								metaData.getVersionMember().setValue(deletedObject, null);
							}
						}
					}
					for (IObjRef intermediateDeleteObjRef : intermediateDeletes)
					{
						childCache.remove(intermediateDeleteObjRef);
					}
					if (objectsToUpdate != null && objectsToUpdate.size() > 0)
					{
						for (Object objectInCache : objectsToUpdate)
						{
							// Check if the objects still have their id. They may have lost them concurrently because this
							// method here may be called from another thread (e.g. UI thread)
							IEntityMetaData metaData = ((IEntityMetaDataHolder) objectInCache).get__EntityMetaData();
							Object id = metaData.getIdMember().getValue(objectInCache, false);
							if (id == null)
							{
								continue;
							}
							if (!parentCache.applyValues(objectInCache, childCache))
							{
								if (log.isWarnEnabled())
								{
									log.warn("No entry for object '" + objectInCache + "' found in second level cache");
								}
							}
						}
					}
				}
				finally
				{
					writeLock.unlock();
				}
			}
		}
		finally
		{
			parentCacheReadLock.unlock();
		}
	}
}
