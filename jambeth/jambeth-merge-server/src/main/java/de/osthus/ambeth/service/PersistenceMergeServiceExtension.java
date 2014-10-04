package de.osthus.ambeth.service;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;

import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.CacheFactoryDirective;
import de.osthus.ambeth.cache.ICacheContext;
import de.osthus.ambeth.cache.ICacheFactory;
import de.osthus.ambeth.cache.IDisposableCache;
import de.osthus.ambeth.cache.ISingleCacheRunnable;
import de.osthus.ambeth.cache.transfer.LoadContainer;
import de.osthus.ambeth.change.CreateCommand;
import de.osthus.ambeth.change.DeleteCommand;
import de.osthus.ambeth.change.IChangeCommand;
import de.osthus.ambeth.change.ICreateCommand;
import de.osthus.ambeth.change.IDeleteCommand;
import de.osthus.ambeth.change.ILinkChangeCommand;
import de.osthus.ambeth.change.ITableChange;
import de.osthus.ambeth.change.IUpdateCommand;
import de.osthus.ambeth.change.LinkContainer;
import de.osthus.ambeth.change.UpdateCommand;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.FastList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.compositeid.ICompositeIdFactory;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.DefaultExtendableContainer;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IMergeListener;
import de.osthus.ambeth.merge.IMergeListenerExtendable;
import de.osthus.ambeth.merge.IMergeSecurityManager;
import de.osthus.ambeth.merge.IMergeServiceExtension;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.IValueObjectConfig;
import de.osthus.ambeth.merge.model.ICUDResult;
import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.model.IDirectObjRef;
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
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.model.IMethodDescription;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.IContextProvider;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.persistence.parallel.IModifyingDatabase;
import de.osthus.ambeth.proxy.PersistenceContext;
import de.osthus.ambeth.proxy.PersistenceContext.PersistenceContextType;
import de.osthus.ambeth.security.ISecurityActivation;
import de.osthus.ambeth.security.SecurityContext;
import de.osthus.ambeth.security.SecurityContext.SecurityContextType;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerDelegate;
import de.osthus.ambeth.util.EqualsUtil;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.OptimisticLockUtil;
import de.osthus.ambeth.util.StringBuilderUtil;

@SecurityContext(SecurityContextType.AUTHENTICATED)
@PersistenceContext
public class PersistenceMergeServiceExtension implements IMergeServiceExtension, IMergeListenerExtendable
{
	@LogInstance
	private ILogger log;

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected ICacheContext cacheContext;

	@Autowired
	protected ICacheFactory cacheFactory;

	@Autowired
	protected ICompositeIdFactory compositeIdFactory;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IDatabase database;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired(optional = true)
	protected IMergeSecurityManager mergeSecurityManager;

	@Autowired
	protected IThreadLocalObjectCollector objectCollector;

	@Autowired
	protected IObjRefHelper oriHelper;

	@Autowired
	protected IRelationMergeService relationMergeService;

	@Autowired
	protected ISecurityActivation securityActivation;

	protected final DefaultExtendableContainer<IMergeListener> mergeListeners = new DefaultExtendableContainer<IMergeListener>(IMergeListener.class,
			"mergeListener");

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

	@Override
	public IOriCollection merge(ICUDResult cudResult, IMethodDescription methodDescription)
	{
		final IDatabase database = this.database.getCurrent();
		try
		{
			if (mergeSecurityManager != null)
			{
				mergeSecurityManager.checkMergeAccess(cudResult, methodDescription);
			}
			for (IMergeListener mergeListener : mergeListeners.getExtensions())
			{
				mergeListener.preMerge(cudResult);
			}
			final List<IChangeContainer> allChanges = cudResult.getAllChanges();
			final HashMap<String, ITableChange> tableChangeMap = new HashMap<String, ITableChange>();
			final List<IObjRef> oriList = new java.util.ArrayList<IObjRef>(allChanges.size());

			IDisposableCache childCache = cacheFactory.createPrivileged(CacheFactoryDirective.NoDCE, false, Boolean.FALSE);
			try
			{
				final IDisposableCache fChildCache = childCache;
				cacheContext.executeWithCache(childCache, new ISingleCacheRunnable<Object>()
				{
					@Override
					public Object run() throws Throwable
					{
						return securityActivation.executeWithoutSecurity(new IResultingBackgroundWorkerDelegate<Object>()
						{
							@Override
							public Object invoke() throws Throwable
							{
								HashMap<IObjRef, Object> toDeleteMap = new HashMap<IObjRef, Object>();
								LinkedHashMap<ITableChange, IList<ILinkChangeCommand>> linkChangeCommands = new LinkedHashMap<ITableChange, IList<ILinkChangeCommand>>();
								LinkedHashMap<Class<?>, IList<IObjRef>> typeToIdlessReferenceMap = new LinkedHashMap<Class<?>, IList<IObjRef>>();
								ArrayList<IObjRef> toLoadForDeletion = new ArrayList<IObjRef>();
								fillOriList(oriList, allChanges, toLoadForDeletion);

								loadEntitiesForDeletion(toLoadForDeletion, toDeleteMap, fChildCache);

								convertChangeContainersToCommands(database, allChanges, tableChangeMap, typeToIdlessReferenceMap, linkChangeCommands,
										toDeleteMap);

								aquireAndAssignIds(database, typeToIdlessReferenceMap);

								processLinkChangeCommands(linkChangeCommands, tableChangeMap, fChildCache);

								return null;
							}
						});
					}
				});
			}
			catch (Throwable e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			finally
			{
				childCache.dispose();
				childCache = null;
			}

			IChangeAggregator changeAggregator = persistTableChanges(database, tableChangeMap);
			changeAggregator.createDataChange();

			for (IMergeListener mergeListener : mergeListeners.getExtensions())
			{
				mergeListener.postMerge(cudResult);
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

	protected void fillOriList(List<IObjRef> oriList, List<IChangeContainer> allChanges, IList<IObjRef> toLoadForDeletion)
	{
		for (int a = 0, size = allChanges.size(); a < size; a++)
		{
			IChangeContainer changeContainer = allChanges.get(a);
			if (changeContainer instanceof CreateContainer)
			{
				oriList.add(changeContainer.getReference());
				((IDirectObjRef) changeContainer.getReference()).setDirect(changeContainer);
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
		ITable table = database.getTableByType(referenceClass);
		if (table == null)
		{
			throw new RuntimeException("No table configured for entity '" + referenceClass + "'");
		}
		return table;
	}

	protected void loadEntitiesForDeletion(IList<IObjRef> toLoadForDeletion, IMap<IObjRef, Object> toDeleteMap, IDisposableCache childCache)
	{
		IList<Object> objects = childCache.getObjects(toLoadForDeletion, CacheDirective.returnMisses());
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
			IList<IObjRef> references = oriHelper.entityToAllObjRefs(object);
			if (!EqualsUtil.equals(expectedVersion, references.get(0).getVersion()))
			{
				throw OptimisticLockUtil.throwModified(references.get(0), expectedVersion, object);
			}
			for (int j = references.size(); j-- > 0;)
			{
				IObjRef objRef = references.get(j);

				toDeleteMap.put(objRef, object);
			}
		}
	}

	protected void convertChangeContainersToCommands(IDatabase database, List<IChangeContainer> allChanges, IMap<String, ITableChange> tableChangeMap,
			ILinkedMap<Class<?>, IList<IObjRef>> typeToIdlessReferenceMap, ILinkedMap<ITableChange, IList<ILinkChangeCommand>> linkChangeCommands,
			IMap<IObjRef, Object> toDeleteMap)
	{
		IObjRefHelper oriHelper = this.oriHelper;
		IRelationMergeService relationMergeService = this.relationMergeService;

		FastList<IChangeContainer> changeQueue = new FastList<IChangeContainer>();

		changeQueue.pushAllFrom(allChanges);

		LinkedHashMap<CheckForPreviousParentKey, IList<IObjRef>> previousParentToMovedOrisMap = new LinkedHashMap<CheckForPreviousParentKey, IList<IObjRef>>();
		LinkedHashMap<IncomingRelationKey, IList<IObjRef>> incomingRelationToReferenceMap = new LinkedHashMap<IncomingRelationKey, IList<IObjRef>>();
		LinkedHashMap<OutgoingRelationKey, IList<IObjRef>> outgoingRelationToReferenceMap = new LinkedHashMap<OutgoingRelationKey, IList<IObjRef>>();
		HashSet<IObjRef> allAddedORIs = new HashSet<IObjRef>();
		HashSet<EntityLinkKey> alreadyHandled = new HashSet<EntityLinkKey>();
		HashSet<Object> alreadyPrefetched = new HashSet<Object>();

		findAllNewlyReferencedORIs(allChanges, allAddedORIs);

		while (true)
		{
			while (!changeQueue.isEmpty())
			{
				IChangeContainer changeContainer = changeQueue.popFirstItem();

				IObjRef reference = changeContainer.getReference();
				Object entityHandler;
				String entityHandlerName;
				if (!(changeContainer instanceof LinkContainer))
				{
					entityHandler = getEnsureTable(database, reference.getRealType());
					entityHandlerName = ((ITable) entityHandler).getName();
				}
				else
				{
					entityHandler = database.getTableByName(((LinkContainer) changeContainer).getTableName());
					if (entityHandler != null)
					{
						entityHandlerName = ((ITable) entityHandler).getName();
					}
					else
					{
						entityHandlerName = ((LinkContainer) changeContainer).getTableName();
					}
				}

				ITableChange tableChange = relationMergeService.getTableChange(tableChangeMap, entityHandler, entityHandlerName);

				IChangeCommand changeCommand = null;
				if (changeContainer instanceof CreateContainer)
				{
					CreateContainer createContainer = (CreateContainer) changeContainer;
					ICreateCommand createCommand = new CreateCommand();
					createCommand.configureFromContainer(createContainer, tableChange.getTable());
					changeCommand = createCommand;
					IList<IChangeContainer> newChanges = relationMergeService.processCreateDependencies(reference, (ITable) entityHandler,
							createContainer.getRelations(), previousParentToMovedOrisMap, allAddedORIs);
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
				else if (changeContainer instanceof UpdateContainer)
				{
					UpdateContainer updateContainer = (UpdateContainer) changeContainer;
					IUpdateCommand updateCommand = new UpdateCommand();
					updateCommand.configureFromContainer(updateContainer, tableChange.getTable());
					changeCommand = updateCommand;
					IList<IChangeContainer> newChanges = relationMergeService.processUpdateDependencies(reference, (ITable) entityHandler,
							updateContainer.getRelations(), toDeleteMap, previousParentToMovedOrisMap, allAddedORIs);
					changeQueue.pushAllFrom(newChanges);
					relationMergeService.handleUpdateNotifications(reference.getRealType(), updateContainer.getRelations(), tableChangeMap);
				}
				else if (changeContainer instanceof DeleteContainer)
				{
					if (reference.getIdNameIndex() != ObjRef.PRIMARY_KEY_INDEX)
					{
						Object entity = toDeleteMap.get(changeContainer.getReference());
						reference = oriHelper.entityToObjRef(entity);
						changeContainer.setReference(reference);
					}
					IDeleteCommand deleteCommand = new DeleteCommand();
					deleteCommand.configureFromContainer(changeContainer, tableChange.getTable());
					changeCommand = deleteCommand;
					IList<IChangeContainer> newChanges = relationMergeService.processDeleteDependencies(reference, (ITable) entityHandler, toDeleteMap,
							outgoingRelationToReferenceMap, incomingRelationToReferenceMap, previousParentToMovedOrisMap, allAddedORIs);
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
			{
				for (Entry<CheckForPreviousParentKey, IList<IObjRef>> entry : previousParentToMovedOrisMap)
				{
					CheckForPreviousParentKey key = entry.getKey();
					IList<IObjRef> value = entry.getValue();
					IList<IChangeContainer> newChanges = relationMergeService.checkForPreviousParent(value, key.entityType, key.memberName);
					changeQueue.pushAllFrom(newChanges);
				}
				previousParentToMovedOrisMap.clear();
			}
			{
				for (Entry<IncomingRelationKey, IList<IObjRef>> entry : incomingRelationToReferenceMap)
				{
					IncomingRelationKey key = entry.getKey();
					IList<IObjRef> value = entry.getValue();
					IList<IChangeContainer> newChanges = relationMergeService.handleIncomingRelation(value, key.idIndex, key.table, key.link, toDeleteMap);
					changeQueue.pushAllFrom(newChanges);
				}
				incomingRelationToReferenceMap.clear();
			}
			{
				for (Entry<OutgoingRelationKey, IList<IObjRef>> entry : outgoingRelationToReferenceMap)
				{
					OutgoingRelationKey key = entry.getKey();
					IList<IObjRef> value = entry.getValue();
					IList<IChangeContainer> newChanges = relationMergeService.handleOutgoingRelation(value, key.idIndex, key.table, key.link, toDeleteMap,
							alreadyHandled, alreadyPrefetched);
					changeQueue.pushAllFrom(newChanges);
				}
				incomingRelationToReferenceMap.clear();
			}
			if (changeQueue.isEmpty())
			{
				break;
			}
		}
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
			IRelationUpdateItem[] relationUpdateItems;
			if (changeContainer instanceof CreateContainer)
			{
				relationUpdateItems = ((CreateContainer) changeContainer).getRelations();
			}
			else if (changeContainer instanceof UpdateContainer)
			{
				relationUpdateItems = ((UpdateContainer) changeContainer).getRelations();
			}
			else
			{
				throw new IllegalArgumentException("Unknown IChangeContainer implementation: '" + changeContainer.getClass().getName() + "'");
			}

			if (relationUpdateItems == null)
			{
				continue;
			}
			for (IRelationUpdateItem relationUpdateItem : relationUpdateItems)
			{
				IObjRef[] addedORIs = relationUpdateItem.getAddedORIs();
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

	protected void processLinkChangeCommands(ILinkedMap<ITableChange, IList<ILinkChangeCommand>> linkChangeCommands,
			final IMap<String, ITableChange> tableChangeMap, final IDisposableCache childCache)
	{
		final IRelationMergeService relationMergeService = this.relationMergeService;
		final LinkedHashMap<Byte, IList<IObjRef>> toChange = new LinkedHashMap<Byte, IList<IObjRef>>();
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
					LoadContainer entity = (LoadContainer) childCache.getObject(ori, CacheDirective.loadContainerResult());
					if (idIndex == ObjRef.PRIMARY_KEY_INDEX)
					{
						id = entity.getReference().getId();
					}
					else
					{
						id = compositeIdFactory.createIdFromPrimitives(metaData, idIndex, entity.getPrimitives());
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

		IChangeAggregator changeAggregator = beanContext.registerAnonymousBean(ChangeAggregator.class).finish();
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
			for (int i = tableChangeList.size(); i-- > 0;)
			{
				ITableChange tableChange = tableChangeList.get(i);
				tableChange.dispose();
			}
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

	@Override
	@SecurityContext(SecurityContextType.NOT_REQUIRED)
	@PersistenceContext(PersistenceContextType.NOT_REQUIRED)
	public void registerMergeListener(IMergeListener mergeListener)
	{
		mergeListeners.register(mergeListener);
	}

	@Override
	@SecurityContext(SecurityContextType.NOT_REQUIRED)
	@PersistenceContext(PersistenceContextType.NOT_REQUIRED)
	public void unregisterMergeListener(IMergeListener mergeListener)
	{
		mergeListeners.unregister(mergeListener);
	}
}
