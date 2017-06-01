package com.koch.ambeth.cache;

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.cache.audit.IVerifyOnLoad;
import com.koch.ambeth.cache.collections.CacheMapEntry;
import com.koch.ambeth.cache.config.CacheConfigurationConstants;
import com.koch.ambeth.cache.proxy.IPropertyChangeConfigurable;
import com.koch.ambeth.cache.rootcachevalue.IRootCacheValueFactory;
import com.koch.ambeth.cache.rootcachevalue.RootCacheValue;
import com.koch.ambeth.cache.service.ICacheRetriever;
import com.koch.ambeth.cache.transfer.LoadContainer;
import com.koch.ambeth.cache.transfer.ObjRelationResult;
import com.koch.ambeth.cache.util.IndirectValueHolderRef;
import com.koch.ambeth.event.IEventQueue;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.util.ImmutableTypeSet;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.CacheFactoryDirective;
import com.koch.ambeth.merge.cache.HandleContentDelegate;
import com.koch.ambeth.merge.cache.ICacheFactory;
import com.koch.ambeth.merge.cache.ICacheModification;
import com.koch.ambeth.merge.cache.ValueHolderState;
import com.koch.ambeth.merge.config.MergeConfigurationConstants;
import com.koch.ambeth.merge.copy.IObjectCopier;
import com.koch.ambeth.merge.metadata.IObjRefFactory;
import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.merge.proxy.IEntityMetaDataHolder;
import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.merge.security.ISecurityActivation;
import com.koch.ambeth.merge.security.ISecurityScopeProvider;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.merge.util.DirectValueHolderRef;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.security.privilege.IPrivilegeProvider;
import com.koch.ambeth.security.privilege.IPrivilegeProviderIntern;
import com.koch.ambeth.security.privilege.model.IPrivilege;
import com.koch.ambeth.service.IOfflineListener;
import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.PrimitiveMember;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.ListUtil;
import com.koch.ambeth.util.Lock;
import com.koch.ambeth.util.LockState;
import com.koch.ambeth.util.ParamHolder;
import com.koch.ambeth.util.ReadWriteLock;
import com.koch.ambeth.util.annotation.CascadeLoadMode;
import com.koch.ambeth.util.collections.AbstractHashSet;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IResizeMapCallback;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.collections.IdentityHashSet;
import com.koch.ambeth.util.collections.IntArrayList;
import com.koch.ambeth.util.collections.InterfaceFastList;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.io.FastByteArrayOutputStream;
import com.koch.ambeth.util.model.IDataObject;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;
import com.koch.ambeth.util.threading.IGuiThreadHelper;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

public class RootCache extends AbstractCache<RootCacheValue>
		implements IRootCache, IOfflineListener, ICacheRetriever {
	static class DoRelationObjRefsRefreshOnResize implements IResizeMapCallback {
		private final RootCache rootCache;

		protected boolean alreadyRefreshed = false;

		DoRelationObjRefsRefreshOnResize(RootCache rootCache) {
			this.rootCache = rootCache;
		}

		@Override
		public void resizeMapRequested(Object map) {
			boolean alreadyRefreshed = this.alreadyRefreshed;
			if (alreadyRefreshed) {
				return;
			}
			this.alreadyRefreshed = true;
			try {
				rootCache.doRelationObjRefsRefresh();
			}
			finally {
				this.alreadyRefreshed = false;
			}
		}
	}

	public static final String P_EVENT_QUEUE = "EventQueue";

	protected static final Map<Class<?>, Object> typeToEmptyArray = new HashMap<>(128, 0.5f);

	public static final Set<CacheDirective> failEarlyCacheValueResultSet =
			EnumSet.of(CacheDirective.FailEarly, CacheDirective.CacheValueResult);

	static {
		List<Class<?>> types = new ArrayList<>();
		ImmutableTypeSet.addImmutableTypesTo(types);
		types.add(Object.class);
		for (Class<?> type : types) {
			if (!void.class.equals(type)) {
				createEmptyArrayEntry(type);
			}
		}
	}

	protected static void createEmptyArrayEntry(Class<?> componentType) {
		typeToEmptyArray.put(componentType, Array.newInstance(componentType, 0));
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final HashMap<IObjRef, Integer> relationOris = new HashMap<>();

	protected final HashSet<IObjRef> currentPendingKeys = new HashSet<>();

	protected final InterfaceFastList<RootCacheValue> lruList = new InterfaceFastList<>();

	protected final ReentrantLock lruLock = new ReentrantLock();

	@Property(name = CacheConfigurationConstants.CacheLruThreshold, defaultValue = "0")
	protected int lruThreshold;

	@Property(name = MergeConfigurationConstants.SecurityActive, defaultValue = "false")
	protected boolean securityActive;

	@Autowired
	protected ICacheFactory cacheFactory;

	@Autowired
	protected ICacheModification cacheModification;

	@Autowired(optional = true)
	protected ICacheRetriever cacheRetriever;

	@Autowired(optional = true)
	protected IEventQueue eventQueue;

	@Autowired(optional = true)
	protected IObjectCopier objectCopier;

	@Autowired
	protected IObjRefFactory objRefFactory;

	@Autowired
	protected IObjRefHelper oriHelper;

	@Autowired
	protected IPrefetchHelper prefetchHelper;

	@Autowired(optional = true)
	protected IPrivilegeProviderIntern privilegeProvider;

	@Autowired
	protected IRootCacheValueFactory rootCacheValueFactory;

	@Autowired(optional = true)
	protected ISecurityActivation securityActivation;

	@Autowired(optional = true)
	protected ISecurityScopeProvider securityScopeProvider;

	@Autowired(optional = true)
	protected IVerifyOnLoad verifyOnLoad;

	@Property(mandatory = false)
	protected boolean privileged;

	protected long relationObjRefsRefreshThrottleOnGC = 60000; // throttle refresh to at most 1 time
																															// per minute

	protected long lastRelationObjRefsRefreshTime;

	protected final Lock pendingKeysReadLock, pendingKeysWriteLock;

	public RootCache() {
		ReadWriteLock pendingKeysRwLock = new ReadWriteLock();
		pendingKeysReadLock = pendingKeysRwLock.getReadLock();
		pendingKeysWriteLock = pendingKeysRwLock.getWriteLock();
		relationOris.setResizeMapCallback(new DoRelationObjRefsRefreshOnResize(this));
	}

	@Override
	public void dispose() {
		cacheFactory = null;
		cacheModification = null;
		cacheRetriever = null;
		eventQueue = null;
		objectCopier = null;
		oriHelper = null;
		prefetchHelper = null;
		privilegeProvider = null;

		super.dispose();
	}

	@Override
	public boolean isPrivileged() {
		return privileged;
	}

	@Override
	public IRootCache getCurrentRootCache() {
		return this;
	}

	@Override
	public IRootCache getParent() {
		return cacheRetriever instanceof IRootCache ? (IRootCache) cacheRetriever : null;
	}

	@Override
	public int getCacheId() {
		return -1;
	}

	@Override
	public void setCacheId(int cacheId) {
		throw new UnsupportedOperationException();
	}

	@Property(name = CacheConfigurationConstants.SecondLevelCacheWeakActive, defaultValue = "true")
	@Override
	public void setWeakEntries(boolean weakEntries) {
		super.setWeakEntries(weakEntries);
	}

	@Override
	protected boolean allowCacheValueReplacement() {
		return true;
	}

	@Override
	public RootCacheValue createCacheValueInstance(IEntityMetaData metaData, Object obj) {
		return rootCacheValueFactory.createRootCacheValue(metaData);
	}

	@Override
	protected Object getIdOfCacheValue(IEntityMetaData metaData, RootCacheValue cacheValue) {
		return cacheValue.getId();
	}

	@Override
	protected void setIdOfCacheValue(IEntityMetaData metaData, RootCacheValue cacheValue, Object id) {
		cacheValue.setId(id);
	}

	@Override
	protected Object getVersionOfCacheValue(IEntityMetaData metaData, RootCacheValue cacheValue) {
		return cacheValue.getVersion();
	}

	@Override
	protected void setVersionOfCacheValue(IEntityMetaData metaData, RootCacheValue cacheValue,
			Object version) {
		Member versionMember = metaData.getVersionMember();
		if (versionMember == null) {
			return;
		}
		version = conversionHelper.convertValueToType(versionMember.getRealType(), version);
		cacheValue.setVersion(version);
	}

	@Override
	protected void setRelationsOfCacheValue(IEntityMetaData metaData, RootCacheValue cacheValue,
			Object[] primitives, IObjRef[][] relations) {
		cacheValue.setPrimitives(primitives);
		cacheValue.setRelations(relations);
	}

	protected boolean isCacheRetrieverCallAllowed(Set<CacheDirective> cacheDirective) {
		if (cacheRetriever == null) {
			// without a valid cacheRetriever a call is never allowed
			return false;
		}
		if (cacheDirective.contains(CacheDirective.FailEarly)) {
			// with FailEarly a cascading call is never allowed
			return false;
		}
		if (cacheDirective.contains(CacheDirective.FailInCacheHierarchy)
				&& !(cacheRetriever instanceof IRootCache)) {
			// with FailInCacheHierarchy a cascading call is only allowed if the cacheRetriever is itself
			// an instance of IRootCache
			return false;
		}
		// in the end a call is only allowed if it is not forbidden for the current thread
		return !AbstractCache.isFailInCacheHierarchyModeActive();
	}

	@Override
	public IList<Object> getObjects(List<IObjRef> orisToGet, Set<CacheDirective> cacheDirective) {
		checkNotDisposed();
		if (orisToGet == null || orisToGet.size() == 0) {
			return EmptyList.getInstance();
		}
		if (cacheDirective.contains(CacheDirective.NoResult)
				|| cacheDirective.contains(CacheDirective.LoadContainerResult)
				|| cacheDirective.contains(CacheDirective.CacheValueResult)) {
			return getObjects(orisToGet, null, cacheDirective);
		}
		ICacheIntern targetCache;
		if (privileged && securityActivation != null && !securityActivation.isFilterActivated()) {
			targetCache = (ICacheIntern) cacheFactory
					.createPrivileged(CacheFactoryDirective.SubscribeTransactionalDCE, "RootCache.ADHOC");
		}
		else {
			targetCache = (ICacheIntern) cacheFactory
					.create(CacheFactoryDirective.SubscribeTransactionalDCE, "RootCache.ADHOC");
		}
		return getObjects(orisToGet, targetCache, cacheDirective);
	}

	@Override
	public Object getObject(IObjRef oriToGet, ICacheIntern targetCache,
			Set<CacheDirective> cacheDirective) {
		checkNotDisposed();
		if (oriToGet == null) {
			return null;
		}
		ArrayList<IObjRef> orisToGet = new ArrayList<>(1);
		orisToGet.add(oriToGet);
		List<Object> objects = getObjects(orisToGet, targetCache, cacheDirective);
		if (objects.isEmpty()) {
			return null;
		}
		return objects.get(0);
	}

	@Override
	public IList<Object> getObjects(final List<IObjRef> orisToGet, final ICacheIntern targetCache,
			final Set<CacheDirective> cacheDirective) {
		IVerifyOnLoad verifyOnLoad = this.verifyOnLoad;
		if (verifyOnLoad == null) {
			return getObjectsIntern(orisToGet, targetCache, cacheDirective);
		}
		try {
			return verifyOnLoad
					.verifyEntitiesOnLoad(new IResultingBackgroundWorkerDelegate<IList<Object>>() {
						@Override
						public IList<Object> invoke() throws Throwable {
							return getObjectsIntern(orisToGet, targetCache, cacheDirective);
						}
					});
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected IList<Object> getObjectsIntern(List<IObjRef> orisToGet, ICacheIntern targetCache,
			Set<CacheDirective> cacheDirective) {
		checkNotDisposed();
		if (orisToGet == null || orisToGet.size() == 0) {
			return EmptyList.getInstance();
		}
		if (cacheDirective == null) {
			cacheDirective = Collections.<CacheDirective>emptySet();
		}
		boolean isCacheRetrieverCallAllowed = isCacheRetrieverCallAllowed(cacheDirective);
		IEventQueue eventQueue = this.eventQueue;
		if (eventQueue != null) {
			eventQueue.pause(this);
		}
		try {
			Lock readLock = getReadLock();
			Lock writeLock = getWriteLock();
			ICacheModification cacheModification = this.cacheModification;
			boolean oldCacheModificationValue = cacheModification.isActive();
			boolean acquireSuccess = acquireHardRefTLIfNotAlready(orisToGet.size());
			if (!oldCacheModificationValue) {
				cacheModification.setActive(true);
			}
			try {
				if (!isCacheRetrieverCallAllowed) {
					// if the cascading call is not allowed we need no pre-scanning for cache-misses
					// we have to do our best while we create the result directly
					readLock.lock();
					try {
						return createResult(orisToGet, null, cacheDirective, targetCache, true, null);
					}
					finally {
						readLock.unlock();
					}
				}

				LockState lockState = writeLock.releaseAllLocks();
				ParamHolder<Boolean> doAnotherRetry = new ParamHolder<>();
				try {
					while (true) {
						doAnotherRetry.setValue(Boolean.FALSE);
						LinkedHashSet<IObjRef> neededObjRefs = new LinkedHashSet<>();
						ArrayList<DirectValueHolderRef> pendingValueHolders = new ArrayList<>();
						IList<Object> result = getObjectsRetry(orisToGet, targetCache, cacheDirective,
								doAnotherRetry, neededObjRefs, pendingValueHolders);
						while (neededObjRefs.size() > 0) {
							IList<IObjRef> objRefsToGetCascade = neededObjRefs.toList();
							neededObjRefs.clear();
							getObjectsRetry(objRefsToGetCascade, targetCache, cacheDirective, doAnotherRetry,
									neededObjRefs, pendingValueHolders);
						}
						if (Boolean.TRUE.equals(doAnotherRetry.getValue())) {
							continue;
						}
						if (pendingValueHolders.size() > 0) {
							prefetchHelper.prefetch(pendingValueHolders);
							continue;
						}
						return result;
					}
				}
				finally {
					writeLock.reacquireLocks(lockState);
				}
			}
			finally {
				if (!oldCacheModificationValue) {
					cacheModification.setActive(oldCacheModificationValue);
				}
				clearHardRefs(acquireSuccess);
			}
		}
		finally {
			if (eventQueue != null) {
				eventQueue.resume(this);
			}
		}
	}

	protected IList<Object> getObjectsRetry(List<IObjRef> orisToGet, ICacheIntern targetCache,
			Set<CacheDirective> cacheDirective, ParamHolder<Boolean> doAnotherRetry,
			LinkedHashSet<IObjRef> neededObjRefs, ArrayList<DirectValueHolderRef> pendingValueHolders) {
		final ArrayList<IObjRef> orisToLoad = new ArrayList<>();
		Lock readLock = getReadLock();
		Lock writeLock = getWriteLock();

		int cacheVersionBeforeLongTimeAction;
		if (boundThread == null) {
			RootCacheValue[] rootCacheValuesToGet = new RootCacheValue[orisToGet.size()];
			cacheVersionBeforeLongTimeAction =
					waitForConcurrentReadFinish(orisToGet, rootCacheValuesToGet, orisToLoad, cacheDirective);
			if (orisToLoad.size() == 0) {
				// Everything found in the cache. We STILL hold the readlock so we can immediately create
				// the result
				// We already even checked the version. So we do not bother with versions anymore here
				try {
					return createResult(orisToGet, rootCacheValuesToGet, cacheDirective, targetCache, false,
							null);
				}
				finally {
					readLock.unlock();
				}
			}
		}
		else {
			readLock.lock();
			try {
				IList<Object> result =
						createResult(orisToGet, null, cacheDirective, targetCache, false, orisToLoad);
				if (orisToLoad.size() == 0) {
					return result;
				}
				cacheVersionBeforeLongTimeAction = changeVersion;
			}
			finally {
				readLock.unlock();
			}
		}
		int cacheVersionAfterLongTimeAction;
		boolean releaseWriteLock = false;
		try {
			boolean loadSuccess = false;
			try {
				List<ILoadContainer> loadedEntities;
				if (privileged && securityActivation != null && securityActivation.isFilterActivated()) {
					try {
						loadedEntities = securityActivation.executeWithoutFiltering(
								new IResultingBackgroundWorkerDelegate<List<ILoadContainer>>() {
									@Override
									public List<ILoadContainer> invoke() throws Throwable {
										return cacheRetriever.getEntities(orisToLoad);
									}
								});
					}
					catch (Throwable e) {
						throw RuntimeExceptionUtil.mask(e);
					}
				}
				else {
					loadedEntities = cacheRetriever.getEntities(orisToLoad);
				}

				// Acquire write lock and mark this state. In the finally-Block the writeLock
				// has to be released in a deterministic way
				LockState releasedState = writeLock.releaseAllLocks();
				try {
					writeLock.lock();
					releaseWriteLock = true;

					cacheVersionAfterLongTimeAction = changeVersion;
					loadObjects(loadedEntities, neededObjRefs, pendingValueHolders);

					loadSuccess = true;

					clearPendingKeysOfCurrentThread(orisToLoad);
					orisToLoad.clear();

					if (neededObjRefs.size() > 0 || pendingValueHolders.size() > 0) {
						writeLock.unlock();
						releaseWriteLock = false;
						return null;
					}
				}
				finally {
					writeLock.reacquireLocks(releasedState);
				}
			}
			finally {
				if (!loadSuccess) {
					clearPendingKeysOfCurrentThread(orisToLoad);
				}
			}
			if (cacheVersionAfterLongTimeAction != cacheVersionBeforeLongTimeAction) {
				// Another thread did some changes (possibly DataChange-Remove actions)
				// We have to ensure that our result-scope is still valid
				// We return null to allow a further full retry of getObjects()
				doAnotherRetry.setValue(Boolean.TRUE);
				return null;
			}
			// write lock may be acquired already. But this is ok with our custom R/W lock implementation
			readLock.lock();
			try {
				return createResult(orisToGet, null, cacheDirective, targetCache, false, null);
			}
			finally {
				readLock.unlock();
			}
		}
		finally {
			if (releaseWriteLock) {
				writeLock.unlock();
			}
		}
	}

	@Override
	public IList<IObjRelationResult> getObjRelations(List<IObjRelation> objRels,
			Set<CacheDirective> cacheDirective) {
		return getObjRelations(objRels, null, cacheDirective);
	}

	@Override
	public IList<IObjRelationResult> getObjRelations(final List<IObjRelation> objRels,
			final ICacheIntern targetCache, final Set<CacheDirective> cacheDirective) {
		IVerifyOnLoad verifyOnLoad = this.verifyOnLoad;
		if (verifyOnLoad == null) {
			return getObjRelationsIntern(objRels, targetCache, cacheDirective);
		}
		try {
			return verifyOnLoad.verifyEntitiesOnLoad(
					new IResultingBackgroundWorkerDelegate<IList<IObjRelationResult>>() {
						@Override
						public IList<IObjRelationResult> invoke() throws Throwable {
							return getObjRelationsIntern(objRels, targetCache, cacheDirective);
						}
					});
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected IList<IObjRelationResult> getObjRelationsIntern(List<IObjRelation> objRels,
			ICacheIntern targetCache, Set<CacheDirective> cacheDirective) {
		checkNotDisposed();
		boolean isCacheRetrieverCallAllowed = isCacheRetrieverCallAllowed(cacheDirective);
		boolean returnMisses = cacheDirective.contains(CacheDirective.ReturnMisses);

		IEventQueue eventQueue = this.eventQueue;
		if (eventQueue != null) {
			eventQueue.pause(this);
		}
		try {
			Lock readLock = getReadLock();
			final ArrayList<IObjRelation> objRelMisses = new ArrayList<>();
			HashMap<IObjRelation, IObjRelationResult> objRelToResultMap = new HashMap<>();
			IdentityHashMap<IObjRef, IObjRef> alreadyClonedObjRefs = new IdentityHashMap<>();

			ICacheModification cacheModification = this.cacheModification;
			boolean oldCacheModificationValue = cacheModification.isActive();
			boolean acquireSuccess = acquireHardRefTLIfNotAlready(objRels.size());
			cacheModification.setActive(true);
			try {
				IList<IObjRelationResult> result = null;
				readLock.lock();
				try {
					for (int a = 0, size = objRels.size(); a < size; a++) {
						IObjRelation objRel = objRels.get(a);
						if (targetCache != null && targetCache != this) {
							IList<Object> cacheResult =
									targetCache.getObjects(objRel.getObjRefs(), CacheDirective.failEarly());
							if (cacheResult.size() > 0) {
								IObjRefContainer item = (IObjRefContainer) cacheResult.get(0); // Only one hit is
																																								// necessary of
																																								// given group of
																																								// objRefs
								int relationIndex =
										item.get__EntityMetaData().getIndexByRelationName(objRel.getMemberName());
								if (ValueHolderState.INIT == item.get__State(relationIndex)
										|| item.get__ObjRefs(relationIndex) != null) {
									continue;
								}
							}
						}
						IObjRelationResult selfResult =
								getObjRelationIfValid(objRel, targetCache, null, alreadyClonedObjRefs);
						if (selfResult == null && isCacheRetrieverCallAllowed) {
							objRelMisses.add(objRel);
						}
					}
					if (objRelMisses.size() == 0) {
						// Create result WITHOUT releasing the readlock in the meantime
						result = createResult(objRels, targetCache, null, alreadyClonedObjRefs, returnMisses);
					}
				}
				finally {
					readLock.unlock();
				}
				if (objRelMisses.size() > 0) {
					List<IObjRelationResult> loadedObjectRelations;
					if (privileged && securityActivation != null && securityActivation.isFilterActivated()) {
						try {
							loadedObjectRelations = securityActivation.executeWithoutFiltering(
									new IResultingBackgroundWorkerDelegate<List<IObjRelationResult>>() {
										@Override
										public List<IObjRelationResult> invoke() throws Throwable {
											return cacheRetriever.getRelations(objRelMisses);
										}
									});
						}
						catch (Throwable e) {
							throw RuntimeExceptionUtil.mask(e);
						}
					}
					else {
						loadedObjectRelations = cacheRetriever.getRelations(objRelMisses);
					}
					loadObjects(loadedObjectRelations, objRelToResultMap);
					readLock.lock();
					try {
						result = createResult(objRels, targetCache, objRelToResultMap, alreadyClonedObjRefs,
								returnMisses);
					}
					finally {
						readLock.unlock();
					}
				}
				if (isFilteringNecessary(targetCache)) {
					writeLock.lock();
					try {
						result = filterObjRelResult(result, targetCache);
					}
					finally {
						writeLock.unlock();
					}
				}
				return result;
			}
			finally {
				cacheModification.setActive(oldCacheModificationValue);
				clearHardRefs(acquireSuccess);
			}
		}
		finally {
			if (eventQueue != null) {
				eventQueue.resume(this);
			}
		}
	}

	protected IObjRelationResult getObjRelationIfValid(IObjRelation objRel, ICacheIntern targetCache,
			HashMap<IObjRelation, IObjRelationResult> objRelToResultMap,
			IdentityHashMap<IObjRef, IObjRef> alreadyClonedObjRefs) {
		IList<Object> cacheValues = getObjects(new ArrayList<IObjRef>(objRel.getObjRefs()), targetCache,
				failEarlyCacheValueResultSet);
		if (cacheValues.size() == 0) {
			if (objRelToResultMap != null) {
				return objRelToResultMap.get(objRel);
			}
			return null;
		}
		RootCacheValue cacheValue = (RootCacheValue) cacheValues.get(0); // Only first hit is needed
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRel.getRealType());
		int index = metaData.getIndexByRelationName(objRel.getMemberName());
		IObjRef[] objRefs = cacheValue.getRelation(index);

		if (objRefs == null) {
			return null;
		}
		ObjRelationResult objRelResult = new ObjRelationResult();
		objRelResult.setReference(objRel);
		objRelResult.setRelations(cloneObjectRefArray(objRefs, alreadyClonedObjRefs));
		return objRelResult;
	}

	protected IList<IObjRelationResult> createResult(List<IObjRelation> objRels,
			ICacheIntern targetCache, HashMap<IObjRelation, IObjRelationResult> objRelToResultMap,
			IdentityHashMap<IObjRef, IObjRef> alreadyClonedObjRefs, boolean returnMisses) {
		IObjRefHelper oriHelper = this.oriHelper;
		ArrayList<IObjRelationResult> objRelResults = new ArrayList<>(objRels.size());

		for (int a = 0, size = objRels.size(); a < size; a++) {
			IObjRelation objRel = objRels.get(a);
			IList<Object> cacheResult = null;
			if (targetCache != null && targetCache != this) {
				cacheResult = targetCache.getObjects(objRel.getObjRefs(), CacheDirective.failEarly());
			}
			if (cacheResult == null || cacheResult.size() == 0) {
				IObjRelationResult selfResult =
						getObjRelationIfValid(objRel, targetCache, objRelToResultMap, alreadyClonedObjRefs);
				if (selfResult != null || returnMisses) {
					objRelResults.add(selfResult);
				}
				continue;
			}
			IObjRefContainer item = (IObjRefContainer) cacheResult.get(0); // Only first hit is needed
			IEntityMetaData metaData = item.get__EntityMetaData();
			int relationIndex = metaData.getIndexByRelationName(objRel.getMemberName());
			RelationMember member = metaData.getRelationMembers()[relationIndex];

			if (ValueHolderState.INIT != item.get__State(relationIndex)) {
				IObjRef[] objRefs = item.get__ObjRefs(relationIndex);
				if (objRefs != null) {
					ObjRelationResult selfResult = new ObjRelationResult();
					selfResult.setReference(objRel);
					selfResult.setRelations(cloneObjectRefArray(objRefs, alreadyClonedObjRefs));
					objRelResults.add(selfResult);
				}
				else {
					IObjRelationResult selfResult =
							getObjRelationIfValid(objRel, targetCache, objRelToResultMap, alreadyClonedObjRefs);
					if (selfResult != null) {
						IObjRef[] relations = selfResult.getRelations();
						item.set__ObjRefs(relationIndex, relations);
						objRelResults.add(selfResult);
					}
					else if (returnMisses) {
						objRelResults.add(null);
					}
				}
				continue;
			}
			Object memberValue = member.getValue(item);
			if (memberValue == null) {
				if (returnMisses) {
					objRelResults.add(null);
				}
				continue;
			}
			IList<IObjRef> oriList = oriHelper.extractObjRefList(memberValue, null);

			ObjRelationResult selfResult = new ObjRelationResult();
			selfResult.setReference(objRel);
			selfResult.setRelations(oriList.toArray(IObjRef.class));
			objRelResults.add(selfResult);
		}
		return objRelResults;
	}

	protected IList<IObjRelationResult> filterObjRelResult(IList<IObjRelationResult> objRelResults,
			ICacheIntern targetCache) {
		if (objRelResults.size() == 0 || !isFilteringNecessary(targetCache)) {
			return objRelResults;
		}
		ArrayList<IObjRef> permittedObjRefs = new ArrayList<>(objRelResults.size());
		for (int a = 0, size = objRelResults.size(); a < size; a++) {
			IObjRelationResult objRelResult = objRelResults.get(a);
			if (objRelResult == null) {
				permittedObjRefs.add(null);
				continue;
			}
			IObjRef[] objRefsOfReference = objRelResult.getReference().getObjRefs();
			IObjRef primaryObjRef = objRefsOfReference[0];
			for (IObjRef objRefOfReference : objRefsOfReference) {
				if (objRefOfReference.getIdNameIndex() == ObjRef.PRIMARY_KEY_INDEX) {
					primaryObjRef = objRefOfReference;
					break;
				}
			}
			permittedObjRefs.add(primaryObjRef);
		}
		IPrivilege[] privileges = getPrivilegesByObjRefWithoutReadLock(permittedObjRefs);
		HashMap<IObjRef, IntArrayList> relatedObjRefs = new HashMap<>();
		for (int index = permittedObjRefs.size(); index-- > 0;) {
			IPrivilege privilege = privileges[index];
			if (privilege == null || !privilege.isReadAllowed()) {
				permittedObjRefs.set(index, null);
				continue;
			}
			IObjRelationResult objRelResult = objRelResults.get(index);
			IObjRef[] relations = objRelResult.getRelations();
			for (IObjRef relation : relations) {
				IntArrayList intArrayList = relatedObjRefs.get(relation);
				if (intArrayList == null) {
					intArrayList = new IntArrayList();
					relatedObjRefs.put(relation, intArrayList);
				}
				intArrayList.add(index);
			}
		}
		IList<IObjRef> relatedObjRefKeys = relatedObjRefs.keySet().toList();
		privileges = getPrivilegesByObjRefWithoutReadLock(relatedObjRefKeys);
		for (int a = 0, size = relatedObjRefKeys.size(); a < size; a++) {
			IPrivilege privilege = privileges[a];
			if (privilege.isReadAllowed()) {
				continue;
			}
			IObjRef relatedObjRefKey = relatedObjRefKeys.get(a);
			IntArrayList intArrayList = relatedObjRefs.get(relatedObjRefKey);
			for (int b = 0, sizeB = intArrayList.size; b < sizeB; b++) {
				int index = intArrayList.array[b];
				IObjRelationResult objRelResult = objRelResults.get(index);
				IObjRef[] relations = objRelResult.getRelations();
				boolean found = false;
				for (int c = relations.length; c-- > 0;) {
					if (relations[c] != relatedObjRefKey) {
						continue;
					}
					relations[c] = null;
					found = true;
					break;
				}
				if (!found) {
					throw new IllegalStateException("Must never happen");
				}
			}
		}
		for (int a = objRelResults.size(); a-- > 0;) {
			IObjRelationResult objRelResult = objRelResults.get(a);
			if (objRelResult == null) {
				continue;
			}
			IObjRef[] relations = objRelResult.getRelations();
			int count = 0;
			for (int b = relations.length; b-- > 0;) {
				if (relations[b] != null) {
					count++;
				}
			}
			if (count == relations.length) {
				continue;
			}
			IObjRef[] filteredRelations = count > 0 ? new IObjRef[count] : ObjRef.EMPTY_ARRAY;
			int index = 0;
			for (int b = relations.length; b-- > 0;) {
				IObjRef relation = relations[b];
				if (relation != null) {
					filteredRelations[index++] = relation;
				}
			}
			if (index != count) {
				throw new IllegalStateException("Must never happen");
			}
			((ObjRelationResult) objRelResult).setRelations(filteredRelations);
		}
		return objRelResults;
	}

	protected IObjRef[] cloneObjectRefArray(IObjRef[] objRefs,
			IdentityHashMap<IObjRef, IObjRef> alreadyClonedObjRefs) {
		if (objRefs == null || objRefs.length == 0) {
			return objRefs;
		}
		// Deep clone of the ObjRefs is important
		IObjRef[] objRefsClone = new IObjRef[objRefs.length];
		for (int b = objRefs.length; b-- > 0;) {
			IObjRef objRef = objRefs[b];
			if (objRef == null) {
				continue;
			}
			IObjRef objRefClone = alreadyClonedObjRefs.get(objRef);
			if (objRefClone == null) {
				objRefClone = objRefFactory.dup(objRef);
				alreadyClonedObjRefs.put(objRef, objRefClone);
			}
			objRefsClone[b] = objRefClone;
		}
		return objRefsClone;
	}

	protected void loadObjects(List<IObjRelationResult> loadedObjectRelations,
			HashMap<IObjRelation, IObjRelationResult> objRelToResultMap) {
		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try {
			for (int a = 0, size = loadedObjectRelations.size(); a < size; a++) {
				IObjRelationResult objRelResult = loadedObjectRelations.get(a);
				IObjRelation objRel = objRelResult.getReference();

				objRelToResultMap.put(objRel, objRelResult);

				IList<Object> cacheValues =
						getObjects(objRel.getObjRefs(), CacheDirective.cacheValueResult());
				if (cacheValues.size() == 0) {
					continue;
				}
				RootCacheValue cacheValue = (RootCacheValue) cacheValues.get(0); // Only first hit needed

				IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRel.getRealType());
				int index = metaData.getIndexByRelationName(objRel.getMemberName());
				unregisterRelations(cacheValue.getRelation(index), cacheValue);
				IObjRef[] relationsOfMember = objRelResult.getRelations();
				if (relationsOfMember.length == 0) {
					relationsOfMember = ObjRef.EMPTY_ARRAY;
				}
				cacheValue.setRelation(index, relationsOfMember);
				registerRelations(relationsOfMember);
			}
		}
		finally {
			writeLock.unlock();
		}
	}

	protected int waitForConcurrentReadFinish(List<IObjRef> orisToGet,
			RootCacheValue[] rootCacheValuesToGet, ArrayList<IObjRef> orisToLoad,
			Set<CacheDirective> cacheDirective) {
		Lock readLock = getReadLock();
		Lock pendingKeysReadLock = this.pendingKeysReadLock;
		HashSet<IObjRef> currentPendingKeys = this.currentPendingKeys;
		IGuiThreadHelper guiThreadHelper = this.guiThreadHelper;
		while (true) {
			boolean concurrentPendingItems = false;
			boolean releaseReadLock = true;
			readLock.lock();
			pendingKeysReadLock.lock();
			try {
				for (int a = 0, size = orisToGet.size(); a < size; a++) {
					IObjRef oriToGet = orisToGet.get(a);
					if (oriToGet == null) {
						continue;
					}
					if ((cacheDirective.contains(CacheDirective.CacheValueResult)
							|| cacheDirective.contains(CacheDirective.LoadContainerResult))
							&& oriToGet instanceof IDirectObjRef
							&& ((IDirectObjRef) oriToGet).getDirect() != null) {
						throw new IllegalArgumentException(
								IDirectObjRef.class.getName() + " cannot be loaded as CacheValue or LoadContainer");
					}
					RootCacheValue cacheValue = existsValue(oriToGet);
					if (cacheValue != null) {
						rootCacheValuesToGet[a] = cacheValue;
						continue;
					}
					if (currentPendingKeys.contains(oriToGet)) {
						concurrentPendingItems = true;
						orisToLoad.clear();
						break;
					}
					orisToLoad.add(oriToGet);
				}
				if (!concurrentPendingItems && orisToLoad.size() == 0) {
					// Do not release the readlock, to prohibit concurrent DCEs
					releaseReadLock = false;
					return changeVersion;
				}
			}
			finally {
				pendingKeysReadLock.unlock();
				if (releaseReadLock) {
					readLock.unlock();
				}
			}
			if (!concurrentPendingItems) {
				Lock pendingKeysWriteLock = this.pendingKeysWriteLock;
				pendingKeysWriteLock.lock();
				try {
					for (int a = 0, size = orisToLoad.size(); a < size; a++) {
						IObjRef objRef = orisToLoad.get(a);
						currentPendingKeys.add(objRef);
					}
					return changeVersion;
				}
				finally {
					pendingKeysWriteLock.unlock();
				}
			}
			if (guiThreadHelper != null && guiThreadHelper.isInGuiThread()) {
				throw new UnsupportedOperationException(
						"It is not allowed to call to method while within specified"
								+ " synchronisation context. If this error currently occurs on client side maybe you are calling from a GUI thread?");
			}
			synchronized (currentPendingKeys) {
				try {
					currentPendingKeys.wait(5000);
				}
				catch (InterruptedException e) {
					// Intended blank
				}
			}
		}
	}

	protected void loadObjects(List<ILoadContainer> loadedEntities, LinkedHashSet<IObjRef> neededORIs,
			ArrayList<DirectValueHolderRef> pendingValueHolders) {
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try {
			for (int a = 0, size = loadedEntities.size(); a < size; a++) {
				loadObject(loadedEntities.get(a), neededORIs, pendingValueHolders);
			}
		}
		finally {
			writeLock.unlock();
		}
	}

	protected void loadObject(ILoadContainer loadContainer, LinkedHashSet<IObjRef> neededORIs,
			ArrayList<DirectValueHolderRef> pendingValueHolders) {
		IObjRef reference = loadContainer.getReference();

		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(reference.getRealType());
		Object[] primitives = loadContainer.getPrimitives();
		CacheKey[] alternateCacheKeys = extractAlternateCacheKeys(metaData, primitives);

		RootCacheValue cacheValue = putIntern(metaData, null, reference.getId(), reference.getVersion(),
				alternateCacheKeys, primitives, loadContainer.getRelations());
		if (weakEntries) {
			addHardRefTL(cacheValue);
		}
		if (pendingValueHolders != null) {
			ensureRelationsExist(cacheValue, metaData, neededORIs, pendingValueHolders);
		}
	}

	@Override
	protected void putIntern(ILoadContainer loadContainer) {
		loadObject(loadContainer, null, null);
	}

	protected void clearPendingKeysOfCurrentThread(ArrayList<IObjRef> cacheKeysToRemove) {
		if (cacheKeysToRemove.isEmpty()) {
			return;
		}
		Lock pendingKeysWriteLock = this.pendingKeysWriteLock;
		pendingKeysWriteLock.lock();
		try {
			currentPendingKeys.removeAll(cacheKeysToRemove);
		}
		finally {
			pendingKeysWriteLock.unlock();
		}
		synchronized (currentPendingKeys) {
			currentPendingKeys.notifyAll();
		}
	}

	protected boolean isFilteringNecessary(ICacheIntern targetCache) {
		return privilegeProvider != null && securityActive
				&& (isPrivileged() && targetCache != null && !targetCache.isPrivileged())
				|| (targetCache == null && securityActivation != null
						&& securityActivation.isFilterActivated());
	}

	protected IList<Object> createResult(List<IObjRef> objRefsToGet,
			RootCacheValue[] rootCacheValuesToGet, Set<CacheDirective> cacheDirective,
			ICacheIntern targetCache, boolean checkVersion, List<IObjRef> objRefsToLoad) {
		boolean loadContainerResult = cacheDirective.contains(CacheDirective.LoadContainerResult);
		boolean cacheValueResult =
				cacheDirective.contains(CacheDirective.CacheValueResult) || targetCache == this;
		if (targetCache == null && !loadContainerResult && !cacheValueResult) {
			return null;
		}
		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		boolean returnMisses = cacheDirective.contains(CacheDirective.ReturnMisses);

		boolean targetCacheAccess = !loadContainerResult && !cacheValueResult;
		final boolean filteringNecessary = isFilteringNecessary(targetCache);
		int getCount = objRefsToGet.size();
		IPrivilege[] privilegesOfObjRefsToGet = null;
		if (filteringNecessary) {
			IPrivilege[] privileges = getPrivilegesByObjRefWithoutReadLock(objRefsToGet);
			ArrayList<IObjRef> filteredObjRefsToGet = new ArrayList<>(objRefsToGet.size());
			privilegesOfObjRefsToGet = new IPrivilege[objRefsToGet.size()];
			RootCacheValue[] filteredRootCacheValuesToGet =
					rootCacheValuesToGet != null ? new RootCacheValue[objRefsToGet.size()] : null;
			getCount = 0;
			for (int a = 0, size = objRefsToGet.size(); a < size; a++) {
				IPrivilege privilege = privileges[a];
				if (privilege != null && privilege.isReadAllowed()) {
					getCount++;
					filteredObjRefsToGet.add(objRefsToGet.get(a));
					privilegesOfObjRefsToGet[a] = privilege;
					if (rootCacheValuesToGet != null) {
						filteredRootCacheValuesToGet[a] = rootCacheValuesToGet[a];
					}
				}
				else {
					filteredObjRefsToGet.add(null);
				}
			}
			rootCacheValuesToGet = filteredRootCacheValuesToGet;
			objRefsToGet = filteredObjRefsToGet;
		}
		if (getCount == 0) {
			return new ArrayList<>(0);
		}
		IEventQueue eventQueue = this.eventQueue;
		if (targetCacheAccess && eventQueue != null) {
			eventQueue.pause(targetCache);
		}
		try {
			ArrayList<Object> result = new ArrayList<>(objRefsToGet.size());
			ArrayList<IBackgroundWorkerParamDelegate<IdentityHashSet<IObjRef>>> runnables = null;
			ArrayList<IObjRef> tempObjRefList = null;
			IdentityHashMap<IObjRef, IObjRef> alreadyClonedObjRefs = null;
			IdentityHashSet<IObjRef> greyListObjRefs = null;
			for (int a = 0, size = objRefsToGet.size(); a < size; a++) {
				IObjRef objRefToGet = objRefsToGet.get(a);
				if (objRefToGet == null) {
					if (returnMisses) {
						result.add(null);
					}
					continue;
				}
				IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRefToGet.getRealType());

				RootCacheValue cacheValue = rootCacheValuesToGet != null
						? rootCacheValuesToGet[a]
						: getCacheValue(metaData, objRefToGet, checkVersion);
				if (cacheValue == null) // Cache miss
				{
					if (targetCacheAccess) {
						Object cacheHitObject =
								targetCache.getObject(objRefToGet, targetCache, CacheDirective.failEarly());
						if (cacheHitObject != null) {
							result.add(cacheHitObject);
							continue;
						}
					}
					if (returnMisses) {
						result.add(null);
					}
					if (objRefsToLoad != null) {
						objRefsToLoad.add(objRefToGet);
					}
					// But we already loaded before so we can do nothing now
					continue;
				}
				if (loadContainerResult) {
					final IObjRef[][] relations = cacheValue.getRelations();
					final LoadContainer loadContainer = new LoadContainer();
					loadContainer.setReference(objRefFactory.createObjRef(cacheValue));
					loadContainer.setPrimitives(cacheValue.getPrimitives());

					if (relations.length == 0 || !filteringNecessary) {
						loadContainer.setRelations(relations);
						result.add(loadContainer);
						continue;
					}
					if (runnables == null) {
						runnables = new ArrayList<>(size);
						greyListObjRefs = new IdentityHashSet<>();
						alreadyClonedObjRefs = new IdentityHashMap<>();
						tempObjRefList = new ArrayList<>();
					}
					scanForAllKnownRelations(relations, greyListObjRefs);

					final ArrayList<IObjRef> fTempObjRefList = tempObjRefList;
					final IdentityHashMap<IObjRef, IObjRef> fAlreadyClonedObjRefs = alreadyClonedObjRefs;
					runnables.add(new IBackgroundWorkerParamDelegate<IdentityHashSet<IObjRef>>() {
						@Override
						public void invoke(IdentityHashSet<IObjRef> whiteListObjRefs) throws Throwable {
							IObjRef[][] whiteListedRelations =
									filterRelations(relations, whiteListObjRefs, fTempObjRefList);
							for (int b = whiteListedRelations.length; b-- > 0;) {
								whiteListedRelations[b] =
										cloneObjectRefArray(whiteListedRelations[b], fAlreadyClonedObjRefs);
							}
							loadContainer.setRelations(whiteListedRelations);
						}
					});
					result.add(loadContainer);
				}
				else if (cacheValueResult) {
					result.add(cacheValue);
				}
				else {
					if (tempObjRefList == null) {
						tempObjRefList = new ArrayList<>(1);
						tempObjRefList.add(new ObjRef());
					}
					Object cacheHitObject = createObjectFromScratch(metaData, cacheValue, targetCache,
							tempObjRefList, filteringNecessary,
							privilegesOfObjRefsToGet != null ? privilegesOfObjRefsToGet[a] : null);
					result.add(cacheHitObject);
				}
			}
			if (runnables != null) {
				IdentityHashSet<IObjRef> whiteListObjRefs = buildWhiteListedObjRefs(greyListObjRefs);
				try {
					for (int a = runnables.size(); a-- > 0;) {
						IBackgroundWorkerParamDelegate<IdentityHashSet<IObjRef>> runnable = runnables.get(a);
						runnable.invoke(whiteListObjRefs);
					}
				}
				catch (Throwable e) {
					throw RuntimeExceptionUtil.mask(e);
				}
			}
			return result;
		}
		finally {
			if (targetCacheAccess && eventQueue != null) {
				eventQueue.resume(targetCache);
			}
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	protected Object createObjectFromScratch(IEntityMetaData metaData, RootCacheValue cacheValue,
			ICacheIntern targetCache, ArrayList<IObjRef> tempObjRefList, boolean filteringNecessary,
			IPrivilege privilegeOfObjRef) {
		Class<?> entityType = cacheValue.getEntityType();

		IObjRef tempObjRef = tempObjRefList.get(0);
		tempObjRef.setId(cacheValue.getId());
		tempObjRef.setIdNameIndex(ObjRef.PRIMARY_KEY_INDEX);
		tempObjRef.setRealType(entityType);

		Lock targetWriteLock = targetCache.getWriteLock();
		targetWriteLock.lock();
		try {
			Object cacheObject =
					targetCache.getObjects(tempObjRefList, CacheDirective.failEarlyAndReturnMisses()).get(0);
			if (cacheObject != null) {
				// this flag is used from the CacheDataChangeListener to give the cache layers a hint that
				// their are currently in "DCE-processing" mode
				if (!AbstractCache.isFailInCacheHierarchyModeActive()
						&& !((IDataObject) cacheObject).hasPendingChanges()) {
					Object secondLevelVersion = getVersionOfCacheValue(metaData, cacheValue);
					PrimitiveMember versionMember = metaData.getVersionMember();
					Object firstLevelVersion =
							versionMember != null ? versionMember.getValue(cacheObject) : null;

					// the secondLevelVersion (childCache) must not be ==null if the firstLevelVersion is
					// !=null. So we intentionally provoke an NPE here
					if (firstLevelVersion == null
							|| ((Comparable) secondLevelVersion).compareTo(firstLevelVersion) > 0) {
						updateExistingObject(metaData, cacheValue, cacheObject, targetCache, filteringNecessary,
								privilegeOfObjRef);
					}
				}
				return cacheObject;
			}
			cacheObject = targetCache.createCacheValueInstance(metaData, null);
			IPropertyChangeConfigurable pcc = null;
			if (cacheObject instanceof IPropertyChangeConfigurable) {
				// we deactivate the current PCE processing because we just created the entity
				// we know that there is no property change listener that might handle the initial PCEs
				pcc = (IPropertyChangeConfigurable) cacheObject;
				pcc.set__PropertyChangeActive(false);
			}
			updateExistingObject(metaData, cacheValue, cacheObject, targetCache, filteringNecessary,
					privilegeOfObjRef);
			if (pcc != null) {
				pcc.set__PropertyChangeActive(true);
			}
			metaData.postLoad(cacheObject);
			return cacheObject;
		}
		finally {
			targetWriteLock.unlock();
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	protected void updateExistingObject(IEntityMetaData metaData, RootCacheValue cacheValue,
			Object obj, ICacheIntern targetCache, boolean filteringNecessary,
			IPrivilege privilegeOfObjRef) {
		IConversionHelper conversionHelper = this.conversionHelper;
		IObjectCopier objectCopier = this.objectCopier;
		IPrivilegeProvider privilegeProvider = this.privilegeProvider;
		Object id = cacheValue.getId();
		Object version = cacheValue.getVersion();
		metaData.getIdMember().setValue(obj, id);
		if (obj instanceof IParentCacheValueHardRef) {
			((IParentCacheValueHardRef) obj).setParentCacheValueHardRef(cacheValue);
		}
		Member versionMember = metaData.getVersionMember();
		if (versionMember != null) {
			versionMember.setValue(obj, version);
		}
		Member[] primitiveMembers = metaData.getPrimitiveMembers();

		for (int primitiveIndex = primitiveMembers.length; primitiveIndex-- > 0;) {
			Member primitiveMember = primitiveMembers[primitiveIndex];

			Object primitiveTemplate = null;
			if (!filteringNecessary) {
				primitiveTemplate = cacheValue.getPrimitive(primitiveIndex);
			}
			else {
				if (privilegeOfObjRef == null) {
					privilegeOfObjRef = privilegeProvider.getPrivilegeByObjRef(
							new ObjRef(metaData.getEntityType(), ObjRef.PRIMARY_KEY_INDEX, id, version));
				}
				if (privilegeOfObjRef.getPrimitivePropertyPrivilege(primitiveIndex).isReadAllowed()) {
					// current user has no permission to read the property of the given entity
					// so we treat this case as if the property is null/empty anyway
					// effectively we handle user-specific data-blinding this way
					primitiveTemplate = cacheValue.getPrimitive(primitiveIndex);
				}
			}

			if (primitiveTemplate != null && filteringNecessary) {
				if (privilegeOfObjRef == null) {
					privilegeOfObjRef = getPrivilegeByObjRefWithoutReadLock(
							new ObjRef(metaData.getEntityType(), ObjRef.PRIMARY_KEY_INDEX, id, version));
				}
				if (!privilegeOfObjRef.getPrimitivePropertyPrivilege(primitiveIndex).isReadAllowed()) {
					// current user has no permission to read the property of the given entity
					// so we treat this case as if the property is null/empty anyway
					// effectively we handle user-specific data-blinding this way
					primitiveTemplate = null;
				}
			}
			Object primitive = null;

			Class<?> memberType = primitiveMember.getRealType();

			if (Collection.class.isAssignableFrom(memberType)) {
				Collection existingCollection = (Collection) primitiveMember.getValue(obj, false);
				if (existingCollection != null) {
					existingCollection.clear();
					if (primitiveTemplate == null) {
						// intended blank
					}
					else if (objectCopier != null) {
						primitive = objectCopier.clone(primitiveTemplate);
						primitive = conversionHelper.convertValueToType(memberType, primitive);
						existingCollection.addAll((Collection) primitive);
					}
					else {
						primitive = createPrimitiveFromTemplate(memberType, primitiveTemplate);
						existingCollection.addAll((Collection) primitive);
					}
					primitive = existingCollection;
				}
			}
			if (primitive == null) {
				if (primitiveTemplate == null) {
					if (Collection.class.isAssignableFrom(memberType)) {
						primitive = ListUtil.createObservableCollectionOfType(memberType, 0);
					}
					else {
						primitive = null;
					}
				}
				else if (objectCopier != null) {
					primitive = objectCopier.clone(primitiveTemplate);
					primitive = conversionHelper.convertValueToType(memberType, primitive);
				}
				else {
					primitive = createPrimitiveFromTemplate(memberType, primitiveTemplate);
				}
				primitiveMember.setValue(obj, primitive);
			}
			if (primitive instanceof IParentEntityAware) {
				((IParentEntityAware) primitive).setParentEntity(obj, primitiveMember);
			}
		}
		IObjRef[][] relations = cacheValue.getRelations();
		relations = filterRelations(relations, filteringNecessary);
		targetCache.addDirect(metaData, id, version, obj, cacheValue, relations);
	}

	protected IPrivilege getPrivilegeByObjRefWithoutReadLock(IObjRef objRef) {
		Lock readLock = getReadLock();
		LockState lockState = null;
		if (privileged && !readLock.isWriteLockHeld() && readLock.isReadLockHeld()) {
			// release the read lock because the PrivilegeProvider MAY request write lock on the
			// privileged cache during rule evaluation
			lockState = readLock.releaseAllLocks();
		}
		try {
			return privilegeProvider.getPrivilegeByObjRef(objRef);
		}
		finally {
			if (lockState != null) {
				readLock.reacquireLocks(lockState);
			}
		}
	}

	protected IPrivilege[] getPrivilegesByObjRefWithoutReadLock(List<? extends IObjRef> objRefs) {
		Lock readLock = getReadLock();
		LockState lockState = null;
		if (privileged && !readLock.isWriteLockHeld() && readLock.isReadLockHeld()) {
			// release the read lock because the PrivilegeProvider MAY request write lock on the
			// privileged cache during rule evaluation
			lockState = readLock.releaseAllLocks();
		}
		try {
			return privilegeProvider.getPrivilegesByObjRef(objRefs).getPrivileges();
		}
		finally {
			if (lockState != null) {
				readLock.reacquireLocks(lockState);
			}
		}
	}

	protected void scanForAllKnownRelations(IObjRef[][] relations,
			IdentityHashSet<IObjRef> allKnownRelations) {
		for (int a = relations.length; a-- > 0;) {
			IObjRef[] relationsOfMember = relations[a];
			if (relationsOfMember == null) {
				continue;
			}
			for (IObjRef relationOfMember : relationsOfMember) {
				if (relationOfMember == null) {
					continue;
				}
				allKnownRelations.add(relationOfMember);
			}
		}
	}

	protected IdentityHashSet<IObjRef> buildWhiteListedObjRefs(
			IdentityHashSet<IObjRef> greyListObjRefs) {
		IList<IObjRef> greyList = greyListObjRefs.toList();
		IdentityHashSet<IObjRef> whiteListObjRefs = IdentityHashSet.create(greyList.size());
		IPrivilege[] privileges = getPrivilegesByObjRefWithoutReadLock(greyList);
		for (int a = privileges.length; a-- > 0;) {
			IPrivilege privilege = privileges[a];
			if (privilege.isReadAllowed()) {
				whiteListObjRefs.add(greyList.get(a));
			}
		}
		return whiteListObjRefs;
	}

	protected IObjRef[][] filterRelations(IObjRef[][] relations, boolean filteringNecessary) {
		if (relations.length == 0 || !filteringNecessary) {
			return relations;
		}
		IdentityHashSet<IObjRef> allKnownRelations = new IdentityHashSet<>();
		scanForAllKnownRelations(relations, allKnownRelations);
		if (allKnownRelations.size() == 0) {
			// nothing to filter
			return relations;
		}
		IdentityHashSet<IObjRef> whiteListObjRefs = buildWhiteListedObjRefs(allKnownRelations);
		return filterRelations(relations, whiteListObjRefs, null);
	}

	protected IObjRef[][] filterRelations(IObjRef[][] relations,
			IdentityHashSet<IObjRef> whiteListObjRefs, ArrayList<IObjRef> tempList) {
		IObjRef[][] filteredRelations = new IObjRef[relations.length][];

		if (tempList == null) {
			tempList = new ArrayList<>();
		}
		// reuse list instance for performance reasons
		for (int a = relations.length; a-- > 0;) {
			IObjRef[] relationsOfMember = relations[a];
			if (relationsOfMember == null) {
				continue;
			}
			tempList.clear();
			for (IObjRef relationOfMember : relationsOfMember) {
				if (relationOfMember == null) {
					continue;
				}
				if (whiteListObjRefs.contains(relationOfMember)) {
					tempList.add(relationOfMember);
				}
			}
			filteredRelations[a] =
					tempList.size() > 0 ? tempList.toArray(IObjRef.class) : ObjRef.EMPTY_ARRAY;
		}
		return filteredRelations;
	}

	protected Object createPrimitiveFromTemplate(Class<?> expectedType, Object primitiveTemplate) {
		if (expectedType.isArray()) {
			// Deep clone non-empty arrays because they are not immutable like other primitive items
			Class<?> componentType = expectedType.getComponentType();
			if (primitiveTemplate == null) {
				return createArray(componentType, 0);
			}
			else if (primitiveTemplate.getClass().isArray()) {
				int length = Array.getLength(primitiveTemplate);
				if (length == 0) {
					if (primitiveTemplate.getClass().getComponentType().equals(componentType)) {
						// At this point an 'immutable' empty array template may be returned directly
						return primitiveTemplate;
					}
					else {
						return createArray(componentType, 0);
					}
				}
				return copyByValue(primitiveTemplate);
			}
			Object primitive = Array.newInstance(componentType, 1);
			Array.set(primitive, 0, primitiveTemplate);
			return primitive;
		}
		else if (primitiveTemplate != null
				&& expectedType.isAssignableFrom(primitiveTemplate.getClass())) {
			// The template itself matches with the expected type. All we have to do is clone the template
			return copyByValue(primitiveTemplate);
		}
		else if (Collection.class.isAssignableFrom(expectedType)) {
			// Deep clone collections because they are not immutable like other primitive items
			if (primitiveTemplate == null) {
				return ListUtil.createCollectionOfType(expectedType, 0);
			}
			if (primitiveTemplate.getClass().isArray()) {
				int length = Array.getLength(primitiveTemplate);
				Collection<Object> primitive =
						ListUtil.createObservableCollectionOfType(expectedType, length);
				if (length == 0) {
					return primitive;
				}
				// Clone template to access its ITEMS by REFERENCE
				primitiveTemplate = copyByValue(primitiveTemplate);
				for (int a = 0; a < length; a++) {
					Object item = Array.get(primitiveTemplate, a);
					primitive.add(item);
				}
				return primitive;
			}
			else if (primitiveTemplate instanceof Collection) {
				int length = ((Collection<?>) primitiveTemplate).size();
				Collection<Object> primitive = ListUtil.createCollectionOfType(expectedType, length);
				if (length == 0) {
					return primitive;
				}
				// Clone template to access its ITEMS by REFERENCE
				primitiveTemplate = copyByValue(primitiveTemplate);
				if (primitiveTemplate instanceof List) {
					List<?> listPrimitiveTemplate = (List<?>) primitiveTemplate;
					for (int a = 0; a < length; a++) {
						Object item = listPrimitiveTemplate.get(a);
						primitive.add(item);
					}
				}
				else {
					primitive.addAll((Collection<?>) primitiveTemplate);
				}
				return primitive;
			}
			Collection<Object> primitive = ListUtil.createCollectionOfType(expectedType, 1);
			primitive.add(copyByValue(primitiveTemplate));
			return primitive;
		}
		else if (primitiveTemplate == null) {
			return null;
		}
		Object convertedPrimitiveTemplate =
				conversionHelper.convertValueToType(expectedType, primitiveTemplate);
		if (convertedPrimitiveTemplate != primitiveTemplate) {
			return convertedPrimitiveTemplate;
		}
		// To be sure, that the conversion has really no relation with the original at all, we clone it
		return copyByValue(convertedPrimitiveTemplate);
	}

	protected Object createArray(Class<?> componentType, int size) {
		if (size == 0) {
			Object array = typeToEmptyArray.get(componentType);
			if (array == null) {
				array = Array.newInstance(componentType, 0);
			}
			return array;
		}
		return Array.newInstance(componentType, size);
	}

	protected Object copyByValue(Object obj) {
		Class<?> type = obj.getClass();
		if (ImmutableTypeSet.isImmutableType(type)) {
			return obj;
		}
		// VERY SLOW fallback if no IObjectCopier implementation provided
		FastByteArrayOutputStream bos = new FastByteArrayOutputStream();
		try {
			@SuppressWarnings("resource")
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.getRawByteArray(), 0, bos.size());
			ObjectInputStream ois = new ObjectInputStream(bis);
			return ois.readObject();
		}
		catch (IOException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		catch (ClassNotFoundException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected Collection<Object> createCollectionIfRequested(Class<?> expectedType, int size) {
		if (Set.class.isAssignableFrom(expectedType)) {
			return new HashSet<>((int) (size / AbstractHashSet.DEFAULT_LOAD_FACTOR) + 1,
					AbstractHashSet.DEFAULT_LOAD_FACTOR);
		}
		else if (Collection.class.isAssignableFrom(expectedType)) {
			return new ArrayList<>(size);
		}
		return null;
	}

	@Override
	public void addDirect(IEntityMetaData metaData, Object id, Object version,
			Object primitiveFilledObject, Object parentCacheValueOrArray, IObjRef[][] relations) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	protected void cacheValueHasBeenAdded(byte idIndex, Object id, IEntityMetaData metaData,
			Object[] primitives, IObjRef[][] relations, Object cacheValueR) {
		super.cacheValueHasBeenAdded(idIndex, id, metaData, primitives, relations, cacheValueR);

		registerAllRelations(relations);
	}

	@Override
	protected void cacheValueHasBeenRead(Object cacheValueR) {
		super.cacheValueHasBeenRead(cacheValueR);
		int lruThreshold = this.lruThreshold;
		if (lruThreshold == 0) {
			// LRU handling disabled
			return;
		}
		RootCacheValue cacheValue = getCacheValueFromReference(cacheValueR);
		if (cacheValue == null) {
			return;
		}
		InterfaceFastList<RootCacheValue> lruList = this.lruList;
		java.util.concurrent.locks.Lock lruLock = this.lruLock;
		lruLock.lock();
		try {
			lruList.remove(cacheValue);
			lruList.pushFirst(cacheValue);
			while (lruList.size() > lruThreshold) {
				lruList.popLast(); // Ignore result
			}
		}
		finally {
			lruLock.unlock();
		}
	}

	@Override
	protected void cacheValueHasBeenUpdated(IEntityMetaData metaData, Object[] primitives,
			IObjRef[][] relations, Object cacheValueR) {
		super.cacheValueHasBeenUpdated(metaData, primitives, relations, cacheValueR);

		RelationMember[] relationMembers = metaData.getRelationMembers();
		RootCacheValue cacheValue = getCacheValueFromReference(cacheValueR);
		for (int relationIndex = relationMembers.length; relationIndex-- > 0;) {
			unregisterRelations(cacheValue.getRelation(relationIndex), cacheValue);
		}
		registerAllRelations(relations);
	}

	@Override
	protected void cacheValueHasBeenRemoved(IEntityMetaData metaData, byte idIndex, Object id,
			RootCacheValue cacheValue) {
		RelationMember[] relationMembers = metaData.getRelationMembers();
		for (int relationIndex = relationMembers.length; relationIndex-- > 0;) {
			unregisterRelations(cacheValue.getRelation(relationIndex), cacheValue);
		}
		if (lruThreshold == 0) {
			// LRU handling disabled
			return;
		}
		java.util.concurrent.locks.Lock lruLock = this.lruLock;
		lruLock.lock();
		try {
			// Item in lru list
			lruList.remove(cacheValue);
		}
		finally {
			lruLock.unlock();
		}
		super.cacheValueHasBeenRemoved(metaData, idIndex, id, cacheValue);
	}

	@Override
	public void removePriorVersions(List<IObjRef> oris) {
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try {
			for (int a = oris.size(); a-- > 0;) {
				IObjRef ori = oris.get(a);
				super.removePriorVersions(ori);
				updateReferenceVersion(ori);
			}
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public void removePriorVersions(IObjRef ori) {
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try {
			super.removePriorVersions(ori);

			updateReferenceVersion(ori);
		}
		finally {
			writeLock.unlock();
		}
	}

	protected void registerAllRelations(IObjRef[][] relations) {
		if (relations == null) {
			return;
		}
		for (IObjRef[] methodRelations : relations) {
			registerRelations(methodRelations);
		}
	}

	protected void registerRelations(IObjRef[] relations) {
		if (relations == null) {
			return;
		}
		HashMap<IObjRef, Integer> relationOris = this.relationOris;
		for (int i = relations.length; i-- > 0;) {
			IObjRef related = relations[i];
			if (related == null) {
				continue;
			}
			IObjRef existing = relationOris.getKey(related);
			if (existing != null) {
				Integer count = relationOris.get(existing);
				relationOris.put(existing, Integer.valueOf(count.intValue() + 1));

				relations[i] = existing;
			}
			else {
				relationOris.put(related, Integer.valueOf(1));
			}
		}
	}

	protected void unregisterAllRelations(IObjRef[][] relations) {
		if (relations == null) {
			return;
		}
		for (IObjRef[] methodRelations : relations) {
			unregisterRelations(methodRelations, null);
		}
	}

	protected void unregisterRelations(IObjRef[] relations, RootCacheValue cacheValue) {
		if (relations == null) {
			return;
		}
		HashMap<IObjRef, Integer> relationOris = this.relationOris;
		for (int i = relations.length; i-- > 0;) {
			IObjRef related = relations[i];
			Integer count = relationOris.get(related);
			if (count == null) {
				log.warn("Potential inconsistency in RootCache: ObjRef unknown: '" + related + "' used in '"
						+ cacheValue + "'");
				continue;
			}
			if (count == 1) {
				relationOris.remove(related);
			}
			else {
				relationOris.put(related, Integer.valueOf(count.intValue() - 1));
			}
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	protected void updateReferenceVersion(IObjRef ori) {
		Object version = ori.getVersion();
		if (version == null) {
			return;
		}
		IObjRef existing = relationOris.getKey(ori);
		if (existing == null) {
			return;
		}

		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(ori.getRealType());
		Member versionMember = metaData.getVersionMember();
		if (versionMember == null) {
			return;
		}
		IConversionHelper conversionHelper = this.conversionHelper;
		Object cacheVersion =
				conversionHelper.convertValueToType(versionMember.getElementType(), existing.getVersion());
		Object currentVersion =
				conversionHelper.convertValueToType(versionMember.getElementType(), version);

		if (cacheVersion == null || ((Comparable) cacheVersion).compareTo(currentVersion) < 0) {
			existing.setVersion(currentVersion);
		}
	}

	@Override
	protected CacheKey[] getAlternateCacheKeysFromCacheValue(IEntityMetaData metaData,
			RootCacheValue cacheValue) {
		return extractAlternateCacheKeys(metaData, cacheValue);
	}

	protected void ensureRelationsExist(RootCacheValue cacheValue, IEntityMetaData metaData,
			LinkedHashSet<IObjRef> cascadeNeededORIs,
			ArrayList<DirectValueHolderRef> pendingValueHolders) {
		RelationMember[] relationMembers = metaData.getRelationMembers();
		IObjRef[][] relations = cacheValue.getRelations();
		for (int a = relations.length; a-- > 0;) {
			IObjRef[] relationsOfMember = relations[a];

			RelationMember relationMember = relationMembers[a];

			CascadeLoadMode loadCascadeMode = relationMember.getCascadeLoadMode();
			switch (loadCascadeMode) {
				case DEFAULT:
				case LAZY:
					break;
				case EAGER: {
					// Ensure the related RootCacheValues will be loaded - we do not bother here if the
					// relations are
					// known or not yet
					pendingValueHolders.add(new IndirectValueHolderRef(cacheValue, relationMember, this));
					break;
				}
				case EAGER_VERSION: {
					if (relationsOfMember != null) {
						// ObjRefs already loaded. Nothing to do
						continue;
					}
					pendingValueHolders
							.add(new IndirectValueHolderRef(cacheValue, relationMember, this, true));
					break;
				}
				default:
					throw RuntimeExceptionUtil.createEnumNotSupportedException(loadCascadeMode);
			}
		}
	}

	@Override
	public boolean applyValues(Object targetObject, ICacheIntern targetCache, IPrivilege privilege) {
		if (targetObject == null) {
			return false;
		}
		IEntityMetaData metaData = ((IEntityMetaDataHolder) targetObject).get__EntityMetaData();
		Object id = metaData.getIdMember().getValue(targetObject, false);
		RootCacheValue cacheValue = getCacheValue(metaData, ObjRef.PRIMARY_KEY_INDEX, id);
		if (cacheValue == null) // Cache miss
		{
			return false;
		}
		updateExistingObject(metaData, cacheValue, targetObject, targetCache,
				isFilteringNecessary(targetCache), privilege);
		return true;
	}

	@Override
	public void getContent(final HandleContentDelegate handleContentDelegate) {
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try {
			for (CacheMapEntry entry : keyToCacheValueDict) {
				RootCacheValue cacheValue = getCacheValueFromReference(entry.getValue());
				if (cacheValue == null) {
					continue;
				}
				handleContentDelegate.invoke(entry.getEntityType(), entry.getIdIndex(), entry.getId(),
						cacheValue);
			}
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	protected Class<?> getEntityTypeOfObject(Object obj) {
		if (obj instanceof RootCacheValue) {
			return ((RootCacheValue) obj).getEntityType();
		}
		return super.getEntityTypeOfObject(obj);
	}

	@Override
	protected Object getIdOfObject(IEntityMetaData metaData, Object obj) {
		if (obj instanceof RootCacheValue) {
			return ((RootCacheValue) obj).getId();
		}
		return super.getIdOfObject(metaData, obj);
	}

	@Override
	protected Object getVersionOfObject(IEntityMetaData metaData, Object obj) {
		if (obj instanceof RootCacheValue) {
			return ((RootCacheValue) obj).getVersion();
		}
		return super.getVersionOfObject(metaData, obj);
	}

	@Override
	protected Object[] extractPrimitives(IEntityMetaData metaData, Object obj) {
		if (obj instanceof RootCacheValue) {
			return ((RootCacheValue) obj).getPrimitives();
		}
		return super.extractPrimitives(metaData, obj);
	}

	@Override
	protected IObjRef[][] extractRelations(IEntityMetaData metaData, Object obj,
			List<Object> relationValues) {
		if (obj instanceof RootCacheValue) {
			return ((RootCacheValue) obj).getRelations();
		}
		return super.extractRelations(metaData, obj, relationValues);
	}

	@Override
	protected void clearIntern() {
		super.clearIntern();
		relationOris.clear();
		java.util.concurrent.locks.Lock lruLock = this.lruLock;
		lruLock.lock();
		try {
			lruList.clear();
		}
		finally {
			lruLock.unlock();
		}
	}

	@Override
	protected int doCleanUpIntern() {
		int cleanupCount = super.doCleanUpIntern();
		if (cleanupCount > 0 && System.currentTimeMillis()
				- lastRelationObjRefsRefreshTime >= relationObjRefsRefreshThrottleOnGC) {
			doRelationObjRefsRefresh();
		}
		return cleanupCount;
	}

	@Override
	protected void putInternObjRelation(RootCacheValue cacheValue, IEntityMetaData metaData,
			IObjRelation objRelation, IObjRef[] relationsOfMember) {
		int relationIndex = metaData.getIndexByRelationName(objRelation.getMemberName());
		if (relationsOfMember.length == 0) {
			relationsOfMember = ObjRef.EMPTY_ARRAY;
		}
		cacheValue.setRelation(relationIndex, relationsOfMember);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public List<ILoadContainer> getEntities(List<IObjRef> orisToLoad) {
		IList result = getObjects(orisToLoad, CacheDirective.loadContainerResult());
		return result;
	}

	@Override
	public List<IObjRelationResult> getRelations(List<IObjRelation> objRelations) {
		return getObjRelations(objRelations, CacheDirective.none());
	}

	@Override
	public void beginOnline() {
		clear();
	}

	@Override
	public void handleOnline() {
		// Intended blank
	}

	@Override
	public void endOnline() {
		// Intended blank
	}

	@Override
	public void beginOffline() {
		clear();
	}

	@Override
	public void handleOffline() {
		// Intended blank
	}

	@Override
	public void endOffline() {
		// Intended blank
	}

	@Override
	public void assignEntityToCache(Object entity) {
		throw new UnsupportedOperationException();
	}

	protected void doRelationObjRefsRefresh() {
		lastRelationObjRefsRefreshTime = System.currentTimeMillis();
		if (!weakEntries) {
			return;
		}
		Integer zero = Integer.valueOf(0);
		for (Entry<IObjRef, Integer> entry : relationOris) {
			entry.setValue(zero);
		}
		final IdentityHashSet<RootCacheValue> alreadyHandledSet =
				IdentityHashSet.create(keyToCacheValueDict.size());
		getContent(new HandleContentDelegate() {
			@Override
			public void invoke(Class<?> entityType, byte idIndex, Object id, Object value) {
				RootCacheValue cacheValue = (RootCacheValue) value;
				if (!alreadyHandledSet.add(cacheValue)) {
					return;
				}
				IEntityMetaData metaData = cacheValue.get__EntityMetaData();
				for (int relationIndex = metaData.getRelationMembers().length; relationIndex-- > 0;) {
					registerRelations(cacheValue.getRelation(relationIndex));
				}
			}
		});

		Iterator<Entry<IObjRef, Integer>> iter = relationOris.iterator();
		while (iter.hasNext()) {
			Entry<IObjRef, Integer> entry = iter.next();
			if (entry.getValue() == zero) {
				iter.remove();
			}
		}
	}
}
