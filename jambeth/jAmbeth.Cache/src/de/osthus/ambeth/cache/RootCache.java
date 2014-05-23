package de.osthus.ambeth.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.cglib.reflect.FastConstructor;
import de.osthus.ambeth.annotation.CascadeLoadMode;
import de.osthus.ambeth.cache.collections.CacheMapEntry;
import de.osthus.ambeth.cache.config.CacheConfigurationConstants;
import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.cache.rootcachevalue.IRootCacheValueTypeProvider;
import de.osthus.ambeth.cache.rootcachevalue.RootCacheValue;
import de.osthus.ambeth.cache.transfer.LoadContainer;
import de.osthus.ambeth.cache.transfer.ObjRelationResult;
import de.osthus.ambeth.collections.AbstractHashSet;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.collections.InterfaceFastList;
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.copy.IObjectCopier;
import de.osthus.ambeth.event.IEventQueue;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.merge.model.IDirectObjRef;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.service.ICacheRetriever;
import de.osthus.ambeth.service.IOfflineListener;
import de.osthus.ambeth.template.ValueHolderContainerTemplate;
import de.osthus.ambeth.threading.IGuiThreadHelper;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.util.DirectValueHolderRef;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.IPrefetchHelper;
import de.osthus.ambeth.util.ImmutableTypeSet;
import de.osthus.ambeth.util.IndirectValueHolderRef;
import de.osthus.ambeth.util.ListUtil;
import de.osthus.ambeth.util.Lock;
import de.osthus.ambeth.util.LockState;
import de.osthus.ambeth.util.ParamHolder;
import de.osthus.ambeth.util.ReadWriteLock;

public class RootCache extends AbstractCache<RootCacheValue> implements IRootCache, IOfflineListener
{
	protected static final Map<Class<?>, Object> typeToEmptyArray = new HashMap<Class<?>, Object>(128, 0.5f);

	public static final Set<CacheDirective> failEarlyCacheValueResultSet = EnumSet.of(CacheDirective.FailEarly, CacheDirective.CacheValueResult);

	static
	{
		List<Class<?>> types = new ArrayList<Class<?>>();
		ImmutableTypeSet.addImmutableTypesTo(types);
		types.add(Object.class);
		for (Class<?> type : types)
		{
			if (!void.class.equals(type))
			{
				createEmptyArrayEntry(type);
			}
		}
	}

	protected static void createEmptyArrayEntry(Class<?> componentType)
	{
		typeToEmptyArray.put(componentType, Array.newInstance(componentType, 0));
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final HashMap<IObjRef, Integer> relationOris = new HashMap<IObjRef, Integer>();

	protected final HashSet<IObjRef> currentPendingKeys = new HashSet<IObjRef>();

	protected final InterfaceFastList<RootCacheValue> lruList = new InterfaceFastList<RootCacheValue>();

	protected final ReentrantLock lruLock = new ReentrantLock();

	@Property(name = CacheConfigurationConstants.CacheLruThreshold, defaultValue = "0")
	protected int lruThreshold;

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
	protected IObjRefHelper oriHelper;

	@Autowired
	protected IPrefetchHelper prefetchHelper;

	@Autowired
	protected IRootCacheValueTypeProvider rootCacheValueTypeProvider;

	@Autowired
	protected ValueHolderContainerTemplate valueHolderContainerTemplate;

	protected final Lock pendingKeysReadLock, pendingKeysWriteLock;

	public RootCache()
	{
		ReadWriteLock pendingKeysRwLock = new ReadWriteLock();
		pendingKeysReadLock = pendingKeysRwLock.getReadLock();
		pendingKeysWriteLock = pendingKeysRwLock.getWriteLock();
	}

	@Override
	public void dispose()
	{
		cacheFactory = null;
		cacheModification = null;
		cacheRetriever = null;
		eventQueue = null;
		objectCopier = null;
		oriHelper = null;
		prefetchHelper = null;
		valueHolderContainerTemplate = null;

		super.dispose();
	}

	@Override
	public int getCacheId()
	{
		return -1;
	}

	@Override
	public void setCacheId(int cacheId)
	{
		throw new UnsupportedOperationException();
	}

	@Property(name = CacheConfigurationConstants.SecondLevelCacheWeakActive, defaultValue = "true")
	@Override
	public void setWeakEntries(boolean weakEntries)
	{
		super.setWeakEntries(weakEntries);
	}

	@Override
	protected boolean allowCacheValueReplacement()
	{
		return true;
	}

	@Override
	public RootCacheValue createCacheValueInstance(IEntityMetaData metaData, Object obj)
	{
		Class<?> entityType = metaData.getEntityType();
		FastConstructor constructor = rootCacheValueTypeProvider.getRootCacheValueType(entityType);
		try
		{
			return (RootCacheValue) constructor.newInstance(new Object[] { entityType });
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Override
	protected Object getIdOfCacheValue(IEntityMetaData metaData, RootCacheValue cacheValue)
	{
		return cacheValue.getId();
	}

	@Override
	protected void setIdOfCacheValue(IEntityMetaData metaData, RootCacheValue cacheValue, Object id)
	{
		cacheValue.setId(id);
	}

	@Override
	protected Object getVersionOfCacheValue(IEntityMetaData metaData, RootCacheValue cacheValue)
	{
		return cacheValue.getVersion();
	}

	@Override
	protected void setVersionOfCacheValue(IEntityMetaData metaData, RootCacheValue cacheValue, Object version)
	{
		cacheValue.setVersion(version);
	}

	@Override
	protected void setRelationsOfCacheValue(IEntityMetaData metaData, RootCacheValue cacheValue, Object[] primitives, IObjRef[][] relations)
	{
		cacheValue.setPrimitives(primitives);
		cacheValue.setRelations(relations);
	}

	@Override
	public boolean isResultCloned()
	{
		// A rootcache always clones objects by itself
		return true;
	}

	@Override
	public IList<Object> getObjects(List<IObjRef> orisToGet, Set<CacheDirective> cacheDirective)
	{
		checkNotDisposed();
		if (orisToGet == null || orisToGet.size() == 0)
		{
			return EmptyList.getInstance();
		}
		if (cacheDirective.contains(CacheDirective.NoResult) || cacheDirective.contains(CacheDirective.LoadContainerResult)
				|| cacheDirective.contains(CacheDirective.CacheValueResult))
		{
			return getObjects(orisToGet, null, cacheDirective);
		}
		ICacheIntern targetCache = (ICacheIntern) cacheFactory.create(CacheFactoryDirective.SubscribeTransactionalDCE);
		return getObjects(orisToGet, targetCache, cacheDirective);
	}

	@Override
	public Object getObject(IObjRef oriToGet, ICacheIntern targetCache, Set<CacheDirective> cacheDirective)
	{
		checkNotDisposed();
		if (oriToGet == null)
		{
			return null;
		}
		ArrayList<IObjRef> orisToGet = new ArrayList<IObjRef>(1);
		orisToGet.add(oriToGet);
		List<Object> objects = getObjects(orisToGet, targetCache, cacheDirective);
		if (objects.isEmpty())
		{
			return null;
		}
		return objects.get(0);
	}

	@Override
	public IList<Object> getObjects(List<IObjRef> orisToGet, ICacheIntern targetCache, Set<CacheDirective> cacheDirective)
	{
		checkNotDisposed();
		if (orisToGet == null || orisToGet.size() == 0)
		{
			return EmptyList.getInstance();
		}
		if (cacheDirective == null)
		{
			cacheDirective = Collections.<CacheDirective> emptySet();
		}
		IEventQueue eventQueue = this.eventQueue;
		if (eventQueue != null)
		{
			eventQueue.pause(this);
		}
		try
		{
			Lock readLock = getReadLock();
			Lock writeLock = getWriteLock();
			ICacheModification cacheModification = this.cacheModification;
			boolean oldCacheModificationValue = cacheModification.isActive();
			boolean acquireSuccess = acquireHardRefTLIfNotAlready(orisToGet.size());
			cacheModification.setActive(true);
			try
			{
				if (cacheDirective.contains(CacheDirective.FailEarly) || cacheDirective.contains(CacheDirective.FailInCacheHierarchy) || cacheRetriever == null
						|| AbstractCache.isFailEarlyModeActive())
				{
					readLock.lock();
					try
					{
						return createResult(orisToGet, null, cacheDirective, targetCache, true);
					}
					finally
					{
						readLock.unlock();
					}
				}

				LockState lockState = writeLock.releaseAllLocks();
				ParamHolder<Boolean> doAnotherRetry = new ParamHolder<Boolean>();
				try
				{
					while (true)
					{
						doAnotherRetry.setValue(Boolean.FALSE);
						LinkedHashSet<IObjRef> neededObjRefs = new LinkedHashSet<IObjRef>();
						ArrayList<DirectValueHolderRef> pendingValueHolders = new ArrayList<DirectValueHolderRef>();
						IList<Object> result = getObjectsRetry(orisToGet, targetCache, cacheDirective, doAnotherRetry, neededObjRefs, pendingValueHolders);
						while (neededObjRefs.size() > 0)
						{
							IList<IObjRef> objRefsToGetCascade = neededObjRefs.toList();
							neededObjRefs.clear();
							getObjectsRetry(objRefsToGetCascade, targetCache, cacheDirective, doAnotherRetry, neededObjRefs, pendingValueHolders);
						}
						if (Boolean.TRUE.equals(doAnotherRetry.getValue()))
						{
							continue;
						}
						if (pendingValueHolders.size() > 0)
						{
							prefetchHelper.prefetch(pendingValueHolders);
							continue;
						}
						return result;
					}
				}
				finally
				{
					writeLock.reacquireLocks(lockState);
				}
			}
			finally
			{
				cacheModification.setActive(oldCacheModificationValue);
				clearHardRefs(acquireSuccess);
			}
		}
		finally
		{
			if (eventQueue != null)
			{
				eventQueue.resume(this);
			}
		}
	}

	protected IList<Object> getObjectsRetry(List<IObjRef> orisToGet, ICacheIntern targetCache, Set<CacheDirective> cacheDirective,
			ParamHolder<Boolean> doAnotherRetry, LinkedHashSet<IObjRef> neededObjRefs, ArrayList<DirectValueHolderRef> pendingValueHolders)
	{
		ArrayList<IObjRef> orisToLoad = new ArrayList<IObjRef>();
		RootCacheValue[] rootCacheValuesToGet = new RootCacheValue[orisToGet.size()];
		Lock readLock = getReadLock();
		Lock writeLock = getWriteLock();
		int cacheVersionBeforeLongTimeAction = waitForConcurrentReadFinish(orisToGet, rootCacheValuesToGet, orisToLoad, cacheDirective);

		if (orisToLoad.size() == 0)
		{
			// Everything found in the cache. We STILL hold the readlock so we can immediately create the result
			// We already even checked the version. So we do not bother version anymore here
			try
			{
				return createResult(orisToGet, rootCacheValuesToGet, cacheDirective, targetCache, false);
			}
			finally
			{
				readLock.unlock();
			}
		}
		int cacheVersionAfterLongTimeAction;
		boolean releaseWriteLock = false;
		try
		{
			boolean loadSuccess = false;
			try
			{
				List<ILoadContainer> loadedEntities = cacheRetriever.getEntities(orisToLoad);

				// Acquire write lock and mark this state. In the finally-Block the writeLock
				// has to be released in a deterministic way
				writeLock.lock();
				releaseWriteLock = true;

				cacheVersionAfterLongTimeAction = changeVersion;
				loadObjects(loadedEntities, neededObjRefs, pendingValueHolders);

				loadSuccess = true;

				clearPendingKeysOfCurrentThread(orisToLoad);
				orisToLoad.clear();

				if (neededObjRefs.size() > 0 || pendingValueHolders.size() > 0)
				{
					writeLock.unlock();
					releaseWriteLock = false;
					return null;
				}
			}
			finally
			{
				if (!loadSuccess)
				{
					clearPendingKeysOfCurrentThread(orisToLoad);
				}
			}
			if (cacheVersionAfterLongTimeAction != cacheVersionBeforeLongTimeAction)
			{
				// Another thread did some changes (possibly DataChange-Remove actions)
				// We have to ensure that our result-scope is still valid
				// We return null to allow a further full retry of getObjects()
				doAnotherRetry.setValue(Boolean.TRUE);
				return null;
			}
			// write lock may be acquired already. But this is ok with our custom R/W lock implementation
			readLock.lock();
			try
			{
				return createResult(orisToGet, null, cacheDirective, targetCache, false);
			}
			finally
			{
				readLock.unlock();
			}
		}
		finally
		{
			if (releaseWriteLock)
			{
				writeLock.unlock();
			}
		}
	}

	@Override
	public IList<IObjRelationResult> getObjRelations(List<IObjRelation> objRels, Set<CacheDirective> cacheDirective)
	{
		return getObjRelations(objRels, null, cacheDirective);
	}

	@Override
	public IList<IObjRelationResult> getObjRelations(List<IObjRelation> objRels, ICacheIntern targetCache, Set<CacheDirective> cacheDirective)
	{
		checkNotDisposed();
		boolean failEarly = cacheDirective.contains(CacheDirective.FailEarly) || cacheDirective.contains(CacheDirective.FailInCacheHierarchy);
		boolean returnMisses = cacheDirective.contains(CacheDirective.ReturnMisses);
		IEventQueue eventQueue = this.eventQueue;
		if (eventQueue != null)
		{
			eventQueue.pause(this);
		}
		try
		{
			Lock readLock = getReadLock();
			ArrayList<IObjRelation> objRelMisses = new ArrayList<IObjRelation>();
			HashMap<IObjRelation, IObjRelationResult> objRelToResultMap = new HashMap<IObjRelation, IObjRelationResult>();
			IdentityHashMap<IObjRef, ObjRef> alreadyClonedObjRefs = new IdentityHashMap<IObjRef, ObjRef>();

			ICacheModification cacheModification = this.cacheModification;
			boolean oldCacheModificationValue = cacheModification.isActive();
			boolean acquireSuccess = acquireHardRefTLIfNotAlready(objRels.size());
			cacheModification.setActive(true);
			try
			{
				readLock.lock();
				try
				{
					for (int a = 0, size = objRels.size(); a < size; a++)
					{
						IObjRelation objRel = objRels.get(a);
						if (targetCache != null)
						{
							IList<Object> cacheResult = targetCache.getObjects(objRel.getObjRefs(), CacheDirective.failEarly());
							if (cacheResult.size() > 0)
							{
								Object item = cacheResult.get(0); // Only one hit is necessary of given group of objRefs
								IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRel.getRealType());
								IRelationInfoItem member = (IRelationInfoItem) metaData.getMemberByName(objRel.getMemberName());
								if (proxyHelper.isInitialized(item, member) || proxyHelper.getObjRefs(item, member) != null)
								{
									continue;
								}
							}
						}
						IObjRelationResult selfResult = getObjRelationIfValid(objRel, null, alreadyClonedObjRefs);
						if (selfResult == null && !failEarly)
						{
							objRelMisses.add(objRel);
						}
					}
					if (objRelMisses.size() == 0)
					{
						// Create result WITHOUT releasing the readlock in the meantime
						return createResult(objRels, targetCache, null, alreadyClonedObjRefs, returnMisses);
					}
				}
				finally
				{
					readLock.unlock();
				}
				if (objRelMisses.size() > 0)
				{
					List<IObjRelationResult> loadedObjectRelations = cacheRetriever.getRelations(objRelMisses);
					loadObjects(loadedObjectRelations, objRelToResultMap);
				}
				readLock.lock();
				try
				{
					return createResult(objRels, targetCache, objRelToResultMap, alreadyClonedObjRefs, returnMisses);
				}
				finally
				{
					readLock.unlock();
				}
			}
			finally
			{
				cacheModification.setActive(oldCacheModificationValue);
				clearHardRefs(acquireSuccess);
			}
		}
		finally
		{
			if (eventQueue != null)
			{
				eventQueue.resume(this);
			}
		}
	}

	protected IObjRelationResult getObjRelationIfValid(IObjRelation objRel, HashMap<IObjRelation, IObjRelationResult> objRelToResultMap,
			IdentityHashMap<IObjRef, ObjRef> alreadyClonedObjRefs)
	{
		IList<Object> cacheValues = getObjects(objRel.getObjRefs(), failEarlyCacheValueResultSet);
		if (cacheValues.size() == 0)
		{
			if (objRelToResultMap != null)
			{
				return objRelToResultMap.get(objRel);
			}
			return null;
		}
		RootCacheValue cacheValue = (RootCacheValue) cacheValues.get(0); // Only first hit is needed
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRel.getRealType());
		int index = metaData.getIndexByRelationName(objRel.getMemberName());
		IObjRef[] objRefs = cacheValue.getRelation(index);

		if (objRefs == null)
		{
			return null;
		}
		ObjRelationResult objRelResult = new ObjRelationResult();
		objRelResult.setReference(objRel);
		objRelResult.setRelations(cloneObjectRefArray(objRefs, alreadyClonedObjRefs));
		return objRelResult;
	}

	protected IList<IObjRelationResult> createResult(List<IObjRelation> objRels, ICacheIntern targetCache,
			HashMap<IObjRelation, IObjRelationResult> objRelToResultMap, IdentityHashMap<IObjRef, ObjRef> alreadyClonedObjRefs, boolean returnMisses)
	{
		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		IObjRefHelper oriHelper = this.oriHelper;
		IProxyHelper proxyHelper = this.proxyHelper;
		ArrayList<IObjRelationResult> objRelResults = new ArrayList<IObjRelationResult>(objRels.size());

		for (int a = 0, size = objRels.size(); a < size; a++)
		{
			IObjRelation objRel = objRels.get(a);
			IList<Object> cacheResult = null;
			if (targetCache != null)
			{
				cacheResult = targetCache.getObjects(objRel.getObjRefs(), CacheDirective.failEarly());
			}
			if (cacheResult == null || cacheResult.size() == 0)
			{
				IObjRelationResult selfResult = getObjRelationIfValid(objRel, objRelToResultMap, alreadyClonedObjRefs);
				if (selfResult != null || returnMisses)
				{
					objRelResults.add(selfResult);
				}
				continue;
			}
			Object item = cacheResult.get(0); // Only first hit is needed
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRel.getRealType());
			IRelationInfoItem member = (IRelationInfoItem) metaData.getMemberByName(objRel.getMemberName());

			if (!proxyHelper.isInitialized(item, member))
			{
				IObjRef[] objRefs = proxyHelper.getObjRefs(item, member);
				if (objRefs != null)
				{
					ObjRelationResult selfResult = new ObjRelationResult();
					selfResult.setReference(objRel);
					selfResult.setRelations(cloneObjectRefArray(objRefs, alreadyClonedObjRefs));
					objRelResults.add(selfResult);
				}
				else
				{
					IObjRelationResult selfResult = getObjRelationIfValid(objRel, objRelToResultMap, alreadyClonedObjRefs);
					if (selfResult != null || returnMisses)
					{
						objRelResults.add(selfResult);
					}
				}
				continue;
			}
			Object memberValue = member.getValue(item);
			if (memberValue == null)
			{
				if (returnMisses)
				{
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

	protected IObjRef[] cloneObjectRefArray(IObjRef[] objRefs, IdentityHashMap<IObjRef, ObjRef> alreadyClonedObjRefs)
	{
		if (objRefs == null || objRefs.length == 0)
		{
			return objRefs;
		}
		// Deep clone of the ObjRefs is important
		IObjRef[] objRefsClone = new IObjRef[objRefs.length];
		for (int b = objRefs.length; b-- > 0;)
		{
			IObjRef objRef = objRefs[b];
			if (objRef == null)
			{
				continue;
			}
			ObjRef objRefClone = alreadyClonedObjRefs.get(objRef);
			if (objRefClone == null)
			{
				objRefClone = new ObjRef(objRef.getRealType(), objRef.getIdNameIndex(), objRef.getId(), objRef.getVersion());
				alreadyClonedObjRefs.put(objRef, objRefClone);
			}
			objRefsClone[b] = objRefClone;
		}
		return objRefsClone;
	}

	protected void loadObjects(List<IObjRelationResult> loadedObjectRelations, HashMap<IObjRelation, IObjRelationResult> objRelToResultMap)
	{
		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			for (int a = 0, size = loadedObjectRelations.size(); a < size; a++)
			{
				IObjRelationResult objRelResult = loadedObjectRelations.get(a);
				IObjRelation objRel = objRelResult.getReference();

				objRelToResultMap.put(objRel, objRelResult);

				IList<Object> cacheValues = getObjects(objRel.getObjRefs(), failEarlyCacheValueResultSet);
				if (cacheValues.size() == 0)
				{
					continue;
				}
				RootCacheValue cacheValue = (RootCacheValue) cacheValues.get(0); // Only first hit needed
				IObjRef[][] relations = cacheValue.getRelations();

				IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRel.getRealType());
				int index = metaData.getIndexByRelationName(objRel.getMemberName());
				unregisterRelations(relations[index]);
				IObjRef[] relationsOfMember = objRelResult.getRelations();
				if (relationsOfMember.length == 0)
				{
					relationsOfMember = ObjRef.EMPTY_ARRAY;
				}
				relations[index] = relationsOfMember;
				cacheValue.setRelation(index, relationsOfMember);
				registerRelations(relations[index]);
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected int waitForConcurrentReadFinish(List<IObjRef> orisToGet, RootCacheValue[] rootCacheValuesToGet, ArrayList<IObjRef> orisToLoad,
			Set<CacheDirective> cacheDirective)
	{
		Lock readLock = getReadLock();
		Lock pendingKeysReadLock = this.pendingKeysReadLock;
		HashSet<IObjRef> currentPendingKeys = this.currentPendingKeys;
		IGuiThreadHelper guiThreadHelper = this.guiThreadHelper;
		while (true)
		{
			boolean concurrentPendingItems = false;
			boolean releaseReadLock = true;
			readLock.lock();
			pendingKeysReadLock.lock();
			try
			{
				for (int a = 0, size = orisToGet.size(); a < size; a++)
				{
					IObjRef oriToGet = orisToGet.get(a);
					if (oriToGet == null)
					{
						continue;
					}
					if ((cacheDirective.contains(CacheDirective.CacheValueResult) || cacheDirective.contains(CacheDirective.LoadContainerResult))
							&& oriToGet instanceof IDirectObjRef && ((IDirectObjRef) oriToGet).getDirect() != null)
					{
						throw new IllegalArgumentException(IDirectObjRef.class.getName() + " cannot be loaded as CacheValue or LoadContainer");
					}
					RootCacheValue cacheValue = existsValue(oriToGet);
					if (cacheValue != null)
					{
						rootCacheValuesToGet[a] = cacheValue;
						continue;
					}
					if (currentPendingKeys.contains(oriToGet))
					{
						concurrentPendingItems = true;
						orisToLoad.clear();
						break;
					}
					orisToLoad.add(oriToGet);
				}
				if (!concurrentPendingItems && orisToLoad.size() == 0)
				{
					// Do not release the readlock, to prohibit concurrent DCEs
					releaseReadLock = false;
					return changeVersion;
				}
			}
			finally
			{
				pendingKeysReadLock.unlock();
				if (releaseReadLock)
				{
					readLock.unlock();
				}
			}
			if (!concurrentPendingItems)
			{
				Lock pendingKeysWriteLock = this.pendingKeysWriteLock;
				pendingKeysWriteLock.lock();
				try
				{
					for (int a = 0, size = orisToLoad.size(); a < size; a++)
					{
						IObjRef objRef = orisToLoad.get(a);
						currentPendingKeys.add(objRef);
					}
					return changeVersion;
				}
				finally
				{
					pendingKeysWriteLock.unlock();
				}
			}
			if (guiThreadHelper != null && guiThreadHelper.isInGuiThread())
			{
				throw new UnsupportedOperationException("It is not allowed to call to method while within specified"
						+ " synchronisation context. If this error currently occurs on client side maybe you are calling from a GUI thread?");
			}
			synchronized (currentPendingKeys)
			{
				try
				{
					currentPendingKeys.wait(5000);
				}
				catch (InterruptedException e)
				{
					// Intended blank
				}
			}
		}
	}

	protected void loadObjects(List<ILoadContainer> loadedEntities, LinkedHashSet<IObjRef> neededORIs, ArrayList<DirectValueHolderRef> pendingValueHolders)
	{
		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			for (int a = 0, size = loadedEntities.size(); a < size; a++)
			{
				ILoadContainer loadContainer = loadedEntities.get(a);
				IObjRef reference = loadContainer.getReference();

				IEntityMetaData metaData = entityMetaDataProvider.getMetaData(reference.getRealType());
				Object[] primitives = loadContainer.getPrimitives();
				CacheKey[] alternateCacheKeys = extractAlternateCacheKeys(metaData, primitives);

				RootCacheValue cacheValue = putIntern(metaData, null, reference.getId(), reference.getVersion(), alternateCacheKeys, primitives,
						loadContainer.getRelations());
				if (weakEntries)
				{
					addHardRefTL(cacheValue);
				}
				ensureRelationsExist(cacheValue, metaData, neededORIs, pendingValueHolders);
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected void clearPendingKeysOfCurrentThread(ArrayList<IObjRef> cacheKeysToRemove)
	{
		if (cacheKeysToRemove.isEmpty())
		{
			return;
		}
		Lock pendingKeysWriteLock = this.pendingKeysWriteLock;
		pendingKeysWriteLock.lock();
		try
		{
			currentPendingKeys.removeAll(cacheKeysToRemove);
		}
		finally
		{
			pendingKeysWriteLock.unlock();
		}
		synchronized (currentPendingKeys)
		{
			currentPendingKeys.notifyAll();
		}
	}

	protected IList<Object> createResult(List<IObjRef> orisToGet, RootCacheValue[] rootCacheValuesToGet, Set<CacheDirective> cacheDirective,
			ICacheIntern targetCache, boolean checkVersion)
	{
		boolean loadContainerResult = cacheDirective.contains(CacheDirective.LoadContainerResult);
		boolean cacheValueResult = cacheDirective.contains(CacheDirective.CacheValueResult) || (targetCache == this);
		if (targetCache == null && !loadContainerResult && !cacheValueResult)
		{
			return null;
		}
		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		boolean returnMisses = cacheDirective.contains(CacheDirective.ReturnMisses);

		boolean targetCacheAccess = !loadContainerResult && !cacheValueResult;

		IEventQueue eventQueue = this.eventQueue;
		if (targetCacheAccess && eventQueue != null)
		{
			eventQueue.pause(targetCache);
		}
		try
		{
			ArrayList<Object> result = new ArrayList<Object>();
			ArrayList<IObjRef> tempObjRefList = null;
			IdentityHashMap<IObjRef, ObjRef> alreadyClonedObjRefs = new IdentityHashMap<IObjRef, ObjRef>();
			for (int a = 0, size = orisToGet.size(); a < size; a++)
			{
				IObjRef oriToGet = orisToGet.get(a);
				if (oriToGet == null)
				{
					if (returnMisses)
					{
						result.add(null);
					}
					continue;
				}
				IEntityMetaData metaData = entityMetaDataProvider.getMetaData(oriToGet.getRealType());

				RootCacheValue cacheValue = rootCacheValuesToGet != null ? rootCacheValuesToGet[a] : getCacheValue(metaData, oriToGet, checkVersion);
				if (cacheValue == null) // Cache miss
				{
					if (targetCacheAccess)
					{
						Object cacheHitObject = targetCache.getObject(oriToGet, targetCache, CacheDirective.failEarly());
						if (cacheHitObject != null)
						{
							result.add(cacheHitObject);
							continue;
						}
					}
					if (returnMisses)
					{
						result.add(null);
					}
					// But we already loaded before so we can do nothing now
					continue;
				}
				if (loadContainerResult)
				{
					IObjRef[][] objRefs = cacheValue.getRelations();
					LoadContainer loadContainer = new LoadContainer();
					loadContainer.setReference(new ObjRef(cacheValue.getEntityType(), ObjRef.PRIMARY_KEY_INDEX, cacheValue.getId(), cacheValue.getVersion()));
					loadContainer.setPrimitives(cacheValue.getPrimitives());
					if (objRefs != null && objRefs.length > 0)
					{
						IObjRef[][] objRefsClone = new IObjRef[objRefs.length][];
						for (int b = objRefs.length; b-- > 0;)
						{
							objRefsClone[b] = cloneObjectRefArray(objRefs[b], alreadyClonedObjRefs);
						}
						objRefs = objRefsClone;
					}
					loadContainer.setRelations(objRefs);
					result.add(loadContainer);
				}
				else if (cacheValueResult)
				{
					result.add(cacheValue);
				}
				else
				{
					if (tempObjRefList == null)
					{
						tempObjRefList = new ArrayList<IObjRef>(1);
						tempObjRefList.add(new ObjRef());
					}
					Object cacheHitObject = createObjectFromScratch(metaData, cacheValue, targetCache, tempObjRefList);
					result.add(cacheHitObject);
				}
			}
			return result;
		}
		finally
		{
			if (targetCacheAccess && eventQueue != null)
			{
				eventQueue.resume(targetCache);
			}
		}
	}

	protected Object createObjectFromScratch(IEntityMetaData metaData, RootCacheValue cacheValue, ICacheIntern targetCache, ArrayList<IObjRef> tempObjRefList)
	{
		Class<?> entityType = cacheValue.getEntityType();

		IObjRef tempObjRef = tempObjRefList.get(0);
		tempObjRef.setId(cacheValue.getId());
		tempObjRef.setIdNameIndex(ObjRef.PRIMARY_KEY_INDEX);
		tempObjRef.setRealType(entityType);

		Lock targetWriteLock = targetCache.getWriteLock();
		targetWriteLock.lock();
		try
		{
			Object cacheObject = targetCache.getObjects(tempObjRefList, CacheDirective.failEarlyAndReturnMisses()).get(0);
			if (cacheObject != null)
			{
				return cacheObject;
			}
			cacheObject = targetCache.createCacheValueInstance(metaData, null);
			updateExistingObject(metaData, cacheValue, cacheObject, targetCache);

			Method[] postLoadMethods = metaData.getPostLoadMethods();
			for (int a = 0, size = postLoadMethods.length; a < size; a++)
			{
				try
				{
					postLoadMethods[a].invoke(cacheObject);
				}
				catch (Exception e)
				{
					throw RuntimeExceptionUtil.mask(e, "Error occured while handling PostLoad method of entity type " + metaData.getEntityType().getName());
				}
			}
			return cacheObject;
		}
		finally
		{
			targetWriteLock.unlock();
		}
	}

	protected void updateExistingObject(IEntityMetaData metaData, RootCacheValue cacheValue, Object obj, ICacheIntern targetCache)
	{
		IConversionHelper conversionHelper = this.conversionHelper;
		IObjectCopier objectCopier = this.objectCopier;
		Object id = cacheValue.getId();
		Object version = cacheValue.getVersion();
		metaData.getIdMember().setValue(obj, id);
		if (obj instanceof IParentCacheValueHardRef)
		{
			((IParentCacheValueHardRef) obj).setParentCacheValueHardRef(cacheValue);
		}
		ITypeInfoItem versionMember = metaData.getVersionMember();
		if (versionMember != null)
		{
			versionMember.setValue(obj, version);
		}
		ITypeInfoItem[] primitiveMembers = metaData.getPrimitiveMembers();
		Object[] primitiveTemplates = cacheValue.getPrimitives();

		for (int a = primitiveMembers.length; a-- > 0;)
		{
			ITypeInfoItem primitiveMember = primitiveMembers[a];
			Class<?> memberType = primitiveMember.getRealType();

			Object primitiveTemplate = primitiveTemplates[a];

			Object primitive;

			if (primitiveTemplate == null)
			{
				if (Collection.class.isAssignableFrom(memberType))
				{
					primitive = ListUtil.createObservableCollectionOfType(memberType, 0);
				}
				else
				{
					primitive = null;
				}
			}
			else if (objectCopier != null)
			{
				primitive = objectCopier.clone(primitiveTemplate);
				primitive = conversionHelper.convertValueToType(memberType, primitive);
			}
			else
			{
				primitive = createPrimitiveFromTemplate(memberType, primitiveTemplate);
			}
			primitiveMember.setValue(obj, primitive);
			if (primitive instanceof IParentEntityAware)
			{
				((IParentEntityAware) primitive).setParentEntity(obj, primitiveMember);
			}
		}
		targetCache.addDirect(metaData, id, version, obj, primitiveTemplates, cacheValue.getRelations());
	}

	protected Object createPrimitiveFromTemplate(Class<?> expectedType, Object primitiveTemplate)
	{
		if (expectedType.isArray())
		{
			// Deep clone non-empty arrays because they are not immutable like other primitive items
			Class<?> componentType = expectedType.getComponentType();
			if (primitiveTemplate == null)
			{
				return createArray(componentType, 0);
			}
			else if (primitiveTemplate.getClass().isArray())
			{
				int length = Array.getLength(primitiveTemplate);
				if (length == 0)
				{
					if (primitiveTemplate.getClass().getComponentType().equals(componentType))
					{
						// At this point an 'immutable' empty array template may be returned directly
						return primitiveTemplate;
					}
					else
					{
						return createArray(componentType, 0);
					}
				}
				return copyByValue(primitiveTemplate);
			}
			Object primitive = Array.newInstance(componentType, 1);
			Array.set(primitive, 0, primitiveTemplate);
			return primitive;
		}
		else if (primitiveTemplate != null && expectedType.isAssignableFrom(primitiveTemplate.getClass()))
		{
			// The template itself matches with the expected type. All we have to do is clone the template
			return copyByValue(primitiveTemplate);
		}
		else if (Collection.class.isAssignableFrom(expectedType))
		{
			// Deep clone collections because they are not immutable like other primitive items
			if (primitiveTemplate == null)
			{
				return ListUtil.createCollectionOfType(expectedType, 0);
			}
			if (primitiveTemplate.getClass().isArray())
			{
				int length = Array.getLength(primitiveTemplate);
				Collection<Object> primitive = ListUtil.createObservableCollectionOfType(expectedType, length);
				if (length == 0)
				{
					return primitive;
				}
				// Clone template to access its ITEMS by REFERENCE
				primitiveTemplate = copyByValue(primitiveTemplate);
				for (int a = 0; a < length; a++)
				{
					Object item = Array.get(primitiveTemplate, a);
					primitive.add(item);
				}
				return primitive;
			}
			else if (primitiveTemplate instanceof Collection)
			{
				int length = ((Collection<?>) primitiveTemplate).size();
				Collection<Object> primitive = ListUtil.createCollectionOfType(expectedType, length);
				if (length == 0)
				{
					return primitive;
				}
				// Clone template to access its ITEMS by REFERENCE
				primitiveTemplate = copyByValue(primitiveTemplate);
				if (primitiveTemplate instanceof List)
				{
					List<?> listPrimitiveTemplate = (List<?>) primitiveTemplate;
					for (int a = 0; a < length; a++)
					{
						Object item = listPrimitiveTemplate.get(a);
						primitive.add(item);
					}
				}
				else
				{
					primitive.addAll((Collection<?>) primitiveTemplate);
				}
				return primitive;
			}
			Collection<Object> primitive = ListUtil.createCollectionOfType(expectedType, 1);
			primitive.add(copyByValue(primitiveTemplate));
			return primitive;
		}
		else if (primitiveTemplate == null)
		{
			return null;
		}
		Object convertedPrimitiveTemplate = conversionHelper.convertValueToType(expectedType, primitiveTemplate);
		if (convertedPrimitiveTemplate != primitiveTemplate)
		{
			return convertedPrimitiveTemplate;
		}
		// To be sure, that the conversion has really no relation with the original at all, we clone it
		return copyByValue(convertedPrimitiveTemplate);
	}

	protected Object createArray(Class<?> componentType, int size)
	{
		if (size == 0)
		{
			Object array = typeToEmptyArray.get(componentType);
			if (array == null)
			{
				array = Array.newInstance(componentType, 0);
			}
			return array;
		}
		return Array.newInstance(componentType, size);
	}

	protected Object copyByValue(Object obj)
	{
		Class<?> type = obj.getClass();
		if (ImmutableTypeSet.isImmutableType(type))
		{
			return obj;
		}
		// VERY SLOW fallback if no IObjectCopier implementation provided
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bis);
			return ois.readObject();
		}
		catch (IOException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		catch (ClassNotFoundException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected Collection<Object> createCollectionIfRequested(Class<?> expectedType, int size)
	{
		if (Set.class.isAssignableFrom(expectedType))
		{
			return new HashSet<Object>((int) (size / AbstractHashSet.DEFAULT_LOAD_FACTOR) + 1, AbstractHashSet.DEFAULT_LOAD_FACTOR);
		}
		else if (Collection.class.isAssignableFrom(expectedType))
		{
			return new ArrayList<Object>(size);
		}
		return null;
	}

	@Override
	public void addDirect(IEntityMetaData metaData, Object id, Object version, Object primitiveFilledObject, Object[] primitives, IObjRef[][] relations)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	protected void cacheValueHasBeenAdded(byte idIndex, Object id, IEntityMetaData metaData, Object[] primitives, IObjRef[][] relations, Object cacheValueR)
	{
		super.cacheValueHasBeenAdded(idIndex, id, metaData, primitives, relations, cacheValueR);

		registerAllRelations(relations);
	}

	@Override
	protected void cacheValueHasBeenRead(Object cacheValueR)
	{
		super.cacheValueHasBeenRead(cacheValueR);
		int lruThreshold = this.lruThreshold;
		if (lruThreshold == 0)
		{
			// LRU handling disabled
			return;
		}
		RootCacheValue cacheValue = getCacheValueFromReference(cacheValueR);
		if (cacheValue == null)
		{
			return;
		}
		InterfaceFastList<RootCacheValue> lruList = this.lruList;
		java.util.concurrent.locks.Lock lruLock = this.lruLock;
		lruLock.lock();
		try
		{
			lruList.remove(cacheValue);
			lruList.pushFirst(cacheValue);
			while (lruList.size() > lruThreshold)
			{
				lruList.popLast(); // Ignore result
			}
		}
		finally
		{
			lruLock.unlock();
		}
	}

	@Override
	protected void cacheValueHasBeenUpdated(IEntityMetaData metaData, Object[] primitives, IObjRef[][] relations, Object cacheValueR)
	{
		super.cacheValueHasBeenUpdated(metaData, primitives, relations, cacheValueR);

		unregisterAllRelations(getCacheValueFromReference(cacheValueR).getRelations());
		registerAllRelations(relations);
	}

	@Override
	protected void cacheValueHasBeenRemoved(Class<?> entityType, byte idIndex, Object id, RootCacheValue cacheValue)
	{
		unregisterAllRelations(cacheValue.getRelations());

		if (lruThreshold == 0)
		{
			// LRU handling disabled
			return;
		}
		java.util.concurrent.locks.Lock lruLock = this.lruLock;
		lruLock.lock();
		try
		{
			// Item in lru list
			lruList.remove(cacheValue);
		}
		finally
		{
			lruLock.unlock();
		}
		super.cacheValueHasBeenRemoved(entityType, idIndex, id, cacheValue);
	}

	@Override
	public void removePriorVersions(IObjRef ori)
	{
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			super.removePriorVersions(ori);

			updateReferenceVersion(ori);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected void registerAllRelations(IObjRef[][] relations)
	{
		if (relations == null)
		{
			return;
		}
		for (IObjRef[] methodRelations : relations)
		{
			registerRelations(methodRelations);
		}
	}

	protected void registerRelations(IObjRef[] relations)
	{
		if (relations == null)
		{
			return;
		}
		HashMap<IObjRef, Integer> relationOris = this.relationOris;
		for (int i = relations.length; i-- > 0;)
		{
			IObjRef related = relations[i];
			if (related == null)
			{
				continue;
			}
			IObjRef existing = relationOris.getKey(related);
			if (existing != null)
			{
				Integer count = relationOris.get(existing);
				relationOris.put(existing, Integer.valueOf(count.intValue() + 1));

				relations[i] = existing;
			}
			else
			{
				relationOris.put(related, Integer.valueOf(1));
			}
		}
	}

	protected void unregisterAllRelations(IObjRef[][] relations)
	{
		if (relations == null)
		{
			return;
		}
		for (IObjRef[] methodRelations : relations)
		{
			unregisterRelations(methodRelations);
		}
	}

	protected void unregisterRelations(IObjRef[] relations)
	{
		if (relations == null)
		{
			return;
		}
		HashMap<IObjRef, Integer> relationOris = this.relationOris;
		for (int i = relations.length; i-- > 0;)
		{
			IObjRef related = relations[i];
			Integer count = relationOris.get(related);
			if (count == 1)
			{
				relationOris.remove(related);
			}
			else
			{
				relationOris.put(related, Integer.valueOf(count.intValue() - 1));
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void updateReferenceVersion(IObjRef ori)
	{
		Object version = ori.getVersion();
		if (version == null)
		{
			return;
		}
		IObjRef existing = relationOris.getKey(ori);
		if (existing == null)
		{
			return;
		}

		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(ori.getRealType());
		ITypeInfoItem versionMember = metaData.getVersionMember();
		if (versionMember == null)
		{
			return;
		}
		IConversionHelper conversionHelper = this.conversionHelper;
		Object cacheVersion = conversionHelper.convertValueToType(versionMember.getElementType(), existing.getVersion());
		Object currentVersion = conversionHelper.convertValueToType(versionMember.getElementType(), version);

		if (cacheVersion == null || ((Comparable) cacheVersion).compareTo(currentVersion) < 0)
		{
			existing.setVersion(currentVersion);
		}
	}

	@Override
	protected CacheKey[] getAlternateCacheKeysFromCacheValue(IEntityMetaData metaData, RootCacheValue cacheValue)
	{
		return extractAlternateCacheKeys(metaData, cacheValue);
	}

	protected void ensureRelationsExist(RootCacheValue cacheValue, IEntityMetaData metaData, LinkedHashSet<IObjRef> cascadeNeededORIs,
			ArrayList<DirectValueHolderRef> pendingValueHolders)
	{
		IRelationInfoItem[] relationMembers = metaData.getRelationMembers();
		IObjRef[][] relations = cacheValue.getRelations();
		for (int a = relations.length; a-- > 0;)
		{
			IObjRef[] relationsOfMember = relations[a];

			IRelationInfoItem relationMember = relationMembers[a];

			CascadeLoadMode loadCascadeMode = relationMember.getCascadeLoadMode();
			switch (loadCascadeMode)
			{
				case DEFAULT:
				case LAZY:
					break;
				case EAGER:
				{
					// Ensure the related RootCacheValues will be loaded - we do not bother here if the relations are
					// known or not yet
					pendingValueHolders.add(new IndirectValueHolderRef(cacheValue, relationMember, this));
					break;
				}
				case EAGER_VERSION:
				{
					if (relationsOfMember != null)
					{
						// ObjRefs already loaded. Nothing to do
						continue;
					}
					// TODO load ONLY the ObjRefs now...
					break;
				}
				default:
					throw RuntimeExceptionUtil.createEnumNotSupportedException(loadCascadeMode);
			}
		}
	}

	@Override
	public boolean applyValues(Object targetObject, ICacheIntern targetCache)
	{
		if (targetObject == null)
		{
			return false;
		}
		ICacheModification cacheModification = this.cacheModification;
		boolean oldCacheModificationValue = cacheModification.isActive();
		cacheModification.setActive(true);
		try
		{
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(targetObject.getClass());
			Object id = metaData.getIdMember().getValue(targetObject, false);
			RootCacheValue cacheValue = getCacheValue(metaData, ObjRef.PRIMARY_KEY_INDEX, id);
			if (cacheValue == null) // Cache miss
			{
				return false;
			}
			updateExistingObject(metaData, cacheValue, targetObject, targetCache);
			return true;
		}
		finally
		{
			cacheModification.setActive(oldCacheModificationValue);
		}
	}

	@Override
	public void getContent(final HandleContentDelegate handleContentDelegate)
	{
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			for (CacheMapEntry entry : keyToCacheValueDict)
			{
				RootCacheValue cacheValue = getCacheValueFromReference(entry.getValue());
				if (cacheValue == null)
				{
					continue;
				}
				handleContentDelegate.invoke(entry.getEntityType(), entry.getIdIndex(), entry.getId(), cacheValue);
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	protected Class<?> getEntityTypeOfObject(Object obj)
	{
		if (obj instanceof RootCacheValue)
		{
			return ((RootCacheValue) obj).getEntityType();
		}
		return super.getEntityTypeOfObject(obj);
	}

	@Override
	protected Object getIdOfObject(IEntityMetaData metaData, Object obj)
	{
		if (obj instanceof RootCacheValue)
		{
			return ((RootCacheValue) obj).getId();
		}
		return super.getIdOfObject(metaData, obj);
	}

	@Override
	protected Object getVersionOfObject(IEntityMetaData metaData, Object obj)
	{
		if (obj instanceof RootCacheValue)
		{
			return ((RootCacheValue) obj).getVersion();
		}
		return super.getVersionOfObject(metaData, obj);
	}

	@Override
	protected Object[] extractPrimitives(IEntityMetaData metaData, Object obj)
	{
		if (obj instanceof RootCacheValue)
		{
			return ((RootCacheValue) obj).getPrimitives();
		}
		return super.extractPrimitives(metaData, obj);
	}

	@Override
	protected IObjRef[][] extractRelations(IEntityMetaData metaData, Object obj, List<Object> relationValues)
	{
		if (obj instanceof RootCacheValue)
		{
			return ((RootCacheValue) obj).getRelations();
		}
		return super.extractRelations(metaData, obj, relationValues);
	}

	@Override
	protected void clearIntern()
	{
		super.clearIntern();
		relationOris.clear();
		java.util.concurrent.locks.Lock lruLock = this.lruLock;
		lruLock.lock();
		try
		{
			lruList.clear();
		}
		finally
		{
			lruLock.unlock();
		}
	}

	@Override
	protected void putInternObjRelation(RootCacheValue cacheValue, IEntityMetaData metaData, IObjRelation objRelation, IObjRef[] relationsOfMember)
	{
		int relationIndex = metaData.getIndexByRelationName(objRelation.getMemberName());
		if (relationsOfMember.length == 0)
		{
			relationsOfMember = ObjRef.EMPTY_ARRAY;
		}
		cacheValue.setRelation(relationIndex, relationsOfMember);
	}

	@Override
	public void beginOnline()
	{
		clear();
	}

	@Override
	public void handleOnline()
	{
		// Intended blank
	}

	@Override
	public void endOnline()
	{
		// Intended blank
	}

	@Override
	public void beginOffline()
	{
		clear();
	}

	@Override
	public void handleOffline()
	{
		// Intended blank
	}

	@Override
	public void endOffline()
	{
		// Intended blank
	}
}
