package de.osthus.ambeth.inmemory;

import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import de.osthus.ambeth.cache.AbstractCacheValue;
import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.IRootCache;
import de.osthus.ambeth.cache.RootCache;
import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.cache.transfer.LoadContainer;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.compositeid.ICompositeIdFactory;
import de.osthus.ambeth.copy.IObjectCopier;
import de.osthus.ambeth.database.ITransactionListener;
import de.osthus.ambeth.event.DatabaseAcquireEvent;
import de.osthus.ambeth.event.DatabaseFailEvent;
import de.osthus.ambeth.event.IEventListener;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IMergeServiceExtension;
import de.osthus.ambeth.merge.ITransactionState;
import de.osthus.ambeth.merge.IValueObjectConfig;
import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IOriCollection;
import de.osthus.ambeth.merge.model.IPrimitiveUpdateItem;
import de.osthus.ambeth.merge.model.IRelationUpdateItem;
import de.osthus.ambeth.merge.transfer.CreateContainer;
import de.osthus.ambeth.merge.transfer.DeleteContainer;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.merge.transfer.OriCollection;
import de.osthus.ambeth.merge.transfer.UpdateContainer;
import de.osthus.ambeth.model.IMethodDescription;
import de.osthus.ambeth.persistence.IContextProvider;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.proxy.PersistenceContext.PersistenceContextType;
import de.osthus.ambeth.service.ChangeAggregator;
import de.osthus.ambeth.service.ICacheRetriever;
import de.osthus.ambeth.service.IChangeAggregator;
import de.osthus.ambeth.typeinfo.IRelationInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.Lock;

@PersistenceContext(PersistenceContextType.NOT_REQUIRED)
public class SimpleInMemoryDatabase implements ICacheRetriever, IMergeServiceExtension, IInitializingBean, IEventListener, ITransactionListener,
		IInMemoryDatabase
{
	protected static final Set<CacheDirective> failEntryLoadContainerResult = EnumSet.of(CacheDirective.FailEarly, CacheDirective.LoadContainerResult);

	protected static final Object[] EMPTY_PRIMITIVES = new Object[0];

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected ICompositeIdFactory compositeIdFactory;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IDatabase database;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IObjectCopier objectCopier;

	@Autowired
	protected ITransactionState transactionState;

	protected RootCache committedData;

	protected final java.util.concurrent.locks.Lock sessionLock = new ReentrantLock();

	protected final java.util.concurrent.locks.Lock sequenceLock = new ReentrantLock();

	protected final HashMap<Long, SimpleInMemorySession> sessionToStateMap = new HashMap<Long, SimpleInMemorySession>();

	protected long sequenceValue = 0;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		committedData = beanContext.registerAnonymousBean(RootCache.class).propertyValue("WeakEntries", Boolean.FALSE)
				.propertyValue("Privileged", Boolean.FALSE).ignoreProperties("CacheRetriever", "EventQueue").finish();
	}

	//
	// protected LoadContainer createLoadContainer(IObjRef ori, Object entity)
	// {
	// LoadContainer loadContainer = new LoadContainer();
	// IEntityMetaData metaData = this.entityMetaDataProvider.getMetaData(construction.getClass());
	//
	// // if (((Long) construction.getId()).equals(ori.getId()))
	// // {
	// loadContainer.setPrimitives(this.cacheHelper.extractPrimitives(metaData, construction));
	// loadContainer.setRelations(this.cacheHelper.extractRelations(metaData, construction));
	// // }
	// IObjRef oriForResult = this.objRefHelper.entityToObjRef(construction);
	// loadContainer.setReference(oriForResult);
	//
	// return loadContainer;
	// }

	@Override
	public void initialSetup(Collection<?> entities)
	{
		if (transactionState.isTransactionActive())
		{
			throw new UnsupportedOperationException();
		}
		committedData.put(entities);
	}

	@Override
	public void handlePreCommit()
	{
		handleDatabaseCommit(Long.valueOf(database.getSessionId()));
	}

	@Override
	public void handleEvent(Object eventObject, long dispatchTime, long sequenceId) throws Exception
	{
		if (eventObject instanceof DatabaseAcquireEvent)
		{
			handleDatabaseAcquire(Long.valueOf(((DatabaseAcquireEvent) eventObject).getSessionId()));
			return;
		}
		if (eventObject instanceof DatabaseFailEvent)
		{
			handleDatabaseFail(Long.valueOf(((DatabaseFailEvent) eventObject).getSessionId()));
			return;
		}
	}

	protected void handleDatabaseAcquire(Long sessionId)
	{
		Lock writeLock = committedData.getWriteLock();
		writeLock.lock();
		try
		{
			RootCache transactionalData = beanContext.registerAnonymousBean(RootCache.class).propertyValue("WeakEntries", Boolean.FALSE)
					.propertyValue("Privileged", Boolean.FALSE).propertyValue("CacheRetriever", committedData).ignoreProperties("EventQueue").finish();
			sessionToStateMap.put(sessionId, new SimpleInMemorySession(transactionalData));
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected void handleDatabaseFail(Long sessionId)
	{
		sessionLock.lock();
		try
		{
			SimpleInMemorySession state = sessionToStateMap.remove(sessionId);
			if (state != null)
			{
				state.dispose();
			}
		}
		finally
		{
			sessionLock.unlock();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void handleDatabaseCommit(Long sessionId)
	{
		SimpleInMemorySession state = null;
		try
		{
			sessionLock.lock();
			try
			{
				state = sessionToStateMap.remove(sessionId);
			}
			finally
			{
				sessionLock.unlock();
			}
			if (state == null || (state.createdObjRefs.size() == 0 && state.updatedObjRefs.size() == 0 && state.deletedObjRefs.size() == 0))
			{
				return;
			}
			// Save information into second level cache for committed data
			IList<IObjRef> deletedObjRefs = state.deletedObjRefs.toList();
			IList<IObjRef> createdObjRefs = state.createdObjRefs.toList();
			IList<IObjRef> updatedObjRefs = state.updatedObjRefs.toList();
			List updatedContent = state.data.getObjects(updatedObjRefs, CacheDirective.cacheValueResult());
			List createdContent = state.data.getObjects(createdObjRefs, CacheDirective.cacheValueResult());
			Lock writeLock = committedData.getWriteLock();
			writeLock.lock();
			try
			{
				ArrayList<IObjRef> existingObjRefs = new ArrayList<IObjRef>();
				existingObjRefs.addAll(deletedObjRefs);
				existingObjRefs.addAll(updatedObjRefs);

				ArrayList<Object> changedContent = new ArrayList<Object>();
				changedContent.addAll(createdContent);
				changedContent.addAll(updatedContent);

				if (existingObjRefs.size() > 0)
				{
					IList<Object> existingCommittedValues = committedData.getObjects(existingObjRefs,
							EnumSet.of(CacheDirective.ReturnMisses, CacheDirective.CacheValueResult));
					for (int a = existingCommittedValues.size(); a-- > 0;)
					{
						IObjRef objRef = existingObjRefs.get(a);
						AbstractCacheValue existingCommittedValue = (AbstractCacheValue) existingCommittedValues.get(a);
						if (existingCommittedValue == null)
						{
							throw new OptimisticLockException("Object not found or outdated: " + objRef);
						}
						IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRef.getRealType());
						checkVersionForOptimisticLock(metaData, objRef, existingCommittedValue);
					}
				}
				if (deletedObjRefs.size() > 0)
				{
					committedData.remove(deletedObjRefs);
				}
				if (changedContent.size() > 0)
				{
					committedData.put(changedContent);
				}
			}
			finally
			{
				writeLock.unlock();
			}
		}
		finally
		{
			if (state != null)
			{
				state.dispose();
			}
		}
	}

	protected IRootCache getData()
	{
		if (transactionState.isTransactionActive())
		{
			sessionLock.lock();
			try
			{
				return sessionToStateMap.get(Long.valueOf(database.getSessionId())).data;
			}
			finally
			{
				sessionLock.unlock();
			}
		}
		return committedData;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<ILoadContainer> getEntities(List<IObjRef> orisToLoad)
	{
		IList result = getData().getObjects(orisToLoad, CacheDirective.loadContainerResult());
		return result;
	}

	@Override
	public List<IObjRelationResult> getRelations(List<IObjRelation> objRelations)
	{
		return getData().getObjRelations(objRelations, null, CacheDirective.none());
	}

	@Override
	@PersistenceContext(PersistenceContextType.REQUIRED)
	public IOriCollection merge(ICUDResult cudResult, IMethodDescription methodDescription)
	{
		if (!transactionState.isTransactionActive())
		{
			throw new IllegalStateException("No transaction active. This operation-mode is currently not supported!");
		}
		List<IChangeContainer> changes = cudResult.getAllChanges();
		IdentityHashMap<IObjRef, IObjRef> givenObjRefToCopyMap = new IdentityHashMap<IObjRef, IObjRef>();
		IdentityHashMap<IObjRef, ILoadContainer> alreadyAcquiredLoadContainerMap = new IdentityHashMap<IObjRef, ILoadContainer>();

		SimpleInMemorySession state = null;
		sessionLock.lock();
		try
		{
			state = sessionToStateMap.get(Long.valueOf(database.getSessionId()));
		}
		finally
		{
			sessionLock.unlock();
		}
		ArrayList<IObjRef> objRefList = new ArrayList<IObjRef>(changes.size());
		LoadContainer[] newLCs = new LoadContainer[changes.size()];
		de.osthus.ambeth.util.Lock writeLock = committedData.getWriteLock();
		writeLock.lock();
		try
		{
			buildCopyOfAllObjRefs(changes, givenObjRefToCopyMap);
			for (int a = 0, size = changes.size(); a < size; a++)
			{
				IChangeContainer changeContainer = changes.get(a);
				IObjRef objRef = changeContainer.getReference();
				objRef = givenObjRefToCopyMap.get(objRef);
				IEntityMetaData metaData = entityMetaDataProvider.getMetaData(objRef.getRealType());
				if (changeContainer instanceof DeleteContainer)
				{
					ILoadContainer existingLC = (ILoadContainer) committedData.getObject(objRef, CacheDirective.loadContainerResult());
					if (existingLC == null)
					{
						throw new OptimisticLockException("Object not found to delete: " + objRef);
					}
					checkVersionForOptimisticLock(metaData, objRef, existingLC);
					continue;
				}
				boolean isUpdate = (changeContainer instanceof UpdateContainer);
				ILoadContainer oldLC = null;
				IPrimitiveUpdateItem[] puis;
				IRelationUpdateItem[] ruis;
				if (isUpdate)
				{
					oldLC = (ILoadContainer) committedData.getObject(objRef, CacheDirective.loadContainerResult());
					if (oldLC == null)
					{
						throw new OptimisticLockException("Object not found to update: " + objRef);
					}
					checkVersionForOptimisticLock(metaData, objRef, oldLC);
					alreadyAcquiredLoadContainerMap.put(objRef, oldLC);
					puis = ((UpdateContainer) changeContainer).getPrimitives();
					ruis = ((UpdateContainer) changeContainer).getRelations();
				}
				else
				{
					puis = ((CreateContainer) changeContainer).getPrimitives();
					ruis = ((CreateContainer) changeContainer).getRelations();
				}

				LoadContainer newLC = createNewLoadContainer(metaData, objRef, isUpdate);
				if (isUpdate)
				{
					Object[] primitives = oldLC.getPrimitives();
					if (primitives.length > 0)
					{
						Object[] newPrimitives = newLC.getPrimitives();
						System.arraycopy(primitives, 0, newPrimitives, 0, primitives.length);
					}
					setPrimitives(metaData, puis, newLC);
					setUpdated(metaData, newLC);
					IObjRef[][] relations = oldLC.getRelations();
					if (relations.length > 0)
					{
						IObjRef[][] newRelations = newLC.getRelations();
						System.arraycopy(relations, 0, newRelations, 0, relations.length);
						for (int b = newRelations.length; b-- > 0;)
						{
							IObjRef[] relationsOfMember = newRelations[b];
							if (relationsOfMember == null || relationsOfMember.length == 0)
							{
								continue;
							}
							IObjRef[] newRelationsOfMember = new IObjRef[relationsOfMember.length];
							for (int c = relationsOfMember.length; c-- > 0;)
							{
								newRelationsOfMember[c] = dupIfNecessary(relationsOfMember[c], givenObjRefToCopyMap);
							}
							newRelations[b] = newRelationsOfMember;
						}
					}
				}
				else
				{
					setPrimitives(metaData, puis, newLC);
					setCreated(metaData, newLC);
					if (metaData.getVersionMember() == null)
					{
						newLC.getReference().setVersion(null);
					}
					else
					{
						Object version = conversionHelper.convertValueToType(metaData.getVersionMember().getRealType(), Integer.valueOf(1));
						newLC.getReference().setVersion(version);
					}
				}
				setRelations(metaData, ruis, newLC, givenObjRefToCopyMap);
				newLCs[a] = newLC;
			}
			doChanges(newLCs, changes, objRefList, givenObjRefToCopyMap, state);

			OriCollection oriCollection = new OriCollection(objectCopier.clone(objRefList));
			IContextProvider contextProvider = database.getContextProvider();
			oriCollection.setChangedBy(contextProvider.getCurrentUser());
			oriCollection.setChangedOn(contextProvider.getCurrentTime());
			return oriCollection;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected IObjRef dupIfNecessary(IObjRef objRef, IMap<IObjRef, IObjRef> givenObjRefToCopyMap)
	{
		IObjRef objRefDup = givenObjRefToCopyMap.get(objRef);
		if (objRefDup != null)
		{
			return objRefDup;
		}
		objRefDup = dupObjRef(objRef);
		givenObjRefToCopyMap.put(objRef, objRefDup);
		return objRefDup;
	}

	protected void buildCopyOfAllObjRefs(List<IChangeContainer> changes, IMap<IObjRef, IObjRef> givenObjRefToCopyMap)
	{
		sequenceLock.lock();
		try
		{
			for (int a = 0, size = changes.size(); a < size; a++)
			{
				buildCopyOfAllObjRefs(changes.get(a), givenObjRefToCopyMap);
			}
		}
		finally
		{
			sequenceLock.unlock();
		}
	}

	protected void buildCopyOfAllObjRefs(IChangeContainer changeContainer, IMap<IObjRef, IObjRef> givenObjRefToCopyMap)
	{
		if (changeContainer instanceof DeleteContainer)
		{
			dupIfNecessary(changeContainer.getReference(), givenObjRefToCopyMap);
			return;
		}
		IObjRef objRef = changeContainer.getReference();
		IRelationUpdateItem[] ruis = null;
		if (changeContainer instanceof CreateContainer)
		{
			IObjRef newObjRef = givenObjRefToCopyMap.get(objRef);
			if (newObjRef == null)
			{
				newObjRef = new ObjRef(objRef.getRealType(), ObjRef.PRIMARY_KEY_INDEX, Long.valueOf(++sequenceValue), null);
				givenObjRefToCopyMap.put(objRef, newObjRef);
				givenObjRefToCopyMap.put(newObjRef, newObjRef);
			}
			ruis = ((CreateContainer) changeContainer).getRelations();
		}
		else
		{
			dupIfNecessary(changeContainer.getReference(), givenObjRefToCopyMap);
			ruis = ((UpdateContainer) changeContainer).getRelations();
		}
		if (ruis == null)
		{
			return;
		}
		for (IRelationUpdateItem rui : ruis)
		{
			IObjRef[] addedORIs = rui.getAddedORIs();
			if (addedORIs != null)
			{
				for (IObjRef addedORI : addedORIs)
				{
					dupIfNecessary(addedORI, givenObjRefToCopyMap);
					;
				}
			}
			IObjRef[] removedORIs = rui.getRemovedORIs();
			if (removedORIs != null)
			{
				for (IObjRef removedORI : removedORIs)
				{
					dupIfNecessary(removedORI, givenObjRefToCopyMap);
				}
			}
		}
	}

	protected LoadContainer createNewLoadContainer(IEntityMetaData metaData, IObjRef objRef, boolean isUpdate)
	{
		if (metaData.getVersionMember() == null)
		{
			objRef.setVersion(null);
		}
		else if (isUpdate)
		{
			Number oldVersion = conversionHelper.convertValueToType(Number.class, objRef.getVersion());
			Object version = conversionHelper.convertValueToType(metaData.getVersionMember().getRealType(), Long.valueOf(oldVersion.longValue() + 1));
			objRef.setVersion(version);
		}
		else
		{
			Object version = conversionHelper.convertValueToType(metaData.getVersionMember().getRealType(), Integer.valueOf(1));
			objRef.setVersion(version);
		}
		LoadContainer newLC = new LoadContainer();
		newLC.setReference(objRef);
		newLC.setPrimitives(metaData.getPrimitiveMembers().length == 0 ? EMPTY_PRIMITIVES : new Object[metaData.getPrimitiveMembers().length]);
		newLC.setRelations(metaData.getRelationMembers().length == 0 ? ObjRef.EMPTY_ARRAY_ARRAY : new IObjRef[metaData.getRelationMembers().length][]);
		return newLC;
	}

	protected IObjRef dupObjRef(IObjRef objRef)
	{
		return new ObjRef(objRef.getRealType(), objRef.getIdNameIndex(), objRef.getId(), objRef.getVersion());
	}

	protected void doChanges(ILoadContainer[] newLCs, List<IChangeContainer> changes, List<IObjRef> objRefList, Map<IObjRef, IObjRef> givenToInternalObjRefMap,
			SimpleInMemorySession state)
	{
		IChangeAggregator changeAggregator = beanContext.registerAnonymousBean(ChangeAggregator.class).finish();
		IList<IObjRef> toRemove = new ArrayList<IObjRef>();
		for (int a = newLCs.length; a-- > 0;)
		{
			IObjRef objRef = givenToInternalObjRefMap.get(changes.get(a).getReference());
			toRemove.add(objRef);
		}
		state.data.remove(toRemove);
		state.data.put(newLCs);
		for (int a = newLCs.length; a-- > 0;)
		{
			ILoadContainer newLC = newLCs[a];
			IObjRef oldObjRef = dupObjRef(changes.get(a).getReference());
			if (newLC == null)
			{
				objRefList.add(null);
				changeAggregator.dataChangeDelete(oldObjRef);
				if (state.createdObjRefs.remove(oldObjRef))
				{
					// object has been created & deleted within same session
					// nothing to do regarding commit
					continue;
				}
				if (state.updatedObjRefs.remove(oldObjRef))
				{
					// object has been updated & deleted within same session
				}
				// so our delete is the real thing we are interested in
				state.deletedObjRefs.add(oldObjRef); // intentionally the "oldObjRef" because on the OptimisticLockCheck in preCommit we need the ORIGINAL
				// version
				continue;
			}
			IObjRef objRef = newLC.getReference();
			objRefList.add(objRef);

			if (changes.get(a) instanceof CreateContainer)
			{
				changeAggregator.dataChangeInsert(objRef);
				state.createdObjRefs.add(objRef);
			}
			else
			{
				if (state.createdObjRefs.contains(objRef))
				{
					// object has been created & update within same session
					// so the "created" is the main important thing regarding commit
					continue;
				}
				state.updatedObjRefs.add(oldObjRef); // intentionally the "oldObjRef" because on the OptimisticLockCheck in preCommit we need the ORIGINAL
														// version
				changeAggregator.dataChangeUpdate(objRef);
			}
		}
		changeAggregator.createDataChange();
	}

	@SuppressWarnings("unchecked")
	protected void checkVersionForOptimisticLock(IEntityMetaData metaData, IObjRef objRef, Object oldLC)
	{
		Object requestedVersion = conversionHelper.convertValueToType(metaData.getVersionMember().getRealType(), objRef.getVersion());
		if (requestedVersion == null)
		{
			throw new OptimisticLockException("Mandatory entity version not provided: " + objRef);
		}
		Object existingVersion;
		if (oldLC instanceof ILoadContainer)
		{
			existingVersion = ((ILoadContainer) oldLC).getReference().getVersion();
		}
		else
		{
			existingVersion = ((AbstractCacheValue) oldLC).getVersion();
		}
		if (((Comparable<Object>) requestedVersion).compareTo(existingVersion) != 0)
		{
			throw new OptimisticLockException("Provided entity version not valid: " + objRef + ". Expected version: " + existingVersion);
		}
	}

	protected void setCreated(IEntityMetaData metaData, ILoadContainer lc)
	{
		IContextProvider contextProvider = database.getContextProvider();
		String currentUser = contextProvider.getCurrentUser();
		Long currentTime = contextProvider.getCurrentTime();
		Object[] primitives = lc.getPrimitives();
		if (metaData.getCreatedByMember() != null)
		{
			int primitiveIndex = metaData.getIndexByPrimitive(metaData.getCreatedByMember());
			primitives[primitiveIndex] = currentUser;
		}
		if (metaData.getUpdatedByMember() != null)
		{
			int primitiveIndex = metaData.getIndexByPrimitive(metaData.getUpdatedByMember());
			primitives[primitiveIndex] = currentTime;
		}
	}

	protected void setUpdated(IEntityMetaData metaData, ILoadContainer lc)
	{
		IContextProvider contextProvider = database.getContextProvider();
		String currentUser = contextProvider.getCurrentUser();
		Long currentTime = contextProvider.getCurrentTime();
		Object[] primitives = lc.getPrimitives();
		if (metaData.getUpdatedByMember() != null)
		{
			int primitiveIndex = metaData.getIndexByPrimitive(metaData.getUpdatedByMember());
			primitives[primitiveIndex] = currentUser;
		}
		if (metaData.getUpdatedOnMember() != null)
		{
			int primitiveIndex = metaData.getIndexByPrimitive(metaData.getUpdatedOnMember());
			primitives[primitiveIndex] = currentTime;
		}
	}

	protected void setPrimitives(IEntityMetaData metaData, IPrimitiveUpdateItem[] puis, ILoadContainer lc)
	{
		if (puis == null)
		{
			return;
		}
		Object[] primitives = lc.getPrimitives();
		ITypeInfoItem[] primitiveMembers = metaData.getPrimitiveMembers();
		for (int a = puis.length; a-- > 0;)
		{
			IPrimitiveUpdateItem pui = puis[a];
			int primitiveIndex = metaData.getIndexByPrimitiveName(pui.getMemberName());
			ITypeInfoItem primitiveMember = primitiveMembers[primitiveIndex];

			Object value = conversionHelper.convertValueToType(primitiveMember.getRealType(), pui.getNewValue());
			if (value instanceof Date)
			{
				// optimize later clone performance (because Long is immutable)
				value = conversionHelper.convertValueToType(Long.class, value);
			}
			primitives[primitiveIndex] = value;
		}
	}

	protected void setRelations(IEntityMetaData metaData, IRelationUpdateItem[] ruis, ILoadContainer lc, Map<IObjRef, IObjRef> givenToInternalObjRefMap)
	{
		if (ruis == null)
		{
			return;
		}
		IObjRef primaryObjRef = lc.getReference();
		IObjRef[][] relations = lc.getRelations();
		IRelationInfoItem[] relationMembers = metaData.getRelationMembers();
		for (int a = ruis.length; a-- > 0;)
		{
			IRelationUpdateItem rui = ruis[a];
			int relationIndex = metaData.getIndexByRelationName(rui.getMemberName());
			IRelationInfoItem relationMember = relationMembers[relationIndex];

			IObjRef[] existingObjRefs = relations[relationIndex];
			ISet<IObjRef> existingObjRefsSet = existingObjRefs != null ? new HashSet<IObjRef>(existingObjRefs) : new HashSet<IObjRef>();
			IObjRef[] addedObjRefs = rui.getAddedORIs();
			IObjRef[] removedObjRefs = rui.getRemovedORIs();
			if (removedObjRefs != null)
			{
				for (IObjRef removedObjRef : removedObjRefs)
				{

					if (!existingObjRefsSet.remove(removedObjRef))
					{
						throw new PersistenceException("Relation to remove does not exist: " + removedObjRef + " on member '" + relationMember.getName() + "' "
								+ lc.getReference() + "");
					}
				}
			}
			if (addedObjRefs != null)
			{
				for (IObjRef addedObjRef : addedObjRefs)
				{
					if (!existingObjRefsSet.add(givenToInternalObjRefMap.get(addedObjRef)))
					{
						throw new PersistenceException("Relation to add does already exist: " + addedObjRef + " on member '" + relationMember.getName() + "' "
								+ lc.getReference() + "");
					}
				}
			}
			relations[relationIndex] = existingObjRefsSet.size() == 0 ? ObjRef.EMPTY_ARRAY : existingObjRefsSet.toArray(IObjRef.class);
		}
	}

	@Override
	public List<IEntityMetaData> getMetaData(List<Class<?>> entityTypes)
	{
		throw new UnsupportedOperationException("Must never happen");
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(Class<?> valueType)
	{
		throw new UnsupportedOperationException("Must never happen");
	}
}