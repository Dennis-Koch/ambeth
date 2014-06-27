package de.osthus.ambeth.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.osthus.ambeth.annotation.CascadeLoadMode;
import de.osthus.ambeth.cache.collections.CacheHashMap;
import de.osthus.ambeth.cache.collections.CacheMapEntry;
import de.osthus.ambeth.cache.config.CacheConfigurationConstants;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.event.IEventQueue;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.merge.model.IDirectObjRef;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.model.IDataObject;
import de.osthus.ambeth.proxy.IDefaultCollection;
import de.osthus.ambeth.proxy.IValueHolderContainer;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.util.CachePath;
import de.osthus.ambeth.util.ICacheHelper;
import de.osthus.ambeth.util.ICachePathHelper;
import de.osthus.ambeth.util.IParamHolder;
import de.osthus.ambeth.util.ListUtil;
import de.osthus.ambeth.util.Lock;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.ParamHolder;

public class ChildCache extends AbstractCache<Object> implements ICacheIntern, IWritableCache, IDisposableCache
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected CacheHashMap keyToAlternateIdsMap;

	protected IMap<Class<?>, List<CachePath>> membersToInitialize;

	@Autowired
	protected ICacheModification cacheModification;

	@Autowired
	protected ICachePathHelper cachePathHelper;

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired(optional = true)
	protected IEventQueue eventQueue;

	@Autowired
	protected IFirstLevelCacheExtendable firstLevelCacheExtendable;

	@Autowired
	protected ICacheIntern parent;

	protected int cacheId;

	@Property(name = CacheConfigurationConstants.ValueholderOnEmptyToOne, defaultValue = "false")
	protected boolean valueholderOnEmptyToOne;

	@Property(name = CacheConfigurationConstants.OverwriteToManyRelationsInChildCache, defaultValue = "true")
	protected boolean overwriteToManyRelations;

	@Property
	protected boolean privileged;

	@Override
	public void afterPropertiesSet()
	{
		super.afterPropertiesSet();

		keyToAlternateIdsMap = new CacheHashMap(cacheMapEntryTypeProvider);
	}

	@Property(name = CacheConfigurationConstants.FirstLevelCacheWeakActive, defaultValue = "true")
	@Override
	public void setWeakEntries(boolean weakEntries)
	{
		super.setWeakEntries(weakEntries);
	}

	@Override
	public boolean isPrivileged()
	{
		return privileged;
	}

	@Override
	public void setCacheId(int cacheId)
	{
		if (this.cacheId != 0 && cacheId != 0)
		{
			throw new UnsupportedOperationException();
		}
		this.cacheId = cacheId;
	}

	@Override
	public void dispose()
	{
		if (cacheId != 0)
		{
			firstLevelCacheExtendable.unregisterFirstLevelCache(this, null, false);
		}
		entityFactory = null;
		firstLevelCacheExtendable = null;
		parent = null;
		membersToInitialize = null;
		keyToAlternateIdsMap = null;
		super.dispose();
	}

	public ICacheIntern getParent()
	{
		return parent;
	}

	@Override
	public int getCacheId()
	{
		return cacheId;
	}

	@Override
	protected void cacheValueHasBeenAdded(byte idIndex, Object id, IEntityMetaData metaData, Object[] primitives, IObjRef[][] relations, Object cacheValueR)
	{
		super.cacheValueHasBeenAdded(idIndex, id, metaData, primitives, relations, cacheValueR);

		Class<?> entityType = metaData.getEntityType();
		CacheKey[] oldAlternateCacheKeys = (CacheKey[]) keyToAlternateIdsMap.get(entityType, idIndex, id);
		if (oldAlternateCacheKeys != null)
		{
			for (int a = oldAlternateCacheKeys.length; a-- > 0;)
			{
				CacheKey alternateCacheKey = oldAlternateCacheKeys[a];
				if (alternateCacheKey != null)
				{
					removeKeyFromCache(alternateCacheKey);
				}
			}
		}
		CacheKey[] newAlternateCacheKeys = oldAlternateCacheKeys;
		if (newAlternateCacheKeys == null)
		{
			// Allocate new array to hold alternate ids
			newAlternateCacheKeys = extractAlternateCacheKeys(metaData, primitives);
			if (newAlternateCacheKeys.length > 0)
			{
				keyToAlternateIdsMap.put(entityType, idIndex, id, newAlternateCacheKeys);
			}
		}
		else
		{
			// reuse existing array for new alternate id-values
			extractAlternateCacheKeys(metaData, primitives, newAlternateCacheKeys);
		}
		putAlternateCacheKeysToCache(metaData, newAlternateCacheKeys, cacheValueR);
	}

	@Override
	public Object createCacheValueInstance(IEntityMetaData metaData, Object obj)
	{
		if (obj != null)
		{
			return obj;
		}
		return entityFactory.createEntity(metaData);
	}

	@Override
	protected Object getIdOfCacheValue(IEntityMetaData metaData, Object cacheValue)
	{
		return metaData.getIdMember().getValue(cacheValue, false);
	}

	@Override
	protected void setIdOfCacheValue(IEntityMetaData metaData, Object cacheValue, Object id)
	{
		metaData.getIdMember().setValue(cacheValue, id);
	}

	@Override
	protected Object getVersionOfCacheValue(IEntityMetaData metaData, Object cacheValue)
	{
		ITypeInfoItem versionMember = metaData.getVersionMember();
		if (versionMember == null)
		{
			return null;
		}
		return versionMember.getValue(cacheValue, false);
	}

	@Override
	protected void setVersionOfCacheValue(IEntityMetaData metaData, Object cacheValue, Object version)
	{
		ITypeInfoItem versionMember = metaData.getVersionMember();
		if (versionMember == null)
		{
			return;
		}
		versionMember.setValue(cacheValue, version);
	}

	public Map<Class<?>, List<CachePath>> getMembersToInitialize()
	{
		return membersToInitialize;
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
	public IList<Object> getObjects(List<IObjRef> orisToGet, Set<CacheDirective> cacheDirective)
	{
		return getObjects(orisToGet, this, cacheDirective);
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
			ICacheModification cacheModification = this.cacheModification;
			boolean oldCacheModificationValue = cacheModification.isActive();
			boolean acquireSuccess = acquireHardRefTLIfNotAlready(orisToGet.size());
			cacheModification.setActive(true);
			try
			{
				if (cacheDirective.contains(CacheDirective.LoadContainerResult) || cacheDirective.contains(CacheDirective.CacheValueResult))
				{
					return parent.getObjects(orisToGet, this, cacheDirective);
				}
				ParamHolder<Boolean> doAnotherRetry = new ParamHolder<Boolean>();
				while (true)
				{
					doAnotherRetry.setValue(Boolean.FALSE);
					IList<Object> result = getObjectsRetry(orisToGet, cacheDirective, doAnotherRetry);
					if (!Boolean.TRUE.equals(doAnotherRetry.getValue()))
					{
						if (!cacheDirective.contains(CacheDirective.FailEarly))
						{
							cachePathHelper.ensureInitializedRelations(result, membersToInitialize);
						}
						return result;
					}
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

	protected IList<Object> getObjectsRetry(List<IObjRef> orisToGet, Set<CacheDirective> cacheDirective, IParamHolder<Boolean> doAnotherRetry)
	{
		Lock readLock = getReadLock();
		if (cacheDirective.contains(CacheDirective.FailEarly))
		{
			readLock.lock();
			try
			{
				return createResult(orisToGet, cacheDirective, true);
			}
			finally
			{
				readLock.unlock();
			}
		}
		ArrayList<IObjRef> orisToLoad = new ArrayList<IObjRef>();
		int cacheVersionBeforeLongTimeAction = waitForConcurrentReadFinish(orisToGet, orisToLoad);
		if (orisToLoad.size() == 0)
		{
			// Everything found in the cache. We STILL hold the readlock so we can immediately create the result
			// We already even checked the version. So we do not bother version anymore here
			try
			{
				return createResult(orisToGet, cacheDirective, false);
			}
			finally
			{
				readLock.unlock();
			}
		}
		Set<CacheDirective> parentCacheDirective = CacheDirective.none();
		if (cacheDirective.contains(CacheDirective.FailInCacheHierarchy))
		{
			parentCacheDirective = CacheDirective.failEarly();
		}
		parent.getObjects(orisToLoad, this, parentCacheDirective);
		// Objects do not have to be put, because their were already
		// added by the parent to this cache
		readLock.lock();
		try
		{
			int cacheVersionAfterLongTimeAction = changeVersion;
			if (cacheVersionAfterLongTimeAction != cacheVersionBeforeLongTimeAction)
			{
				// Another thread did some changes (possibly DataChange-Remove actions)
				// We have to ensure that our result-scope is still valid
				// We return null to allow a further full retry of getObjects()
				doAnotherRetry.setValue(Boolean.TRUE);
				return null;
			}
			return createResult(orisToGet, cacheDirective, false);
		}
		finally
		{
			readLock.unlock();
		}
	}

	protected int waitForConcurrentReadFinish(List<IObjRef> orisToGet, List<IObjRef> orisToLoad)
	{
		Lock readLock = getReadLock();
		boolean releaseReadLock = true;
		HashSet<IObjRef> objRefsAlreadyQueried = null;
		readLock.lock();
		try
		{
			for (int a = 0, size = orisToGet.size(); a < size; a++)
			{
				IObjRef oriToGet = orisToGet.get(a);
				if (oriToGet == null || oriToGet instanceof IDirectObjRef && ((IDirectObjRef) oriToGet).getDirect() != null)
				{
					continue;
				}
				Object cacheValue = existsValue(oriToGet);
				if (cacheValue != null)
				{
					// Cache hit, but not relevant at this step, so we continue
					continue;
				}
				if (objRefsAlreadyQueried == null)
				{
					objRefsAlreadyQueried = HashSet.create(size - a);
				}
				if (!objRefsAlreadyQueried.add(oriToGet))
				{
					// Object has been already queried from parent
					// It makes no sense to query it multiple times
					continue;
				}
				orisToLoad.add(oriToGet);
			}
			if (orisToLoad.size() == 0)
			{
				releaseReadLock = false;
			}
			return changeVersion;
		}
		finally
		{
			if (releaseReadLock)
			{
				readLock.unlock();
			}
		}
	}

	protected IList<Object> createResult(List<IObjRef> orisToGet, Set<CacheDirective> cacheDirective, boolean checkVersion)
	{
		ArrayList<Object> result = new ArrayList<Object>(orisToGet.size());

		boolean returnMisses = cacheDirective.contains(CacheDirective.ReturnMisses);

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
			if (oriToGet instanceof IDirectObjRef)
			{
				IDirectObjRef dori = (IDirectObjRef) oriToGet;
				Object entity = dori.getDirect();
				if (entity != null)
				{
					result.add(entity);
					continue;
				}
			}
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(oriToGet.getRealType());
			Object cacheValue = getCacheValue(metaData, oriToGet, checkVersion);
			if (cacheValue != null || returnMisses)
			{
				result.add(cacheValue);
			}
		}
		return result;
	}

	@Override
	public IList<IObjRelationResult> getObjRelations(List<IObjRelation> objRels, Set<CacheDirective> cacheDirective)
	{
		return getObjRelations(objRels, this, cacheDirective);
	}

	@Override
	public IList<IObjRelationResult> getObjRelations(List<IObjRelation> objRels, ICacheIntern targetCache, Set<CacheDirective> cacheDirective)
	{
		checkNotDisposed();
		IEventQueue eventQueue = this.eventQueue;
		if (eventQueue != null)
		{
			eventQueue.pause(this);
		}
		try
		{
			ICacheModification cacheModification = this.cacheModification;
			boolean oldCacheModificationValue = cacheModification.isActive();
			boolean acquireSuccess = acquireHardRefTLIfNotAlready(objRels.size());
			cacheModification.setActive(true);
			try
			{
				return parent.getObjRelations(objRels, targetCache, cacheDirective);
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

	@Override
	public void addDirect(IEntityMetaData metaData, Object id, Object version, Object primitiveFilledObject, Object[] primitives, IObjRef[][] relations)
	{
		if (id == null)
		{
			throw new IllegalArgumentException("Key must be valid: " + primitiveFilledObject);
		}
		Class<?> entityType = metaData.getEntityType();
		byte idIndex = ObjRef.PRIMARY_KEY_INDEX;
		CacheKey[] oldAlternateCacheKeys = null;
		Object cacheValue;
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			Object cacheValueR = getCacheValueR(metaData, idIndex, id);
			cacheValue = getCacheValueFromReference(cacheValueR);

			oldAlternateCacheKeys = (CacheKey[]) keyToAlternateIdsMap.get(entityType, idIndex, id);
			if (oldAlternateCacheKeys != null)
			{
				for (int a = oldAlternateCacheKeys.length; a-- > 0;)
				{
					CacheKey alternateCacheKey = oldAlternateCacheKeys[a];
					if (alternateCacheKey != null)
					{
						removeKeyFromCache(alternateCacheKey);
					}
				}
			}

			if (cacheValue != null)
			{
				if (cacheValue != primitiveFilledObject)
				{
					throw new RuntimeException("There is already another instance of the same entity in this cache. This is a fatal state");
				}
				// Object (same instance) already in cache. Nothing to do here
			}
			else
			{
				cacheValue = primitiveFilledObject;
				cacheValueR = createReference(cacheValue, entityType, idIndex, id);

				keyToCacheValueDict.put(entityType, idIndex, id, cacheValueR);
			}
			CacheKey[] newAlternateCacheKeys = oldAlternateCacheKeys;
			if (newAlternateCacheKeys == null)
			{
				// Allocate new array to hold alternate ids
				newAlternateCacheKeys = extractAlternateCacheKeys(metaData, primitives);
			}
			else
			{
				// reuse existing array for new alternate id-values
				extractAlternateCacheKeys(metaData, primitives, newAlternateCacheKeys);
			}
			if (newAlternateCacheKeys.length > 0)
			{
				keyToAlternateIdsMap.put(entityType, idIndex, id, newAlternateCacheKeys);
				putAlternateCacheKeysToCache(metaData, newAlternateCacheKeys, cacheValueR);
			}
		}
		finally
		{
			writeLock.unlock();
		}
		if (weakEntries)
		{
			addHardRefTL(cacheValue);
		}
		if (relations != null && relations.length > 0)
		{
			IRelationInfoItem[] relationMembers = metaData.getRelationMembers();

			((IValueHolderContainer) primitiveFilledObject).set__TargetCache(this);
			handleValueHolderContainer(primitiveFilledObject, relationMembers, relations);
		}
		if (primitiveFilledObject instanceof IDataObject)
		{
			((IDataObject) primitiveFilledObject).setToBeUpdated(false);
		}
	}

	@SuppressWarnings("unchecked")
	protected void handleValueHolderContainer(Object primitiveFilledObject, IRelationInfoItem[] relationMembers, IObjRef[][] relations)
	{
		ICacheHelper cacheHelper = this.cacheHelper;
		ICacheIntern parent = this.parent;
		IProxyHelper proxyHelper = this.proxyHelper;
		for (int a = relationMembers.length; a-- > 0;)
		{
			IRelationInfoItem relationMember = relationMembers[a];
			IObjRef[] relationsOfMember = relations[a];

			if (!CascadeLoadMode.EAGER.equals(relationMember.getCascadeLoadMode()))
			{
				if (!proxyHelper.isInitialized(primitiveFilledObject, relationMember))
				{
					// Update ObjRef information within the entity and do nothing else
					proxyHelper.setObjRefs(primitiveFilledObject, relationMember, relationsOfMember);
					continue;
				}
			}
			// We can safely access to relation if we want to
			if (relationsOfMember == null)
			{
				// Reset value holder state because we do not know the content currently
				proxyHelper.setUninitialized(primitiveFilledObject, relationMember, null);
				continue;
			}
			Object relationValue = relationMember.getValue(primitiveFilledObject);
			if (relationsOfMember.length == 0)
			{
				if (!relationMember.isToMany())
				{
					if (relationValue != null)
					{
						// Relation has to be flushed
						relationMember.setValue(primitiveFilledObject, null);
					}
				}
				else
				{
					if (relationValue != null)
					{
						// Reuse existing collection
						((Collection<?>) relationValue).clear();
					}
					else
					{
						// We have to create a new empty collection
						relationValue = cacheHelper.createInstanceOfTargetExpectedType(relationMember.getRealType(), relationMember.getElementType());
						relationMember.setValue(primitiveFilledObject, relationValue);
					}
				}
				continue;
			}
			// So we know the new content (which is not empty) and we know that the current content is already initialized
			// Now we have to refresh the current content eagerly

			// load entities as if we were an "eager valueholder" here
			IList<Object> potentialNewItems = parent.getObjects(new ArrayList<IObjRef>(relationsOfMember), this,
					isFailEarlyModeActive() ? EnumSet.of(CacheDirective.FailEarly) : CacheDirective.none());
			if (overwriteToManyRelations)
			{
				Object newRelationValue = cacheHelper.convertResultListToExpectedType(potentialNewItems, relationMember.getRealType(),
						relationMember.getElementType());
				// Set new to-many-relation, even if there has not changed anything in its item content
				relationMember.setValue(primitiveFilledObject, newRelationValue);
				continue;
			}
			List<Object> relationItems = ListUtil.anyToList(relationValue);

			boolean diff = relationItems.size() != potentialNewItems.size();
			if (!diff)
			{
				for (int b = potentialNewItems.size(); b-- > 0;)
				{
					if (potentialNewItems.get(b) != relationItems.get(b))
					{
						diff = true;
						break;
					}
				}
			}
			if (!diff)
			{
				// Nothing to do
				continue;
			}
			if (relationValue != null)
			{
				// Reuse existing collection
				Collection<Object> coll = (Collection<Object>) relationValue;
				coll.clear();
				for (int b = 0, sizeB = relationItems.size(); b < sizeB; b++)
				{
					coll.add(relationItems.get(b));
				}
			}
			else
			{
				// We have to create a new empty collection
				Object newRelationValue = cacheHelper.convertResultListToExpectedType(potentialNewItems, relationMember.getRealType(),
						relationMember.getElementType());
				relationMember.setValue(primitiveFilledObject, newRelationValue);
			}
		}
	}

	protected boolean isNotNullRelationValue(Object relationValue)
	{
		return relationValue != null && (!(relationValue instanceof IDefaultCollection) || !((IDefaultCollection) relationValue).hasDefaultState());
	}

	@Override
	protected void clearIntern()
	{
		super.clearIntern();
		keyToAlternateIdsMap.clear();
	}

	@Override
	protected Object removeKeyFromCache(Class<?> entityType, byte idIndex, Object id)
	{
		if (entityType == null)
		{
			return null;
		}
		Object cacheValueR = super.removeKeyFromCache(entityType, idIndex, id);
		CacheKey[] alternateCacheKeys = (CacheKey[]) keyToAlternateIdsMap.remove(entityType, idIndex, id);
		if (alternateCacheKeys != null)
		{
			for (int a = alternateCacheKeys.length; a-- > 0;)
			{
				removeKeyFromCache(alternateCacheKeys[a]);
			}
		}
		return cacheValueR;
	}

	@Override
	protected CacheKey[] getAlternateCacheKeysFromCacheValue(IEntityMetaData metaData, Object cacheValue)
	{
		return emptyCacheKeyArray;
	}

	@Override
	public void getContent(final HandleContentDelegate handleContentDelegate)
	{
		checkNotDisposed();
		final CacheHashMap keyToInstanceMap = new CacheHashMap(cacheMapEntryTypeProvider);
		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			for (CacheMapEntry entry : keyToCacheValueDict)
			{
				Object cacheValue = getCacheValueFromReference(entry.getValue());
				if (cacheValue == null)
				{
					continue;
				}
				keyToInstanceMap.put(entry.getEntityType(), entry.getIdIndex(), entry.getId(), cacheValue);
			}
			for (CacheMapEntry entry : keyToInstanceMap)
			{
				byte idIndex = entry.getIdIndex();
				if (idIndex == ObjRef.PRIMARY_KEY_INDEX)
				{
					handleContentDelegate.invoke(entry.getEntityType(), idIndex, entry.getId(), entry.getValue());
				}
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void cascadeLoadPath(Class<?> entityType, String cascadeLoadPath)
	{
		ParamChecker.assertParamNotNull(cascadeLoadPath, "cascadeLoadPath");

		Lock writeLock = getWriteLock();
		writeLock.lock();
		try
		{
			if (membersToInitialize == null)
			{
				membersToInitialize = new HashMap<Class<?>, List<CachePath>>();
			}

			List<CachePath> cachePaths = membersToInitialize.get(entityType);
			if (cachePaths == null)
			{
				cachePaths = new ArrayList<CachePath>();
				membersToInitialize.put(entityType, cachePaths);
			}

			cachePathHelper.buildCachePath(entityType, cascadeLoadPath, cachePaths);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	protected void putInternObjRelation(Object cacheValue, IEntityMetaData metaData, IObjRelation objRelation, IObjRef[] relationsOfMember)
	{
		IRelationInfoItem member = (IRelationInfoItem) metaData.getMemberByName(objRelation.getMemberName());
		if (proxyHelper.isInitialized(cacheValue, member))
		{
			// It is not allowed to set ObjRefs for an already initialized relation
			return;
		}
		proxyHelper.setObjRefs(cacheValue, member, relationsOfMember);
	}
}
