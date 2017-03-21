package com.koch.ambeth.cache.util;

/*-
 * #%L
 * jambeth-cache
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

import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.koch.ambeth.cache.ICacheIntern;
import com.koch.ambeth.cache.mixin.ValueHolderContainerMixin;
import com.koch.ambeth.cache.proxy.IValueHolderContainer;
import com.koch.ambeth.cache.rootcachevalue.RootCacheValue;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.ILightweightTransaction;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICacheModification;
import com.koch.ambeth.merge.cache.ValueHolderState;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.merge.metadata.IMemberTypeProvider;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.merge.util.DirectValueHolderRef;
import com.koch.ambeth.merge.util.ICacheHelper;
import com.koch.ambeth.merge.util.IPrefetchConfig;
import com.koch.ambeth.merge.util.IPrefetchHandle;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.merge.util.IPrefetchState;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.PrimitiveMember;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.IdentityLinkedMap;
import com.koch.ambeth.util.collections.IdentityLinkedSet;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.collections.ObservableArrayList;
import com.koch.ambeth.util.collections.ObservableHashSet;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IGuiThreadHelper;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

public class CacheHelper implements ICacheHelper, ICachePathHelper, IPrefetchHelper {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	private static final Object[] emptyObjectArray = new Object[0];

	private static final Set<CacheDirective> failEarlyReturnMisses =
			EnumSet.of(CacheDirective.FailEarly, CacheDirective.ReturnMisses);

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected ICacheModification cacheModification;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IGuiThreadHelper guiThreadHelper;

	@Autowired
	protected IMemberTypeProvider memberTypeProvider;

	@Autowired
	protected IObjRefHelper objRefHelper;

	@Autowired
	protected IPrioMembersProvider prioMembersProvider;

	@Autowired(optional = true)
	protected ILightweightTransaction transaction;

	@Autowired
	protected ValueHolderContainerMixin valueHolderContainerMixin;

	@Property(name = MergeConfigurationConstants.PrefetchInLazyTransactionActive,
			defaultValue = "true")
	protected boolean lazyTransactionActive;

	protected final ThreadLocal<AlreadyHandledSet> alreadyHandledSetTL =
			new ThreadLocal<AlreadyHandledSet>();

	@Override
	public void buildCachePath(Class<?> entityType, String memberToInitialize,
			ISet<AppendableCachePath> cachePaths) {
		Class<?> currentType = entityType;
		String requestedMemberName = memberToInitialize;
		AppendableCachePath currentCachePath = null;
		ISet<AppendableCachePath> currentCachePaths = cachePaths;

		while (true) {
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(currentType);
			Member widenedMember = metaData.getWidenedMatchingMember(requestedMemberName);
			if (widenedMember == null) {
				throw new IllegalArgumentException(
						"No member found to resolve path " + entityType.getName() + "." + memberToInitialize);
			}
			String widenedMemberName = widenedMember.getName();
			if (widenedMember instanceof PrimitiveMember) {
				if (widenedMemberName.equals(memberToInitialize)) {
					// this member does not need to be prefetched
					return;
				}
				// widened member has been found but not the full path of the requested member name
				throw new IllegalArgumentException(
						"No member found to resolve path " + entityType.getName() + "." + memberToInitialize);
			}
			AppendableCachePath childCachePath = null;
			if (currentCachePaths == null) {
				currentCachePaths = new LinkedHashSet<AppendableCachePath>();
				currentCachePath.children = currentCachePaths;
			}
			for (AppendableCachePath cachePath : currentCachePaths) {
				if (widenedMemberName.equals(cachePath.memberName)) {
					childCachePath = cachePath;
					break;
				}
			}
			if (childCachePath == null) {
				int relationIndex = metaData.getIndexByRelation(widenedMember);
				childCachePath = new AppendableCachePath(widenedMember.getElementType(), relationIndex,
						widenedMemberName);
				currentCachePaths.add(childCachePath);
			}
			if (widenedMemberName.equals(requestedMemberName)) {
				// we have travered the full path of the requested member name
				return;
			}
			requestedMemberName = requestedMemberName.substring(widenedMemberName.length() + 1);
			currentCachePath = childCachePath;
			currentType = currentCachePath.memberType;
			currentCachePaths = currentCachePath.children;
		}
	}

	@Override
	public IPrefetchConfig createPrefetch() {
		return beanContext.registerBean(PrefetchConfig.class).finish();
	}

	@Override
	public IPrefetchState ensureInitializedRelations(Object objects,
			ILinkedMap<Class<?>, PrefetchPath[]> entityTypeToPrefetchSteps) {
		if (objects == null || entityTypeToPrefetchSteps == null
				|| entityTypeToPrefetchSteps.size() == 0) {
			return null;
		}
		return ensureInitializedRelationsIntern(objects, entityTypeToPrefetchSteps);
	}

	@Override
	public IPrefetchState prefetch(Object objects) {
		return ensureInitializedRelationsIntern(objects, null);
	}

	protected IPrefetchState ensureInitializedRelationsIntern(final Object objects,
			final ILinkedMap<Class<?>, PrefetchPath[]> entityTypeToPrefetchPaths) {
		if (objects == null) {
			return null;
		}
		ICacheModification cacheModification = this.cacheModification;
		boolean oldActive = cacheModification.isActive();
		if (!oldActive) {
			cacheModification.setActive(true);
		}
		try {
			if (!lazyTransactionActive || transaction == null || transaction.isActive()) {
				return ensureInitializedRelationsIntern2(objects, entityTypeToPrefetchPaths);
			}
			return transaction
					.runInLazyTransaction(new IResultingBackgroundWorkerDelegate<IPrefetchState>() {
						@Override
						public IPrefetchState invoke() throws Throwable {
							return ensureInitializedRelationsIntern2(objects, entityTypeToPrefetchPaths);
						}
					});
		}
		finally {
			if (!oldActive) {
				cacheModification.setActive(false);
			}
		}
	}

	protected IPrefetchState ensureInitializedRelationsIntern2(Object objects,
			ILinkedMap<Class<?>, PrefetchPath[]> entityTypeToPrefetchPath) {
		boolean setCreated = false;
		AlreadyHandledSet alreadyHandledSet = alreadyHandledSetTL.get();
		try {
			if (alreadyHandledSet == null) {
				alreadyHandledSet = new AlreadyHandledSet();
				alreadyHandledSetTL.set(alreadyHandledSet);
				setCreated = true;
			}

			IdentityLinkedMap<ICacheIntern, ISet<IObjRef>> cacheToOrisLoadedHistory =
					new IdentityLinkedMap<ICacheIntern, ISet<IObjRef>>();
			IdentityLinkedMap<ICacheIntern, ISet<IObjRelation>> cacheToOrelsLoadedHistory =
					new IdentityLinkedMap<ICacheIntern, ISet<IObjRelation>>();
			IdentityLinkedMap<ICacheIntern, ISet<IObjRef>> cacheToOrisToLoad =
					new IdentityLinkedMap<ICacheIntern, ISet<IObjRef>>();
			IdentityLinkedMap<ICacheIntern, IMap<IObjRelation, Boolean>> cacheToOrelsToLoad =
					new IdentityLinkedMap<ICacheIntern, IMap<IObjRelation, Boolean>>();

			ArrayList<PrefetchCommand> loadItems = new ArrayList<PrefetchCommand>();

			handleObjects(objects, entityTypeToPrefetchPath, alreadyHandledSet, cacheToOrisLoadedHistory,
					cacheToOrelsLoadedHistory, cacheToOrisToLoad, cacheToOrelsToLoad, loadItems);
			// Remove all oris which have already been tried to load before
			if (cacheToOrisToLoad.isEmpty() && cacheToOrelsToLoad.isEmpty()) {
				// No ori remaining which makes sense to try to load
				if (setCreated) {
					return new PrefetchState(alreadyHandledSet);
				}
				return null;
			}

			ArrayList<Object> hardRefList = new ArrayList<Object>();
			// Store hard-ref-list to global hard ref
			alreadyHandledSet.put(hardRefList, null, Boolean.TRUE);

			processPendingOrelsAndObjRefs(entityTypeToPrefetchPath, alreadyHandledSet,
					cacheToOrisLoadedHistory, cacheToOrelsLoadedHistory, cacheToOrisToLoad,
					cacheToOrelsToLoad, loadItems, hardRefList);
			// No ori remaining which makes sense to try to load
			if (setCreated) {
				return new PrefetchState(alreadyHandledSet);
			}
			return null;
		}
		finally {
			if (setCreated) {
				alreadyHandledSetTL.remove();
			}
		}
	}

	protected void handleObjects(Object objects,
			ILinkedMap<Class<?>, PrefetchPath[]> entityTypeToPrefetchPath,
			AlreadyHandledSet alreadyHandledSet,
			IdentityLinkedMap<ICacheIntern, ISet<IObjRef>> cacheToOrisLoadedHistory,
			IdentityLinkedMap<ICacheIntern, ISet<IObjRelation>> cacheToOrelsLoadedHistory,
			IdentityLinkedMap<ICacheIntern, ISet<IObjRef>> cacheToOrisToLoad,
			IdentityLinkedMap<ICacheIntern, IMap<IObjRelation, Boolean>> cacheToOrelsToLoad,
			ArrayList<PrefetchCommand> loadItems) {
		if (objects instanceof Collection) {
			for (Object item : (Iterable<?>) objects) {
				if (item == null) {
					continue;
				}
				handleObjects(item, entityTypeToPrefetchPath, alreadyHandledSet, cacheToOrisLoadedHistory,
						cacheToOrelsLoadedHistory, cacheToOrisToLoad, cacheToOrelsToLoad, loadItems);
			}
			return;
		}
		else if (objects.getClass().isArray()) {
			for (Object item : (Object[]) objects) {
				if (item == null) {
					continue;
				}
				handleObjects(item, entityTypeToPrefetchPath, alreadyHandledSet, cacheToOrisLoadedHistory,
						cacheToOrelsLoadedHistory, cacheToOrisToLoad, cacheToOrelsToLoad, loadItems);
			}
			return;
		}
		PrefetchPath[] cachePaths = null;
		if (entityTypeToPrefetchPath != null) {
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objects.getClass());

			cachePaths = entityTypeToPrefetchPath.get(metaData.getEntityType());

			if (cachePaths == null || cachePaths.length == 0) {
				return;
			}
		}
		ensureInitializedRelationsIntern3(objects, cachePaths, entityTypeToPrefetchPath,
				cacheToOrisToLoad, cacheToOrelsToLoad, cacheToOrisLoadedHistory, cacheToOrelsLoadedHistory,
				alreadyHandledSet, loadItems);
	}

	protected void processPendingOrelsAndObjRefs(
			final ILinkedMap<Class<?>, PrefetchPath[]> entityTypeToPrefetchPath,
			final AlreadyHandledSet alreadyHandledSet,
			final IdentityLinkedMap<ICacheIntern, ISet<IObjRef>> cacheToOrisLoadedHistory,
			final IdentityLinkedMap<ICacheIntern, ISet<IObjRelation>> cacheToOrelsLoadedHistory,
			final IdentityLinkedMap<ICacheIntern, ISet<IObjRef>> cacheToOrisToLoad,
			final IdentityLinkedMap<ICacheIntern, IMap<IObjRelation, Boolean>> cacheToOrelsToLoad,
			final ArrayList<PrefetchCommand> pendingPrefetchCommands, ArrayList<Object> hardRefList) {
		// all relation members where at least one instance of the owning entity type needs a prefetch
		// on this member in the immediate next step
		final MergePrefetchPathsCache mergePrefetchPathsCache =
				new MergePrefetchPathsCache(entityMetaDataProvider);

		IdentityLinkedSet<Member> prioMembers = prioMembersProvider
				.getPrioMembers(entityTypeToPrefetchPath, pendingPrefetchCommands, mergePrefetchPathsCache);

		loadAndAddOrels(cacheToOrelsToLoad, hardRefList, cacheToOrelsLoadedHistory, cacheToOrisToLoad,
				prioMembers);
		loadAndAddOris(cacheToOrisToLoad, hardRefList, cacheToOrisLoadedHistory);

		while (!pendingPrefetchCommands.isEmpty()) {
			final PrefetchCommand[] currentPrefetchCommands =
					pendingPrefetchCommands.toArray(PrefetchCommand.class);
			// Clear the items to be ready for cascaded items in new batch recursion step
			pendingPrefetchCommands.clear();
			if (prioMembers.size() > 0) {
				for (int a = 0, size = currentPrefetchCommands.length; a < size; a++) {
					PrefetchCommand prefetchCommand = currentPrefetchCommands[a];
					DirectValueHolderRef valueHolder = prefetchCommand.valueHolder;
					if (!prioMembers.contains(valueHolder.getMember())) {
						currentPrefetchCommands[a] = null;
						pendingPrefetchCommands.add(prefetchCommand);
					}
				}
			}
			guiThreadHelper.invokeInGuiAndWait(new IBackgroundWorkerDelegate() {
				@Override
				public void invoke() throws Throwable {
					ICacheModification cacheModification = CacheHelper.this.cacheModification;
					ValueHolderContainerMixin valueHolderContainerMixin =
							CacheHelper.this.valueHolderContainerMixin;
					boolean oldActive = cacheModification.isActive();
					if (!oldActive) {
						cacheModification.setActive(true);
					}
					try {
						for (PrefetchCommand prefetchCommand : currentPrefetchCommands) {
							if (prefetchCommand == null) {
								continue;
							}
							DirectValueHolderRef valueHolder = prefetchCommand.valueHolder;
							PrefetchPath[] cachePaths = prefetchCommand.prefetchPaths;

							RelationMember member = valueHolder.getMember();
							// Merge the root prefetch path with the relative prefetch path
							cachePaths = mergePrefetchPathsCache.mergePrefetchPaths(member.getElementType(),
									cachePaths, entityTypeToPrefetchPath);

							IObjRefContainer vhc = valueHolder.getVhc();
							ICacheIntern targetCache;
							boolean doSetValue = false;
							if (valueHolder instanceof IndirectValueHolderRef) {
								IndirectValueHolderRef valueHolderKey = (IndirectValueHolderRef) valueHolder;
								targetCache = valueHolderKey.getRootCache();
							}
							else {
								targetCache = ((IValueHolderContainer) vhc).get__TargetCache();
								doSetValue = true;
							}
							int relationIndex = vhc.get__EntityMetaData().getIndexByRelation(member);
							IObjRef[] objRefs = vhc.get__ObjRefs(relationIndex);
							Object obj = valueHolderContainerMixin.getValue(vhc, relationIndex, member,
									targetCache, objRefs, CacheDirective.failEarly());
							if (doSetValue) {
								member.setValue(vhc, obj);
							}
							ensureInitializedRelationsIntern3(obj, cachePaths, entityTypeToPrefetchPath,
									cacheToOrisToLoad, cacheToOrelsToLoad, cacheToOrisLoadedHistory,
									cacheToOrelsLoadedHistory, alreadyHandledSet, pendingPrefetchCommands);
						}
					}
					finally {
						if (!oldActive) {
							cacheModification.setActive(false);
						}
					}
				}
			});
			// Remove all oris which have already been tried to load before
			if (cacheToOrisToLoad.size() == 0 && cacheToOrelsToLoad.size() == 0
					&& pendingPrefetchCommands.size() == 0) {
				return;
			}
			prioMembers = prioMembersProvider.getPrioMembers(entityTypeToPrefetchPath,
					pendingPrefetchCommands, mergePrefetchPathsCache);
			loadAndAddOrels(cacheToOrelsToLoad, hardRefList, cacheToOrelsLoadedHistory, cacheToOrisToLoad,
					prioMembers);
			loadAndAddOris(cacheToOrisToLoad, hardRefList, cacheToOrisLoadedHistory);
		}
	}

	protected void loadAndAddOris(ILinkedMap<ICacheIntern, ISet<IObjRef>> cacheToOrisToLoad,
			List<Object> hardRefList, IMap<ICacheIntern, ISet<IObjRef>> cacheToOrisLoadedHistory) {
		Iterator<Entry<ICacheIntern, ISet<IObjRef>>> iter = cacheToOrisToLoad.iterator();
		while (iter.hasNext()) {
			Entry<ICacheIntern, ISet<IObjRef>> entry = iter.next();
			ICacheIntern cache = entry.getKey();
			ISet<IObjRef> orisToLoad = entry.getValue();
			iter.remove();

			loadAndAddOris(cache, orisToLoad, hardRefList, cacheToOrisLoadedHistory);
		}
	}

	protected void loadAndAddOris(ICacheIntern cache, ISet<IObjRef> orisToLoad,
			List<Object> hardRefList, IMap<ICacheIntern, ISet<IObjRef>> cacheToOrisLoadedHistory) {
		IList<Object> result = cache.getObjects(orisToLoad.toList(), cache, CacheDirective.none());
		hardRefList.add(result);
		ISet<IObjRef> orisLoadedHistory = cacheToOrisLoadedHistory.get(cache);
		if (orisLoadedHistory == null) {
			orisLoadedHistory = new HashSet<IObjRef>();
			cacheToOrisLoadedHistory.put(cache, orisLoadedHistory);
		}
		orisLoadedHistory.addAll(orisToLoad);
	}

	protected void loadAndAddOrels(
			ILinkedMap<ICacheIntern, IMap<IObjRelation, Boolean>> cacheToOrelsToLoad,
			List<Object> hardRefList, IMap<ICacheIntern, ISet<IObjRelation>> cacheToOrelsLoadedHistory,
			ILinkedMap<ICacheIntern, ISet<IObjRef>> cacheToOrisToLoad,
			IdentityLinkedSet<Member> prioMembers) {
		Iterator<Entry<ICacheIntern, IMap<IObjRelation, Boolean>>> iter = cacheToOrelsToLoad.iterator();
		while (iter.hasNext()) {
			Entry<ICacheIntern, IMap<IObjRelation, Boolean>> entry = iter.next();
			ICacheIntern cache = entry.getKey();
			IMap<IObjRelation, Boolean> orelsToLoad = entry.getValue();

			loadAndAddOrels(cache, orelsToLoad, hardRefList, cacheToOrelsLoadedHistory, cacheToOrisToLoad,
					prioMembers);
			if (orelsToLoad.size() == 0) {
				iter.remove();
			}
		}
	}

	protected void loadAndAddOrels(ICacheIntern cache, IMap<IObjRelation, Boolean> orelsToLoad,
			List<Object> hardRefList, IMap<ICacheIntern, ISet<IObjRelation>> cacheToOrelsLoadedHistory,
			ILinkedMap<ICacheIntern, ISet<IObjRef>> cacheToOrisToLoad,
			IdentityLinkedSet<Member> prioMembers) {
		IList<IObjRelation> objRelList;
		if (prioMembers.size() > 0) {
			objRelList = new ArrayList<IObjRelation>(orelsToLoad.size());
			IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
			for (Entry<IObjRelation, Boolean> entry : orelsToLoad) {
				IObjRelation objRel = entry.getKey();
				IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRel.getRealType());
				RelationMember memberByName =
						(RelationMember) metaData.getMemberByName(objRel.getMemberName());
				if (!prioMembers.contains(memberByName)) {
					continue;
				}
				objRelList.add(objRel);
			}
		}
		else {
			objRelList = orelsToLoad.keyList();
		}
		IList<IObjRelationResult> objRelResults =
				cache.getObjRelations(objRelList, cache, CacheDirective.returnMisses());

		ISet<IObjRef> orisToLoad = null;
		for (int a = 0, size = objRelResults.size(); a < size; a++) {
			IObjRelation objRel = objRelList.get(a);
			IObjRelationResult objRelResult = objRelResults.get(a);
			Boolean objRefsOnly = orelsToLoad.remove(objRel);
			if (objRelResult == null) {
				continue;
			}
			IObjRef[] relations = objRelResult.getRelations();

			if (relations.length == 0 || objRefsOnly.booleanValue()) {
				// fetch only the objRefs, not the objects themselves
				continue;
			}
			if (orisToLoad == null) {
				orisToLoad = cacheToOrisToLoad.get(cache);
				if (orisToLoad == null) {
					orisToLoad = new HashSet<IObjRef>();
					cacheToOrisToLoad.put(cache, orisToLoad);
				}
			}
			orisToLoad.addAll(relations);
		}
		ISet<IObjRelation> orelsLoadedHistory = cacheToOrelsLoadedHistory.get(cache);
		if (orelsLoadedHistory == null) {
			orelsLoadedHistory = new HashSet<IObjRelation>();
			cacheToOrelsLoadedHistory.put(cache, orelsLoadedHistory);
		}
		orelsLoadedHistory.addAll(objRelList);
	}

	protected void ensureInitializedRelationsIntern3(Object obj, PrefetchPath[] cachePaths,
			ILinkedMap<Class<?>, PrefetchPath[]> entityTypeToPrefetchPaths,
			Map<ICacheIntern, ISet<IObjRef>> cacheToOrisToLoad,
			Map<ICacheIntern, IMap<IObjRelation, Boolean>> cacheToOrelsToLoad,
			Map<ICacheIntern, ISet<IObjRef>> cacheToOrisLoadedHistory,
			Map<ICacheIntern, ISet<IObjRelation>> cacheToOrelsLoadedHistory,
			AlreadyHandledSet alreadyHandledSet, List<PrefetchCommand> cascadeLoadItems) {
		if (obj == null) {
			return;
		}
		if (!alreadyHandledSet.putIfNotExists(obj, cachePaths, Boolean.TRUE)) {
			return;
		}
		if (obj instanceof IndirectValueHolderRef) {
			IndirectValueHolderRef vhk = (IndirectValueHolderRef) obj;
			handleValueHolder(vhk, cachePaths, cacheToOrisToLoad, cacheToOrelsToLoad,
					cacheToOrisLoadedHistory, cacheToOrelsLoadedHistory, cascadeLoadItems);
			// Do nothing because this is only to prefetch RootCache entries
			return;
		}
		if (obj instanceof DirectValueHolderRef) {
			DirectValueHolderRef vhk = (DirectValueHolderRef) obj;
			if (!handleValueHolder(vhk, cachePaths, cacheToOrisToLoad, cacheToOrelsToLoad,
					cacheToOrisLoadedHistory, cacheToOrelsLoadedHistory, cascadeLoadItems)) {
				return;
			}
			// force valueholder init. at this point we know that all related items are already in the
			// cache. there will
			// be no roundtrip to the server
			obj = vhk.getMember().getValue(vhk.getVhc());
		}
		if (obj == null) {
			// this check is necessary because even if we create only instances of DirectValueHolderRef in
			// cases where there is a not initalized relation
			// even then it might be possible that a concurrent thread initializes the valueholder to null
			// (e.g. an empty to-one relation)
			return;
		}
		if ((cachePaths == null || cachePaths.length == 0) && entityTypeToPrefetchPaths == null) {
			return;
		}
		if (obj instanceof Iterable) {
			for (Object item : (Iterable<?>) obj) {
				if (item == null) {
					continue;
				}
				ensureInitializedRelationsIntern3(item, cachePaths, entityTypeToPrefetchPaths,
						cacheToOrisToLoad, cacheToOrelsToLoad, cacheToOrisLoadedHistory,
						cacheToOrelsLoadedHistory, alreadyHandledSet, cascadeLoadItems);
			}
			return;
		}
		IEntityMetaData metaData = ((IEntityMetaDataHolder) obj).get__EntityMetaData();
		if (cachePaths == null || cachePaths.length == 0) {
			if (entityTypeToPrefetchPaths != null) {
				cachePaths = entityTypeToPrefetchPaths.get(metaData.getEntityType());
			}
			if (cachePaths == null || cachePaths.length == 0) {
				return;
			}
		}
		RelationMember[] relationMembers = metaData.getRelationMembers();
		if (relationMembers.length == 0) {
			return;
		}
		IValueHolderContainer vhc = (IValueHolderContainer) obj;
		for (int a = cachePaths.length; a-- > 0;) {
			PrefetchPath path = cachePaths[a];

			int relationIndex = path.memberIndex;
			RelationMember member = relationMembers[relationIndex];

			if (ValueHolderState.INIT != vhc.get__State(relationIndex)) {
				DirectValueHolderRef vhk = new DirectValueHolderRef(vhc, member);
				ensureInitializedRelationsIntern3(vhk, path.children, entityTypeToPrefetchPaths,
						cacheToOrisToLoad, cacheToOrelsToLoad, cacheToOrisLoadedHistory,
						cacheToOrelsLoadedHistory, alreadyHandledSet, cascadeLoadItems);
				continue;
			}
			Object memberValue = member.getValue(obj);
			if (memberValue == null) {
				continue;
			}
			ensureInitializedRelationsIntern3(memberValue, path.children, entityTypeToPrefetchPaths,
					cacheToOrisToLoad, cacheToOrelsToLoad, cacheToOrisLoadedHistory,
					cacheToOrelsLoadedHistory, alreadyHandledSet, cascadeLoadItems);
		}
	}

	protected boolean handleValueHolder(DirectValueHolderRef vhr, PrefetchPath[] cachePaths,
			Map<ICacheIntern, ISet<IObjRef>> cacheToOrisToLoad,
			Map<ICacheIntern, IMap<IObjRelation, Boolean>> cacheToOrelsToLoad,
			Map<ICacheIntern, ISet<IObjRef>> cacheToOrisLoadedHistory,
			Map<ICacheIntern, ISet<IObjRelation>> cacheToOrelsLoadedHistory,
			List<PrefetchCommand> cascadeLoadItems) {
		RelationMember member = vhr.getMember();
		boolean newOriToLoad = false;
		if (vhr instanceof IndirectValueHolderRef) {
			RootCacheValue rcv = (RootCacheValue) vhr.getVhc();
			ICacheIntern rootCache = ((IndirectValueHolderRef) vhr).getRootCache();
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(rcv.getEntityType());
			int relationIndex = metaData.getIndexByRelationName(member.getName());
			IObjRef[] rcvObjRefs = rcv.getRelation(relationIndex);
			if (rcvObjRefs == null) {
				IObjRelation self = valueHolderContainerMixin.getSelf(rcv, member.getName());
				ISet<IObjRelation> orelsLoadedHistory = cacheToOrelsLoadedHistory.get(rootCache);
				if (orelsLoadedHistory == null || !orelsLoadedHistory.contains(self)) {
					IMap<IObjRelation, Boolean> orelsToLoad = cacheToOrelsToLoad.get(rootCache);
					if (orelsToLoad == null) {
						orelsToLoad = new HashMap<IObjRelation, Boolean>();
						cacheToOrelsToLoad.put(rootCache, orelsToLoad);
					}
					orelsToLoad.put(self, Boolean.valueOf(vhr.isObjRefsOnly()));
					addCascadeLoadItem(vhr, cachePaths, cascadeLoadItems);
				}
				return false;
			}
			else if (!vhr.isObjRefsOnly() && rcvObjRefs.length > 0) {
				ISet<IObjRef> orisLoadedHistory = cacheToOrisLoadedHistory.get(rootCache);
				for (int b = rcvObjRefs.length; b-- > 0;) {
					IObjRef ori = rcvObjRefs[b];
					if (orisLoadedHistory != null && orisLoadedHistory.contains(ori)) {
						// Object has been tried to load before but it is obviously not in the cache
						// So the load must have been failed somehow. It is assumed that the entity
						// is not persisted in the database anymore (deleted before) so the ORI is illegal.
						// We cleanup the ValueHolder so that future calls will not lead to
						// another unnecessary roundtrip to the server
						rcvObjRefs[b] = null;
						continue;
					}
					ISet<IObjRef> orisToLoad = cacheToOrisToLoad.get(rootCache);
					if (orisToLoad == null) {
						orisToLoad = new HashSet<IObjRef>();
						cacheToOrisToLoad.put(rootCache, orisToLoad);
					}
					orisToLoad.add(ori);
					newOriToLoad = true;
				}
				if (newOriToLoad) {
					addCascadeLoadItem(vhr, cachePaths, cascadeLoadItems);
				}
			}
			return false;
		}
		IValueHolderContainer vhc = (IValueHolderContainer) vhr.getVhc();
		int relationIndex = vhc.get__EntityMetaData().getIndexByRelationName(member.getName());

		if (ValueHolderState.INIT == vhc.get__State(relationIndex)) {
			return true;
		}
		ICacheIntern cache = vhc.get__TargetCache();
		IObjRef[] objRefs = vhc.get__ObjRefs(relationIndex);
		if (objRefs == null) {
			IObjRelation self = vhc.get__Self(relationIndex);
			ArrayList<IObjRelation> orels = new ArrayList<IObjRelation>();
			orels.add(self);
			IList<IObjRelationResult> orelResults =
					cache.getObjRelations(orels, cache, failEarlyReturnMisses);
			IObjRelationResult orelResult = orelResults.get(0);
			if (orelResult == null) {
				ISet<IObjRelation> orelsLoadedHistory = cacheToOrelsLoadedHistory.get(cache);
				if (orelsLoadedHistory == null || !orelsLoadedHistory.contains(self)) {
					IMap<IObjRelation, Boolean> orelsToLoad = cacheToOrelsToLoad.get(cache);
					if (orelsToLoad == null) {
						orelsToLoad = new HashMap<IObjRelation, Boolean>();
						cacheToOrelsToLoad.put(cache, orelsToLoad);
					}
					orelsToLoad.put(self, vhr.isObjRefsOnly());
					addCascadeLoadItem(vhr, cachePaths, cascadeLoadItems);
				}
				return false;
			}
			objRefs = orelResult.getRelations();
			if (objRefs != null) {
				vhc.set__ObjRefs(relationIndex, objRefs);
			}
		}
		if (!vhr.isObjRefsOnly() && objRefs != null && objRefs.length > 0) {
			List<Object> loadedObjects =
					cache.getObjects(new ArrayList<IObjRef>(objRefs), cache, failEarlyReturnMisses);
			try {
				for (int b = objRefs.length; b-- > 0;) {
					IObjRef ori = objRefs[b];
					Object loadedObject = loadedObjects.get(b);
					if (loadedObject != null) {
						continue;
					}
					ISet<IObjRef> orisLoadedHistory = cacheToOrisLoadedHistory.get(cache);
					if (orisLoadedHistory != null && orisLoadedHistory.contains(ori)) {
						// Object has been tried to load before but it is obviously not in the cache
						// So the load must have been failed somehow. It is assumed that the entity
						// is not persisted in the database anymore (deleted before) so the ORI is illegal.
						// We cleanup the ValueHolder so that future calls will not lead to
						// another unnecessary roundtrip to the server
						objRefs[b] = null;
						continue;
					}
					ISet<IObjRef> orisToLoad = cacheToOrisToLoad.get(cache);
					if (orisToLoad == null) {
						orisToLoad = new HashSet<IObjRef>();
						cacheToOrisToLoad.put(cache, orisToLoad);
					}
					orisToLoad.add(ori);
					newOriToLoad = true;
				}
			}
			finally {
				loadedObjects.clear();
				loadedObjects = null;
			}
		}
		if (objRefs == null || newOriToLoad) {
			addCascadeLoadItem(vhr, cachePaths, cascadeLoadItems);
			return false;
		}
		return true;
	}

	protected void addCascadeLoadItem(DirectValueHolderRef vhr, PrefetchPath[] cachePaths,
			List<PrefetchCommand> cascadeLoadItems) {
		if (cachePaths != null || !vhr.isObjRefsOnly()) {
			PrefetchCommand cascadeLoadItem = new PrefetchCommand(vhr, cachePaths);
			cascadeLoadItems.add(cascadeLoadItem);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Collection createInstanceOfTargetExpectedType(Class<?> expectedType,
			Class<?> elementType) {
		// OneToMany or ManyToMany Relationship
		if (Iterable.class.isAssignableFrom(expectedType)) {
			if (expectedType.isInterface()) {
				if (Set.class.isAssignableFrom(expectedType)) {
					return new ObservableHashSet();
				}
				return new ObservableArrayList();
			}
			try {
				return (Collection) expectedType.newInstance();
			}
			catch (Throwable e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		return null;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public Object convertResultListToExpectedType(List<Object> resultList, Class<?> expectedType,
			Class<?> elementType) {
		// OneToMany or ManyToMany Relationship
		if (Collection.class.isAssignableFrom(expectedType)) {
			Collection targetCollection = createInstanceOfTargetExpectedType(expectedType, elementType);

			if (resultList != null) {
				((Collection<Object>) targetCollection).addAll(resultList);
			}
			return targetCollection;
		}
		if (resultList != null && !resultList.isEmpty()) {
			return resultList.get(0);
		}

		return null;
	}

	@Override
	public Object[] extractPrimitives(IEntityMetaData metaData, Object obj) {
		Member[] primitiveMembers = metaData.getPrimitiveMembers();
		Object[] primitives;

		if (primitiveMembers.length == 0) {
			primitives = emptyObjectArray;
		}
		else {
			primitives = new Object[primitiveMembers.length];
			for (int a = primitiveMembers.length; a-- > 0;) {
				Member primitiveMember = primitiveMembers[a];

				Object primitiveValue = primitiveMember.getValue(obj, true);

				if (primitiveValue != null
						&& java.util.Date.class.isAssignableFrom(primitiveValue.getClass())) {
					primitiveValue = ((java.util.Date) primitiveValue).getTime();
				}
				primitives[a] = primitiveValue;
			}
		}

		return primitives;
	}

	@Override
	public IObjRef[][] extractRelations(IEntityMetaData metaData, Object obj) {
		return extractRelations(metaData, obj, null);
	}

	@Override
	public IObjRef[][] extractRelations(IEntityMetaData metaData, Object obj,
			List<Object> relationValues) {
		RelationMember[] relationMembers = metaData.getRelationMembers();

		if (relationMembers.length == 0) {
			return ObjRef.EMPTY_ARRAY_ARRAY;
		}
		IValueHolderContainer vhc = (IValueHolderContainer) obj;
		IObjRef[][] relations = new IObjRef[relationMembers.length][];

		IObjRefHelper objRefHelper = this.objRefHelper;
		for (int a = relationMembers.length; a-- > 0;) {
			if (ValueHolderState.INIT != vhc.get__State(a)) {
				relations[a] = vhc.get__ObjRefs(a);
				continue;
			}
			Object relationValue = relationMembers[a].getValue(obj, false);
			if (relationValue == null) {
				relations[a] = ObjRef.EMPTY_ARRAY;
				continue;
			}
			IList<IObjRef> oris = objRefHelper.extractObjRefList(relationValue, null, null);
			if (relationValues != null) {
				relationValues.add(relationValue);
			}
			if (oris != null) {
				relations[a] = oris.toArray(IObjRef.class);
			}
		}

		return relations;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T, S> IList<T> extractTargetEntities(List<S> sourceEntities,
			String sourceToTargetEntityPropertyPath, Class<S> sourceEntityType) {
		// Einen Accessor ermitteln, der die gesamte Hierachie aus dem propertyPath ('A.B.C')
		// selbststaendig traversiert
		Member member =
				memberTypeProvider.getMember(sourceEntityType, sourceToTargetEntityPropertyPath);

		// MetaDaten der Ziel-Entity ermitteln, da wir (generisch) den PK brauchen, um damit ein
		// DISTINCT-Behavior durch
		// eine Map als Zwischenstruktur zu
		// erreichen
		IEntityMetaData targetMetaData = entityMetaDataProvider.getMetaData(member.getElementType());
		Member targetIdMember = targetMetaData.getIdMember();

		// Damit bei der Traversion keine Initialisierungen mit DB-Roundtrips entstehen, machen wir
		// vorher eine Prefetch
		// passend zum PropertyPath auf allen
		// uebergebenen Quell-Entities
		// Dadurch entstehen maximal 2 gebatchte SELECTs, egal wie gross die Liste ist
		IPrefetchHandle prefetch =
				createPrefetch().add(sourceEntityType, sourceToTargetEntityPropertyPath).build();
		@SuppressWarnings("unused")
		// Speichere das State-Result unbenutzt - wichtig fuer concurrent GC Aktivitaeten, um Verluste
		// an
		// Entity-Referenzen zu verhindern
		IPrefetchState state = prefetch.prefetch(sourceEntities);

		LinkedHashMap<Object, T> targetDistinctMap = new LinkedHashMap<Object, T>();
		// Danach traversieren, wobei wir jetzt wissen, dass uns das keine Roundtrips kostet
		for (int a = 0, size = sourceEntities.size(); a < size; a++) {
			S sourceEntity = sourceEntities.get(a);
			if (sourceEntity == null) {
				continue;
			}
			Object targetEntities = member.getValue(sourceEntity);
			if (targetEntities == null) {
				continue;
			}
			// Ergebnismenge flexibel (bei *-To-Many) verarbeiten oder so lassen (bei *-To-One)
			if (targetEntities instanceof Iterable) {
				for (Object targetEntity : (Iterable<?>) targetEntities) {
					if (targetEntity == null) {
						continue;
					}
					Object targetId = targetIdMember.getValue(targetEntity);
					if (targetId == null) {
						// Falls die Entity keine ID hat, speichern wir sie ausnahmsweise selbst als Key
						targetId = targetEntity;
					}
					targetDistinctMap.put(targetId, (T) targetEntity);
				}
			}
			else {
				Object targetId = targetIdMember.getValue(targetEntities);
				if (targetId == null) {
					// Falls die Entity keine ID hat, speichern wir sie ausnahmsweise selbst als Key
					targetId = targetEntities;
				}
				targetDistinctMap.put(targetId, (T) targetEntities);
			}
		}
		// Alle values sind unsere eindeutigen Target Entities ohne Duplikate
		return targetDistinctMap.values();
	}

	@Override
	public AppendableCachePath copyCachePathToAppendable(PrefetchPath cachePath) {
		PrefetchPath[] children = cachePath.children;
		LinkedHashSet<AppendableCachePath> clonedChildren = null;
		if (children != null) {
			clonedChildren = LinkedHashSet.create(children.length);
			for (int a = children.length; a-- > 0;) {
				clonedChildren.add(copyCachePathToAppendable(children[a]));
			}
		}
		AppendableCachePath clonedCachePath =
				new AppendableCachePath(cachePath.memberType, cachePath.memberIndex, cachePath.memberName);
		clonedCachePath.children = clonedChildren;
		return clonedCachePath;
	}

	@Override
	public PrefetchPath[] copyAppendableToCachePath(ISet<AppendableCachePath> children) {
		if (children == null) {
			return null;
		}
		PrefetchPath[] clonedChildren = new PrefetchPath[children.size()];
		int index = 0;
		for (AppendableCachePath child : children) {
			clonedChildren[index] = copyAppendableToCachePath(child);
			index++;
		}
		return clonedChildren;
	}

	@Override
	public PrefetchPath copyAppendableToCachePath(AppendableCachePath cachePath) {
		PrefetchPath[] clonedChildren = copyAppendableToCachePath(cachePath.children);
		if (clonedChildren == null) {
			return new PrefetchPath(cachePath.memberType, cachePath.memberIndex, cachePath.memberName,
					clonedChildren, new Class[0]);
		}
		HashSet<Class<?>> memberTypesOnDescendants = new HashSet<Class<?>>();
		for (PrefetchPath clonedChild : clonedChildren) {
			memberTypesOnDescendants.add(clonedChild.memberType);
			memberTypesOnDescendants.addAll(clonedChild.memberTypesOnDescendants);
		}
		return new PrefetchPath(cachePath.memberType, cachePath.memberIndex, cachePath.memberName,
				clonedChildren, memberTypesOnDescendants.toArray(Class.class));
	}

	@Override
	public void unionCachePath(AppendableCachePath cachePath, AppendableCachePath other) {
		ISet<AppendableCachePath> otherChildren = other.children;
		if (otherChildren == null) {
			// fast case 1
			return;
		}
		ISet<AppendableCachePath> children = cachePath.children;
		if (children == null) {
			// fast case 2
			cachePath.children = otherChildren;
			return;
		}
		for (AppendableCachePath otherCachePath : otherChildren) {
			if (children.add(otherCachePath)) {
				continue;
			}
			unionCachePath(children.get(otherCachePath), otherCachePath);
		}
	}
}
