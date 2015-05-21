package de.osthus.ambeth.service;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import de.osthus.ambeth.cache.AbstractCacheValue;
import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.cache.IRootCache;
import de.osthus.ambeth.cache.RootCache;
import de.osthus.ambeth.cache.rootcachevalue.RootCacheValue;
import de.osthus.ambeth.change.CreateCommand;
import de.osthus.ambeth.change.DeleteCommand;
import de.osthus.ambeth.change.IChangeCommand;
import de.osthus.ambeth.change.ICreateCommand;
import de.osthus.ambeth.change.ILinkChangeCommand;
import de.osthus.ambeth.change.IRowCommand;
import de.osthus.ambeth.change.ITableChange;
import de.osthus.ambeth.change.IUpdateCommand;
import de.osthus.ambeth.change.LinkContainer;
import de.osthus.ambeth.change.LinkTableChange;
import de.osthus.ambeth.change.TableChange;
import de.osthus.ambeth.change.UpdateCommand;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.collections.InterfaceFastList;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.compositeid.ICompositeIdFactory;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IMergeServiceExtension;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.IValueObjectConfig;
import de.osthus.ambeth.merge.incremental.IIncrementalMergeState;
import de.osthus.ambeth.merge.model.CreateOrUpdateContainerBuild;
import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.model.ICreateOrUpdateContainer;
import de.osthus.ambeth.merge.model.IDirectObjRef;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IOriCollection;
import de.osthus.ambeth.merge.model.IPrimitiveUpdateItem;
import de.osthus.ambeth.merge.model.IRelationUpdateItem;
import de.osthus.ambeth.merge.model.RelationUpdateItemBuild;
import de.osthus.ambeth.merge.transfer.CUDResult;
import de.osthus.ambeth.merge.transfer.CreateContainer;
import de.osthus.ambeth.merge.transfer.DeleteContainer;
import de.osthus.ambeth.merge.transfer.DirectObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.merge.transfer.OriCollection;
import de.osthus.ambeth.merge.transfer.UpdateContainer;
import de.osthus.ambeth.metadata.IObjRefFactory;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.PrimitiveMember;
import de.osthus.ambeth.metadata.RelationMember;
import de.osthus.ambeth.model.IMethodDescription;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.IContextProvider;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IDatabaseMetaData;
import de.osthus.ambeth.persistence.IDirectedLinkMetaData;
import de.osthus.ambeth.persistence.IPrimaryKeyProvider;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.persistence.ITableMetaData;
import de.osthus.ambeth.persistence.parallel.IModifyingDatabase;
import de.osthus.ambeth.proxy.IEntityMetaDataHolder;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.security.ISecurityActivation;
import de.osthus.ambeth.security.SecurityContext;
import de.osthus.ambeth.security.SecurityContextType;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;
import de.osthus.ambeth.util.EqualsUtil;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.IPrefetchHelper;
import de.osthus.ambeth.util.IPrefetchState;
import de.osthus.ambeth.util.IndirectValueHolderRef;
import de.osthus.ambeth.util.OptimisticLockUtil;
import de.osthus.ambeth.util.StringBuilderUtil;

@SecurityContext(SecurityContextType.AUTHENTICATED)
@PersistenceContext
public class PersistenceMergeServiceExtension implements IMergeServiceExtension
{
	public class ReverseRelationRunnable implements IBackgroundWorkerDelegate
	{
		private final RelationMember reverseMember;
		private final IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap;
		private final IObjRef[] addedORIs;
		private final IObjRef[] removedORIs;
		private final IObjRef objRef;

		public ReverseRelationRunnable(RelationMember reverseMember, IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap, IObjRef[] addedORIs,
				IObjRef[] removedORIs, IObjRef objRef)
		{
			this.reverseMember = reverseMember;
			this.objRefToChangeContainerMap = objRefToChangeContainerMap;
			this.addedORIs = addedORIs;
			this.removedORIs = removedORIs;
			this.objRef = objRef;
		}

		@Override
		public void invoke() throws Throwable
		{
			if (addedORIs != null)
			{
				for (IObjRef addedObjRef : addedORIs)
				{
					CreateOrUpdateContainerBuild referredChangeContainer = (CreateOrUpdateContainerBuild) objRefToChangeContainerMap.get(addedObjRef);
					RelationUpdateItemBuild existingRui = referredChangeContainer.ensureRelation(reverseMember.getName());
					existingRui.addObjRef(objRef);
				}
			}
			if (removedORIs != null)
			{
				for (IObjRef removedObjRef : removedORIs)
				{
					CreateOrUpdateContainerBuild referredChangeContainer = (CreateOrUpdateContainerBuild) objRefToChangeContainerMap.get(removedObjRef);
					RelationUpdateItemBuild existingRui = referredChangeContainer.ensureRelation(reverseMember.getName());
					existingRui.removeObjRef(objRef);
				}
			}
		}
	}

	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected ICache cache;

	@Autowired
	protected ICompositeIdFactory compositeIdFactory;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IDatabase database;

	@Autowired
	protected IDatabaseMetaData databaseMetaData;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IObjRefFactory objRefFactory;

	@Autowired
	protected IObjRefHelper objRefHelper;

	@Autowired
	protected IPrefetchHelper prefetchHelper;

	@Autowired
	protected IPrimaryKeyProvider primaryKeyProvider;

	@Autowired
	protected IRelationMergeService relationMergeService;

	@Autowired
	protected IRootCache rootCache;

	@Autowired
	protected ISecurityActivation securityActivation;

	@Override
	public List<IEntityMetaData> getMetaData(List<Class<?>> entityTypes)
	{
		return entityMetaDataProvider.getMetaData(entityTypes);
	}

	@Override
	public IValueObjectConfig getValueObjectConfig(Class<?> valueType)
	{
		return entityMetaDataProvider.getValueObjectConfig(valueType);
	}

	// protected IList<IChangeContainer> transformToBuildableCUDResult(ICUDResult cudResult,
	// HashMap<Class<?>, HashMap<String, Integer>> typeToMemberNameToIndexMap,
	// HashMap<Class<?>, HashMap<String, Integer>> typeToPrimitiveMemberNameToIndexMap,
	// IMap<IChangeContainer, IChangeContainer> buildableToOriginalChangeContainerMap, IdentityHashMap<IObjRef, IObjRef> objRefReplaceMap)
	// {
	// List<IChangeContainer> allChanges = cudResult.getAllChanges();
	// ArrayList<IChangeContainer> buildableAllChanges = new ArrayList<IChangeContainer>(allChanges.size());
	// for (int a = 0, size = allChanges.size(); a < size; a++)
	// {
	// IChangeContainer changeContainer = allChanges.get(a);
	// if (changeContainer instanceof CreateOrUpdateContainerBuild)
	// {
	// buildableToOriginalChangeContainerMap.put(changeContainer, changeContainer);
	// buildableAllChanges.add(changeContainer);
	// // nothing to do
	// continue;
	// }
	// if (changeContainer instanceof DeleteContainer)
	// {
	// DeleteContainer deleteContainer = new DeleteContainer(); // important to clone the deleteContainer because the CUDResult in the end must be
	// // disconnected from the provided CUDResult object graph
	// deleteContainer.setReference(changeContainer.getReference()); // all objRefs from the provided object graph will be cloned later
	// buildableAllChanges.add(deleteContainer);
	// // nothing to do
	// continue;
	// }
	// IObjRef objRef = replaceObjRefIfNecessary(changeContainer.getReference(), objRefReplaceMap);
	// CreateOrUpdateContainerBuild buildableContainer = new CreateOrUpdateContainerBuild(changeContainer instanceof CreateContainer,
	// getOrCreateRelationMemberNameToIndexMap(objRef.getRealType(), typeToMemberNameToIndexMap), getOrCreatePrimitiveMemberNameToIndexMap(
	// objRef.getRealType(), typeToPrimitiveMemberNameToIndexMap));
	// buildableAllChanges.add(buildableContainer);
	// buildableToOriginalChangeContainerMap.put(buildableContainer, changeContainer);
	// if (objRef instanceof IDirectObjRef)
	// {
	// ((IDirectObjRef) objRef).setDirect(buildableContainer);
	// }
	// buildableContainer.setReference(objRef);
	// }
	// for (Entry<IChangeContainer, IChangeContainer> entry : buildableToOriginalChangeContainerMap)
	// {
	// CreateOrUpdateContainerBuild buildableContainer = (CreateOrUpdateContainerBuild) entry.getKey();
	// IChangeContainer changeContainer = entry.getValue();
	//
	// IPrimitiveUpdateItem[] puis = null;
	// IRelationUpdateItem[] ruis = null;
	// if (changeContainer instanceof CreateContainer)
	// {
	// puis = ((CreateContainer) changeContainer).getPrimitives();
	// ruis = ((CreateContainer) changeContainer).getRelations();
	// }
	// else if (changeContainer instanceof UpdateContainer)
	// {
	// puis = ((UpdateContainer) changeContainer).getPrimitives();
	// ruis = ((UpdateContainer) changeContainer).getRelations();
	// }
	// if (puis != null)
	// {
	// for (IPrimitiveUpdateItem pui : puis)
	// {
	// buildableContainer.addPrimitive(pui);
	// }
	// }
	// if (ruis == null)
	// {
	// continue;
	// }
	// for (IRelationUpdateItem rui : ruis)
	// {
	// RelationUpdateItemBuild existingRui = buildableContainer.ensureRelation(rui.getMemberName());
	// IObjRef[] addedORIs = rui.getAddedORIs();
	// if (addedORIs != null)
	// {
	// for (IObjRef objRef : addedORIs)
	// {
	// IObjRef replacedObjRef = replaceObjRefIfNecessary(objRef, objRefReplaceMap);
	// existingRui.addObjRef(replacedObjRef);
	// }
	// }
	// IObjRef[] removedORIs = rui.getRemovedORIs();
	// if (removedORIs != null)
	// {
	// for (IObjRef objRef : removedORIs)
	// {
	// IObjRef replacedObjRef = replaceObjRefIfNecessary(objRef, objRefReplaceMap);
	// existingRui.removeObjRef(replacedObjRef);
	// }
	// }
	// }
	// }
	// return buildableAllChanges;
	// }

	protected IList<IChangeContainer> transformToBuildableCUDResult(ICUDResult cudResult, IIncrementalMergeState incrementalState,
			IMap<IChangeContainer, IChangeContainer> buildableToOriginalChangeContainerMap)
	{
		List<IChangeContainer> allChanges = cudResult.getAllChanges();
		IdentityHashMap<IDirectObjRef, IDirectObjRef> directObjRefReplaceMap = new IdentityHashMap<IDirectObjRef, IDirectObjRef>();
		ArrayList<IChangeContainer> buildableAllChanges = new ArrayList<IChangeContainer>(allChanges.size());
		for (int a = 0, size = allChanges.size(); a < size; a++)
		{
			IChangeContainer changeContainer = allChanges.get(a);
			if (changeContainer instanceof CreateOrUpdateContainerBuild)
			{
				buildableToOriginalChangeContainerMap.put(changeContainer, changeContainer);
				buildableAllChanges.add(changeContainer);
				// nothing to do
				continue;
			}
			if (changeContainer instanceof DeleteContainer)
			{
				buildableAllChanges.add(changeContainer);
				// nothing to do
				continue;
			}
			IObjRef objRef = replaceObjRefIfNecessary(changeContainer.getReference(), directObjRefReplaceMap);
			CreateOrUpdateContainerBuild buildableContainer = changeContainer instanceof CreateContainer ? incrementalState.newCreateContainer(objRef
					.getRealType()) : incrementalState.newUpdateContainer(objRef.getRealType());
			buildableAllChanges.add(buildableContainer);
			buildableToOriginalChangeContainerMap.put(buildableContainer, changeContainer);
			buildableContainer.setReference(objRef);
		}
		for (Entry<IChangeContainer, IChangeContainer> entry : buildableToOriginalChangeContainerMap)
		{
			CreateOrUpdateContainerBuild buildableContainer = (CreateOrUpdateContainerBuild) entry.getKey();
			IChangeContainer changeContainer = entry.getValue();

			IPrimitiveUpdateItem[] puis = null;
			IRelationUpdateItem[] ruis = null;
			if (changeContainer instanceof CreateContainer)
			{
				puis = ((CreateContainer) changeContainer).getPrimitives();
				ruis = ((CreateContainer) changeContainer).getRelations();
			}
			else if (changeContainer instanceof UpdateContainer)
			{
				puis = ((UpdateContainer) changeContainer).getPrimitives();
				ruis = ((UpdateContainer) changeContainer).getRelations();
			}
			if (puis != null)
			{
				for (IPrimitiveUpdateItem pui : puis)
				{
					buildableContainer.addPrimitive(pui);
				}
			}
			if (ruis == null)
			{
				continue;
			}
			for (IRelationUpdateItem rui : ruis)
			{
				RelationUpdateItemBuild existingRui = buildableContainer.ensureRelation(rui.getMemberName());
				IObjRef[] addedORIs = rui.getAddedORIs();
				if (addedORIs != null)
				{
					for (IObjRef addedObjRef : addedORIs)
					{
						existingRui.addObjRef(replaceObjRefIfNecessary(addedObjRef, directObjRefReplaceMap));
					}
				}
				IObjRef[] removedORIs = rui.getRemovedORIs();
				if (removedORIs != null)
				{
					for (IObjRef removedObjRef : removedORIs)
					{
						existingRui.removeObjRef(replaceObjRefIfNecessary(removedObjRef, directObjRefReplaceMap));
					}
				}
			}
		}
		return buildableAllChanges;
	}

	protected IObjRef replaceObjRefIfNecessary(IObjRef objRef, IMap<IDirectObjRef, IDirectObjRef> directObjRefReplaceMap)
	{
		if (!(objRef instanceof IDirectObjRef))
		{
			return objRef;
		}
		IDirectObjRef directObjRef = (IDirectObjRef) objRef;
		IDirectObjRef replacedObjRef = directObjRefReplaceMap.get(directObjRef);
		if (replacedObjRef == null)
		{
			replacedObjRef = new DirectObjRef(directObjRef.getRealType(), ((IDirectObjRef) objRef).getDirect());
			directObjRefReplaceMap.put(directObjRef, replacedObjRef);
		}
		return replacedObjRef;
	}

	@Override
	public ICUDResult evaluateImplictChanges(ICUDResult cudResult, final IIncrementalMergeState incrementalState)
	{
		try
		{
			List<IChangeContainer> allChanges = cudResult.getAllChanges();
			List<Object> originalRefs = cudResult.getOriginalRefs();
			IdentityHashMap<IChangeContainer, IChangeContainer> buildableToOriginalChangeContainerMap = IdentityHashMap
					.<IChangeContainer, IChangeContainer> create(allChanges.size());
			HashMap<String, ITableChange> tableChangeMap = new HashMap<String, ITableChange>();
			ArrayList<IObjRef> oriList = new ArrayList<IObjRef>(allChanges.size());

			HashMap<Long, IObjRef> mockIdToObjRefMap = new HashMap<Long, IObjRef>();
			HashMap<IObjRef, Object> objRefToEntityMap = new HashMap<IObjRef, Object>();
			HashMap<IObjRef, IChangeContainer> objRefToChangeContainerMap = new HashMap<IObjRef, IChangeContainer>()
			{
				@Override
				public IChangeContainer put(IObjRef key, IChangeContainer value)
				{
					if (value instanceof LinkContainer)
					{
						ILinkChangeCommand linkCommand = ((LinkContainer) value).getCommand();
						IDirectedLinkMetaData linkMetaData = linkCommand.getDirectedLink().getMetaData();
						IObjRef objRef = linkCommand.getReference();
						RelationMember fromMember = linkMetaData.getMember();
						if (fromMember != null)
						{
							IChangeContainer changeContainer = get(objRef);
							if (!(changeContainer instanceof DeleteContainer))
							{
								if (changeContainer == null)
								{
									changeContainer = incrementalState.newUpdateContainer(objRef.getRealType());
									changeContainer.setReference(objRef);
									put(objRef, changeContainer);
								}
								RelationUpdateItemBuild rui = ((CreateOrUpdateContainerBuild) changeContainer).ensureRelation(fromMember.getName());
								rui.addObjRefs(linkCommand.getRefsToLink());
								rui.removeObjRefs(linkCommand.getRefsToUnlink());
							}
						}
						RelationMember toMember = linkMetaData.getReverseLink().getMember();
						if (toMember != null)
						{
							String toMemberName = toMember.getName();
							List<IObjRef> refsToLink = linkCommand.getRefsToLink();
							for (int a = refsToLink.size(); a-- > 0;)
							{
								IObjRef linkedObjRef = refsToLink.get(a);
								IChangeContainer changeContainer = get(linkedObjRef);
								if (changeContainer instanceof DeleteContainer)
								{
									continue;
								}
								if (changeContainer == null)
								{
									changeContainer = incrementalState.newUpdateContainer(linkedObjRef.getRealType());
									changeContainer.setReference(linkedObjRef);
									put(linkedObjRef, changeContainer);
								}
								((CreateOrUpdateContainerBuild) changeContainer).ensureRelation(toMemberName).addObjRef(objRef);
							}
							List<IObjRef> refsToUnlink = linkCommand.getRefsToUnlink();
							for (int a = refsToUnlink.size(); a-- > 0;)
							{
								IObjRef linkedObjRef = refsToUnlink.get(a);
								IChangeContainer changeContainer = get(linkedObjRef);
								if (changeContainer instanceof DeleteContainer)
								{
									continue;
								}
								if (changeContainer == null)
								{
									changeContainer = incrementalState.newUpdateContainer(linkedObjRef.getRealType());
									changeContainer.setReference(linkedObjRef);
									put(linkedObjRef, changeContainer);
								}
								((CreateOrUpdateContainerBuild) changeContainer).ensureRelation(toMemberName).removeObjRef(objRef);
							}
						}
						return null;
					}
					IChangeContainer existingValue = super.get(key);
					if (existingValue == value || existingValue instanceof DeleteContainer)
					{
						return null;
					}
					return super.put(key, value);
				}
			};
			IList<IChangeContainer> buildableAllChanges = transformToBuildableCUDResult(cudResult, incrementalState, buildableToOriginalChangeContainerMap);

			for (int a = buildableAllChanges.size(); a-- > 0;)
			{
				IChangeContainer changeContainer = buildableAllChanges.get(a);
				IObjRef objRef = changeContainer.getReference();
				Object entity = originalRefs.get(a);

				objRefToChangeContainerMap.put(objRef, changeContainer);
				IList<IObjRef> allObjRefs = objRefHelper.entityToAllObjRefs(entity);
				for (int b = allObjRefs.size(); b-- > 0;)
				{
					objRefToChangeContainerMap.put(allObjRefs.get(b), changeContainer);
				}
				objRefToEntityMap.put(objRef, entity);
			}
			executeWithoutSecurity(buildableAllChanges, tableChangeMap, oriList, mockIdToObjRefMap, objRefToChangeContainerMap, incrementalState);

			IdentityHashSet<IChangeContainer> changeContainersSet = new IdentityHashSet<IChangeContainer>();

			for (Entry<IObjRef, IChangeContainer> entry : objRefToChangeContainerMap)
			{
				IChangeContainer changeContainer = entry.getValue();
				changeContainersSet.add(changeContainer);
			}

			IChangeContainer[] changeContainers = changeContainersSet.toArray(IChangeContainer.class);
			Object[] newAllObjects = new Object[changeContainers.length];

			IObjRef[] objRefsToLoad = null;

			for (int a = changeContainers.length; a-- > 0;)
			{
				IChangeContainer changeContainer = changeContainers[a];
				IObjRef objRef = changeContainer.getReference();
				if (objRef instanceof IDirectObjRef)
				{
					((IDirectObjRef) objRef).setCreateContainerIndex(a);
				}
				if (changeContainer instanceof CreateOrUpdateContainerBuild)
				{
					changeContainers[a] = ((CreateOrUpdateContainerBuild) changeContainer).build();
				}
				Object entity = objRefToEntityMap.get(objRef);
				if (entity == null)
				{
					if (objRefsToLoad == null)
					{
						objRefsToLoad = new IObjRef[changeContainers.length];
					}
					objRefsToLoad[a] = objRef;
					continue;
				}
				newAllObjects[a] = entity;
			}
			if (objRefsToLoad != null)
			{
				IList<Object> entities = cache.getObjects(objRefsToLoad, CacheDirective.returnMisses());
				for (int a = entities.size(); a-- > 0;)
				{
					Object entity = entities.get(a);
					if (entity != null)
					{
						newAllObjects[a] = entity;
						continue;
					}
					if (objRefsToLoad[a] != null)
					{
						throw new IllegalStateException("Must never happen");
					}
				}
			}
			return new CUDResult(new ArrayList<IChangeContainer>(changeContainers), new ArrayList<Object>(newAllObjects));
		}
		finally
		{
			database.getContextProvider().clearAfterMerge();
		}
	}

	protected void executeRunnables(IList<IBackgroundWorkerDelegate> runnables)
	{
		while (runnables.size() > 0)
		{
			IBackgroundWorkerDelegate[] runnableArray = runnables.toArray(IBackgroundWorkerDelegate.class);
			runnables.clear();
			for (IBackgroundWorkerDelegate runnable : runnableArray)
			{
				try
				{
					runnable.invoke();
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void ensureCorrectIdIndexOfAllRelations(ICUDResult cudResult)
	{
		ArrayList<IBackgroundWorkerParamDelegate<IMap<IObjRef, Object>>> runnables = new ArrayList<IBackgroundWorkerParamDelegate<IMap<IObjRef, Object>>>();
		HashSet<IObjRef> objRefsWithWrongIdIndex = new HashSet<IObjRef>();

		List<IChangeContainer> allChanges = cudResult.getAllChanges();
		for (int a = allChanges.size(); a-- > 0;)
		{
			IChangeContainer change = allChanges.get(a);
			if (!(change instanceof ICreateOrUpdateContainer))
			{
				continue;
			}
			IRelationUpdateItem[] fullRUIs = ((ICreateOrUpdateContainer) change).getFullRUIs();
			if (fullRUIs == null)
			{
				continue;
			}
			ITableMetaData table = databaseMetaData.getTableByType(change.getReference().getRealType());
			for (IRelationUpdateItem rui : fullRUIs)
			{
				if (rui == null)
				{
					continue;
				}
				IDirectedLinkMetaData link = table.getLinkByMemberName(rui.getMemberName());
				byte expectedIdIndex = link.getToIdIndex();
				ensureCorrectIdIndexOfRelation(rui.getAddedORIs(), expectedIdIndex, objRefsWithWrongIdIndex, runnables);
				ensureCorrectIdIndexOfRelation(rui.getRemovedORIs(), expectedIdIndex, objRefsWithWrongIdIndex, runnables);
			}
		}
		while (runnables.size() > 0)
		{
			IList<IObjRef> objRefsWithWrongIdIndexList = objRefsWithWrongIdIndex.toList();
			objRefsWithWrongIdIndex.clear();

			ArrayList<IObjRef> objRefsList = new ArrayList<IObjRef>(objRefsWithWrongIdIndexList.size());
			for (int a = 0, size = objRefsWithWrongIdIndexList.size(); a < size; a++)
			{
				IObjRef objRef = objRefsWithWrongIdIndexList.get(a);
				if (objRef instanceof IDirectObjRef)
				{
					objRefsList.add(null);
					continue;
				}
				objRefsList.add(objRef);
			}

			IList<Object> entities = rootCache.getObjects(objRefsList, EnumSet.of(CacheDirective.CacheValueResult, CacheDirective.ReturnMisses));

			HashMap<IObjRef, Object> objRefToEntityMap = HashMap.create(entities.size());
			for (int a = entities.size(); a-- > 0;)
			{
				IObjRef objRef = objRefsWithWrongIdIndexList.get(a);
				if (objRef instanceof IDirectObjRef)
				{
					objRefToEntityMap.put(objRef, ((IDirectObjRef) objRef).getDirect());
					continue;
				}
				objRefToEntityMap.put(objRef, entities.get(a));
			}
			IBackgroundWorkerParamDelegate<IMap<IObjRef, Object>>[] runnablesArray = runnables.toArray(IBackgroundWorkerParamDelegate.class);
			runnables.clear();
			for (IBackgroundWorkerParamDelegate<IMap<IObjRef, Object>> runnable : runnablesArray)
			{
				try
				{
					runnable.invoke(objRefToEntityMap);
				}
				catch (Throwable e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		}
	}

	protected void ensureCorrectIdIndexOfRelation(final IObjRef[] objRefs, final int expectedIdIndex, ISet<IObjRef> objRefsWithWrongIdIndex,
			IList<IBackgroundWorkerParamDelegate<IMap<IObjRef, Object>>> runnables)
	{
		if (objRefs == null)
		{
			return;
		}
		boolean hasObjRefsWithWrongIdIndex = false;
		for (IObjRef objRef : objRefs)
		{
			// if (objRef instanceof IDirectObjRef && ((IDirectObjRef) objRef).getDirect() != null)
			// {
			// continue;
			// }
			if (objRef.getIdNameIndex() == expectedIdIndex)
			{
				continue;
			}
			objRefsWithWrongIdIndex.add(objRef);
			hasObjRefsWithWrongIdIndex = true;
		}
		if (!hasObjRefsWithWrongIdIndex)
		{
			return;
		}
		runnables.add(new IBackgroundWorkerParamDelegate<IMap<IObjRef, Object>>()
		{
			@Override
			public void invoke(IMap<IObjRef, Object> objRefToEntityMap) throws Throwable
			{
				for (int a = objRefs.length; a-- > 0;)
				{
					IObjRef objRef = objRefs[a];
					if (objRef.getIdNameIndex() == expectedIdIndex)
					{
						continue;
					}
					Object entity = objRefToEntityMap.get(objRef);
					IObjRef expectedObjRef = objRefHelper.entityToObjRef(entity, expectedIdIndex);
					objRefs[a] = expectedObjRef;
				}
			}
		});
	}

	@Override
	public IOriCollection merge(ICUDResult cudResult, IMethodDescription methodDescription)
	{
		IDatabase database = this.database.getCurrent();
		try
		{
			// ensureCorrectIdIndexOfAllRelations(cudResult);

			List<IChangeContainer> allChanges = cudResult.getAllChanges();
			HashMap<String, ITableChange> tableChangeMap = new HashMap<String, ITableChange>();
			ArrayList<IObjRef> oriList = new ArrayList<IObjRef>(allChanges.size());
			HashMap<IObjRef, IChangeContainer> objRefToChangeContainerMap = new HashMap<IObjRef, IChangeContainer>();

			for (int a = allChanges.size(); a-- > 0;)
			{
				IChangeContainer changeContainer = allChanges.get(a);
				IObjRef objRef = changeContainer.getReference();
				objRefToChangeContainerMap.put(objRef, changeContainer);
			}

			executeWithoutSecurity(allChanges, tableChangeMap, oriList, null, objRefToChangeContainerMap, null);

			ArrayList<IObjRef> objRefWithoutVersion = new ArrayList<IObjRef>();
			for (Entry<String, ITableChange> entry : tableChangeMap)
			{
				ITableChange tableChange = entry.getValue();
				if (tableChange instanceof LinkTableChange)
				{
					continue;
				}
				for (Entry<IObjRef, IRowCommand> rowEntry : ((TableChange) tableChange).getRowCommands())
				{
					IObjRef objRef = rowEntry.getKey();
					if (objRef.getVersion() != null || rowEntry.getValue().getCommand() instanceof ICreateCommand)
					{
						continue;
					}
					objRefWithoutVersion.add(objRef);
				}
			}
			if (objRefWithoutVersion.size() > 0)
			{
				IList<Object> objects = cache.getObjects(objRefWithoutVersion, CacheDirective.returnMisses());
				for (int a = objects.size(); a-- > 0;)
				{
					Object entity = objects.get(a);
					PrimitiveMember versionMember = ((IEntityMetaDataHolder) entity).get__EntityMetaData().getVersionMember();
					if (versionMember != null)
					{
						objRefWithoutVersion.get(a).setVersion(versionMember.getValue(entity));
					}
				}
			}
			IChangeAggregator changeAggregator = persistTableChanges(database, tableChangeMap);
			changeAggregator.createDataChange();

			for (int a = oriList.size(); a-- > 0;)
			{
				IObjRef objRef = oriList.get(a);
				if (!(objRef instanceof IDirectObjRef))
				{
					continue;
				}
				oriList.set(a, objRefFactory.dup(objRef));
			}
			OriCollection oriCollection = new OriCollection(oriList);
			IContextProvider contextProvider = database.getContextProvider();
			oriCollection.setChangedOn(contextProvider.getCurrentTime().longValue());
			oriCollection.setChangedBy(contextProvider.getCurrentUser());

			return oriCollection;
		}
		finally
		{
			database.getContextProvider().clearAfterMerge();
		}
	}

	protected void executeWithoutSecurity(final List<IChangeContainer> allChanges, final HashMap<String, ITableChange> tableChangeMap,
			final List<IObjRef> oriList, final IMap<Long, IObjRef> mockIdToObjRefMap, final IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap,
			final IIncrementalMergeState incrementalState)
	{
		try
		{
			securityActivation.executeWithoutSecurity(new IBackgroundWorkerDelegate()
			{
				@Override
				public void invoke() throws Throwable
				{
					IDatabase database = PersistenceMergeServiceExtension.this.database.getCurrent();
					IRootCache rootCache = PersistenceMergeServiceExtension.this.rootCache.getCurrentRootCache();

					HashMap<IObjRef, RootCacheValue> toDeleteMap = new HashMap<IObjRef, RootCacheValue>();
					LinkedHashMap<ITableChange, IList<ILinkChangeCommand>> linkChangeCommands = new LinkedHashMap<ITableChange, IList<ILinkChangeCommand>>();
					LinkedHashMap<Class<?>, IList<IObjRef>> typeToIdlessReferenceMap = new LinkedHashMap<Class<?>, IList<IObjRef>>();
					ArrayList<IObjRef> toLoadForDeletion = new ArrayList<IObjRef>();
					fillOriList(oriList, allChanges, toLoadForDeletion);

					loadEntitiesForDeletion(toLoadForDeletion, toDeleteMap, rootCache);

					convertChangeContainersToCommands(database, allChanges, tableChangeMap, typeToIdlessReferenceMap, linkChangeCommands, toDeleteMap,
							objRefToChangeContainerMap, rootCache, incrementalState);

					if (mockIdToObjRefMap == null)
					{
						aquireAndAssignIds(typeToIdlessReferenceMap);
					}
					else
					{
						mockAquireAndAssignIds(typeToIdlessReferenceMap, mockIdToObjRefMap);
					}
					processLinkChangeCommands(linkChangeCommands, tableChangeMap, rootCache);

					if (mockIdToObjRefMap != null)
					{
						undoMockIds(mockIdToObjRefMap);
					}
				}
			});
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected void fillOriList(List<IObjRef> oriList, List<IChangeContainer> allChanges, IList<IObjRef> toLoadForDeletion)
	{
		for (int a = 0, size = allChanges.size(); a < size; a++)
		{
			IChangeContainer changeContainer = allChanges.get(a);
			if (changeContainer instanceof CreateContainer)
			{
				oriList.add(changeContainer.getReference());
				// ((IDirectObjRef) changeContainer.getReference()).setDirect(changeContainer);
			}
			else if (changeContainer instanceof UpdateContainer)
			{
				oriList.add(changeContainer.getReference());
			}
			else if (changeContainer instanceof DeleteContainer)
			{
				oriList.add(null);
				toLoadForDeletion.add(changeContainer.getReference());
			}
		}
	}

	protected ITable getEnsureTable(IDatabase database, Class<?> referenceClass)
	{
		referenceClass = entityMetaDataProvider.getMetaData(referenceClass).getEntityType();
		ITable table = database.getTableByType(referenceClass);
		if (table == null)
		{
			throw new RuntimeException("No table configured for entity '" + referenceClass + "'");
		}
		return table;
	}

	protected void loadEntitiesForDeletion(IList<IObjRef> toLoadForDeletion, IMap<IObjRef, RootCacheValue> toDeleteMap, IRootCache rootCache)
	{
		IConversionHelper conversionHelper = this.conversionHelper;
		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		IObjRefHelper objRefHelper = this.objRefHelper;
		IList<Object> objects = rootCache.getObjects(toLoadForDeletion, RelationMergeService.cacheValueAndReturnMissesSet);
		for (int i = objects.size(); i-- > 0;)
		{
			Object object = objects.get(i);

			IObjRef oriToLoad = toLoadForDeletion.get(i);
			if (object == null)
			{
				throw OptimisticLockUtil.throwDeleted(oriToLoad);
			}
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(oriToLoad.getRealType());
			Member versionMember = metaData.getVersionMember();
			Object expectedVersion = oriToLoad.getVersion();
			if (expectedVersion == null && versionMember != null)
			{
				throw new OptimisticLockException("Object " + oriToLoad + " is not specified with a version. You should know what you want to delete", null,
						object);
			}
			if (versionMember != null)
			{
				expectedVersion = conversionHelper.convertValueToType(versionMember.getRealType(), expectedVersion);
			}
			IList<IObjRef> references = objRefHelper.entityToAllObjRefs(object);
			if (!EqualsUtil.equals(expectedVersion, references.get(0).getVersion()))
			{
				throw OptimisticLockUtil.throwModified(references.get(0), expectedVersion, object);
			}
			for (int j = references.size(); j-- > 0;)
			{
				IObjRef objRef = references.get(j);

				toDeleteMap.put(objRef, (RootCacheValue) object);
			}
		}
	}

	protected void convertChangeContainersToCommands(IDatabase database, List<IChangeContainer> allChanges, IMap<String, ITableChange> tableChangeMap,
			ILinkedMap<Class<?>, IList<IObjRef>> typeToIdlessReferenceMap, ILinkedMap<ITableChange, IList<ILinkChangeCommand>> linkChangeCommands,
			final IMap<IObjRef, RootCacheValue> toDeleteMap, final IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap, final IRootCache rootCache,
			IIncrementalMergeState incrementalState)
	{
		IRelationMergeService relationMergeService = this.relationMergeService;

		final InterfaceFastList<IChangeContainer> changeQueue = new InterfaceFastList<IChangeContainer>();

		changeQueue.pushAllFrom(allChanges);

		LinkedHashMap<CheckForPreviousParentKey, IList<IObjRef>> previousParentToMovedOrisMap = new LinkedHashMap<CheckForPreviousParentKey, IList<IObjRef>>();
		LinkedHashMap<IncomingRelationKey, IList<IObjRef>> incomingRelationToReferenceMap = new LinkedHashMap<IncomingRelationKey, IList<IObjRef>>();
		LinkedHashMap<OutgoingRelationKey, IList<IObjRef>> outgoingRelationToReferenceMap = new LinkedHashMap<OutgoingRelationKey, IList<IObjRef>>();
		HashSet<IObjRef> allAddedORIs = new HashSet<IObjRef>();
		final HashSet<EntityLinkKey> alreadyHandled = new HashSet<EntityLinkKey>();
		final IdentityHashSet<RootCacheValue> alreadyPrefetched = new IdentityHashSet<RootCacheValue>();

		findAllNewlyReferencedORIs(allChanges, allAddedORIs);

		while (true)
		{
			while (!changeQueue.isEmpty())
			{
				IChangeContainer changeContainer = changeQueue.popFirst().getElemValue();

				IObjRef reference = changeContainer.getReference();
				Object entityHandler;
				String entityHandlerName;
				if (!(changeContainer instanceof LinkContainer))
				{
					entityHandler = getEnsureTable(database, reference.getRealType());
					entityHandlerName = ((ITable) entityHandler).getMetaData().getName();
				}
				else
				{
					entityHandler = database.getTableByName(((LinkContainer) changeContainer).getTableName());
					if (entityHandler != null)
					{
						entityHandlerName = ((ITable) entityHandler).getMetaData().getName();
					}
					else
					{
						entityHandlerName = ((LinkContainer) changeContainer).getTableName();
					}
				}

				ITableChange tableChange = relationMergeService.getTableChange(tableChangeMap, entityHandler, entityHandlerName);

				IChangeCommand changeCommand = null;
				if ((changeContainer instanceof CreateOrUpdateContainerBuild && ((CreateOrUpdateContainerBuild) changeContainer).isCreate())
						|| changeContainer instanceof CreateContainer)
				{
					ICreateCommand createCommand = new CreateCommand(changeContainer.getReference());
					createCommand.configureFromContainer(changeContainer, tableChange.getTable());
					changeCommand = createCommand;
					IRelationUpdateItem[] ruis = ((ICreateOrUpdateContainer) changeContainer).getFullRUIs();
					IList<IChangeContainer> newChanges = relationMergeService.processCreateDependencies(reference, (ITable) entityHandler, ruis,
							previousParentToMovedOrisMap, allAddedORIs, objRefToChangeContainerMap, rootCache);
					changeQueue.pushAllFrom(newChanges);

					Class<?> realType = reference.getRealType();
					IList<IObjRef> references = typeToIdlessReferenceMap.get(realType);
					if (references == null)
					{
						references = new ArrayList<IObjRef>();
						typeToIdlessReferenceMap.put(realType, references);
					}
					references.add(reference);
				}
				else if ((changeContainer instanceof CreateOrUpdateContainerBuild && ((CreateOrUpdateContainerBuild) changeContainer).isUpdate())
						|| changeContainer instanceof UpdateContainer)
				{
					IUpdateCommand updateCommand = new UpdateCommand(changeContainer.getReference());
					updateCommand.configureFromContainer(changeContainer, tableChange.getTable());
					changeCommand = updateCommand;
					IRelationUpdateItem[] ruis = ((ICreateOrUpdateContainer) changeContainer).getFullRUIs();
					IList<IChangeContainer> newChanges = relationMergeService.processUpdateDependencies(reference, (ITable) entityHandler, ruis, toDeleteMap,
							previousParentToMovedOrisMap, allAddedORIs, objRefToChangeContainerMap, rootCache);
					changeQueue.pushAllFrom(newChanges);
					relationMergeService.handleUpdateNotifications(reference.getRealType(), ruis, tableChangeMap);
				}
				else if (changeContainer instanceof DeleteContainer)
				{
					if (reference.getIdNameIndex() != ObjRef.PRIMARY_KEY_INDEX)
					{
						Object entity = toDeleteMap.get(reference);
						reference = objRefHelper.entityToObjRef(entity);
						changeContainer.setReference(reference);
						objRefToChangeContainerMap.put(reference, changeContainer);
					}
					DeleteCommand deleteCommand = new DeleteCommand(changeContainer.getReference());
					deleteCommand.configureFromContainer(changeContainer, tableChange.getTable());
					changeCommand = deleteCommand;
					IList<IChangeContainer> newChanges = relationMergeService.processDeleteDependencies(reference, (ITable) entityHandler, toDeleteMap,
							outgoingRelationToReferenceMap, incomingRelationToReferenceMap, previousParentToMovedOrisMap, allAddedORIs,
							objRefToChangeContainerMap, rootCache);
					changeQueue.pushAllFrom(newChanges);
				}
				else if (changeContainer instanceof LinkContainer)
				{
					// Link commands may be converted in updates of foreign key columns. Since new objects may not have
					// an ID yet we have to process them later.
					IList<ILinkChangeCommand> changeCommands = linkChangeCommands.get(tableChange);
					if (changeCommands == null)
					{
						changeCommands = new ArrayList<ILinkChangeCommand>();
						linkChangeCommands.put(tableChange, changeCommands);
					}
					changeCommands.add(((LinkContainer) changeContainer).getCommand());
					continue;
				}

				tableChange.addChangeCommand(changeCommand);
			}
			for (Entry<CheckForPreviousParentKey, IList<IObjRef>> entry : previousParentToMovedOrisMap)
			{
				CheckForPreviousParentKey key = entry.getKey();
				IList<IObjRef> value = entry.getValue();
				IList<IChangeContainer> resultOfFork = relationMergeService.checkForPreviousParent(value, key.entityType, key.memberName,
						objRefToChangeContainerMap, incrementalState);
				changeQueue.pushAllFrom(resultOfFork);
			}
			previousParentToMovedOrisMap.clear();

			for (Entry<IncomingRelationKey, IList<IObjRef>> entry : incomingRelationToReferenceMap)
			{
				IncomingRelationKey key = entry.getKey();
				IList<IObjRef> value = entry.getValue();
				IList<IChangeContainer> resultOfFork = relationMergeService.handleIncomingRelation(value, key.idIndex, key.table, key.link, toDeleteMap,
						objRefToChangeContainerMap, rootCache, incrementalState);
				changeQueue.pushAllFrom(resultOfFork);
			}
			incomingRelationToReferenceMap.clear();

			@SuppressWarnings("unused")
			IPrefetchState prefetchState = prefetchAllReferredMembers(outgoingRelationToReferenceMap, toDeleteMap, alreadyHandled, alreadyPrefetched, rootCache);

			for (Entry<OutgoingRelationKey, IList<IObjRef>> entry : outgoingRelationToReferenceMap)
			{
				OutgoingRelationKey key = entry.getKey();
				IList<IObjRef> value = entry.getValue();
				IList<IChangeContainer> resultOfFork = relationMergeService.handleOutgoingRelation(value, key.idIndex, key.table, key.link, toDeleteMap,
						alreadyHandled, alreadyPrefetched, objRefToChangeContainerMap, rootCache);
				changeQueue.pushAllFrom(resultOfFork);
			}
			outgoingRelationToReferenceMap.clear();

			if (changeQueue.isEmpty())
			{
				break;
			}
		}
	}

	protected IPrefetchState prefetchAllReferredMembers(IMap<OutgoingRelationKey, IList<IObjRef>> outgoingRelationToReferenceMap,
			IMap<IObjRef, RootCacheValue> toDeleteMap, HashSet<EntityLinkKey> alreadyHandled, IdentityHashSet<RootCacheValue> alreadyPrefetched,
			IRootCache rootCache)
	{
		ArrayList<IndirectValueHolderRef> toPrefetch = new ArrayList<IndirectValueHolderRef>();
		for (Entry<OutgoingRelationKey, IList<IObjRef>> entry : outgoingRelationToReferenceMap)
		{
			IList<IObjRef> references = entry.getValue();
			for (IObjRef reference : references)
			{
				RootCacheValue entity = toDeleteMap.get(reference);
				if (!alreadyPrefetched.add(entity))
				{
					continue;
				}
				IEntityMetaData metaData = entity.get__EntityMetaData();
				RelationMember[] relationMembers = metaData.getRelationMembers();
				for (int a = relationMembers.length; a-- > 0;)
				{
					toPrefetch.add(new IndirectValueHolderRef(entity, relationMembers[a], (RootCache) rootCache));
				}
			}
		}
		if (toPrefetch.size() == 0)
		{
			return null;
		}
		return prefetchHelper.prefetch(toPrefetch);
	}

	/**
	 * Finds all ORIs referenced as 'added' in a RUI. Used to not cascade-delete moved entities.
	 * 
	 * @param allChanges
	 *            All changes in the CUDResult.
	 * @param allAddedORIs
	 *            All ORIs referenced as 'added' in a RUI.
	 */
	protected void findAllNewlyReferencedORIs(List<IChangeContainer> allChanges, HashSet<IObjRef> allAddedORIs)
	{
		for (int i = allChanges.size(); i-- > 0;)
		{
			IChangeContainer changeContainer = allChanges.get(i);
			if (changeContainer instanceof DeleteContainer)
			{
				continue;
			}
			IRelationUpdateItem[] fullRUIs;
			if (changeContainer instanceof CreateOrUpdateContainerBuild)
			{
				fullRUIs = ((CreateOrUpdateContainerBuild) changeContainer).getFullRUIs();
			}
			else if (changeContainer instanceof CreateContainer)
			{
				fullRUIs = ((CreateContainer) changeContainer).getRelations();
			}
			else if (changeContainer instanceof UpdateContainer)
			{
				fullRUIs = ((UpdateContainer) changeContainer).getRelations();
			}
			else
			{
				throw new IllegalArgumentException("Unknown IChangeContainer implementation: '" + changeContainer.getClass().getName() + "'");
			}
			if (fullRUIs == null)
			{
				continue;
			}
			for (IRelationUpdateItem rui : fullRUIs)
			{
				if (rui == null)
				{
					continue;
				}
				IObjRef[] addedORIs = rui.getAddedORIs();
				if (addedORIs != null)
				{
					allAddedORIs.addAll(addedORIs);
				}
			}
		}
	}

	protected void aquireAndAssignIds(ILinkedMap<Class<?>, IList<IObjRef>> typeToIdlessReferenceMap)
	{
		for (Entry<Class<?>, IList<IObjRef>> entry : typeToIdlessReferenceMap)
		{
			Class<?> entityType = entry.getKey();
			IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
			ITableMetaData table = databaseMetaData.getTableByType(metaData.getEntityType());
			if (table == null)
			{
				throw new RuntimeException("No table configured for entity '" + entityType + "'");
			}
			IList<IObjRef> idlessReferences = entry.getValue();

			primaryKeyProvider.acquireIds(table, idlessReferences);
		}
	}

	protected void mockAquireAndAssignIds(ILinkedMap<Class<?>, IList<IObjRef>> typeToIdlessReferenceMap, IMap<Long, IObjRef> mockIdToObjRefMap)
	{
		long idMock = -1;
		for (Entry<Class<?>, IList<IObjRef>> entry : typeToIdlessReferenceMap)
		{
			IList<IObjRef> idlessReferences = entry.getValue();
			for (int i = idlessReferences.size(); i-- > 0;)
			{
				IObjRef reference = idlessReferences.get(i);
				Long value = new Long(idMock);
				reference.setId(value);
				reference.setIdNameIndex(ObjRef.PRIMARY_KEY_INDEX);
				idMock--;
				mockIdToObjRefMap.put(value, reference);
			}
		}
	}

	protected void undoMockIds(IMap<Long, IObjRef> mockIdToObjRefMap)
	{
		for (Entry<Long, IObjRef> entry : mockIdToObjRefMap)
		{
			entry.getValue().setId(null);
		}
	}

	protected void processLinkChangeCommands(ILinkedMap<ITableChange, IList<ILinkChangeCommand>> linkChangeCommands, IMap<String, ITableChange> tableChangeMap,
			IRootCache rootCache)
	{
		ICompositeIdFactory compositeIdFactory = this.compositeIdFactory;
		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		IRelationMergeService relationMergeService = this.relationMergeService;
		LinkedHashMap<Byte, IList<IObjRef>> toChange = new LinkedHashMap<Byte, IList<IObjRef>>();
		for (Entry<ITableChange, IList<ILinkChangeCommand>> entry : linkChangeCommands)
		{
			IList<ILinkChangeCommand> changeCommands = entry.getValue();
			for (int i = changeCommands.size(); i-- > 0;)
			{
				ILinkChangeCommand changeCommand = changeCommands.get(i);
				relationMergeService.handleUpdateNotifications(changeCommand, tableChangeMap);
				relationMergeService.checkForCorrectIdIndex(changeCommand, toChange);
			}
		}
		for (Entry<Byte, IList<IObjRef>> entry : toChange)
		{
			byte idIndex = entry.getKey().byteValue();
			IList<IObjRef> changeList = entry.getValue();
			for (int i = changeList.size(); i-- > 0;)
			{
				IObjRef ori = changeList.get(i);
				IEntityMetaData metaData = entityMetaDataProvider.getMetaData(ori.getRealType());
				Object id = null;
				if (ori instanceof IDirectObjRef)
				{
					String idPropertyName = metaData.getIdMemberByIdIndex(idIndex).getName();
					Object directRef = ((IDirectObjRef) ori).getDirect();
					if (directRef instanceof CreateContainer)
					{
						CreateContainer container = (CreateContainer) directRef;
						IPrimitiveUpdateItem[] updateItems = container.getPrimitives();
						if (updateItems != null)
						{
							for (int j = updateItems.length; j-- > 0;)
							{
								IPrimitiveUpdateItem updateItem = updateItems[j];
								if (idPropertyName.equals(updateItem.getMemberName()))
								{
									id = updateItem.getNewValue();
									break;
								}
							}
						}
					}
					else if (metaData.getEntityType().isAssignableFrom(directRef.getClass()))
					{
						id = compositeIdFactory.createIdFromEntity(metaData, idIndex, directRef);
					}
					else
					{
						throw new IllegalArgumentException("Type of '" + directRef.getClass().getName() + "' is not expected in DirectObjRef for entity type '"
								+ metaData.getEntityType().getName() + "'");
					}
				}
				else
				{
					AbstractCacheValue entity = (AbstractCacheValue) rootCache.getObject(ori, CacheDirective.cacheValueResult());
					if (idIndex == ObjRef.PRIMARY_KEY_INDEX)
					{
						id = entity.getId();
					}
					else
					{
						id = compositeIdFactory.createIdFromPrimitives(metaData, idIndex, entity);
					}
				}
				if (id == null)
				{
					throw new IllegalArgumentException("Missing id value for relation");
				}
				ori.setId(id);
				ori.setIdNameIndex(idIndex);
			}
		}
		for (Entry<ITableChange, IList<ILinkChangeCommand>> entry : linkChangeCommands)
		{
			ITableChange tableChange = entry.getKey();
			IList<ILinkChangeCommand> changeCommands = entry.getValue();
			for (int i = changeCommands.size(); i-- > 0;)
			{
				tableChange.addChangeCommand(changeCommands.get(i));
			}
		}
	}

	protected IChangeAggregator persistTableChanges(IDatabase database, IMap<String, ITableChange> tableChangeMap)
	{
		// Mark this database as modifying (e.g. to suppress later out-of-transaction parallel reads)
		IModifyingDatabase modifyingDatabase = database.getAutowiredBeanInContext(IModifyingDatabase.class);
		if (!modifyingDatabase.isModifyingAllowed())
		{
			throw new PersistenceException("It is not allowed to modify anything while the transaction is in read-only mode");
		}
		modifyingDatabase.setModifyingDatabase(true);

		IChangeAggregator changeAggregator = beanContext.registerBean(ChangeAggregator.class).finish();
		IList<ITableChange> tableChangeList = tableChangeMap.values();
		long start = System.currentTimeMillis();

		// Important to sort the table changes to deal with deadlock issues due to pessimistic locking
		Collections.sort(tableChangeList);
		try
		{
			RuntimeException primaryException = null;
			IList<String> disabled = database.disableConstraints();
			try
			{
				executeTableChanges(tableChangeList, changeAggregator);
			}
			catch (RuntimeException e)
			{
				primaryException = e;
			}
			finally
			{
				try
				{
					database.enableConstraints(disabled);
				}
				catch (RuntimeException e)
				{
					if (primaryException == null)
					{
						throw e;
					}
				}
				if (primaryException != null)
				{
					throw primaryException;
				}
			}
		}
		finally
		{
			long end = System.currentTimeMillis();
			if (log.isDebugEnabled())
			{
				long spent = end - start;
				log.debug(StringBuilderUtil.concat(objectCollector, "Spent ", spent, " ms on JDBC execution"));
			}
		}

		return changeAggregator;
	}

	protected void executeTableChanges(List<ITableChange> tableChangeList, IChangeAggregator changeAggregator)
	{
		for (int i = tableChangeList.size(); i-- > 0;)
		{
			ITableChange tableChange = tableChangeList.get(i);
			tableChange.execute(changeAggregator);
		}
	}
}
