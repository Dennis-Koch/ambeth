package de.osthus.ambeth.merge;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.ICacheContext;
import de.osthus.ambeth.cache.IWritableCache;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.incremental.IIncrementalMergeState;
import de.osthus.ambeth.merge.incremental.IncrementalMergeState;
import de.osthus.ambeth.merge.incremental.IncrementalMergeState.StateEntry;
import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.model.IDirectObjRef;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IPrimitiveUpdateItem;
import de.osthus.ambeth.merge.model.IRelationUpdateItem;
import de.osthus.ambeth.merge.transfer.AbstractChangeContainer;
import de.osthus.ambeth.merge.transfer.CUDResult;
import de.osthus.ambeth.merge.transfer.CreateContainer;
import de.osthus.ambeth.merge.transfer.DeleteContainer;
import de.osthus.ambeth.merge.transfer.DirectObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.merge.transfer.RelationUpdateItem;
import de.osthus.ambeth.merge.transfer.UpdateContainer;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.RelationMember;
import de.osthus.ambeth.model.IDataObject;
import de.osthus.ambeth.proxy.IEntityMetaDataHolder;
import de.osthus.ambeth.proxy.IObjRefContainer;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerParamDelegate;
import de.osthus.ambeth.util.DirectValueHolderRef;
import de.osthus.ambeth.util.IPrefetchHelper;
import de.osthus.ambeth.util.ListUtil;
import de.osthus.ambeth.util.OptimisticLockUtil;

public class CUDResultApplier implements ICUDResultApplier
{
	private static class CloneState
	{
		public final IdentityHashMap<IObjRef, StateEntry> newObjRefToStateEntryMap;

		public final IncrementalMergeState incrementalState;

		public CloneState(IdentityHashMap<IObjRef, StateEntry> newObjRefToStateEntryMap, IncrementalMergeState incrementalState)
		{
			this.newObjRefToStateEntryMap = newObjRefToStateEntryMap;
			this.incrementalState = incrementalState;
		}
	}

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected ICacheContext cacheContext;

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IPrefetchHelper prefetchHelper;

	@Autowired
	protected IObjRefHelper objRefHelper;

	protected final ThreadLocal<CloneState> cloneStateTL = new ThreadLocal<CloneState>();

	@Override
	public IIncrementalMergeState acquireNewState(ICache stateCache)
	{
		return beanContext.registerBean(IncrementalMergeState.class)//
				.propertyValue("StateCache", stateCache)//
				.finish();
	}

	@Override
	public ICUDResult applyCUDResultOnEntitiesOfCache(ICUDResult cudResult, final boolean checkBaseState, final IIncrementalMergeState incrementalState)
	{
		ICache cache = incrementalState.getStateCache().getCurrentCache();
		if (cache.getCurrentCache() == cache)
		{
			// given cache is already the current cache
			return applyIntern(cudResult, checkBaseState, (IncrementalMergeState) incrementalState);
		}
		try
		{
			return cacheContext.executeWithCache(cache, new IResultingBackgroundWorkerParamDelegate<ICUDResult, ICUDResult>()
			{
				@Override
				public ICUDResult invoke(ICUDResult state) throws Throwable
				{
					return applyIntern(state, checkBaseState, (IncrementalMergeState) incrementalState);
				}
			}, cudResult);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected IList<Object> getAllExistingObjectsFromCache(ICache cache, List<IChangeContainer> allChanges)
	{
		ArrayList<IObjRef> existingObjRefs = new ArrayList<IObjRef>(allChanges.size());
		for (int a = 0, size = allChanges.size(); a < size; a++)
		{
			IChangeContainer changeContainer = allChanges.get(a);
			if (changeContainer instanceof CreateContainer)
			{
				existingObjRefs.add(null);
				continue;
			}
			if (changeContainer.getReference() instanceof IDirectObjRef)
			{
				throw new IllegalStateException();
			}
			existingObjRefs.add(changeContainer.getReference());
		}
		return cache.getObjects(existingObjRefs, CacheDirective.returnMisses());
	}

	protected ICUDResult applyIntern(ICUDResult cudResult, boolean checkBaseState, IncrementalMergeState incrementalState)
	{
		ICache stateCache = incrementalState.getStateCache();
		List<IChangeContainer> allChanges = cudResult.getAllChanges();
		List<Object> originalRefs = cudResult.getOriginalRefs();
		IList<Object> allObjects = getAllExistingObjectsFromCache(stateCache, allChanges);
		ArrayList<Object> hardRefs = new ArrayList<Object>();
		hardRefs.add(allObjects); // add list as item intended. adding each item of the source is NOT needed

		ArrayList<IObjRef> toFetchFromCache = new ArrayList<IObjRef>();
		ArrayList<DirectValueHolderRef> toPrefetch = new ArrayList<DirectValueHolderRef>();
		ArrayList<IBackgroundWorkerDelegate> runnables = new ArrayList<IBackgroundWorkerDelegate>();

		IEntityFactory entityFactory = this.entityFactory;

		IdentityHashMap<IObjRef, StateEntry> newObjRefToStateEntryMap = new IdentityHashMap<IObjRef, StateEntry>();
		IdentityHashMap<IChangeContainer, IChangeContainer> alreadyClonedMap = new IdentityHashMap<IChangeContainer, IChangeContainer>();

		ArrayList<IChangeContainer> newAllChanges = new ArrayList<IChangeContainer>(allChanges.size());

		for (int a = 0, size = allChanges.size(); a < size; a++)
		{
			IChangeContainer changeContainer = allChanges.get(a);
			Object originalEntity = originalRefs.get(a);

			StateEntry stateEntry = incrementalState.entityToStateMap.get(originalEntity);

			IChangeContainer newChangeContainer;
			if (changeContainer instanceof CreateContainer)
			{
				newChangeContainer = new CreateContainer();
			}
			else if (changeContainer instanceof UpdateContainer)
			{
				newChangeContainer = new UpdateContainer();
			}
			else
			{
				newChangeContainer = new DeleteContainer();
			}
			newAllChanges.add(newChangeContainer);
			alreadyClonedMap.put(changeContainer, newChangeContainer);

			if (!(changeContainer instanceof CreateContainer))
			{
				Object stateCacheEntity = allObjects.get(a);
				stateEntry = incrementalState.entityToStateMap.get(stateCacheEntity);
				if (stateEntry == null)
				{
					stateEntry = new StateEntry(stateCacheEntity, changeContainer.getReference(), incrementalState.entityToStateMap.size() + 1);

					incrementalState.entityToStateMap.put(stateCacheEntity, stateEntry);
					incrementalState.objRefToStateMap.put(stateEntry.objRef, stateEntry);
				}
				// delete & update do not need further handling
				continue;
			}
			Class<?> realType = changeContainer.getReference().getRealType();

			Object stateCacheEntity;
			if (stateEntry == null)
			{
				stateCacheEntity = entityFactory.createEntity(realType);

				DirectObjRef directObjRef = new DirectObjRef(realType, stateCacheEntity);
				directObjRef.setCreateContainerIndex(a);

				stateEntry = new StateEntry(stateCacheEntity, directObjRef, incrementalState.entityToStateMap.size() + 1);

				incrementalState.entityToStateMap.put(stateCacheEntity, stateEntry);
				incrementalState.objRefToStateMap.put(stateEntry.objRef, stateEntry);
				newObjRefToStateEntryMap.put(changeContainer.getReference(), stateEntry);
			}
			else
			{
				stateCacheEntity = stateEntry.entity;
			}
			allObjects.set(a, stateCacheEntity);
		}
		cloneStateTL.set(new CloneState(newObjRefToStateEntryMap, incrementalState));
		try
		{
			for (int a = allChanges.size(); a-- > 0;)
			{
				IChangeContainer changeContainer = allChanges.get(a);
				IObjRefContainer entity = (IObjRefContainer) allObjects.get(a);

				changeContainer = fillClonedChangeContainer(changeContainer, alreadyClonedMap);

				IPrimitiveUpdateItem[] puis;
				IRelationUpdateItem[] ruis;
				if (changeContainer instanceof CreateContainer)
				{
					CreateContainer createContainer = (CreateContainer) changeContainer;
					puis = createContainer.getPrimitives();
					ruis = createContainer.getRelations();
				}
				else if (changeContainer instanceof UpdateContainer)
				{
					UpdateContainer updateContainer = (UpdateContainer) changeContainer;
					puis = updateContainer.getPrimitives();
					ruis = updateContainer.getRelations();
				}
				else
				{
					((IDataObject) entity).setToBeDeleted(true);
					continue;
				}
				IEntityMetaData metaData = ((IEntityMetaDataHolder) entity).get__EntityMetaData();
				applyPrimitiveUpdateItems(entity, puis, metaData);

				if (ruis != null)
				{
					boolean isUpdate = changeContainer instanceof UpdateContainer;
					for (IRelationUpdateItem rui : ruis)
					{
						applyRelationUpdateItem(entity, rui, isUpdate, metaData, toPrefetch, toFetchFromCache, checkBaseState, runnables);
					}
				}
			}
			while (toPrefetch.size() > 0 || toFetchFromCache.size() > 0 || runnables.size() > 0)
			{
				if (toPrefetch.size() > 0)
				{
					prefetchHelper.prefetch(toPrefetch);
					toPrefetch.clear();
				}
				if (toFetchFromCache.size() > 0)
				{
					IList<Object> fetchedObjects = stateCache.getObjects(toFetchFromCache, CacheDirective.none());
					hardRefs.add(fetchedObjects); // add list as item intended. adding each item of the source is NOT needed
					toFetchFromCache.clear();
				}
				IBackgroundWorkerDelegate[] runnableArray = runnables.toArray(IBackgroundWorkerDelegate.class);
				runnables.clear();
				try
				{
					for (IBackgroundWorkerDelegate runnable : runnableArray)
					{
						runnable.invoke();
					}
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
			ArrayList<Object> newObjects = new ArrayList<Object>(allObjects.size());
			ArrayList<DirectValueHolderRef> changedRelationRefs = new ArrayList<DirectValueHolderRef>();
			for (int a = allObjects.size(); a-- > 0;)
			{
				IChangeContainer newChange = newAllChanges.get(a);
				IRelationUpdateItem[] ruis = null;
				Object entity = allObjects.get(a);
				if (newChange instanceof CreateContainer)
				{
					newObjects.add(entity);
					ruis = ((CreateContainer) newChange).getRelations();
				}
				else if (newChange instanceof UpdateContainer)
				{
					ruis = ((UpdateContainer) newChange).getRelations();
				}
				if (ruis == null)
				{
					continue;
				}
				IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entity.getClass());
				for (IRelationUpdateItem rui : ruis)
				{
					Member member = metaData.getMemberByName(rui.getMemberName());
					changedRelationRefs.add(new DirectValueHolderRef((IObjRefContainer) entity, (RelationMember) member));
				}
			}
			if (newObjects.size() > 0)
			{
				((IWritableCache) stateCache).put(newObjects);
			}
			if (changedRelationRefs.size() > 0)
			{
				prefetchHelper.prefetch(changedRelationRefs);
			}
			return new CUDResult(newAllChanges, allObjects);
		}
		finally
		{
			cloneStateTL.set(null);
		}
	}

	protected IChangeContainer fillClonedChangeContainer(IChangeContainer original, IdentityHashMap<IChangeContainer, IChangeContainer> alreadyClonedMap)
	{
		IChangeContainer clone = alreadyClonedMap.get(original);
		if (clone instanceof CreateContainer)
		{
			((CreateContainer) clone).setPrimitives(clonePrimitives(((CreateContainer) original).getPrimitives()));
			((CreateContainer) clone).setRelations(cloneRelations(((CreateContainer) original).getRelations()));
		}
		else if (clone instanceof UpdateContainer)
		{
			((UpdateContainer) clone).setPrimitives(clonePrimitives(((UpdateContainer) original).getPrimitives()));
			((UpdateContainer) clone).setRelations(cloneRelations(((UpdateContainer) original).getRelations()));
		}
		((AbstractChangeContainer) clone).setReference(cloneObjRef(original.getReference(), true));
		return clone;
	}

	protected IRelationUpdateItem[] cloneRelations(IRelationUpdateItem[] original)
	{
		if (original == null)
		{
			return null;
		}
		IRelationUpdateItem[] clone = new IRelationUpdateItem[original.length];
		for (int a = original.length; a-- > 0;)
		{
			clone[a] = cloneRelation(original[a]);
		}
		return clone;
	}

	protected IPrimitiveUpdateItem[] clonePrimitives(IPrimitiveUpdateItem[] original)
	{
		// no need to clone PUIs. even the array is assumed to be never modified
		return original;
	}

	protected IRelationUpdateItem cloneRelation(IRelationUpdateItem original)
	{
		RelationUpdateItem clone = new RelationUpdateItem();
		clone.setMemberName(original.getMemberName());
		clone.setAddedORIs(cloneObjRefs(original.getAddedORIs()));
		clone.setRemovedORIs(cloneObjRefs(original.getRemovedORIs()));
		return clone;
	}

	protected IObjRef[] cloneObjRefs(IObjRef[] original)
	{
		if (original == null || original.length == 0)
		{
			return original;
		}
		IObjRef[] clone = new IObjRef[original.length];
		for (int a = original.length; a-- > 0;)
		{
			clone[a] = cloneObjRef(original[a], true);
		}
		return clone;
	}

	protected IObjRef cloneObjRef(IObjRef original, boolean fromChangeContainer)
	{
		CloneState cloneState = cloneStateTL.get();
		return resolveObjRefOfCache(original, cloneState);

	}

	protected void applyPrimitiveUpdateItems(Object entity, IPrimitiveUpdateItem[] puis, IEntityMetaData metadata)
	{
		if (puis == null)
		{
			return;
		}

		for (IPrimitiveUpdateItem pui : puis)
		{
			String memberName = pui.getMemberName();
			Object newValue = pui.getNewValue();
			Member member = metadata.getMemberByName(memberName);
			member.setValue(entity, newValue);
		}
	}

	protected void applyRelationUpdateItem(final IObjRefContainer entity, final IRelationUpdateItem rui, final boolean isUpdate,
			final IEntityMetaData metaData, final IList<DirectValueHolderRef> toPrefetch, final IList<IObjRef> toFetchFromCache, final boolean checkBaseState,
			final IList<IBackgroundWorkerDelegate> runnables)
	{
		IObjRefHelper objRefHelper = this.objRefHelper;
		String memberName = rui.getMemberName();
		int relationIndex = metaData.getIndexByRelationName(memberName);
		final RelationMember relationMember = metaData.getRelationMembers()[relationIndex];
		IObjRef[] existingORIs;
		if (entity.is__Initialized(relationIndex))
		{
			existingORIs = objRefHelper.extractObjRefList(relationMember.getValue(entity), null).toArray(IObjRef.class);
		}
		else
		{
			existingORIs = entity.get__ObjRefs(relationIndex);
			if (existingORIs == null)
			{
				toPrefetch.add(new DirectValueHolderRef(entity, relationMember, true));
				runnables.add(new IBackgroundWorkerDelegate()
				{
					@Override
					public void invoke() throws Throwable
					{
						applyRelationUpdateItem(entity, rui, isUpdate, metaData, toPrefetch, toFetchFromCache, checkBaseState, runnables);
					}
				});
				return;
			}
		}
		IObjRef[] addedORIs = rui.getAddedORIs();
		IObjRef[] removedORIs = rui.getRemovedORIs();

		final IObjRef[] newORIs;
		if (existingORIs.length == 0)
		{
			if (checkBaseState && removedORIs != null)
			{
				throw new IllegalArgumentException("Removing from empty member");
			}
			newORIs = addedORIs != null ? Arrays.copyOf(addedORIs, addedORIs.length) : ObjRef.EMPTY_ARRAY;
			for (int a = newORIs.length; a-- > 0;)
			{
				newORIs[a] = cloneObjRef(newORIs[a], false);
			}
		}
		else
		{
			// Set to efficiently remove entries
			LinkedHashSet<IObjRef> existingORIsSet = new LinkedHashSet<IObjRef>(existingORIs);
			if (removedORIs != null)
			{
				for (IObjRef removedORI : removedORIs)
				{
					IObjRef clonedObjRef = cloneObjRef(removedORI, false);
					if (existingORIsSet.remove(clonedObjRef) || !checkBaseState)
					{
						continue;
					}
					throw OptimisticLockUtil.throwModified(objRefHelper.entityToObjRef(entity), null, entity);
				}
			}
			if (addedORIs != null)
			{
				for (IObjRef addedORI : addedORIs)
				{
					IObjRef clonedObjRef = cloneObjRef(addedORI, false);
					if (existingORIsSet.add(clonedObjRef) || !checkBaseState)
					{
						continue;
					}
					throw OptimisticLockUtil.throwModified(objRefHelper.entityToObjRef(entity), null, entity);
				}
			}
			if (existingORIsSet.size() == 0)
			{
				newORIs = ObjRef.EMPTY_ARRAY;
			}
			else
			{
				newORIs = existingORIsSet.toArray(IObjRef.class);
			}
		}
		if (!entity.is__Initialized(relationIndex))
		{
			entity.set__ObjRefs(relationIndex, newORIs);
			return;
		}
		toFetchFromCache.addAll(newORIs);
		runnables.add(new IBackgroundWorkerDelegate()
		{
			@Override
			public void invoke() throws Throwable
			{
				ICache stateCache = cloneStateTL.get().incrementalState.getStateCache();
				IList<Object> objects = stateCache.getObjects(newORIs, CacheDirective.failEarly());
				Object value;
				if (relationMember.isToMany())
				{
					// To-many relation
					Collection<Object> coll = ListUtil.createObservableCollectionOfType(relationMember.getRealType(), objects.size());
					coll.addAll(objects);
					value = coll;
				}
				else
				{
					// To-one relation
					value = objects.size() > 0 ? objects.get(0) : null;
				}
				relationMember.setValue(entity, value);
			}
		});
	}

	protected IObjRef resolveObjRefOfCache(IObjRef objRef, CloneState cloneState)
	{
		StateEntry stateEntry = cloneState.incrementalState.objRefToStateMap.get(objRef);
		if (stateEntry != null)
		{
			return stateEntry.objRef;
		}
		stateEntry = cloneState.newObjRefToStateEntryMap.get(objRef);
		if (stateEntry != null)
		{
			return stateEntry.objRef;
		}
		return objRef;
	}
}
