package de.osthus.ambeth.service;

import java.lang.reflect.Array;
import java.util.Collections;
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
import de.osthus.ambeth.change.IDeleteCommand;
import de.osthus.ambeth.change.ILinkChangeCommand;
import de.osthus.ambeth.change.IRowCommand;
import de.osthus.ambeth.change.ITableChange;
import de.osthus.ambeth.change.IUpdateCommand;
import de.osthus.ambeth.change.LinkContainer;
import de.osthus.ambeth.change.LinkTableChange;
import de.osthus.ambeth.change.TableChange;
import de.osthus.ambeth.change.UpdateCommand;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
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
import de.osthus.ambeth.merge.ICUDResultHelper;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IMergeController;
import de.osthus.ambeth.merge.IMergeServiceExtension;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.IObjRefProvider;
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
import de.osthus.ambeth.merge.transfer.PrimitiveUpdateItem;
import de.osthus.ambeth.merge.transfer.UpdateContainer;
import de.osthus.ambeth.metadata.IObjRefFactory;
import de.osthus.ambeth.metadata.IPreparedObjRefFactory;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.PrimitiveMember;
import de.osthus.ambeth.metadata.RelationMember;
import de.osthus.ambeth.model.IDataObject;
import de.osthus.ambeth.model.IMethodDescription;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.IContextProvider;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IDirectedLink;
import de.osthus.ambeth.persistence.IDirectedLinkMetaData;
import de.osthus.ambeth.persistence.IFieldMetaData;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.persistence.ITableMetaData;
import de.osthus.ambeth.persistence.parallel.IModifyingDatabase;
import de.osthus.ambeth.proxy.IEntityMetaDataHolder;
import de.osthus.ambeth.proxy.IObjRefContainer;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.security.ISecurityActivation;
import de.osthus.ambeth.security.SecurityContext;
import de.osthus.ambeth.security.SecurityContextType;
import de.osthus.ambeth.threading.IBackgroundWorkerDelegate;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerParamDelegate;
import de.osthus.ambeth.util.EqualsUtil;
import de.osthus.ambeth.util.IAggregrateResultHandler;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.IMultithreadingHelper;
import de.osthus.ambeth.util.IPrefetchHelper;
import de.osthus.ambeth.util.IPrefetchState;
import de.osthus.ambeth.util.IndirectValueHolderRef;
import de.osthus.ambeth.util.OptimisticLockUtil;
import de.osthus.ambeth.util.StringBuilderUtil;

@SecurityContext(SecurityContextType.AUTHENTICATED)
@PersistenceContext
public class PersistenceMergeServiceExtension implements IMergeServiceExtension
{
	public class ReverseRelationRunnable implements Runnable
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
		public void run()
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
	protected ICUDResultHelper cudResultHelper;

	@Autowired
	protected IDatabase database;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IMergeController mergeController;

	@Autowired
	protected IMultithreadingHelper multithreadingHelper;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IObjRefFactory objRefFactory;

	@Autowired
	protected IObjRefHelper objRefHelper;

	@Autowired
	protected IPrefetchHelper prefetchHelper;

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
			IObjRef objRef = changeContainer.getReference();
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
					existingRui.addObjRefs(addedORIs);
				}
				IObjRef[] removedORIs = rui.getRemovedORIs();
				if (removedORIs != null)
				{
					existingRui.removeObjRefs(removedORIs);
				}
			}
		}
		return buildableAllChanges;
	}

	@Override
	public ICUDResult evaluateImplictChanges(ICUDResult cudResult, IIncrementalMergeState incrementalState)
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
			HashMap<IObjRef, IChangeContainer> objRefToChangeContainerMap = new HashMap<IObjRef, IChangeContainer>();
			IList<IChangeContainer> buildableAllChanges = transformToBuildableCUDResult(cudResult, incrementalState, buildableToOriginalChangeContainerMap);

			for (int a = buildableAllChanges.size(); a-- > 0;)
			{
				IChangeContainer changeContainer = buildableAllChanges.get(a);
				IObjRef objRef = changeContainer.getReference();
				objRefToChangeContainerMap.put(objRef, changeContainer);
				// IObjRef originalObjRef = allChanges.get(a).getReference();
				// if (originalObjRef != objRef)
				// {
				// objRefToChangeContainerMap.put(originalObjRef, changeContainer);
				// }
				if (changeContainer instanceof CreateOrUpdateContainerBuild && ((CreateOrUpdateContainerBuild) changeContainer).isCreate())
				{
					// add all unpersisted entities
					objRefToEntityMap.put(objRef, originalRefs.get(a));
					// identityObjRefToEntityMap.put(objRef, originalRefs.get(a));
				}
			}

			executeWithoutSecurity(buildableAllChanges, tableChangeMap, oriList, mockIdToObjRefMap, objRefToChangeContainerMap);

			IList<IObjRef> allPersistedObjRefs = collectAllPersistedObjRefs(tableChangeMap);

			ICache cache = this.cache.getCurrentCache();
			IList<Object> allPersistedObjects = cache.getObjects(allPersistedObjRefs, CacheDirective.returnMisses());
			for (int a = allPersistedObjects.size(); a-- > 0;)
			{
				IObjRef objRef = allPersistedObjRefs.get(a);
				Object entity = allPersistedObjects.get(a);
				if (entity == null)
				{
					throw OptimisticLockUtil.throwDeleted(objRef);
				}
				// add all persisted entities (unpersisted is already added)
				objRefToEntityMap.put(objRef, entity);
			}
			for (int a = buildableAllChanges.size(); a-- > 0;)
			{
				IChangeContainer changeContainer = buildableAllChanges.get(a);
				if (!(changeContainer instanceof CreateContainer))
				{
					continue;
				}
				Object unpersistedEntity = originalRefs.get(a);
				objRefToEntityMap.put(changeContainer.getReference(), unpersistedEntity);
			}

			ArrayList<IChangeContainer> newAllChanges = new ArrayList<IChangeContainer>();
			ArrayList<Runnable> runnables = new ArrayList<Runnable>();
			for (Entry<String, ITableChange> entry : tableChangeMap)
			{
				ITableChange tableChange = entry.getValue();
				if (tableChange instanceof LinkTableChange)
				{
					// handled later
					continue;
				}
				ITableMetaData table = tableChange.getTable().getMetaData();
				for (Entry<IObjRef, IRowCommand> rowEntry : ((TableChange) tableChange).getRowCommands())
				{
					IObjRef objRef = rowEntry.getKey();

					IChangeCommand changeCommand = rowEntry.getValue().getCommand();
					if (changeCommand instanceof CreateCommand)
					{
						CreateCommand createCommand = (CreateCommand) changeCommand;

						CreateOrUpdateContainerBuild container = (CreateOrUpdateContainerBuild) objRefToChangeContainerMap.get(objRef);
						if (container == null)
						{
							container = incrementalState.newCreateContainer(objRef.getRealType());
							objRefToChangeContainerMap.put(objRef, container);
						}
						buildPUIsAndRUIs(table, false, objRef, createCommand.getItems(), objRefToEntityMap, objRefToChangeContainerMap, mockIdToObjRefMap,
								container, runnables);

						newAllChanges.add(container);
						continue;
					}
					else if (changeCommand instanceof UpdateCommand)
					{
						UpdateCommand updateCommand = (UpdateCommand) changeCommand;

						CreateOrUpdateContainerBuild container = (CreateOrUpdateContainerBuild) objRefToChangeContainerMap.get(objRef);
						if (container == null)
						{
							container = incrementalState.newUpdateContainer(objRef.getRealType());
							objRefToChangeContainerMap.put(objRef, container);
						}
						container.setReference(objRef);

						buildPUIsAndRUIs(table, true, objRef, updateCommand.getItems(), objRefToEntityMap, objRefToChangeContainerMap, mockIdToObjRefMap,
								container, runnables);

						newAllChanges.add(container);
						continue;
					}
					IChangeContainer container = objRefToChangeContainerMap.get(objRef);
					if (!(container instanceof DeleteContainer))
					{
						throw new IllegalStateException("Must never happen");
					}
					newAllChanges.add(container);
				}
			}
			executeRunnables(runnables);
			for (Entry<String, ITableChange> entry : tableChangeMap)
			{
				ITableChange tableChange = entry.getValue();
				if (!(tableChange instanceof LinkTableChange))
				{
					// already handled before
					continue;
				}
				for (Entry<IObjRef, ILinkChangeCommand> rowEntry : ((LinkTableChange) tableChange).getRowCommands())
				{
					IChangeContainer changeContainer = objRefToChangeContainerMap.get(rowEntry.getKey());
					if (changeContainer instanceof DeleteContainer)
					{
						// nothing to do
						continue;
					}
					CreateOrUpdateContainerBuild createOrUpdate = (CreateOrUpdateContainerBuild) changeContainer;

					ILinkChangeCommand linkChange = rowEntry.getValue();
					RelationMember member = linkChange.getDirectedLink().getMetaData().getMember();
					IDirectedLink reverseLink = linkChange.getDirectedLink().getReverseLink();
					RelationMember reverseMember = reverseLink.getMetaData().getMember();

					List<IObjRef> toLink = linkChange.getRefsToLink();
					if (toLink.size() > 0)
					{
						RelationUpdateItemBuild rui = createOrUpdate.ensureRelation(member.getName());
						rui.addObjRefs(toLink);
						if (reverseMember != null)
						{
							for (int a = toLink.size(); a-- > 0;)
							{
								IObjRef toLinkObjRef = toLink.get(a);
								IChangeContainer toLinkChangeContainer = objRefToChangeContainerMap.get(toLinkObjRef);
								CreateOrUpdateContainerBuild toLinkCreateOrUpdate = (CreateOrUpdateContainerBuild) toLinkChangeContainer;
								RelationUpdateItemBuild toLinkRui = toLinkCreateOrUpdate.ensureRelation(reverseMember.getName());
								toLinkRui.addObjRef(rowEntry.getKey());
							}
						}
					}
					toLink = linkChange.getRefsToUnlink();
					if (toLink.size() > 0)
					{
						RelationUpdateItemBuild rui = createOrUpdate.ensureRelation(member.getName());
						rui.removeObjRefs(toLink);
						if (reverseMember != null)
						{
							for (int a = toLink.size(); a-- > 0;)
							{
								IObjRef toLinkObjRef = toLink.get(a);
								IChangeContainer toLinkChangeContainer = objRefToChangeContainerMap.get(toLinkObjRef);
								CreateOrUpdateContainerBuild toLinkCreateOrUpdate = (CreateOrUpdateContainerBuild) toLinkChangeContainer;
								RelationUpdateItemBuild toLinkRui = toLinkCreateOrUpdate.ensureRelation(reverseMember.getName());
								toLinkRui.removeObjRef(rowEntry.getKey());
							}
						}
					}
				}
			}
			HashSet<IObjRef> objRefsWithoutVersion = new HashSet<IObjRef>();
			ArrayList<Object> newAllObjects = new ArrayList<Object>(newAllChanges.size());
			for (int a = 0, size = newAllChanges.size(); a < size; a++)
			{
				IChangeContainer changeContainer = newAllChanges.get(a);
				Object entity = objRefToEntityMap.get(changeContainer.getReference());
				if (entity == null)
				{
					throw new IllegalStateException("Must never happen");
				}
				if (changeContainer instanceof CreateOrUpdateContainerBuild)
				{
					CreateOrUpdateContainerBuild createOrUpdate = (CreateOrUpdateContainerBuild) changeContainer;

					IPrimitiveUpdateItem[] puis = cudResultHelper.compactPUIs(createOrUpdate.getFullPUIs(), createOrUpdate.getPuiCount());
					IRelationUpdateItem[] ruis = cudResultHelper.compactRUIs(createOrUpdate.getFullRUIs(), createOrUpdate.getRuiCount());
					if (ruis != null)
					{
						for (int b = ruis.length; b-- > 0;)
						{
							IRelationUpdateItem rui = ruis[b];
							if (rui instanceof RelationUpdateItemBuild)
							{
								rui = ((RelationUpdateItemBuild) rui).buildRUI();
								ruis[b] = rui;
							}
							IObjRef[] addedORIs = rui.getAddedORIs();
							if (addedORIs != null)
							{
								for (int c = addedORIs.length; c-- > 0;)
								{
									IObjRef objRef = addedORIs[c];
									if (objRef.getVersion() == null && objRef.getId() != null)
									{
										objRefsWithoutVersion.add(objRef);
									}
								}
							}
							IObjRef[] removedORIs = rui.getRemovedORIs();
							if (removedORIs != null)
							{
								for (int c = removedORIs.length; c-- > 0;)
								{
									IObjRef objRef = removedORIs[c];
									if (objRef.getVersion() == null && objRef.getId() != null)
									{
										objRefsWithoutVersion.add(objRef);
									}
								}
							}
						}
					}
					if (createOrUpdate.isCreate())
					{
						CreateContainer cc = new CreateContainer();
						cc.setReference(createOrUpdate.getReference());
						((IDirectObjRef) cc.getReference()).setCreateContainerIndex(a);
						cc.setPrimitives(puis);
						cc.setRelations(ruis);
						newAllChanges.set(a, cc);
					}
					else if (createOrUpdate.isUpdate())
					{
						if (puis == null && ruis == null)
						{
							newAllChanges.set(a, null);
							continue;
						}
						IObjRef objRef = createOrUpdate.getReference();
						UpdateContainer uc = new UpdateContainer();
						uc.setReference(objRef);
						uc.setPrimitives(puis);
						uc.setRelations(ruis);
						newAllChanges.set(a, uc);
					}
				}
				else
				{
					((IDataObject) entity).setToBeDeleted(true);
				}
				newAllObjects.add(entity);
			}
			if (objRefsWithoutVersion.size() > 0)
			{
				IList<IObjRef> objRefsList = objRefsWithoutVersion.toList();
				IList<Object> entities = cache.getObjects(objRefsList, CacheDirective.none());
				for (int a = entities.size(); a-- > 0;)
				{
					Object entity = entities.get(a);
					PrimitiveMember versionMember = ((IEntityMetaDataHolder) entity).get__EntityMetaData().getVersionMember();
					if (versionMember == null)
					{
						continue;
					}
					objRefsList.get(a).setVersion(versionMember.getValue(entity));
				}
			}
			ArrayList<IChangeContainer> compactedNewAllChanges = new ArrayList<IChangeContainer>(newAllChanges.size());
			for (int a = 0, size = newAllChanges.size(); a < size; a++)
			{
				IChangeContainer changeContainer = newAllChanges.get(a);
				if (changeContainer == null)
				{
					continue;
				}
				compactedNewAllChanges.add(changeContainer);
			}
			return new CUDResult(compactedNewAllChanges, newAllObjects);
		}
		finally
		{
			database.getContextProvider().clearAfterMerge();
		}
	}

	protected void executeRunnables(IList<Runnable> runnables)
	{
		while (runnables.size() > 0)
		{
			Runnable[] runnableArray = runnables.toArray(Runnable.class);
			runnables.clear();
			for (Runnable runnable : runnableArray)
			{
				runnable.run();
			}
		}
	}

	protected IRelationUpdateItem[] mergeRelations(IRelationUpdateItem[] relations, IRelationUpdateItem rui)
	{
		if (relations == null)
		{
			return new IRelationUpdateItem[] { rui };
		}
		IRelationUpdateItem[] newRelations = new IRelationUpdateItem[relations.length + 1];
		System.arraycopy(relations, 0, newRelations, 0, relations.length);
		newRelations[relations.length] = rui;
		return newRelations;
	}

	protected IObjRef[] mergeObjRefs(IObjRef[] objRefs, IObjRef objRef)
	{
		if (objRefs == null)
		{
			return new IObjRef[] { objRef };
		}
		IObjRef[] newRelations = new IObjRef[objRefs.length + 1];
		System.arraycopy(objRefs, 0, newRelations, 0, objRefs.length);
		newRelations[objRefs.length] = objRef;
		return newRelations;
	}

	protected IList<IObjRef> collectAllPersistedObjRefs(IMap<String, ITableChange> tableChangeMap)
	{
		HashSet<IObjRef> allObjRefsSet = new HashSet<IObjRef>();

		for (Entry<String, ITableChange> entry : tableChangeMap)
		{
			ITableChange tableChange = entry.getValue();
			if (tableChange instanceof LinkTableChange)
			{
				IMap<IObjRef, ILinkChangeCommand> rowCommands = ((LinkTableChange) tableChange).getRowCommands();
				for (Entry<IObjRef, ILinkChangeCommand> rowEntry : rowCommands)
				{
					IObjRef objRef = rowEntry.getKey();
					if (objRef instanceof IDirectObjRef)
					{
						continue;
					}
					allObjRefsSet.add(objRef);
				}
				continue;
			}
			IMap<IObjRef, IRowCommand> rowCommands = ((TableChange) tableChange).getRowCommands();
			for (Entry<IObjRef, IRowCommand> rowEntry : rowCommands)
			{
				IObjRef objRef = rowEntry.getKey();
				if (objRef instanceof IDirectObjRef)
				{
					continue;
				}
				allObjRefsSet.add(objRef);
			}
		}
		return allObjRefsSet.toList();
	}

	protected void buildPUIsAndRUIs(ITableMetaData table, boolean isUpdate, final IObjRef objRef, ILinkedMap<String, Object> items,
			IMap<IObjRef, Object> objRefToEntityMap, IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap, IMap<Long, IObjRef> mockIdToObjRefMap,
			CreateOrUpdateContainerBuild container, List<Runnable> runnables)
	{
		Object entity = objRefToEntityMap.get(objRef);

		for (Entry<String, Object> itemEntry : items)
		{
			String fieldName = itemEntry.getKey();
			Object newValue = itemEntry.getValue();
			IFieldMetaData field = table.getFieldByName(fieldName);

			IDirectedLinkMetaData link = table.getLinkByFieldName(field.getName());
			if (link == null)
			{
				Member member = field.getMember();

				PrimitiveUpdateItem pui = container.ensurePrimitive(member.getName());
				pui.setNewValue(newValue);
				continue;
			}
			Member member = link.getMember();

			IDirectedLinkMetaData reverseLink = link.getReverseLink();
			IList<IObjRef> newOriList = extractObjRefList(newValue, reverseLink.getEntityType(), field.getIdIndex(), mockIdToObjRefMap);
			if (member == null)
			{
				if (reverseLink.getMember() != null)
				{
					runnables.add(new ReverseRelationRunnable(reverseLink.getMember(), objRefToChangeContainerMap, newOriList.toArray(IObjRef.class), null,
							objRef));
				}
				continue;
			}
			Object oldValue = member.getValue(entity);

			for (Entry<IObjRef, Object> entry : objRefToEntityMap)
			{
				if (oldValue != entry.getValue())
				{
					continue;
				}
				oldValue = entry.getKey();
				break;
			}
			IList<IObjRef> oldOriList = extractObjRefList(oldValue, reverseLink.getEntityType(), field.getIdIndex(), mockIdToObjRefMap);

			RelationUpdateItemBuild rui = mergeController.createRUIBuild(member.getName(), oldOriList, newOriList);
			if (rui != null)
			{
				container.addRelation(rui);

				if (reverseLink.getMember() != null)
				{
					runnables.add(new ReverseRelationRunnable(reverseLink.getMember(), objRefToChangeContainerMap, rui.getAddedORIs(), rui.getRemovedORIs(),
							objRef));
				}
			}
		}
	}

	protected IList<IObjRef> extractObjRefList(Object value, Class<?> entityType, byte idIndex, IMap<Long, IObjRef> mockIdToObjRefMap)
	{
		if (value == null)
		{
			return EmptyList.<IObjRef> getInstance();
		}
		if (!(value instanceof IObjRef) && !(value instanceof IObjRefContainer))
		{
			IPreparedObjRefFactory preparedObjRefFactory = objRefFactory.prepareObjRefFactory(entityType, idIndex);
			if (value instanceof List)
			{
				List<?> list = (List<?>) value;
				ArrayList<IObjRef> objRefList = new ArrayList<IObjRef>(list.size());
				for (int a = 0, size = list.size(); a < size; a++)
				{
					Object id = list.get(a);
					if (id instanceof Number)
					{
						IObjRef objRef = mockIdToObjRefMap.get(Long.valueOf(((Number) id).longValue()));
						if (objRef != null)
						{
							objRefList.add(objRef);
							continue;
						}
					}
					objRefList.add(preparedObjRefFactory.createObjRef(id, null));
				}
				return objRefList;
			}
			else if (value.getClass().isArray())
			{
				int length = Array.getLength(value);
				ArrayList<IObjRef> objRefList = new ArrayList<IObjRef>(length);
				for (int a = 0; a < length; a++)
				{
					Object id = Array.get(value, a);
					if (id instanceof Number)
					{
						IObjRef objRef = mockIdToObjRefMap.get(Long.valueOf(((Number) id).longValue()));
						if (objRef != null)
						{
							objRefList.add(objRef);
							continue;
						}
					}
					objRefList.add(preparedObjRefFactory.createObjRef(id, null));
				}
				return objRefList;
			}
			ArrayList<IObjRef> objRefList = new ArrayList<IObjRef>(1);
			if (value instanceof Number)
			{
				IObjRef objRef = mockIdToObjRefMap.get(Long.valueOf(((Number) value).longValue()));
				if (objRef != null)
				{
					objRefList.add(objRef);
				}
				else
				{
					objRefList.add(preparedObjRefFactory.createObjRef(value, null));
				}
			}
			else
			{
				objRefList.add(preparedObjRefFactory.createObjRef(value, null));
			}
			return objRefList;
		}
		return objRefHelper.extractObjRefList(value, new IObjRefProvider()
		{
			@Override
			public IObjRef getORI(Object obj, IEntityMetaData metaData)
			{
				PrimitiveMember versionMember = metaData.getVersionMember();
				Object id = metaData.getIdMember().getValue(obj);
				if (id == null)
				{
					return new DirectObjRef(metaData.getEntityType(), obj);
				}
				Object version = versionMember != null ? versionMember.getValue(obj) : null;
				IObjRef objRef = objRefFactory.createObjRef(metaData.getEntityType(), ObjRef.PRIMARY_KEY_INDEX, id, version);
				return objRef;
			}
		}, null, null);
	}

	@Override
	public IOriCollection merge(ICUDResult cudResult, IMethodDescription methodDescription)
	{
		IDatabase database = this.database.getCurrent();
		try
		{
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
			executeWithoutSecurity(allChanges, tableChangeMap, oriList, null, objRefToChangeContainerMap);

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
			final List<IObjRef> oriList, final IMap<Long, IObjRef> mockIdToObjRefMap, final IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap)
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
							objRefToChangeContainerMap, rootCache);

					if (mockIdToObjRefMap == null)
					{
						aquireAndAssignIds(database, typeToIdlessReferenceMap);
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
			final IMap<IObjRef, RootCacheValue> toDeleteMap, final IMap<IObjRef, IChangeContainer> objRefToChangeContainerMap, final IRootCache rootCache)
	{
		IObjRefHelper objRefHelper = this.objRefHelper;
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
						Object entity = toDeleteMap.get(changeContainer.getReference());
						reference = objRefHelper.entityToObjRef(entity);
						changeContainer.setReference(reference);
					}
					IDeleteCommand deleteCommand = new DeleteCommand(changeContainer.getReference());
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
			multithreadingHelper.invokeAndWait(previousParentToMovedOrisMap,
					new IResultingBackgroundWorkerParamDelegate<IList<IChangeContainer>, Entry<CheckForPreviousParentKey, IList<IObjRef>>>()
					{
						@Override
						public IList<IChangeContainer> invoke(Entry<CheckForPreviousParentKey, IList<IObjRef>> itemOfFork) throws Throwable
						{
							CheckForPreviousParentKey key = itemOfFork.getKey();
							IList<IObjRef> value = itemOfFork.getValue();
							return PersistenceMergeServiceExtension.this.relationMergeService.checkForPreviousParent(value, key.entityType, key.memberName);
						}
					}, new IAggregrateResultHandler<IList<IChangeContainer>, Entry<CheckForPreviousParentKey, IList<IObjRef>>>()
					{
						@Override
						public void aggregateResult(IList<IChangeContainer> resultOfFork, Entry<CheckForPreviousParentKey, IList<IObjRef>> itemOfFork)
						{
							changeQueue.pushAllFrom(resultOfFork);
						}
					});
			previousParentToMovedOrisMap.clear();

			multithreadingHelper.invokeAndWait(incomingRelationToReferenceMap,
					new IResultingBackgroundWorkerParamDelegate<IList<IChangeContainer>, Entry<IncomingRelationKey, IList<IObjRef>>>()
					{
						@Override
						public IList<IChangeContainer> invoke(Entry<IncomingRelationKey, IList<IObjRef>> itemOfFork) throws Throwable
						{
							IncomingRelationKey key = itemOfFork.getKey();
							IList<IObjRef> value = itemOfFork.getValue();
							return PersistenceMergeServiceExtension.this.relationMergeService.handleIncomingRelation(value, key.idIndex, key.table, key.link,
									toDeleteMap, objRefToChangeContainerMap, rootCache);
						}
					}, new IAggregrateResultHandler<IList<IChangeContainer>, Entry<IncomingRelationKey, IList<IObjRef>>>()
					{
						@Override
						public void aggregateResult(IList<IChangeContainer> resultOfFork, Entry<IncomingRelationKey, IList<IObjRef>> itemOfFork)
						{
							changeQueue.pushAllFrom(resultOfFork);
						}
					});
			incomingRelationToReferenceMap.clear();

			@SuppressWarnings("unused")
			IPrefetchState prefetchState = prefetchAllReferredMembers(outgoingRelationToReferenceMap, toDeleteMap, alreadyHandled, alreadyPrefetched, rootCache);

			multithreadingHelper.invokeAndWait(outgoingRelationToReferenceMap,
					new IResultingBackgroundWorkerParamDelegate<IList<IChangeContainer>, Entry<OutgoingRelationKey, IList<IObjRef>>>()
					{
						@Override
						public IList<IChangeContainer> invoke(Entry<OutgoingRelationKey, IList<IObjRef>> itemOfFork) throws Throwable
						{
							OutgoingRelationKey key = itemOfFork.getKey();
							IList<IObjRef> value = itemOfFork.getValue();
							return PersistenceMergeServiceExtension.this.relationMergeService.handleOutgoingRelation(value, key.idIndex, key.table, key.link,
									toDeleteMap, alreadyHandled, alreadyPrefetched, rootCache);
						}
					}, new IAggregrateResultHandler<IList<IChangeContainer>, Entry<OutgoingRelationKey, IList<IObjRef>>>()
					{
						@Override
						public void aggregateResult(IList<IChangeContainer> resultOfFork, Entry<OutgoingRelationKey, IList<IObjRef>> itemOfFork)
						{
							changeQueue.pushAllFrom(resultOfFork);
						}
					});
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

	protected void aquireAndAssignIds(IDatabase database, ILinkedMap<Class<?>, IList<IObjRef>> typeToIdlessReferenceMap)
	{
		for (Entry<Class<?>, IList<IObjRef>> entry : typeToIdlessReferenceMap)
		{
			ITable entityHandler = getEnsureTable(database, entry.getKey());
			IList<IObjRef> idlessReferences = entry.getValue();
			IList<Object> acquiredIds = entityHandler.acquireIds(idlessReferences.size());
			for (int i = idlessReferences.size(); i-- > 0;)
			{
				IObjRef reference = idlessReferences.get(i);
				reference.setId(acquiredIds.get(i));
				reference.setIdNameIndex(ObjRef.PRIMARY_KEY_INDEX);
			}
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
						Member idMethod = metaData.getIdMember();
						id = idMethod.getValue(directRef, false);
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
			IList<String[]> disabled = database.disableConstraints();
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
