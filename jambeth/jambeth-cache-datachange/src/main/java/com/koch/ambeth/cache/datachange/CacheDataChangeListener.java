package com.koch.ambeth.cache.datachange;

/*-
 * #%L
 * jambeth-cache-datachange
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.koch.ambeth.cache.AbstractCache;
import com.koch.ambeth.cache.ChildCache;
import com.koch.ambeth.cache.IFirstLevelCacheManager;
import com.koch.ambeth.cache.IRootCache;
import com.koch.ambeth.cache.ISecondLevelCacheManager;
import com.koch.ambeth.cache.mixin.ValueHolderContainerMixin;
import com.koch.ambeth.cache.proxy.IValueHolderContainer;
import com.koch.ambeth.cache.rootcachevalue.RootCacheValue;
import com.koch.ambeth.cache.transfer.ObjRelation;
import com.koch.ambeth.datachange.model.DirectDataChangeEntry;
import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.datachange.model.IDataChangeEntry;
import com.koch.ambeth.datachange.model.PostDataChange;
import com.koch.ambeth.datachange.transfer.DataChangeEvent;
import com.koch.ambeth.event.IEventDispatcher;
import com.koch.ambeth.event.IEventListener;
import com.koch.ambeth.event.IEventTargetEventListener;
import com.koch.ambeth.event.IProcessResumeItem;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICacheModification;
import com.koch.ambeth.merge.cache.IWritableCache;
import com.koch.ambeth.merge.cache.ValueHolderState;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.security.privilege.IPrivilegeProvider;
import com.koch.ambeth.security.privilege.model.IPrivilege;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.PrimitiveMember;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.Lock;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.util.model.IDataObject;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;
import com.koch.ambeth.util.threading.IGuiThreadHelper;

public class CacheDataChangeListener implements IEventListener, IEventTargetEventListener {
	@LogInstance
	private ILogger log;

	protected static final Set<CacheDirective> cacheValueResultAndReturnMissesSet = EnumSet
			.of(CacheDirective.CacheValueResult, CacheDirective.ReturnMisses);

	protected static final Set<CacheDirective> failInCacheHierarchyAndCacheValueResultAndReturnMissesSet =
			EnumSet
					.of(CacheDirective.FailInCacheHierarchy, CacheDirective.CacheValueResult,
							CacheDirective.ReturnMisses);

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

	@Autowired(optional = true)
	protected IPrivilegeProvider privilegeProvider;

	@Autowired
	protected ISecondLevelCacheManager secondLevelCacheManager;

	@Autowired(optional = true)
	protected ISecurityActivation securityActivation;

	@Autowired
	protected ValueHolderContainerMixin valueHolderContainerTemplate;

	@Override
	public void handleEvent(Object eventObject, long dispatchTime, long sequenceId) {
		dataChanged((IDataChange) eventObject, null, null, dispatchTime, sequenceId);
	}

	@Override
	public void handleEvent(Object eventObject, Object resumedEventTarget,
			List<Object> pausedEventTargets, long dispatchTime, long sequenceId) {
		dataChanged((IDataChange) eventObject, resumedEventTarget, pausedEventTargets, dispatchTime,
				sequenceId);
	}

	protected void dataChanged(final IDataChange dataChange, Object resumedEventTarget,
			final List<Object> pausedEventTargets, long dispatchTime, long sequenceId) {
		final CacheDependencyNode rootNode = buildCacheDependency();

		if (pausedEventTargets != null && !pausedEventTargets.isEmpty()) {
			IdentityHashSet<Object> collisionSet = buildCollisionSet(rootNode);
			if (collisionSet.containsAny(pausedEventTargets)) {
				// Without the current rootcache we can not handle the event now. We have to block till the
				// rootCache and all childCaches get valid
				eventDispatcher.waitEventToResume(collisionSet, -1,
						new IBackgroundWorkerParamDelegate<IProcessResumeItem>() {
							@Override
							public void invoke(IProcessResumeItem processResumeItem) throws Exception {
								dataChangedIntern(dataChange, pausedEventTargets, processResumeItem, rootNode);
								eventDispatcher.dispatchEvent(new PostDataChange(dataChange));
							}
						}, null);
				return;
			}
		}
		dataChangedIntern(dataChange, pausedEventTargets, null, rootNode);
		eventDispatcher.dispatchEvent(new PostDataChange(dataChange));
	}

	protected IdentityHashSet<Object> buildCollisionSet(CacheDependencyNode node) {
		IdentityHashSet<Object> collisionSet = new IdentityHashSet<>();
		buildCollisionSetIntern(node, collisionSet);
		return collisionSet;
	}

	protected void buildCollisionSetIntern(CacheDependencyNode node,
			IdentityHashSet<Object> collisionSet) {
		collisionSet.add(node.rootCache);
		ArrayList<CacheDependencyNode> childNodes = node.childNodes;
		for (int a = childNodes.size(); a-- > 0;) {
			buildCollisionSetIntern(childNodes.get(a), collisionSet);
		}
		collisionSet.addAll(node.directChildCaches);
	}

	protected void cleanupSecondLevelCaches(CacheDependencyNode node, IList<IObjRef> deletesList,
			List<IDataChangeEntry> updates, HashSet<Class<?>> occuringTypes) {
		ArrayList<IObjRef> objRefsRemovePriorVersions = new ArrayList<>(updates.size());
		for (int a = updates.size(); a-- > 0;) {
			IDataChangeEntry updateEntry = updates.get(a);
			Class<?> entityType = updateEntry.getEntityType();
			occuringTypes.add(entityType);
			objRefsRemovePriorVersions.add(new ObjRef(entityType, updateEntry.getIdNameIndex(),
					updateEntry.getId(), updateEntry.getVersion()));
		}
		cleanupSecondLevelCachesIntern(node, deletesList, objRefsRemovePriorVersions);
	}

	protected void cleanupSecondLevelCachesIntern(CacheDependencyNode node,
			IList<IObjRef> deletesList, ArrayList<IObjRef> objRefsRemovePriorVersions) {
		IRootCache rootCache = node.rootCache;
		Lock writeLock = rootCache.getWriteLock();
		writeLock.lock();
		try {
			rootCache.remove(deletesList);
			rootCache.removePriorVersions(objRefsRemovePriorVersions);
		}
		finally {
			writeLock.unlock();
		}
		ArrayList<CacheDependencyNode> childNodes = node.childNodes;
		for (int a = childNodes.size(); a-- > 0;) {
			cleanupSecondLevelCachesIntern(childNodes.get(a), deletesList, objRefsRemovePriorVersions);
		}
	}

	protected void dataChangedIntern(IDataChange dataChange, List<Object> pausedEventTargets,
			IProcessResumeItem processResumeItem, final CacheDependencyNode rootNode) {
		try {
			boolean isLocalSource = dataChange.isLocalSource();
			List<IDataChangeEntry> deletes = dataChange.getDeletes();
			List<IDataChangeEntry> updates = dataChange.getUpdates();
			List<IDataChangeEntry> inserts = dataChange.getInserts();

			final HashSet<Class<?>> occuringTypes = new HashSet<>();
			HashSet<IObjRef> deletesSet = new HashSet<>();
			final HashSet<Class<?>> directRelatingTypes = new HashSet<>();
			boolean acquirementSuccessful = rootNode.rootCache.acquireHardRefTLIfNotAlready();
			try {
				for (int a = deletes.size(); a-- > 0;) {
					IDataChangeEntry deleteEntry = deletes.get(a);
					Class<?> entityType = deleteEntry.getEntityType();
					occuringTypes.add(entityType);
					if (deleteEntry instanceof DirectDataChangeEntry) {
						// Ignore delete entries of unpersisted objects here
						continue;
					}
					ObjRef tempORI = new ObjRef(entityType, deleteEntry.getIdNameIndex(), deleteEntry.getId(),
							deleteEntry.getVersion());
					deletesSet.add(tempORI);
				}
				// Remove items from the cache only if they are really deleted/updates by a remote event
				// And not 'simulated' by a local source
				boolean cleanupSecondLevelCaches = false;
				if (pausedEventTargets != null && (!deletes.isEmpty() || !updates.isEmpty())
						&& !isLocalSource) {
					cleanupSecondLevelCaches = true;
				}
				else if (!updates.isEmpty()) {
					for (int a = updates.size(); a-- > 0;) {
						IDataChangeEntry updateEntry = updates.get(a);
						Class<?> entityType = updateEntry.getEntityType();
						occuringTypes.add(entityType);
					}
				}
				for (int a = inserts.size(); a-- > 0;) {
					IDataChangeEntry insertEntry = inserts.get(a);
					Class<?> entityType = insertEntry.getEntityType();
					occuringTypes.add(entityType);
				}
				ensureMetaDataIsLoaded(occuringTypes, directRelatingTypes);

				if (cleanupSecondLevelCaches) {
					cleanupSecondLevelCaches(rootNode, deletesSet.toList(), updates, occuringTypes);
				}

				buildCacheChangeItems(rootNode, dataChange);

				rootNode.aggregateAllCascadedObjRefs();

				final ISet<IObjRef> intermediateDeletes = rootNode.lookForIntermediateDeletes();

				changeSecondLevelCache(rootNode);

				if (rootNode.isPendingChangeOnAnyChildCache()) {
					guiThreadHelper.invokeInGuiAndWait(new IBackgroundWorkerDelegate() {
						@Override
						public void invoke() throws Exception {
							boolean oldFailEarlyModeActive = AbstractCache.isFailInCacheHierarchyModeActive();
							AbstractCache.setFailInCacheHierarchyModeActive(true);
							try {
								changeFirstLevelCaches(rootNode, intermediateDeletes);
							}
							finally {
								AbstractCache.setFailInCacheHierarchyModeActive(oldFailEarlyModeActive);
							}
						}
					});
				}
			}
			finally {
				rootNode.rootCache.clearHardRefs(acquirementSuccessful);
			}
		}
		finally {
			if (processResumeItem != null) {
				processResumeItem.resumeProcessingFinished();
			}
		}
	}

	protected void changeSecondLevelCache(CacheDependencyNode node) {
		changeSecondLevelCacheIntern(node, cacheValueResultAndReturnMissesSet);
	}

	protected void changeSecondLevelCacheIntern(CacheDependencyNode node,
			Set<CacheDirective> cacheDirective) {
		IRootCache rootCache = node.rootCache;
		HashMap<IObjRef, CacheValueAndPrivilege> objRefToLoadContainerDict = node.objRefToCacheValueMap;
		HashSet<IObjRef> objRefsToLoad = new HashSet<>(node.objRefsToLoad);

		if (node.cacheChangeItems != null) {
			for (CacheChangeItem cci : node.cacheChangeItems) {
				if (cci == null) {
					continue;
				}
				objRefsToLoad.addAll(cci.updatedObjRefs);
			}
		}
		IList<IObjRef> objRefs = objRefsToLoad.toList();
		IList<Object> refreshResult = rootCache.getObjects(objRefs, cacheDirective);

		IPrivilege[] privileges = null;
		if (securityActivation != null && privilegeProvider != null
				&& securityActivation.isFilterActivated()) {
			privileges = privilegeProvider.getPrivilegesByObjRef(objRefs).getPrivileges();
		}
		for (int a = refreshResult.size(); a-- > 0;) {
			RootCacheValue cacheValue = (RootCacheValue) refreshResult.get(a);
			if (cacheValue == null) {
				continue;
			}
			objRefToLoadContainerDict.put(objRefs.get(a),
					new CacheValueAndPrivilege(cacheValue, privileges != null ? privileges[a] : null));
		}
		checkCascadeRefreshNeeded(node);

		HashSet<IObjRef> cascadeRefreshObjRefsSet = node.cascadeRefreshObjRefsSet;
		HashSet<IObjRelation> cascadeRefreshObjRelationsSet = node.cascadeRefreshObjRelationsSet;
		if (!cascadeRefreshObjRelationsSet.isEmpty()) {
			IList<IObjRelationResult> relationsResult = rootCache
					.getObjRelations(cascadeRefreshObjRelationsSet.toList(), cacheDirective);
			for (int a = relationsResult.size(); a-- > 0;) {
				IObjRelationResult relationResult = relationsResult.get(a);
				cascadeRefreshObjRefsSet.addAll(relationResult.getRelations());
			}
			// apply gathered information of unknown relations to the rootCache
			rootCache.put(relationsResult);
		}
		if (!cascadeRefreshObjRefsSet.isEmpty()) {
			IList<IObjRef> cascadeRefreshObjRefsSetList = cascadeRefreshObjRefsSet.toList();
			refreshResult = rootCache.getObjects(cascadeRefreshObjRefsSetList, cacheDirective);
		}
		ArrayList<CacheDependencyNode> childNodes = node.childNodes;
		for (int a = childNodes.size(); a-- > 0;) {
			changeSecondLevelCacheIntern(childNodes.get(a),
					failInCacheHierarchyAndCacheValueResultAndReturnMissesSet);
		}
	}

	protected CacheDependencyNode buildCacheDependency() {
		IRootCache privilegedSecondLevelCache = secondLevelCacheManager
				.selectPrivilegedSecondLevelCache(true);
		IRootCache nonPrivilegedSecondLevelCache = secondLevelCacheManager
				.selectNonPrivilegedSecondLevelCache(false);
		IList<IWritableCache> selectedFirstLevelCaches = firstLevelCacheManager
				.selectFirstLevelCaches();

		IdentityHashMap<IRootCache, CacheDependencyNode> secondLevelCacheToNodeMap =
				new IdentityHashMap<>();
		if (privilegedSecondLevelCache != null) {
			CacheDependencyNodeFactory.addRootCache(privilegedSecondLevelCache.getCurrentRootCache(),
					secondLevelCacheToNodeMap);
		}
		if (nonPrivilegedSecondLevelCache != null) {
			CacheDependencyNodeFactory.addRootCache(nonPrivilegedSecondLevelCache.getCurrentRootCache(),
					secondLevelCacheToNodeMap);
		}
		for (int a = selectedFirstLevelCaches.size(); a-- > 0;) {
			ChildCache childCache = (ChildCache) selectedFirstLevelCaches.get(a);

			IRootCache parent = ((IRootCache) childCache.getParent()).getCurrentRootCache();

			CacheDependencyNode node = CacheDependencyNodeFactory.addRootCache(parent,
					secondLevelCacheToNodeMap);
			node.directChildCaches.add(childCache);
		}
		return CacheDependencyNodeFactory.buildRootNode(secondLevelCacheToNodeMap);
	}

	protected void buildCacheChangeItems(CacheDependencyNode rootNode, IDataChange dataChange) {
		ArrayList<IDataChangeEntry> insertsAndUpdates = new ArrayList<>();
		List<IDataChangeEntry> deletes = dataChange.getDeletes();

		insertsAndUpdates.addAll(dataChange.getUpdates());
		insertsAndUpdates.addAll(dataChange.getInserts());
		ArrayList<IObjRef> changesToSearchInCache = new ArrayList<>(insertsAndUpdates.size());
		ArrayList<IObjRef> changesWithVersion = new ArrayList<>(insertsAndUpdates.size());
		ArrayList<IObjRef> deletesToSearchInCache = new ArrayList<>(deletes.size());
		for (int a = deletes.size(); a-- > 0;) {
			IDataChangeEntry deleteEntry = deletes.get(a);
			Object id = deleteEntry.getId();
			if (id == null) {
				deletesToSearchInCache.add(null);
				continue;
			}
			deletesToSearchInCache
					.add(new ObjRef(deleteEntry.getEntityType(), deleteEntry.getIdNameIndex(), id, null));
		}
		for (int a = insertsAndUpdates.size(); a-- > 0;) {
			IDataChangeEntry updateEntry = insertsAndUpdates.get(a);
			Object id = updateEntry.getId();
			if (id == null) {
				changesToSearchInCache.add(null);
				continue;
			}
			changesToSearchInCache
					.add(new ObjRef(updateEntry.getEntityType(), updateEntry.getIdNameIndex(), id, null));
			changesWithVersion.add(new ObjRef(updateEntry.getEntityType(), updateEntry.getIdNameIndex(),
					id, updateEntry.getVersion()));
		}
		buildCacheChangeItems(rootNode, deletesToSearchInCache, changesToSearchInCache,
				changesWithVersion);
	}

	@SuppressWarnings("unchecked")
	protected void buildCacheChangeItems(CacheDependencyNode node,
			ArrayList<IObjRef> deletesToSearchInCache, ArrayList<IObjRef> changesToSearchInCache,
			ArrayList<IObjRef> changesWithVersion) {
		ArrayList<ChildCache> directChildCaches = node.directChildCaches;
		for (int flcIndex = directChildCaches.size(); flcIndex-- > 0;) {
			ChildCache childCache = directChildCaches.get(flcIndex);

			ArrayList<IObjRef> objectRefsToDelete = new ArrayList<>();
			ArrayList<IObjRef> objectRefsToUpdate = new ArrayList<>();
			ArrayList<Object> objectsToUpdate = new ArrayList<>();

			Lock readLock = childCache.getReadLock();
			readLock.lock();
			try {
				List<Object> deletesInCache = childCache.getObjects(deletesToSearchInCache,
						CacheDirective.failEarlyAndReturnMisses());
				for (int a = deletesToSearchInCache.size(); a-- > 0;) {
					Object result = deletesInCache.get(a);
					if (result == null) {
						// not in this cache
						continue;
					}
					objectRefsToDelete.add(deletesToSearchInCache.get(a));
				}
				List<Object> changesInCache = childCache.getObjects(changesToSearchInCache,
						CacheDirective.failEarlyAndReturnMisses());
				for (int a = changesToSearchInCache.size(); a-- > 0;) {
					Object result = changesInCache.get(a);
					if (result == null) {
						// not in this cache
						continue;
					}
					// Attach version to ORI. We can not do this before because then we would have had a
					// cache miss in the childCache above. We need the version now because our second level
					// cache
					// has to refresh its entries
					IObjRef objRefWithVersion = changesWithVersion.get(a);

					node.hardRefObjRefsToLoad.add(objRefWithVersion);

					if (result instanceof IDataObject) {
						IDataObject dataObject = (IDataObject) result;
						if (dataObject.isToBeUpdated() || dataObject.isToBeDeleted()) {
							continue;
						}
					}
					if (objRefWithVersion.getVersion() != null) {
						IEntityMetaData metaData = ((IEntityMetaDataHolder) result).get__EntityMetaData();
						Object versionInCache = metaData.getVersionMember() != null
								? metaData.getVersionMember().getValue(result, false)
								: null;
						if (versionInCache != null && ((Comparable<Object>) objRefWithVersion.getVersion())
								.compareTo(versionInCache) <= 0) {
							continue;
						}
					}
					objectsToUpdate.add(result);

					node.objRefsToLoad.add(objRefWithVersion);
					objectRefsToUpdate.add(objRefWithVersion);
				}
			}
			finally {
				readLock.unlock();
			}
			if (objectRefsToDelete.isEmpty() && objectsToUpdate.isEmpty()) {
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
		for (int a = childNodes.size(); a-- > 0;) {
			buildCacheChangeItems(childNodes.get(a), deletesToSearchInCache, changesToSearchInCache,
					changesWithVersion);
		}
	}

	protected void ensureMetaDataIsLoaded(ISet<Class<?>> occuringTypes,
			ISet<Class<?>> directRelatingTypes) {
		HashSet<Class<?>> wholeRelatedTypes = new HashSet<>(occuringTypes);
		ArrayList<Class<?>> additionalTypes = new ArrayList<>();
		{
			// Own code scope
			IList<Class<?>> occuringTypesList = occuringTypes.toList();
			IList<IEntityMetaData> occuringMetaData = entityMetaDataProvider
					.getMetaData(occuringTypesList);

			for (int a = 0, size = occuringMetaData.size(); a < size; a++) {
				IEntityMetaData metaData = occuringMetaData.get(a);
				for (Class<?> type : metaData.getTypesRelatingToThis()) {
					directRelatingTypes.add(type);
					if (wholeRelatedTypes.add(type)) {
						// Additional related type in this whole datachange
						additionalTypes.add(type);
					}
				}
			}
		}
		while (!additionalTypes.isEmpty()) {
			IList<IEntityMetaData> additionalMetaData = entityMetaDataProvider
					.getMetaData(additionalTypes);
			additionalTypes.clear();
			for (IEntityMetaData metaData : additionalMetaData) {
				for (Class<?> type : metaData.getTypesRelatingToThis()) {
					if (wholeRelatedTypes.add(type)) {
						// Additional related type in this whole datachange
						additionalTypes.add(type);
					}
				}
			}
		}
	}

	protected void checkCascadeRefreshNeeded(CacheDependencyNode node) {
		CacheChangeItem[] cacheChangeItems = node.cacheChangeItems;
		if (cacheChangeItems == null) {
			return;
		}
		HashMap<IObjRef, CacheValueAndPrivilege> objRefToCacheValueMap = node.objRefToCacheValueMap;
		for (int c = cacheChangeItems.length; c-- > 0;) {
			CacheChangeItem cci = cacheChangeItems[c];
			if (cci == null) {
				continue;
			}
			IList<IObjRef> objectRefsToUpdate = cci.updatedObjRefs;
			IList<Object> objectsToUpdate = cci.updatedObjects;

			for (int a = objectRefsToUpdate.size(); a-- > 0;) {
				IObjRef objRefToUpdate = objectRefsToUpdate.get(a);
				Object objectToUpdate = objectsToUpdate.get(a);
				CacheValueAndPrivilege cacheValueAndPrivilege = objRefToCacheValueMap.get(objRefToUpdate);
				if (cacheValueAndPrivilege == null) {
					// Current value in childCache is not in our interest here
					continue;
				}
				IEntityMetaData metaData = ((IEntityMetaDataHolder) objectToUpdate).get__EntityMetaData();
				RelationMember[] relationMembers = metaData.getRelationMembers();
				if (relationMembers.length == 0) {
					continue;
				}
				RootCacheValue cacheValue = cacheValueAndPrivilege.cacheValue;
				IObjRefContainer vhc = (IObjRefContainer) objectToUpdate;
				for (int relationIndex = relationMembers.length; relationIndex-- > 0;) {
					if (ValueHolderState.INIT != vhc.get__State(relationIndex)) {
						continue;
					}
					// the object which has to be updated has initialized relations. So we have to ensure
					// that these relations are in the RootCache at the time the target object will be
					// updated.
					// This is because initialized relations have to remain initialized after update but the
					// relations
					// may have been updated, too
					batchPendingRelations(cacheValue, relationMembers[relationIndex],
							cacheValue.getRelation(relationIndex), node);
				}
			}
		}
	}

	protected void batchPendingRelations(RootCacheValue cacheValue, RelationMember member,
			IObjRef[] relationsOfMember, CacheDependencyNode node) {
		if (relationsOfMember == null) {
			IObjRelation objRelation = valueHolderContainerTemplate.getSelf(cacheValue, member.getName());
			IObjRef[] objRefs = objRelation.getObjRefs();
			for (int a = objRefs.length; a-- > 0;) {
				objRefs[a].setVersion(null);
			}
			((ObjRelation) objRelation).setVersion(null);
			node.cascadeRefreshObjRelationsSet.add(objRelation);
		}
		else {
			node.cascadeRefreshObjRefsSet.addAll(relationsOfMember);
		}
	}

	protected void changeFirstLevelCaches(CacheDependencyNode node,
			ISet<IObjRef> intermediateDeletes) {
		ArrayList<IDataChangeEntry> deletes = new ArrayList<>();
		ICacheModification cacheModification = this.cacheModification;

		boolean oldCacheModificationValue = cacheModification.isActive();
		if (!oldCacheModificationValue) {
			cacheModification.setActive(true);
		}
		try {
			changeFirstLevelCachesIntern(node, intermediateDeletes);
		}
		finally {
			if (!oldCacheModificationValue) {
				cacheModification.setActive(oldCacheModificationValue);
			}
		}
		if (!deletes.isEmpty()) {
			final IDataChange dce = DataChangeEvent.create(0, 0, deletes.size());
			dce.getDeletes().addAll(deletes);
			guiThreadHelper.invokeOutOfGui(new IBackgroundWorkerDelegate() {
				@Override
				public void invoke() throws Exception {
					eventDispatcher.dispatchEvent(dce);
				}
			});
		}
	}

	protected void changeFirstLevelCachesIntern(CacheDependencyNode node,
			ISet<IObjRef> intermediateDeletes) {
		ArrayList<CacheDependencyNode> childNodes = node.childNodes;
		for (int a = childNodes.size(); a-- > 0;) {
			changeFirstLevelCachesIntern(childNodes.get(a), intermediateDeletes);
		}
		CacheChangeItem[] cacheChangeItems = node.cacheChangeItems;
		if (cacheChangeItems == null) {
			return;
		}
		IRootCache parentCache = node.rootCache;
		// RootCache readlock must be acquired before individual writelock to the child caches due to
		// deadlock reasons
		Lock parentCacheReadLock = parentCache.getReadLock();

		parentCacheReadLock.lock();
		try {
			HashMap<IObjRef, CacheValueAndPrivilege> objRefToCacheValueMap = node.objRefToCacheValueMap;

			for (int a = cacheChangeItems.length; a-- > 0;) {
				CacheChangeItem cci = cacheChangeItems[a];
				if (cci == null) {
					continue;
				}
				ChildCache childCache = node.directChildCaches.get(a);

				IList<IObjRef> deletedObjRefs = cci.deletedObjRefs;
				IList<Object> objectsToUpdate = cci.updatedObjects;
				IList<IObjRef> objRefsToUpdate = cci.updatedObjRefs;

				com.koch.ambeth.util.Lock writeLock = childCache.getWriteLock();

				writeLock.lock();
				try {
					if (deletedObjRefs != null && !deletedObjRefs.isEmpty()) {
						IList<Object> deletedObjects = childCache.getObjects(cci.deletedObjRefs,
								CacheDirective.failEarly());
						childCache.remove(cci.deletedObjRefs);
						for (int b = deletedObjects.size(); b-- > 0;) {
							Object deletedObject = deletedObjects.get(b);
							IEntityMetaData metaData = ((IEntityMetaDataHolder) deletedObject)
									.get__EntityMetaData();
							metaData.getIdMember().setValue(deletedObject, null);
							if (metaData.getVersionMember() != null) {
								metaData.getVersionMember().setValue(deletedObject, null);
							}
						}
					}
					for (IObjRef intermediateDeleteObjRef : intermediateDeletes) {
						childCache.remove(intermediateDeleteObjRef);
					}
					if (objectsToUpdate != null && !objectsToUpdate.isEmpty()) {
						ArrayList<IObjRef> objRefsToForget = null;
						for (int b = objectsToUpdate.size(); b-- > 0;) {
							Object objectInCache = objectsToUpdate.get(b);
							IObjRef objRefInCache = objRefsToUpdate.get(b);
							// Check if the objects still have their id. They may have lost them concurrently
							// because this
							// method here may be called from another thread (e.g. UI thread)
							IEntityMetaData metaData = ((IEntityMetaDataHolder) objectInCache)
									.get__EntityMetaData();
							Object id = metaData.getIdMember().getValue(objectInCache, false);
							if (id == null) {
								continue;
							}
							CacheValueAndPrivilege cacheValueP = objRefToCacheValueMap.get(objRefInCache);
							if (cacheValueP == null) {
								if (objRefsToForget == null) {
									objRefsToForget = new ArrayList<>();
								}
								objRefsToForget.add(objRefInCache);

								for (PrimitiveMember member : metaData.getPrimitiveMembers()) {
									member.setValue(objectInCache, null);
								}
								RelationMember[] relationMembers = metaData.getRelationMembers();
								for (int relationIndex = relationMembers.length; relationIndex-- > 0;) {
									((IValueHolderContainer) objectInCache).set__Uninitialized(relationIndex, null);
								}
								continue;
							}
							if (!parentCache.applyValues(objectInCache, childCache, cacheValueP.privilege)) {
								if (log.isWarnEnabled()) {
									log.warn(
											"No entry for object '" + objectInCache + "' found in second level cache");
								}
							}
						}
						if (objRefsToForget != null) {
							childCache.remove(objRefsToForget);
						}
					}
				}
				finally {
					writeLock.unlock();
				}
			}
		}
		finally {
			parentCacheReadLock.unlock();
		}
	}
}
