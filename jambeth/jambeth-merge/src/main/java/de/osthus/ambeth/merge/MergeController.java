package de.osthus.ambeth.merge;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.CacheFactoryDirective;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ICacheFactory;
import de.osthus.ambeth.cache.ICacheModification;
import de.osthus.ambeth.cache.ICacheProvider;
import de.osthus.ambeth.cache.IWritableCache;
import de.osthus.ambeth.cache.ValueHolderState;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.config.MergeConfigurationConstants;
import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IUpdateItem;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.merge.transfer.PrimitiveUpdateItem;
import de.osthus.ambeth.merge.transfer.RelationUpdateItem;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.RelationMember;
import de.osthus.ambeth.model.IDataObject;
import de.osthus.ambeth.proxy.IObjRefContainer;
import de.osthus.ambeth.util.DirectValueHolderRef;
import de.osthus.ambeth.util.EqualsUtil;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.IPrefetchHelper;
import de.osthus.ambeth.util.OptimisticLockUtil;
import de.osthus.ambeth.util.ValueHolderRef;

public class MergeController implements IMergeController, IMergeExtendable
{
	protected static final Set<CacheDirective> failEarlyAndReturnMissesSet = EnumSet.of(CacheDirective.FailEarly, CacheDirective.ReturnMisses);

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICacheFactory cacheFactory;

	@Autowired
	protected ICacheModification cacheModification;

	@Autowired
	protected ICacheProvider cacheProvider;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IPrefetchHelper prefetchHelper;

	@Autowired
	protected ICUDResultHelper cudResultHelper;

	@Autowired
	protected IObjRefHelper oriHelper;

	@Property(name = MergeConfigurationConstants.ExactVersionForOptimisticLockingRequired, defaultValue = "false")
	protected boolean exactVersionForOptimisticLockingRequired;

	@Property(name = MergeConfigurationConstants.AlwaysUpdateVersionInChangedEntities, defaultValue = "false")
	protected boolean alwaysUpdateVersionInChangedEntities;

	protected final List<IMergeExtension> mergeExtensions = new ArrayList<IMergeExtension>();

	@Override
	public void registerMergeExtension(IMergeExtension mergeExtension)
	{
		if (mergeExtension == null)
		{
			throw new IllegalStateException("mergeExtension");
		}
		if (mergeExtensions.contains(mergeExtension))
		{
			throw new IllegalStateException("MergeExtension already registered: " + mergeExtension);
		}
		mergeExtensions.add(mergeExtension);
	}

	@Override
	public void unregisterMergeExtension(IMergeExtension mergeExtension)
	{
		if (mergeExtension == null)
		{
			throw new IllegalStateException("mergeExtension");
		}
		if (!mergeExtensions.remove(mergeExtension))
		{
			throw new IllegalStateException("MergeExtension to remove is not registered: " + mergeExtension);
		}
	}

	@Override
	public void applyChangesToOriginals(List<Object> originalRefs, List<IObjRef> oriList, Long changedOn, String changedBy)
	{
		ICacheModification cacheModification = this.cacheModification;
		boolean newInstanceOnCall = cacheProvider.isNewInstanceOnCall();
		boolean oldCacheModificationValue = cacheModification.isActive();
		ArrayList<Object> validObjects = new ArrayList<Object>();
		cacheModification.setActive(true);
		try
		{
			for (int a = originalRefs.size(); a-- > 0;)
			{
				Object originalRef = originalRefs.get(a);
				IObjRef ori = oriList.get(a);

				if (originalRef == null)
				{
					// Object has been deleted by cascade delete contraints on server merge or simply a "not specified"
					// original ref
					continue;
				}
				if (originalRef instanceof IObjRef)
				{
					continue;
				}
				IEntityMetaData metaData = entityMetaDataProvider.getMetaData(originalRef.getClass());

				Member keyMember = metaData.getIdMember();
				Member versionMember = metaData.getVersionMember();

				Member onMember, byMember;
				if (keyMember.getValue(originalRef, false) == null)
				{
					onMember = metaData.getCreatedOnMember();
					byMember = metaData.getCreatedByMember();
				}
				else
				{
					onMember = metaData.getUpdatedOnMember();
					byMember = metaData.getUpdatedByMember();
				}
				if (onMember != null && changedOn != null)
				{
					Object createdOn = conversionHelper.convertValueToType(onMember.getElementType(), changedOn);
					onMember.setValue(originalRef, createdOn);
				}
				if (byMember != null && changedBy != null)
				{
					Object createdBy = conversionHelper.convertValueToType(byMember.getElementType(), changedBy);
					byMember.setValue(originalRef, createdBy);
				}
				if (ori == null)
				{
					keyMember.setValue(originalRef, null);
					if (versionMember != null)
					{
						versionMember.setValue(originalRef, null);
					}
					if (originalRef instanceof IDataObject)
					{
						((IDataObject) originalRef).setToBeUpdated(false);
						((IDataObject) originalRef).setToBeDeleted(false);
					}
					continue; // Object has been deleted directly
				}
				keyMember.setValue(originalRef, conversionHelper.convertValueToType(keyMember.getRealType(), ori.getId()));
				if (versionMember != null)
				{
					if (newInstanceOnCall || alwaysUpdateVersionInChangedEntities)
					{
						versionMember.setValue(originalRef, conversionHelper.convertValueToType(versionMember.getRealType(), ori.getVersion()));
					}
					else
					{
						// We INTENTIONALLY do NOT set the version and let it on its old value, to force the following
						// DCE to refresh the cached object with 'real' data
						// If we set the version here to the ori.getVersion(), the DCE will 'see' a already valid object
						// - but it is NOT valid
						// because it may not contain bi-directional information which can only be resolved by reloading
						// the object from persistence layer
					}
				}
				if (originalRef instanceof IDataObject)
				{
					((IDataObject) originalRef).setToBeUpdated(false);
					((IDataObject) originalRef).setToBeDeleted(false);
				}
				validObjects.add(originalRef);
			}
			putInstancesToCurrentCache(validObjects);
		}
		finally
		{
			cacheModification.setActive(oldCacheModificationValue);
		}
	}

	protected void putInstancesToCurrentCache(List<Object> validObjects)
	{
		if (!MergeProcess.isAddNewlyPersistedEntities())
		{
			return;
		}
		if (validObjects.size() == 0 || cacheProvider.isNewInstanceOnCall())
		{
			// Following code only necessary if cache instance is singleton or threadlocal
			return;
		}
		IWritableCache cache = (IWritableCache) cacheProvider.getCurrentCache();
		cache.put(validObjects);
	}

	@Override
	public ICUDResult mergeDeep(Object obj, MergeHandle handle)
	{
		ICache cache = handle.getCache();
		if (cache == null && cacheFactory != null)
		{
			cache = cacheFactory.create(CacheFactoryDirective.NoDCE, false, Boolean.FALSE);
			handle.setCache(cache);
		}
		LinkedHashMap<Class<?>, IList<Object>> typeToObjectsToMerge = null;
		Class<?>[] entityPersistOrder = entityMetaDataProvider.getEntityPersistOrder();
		if (entityPersistOrder != null && entityPersistOrder.length > 0)
		{
			typeToObjectsToMerge = new LinkedHashMap<Class<?>, IList<Object>>();
		}
		ArrayList<IObjRef> objRefs = new ArrayList<IObjRef>();
		ArrayList<ValueHolderRef> valueHolderKeys = new ArrayList<ValueHolderRef>();
		IList<Object> objectsToMerge = scanForInitializedObjects(obj, handle.isDeepMerge(), typeToObjectsToMerge, objRefs, valueHolderKeys);
		IList<Object> eagerlyLoadedOriginals = null;
		if (cache != null)
		{
			// Load all requested object originals in one roundtrip
			if (objRefs.size() > 0)
			{
				eagerlyLoadedOriginals = cache.getObjects(objRefs, CacheDirective.returnMisses());
				for (int a = eagerlyLoadedOriginals.size(); a-- > 0;)
				{
					IObjRef existingOri = objRefs.get(a);
					if (eagerlyLoadedOriginals.get(a) == null && existingOri != null && existingOri.getId() != null)
					{
						// Cache miss for an entity we want to merge. This is an OptimisticLock-State
						throw OptimisticLockUtil.throwDeleted(existingOri);
					}
				}
				ArrayList<IObjRef> objRefsOfVhks = new ArrayList<IObjRef>(valueHolderKeys.size());
				for (int a = 0, size = valueHolderKeys.size(); a < size; a++)
				{
					objRefsOfVhks.add(valueHolderKeys.get(a).getObjRef());
				}
				IList<Object> objectsOfVhks = cache.getObjects(objRefsOfVhks, failEarlyAndReturnMissesSet);
				for (int a = valueHolderKeys.size(); a-- > 0;)
				{
					IObjRefContainer objectOfVhk = (IObjRefContainer) objectsOfVhks.get(a);
					if (objectOfVhk == null)
					{
						continue;
					}
					ValueHolderRef valueHolderRef = valueHolderKeys.get(a);
					if (ValueHolderState.INIT == objectOfVhk.get__State(valueHolderRef.getRelationIndex()))
					{
						continue;
					}
					DirectValueHolderRef vhcKey = new DirectValueHolderRef(objectOfVhk, valueHolderRef.getMember());
					handle.getPendingValueHolders().add(vhcKey);
				}
			}
		}
		if (typeToObjectsToMerge != null)
		{
			for (Class<?> orderedEntityType : entityPersistOrder)
			{
				IList<Object> objectsToMergeOfOrderedType = typeToObjectsToMerge.remove(orderedEntityType);
				if (objectsToMergeOfOrderedType == null)
				{
					continue;
				}
				mergeDeepStart(objectsToMergeOfOrderedType, handle);
			}
			for (Entry<Class<?>, IList<Object>> entry : typeToObjectsToMerge)
			{
				IList<Object> objectsToMergeOfUnorderedType = entry.getValue();
				mergeDeepStart(objectsToMergeOfUnorderedType, handle);
			}
		}
		else if (objectsToMerge.size() > 0)
		{
			mergeDeepStart(objectsToMerge, handle);
		}
		return cudResultHelper.createCUDResult(handle);
	}

	@Override
	public IList<Object> scanForInitializedObjects(Object obj, boolean isDeepMerge, Map<Class<?>, IList<Object>> typeToObjectsToMerge, List<IObjRef> objRefs,
			List<ValueHolderRef> valueHolderKeys)
	{
		ArrayList<Object> objects = new ArrayList<Object>();
		IdentityHashSet<Object> alreadyHandledObjectsSet = new IdentityHashSet<Object>();
		scanForInitializedObjectsIntern(obj, isDeepMerge, objects, typeToObjectsToMerge, alreadyHandledObjectsSet, objRefs, valueHolderKeys);
		return objects;
	}

	protected void scanForInitializedObjectsIntern(Object obj, boolean isDeepMerge, List<Object> objects, Map<Class<?>, IList<Object>> typeToObjectsToMerge,
			ISet<Object> alreadyHandledObjectsSet, List<IObjRef> objRefs, List<ValueHolderRef> valueHolderKeys)
	{
		if (obj == null || !alreadyHandledObjectsSet.add(obj))
		{
			return;
		}
		if (obj instanceof List)
		{
			List<?> list = (List<?>) obj;
			for (int a = 0, size = list.size(); a < size; a++)
			{
				scanForInitializedObjectsIntern(list.get(a), isDeepMerge, objects, typeToObjectsToMerge, alreadyHandledObjectsSet, objRefs, valueHolderKeys);
			}
			return;
		}
		else if (obj instanceof Iterable)
		{
			for (Object item : (Iterable<?>) obj)
			{
				scanForInitializedObjectsIntern(item, isDeepMerge, objects, typeToObjectsToMerge, alreadyHandledObjectsSet, objRefs, valueHolderKeys);
			}
			return;
		}
		else if (obj.getClass().isArray())
		{
			// This is valid for non-native arrays in java
			Object[] array = (Object[]) obj;
			for (int a = array.length; a-- > 0;)
			{
				Object item = array[a];
				scanForInitializedObjectsIntern(item, isDeepMerge, objects, typeToObjectsToMerge, alreadyHandledObjectsSet, objRefs, valueHolderKeys);
			}
			return;
		}
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(obj.getClass(), true);
		if (metaData == null)
		{
			return;
		}
		ObjRef objRef = null;
		Object id = metaData.getIdMember().getValue(obj, false);
		if (id != null)
		{
			objRef = new ObjRef(metaData.getEntityType(), ObjRef.PRIMARY_KEY_INDEX, id, null);
		}
		if (!(obj instanceof IDataObject) || ((IDataObject) obj).hasPendingChanges())
		{
			if (typeToObjectsToMerge != null)
			{
				IList<Object> objectsToMerge = typeToObjectsToMerge.get(metaData.getEntityType());
				if (objectsToMerge == null)
				{
					objectsToMerge = new ArrayList<Object>();
					typeToObjectsToMerge.put(metaData.getEntityType(), objectsToMerge);
				}
				objectsToMerge.add(obj);
			}
			objects.add(obj);
			objRefs.add(objRef);
		}
		if (!isDeepMerge)
		{
			return;
		}
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
			RelationMember relationMember = relationMembers[relationIndex];
			Object item = relationMember.getValue(obj);
			if (objRef != null && item != null)
			{
				ValueHolderRef vhk = new ValueHolderRef(objRef, relationMember, relationIndex);
				valueHolderKeys.add(vhk);
			}
			scanForInitializedObjectsIntern(item, isDeepMerge, objects, typeToObjectsToMerge, alreadyHandledObjectsSet, objRefs, valueHolderKeys);
		}
	}

	protected void mergeDeepStart(Object obj, MergeHandle handle)
	{
		if (handle.getPendingValueHolders().size() > 0)
		{
			IList<Object> pendingValueHolders = handle.getPendingValueHolders();
			prefetchHelper.prefetch(pendingValueHolders);
			pendingValueHolders.clear();
		}
		mergeDeepIntern(obj, handle);

		while (true)
		{
			IList<Runnable> pendingRunnables = handle.getPendingRunnables();
			IList<Object> pendingValueHolders = handle.getPendingValueHolders();
			if (pendingValueHolders.size() == 0 && pendingRunnables.size() == 0)
			{
				return;
			}
			if (pendingValueHolders.size() > 0)
			{
				prefetchHelper.prefetch(pendingValueHolders);
				pendingValueHolders.clear();
			}
			if (pendingRunnables.size() > 0)
			{
				ArrayList<Runnable> pendingRunnablesClone = new ArrayList<Runnable>(pendingRunnables);
				pendingRunnables.clear();
				for (int a = 0, size = pendingRunnablesClone.size(); a < size; a++)
				{
					pendingRunnablesClone.get(a).run();
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	protected void mergeDeepIntern(Object obj, MergeHandle handle)
	{
		if (obj == null)
		{
			return;
		}
		if (obj instanceof List)
		{
			if (!handle.alreadyProcessedSet.add(obj))
			{
				return;
			}
			List objList = (List) obj;
			for (int a = 0, size = objList.size(); a < size; a++)
			{
				mergeOrPersist(objList.get(a), handle);
			}
		}
		else if (obj instanceof Iterable)
		{
			if (!handle.alreadyProcessedSet.add(obj))
			{
				return;
			}
			Iterator<?> iter = ((Iterable<?>) obj).iterator();
			while (iter.hasNext())
			{
				mergeOrPersist(iter.next(), handle);
			}
		}
		else if (obj.getClass().isArray())
		{
			Object[] array = (Object[]) obj;
			for (int a = array.length; a-- > 0;)
			{
				mergeOrPersist(array[a], handle);
			}
		}
		else
		{
			mergeOrPersist(obj, handle);
		}
	}

	protected void mergeOrPersist(Object obj, MergeHandle handle)
	{
		if (obj == null || !handle.alreadyProcessedSet.add(obj))
		{
			return;
		}
		if (obj instanceof IDataObject)
		{
			IDataObject dataObject = (IDataObject) obj;
			if (!dataObject.hasPendingChanges())
			{
				return;
			}
			if (dataObject.isToBeDeleted())
			{
				handle.objToDeleteSet.add(obj);
				return;
			}
		}
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(obj.getClass());
		metaData.prePersist(obj);
		Object key = metaData.getIdMember().getValue(obj, false);
		if (key == null)
		{
			persist(obj, handle);
			return;
		}
		ICache cache = handle.getCache();
		if (cache == null)
		{
			throw new IllegalStateException("Object has been cloned somewhere");
		}
		Object clone = cache.getObject(metaData.getEntityType(), key, CacheDirective.none());
		if (clone == null)
		{
			throw OptimisticLockUtil.throwDeleted(oriHelper.entityToObjRef(obj), obj);
		}
		merge(obj, clone, handle);
	}

	protected void persist(Object obj, MergeHandle handle)
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(obj.getClass());

		// Ensure entity will be persisted even if no single property is specified
		addModification(obj, handle);

		RelationMember[] relationMembers = metaData.getRelationMembers();
		if (relationMembers.length > 0)
		{
			IObjRefContainer vhc = (IObjRefContainer) obj;

			for (int relationIndex = relationMembers.length; relationIndex-- > 0;)
			{
				RelationMember relationMember = relationMembers[relationIndex];
				if (ValueHolderState.INIT != vhc.get__State(relationIndex))
				{
					continue;
				}
				Object objMember = relationMember.getValue(obj, false);

				if (objMember == null)
				{
					continue;
				}
				addOriModification(obj, relationMember.getName(), objMember, null, handle);
			}
		}
		for (Member primitiveMember : metaData.getPrimitiveMembers())
		{
			if (!metaData.isMergeRelevant(primitiveMember))
			{
				continue;
			}
			Object objMember = primitiveMember.getValue(obj, true);

			if (objMember != null)
			{
				addModification(obj, primitiveMember.getName(), primitiveMember.getElementType(), objMember, null, handle);
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void merge(final Object obj, final Object clone, final MergeHandle handle)
	{
		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(obj.getClass());

		boolean fieldBasedMergeActive = handle.isFieldBasedMergeActive();
		boolean oneChangeOccured = false;
		try
		{
			RelationMember[] relationMembers = metaData.getRelationMembers();
			if (relationMembers.length > 0)
			{
				IObjRefContainer vhc = (IObjRefContainer) obj;

				for (int relationIndex = relationMembers.length; relationIndex-- > 0;)
				{
					RelationMember relationMember = relationMembers[relationIndex];
					if (!metaData.isMergeRelevant(relationMember))
					{
						continue;
					}
					if (ValueHolderState.INIT != vhc.get__State(relationIndex))
					{
						// v2 valueholder is not initialized. so a change is impossible
						continue;
					}
					Object objMember = relationMember.getValue(obj, false);
					Object cloneMember = relationMember.getValue(clone, false);
					if (objMember instanceof IDataObject && !((IDataObject) objMember).hasPendingChanges())
					{
						IEntityMetaData relationMetaData = entityMetaDataProvider.getMetaData(relationMember.getRealType());
						if (equalsReferenceOrId(objMember, cloneMember, handle, relationMetaData))
						{
							continue;
						}
					}

					IEntityMetaData childMetaData = entityMetaDataProvider.getMetaData(relationMember.getElementType());

					if (isMemberModified(objMember, cloneMember, handle, childMetaData))
					{
						oneChangeOccured = true;
						addOriModification(obj, relationMember.getName(), objMember, cloneMember, handle);
					}
				}
			}
			if (fieldBasedMergeActive)
			{
				mergePrimitivesFieldBased(metaData, obj, clone, handle);
				return;
			}
			boolean additionalRound;
			do
			{
				additionalRound = !oneChangeOccured;
				for (Member primitiveMember : metaData.getPrimitiveMembers())
				{
					if (!metaData.isMergeRelevant(primitiveMember))
					{
						continue;
					}
					Object objValue = primitiveMember.getValue(obj, true);
					if (oneChangeOccured)
					{
						addModification(obj, primitiveMember.getName(), primitiveMember.getElementType(), objValue, null, handle);
						continue;
					}
					Object cloneValue = primitiveMember.getValue(clone, true);
					if (!arePrimitivesEqual(metaData, primitiveMember, objValue, cloneValue, handle))
					{
						oneChangeOccured = true;
						break;
					}
				}
			}
			while (additionalRound && oneChangeOccured);
		}
		finally
		{
			Member versionMember = metaData.getVersionMember();
			if (oneChangeOccured && versionMember != null)
			{
				// Check for early optimistic locking (Another, later level is directly on persistence layer)
				Object versionToMerge = versionMember.getValue(obj, true);
				Object currentVersion = versionMember.getValue(clone, true);

				int compareResult = ((Comparable) versionToMerge).compareTo(currentVersion);
				if (exactVersionForOptimisticLockingRequired ? compareResult != 0 : compareResult < 0)
				{
					throw OptimisticLockUtil.throwModified(oriHelper.entityToObjRef(clone), versionToMerge, obj);
				}
			}
		}
	}

	protected void mergePrimitivesFieldBased(IEntityMetaData metaData, Object obj, Object clone, MergeHandle handle)
	{
		for (Member primitiveMember : metaData.getPrimitiveMembers())
		{
			if (!metaData.isMergeRelevant(primitiveMember))
			{
				continue;
			}
			Object objValue = primitiveMember.getValue(obj, true);
			Object cloneValue = primitiveMember.getValue(clone, true);
			if (!arePrimitivesEqual(metaData, primitiveMember, objValue, cloneValue, handle))
			{
				addModification(obj, primitiveMember.getName(), primitiveMember.getElementType(), objValue, cloneValue, handle);
				continue;
			}
		}
	}

	protected boolean arePrimitivesEqual(IEntityMetaData metaData, Member primitiveMember, Object objValue, Object cloneValue, MergeHandle handle)
	{
		if (objValue != null && cloneValue != null)
		{
			if (objValue.getClass().isArray() && cloneValue.getClass().isArray())
			{
				int objLength = Array.getLength(objValue);
				int cloneLength = Array.getLength(cloneValue);
				if (objLength != cloneLength)
				{
					return false;
				}
				for (int b = objLength; b-- > 0;)
				{
					Object objItem = Array.get(objValue, b);
					Object cloneItem = Array.get(cloneValue, b);
					if (!equalsObjects(objItem, cloneItem))
					{
						return false;
					}
				}
				return true;
			}
			else if (objValue instanceof List && cloneValue instanceof List)
			{
				List<?> objList = (List<?>) objValue;
				List<?> cloneList = (List<?>) cloneValue;
				if (objList.size() != cloneList.size())
				{
					return false;
				}
				for (int b = objList.size(); b-- > 0;)
				{
					Object objItem = objList.get(b);
					Object cloneItem = cloneList.get(b);
					if (!equalsObjects(objItem, cloneItem))
					{
						return false;
					}
				}
				return true;
			}
			else if (objValue instanceof Set && cloneValue instanceof Set)
			{
				Set<?> objColl = (Set<?>) objValue;
				Set<?> cloneColl = (Set<?>) cloneValue;
				if (objColl.size() != cloneColl.size())
				{
					return false;
				}
				return cloneColl.containsAll(objColl);
			}
			else if (objValue instanceof Iterable && cloneValue instanceof Iterable)
			{
				Iterator<?> objIter = ((Iterable<?>) objValue).iterator();
				Iterator<?> cloneIter = ((Iterable<?>) cloneValue).iterator();
				while (objIter.hasNext())
				{
					if (!cloneIter.hasNext())
					{
						return false;
					}
					Object objItem = objIter.next();
					Object cloneItem = cloneIter.next();
					if (!equalsObjects(objItem, cloneItem))
					{
						return false;
					}
				}
				if (cloneIter.hasNext())
				{
					return false;
				}
				return true;
			}
		}
		return equalsObjects(objValue, cloneValue);
	}

	protected boolean equalsObjects(Object left, Object right)
	{
		if (left == null)
		{
			return right == null;
		}
		if (right == null)
		{
			return false;
		}
		if (left.equals(right))
		{
			return true;
		}
		for (int a = 0, size = mergeExtensions.size(); a < size; a++)
		{
			IMergeExtension mergeExtension = mergeExtensions.get(a);
			if (mergeExtension.handlesType(left.getClass()))
			{
				return mergeExtension.equalsObjects(left, right);
			}
		}
		return false;
	}

	protected IList<IUpdateItem> addModification(Object obj, MergeHandle handle)
	{
		IList<IUpdateItem> modItemList = handle.objToModDict.get(obj);
		if (modItemList == null)
		{
			modItemList = new ArrayList<IUpdateItem>();
			handle.objToModDict.put(obj, modItemList);
		}
		return modItemList;
	}

	protected void addModification(Object obj, String memberName, Class<?> targetValueType, Object value, Object cloneValue, MergeHandle handle)
	{
		if (value != null && Collection.class.isAssignableFrom(value.getClass()) && ((Collection<?>) value).size() == 0)
		{
			return;
		}
		for (int a = 0, size = mergeExtensions.size(); a < size; a++)
		{
			IMergeExtension mergeExtension = mergeExtensions.get(a);
			if (mergeExtension.handlesType(targetValueType))
			{
				value = mergeExtension.extractPrimitiveValueToMerge(value);
			}
		}
		PrimitiveUpdateItem primModItem = new PrimitiveUpdateItem();
		primModItem.setMemberName(memberName);
		primModItem.setNewValue(value);

		IList<IUpdateItem> modItemList = addModification(obj, handle);
		modItemList.add(primModItem);
	}

	@SuppressWarnings("rawtypes")
	protected void addOriModification(Object obj, String memberName, Object value, Object cloneValue, MergeHandle handle)
	{
		if (value instanceof List)
		{
			List list = (List) value;
			for (int a = 0, size = list.size(); a < size; a++)
			{
				Object objItem = list.get(a);
				mergeOrPersist(objItem, handle);
			}
		}
		else if (value instanceof Collection)
		{
			Iterator<?> iter = ((Collection<?>) value).iterator();
			while (iter.hasNext())
			{
				Object objItem = iter.next();
				mergeOrPersist(objItem, handle);
			}
		}
		else
		{
			mergeOrPersist(value, handle);
		}
		try
		{
			IList<IObjRef> oldOriList = oriHelper.extractObjRefList(cloneValue, handle, handle.getOldOrList());
			IList<IObjRef> newOriList = oriHelper.extractObjRefList(value, handle, handle.getNewOrList());

			// Check unchanged ORIs
			for (int a = oldOriList.size(); a-- > 0;)
			{
				IObjRef oldOri = oldOriList.get(a);
				for (int b = newOriList.size(); b-- > 0;)
				{
					IObjRef newOri = newOriList.get(b);
					if (oldOri.equals(newOri))
					{
						// Old ORIs, which exist as new ORIs, too, are unchanged
						newOriList.remove(b);
						oldOriList.remove(a);
						break;
					}
				}
			}
			if (oldOriList.size() == 0 && newOriList.size() == 0)
			{
				return;
			}
			// Old ORIs are now handled as REMOVE, New ORIs as ADD
			RelationUpdateItem oriModItem = new RelationUpdateItem();
			oriModItem.setMemberName(memberName);
			if (oldOriList.size() > 0)
			{
				oriModItem.setRemovedORIs(oldOriList.toArray(new IObjRef[oldOriList.size()]));
			}
			if (newOriList.size() > 0)
			{
				oriModItem.setAddedORIs(newOriList.toArray(new IObjRef[newOriList.size()]));
			}

			IList<IUpdateItem> modItemList = addModification(obj, handle);

			modItemList.add(oriModItem);
		}
		finally
		{
			handle.getOldOrList().clear();
			handle.getNewOrList().clear();
		}
	}

	@SuppressWarnings("rawtypes")
	protected boolean isMemberModified(Object objValue, Object cloneValue, MergeHandle handle, IEntityMetaData metaData)
	{
		if (objValue == null)
		{
			return cloneValue != null;
		}
		if (cloneValue == null)
		{
			mergeDeepIntern(objValue, handle);
			return true;
		}
		if (objValue instanceof List)
		{
			List objList = (List) objValue;
			List cloneList = (List) cloneValue;

			boolean memberModified = false;

			if (objList.size() != cloneList.size())
			{
				memberModified = true;
			}
			for (int a = 0, size = objList.size(); a < size; a++)
			{
				Object objItem = objList.get(a);

				if (cloneList.size() > a)
				{
					Object cloneItem = cloneList.get(a);
					if (!equalsReferenceOrId(objItem, cloneItem, handle, metaData))
					{
						memberModified = true;
					}
				}
				mergeOrPersist(objItem, handle);
			}
			return memberModified;
		}
		if (objValue instanceof Iterable)
		{
			Iterator<?> objEnumerator = ((Iterable<?>) objValue).iterator();
			Iterator<?> cloneEnumerator = ((Iterable<?>) cloneValue).iterator();

			boolean memberModified = false;
			while (objEnumerator.hasNext())
			{
				Object objItem = objEnumerator.next();
				if (!cloneEnumerator.hasNext())
				{
					memberModified = true;
				}
				else
				{
					Object cloneItem = cloneEnumerator.next();
					if (!equalsReferenceOrId(objItem, cloneItem, handle, metaData))
					{
						memberModified = true;
					}
				}
				mergeOrPersist(objItem, handle);
			}
			if (cloneEnumerator.hasNext())
			{
				memberModified = true;
			}
			return memberModified;
		}
		mergeOrPersist(objValue, handle);
		return !equalsReferenceOrId(objValue, cloneValue, handle, metaData);
	}

	protected boolean equalsReferenceOrId(Object original, Object clone, MergeHandle handle, IEntityMetaData metaData)
	{
		if (original == null)
		{
			return clone == null;
		}
		if (clone == null)
		{
			return false;
		}
		Member keyMember = metaData.getIdMember();
		return EqualsUtil.equals(keyMember.getValue(clone, false), keyMember.getValue(original, false));
	}
}
