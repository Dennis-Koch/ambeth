package de.osthus.ambeth.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.osthus.ambeth.cache.CacheDirective;
import de.osthus.ambeth.cache.ICache;
import de.osthus.ambeth.change.ILinkChangeCommand;
import de.osthus.ambeth.change.ITableChange;
import de.osthus.ambeth.change.LinkChangeCommand;
import de.osthus.ambeth.change.LinkContainer;
import de.osthus.ambeth.change.LinkTableChange;
import de.osthus.ambeth.change.TableChange;
import de.osthus.ambeth.change.UpdateCommand;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.ioc.IServiceContext;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.IObjRefHelper;
import de.osthus.ambeth.merge.model.IChangeContainer;
import de.osthus.ambeth.merge.model.IDirectObjRef;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.model.IRelationUpdateItem;
import de.osthus.ambeth.merge.transfer.DeleteContainer;
import de.osthus.ambeth.merge.transfer.DirectObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.merge.transfer.UpdateContainer;
import de.osthus.ambeth.objectcollector.IObjectCollector;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.persistence.IDatabase;
import de.osthus.ambeth.persistence.IDirectedLink;
import de.osthus.ambeth.persistence.ITable;
import de.osthus.ambeth.persistence.IVersionCursor;
import de.osthus.ambeth.persistence.IVersionItem;
import de.osthus.ambeth.query.IOperand;
import de.osthus.ambeth.query.IQuery;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.IPrefetchConfig;
import de.osthus.ambeth.util.IPrefetchHandle;
import de.osthus.ambeth.util.IPrefetchHelper;
import de.osthus.ambeth.util.ListUtil;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.StringBuilderUtil;

public class RelationMergeService implements IRelationMergeService, IInitializingBean
{
	@LogInstance
	private ILogger log;

	protected IServiceContext beanContext;

	protected ICache cache;

	protected IConversionHelper conversionHelper;

	protected IDatabase database;

	protected IEntityMetaDataProvider entityMetaDataProvider;

	protected IObjectCollector objectCollector;

	protected IObjRefHelper oriHelper;

	protected IPrefetchHelper prefetchHelper;

	protected IQueryBuilderFactory queryBuilderFactory;

	@Override
	public void afterPropertiesSet()
	{
		ParamChecker.assertNotNull(beanContext, "beanContext");
		ParamChecker.assertNotNull(cache, "cache");
		ParamChecker.assertNotNull(conversionHelper, "conversionHelper");
		ParamChecker.assertNotNull(database, "database");
		ParamChecker.assertNotNull(entityMetaDataProvider, "entityMetaDataProvider");
		ParamChecker.assertNotNull(objectCollector, "objectCollector");
		ParamChecker.assertNotNull(oriHelper, "oriHelper");
		ParamChecker.assertNotNull(prefetchHelper, "prefetchHelper");
		ParamChecker.assertNotNull(queryBuilderFactory, "queryBuilderFactory");
	}

	public void setBeanContext(IServiceContext beanContext)
	{
		this.beanContext = beanContext;
	}

	public void setCache(ICache cache)
	{
		this.cache = cache;
	}

	public void setConversionHelper(IConversionHelper conversionHelper)
	{
		this.conversionHelper = conversionHelper;
	}

	public void setDatabase(IDatabase database)
	{
		this.database = database;
	}

	public void setEntityMetaDataProvider(IEntityMetaDataProvider entityMetaDataProvider)
	{
		this.entityMetaDataProvider = entityMetaDataProvider;
	}

	public void setObjectCollector(IObjectCollector objectCollector)
	{
		this.objectCollector = objectCollector;
	}

	public void setPrefetchHelper(IPrefetchHelper prefetchHelper)
	{
		this.prefetchHelper = prefetchHelper;
	}

	public void setQueryBuilderFactory(IQueryBuilderFactory queryBuilderFactory)
	{
		this.queryBuilderFactory = queryBuilderFactory;
	}

	public void setOriHelper(IObjRefHelper oriHelper)
	{
		this.oriHelper = oriHelper;
	}

	@Override
	public ITableChange getTableChange(IMap<String, ITableChange> tableChangeMap, Object entityHandler, String entityHandlerName)
	{
		ITableChange tableChange = tableChangeMap.get(entityHandlerName);
		if (tableChange == null)
		{
			Class<? extends ITableChange> tableChangeType = entityHandler != null ? TableChange.class : LinkTableChange.class;
			tableChange = beanContext.registerAnonymousBean(tableChangeType).propertyValue("EntityHandlerName", entityHandlerName)
					.propertyValue("Table", entityHandler).finish();
			tableChangeMap.put(entityHandlerName, tableChange);
		}
		return tableChange;
	}

	@Override
	public void handleUpdateNotifications(Class<?> parentType, IRelationUpdateItem[] ruis, IMap<String, ITableChange> tableChangeMap)
	{
		if (ruis == null)
		{
			return;
		}
		IDatabase database = this.database.getCurrent();
		IEntityMetaData parentMetaData = entityMetaDataProvider.getMetaData(parentType);
		for (int i = ruis.length; i-- > 0;)
		{
			IRelationUpdateItem rui = ruis[i];

			ITypeInfoItem relationMethod = parentMetaData.getMemberByName(rui.getMemberName());
			Class<?> childType = relationMethod.getElementType();
			if (!parentMetaData.isRelatingToThis(childType))
			{
				continue;
			}

			ITableChange tableChange = null;

			IObjRef[] added = rui.getAddedORIs();
			if (added != null && added.length > 0)
			{
				ITable otherTable = database.getTableByType(added[0].getRealType());
				tableChange = getTableChange(tableChangeMap, otherTable, otherTable.getName());
				createUpdateNotifications(tableChange, Arrays.asList(added));
			}

			IObjRef[] removed = rui.getRemovedORIs();
			if (removed != null && removed.length > 0)
			{
				if (tableChange == null)
				{
					ITable otherTable = database.getTableByType(removed[0].getRealType());
					tableChange = getTableChange(tableChangeMap, otherTable, otherTable.getName());
				}
				createUpdateNotifications(tableChange, Arrays.asList(removed));
			}
		}
	}

	@Override
	public void handleUpdateNotifications(ILinkChangeCommand changeCommand, IMap<String, ITableChange> tableChangeMap)
	{
		IDirectedLink fromLink = changeCommand.getDirectedLink();
		IDirectedLink toLink = fromLink.getReverse();

		ITypeInfoItem member = toLink.getMember();
		if (member != null)
		{
			ITable table = toLink.getFromTable();
			ITableChange tableChange = getTableChange(tableChangeMap, table, table.getName());
			createUpdateNotifications(tableChange, changeCommand.getRefsToLink());
			createUpdateNotifications(tableChange, changeCommand.getRefsToUnlink());
		}
	}

	protected void createUpdateNotifications(ITableChange tableChange, List<IObjRef> references)
	{
		for (int i = references.size(); i-- > 0;)
		{
			UpdateCommand command = new UpdateCommand();
			command.setReference(references.get(i));
			tableChange.addChangeCommand(command);
		}
	}

	@Override
	public IList<IChangeContainer> processCreateDependencies(IObjRef reference, ITable table, IRelationUpdateItem[] ruis,
			Map<CheckForPreviousParentKey, IList<IObjRef>> previousParentToMovedOrisMap, HashSet<IObjRef> allAddedORIs)
	{
		return processInsertAndUpdateDependencies(reference, table, ruis, null, previousParentToMovedOrisMap, allAddedORIs);
	}

	@Override
	public IList<IChangeContainer> processUpdateDependencies(IObjRef reference, ITable table, IRelationUpdateItem[] ruis, Map<IObjRef, Object> toDeleteMap,
			Map<CheckForPreviousParentKey, IList<IObjRef>> previousParentToMovedOrisMap, HashSet<IObjRef> allAddedORIs)
	{
		List<IDirectedLink> links = table.getLinks();
		if (links.isEmpty() || ruis == null || ruis.length == 0)
		{
			return EmptyList.getInstance();
		}
		return processInsertAndUpdateDependencies(reference, table, ruis, toDeleteMap, previousParentToMovedOrisMap, allAddedORIs);
	}

	protected IList<IChangeContainer> processInsertAndUpdateDependencies(IObjRef reference, ITable table, IRelationUpdateItem[] ruis,
			Map<IObjRef, Object> toDeleteMap, Map<CheckForPreviousParentKey, IList<IObjRef>> previousParentToMovedOrisMap, HashSet<IObjRef> allAddedORIs)
	{
		if (ruis == null || ruis.length == 0)
		{
			return EmptyList.getInstance();
		}
		ArrayList<IChangeContainer> changeContainers = new ArrayList<IChangeContainer>();

		ICache cache = this.cache;
		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(reference.getRealType());
		for (int a = ruis.length; a-- > 0;)
		{
			IRelationUpdateItem rui = ruis[a];
			IDirectedLink link = table.getLinkByMemberName(rui.getMemberName());
			if (link == null)
			{
				throw new RuntimeException("No link found for member '" + rui.getMemberName() + "' on entity '" + table.getEntityType() + "'");
			}
			if (!link.isPersistingLink())
			{
				continue;
			}
			IObjRef[] removedORIs = rui.getRemovedORIs();
			IObjRef[] addedORIs = rui.getAddedORIs();
			if (removedORIs != null && removedORIs.length > 0)
			{
				if (link.isCascadeDelete())
				{
					IList<Object> objectsToDelete = cache.getObjects(removedORIs, CacheDirective.returnMisses());
					for (int b = objectsToDelete.size(); b-- > 0;)
					{
						Object objectToDelete = objectsToDelete.get(b);
						IObjRef removedORI = removedORIs[b];
						if (allAddedORIs.contains(removedORI))
						{
							// Entity was not orphaned but moved
							continue;
						}
						if (objectToDelete == null)
						{
							throw new IllegalStateException("Entity could not be retrieved: " + removedORI);
						}
						DeleteContainer cascadeDeleteContainer = new DeleteContainer();
						cascadeDeleteContainer.setReference(removedORI);
						changeContainers.add(cascadeDeleteContainer);

						toDeleteMap.put(removedORI, objectToDelete);
					}
				}

				ILinkChangeCommand command = new LinkChangeCommand();
				command.setLink(link);
				command.setReference(reference);
				for (int i = removedORIs.length; i-- > 0;)
				{
					IObjRef removedORI = removedORIs[i];
					command.getRefsToUnlink().add(removedORI);
				}
				LinkContainer linkContainer = new LinkContainer();
				linkContainer.setReference(reference);
				linkContainer.setCommand(command);
				changeContainers.add(linkContainer);
			}
			if (addedORIs != null && addedORIs.length > 0)
			{
				if (!link.getReverse().isStandaloneLink())
				{
					CheckForPreviousParentKey key = new CheckForPreviousParentKey(metaData.getEntityType(), rui.getMemberName());
					IList<IObjRef> movedOris = previousParentToMovedOrisMap.get(key);
					if (movedOris == null)
					{
						movedOris = new ArrayList<IObjRef>();
						previousParentToMovedOrisMap.put(key, movedOris);
					}
					movedOris.addAll(addedORIs);
				}
				ILinkChangeCommand command = new LinkChangeCommand();
				command.setLink(link);
				command.setReference(reference);
				for (int i = addedORIs.length; i-- > 0;)
				{
					command.getRefsToLink().add(addedORIs[i]);
				}
				LinkContainer linkContainer = new LinkContainer();
				linkContainer.setReference(reference);
				linkContainer.setCommand(command);
				changeContainers.add(linkContainer);
			}
		}
		return changeContainers;
	}

	protected ILinkedMap<String, IList<Object>> buildPropertyNameToIdsMap(List<IObjRef> oris, Class<?> entityType)
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
		LinkedHashMap<String, IList<Object>> propertyNameToIdsMap = new LinkedHashMap<String, IList<Object>>();

		// Check for all oris and map the ids to their corresponding member name
		for (int a = oris.size(); a-- > 0;)
		{
			IObjRef ori = oris.get(a);
			ITypeInfoItem idMember = metaData.getIdMemberByIdIndex(ori.getIdNameIndex());
			IList<Object> idsList = propertyNameToIdsMap.get(idMember.getName());
			if (idsList == null)
			{
				idsList = new ArrayList<Object>();
				propertyNameToIdsMap.put(idMember.getName(), idsList);
			}
			idsList.add(ori.getId());
		}
		return propertyNameToIdsMap;
	}

	protected void disposePropertyNameToIdsMap(ILinkedMap<String, IList<Object>> childMemberNameToIdsMap)
	{
		childMemberNameToIdsMap.clear();
	}

	protected IQuery<?> buildParentChildQuery(Class<?> selectedEntityType, String selectingMemberName, ILinkedMap<String, IList<Object>> childMemberNameToIdsMap)
	{
		if (childMemberNameToIdsMap.size() == 0)
		{
			throw new IllegalArgumentException("Illegal map");
		}
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		IQueryBuilder<?> qb = queryBuilderFactory.create(selectedEntityType);
		IOperand operand = null;
		// Build IS IN clauses for each referred member name
		for (Entry<String, IList<Object>> entry : childMemberNameToIdsMap)
		{
			String childMemberName = entry.getKey();
			String propertyName = StringBuilderUtil.concat(tlObjectCollector, selectingMemberName, ".", childMemberName);
			IOperand inOperator = qb.isIn(qb.property(propertyName), qb.valueName(propertyName));
			if (operand == null)
			{
				operand = inOperator;
			}
			else
			{
				operand = qb.or(operand, inOperator);
			}
		}
		return qb.build(operand);
	}

	protected IQuery<?> parameterizeParentChildQuery(IQuery<?> query, String selectingMemberName, ILinkedMap<String, IList<Object>> childMemberNameToIdsMap)
	{
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		// Parameterize query for each referred member name
		for (Entry<String, IList<Object>> entry : childMemberNameToIdsMap)
		{
			String childMemberName = entry.getKey();
			String propertyName = StringBuilderUtil.concat(tlObjectCollector, selectingMemberName, ".", childMemberName);
			query = query.param(propertyName, entry.getValue());
		}
		return query;
	}

	@Override
	public IList<IChangeContainer> checkForPreviousParent(List<IObjRef> oris, Class<?> entityType, String memberName)
	{
		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
		ITypeInfoItem member = metaData.getMemberByName(memberName);
		Class<?> parentType = metaData.getEntityType();

		ILinkedMap<String, IList<Object>> childMemberNameToIdsMap = buildPropertyNameToIdsMap(oris, member.getElementType());
		IQuery<?> query = buildParentChildQuery(parentType, memberName, childMemberNameToIdsMap);
		query = parameterizeParentChildQuery(query, memberName, childMemberNameToIdsMap);

		ArrayList<IChangeContainer> changeContainers = new ArrayList<IChangeContainer>();
		Class<?> idType = metaData.getIdMember().getRealType();
		Class<?> versionType = metaData.getVersionMember() != null ? metaData.getVersionMember().getRealType() : null;
		IVersionCursor cursor = query.retrieveAsVersions();
		try
		{
			IConversionHelper conversionHelper = this.conversionHelper;
			while (cursor.moveNext())
			{
				IVersionItem item = cursor.getCurrent();
				Object oldParentId = conversionHelper.convertValueToType(idType, item.getId());
				Object oldParentVersion = versionType != null ? conversionHelper.convertValueToType(versionType, item.getVersion()) : null;
				UpdateContainer updateContainer = new UpdateContainer();
				ObjRef objRef = new ObjRef(parentType, ObjRef.PRIMARY_KEY_INDEX, oldParentId, oldParentVersion);
				updateContainer.setReference(objRef);
				changeContainers.add(updateContainer);
			}
		}
		finally
		{
			cursor.dispose();
		}
		disposePropertyNameToIdsMap(childMemberNameToIdsMap);
		return changeContainers;
	}

	@Override
	public IList<IChangeContainer> processDeleteDependencies(IObjRef reference, ITable table, Map<IObjRef, Object> toDeleteMap,
			Map<OutgoingRelationKey, IList<IObjRef>> outgoingRelationToReferenceMap, Map<IncomingRelationKey, IList<IObjRef>> incomingRelationToReferenceMap,
			Map<CheckForPreviousParentKey, IList<IObjRef>> previousParentToMovedOrisMap, HashSet<IObjRef> allAddedORIs)
	{
		List<IDirectedLink> links = table.getLinks();
		if (links.isEmpty())
		{
			return EmptyList.getInstance();
		}
		ArrayList<IChangeContainer> changeContainers = new ArrayList<IChangeContainer>();
		for (int i = links.size(); i-- > 0;)
		{
			IDirectedLink link = links.get(i);

			IDirectedLink reverseLink = link.getReverse();
			if (reverseLink.getMember() != null)
			{
				ITypeInfoItem member = reverseLink.getMember();
				Class<?> entityType = reverseLink.getFromTable().getEntityType();
				CheckForPreviousParentKey key = new CheckForPreviousParentKey(entityType, member.getName());
				IList<IObjRef> movedOris = previousParentToMovedOrisMap.get(key);
				if (movedOris == null)
				{
					movedOris = new ArrayList<IObjRef>();
					previousParentToMovedOrisMap.put(key, movedOris);
				}
				movedOris.add(reference);
			}

			boolean cascadeDelete = link.isCascadeDelete();
			boolean selfRelation = link.getToTable().equals(table);
			boolean removeRelations;
			if (selfRelation)
			{
				removeRelations = true;
			}
			else if (cascadeDelete)
			{
				removeRelations = link.isStandaloneLink() && link.getReverse().isStandaloneLink();
			}
			else
			{
				removeRelations = link.isStandaloneLink();
			}

			if (!cascadeDelete && !removeRelations)
			{
				continue;
			}

			if (link.getMember() != null)
			{
				OutgoingRelationKey key = new OutgoingRelationKey(reference.getIdNameIndex(), table, link);
				IList<IObjRef> movedOris = outgoingRelationToReferenceMap.get(key);
				if (movedOris == null)
				{
					movedOris = new ArrayList<IObjRef>();
					outgoingRelationToReferenceMap.put(key, movedOris);
				}
				movedOris.add(reference);
				// handleOutgoingRelation(reference, toDeleteMap, link, changeContainers, cascadeDelete,
				// removeRelations);
			}
			Boolean becauseOfSelfRelation = null;
			if (link.getReverse().getMember() != null)
			{
				becauseOfSelfRelation = Boolean.FALSE;
			}
			if (selfRelation && link.getMember() != null)
			{
				becauseOfSelfRelation = Boolean.TRUE;
			}
			if (becauseOfSelfRelation != null)
			{
				IncomingRelationKey key = new IncomingRelationKey(reference.getIdNameIndex(), table, link);
				IList<IObjRef> movedOris = incomingRelationToReferenceMap.get(key);
				if (movedOris == null)
				{
					movedOris = new ArrayList<IObjRef>();
					incomingRelationToReferenceMap.put(key, movedOris);
				}
				movedOris.add(reference);
				// handleIncomingRelation(reference, table, toDeleteMap, link, changeContainers, cascadeDelete,
				// removeRelations,
				// becauseOfSelfRelation.booleanValue());
			}
		}
		return changeContainers;
	}

	protected IList<IChangeContainer> handleOutgoingRelation(List<IObjRef> references, byte idIndex2, IDirectedLink link, boolean cascadeDelete,
			boolean removeRelations, Map<IObjRef, Object> toDeleteMap, Set<EntityLinkKey> alreadyHandled, Set<Object> alreadyPrefetched)
	{
		IObjRefHelper oriHelper = this.oriHelper;

		ArrayList<IChangeContainer> changeContainers = new ArrayList<IChangeContainer>();

		IEntityMetaData metadata = entityMetaDataProvider.getMetaData(references.get(0).getRealType());
		ITypeInfoItem member = link.getMember();

		List<Object> entities = new ArrayList<Object>();
		for (IObjRef reference : references)
		{
			Object entity = toDeleteMap.get(reference);
			EntityLinkKey elk = new EntityLinkKey(entity, link);
			if (!alreadyHandled.add(elk))
			{
				continue;
			}
			entities.add(entity);
		}
		if (entities.isEmpty())
		{
			return changeContainers;
		}

		prefetchAllRelations(entities, metadata, alreadyPrefetched);

		removeRelations &= link.isNullable();
		byte idIndex = link.getToField().getIdIndex();
		IEntityMetaData relatedMetaData = entityMetaDataProvider.getMetaData(link.getToEntityType());

		for (IObjRef reference : references)
		{
			Object entity = toDeleteMap.get(reference);
			Object related;
			related = member.getValue(entity, false);

			if (related == null)
			{
				continue;
			}
			List<Object> relatedObjects = ListUtil.anyToList(related);
			if (relatedObjects.isEmpty())
			{
				continue;
			}

			IObjRef[] relatedRefs;
			relatedRefs = new IObjRef[relatedObjects.size()];
			for (int j = relatedObjects.size(); j-- > 0;)
			{
				IObjRef objRef = oriHelper.entityToObjRef(relatedObjects.get(j), idIndex, relatedMetaData);
				relatedRefs[j] = objRef;
			}

			if (cascadeDelete)
			{
				for (int j = relatedObjects.size(); j-- > 0;)
				{
					Object relatedEntity = relatedObjects.get(j);
					IObjRef objRef = relatedRefs[j];

					if (toDeleteMap.containsKey(objRef))
					{
						continue;
					}
					toDeleteMap.put(objRef, relatedEntity);

					DeleteContainer container = new DeleteContainer();
					container.setReference(objRef);
					changeContainers.add(container);
				}
			}

			if (removeRelations)
			{
				if (link.isNullable())
				{
					List<IObjRef> fromOris = Arrays.asList(reference);
					List<IObjRef> toOris = Arrays.asList(relatedRefs);
					addLinkChangeContainer(changeContainers, link, fromOris, toOris);
				}
				else
				{
					if (log.isWarnEnabled())
					{
						log.warn("Deletion may fail due to not nullable link from other table!");
					}
				}
			}
		}

		return changeContainers;
	}

	protected void prefetchAllRelations(List<Object> entities, IEntityMetaData metadata, Set<Object> alreadyPrefeched)
	{
		List<Object> toPrefetch = new ArrayList<Object>(entities.size());
		for (int i = entities.size(); i-- > 0;)
		{
			Object entity = entities.get(i);
			if (!alreadyPrefeched.contains(entity))
			{
				toPrefetch.add(entity);
			}
		}
		if (toPrefetch.isEmpty())
		{
			return;
		}

		alreadyPrefeched.addAll(toPrefetch);

		Class<?> entityType = metadata.getEntityType();
		IPrefetchConfig prefetchConfig = prefetchHelper.createPrefetch();
		for (ITypeInfoItem member : metadata.getRelationMembers())
		{
			prefetchConfig.add(entityType, member.getName());
		}
		IPrefetchHandle prefetchHandle = prefetchConfig.build();
		prefetchHandle.prefetch(toPrefetch);
	}

	@Override
	public IList<IChangeContainer> handleOutgoingRelation(List<IObjRef> references, byte idIndex, ITable table, IDirectedLink link,
			Map<IObjRef, Object> toDeleteMap, Set<EntityLinkKey> alreadyHandled, Set<Object> alreadyPrefetched)
	{
		boolean cascadeDelete = link.isCascadeDelete();
		boolean selfRelation = link.getToTable().equals(table);
		boolean removeRelations;
		if (selfRelation)
		{
			removeRelations = true;
		}
		else if (cascadeDelete)
		{
			removeRelations = link.isStandaloneLink() && link.getReverse().isStandaloneLink();
		}
		else
		{
			removeRelations = link.isStandaloneLink();
		}
		if (!cascadeDelete && !removeRelations)
		{
			throw new IllegalStateException("Must never happen, because the queueing map would not have been filled with this state");
		}
		return handleOutgoingRelation(references, idIndex, link, cascadeDelete, removeRelations, toDeleteMap, alreadyHandled, alreadyPrefetched);
	}

	@Override
	public IList<IChangeContainer> handleIncomingRelation(List<IObjRef> references, byte idIndex, ITable table, IDirectedLink link,
			Map<IObjRef, Object> toDeleteMap)
	{
		boolean cascadeDelete = link.isCascadeDelete();
		boolean selfRelation = link.getToTable().equals(table);
		boolean removeRelations;
		if (selfRelation)
		{
			removeRelations = true;
		}
		else if (cascadeDelete)
		{
			removeRelations = link.isStandaloneLink() && link.getReverse().isStandaloneLink();
		}
		else
		{
			removeRelations = link.isStandaloneLink();
		}
		if (!cascadeDelete && !removeRelations)
		{
			throw new IllegalStateException("Must never happen, because the queueing map would not have been filled with this state");
		}
		Boolean becauseOfSelfRelation = null;
		if (link.getReverse().getMember() != null)
		{
			becauseOfSelfRelation = Boolean.FALSE;
		}
		if (selfRelation && link.getMember() != null)
		{
			becauseOfSelfRelation = Boolean.TRUE;
		}
		if (becauseOfSelfRelation == null)
		{
			throw new IllegalStateException("Must never happen, because the queueing map would not have been filled with this state");
		}
		return handleIncomingRelation(references, idIndex, table, link, cascadeDelete, removeRelations, becauseOfSelfRelation, toDeleteMap);
	}

	protected IList<IChangeContainer> handleIncomingRelation(List<IObjRef> references, byte srcIdIndex, ITable table, IDirectedLink link,
			boolean cascadeDelete, boolean removeRelations, boolean becauseOfSelfRelation, Map<IObjRef, Object> toDeleteMap)
	{
		IConversionHelper conversionHelper = this.conversionHelper;
		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		IObjRefHelper oriHelper = this.oriHelper;
		IEntityMetaData relatedMetaData = entityMetaDataProvider.getMetaData(link.getToTable().getEntityType());
		Class<?> relatedType = relatedMetaData.getEntityType();
		ITypeInfoItem member = becauseOfSelfRelation ? link.getMember() : link.getReverse().getMember();
		removeRelations &= becauseOfSelfRelation ? link.isNullable() : link.getReverse().isNullable();

		ILinkedMap<String, IList<Object>> childMemberNameToIdsMap = buildPropertyNameToIdsMap(references, member.getElementType());
		IQuery<?> query = buildParentChildQuery(relatedType, member.getName(), childMemberNameToIdsMap);
		query = parameterizeParentChildQuery(query, member.getName(), childMemberNameToIdsMap);

		ArrayList<IObjRef> relatingRefs = new ArrayList<IObjRef>();

		ArrayList<IChangeContainer> changeContainers = new ArrayList<IChangeContainer>();

		try
		{
			if (cascadeDelete)
			{
				IList<?> relatingEntities = query.retrieve();
				byte idIndex = becauseOfSelfRelation ? link.getToField().getIdIndex() : link.getReverse().getToField().getIdIndex();
				for (int j = 0; j < relatingEntities.size(); j++)
				{
					Object relatingEntity = relatingEntities.get(j);
					IObjRef relatingRef = oriHelper.entityToObjRef(relatingEntity, idIndex, relatedMetaData);
					relatingRefs.add(relatingRef);

					if (toDeleteMap.containsKey(relatingRef))
					{
						continue;
					}
					toDeleteMap.put(relatingRef, relatingEntity);

					DeleteContainer container = new DeleteContainer();
					container.setReference(relatingRef);
					changeContainers.add(container);
				}
			}
			else
			{
				IVersionCursor cursor = query.retrieveAsVersions();
				try
				{
					Class<?> relatingIdType = relatedMetaData.getIdMember().getElementType();
					Class<?> relatingVersionType = relatedMetaData.getVersionMember().getElementType();
					while (cursor.moveNext())
					{
						IVersionItem versionItem = cursor.getCurrent();
						Object id = conversionHelper.convertValueToType(relatingIdType, versionItem.getId());
						Object version = conversionHelper.convertValueToType(relatingVersionType, versionItem.getVersion());
						relatingRefs.add(new ObjRef(relatedType, id, version));
					}
				}
				finally
				{
					cursor.dispose();
				}
			}

			if (removeRelations && !relatingRefs.isEmpty())
			{
				IDirectedLink directedLink = becauseOfSelfRelation ? link : link.getReverse();
				addLinkChangeContainer(changeContainers, directedLink, relatingRefs, references);
			}
			return changeContainers;
		}
		finally
		{
			disposePropertyNameToIdsMap(childMemberNameToIdsMap);
		}
	}

	protected void addLinkChangeContainer(List<IChangeContainer> changeContainers, IDirectedLink link, List<IObjRef> fromOris, List<IObjRef> toOris)
	{
		IDirectedLink directedLink = link.getLink().getDirectedLink();
		if (!directedLink.equals(link))
		{
			List<IObjRef> temp = fromOris;
			fromOris = toOris;
			toOris = temp;
		}

		for (int i = fromOris.size(); i-- > 0;)
		{
			ILinkChangeCommand command = new LinkChangeCommand();
			IObjRef fromOri = fromOris.get(i);
			command.setLink(directedLink);
			command.setReference(fromOri);
			for (int j = toOris.size(); j-- > 0;)
			{
				command.getRefsToUnlink().add(toOris.get(j));
			}
			LinkContainer linkContainer = new LinkContainer();
			linkContainer.setReference(fromOri);
			linkContainer.setCommand(command);
			changeContainers.add(linkContainer);
		}
	}

	@Override
	public void checkForCorrectIdIndex(ILinkChangeCommand changeCommand, IMap<Byte, IList<IObjRef>> toChange)
	{
		List<IObjRef> toLink = changeCommand.getRefsToLink();
		if (!toLink.isEmpty())
		{
			byte idIndex = changeCommand.getDirectedLink().getToIdIndex();
			IList<IObjRef> toChangeList = toChange.get(idIndex);
			for (int i = toLink.size(); i-- > 0;)
			{
				IObjRef ori = toLink.get(i);
				if (ori.getIdNameIndex() != idIndex)
				{
					if (toChangeList == null)
					{
						toChangeList = new ArrayList<IObjRef>();
						toChange.put(idIndex, toChangeList);
					}
					IObjRef newOri;
					if (ori instanceof IDirectObjRef)
					{
						newOri = new DirectObjRef(ori.getRealType(), ((IDirectObjRef) ori).getDirect());
					}
					else
					{
						newOri = new ObjRef(ori.getRealType(), ori.getIdNameIndex(), ori.getId(), ori.getVersion());
					}
					toChangeList.add(newOri);
					toLink.set(i, newOri);
				}
			}
		}
	}
}
