package de.osthus.ambeth.cache;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import de.osthus.ambeth.cache.collections.CacheHashMap;
import de.osthus.ambeth.cache.collections.ICacheMapEntryTypeProvider;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.compositeid.ICompositeIdFactory;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.threading.IGuiThreadHelper;
import de.osthus.ambeth.threading.SensitiveThreadLocal;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.util.ICacheHelper;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.IDisposable;
import de.osthus.ambeth.util.Lock;
import de.osthus.ambeth.util.ReadWriteLock;

public abstract class AbstractCache<V> implements ICache, IInitializingBean, IDisposable
{
	protected static final CacheKey[] emptyCacheKeyArray = new CacheKey[0];

	protected static final ThreadLocal<Boolean> failEarlyModeActiveTL = new SensitiveThreadLocal<Boolean>();

	private static final ThreadLocal<IdentityHashSet<Object>> hardRefTL = new SensitiveThreadLocal<IdentityHashSet<Object>>();

	public static boolean isFailEarlyModeActive()
	{
		return Boolean.TRUE.equals(failEarlyModeActiveTL.get());
	}

	public static void setFailEarlyModeActive(boolean failEarlyModeActive)
	{
		failEarlyModeActiveTL.set(Boolean.valueOf(failEarlyModeActive));
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected CacheHashMap keyToCacheValueDict;

	@Autowired
	protected ICacheMapEntryTypeProvider cacheMapEntryTypeProvider;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected ICompositeIdFactory compositeIdFactory;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected ICacheHelper cacheHelper;

	@Autowired(optional = true)
	protected IGuiThreadHelper guiThreadHelper;

	@Autowired
	protected IProxyHelper proxyHelper;

	protected boolean weakEntries;

	protected volatile int changeVersion = 1;

	protected final ReferenceQueue<V> referenceQueue = new ReferenceQueue<V>();

	protected final Lock readLock, writeLock;

	public AbstractCache()
	{
		ReadWriteLock rwLock = new ReadWriteLock();
		readLock = rwLock.getReadLock();
		writeLock = rwLock.getWriteLock();
	}

	@Override
	public void afterPropertiesSet()
	{
		keyToCacheValueDict = new CacheHashMap(cacheMapEntryTypeProvider);
	}

	@Override
	public void dispose()
	{
		cacheHelper = null;
		cacheMapEntryTypeProvider = null;
		compositeIdFactory = null;
		conversionHelper = null;
		entityMetaDataProvider = null;
		guiThreadHelper = null;
		proxyHelper = null;
		keyToCacheValueDict = null;
	}

	public void setWeakEntries(boolean weakEntries)
	{
		this.weakEntries = weakEntries;
	}

	@Override
	public Lock getReadLock()
	{
		return readLock;
	}

	@Override
	public Lock getWriteLock()
	{
		return writeLock;
	}

	protected void checkNotDisposed()
	{
		if (conversionHelper == null)
		{
			throw new IllegalStateException("Cache already disposed");
		}
	}

	public boolean acquireHardRefTLIfNotAlready()
	{
		return acquireHardRefTLIfNotAlready(0);
	}

	public boolean acquireHardRefTLIfNotAlready(int sizeHint)
	{
		if (!weakEntries)
		{
			return false;
		}
		IdentityHashSet<Object> hardRefSet = hardRefTL.get();
		if (hardRefSet != null)
		{
			return false;
		}
		hardRefSet = sizeHint > 0 ? IdentityHashSet.create(sizeHint) : new IdentityHashSet<Object>();
		hardRefTL.set(hardRefSet);
		return true;
	}

	public static void addHardRefTL(Object obj)
	{
		if (obj == null)
		{
			return;
		}
		IdentityHashSet<Object> hardRefSet = hardRefTL.get();
		if (hardRefSet == null)
		{
			return;
		}
		hardRefSet.add(obj);
	}

	public void clearHardRefs(boolean acquirementSuccessful)
	{
		if (!acquirementSuccessful)
		{
			return;
		}
		hardRefTL.remove();
	}

	/**
	 * Checks if an entity with a given type and ID and at least the given version exists in cache.
	 * 
	 * @param ori
	 *            Object reference.
	 * @return True if a request for the referenced object could be satisfied, otherwise false.
	 */
	protected boolean exists(IObjRef ori)
	{
		return existsValue(ori) != null;
	}

	@SuppressWarnings("unchecked")
	protected V getCacheValueFromReference(Object reference)
	{
		if (reference == null)
		{
			return null;
		}
		if (weakEntries)
		{
			return ((Reference<V>) reference).get();
		}
		return (V) reference;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected V existsValue(IObjRef ori)
	{
		IEntityMetaData metaData = this.entityMetaDataProvider.getMetaData(ori.getRealType());
		ITypeInfoItem idMember = metaData.getIdMemberByIdIndex(ori.getIdNameIndex());
		Object id = conversionHelper.convertValueToType(idMember.getRealType(), ori.getId());
		Lock readLock = getReadLock();
		readLock.lock();
		try
		{
			Object cacheValueR = getCacheValueR(metaData, ori.getIdNameIndex(), id);
			V cacheValue = getCacheValueFromReference(cacheValueR);
			if (cacheValue == null)
			{
				return null;
			}
			ITypeInfoItem versionMember = metaData.getVersionMember();
			if (versionMember == null)
			{
				if (weakEntries)
				{
					addHardRefTL(cacheValue);
				}

				// without a versionMember each cache hit is a valid hit
				return cacheValue;
			}
			Object cacheVersion = getVersionOfCacheValue(metaData, cacheValue);
			// Compare operation only works on identical operand types
			Object requestedVersion = conversionHelper.convertValueToType(versionMember.getElementType(), ori.getVersion());

			if (requestedVersion == null || cacheVersion == null || ((Comparable) cacheVersion).compareTo(requestedVersion) >= 0)
			{
				if (weakEntries)
				{
					addHardRefTL(cacheValue);
				}
				// requested version is lower or equal than cached
				// version
				return cacheValue;
			}
			return null;
		}
		finally
		{
			readLock.unlock();
		}
	}

	protected CacheKey[] extractAlternateCacheKeys(IEntityMetaData metaData, Object obj)
	{
		int alternateIdCount = metaData.getAlternateIdCount();
		if (alternateIdCount == 0)
		{
			return emptyCacheKeyArray;
		}
		CacheKey[] alternateCacheKeys = new CacheKey[alternateIdCount];
		extractAlternateCacheKeys(metaData, obj, alternateCacheKeys);
		return alternateCacheKeys;
	}

	protected void extractAlternateCacheKeys(IEntityMetaData metaData, Object obj, CacheKey[] alternateCacheKeys)
	{
		if (alternateCacheKeys.length == 0)
		{
			return;
		}
		Class<?> entityType = metaData.getEntityType();
		for (int idIndex = metaData.getAlternateIdCount(); idIndex-- > 0;)
		{
			Object alternateId;
			if (obj instanceof Object[])
			{
				alternateId = compositeIdFactory.createIdFromPrimitives(metaData, idIndex, (Object[]) obj);
			}
			else
			{
				alternateId = compositeIdFactory.createIdFromPrimitives(metaData, idIndex, (AbstractCacheValue) obj);
			}
			CacheKey alternateCacheKey = alternateCacheKeys[idIndex];
			if (alternateId == null)
			{
				if (alternateCacheKey != null)
				{
					alternateCacheKeys[idIndex] = null;
				}
				continue;
			}
			if (alternateCacheKey == null)
			{
				alternateCacheKey = new CacheKey();
				alternateCacheKeys[idIndex] = alternateCacheKey;
			}
			alternateCacheKey.setEntityType(entityType);
			alternateCacheKey.setId(alternateId);
			alternateCacheKey.setIdNameIndex((byte) idIndex);
		}
	}

	protected abstract CacheKey[] getAlternateCacheKeysFromCacheValue(IEntityMetaData metaData, V cacheValue);

	public void remove(Class<?> type, Object id)
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(type);
		removeCacheValueFromCacheCascade(metaData, ObjRef.PRIMARY_KEY_INDEX, id);
	}

	public void remove(IObjRef ori)
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(ori.getRealType());
		removeCacheValueFromCacheCascade(metaData, ori.getIdNameIndex(), ori.getId());
	}

	public void remove(List<IObjRef> oris)
	{
		for (int a = oris.size(); a-- > 0;)
		{
			IObjRef ori = oris.get(a);
			remove(ori);
		}
	}

	public void removePriorVersions(IObjRef ori)
	{
		if (ori.getVersion() != null)
		{
			if (existsValue(ori) != null)
			{
				// if there is a object in the cache with the requested version
				// it
				// has already been refreshed
				return;
			}
		}
		remove(ori);
	}

	public void removePriorVersions(List<IObjRef> oris)
	{
		for (int a = oris.size(); a-- > 0;)
		{
			IObjRef ori = oris.get(a);
			removePriorVersions(ori);
		}
	}

	protected void removeCacheValueFromCacheCascade(IEntityMetaData metaData, byte idIndex, Object id)
	{
		Class<?> entityType = metaData.getEntityType();
		ITypeInfoItem idMember = metaData.getIdMemberByIdIndex(idIndex);
		id = conversionHelper.convertValueToType(idMember.getRealType(), id);
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			Object cacheValueR = removeKeyFromCache(entityType, idIndex, id);
			V cacheValue = getCacheValueFromReference(cacheValueR);
			if (cacheValue == null)
			{
				return;
			}
			cacheValueHasBeenRemoved(entityType, idIndex, id, cacheValue);
			Object primaryId = getIdOfCacheValue(metaData, cacheValue);
			if (primaryId != null)
			{
				removeCacheValueFromCacheSingle(metaData, ObjRef.PRIMARY_KEY_INDEX, getIdOfCacheValue(metaData, cacheValue));
			}
			CacheKey[] alternateCacheKeys = getAlternateCacheKeysFromCacheValue(metaData, cacheValue);
			for (int a = alternateCacheKeys.length; a-- > 0;)
			{
				removeKeyFromCache(alternateCacheKeys[a]);
			}
			increaseVersion();
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected Object removeKeyFromCache(CacheKey cacheKey)
	{
		if (cacheKey == null)
		{
			return null;
		}
		return removeKeyFromCache(cacheKey.getEntityType(), cacheKey.getIdNameIndex(), cacheKey.getId());
	}

	protected Object removeKeyFromCache(Class<?> entityType, byte idIndex, Object id)
	{
		if (entityType == null)
		{
			return null;
		}
		return this.keyToCacheValueDict.remove(entityType, idIndex, id);
	}

	public abstract V createCacheValueInstance(IEntityMetaData metaData, Object obj);

	protected abstract Object getIdOfCacheValue(IEntityMetaData metaData, V cacheValue);

	protected abstract void setIdOfCacheValue(IEntityMetaData metaData, V cacheValue, Object id);

	protected abstract Object getVersionOfCacheValue(IEntityMetaData metaData, V cacheValue);

	protected abstract void setVersionOfCacheValue(IEntityMetaData metaData, V cacheValue, Object version);

	protected void setRelationsOfCacheValue(IEntityMetaData metaData, V cacheValue, Object[] primitives, IObjRef[][] relations)
	{
		// Intended blank
	}

	protected void increaseVersion()
	{
		if (++changeVersion == Integer.MAX_VALUE)
		{
			changeVersion = 1;
		}
	}

	protected void removeCacheValueFromCacheSingle(IEntityMetaData metaData, byte idIndex, Object id)
	{
		ITypeInfoItem idMember = metaData.getIdMemberByIdIndex(idIndex);
		id = conversionHelper.convertValueToType(idMember.getRealType(), id);
		removeKeyFromCache(metaData.getEntityType(), idIndex, id);
	}

	protected void removeAlternateCacheKeysFromCache(IEntityMetaData metaData, CacheKey[] alternateCacheKeys)
	{
		if (alternateCacheKeys == null)
		{
			return;
		}
		for (int a = alternateCacheKeys.length; a-- > 0;)
		{
			removeKeyFromCache(alternateCacheKeys[a]);
		}
	}

	protected Object createReference(V obj, Class<?> entityType, byte idIndex, Object id)
	{
		if (weakEntries)
		{
			return new CacheWeakReference<V>(obj, referenceQueue);
		}
		return obj;
	}

	public List<Object> put(Object objectToCache)
	{
		HashSet<IObjRef> cascadeNeededORIs = new HashSet<IObjRef>();
		IdentityHashSet<Object> alreadyHandledSet = new IdentityHashSet<Object>();
		ArrayList<Object> hardRefsToCacheValue = new ArrayList<Object>();
		boolean success = acquireHardRefTLIfNotAlready();
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			putIntern(objectToCache, hardRefsToCacheValue, alreadyHandledSet, cascadeNeededORIs);
			increaseVersion();
			return hardRefsToCacheValue;
		}
		finally
		{
			writeLock.unlock();
			clearHardRefs(success);
		}
	}

	protected Class<?> getEntityTypeOfObject(Object obj)
	{
		return obj.getClass();
	}

	protected Object getIdOfObject(IEntityMetaData metaData, Object obj)
	{
		return metaData.getIdMember().getValue(obj, false);
	}

	protected Object getVersionOfObject(IEntityMetaData metaData, Object obj)
	{
		ITypeInfoItem versionMember = metaData.getVersionMember();
		return versionMember != null ? versionMember.getValue(obj, false) : null;
	}

	protected Object[] extractPrimitives(IEntityMetaData metaData, Object obj)
	{
		return cacheHelper.extractPrimitives(metaData, obj);
	}

	protected IObjRef[][] extractRelations(IEntityMetaData metaData, Object obj, List<Object> relationValues)
	{
		return cacheHelper.extractRelations(metaData, obj, relationValues);
	}

	protected abstract void putInternObjRelation(V cacheValue, IEntityMetaData metaData, IObjRelation objRelation, IObjRef[] relationsOfMember);

	protected void putIntern(Object objectToCache, ArrayList<Object> hardRefsToCacheValue, IdentityHashSet<Object> alreadyHandledSet,
			HashSet<IObjRef> cascadeNeededORIs)
	{
		if (objectToCache == null || !alreadyHandledSet.add(objectToCache))
		{
			return;
		}
		if (objectToCache instanceof List)
		{
			List<?> list = (List<?>) objectToCache;
			for (int a = list.size(); a-- > 0;)
			{
				putIntern(list.get(a), hardRefsToCacheValue, alreadyHandledSet, cascadeNeededORIs);
			}
			return;
		}
		if (objectToCache instanceof Collection)
		{
			for (Object item : (Collection<?>) objectToCache)
			{
				putIntern(item, hardRefsToCacheValue, alreadyHandledSet, cascadeNeededORIs);
			}
			return;
		}
		if (objectToCache instanceof IObjRelationResult)
		{
			IObjRelationResult objRelationResult = (IObjRelationResult) objectToCache;
			IObjRelation objRelation = objRelationResult.getReference();
			IObjRef objRef = objRelation.getObjRefs()[0];
			IEntityMetaData metaData2 = entityMetaDataProvider.getMetaData(objRef.getRealType());

			Object cacheValueR = getCacheValueR(metaData2, objRef.getIdNameIndex(), objRef.getId());
			V cacheValue = getCacheValueFromReference(cacheValueR);
			if (cacheValue == null)
			{
				return;
			}
			putInternObjRelation(cacheValue, metaData2, objRelation, objRelationResult.getRelations());
			return;
		}
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(getEntityTypeOfObject(objectToCache));
		Object key = getIdOfObject(metaData, objectToCache);

		ArrayList<Object> relationValues = new ArrayList<Object>();
		IObjRef[][] relations = extractRelations(metaData, objectToCache, relationValues);

		if (key != null)
		{
			// Object itself can only be cached with a primary key
			Object version = getVersionOfObject(metaData, objectToCache);

			Object cacheValueR = getCacheValueR(metaData, ObjRef.PRIMARY_KEY_INDEX, key);
			V cacheValue = getCacheValueFromReference(cacheValueR);
			boolean objectItselfIsUpToDate = false;
			if (cacheValue != null && getIdOfCacheValue(metaData, cacheValue) != null)
			{
				// Similar object already cached. Let's see how the version
				// compares...
				Object cachedVersion = getVersionOfCacheValue(metaData, cacheValue);
				if (cachedVersion != null && cachedVersion.equals(version))
				{
					// Object has even already the same version, so there is
					// absolutely nothing to do here
					objectItselfIsUpToDate = true;
				}
			}
			if (!objectItselfIsUpToDate)
			{
				Object[] primitives = extractPrimitives(metaData, objectToCache);
				CacheKey[] alternateCacheKeys = extractAlternateCacheKeys(metaData, primitives);
				Object hardRef = putIntern(metaData, objectToCache, key, version, alternateCacheKeys, primitives, relations);
				hardRefsToCacheValue.add(hardRef);
			}
			else
			{
				hardRefsToCacheValue.add(cacheValue);
			}
		}

		// Even if it has no id we look for its relations and cache them
		for (int a = relationValues.size(); a-- > 0;)
		{
			putIntern(relationValues.get(a), hardRefsToCacheValue, alreadyHandledSet, cascadeNeededORIs);
		}
	}

	protected boolean allowCacheValueReplacement()
	{
		return false;
	}

	protected V putIntern(IEntityMetaData metaData, Object obj, Object id, Object version, CacheKey[] alternateCacheKeys, Object[] primitives,
			IObjRef[][] relations)
	{
		byte idIndex = ObjRef.PRIMARY_KEY_INDEX;
		Object cacheValueR = getCacheValueR(metaData, idIndex, id);
		V cacheValue = getCacheValueFromReference(cacheValueR);
		if (cacheValue == null)
		{
			Class<?> entityType = metaData.getEntityType();
			cacheValue = createCacheValueInstance(metaData, obj);
			cacheValueR = createReference(cacheValue, entityType, idIndex, id);
			id = conversionHelper.convertValueToType(metaData.getIdMember().getRealType(), id);
			setIdOfCacheValue(metaData, cacheValue, id);

			keyToCacheValueDict.put(entityType, idIndex, id, cacheValueR);
			cacheValueHasBeenAdded(idIndex, id, metaData, primitives, relations, cacheValueR);
		}
		else if (obj != null && cacheValue != obj && !allowCacheValueReplacement())
		{
			// If the cache does not allow replacements, do nothing with this
			// put-request
			return cacheValue;
		}
		else
		{
			CacheKey[] oldAlternateIds = extractAlternateCacheKeys(metaData, primitives);
			for (int a = oldAlternateIds.length; a-- > 0;)
			{
				removeKeyFromCache(oldAlternateIds[a]);
			}
			cacheValueHasBeenUpdated(metaData, primitives, relations, cacheValueR);
		}
		cacheValueHasBeenRead(cacheValueR);

		// Create cache entry for the primary id and all alternate ids
		putAlternateCacheKeysToCache(metaData, alternateCacheKeys, cacheValueR);

		setVersionOfCacheValue(metaData, cacheValue, version);
		setRelationsOfCacheValue(metaData, cacheValue, primitives, relations);
		return cacheValue;
	}

	protected void putAlternateCacheKeysToCache(IEntityMetaData metaData, CacheKey[] alternateCacheKeys, Object cacheValueR)
	{
		for (int a = alternateCacheKeys.length; a-- > 0;)
		{
			CacheKey alternateCacheKey = alternateCacheKeys[a];
			if (alternateCacheKey != null)
			{
				keyToCacheValueDict.put(alternateCacheKey.getEntityType(), alternateCacheKey.getIdNameIndex(), alternateCacheKey.getId(), cacheValueR);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected V getCacheValue(IEntityMetaData metaData, IObjRef objRef, boolean checkVersion)
	{
		Object cacheValueR = getCacheValueR(metaData, objRef.getIdNameIndex(), objRef.getId());
		V cacheValue = getCacheValueFromReference(cacheValueR);
		if (cacheValue == null)
		{
			return null;
		}
		ITypeInfoItem versionMember = metaData.getVersionMember();
		if (checkVersion && versionMember != null && objRef.getVersion() != null)
		{
			Object cacheVersion = getVersionOfCacheValue(metaData, cacheValue);
			// Compare operation only works on identical operand types
			Object requestedVersion = conversionHelper.convertValueToType(versionMember.getElementType(), objRef.getVersion());

			if (cacheVersion != null && ((Comparable) cacheVersion).compareTo(requestedVersion) < 0)
			{
				// requested version is higher than cached version. So this is a cache miss because of outdated information
				return null;
			}
		}
		return cacheValue;
	}

	protected Object getCacheValueR(IEntityMetaData metaData, byte idIndex, Object id)
	{
		ITypeInfoItem idMember = metaData.getIdMemberByIdIndex(idIndex);
		id = conversionHelper.convertValueToType(idMember.getRealType(), id);
		Object cacheValueR = keyToCacheValueDict.get(metaData.getEntityType(), idIndex, id);
		cacheValueHasBeenRead(cacheValueR);
		return cacheValueR;
	}

	protected V getCacheValue(IEntityMetaData metaData, byte idIndex, Object id)
	{
		Object cacheValueR = getCacheValueR(metaData, idIndex, id);
		return getCacheValueFromReference(cacheValueR);
	}

	protected void cacheValueHasBeenAdded(byte idIndex, Object id, IEntityMetaData metaData, Object[] primitives, IObjRef[][] relations, Object cacheValueR)
	{
		checkForCleanup();
	}

	protected void cacheValueHasBeenRead(Object cacheValueR)
	{
		if (weakEntries)
		{
			addHardRefTL(getCacheValueFromReference(cacheValueR));
		}
	}

	protected void cacheValueHasBeenUpdated(IEntityMetaData metaData, Object[] primitives, IObjRef[][] relations, Object cacheValueR)
	{
		checkForCleanup();
	}

	protected void cacheValueHasBeenRemoved(Class<?> entityType, byte idIndex, Object id, V cacheValue)
	{
		checkForCleanup();
	}

	@Override
	public <E> E getObject(Class<E> type, Object id)
	{
		return getObject(type, id, Collections.<CacheDirective> emptySet());
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E> E getObject(Class<E> type, Object... compositeIdParts)
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(type);
		Object id = compositeIdFactory.createCompositeId(metaData, metaData.getIdMember(), compositeIdParts);
		ObjRef objRef = new ObjRef(metaData.getEntityType(), id, null);
		return (E) getObject(objRef, CacheDirective.none());
	}

	@Override
	public <E> E getObject(Class<E> type, String idName, Object id)
	{
		return getObject(type, idName, id, Collections.<CacheDirective> emptySet());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> E getObject(Class<E> type, Object id, Set<CacheDirective> cacheDirective)
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(type);
		ObjRef objRef = new ObjRef(metaData.getEntityType(), id, null);
		return (E) getObject(objRef, cacheDirective);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> E getObject(Class<E> type, String idName, Object id, Set<CacheDirective> cacheDirective)
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(type);
		ObjRef objRef = new ObjRef(metaData.getEntityType(), metaData.getIdIndexByMemberName(idName), id, null);
		return (E) getObject(objRef, cacheDirective);
	}

	@Override
	public Object getObject(IObjRef oriToGet, Set<CacheDirective> cacheDirective)
	{
		if (oriToGet == null)
		{
			return null;
		}
		ArrayList<IObjRef> orisToGet = new ArrayList<IObjRef>(1);
		orisToGet.add(oriToGet);
		List<Object> objects = getObjects(orisToGet, cacheDirective);
		if (objects.isEmpty())
		{
			return null;
		}
		return objects.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> IList<E> getObjects(Class<E> type, Object... ids)
	{
		ArrayList<IObjRef> orisToGet = new ArrayList<IObjRef>(ids.length);
		for (int a = 0, size = ids.length; a < size; a++)
		{
			Object id = ids[a];
			ObjRef objRef = new ObjRef(type, ObjRef.PRIMARY_KEY_INDEX, id, null);
			orisToGet.add(objRef);
		}
		return (IList<E>) getObjects(orisToGet, Collections.<CacheDirective> emptySet());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E> IList<E> getObjects(Class<E> type, List<?> ids)
	{
		ArrayList<IObjRef> orisToGet = new ArrayList<IObjRef>(ids.size());
		for (int a = 0, size = ids.size(); a < size; a++)
		{
			orisToGet.add(new ObjRef(type, ObjRef.PRIMARY_KEY_INDEX, ids.get(a), null));
		}
		return (IList<E>) getObjects(orisToGet, Collections.<CacheDirective> emptySet());
	}

	@Override
	public IList<Object> getObjects(IObjRef[] orisToGetArray, Set<CacheDirective> cacheDirective)
	{
		ArrayList<IObjRef> orisToGet = new ArrayList<IObjRef>(orisToGetArray);
		return getObjects(orisToGet, cacheDirective);
	}

	@Override
	public abstract IList<Object> getObjects(List<IObjRef> orisToGet, Set<CacheDirective> cacheDirective);

	@Override
	public abstract IList<IObjRelationResult> getObjRelations(List<IObjRelation> objRels, Set<CacheDirective> cacheDirective);

	protected void checkForCleanup()
	{
		if (!weakEntries)
		{
			return;
		}
		doCleanUpIntern();
	}

	public void cleanUp()
	{
		if (!weakEntries)
		{
			return;
		}
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			doCleanUpIntern();
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected void doCleanUpIntern()
	{
		ICacheReference cacheValueR;
		while ((cacheValueR = (ICacheReference) referenceQueue.poll()) != null)
		{
			Class<?> entityType = cacheValueR.getEntityType();
			byte idIndex = cacheValueR.getIdIndex();
			Object id = cacheValueR.getId();
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);

			Object existingCacheValueR = getCacheValueR(metaData, idIndex, id);
			if (existingCacheValueR != cacheValueR)
			{
				// new entry is already another instance reflecting the same entity
				continue;
			}
			removeCacheValueFromCacheCascade(metaData, idIndex, id);
		}
	}

	public int size()
	{
		Lock readLock = getReadLock();
		readLock.lock();
		try
		{
			return keyToCacheValueDict.size();
		}
		finally
		{
			readLock.unlock();
		}
	}

	public void clear()
	{
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			clearIntern();
			increaseVersion();
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected void clearIntern()
	{
		keyToCacheValueDict.clear();
	}

	@Override
	public void getContent(HandleContentDelegate handleContentDelegate)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	public void cascadeLoadPath(Class<?> entityType, String cascadeLoadPath)
	{
		throw new UnsupportedOperationException("Not implemented");
	}
}
