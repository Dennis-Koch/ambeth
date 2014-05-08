package de.osthus.ambeth;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.osthus.ambeth.cache.AbstractCache;
import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.ChildCache;
import de.osthus.ambeth.cache.ICacheIntern;
import de.osthus.ambeth.cache.ICacheModification;
import de.osthus.ambeth.cache.IFirstLevelCacheManager;
import de.osthus.ambeth.cache.IRootCache;
import de.osthus.ambeth.cache.ISecondLevelCacheManager;
import de.osthus.ambeth.cache.IWritableCache;
import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.datachange.CacheChangeItem;
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
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.model.IDataObject;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.template.ValueHolderContainerTemplate;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;
import de.osthus.ambeth.threading.IGuiThreadHelper;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.util.CachePath;
import de.osthus.ambeth.util.Lock;
import de.osthus.ambeth.util.StringBuilderUtil;

public class CacheDataChangeListener implements IEventListener, IEventTargetEventListener
{
	@LogInstance
	private ILogger log;

	protected static final Set<CacheDirective> cacheValueResultAndReturnMissesSet = EnumSet.of(CacheDirective.CacheValueResult, CacheDirective.ReturnMisses);

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
	protected IProxyHelper proxyHelper;

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
		final IRootCache rootCache = secondLevelCacheManager.selectSecondLevelCache();
		final IList<IWritableCache> selectedFirstLevelCaches = firstLevelCacheManager.selectFirstLevelCaches();

		if (pausedEventTargets != null && pausedEventTargets.size() > 0)
		{
			IdentityHashSet<Object> collisionSet = new IdentityHashSet<Object>();
			collisionSet.add(rootCache);
			collisionSet.addAll(selectedFirstLevelCaches);
			collisionSet.retainAll(pausedEventTargets);
			if (collisionSet.size() > 0)
			{
				// Restore ALL necessary items to handle this DCE
				collisionSet.clear();
				collisionSet.add(rootCache);
				collisionSet.addAll(selectedFirstLevelCaches);
				// Without the current rootcache we can not handle the event now. We have to block till the rootCache and all childCaches get valid
				eventDispatcher.waitEventToResume(collisionSet, -1, new IBackgroundWorkerParamDelegate<IProcessResumeItem>()
				{
					@Override
					public void invoke(IProcessResumeItem processResumeItem) throws Throwable
					{
						dataChangedIntern(dataChange, pausedEventTargets, processResumeItem, rootCache, selectedFirstLevelCaches);
					}
				}, null);
				return;
			}
		}
		dataChangedIntern(dataChange, pausedEventTargets, null, rootCache, selectedFirstLevelCaches);
	}

	protected void dataChangedIntern(IDataChange dataChange, List<Object> pausedEventTargets, IProcessResumeItem processResumeItem, final IRootCache rootCache,
			IList<IWritableCache> selectedFirstLevelCaches)
	{
		try
		{
			boolean isLocalSource = dataChange.isLocalSource();
			List<IDataChangeEntry> deletes = dataChange.getDeletes();
			List<IDataChangeEntry> updates = dataChange.getUpdates();
			List<IDataChangeEntry> inserts = dataChange.getInserts();

			final HashSet<Class<?>> occuringTypes = new HashSet<Class<?>>();
			final HashSet<IObjRef> intermediateDeletes = new HashSet<IObjRef>();
			LinkedHashSet<IObjRef> deletesSet = new LinkedHashSet<IObjRef>();
			HashMap<IObjRef, ILoadContainer> objRefToLoadContainerDict = new HashMap<IObjRef, ILoadContainer>();
			final HashSet<Class<?>> directRelatingTypes = new HashSet<Class<?>>();
			final ArrayList<CacheChangeItem> cacheChangeItems = new ArrayList<CacheChangeItem>();
			boolean acquirementSuccessful = rootCache.acquireHardRefTLIfNotAlready();
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
				if (pausedEventTargets != null && (deletes.size() > 0 || updates.size() > 0) && !isLocalSource)
				{
					de.osthus.ambeth.util.Lock rootCacheWriteLock = rootCache.getWriteLock();

					rootCacheWriteLock.lock();
					try
					{
						IList<IObjRef> deletesList = deletesSet.toList();
						rootCache.remove(deletesList);
						ObjRef tempORI = new ObjRef(null, ObjRef.PRIMARY_KEY_INDEX, null, null);
						for (int a = updates.size(); a-- > 0;)
						{
							IDataChangeEntry updateEntry = updates.get(a);
							Class<?> entityType = updateEntry.getEntityType();
							occuringTypes.add(entityType);
							tempORI.setRealType(entityType);
							tempORI.setId(updateEntry.getId());
							tempORI.setIdNameIndex(updateEntry.getIdNameIndex());
							tempORI.setVersion(updateEntry.getVersion());
							rootCache.removePriorVersions(tempORI);
						}
					}
					finally
					{
						rootCacheWriteLock.unlock();
					}
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

				HashSet<IObjRef> orisToLoad = new HashSet<IObjRef>();
				HashSet<IObjRef> hardRefOrisToLoad = new HashSet<IObjRef>();
				IdentityHashSet<Object> hardRefChildren = new IdentityHashSet<Object>();

				selectFirstLevelChanges(selectedFirstLevelCaches, hardRefChildren, directRelatingTypes, hardRefOrisToLoad, dataChange, cacheChangeItems,
						orisToLoad, deletesSet, rootCache);

				IList<IObjRef> hardRefOrisToLoadList = hardRefOrisToLoad.toList();

				// Hold cache values as hard ref to prohibit cache loss due to GC
				IList<Object> hardRefResult = rootCache.getObjects(hardRefOrisToLoadList, cacheValueResultAndReturnMissesSet);
				for (int a = hardRefResult.size(); a-- > 0;)
				{
					Object hardRef = hardRefResult.get(a);
					if (hardRef != null)
					{
						continue;
					}
					// Objects are marked as UPDATED in the DCE, but could not be newly retrieved from the server
					// This occurs if a fast DELETE event on the server happened but has not been processed, yet
					IObjRef hardRefOriToLoad = hardRefOrisToLoadList.get(a);
					intermediateDeletes.add(hardRefOriToLoad);
					orisToLoad.remove(hardRefOriToLoad);
				}
				IList<IObjRef> orisToLoadList = orisToLoad.toList();
				IList<Object> refreshResult = rootCache.getObjects(orisToLoadList, CacheDirective.loadContainerResult());
				for (int a = refreshResult.size(); a-- > 0;)
				{
					ILoadContainer loadContainer = (ILoadContainer) refreshResult.get(a);
					objRefToLoadContainerDict.put(loadContainer.getReference(), loadContainer);
				}
				HashSet<IObjRef> cascadeRefreshObjRefsSet = new HashSet<IObjRef>();
				HashSet<IObjRelation> cascadeRefreshObjRelationsSet = new HashSet<IObjRelation>();
				checkCascadeRefreshNeeded(cacheChangeItems, objRefToLoadContainerDict, cascadeRefreshObjRefsSet, cascadeRefreshObjRelationsSet);
				if (cascadeRefreshObjRelationsSet.size() > 0)
				{
					IList<IObjRelationResult> relationsResult = rootCache.getObjRelations(cascadeRefreshObjRelationsSet.toList(),
							CacheDirective.loadContainerResult());
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
					refreshResult = rootCache.getObjects(cascadeRefreshObjRefsSetList, CacheDirective.loadContainerResult());
				}
				guiThreadHelper.invokeInGuiAndWait(new IBackgroundWorkerDelegate()
				{
					@Override
					public void invoke() throws Throwable
					{
						boolean oldFailEarlyModeActive = AbstractCache.isFailEarlyModeActive();
						AbstractCache.setFailEarlyModeActive(true);
						try
						{
							changeFirstLevelCaches(rootCache, occuringTypes, directRelatingTypes, cacheChangeItems, intermediateDeletes);
						}
						finally
						{
							AbstractCache.setFailEarlyModeActive(oldFailEarlyModeActive);
						}
					}
				});
			}
			finally
			{
				rootCache.clearHardRefs(acquirementSuccessful);
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

	@SuppressWarnings("unchecked")
	protected void selectFirstLevelChanges(List<IWritableCache> firstLevelCaches, Set<Object> hardRefChildren, Set<Class<?>> directRelatingTypes,
			Set<IObjRef> hardRefOrisToLoad, IDataChange dataChange, List<CacheChangeItem> cciList, Set<IObjRef> orisToLoad, Set<IObjRef> deletedSet,
			IRootCache rootCache)
	{
		ObjRef tempORI = new ObjRef(null, ObjRef.PRIMARY_KEY_INDEX, null, null);
		ArrayList<IObjRef> tempList = new ArrayList<IObjRef>(1);

		EnumSet<CacheDirective> failEarlyReturnMisses = EnumSet.of(CacheDirective.FailEarly, CacheDirective.ReturnMisses);

		ArrayList<IDataChangeEntry> insertsAndUpdates = new ArrayList<IDataChangeEntry>();

		for (int flcIndex = firstLevelCaches.size(); flcIndex-- > 0;)
		{
			IWritableCache childCache = firstLevelCaches.get(flcIndex);
			insertsAndUpdates.clear();
			// childCache.getContent(new HandleContentDelegate()
			// {
			//
			// @Override
			// public void invoke(Class<?> entityType, byte idIndex, Object id, Object value)
			// {
			// // This entity will be refreshed with (potential) new information about the objects which
			// // have been specified as changed in the DCE
			// // This is a robustness feature because normally a DCE contains ALL information about object
			// // changes. And if object relations have been changed they are (supposed to) always been changed
			// // for both sides of the relation and therefore imply a change on both entities
			// // We store a hard ref to this object to ensure that the GC does not cut the reference in between
			// hardRefChildren.add(value);
			// if (!directRelatingTypes.contains(entityType))
			// {
			// return;
			// }
			// ObjRef hardRefOri = ObjRef.create(tlObjectCollector, entityType, idIndex, id, null);
			// if (deletedSet.contains(hardRefOri) || !hardRefOrisToLoad.add(hardRefOri))
			// {
			// tlObjectCollector.dispose(hardRefOri);
			// }
			// }
			// });

			ArrayList<IObjRef> objectRefsToDelete = new ArrayList<IObjRef>();
			ArrayList<IObjRef> objectRefsToUpdate = new ArrayList<IObjRef>();
			ArrayList<Object> objectsToUpdate = new ArrayList<Object>();

			Lock readLock = childCache.getReadLock();
			readLock.lock();
			try
			{
				List<IDataChangeEntry> deletes = dataChange.getDeletes();
				for (int a = deletes.size(); a-- > 0;)
				{
					IDataChangeEntry deleteEntry = deletes.get(a);
					Object id = deleteEntry.getId();
					if (id == null)
					{
						continue;
					}
					tempORI.init(deleteEntry.getEntityType(), deleteEntry.getIdNameIndex(), id, null);

					tempList.clear();
					tempList.add(tempORI);
					Object result = childCache.getObjects(tempList, failEarlyReturnMisses).get(0);
					if (result == null)
					{
						continue;
					}
					objectRefsToDelete.add(tempORI);
					tempORI = new ObjRef(null, ObjRef.PRIMARY_KEY_INDEX, null, null);
				}
				insertsAndUpdates.addAll(dataChange.getUpdates());
				insertsAndUpdates.addAll(dataChange.getInserts());
				for (int a = insertsAndUpdates.size(); a-- > 0;)
				{
					IDataChangeEntry updateEntry = insertsAndUpdates.get(a);
					Object id = updateEntry.getId();
					if (id == null)
					{
						continue;
					}
					tempORI.init(updateEntry.getEntityType(), updateEntry.getIdNameIndex(), id, null);

					tempList.clear();
					tempList.add(tempORI);
					Object result = childCache.getObjects(tempList, failEarlyReturnMisses).get(0);
					if (result == null)
					{
						continue;
					}
					Object versionInDCE = updateEntry.getVersion();

					// Attach version to ORI. We can not do this before because then we would have had a
					// cache miss in the childCache above. We need the version now because our second level cache
					// has to refresh its entries
					tempORI.setVersion(versionInDCE);
					hardRefOrisToLoad.add(tempORI);

					if (result instanceof IDataObject)
					{
						IDataObject dataObject = (IDataObject) result;
						if (dataObject.isToBeUpdated() || dataObject.isToBeDeleted())
						{
							continue;
						}
					}
					if (versionInDCE != null)
					{
						IEntityMetaData metaData = entityMetaDataProvider.getMetaData(tempORI.getRealType());
						Object versionInCache = metaData.getVersionMember() != null ? metaData.getVersionMember().getValue(result, false) : null;
						if (versionInCache != null && ((Comparable<Object>) versionInDCE).compareTo(versionInCache) <= 0)
						{
							continue;
						}
					}
					objectsToUpdate.add(result);
					orisToLoad.add(tempORI);
					// scanForInitializedObjects(result, alreadyScannedObjects, hardRefOrisToLoad);
					objectRefsToUpdate.add(tempORI);
					tempORI = new ObjRef(null, ObjRef.PRIMARY_KEY_INDEX, null, null);
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
			cciList.add(cci);
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
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(obj.getClass());
		Object id = metaData.getIdMember().getValue(obj, false);
		if (id == null)
		{
			// This may happen if a normally retrieved object gets deleted and therefore has lost its primary id
			// TODO: The object is still contained in the cache, maybe this should be reviewed
			return;
		}
		ObjRef objRef = new ObjRef(metaData.getEntityType(), ObjRef.PRIMARY_KEY_INDEX, id, null);
		objRefs.add(objRef);
		IProxyHelper proxyHelper = this.proxyHelper;
		IRelationInfoItem[] relationMembers = metaData.getRelationMembers();
		for (int a = relationMembers.length; a-- > 0;)
		{
			IRelationInfoItem relationMember = relationMembers[a];
			if (!proxyHelper.isInitialized(obj, relationMember))
			{
				continue;
			}
			Object value = relationMember.getValue(obj, false);
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

	protected void checkCascadeRefreshNeeded(List<CacheChangeItem> cacheChangeItems, Map<IObjRef, ILoadContainer> objRefToLoadContainerDict,
			ISet<IObjRef> cascadeRefreshObjRefsSet, ISet<IObjRelation> cascadeRefreshObjRelationsSet)
	{
		for (int c = cacheChangeItems.size(); c-- > 0;)
		{
			CacheChangeItem cci = cacheChangeItems.get(c);
			IWritableCache childCache = cci.cache;

			ChildCache cacheIntern = (ChildCache) childCache;
			IList<IObjRef> objectRefsToUpdate = cci.updatedObjRefs;
			IList<Object> objectsToUpdate = cci.updatedObjects;
			Map<Class<?>, List<CachePath>> typeToCachePathsDict = cacheIntern.getMembersToInitialize();

			if (typeToCachePathsDict == null || typeToCachePathsDict.size() == 0)
			{
				// No custom cascade load path defined here
				continue;
			}

			for (int a = objectRefsToUpdate.size(); a-- > 0;)
			{
				IObjRef oriToUpdate = objectRefsToUpdate.get(a);
				Object objectToUpdate = objectsToUpdate.get(a);
				ILoadContainer loadContainer = objRefToLoadContainerDict.get(oriToUpdate);
				if (loadContainer == null)
				{
					// Current value in childCache is not in our interest here
					return;
				}
				IObjRef[][] relations = loadContainer.getRelations();
				IEntityMetaData metaData = entityMetaDataProvider.getMetaData(oriToUpdate.getRealType());
				Class<?> entityType = metaData.getEntityType();
				IRelationInfoItem[] relationMembers = metaData.getRelationMembers();

				for (int b = relationMembers.length; b-- > 0;)
				{
					IRelationInfoItem member = relationMembers[b];
					if (proxyHelper.isInitialized(objectToUpdate, member))
					{
						// the object which has to be updated has initialized relations. So we have to ensure
						// that these relations are in the RootCache at the time the target object will be updated.
						// This is because initialized relations have to remain initialized after update but the relations
						// may have updated, too
						batchPendingRelations(objectToUpdate, member, relations[b], cascadeRefreshObjRefsSet, cascadeRefreshObjRelationsSet);
					}
				}

				if (typeToCachePathsDict == null || typeToCachePathsDict.size() == 0)
				{
					// No custom cascade load path defined here
					continue;
				}
				List<CachePath> cachePaths = typeToCachePathsDict.get(entityType);
				if (cachePaths == null)
				{
					return; // Nothing to do for this type of entity
				}
				for (int b = cachePaths.size(); b-- > 0;)
				{
					CachePath cachePath = cachePaths.get(b);
					int memberIndex = metaData.getIndexByRelationName(cachePath.memberName);
					batchPendingRelations(objectToUpdate, relationMembers[memberIndex], relations[memberIndex], cascadeRefreshObjRefsSet,
							cascadeRefreshObjRelationsSet);
				}
			}
		}
	}

	protected void batchPendingRelations(Object entity, IRelationInfoItem member, IObjRef[] relationsOfMember, ISet<IObjRef> cascadeRefreshObjRefsSet,
			ISet<IObjRelation> cascadeRefreshObjRelationsSet)
	{
		if (relationsOfMember == null)
		{
			IObjRelation objRelation = valueHolderContainerTemplate.getSelf(entity, member.getName());
			cascadeRefreshObjRelationsSet.add(objRelation);
		}
		else
		{
			cascadeRefreshObjRefsSet.addAll(relationsOfMember);
		}
	}

	protected void changeFirstLevelCaches(final IRootCache rootCache, ISet<Class<?>> occuringTypes, final ISet<Class<?>> directRelatingTypes,
			List<CacheChangeItem> cacheChangeItems, ISet<IObjRef> intermediateDeletes)
	{
		if (cacheChangeItems.size() == 0)
		{
			return;
		}
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		ArrayList<IDataChangeEntry> deletes = new ArrayList<IDataChangeEntry>();
		ICacheModification cacheModification = this.cacheModification;

		de.osthus.ambeth.util.Lock rootCacheReadLock = rootCache.getReadLock();
		boolean oldCacheModificationValue = cacheModification.isActive();
		cacheModification.setActive(true);
		// RootCache readlock must be acquired before individual writelock to the child caches due to deadlock reasons
		rootCacheReadLock.lock();
		try
		{
			for (int a = cacheChangeItems.size(); a-- > 0;)
			{
				CacheChangeItem cci = cacheChangeItems.get(a);
				IWritableCache childCache = cci.cache;

				ICacheIntern cacheIntern = (ICacheIntern) childCache;
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
							IEntityMetaData metaData = entityMetaDataProvider.getMetaData(deletedObject.getClass());
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
							IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objectInCache.getClass());
							Object id = metaData.getIdMember().getValue(objectInCache, false);
							if (id == null)
							{
								continue;
							}
							if (!rootCache.applyValues(objectInCache, cacheIntern))
							{
								if (log.isWarnEnabled())
								{
									log.warn(StringBuilderUtil.concat(tlObjectCollector, "No entry for object '", objectInCache,
											"' found in second level cache"));
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
			rootCacheReadLock.unlock();
			cacheModification.setActive(oldCacheModificationValue);
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
}
