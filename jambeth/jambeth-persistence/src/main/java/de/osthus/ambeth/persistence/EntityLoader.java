package de.osthus.ambeth.persistence;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.osthus.ambeth.cache.CacheKey;
import de.osthus.ambeth.cache.model.ILoadContainer;
import de.osthus.ambeth.cache.model.IObjRelation;
import de.osthus.ambeth.cache.model.IObjRelationResult;
import de.osthus.ambeth.cache.transfer.LoadContainer;
import de.osthus.ambeth.cache.transfer.ObjRelationResult;
import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.ILinkedMap;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.collections.IdentityLinkedSet;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.compositeid.ICompositeIdFactory;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IEntityFactory;
import de.osthus.ambeth.merge.IEntityMetaDataProvider;
import de.osthus.ambeth.merge.model.IEntityMetaData;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.merge.transfer.ObjRef;
import de.osthus.ambeth.metadata.IObjRefFactory;
import de.osthus.ambeth.metadata.IPreparedObjRefFactory;
import de.osthus.ambeth.metadata.Member;
import de.osthus.ambeth.metadata.RelationMember;
import de.osthus.ambeth.persistence.parallel.IEntityLoaderParallelInvoker;
import de.osthus.ambeth.persistence.parallel.ParallelLoadCascadeItem;
import de.osthus.ambeth.persistence.parallel.ParallelLoadItem;
import de.osthus.ambeth.proxy.IObjRefContainer;
import de.osthus.ambeth.query.IOperator;
import de.osthus.ambeth.query.IQueryBuilder;
import de.osthus.ambeth.query.IQueryBuilderFactory;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.util.IAlreadyLoadedCache;
import de.osthus.ambeth.util.IConversionHelper;
import de.osthus.ambeth.util.IInterningFeature;
import de.osthus.ambeth.util.IdTypeTuple;

public class EntityLoader implements IEntityLoader, ILoadContainerProvider, IStartingBean
{
	@LogInstance
	private ILogger log;

	private static final IObjRef[][] EMPTY_RELATIONS_ARRAY = ObjRef.EMPTY_ARRAY_ARRAY;

	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

	@SuppressWarnings("unchecked")
	private static final IList<IObjRef>[] EMPTY_LIST_ARRAY = new IList[0];

	@Autowired
	protected ICompositeIdFactory compositeIdFactory;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IDatabase database;

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IEntityLoaderParallelInvoker entityLoaderParallelInvoker;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IObjRefFactory objRefFactory;

	@Autowired
	protected IPersistenceHelper persistenceHelper;

	@Autowired
	protected IQueryBuilderFactory queryBuilderFactory;

	@Autowired(optional = true)
	protected IInterningFeature interningFeature;

	protected boolean doInternId = true;

	protected boolean doInternVersion = true;

	private boolean supportsValueHolderContainer;

	@Override
	public void afterStarted() throws Throwable
	{
		supportsValueHolderContainer = entityFactory.supportsEnhancement(IObjRefContainer.class);
	}

	@Override
	public void assignInstances(List<IObjRef> orisToLoad, List<ILoadContainer> targetEntities)
	{
		IConversionHelper conversionHelper = this.conversionHelper;
		IDatabase database = this.database.getCurrent();
		IAlreadyLoadedCache alCache = database.getContextProvider().getAlreadyLoadedCache();
		LinkedHashMap<Class<?>, Collection<Object>[]> typeToPendingInit = new LinkedHashMap<Class<?>, Collection<Object>[]>();
		LinkedHashMap<Class<?>, Collection<Object>[]> cascadeTypeToPendingInit = new LinkedHashMap<Class<?>, Collection<Object>[]>();
		IdentityLinkedSet<ILoadContainer> loadContainerSet = IdentityLinkedSet.<ILoadContainer> create(orisToLoad.size());
		try
		{
			for (int a = orisToLoad.size(); a-- > 0;)
			{
				IObjRef oriToLoad = orisToLoad.get(a);
				Class<?> type = oriToLoad.getRealType();
				byte idIndex = oriToLoad.getIdNameIndex();

				ITable table = database.getTableByType(type);
				Class<?> persistentIdType = table.getIdFieldByAlternateIdIndex(idIndex).getFieldType();
				Object persistentId = conversionHelper.convertValueToType(persistentIdType, oriToLoad.getId());
				Collection<Object> pendingInit = getEnsurePendingInit(table, typeToPendingInit, idIndex);
				pendingInit.add(persistentId);
			}
			initInstances(typeToPendingInit, cascadeTypeToPendingInit, LoadMode.REFERENCE_ONLY);
			while (0 < cascadeTypeToPendingInit.size())
			{
				typeToPendingInit.clear();
				LinkedHashMap<Class<?>, Collection<Object>[]> switchVariable = typeToPendingInit;
				typeToPendingInit = cascadeTypeToPendingInit;
				cascadeTypeToPendingInit = switchVariable;
				initInstances(typeToPendingInit, cascadeTypeToPendingInit, LoadMode.VERSION_ONLY);
			}
			for (int a = orisToLoad.size(); a-- > 0;)
			{
				IObjRef oriToLoad = orisToLoad.get(a);

				ITable table = database.getTableByType(oriToLoad.getRealType());
				byte idIndex = oriToLoad.getIdNameIndex();
				Class<?> persistentIdType = table.getIdFieldByAlternateIdIndex(idIndex).getFieldType();
				Object persistentId = conversionHelper.convertValueToType(persistentIdType, oriToLoad.getId());

				ILoadContainer loadContainer = alCache.getObject(idIndex, persistentId, table.getEntityType());
				if (loadContainer == null)
				{
					continue;
				}
				if (table.getVersionField() != null)
				{
					if (loadContainer.getReference().getVersion() == null)
					{
						// Entity has not been correctly initialized in
						// InitInstances...
						continue;
					}
				}
				loadContainerSet.add(loadContainer);
			}
			for (ILoadContainer loadContainer : loadContainerSet)
			{
				if (!supportsLazyBehavior())
				{
					IObjRef[][] relations = loadContainer.getRelations();
					for (int i = relations.length; i-- > 0;)
					{
						if (relations[i] == null)
						{
							IObjRef ori = loadContainer.getReference();
							IEntityMetaData metaData = entityMetaDataProvider.getMetaData(ori.getRealType());
							String memberName = metaData.getRelationMembers()[i].getName();
							String msg = "Null load value for member '" + memberName + "' of " + ori + " not allowed. Mapping ok?";
							throw new IllegalStateException(msg);
						}
					}
				}
				targetEntities.add(loadContainer);
			}
		}
		finally
		{
			cascadeTypeToPendingInit.clear();
			typeToPendingInit.clear();
		}
	}

	@Override
	public void assignRelations(List<IObjRelation> orelsToLoad, List<IObjRelationResult> targetRelations)
	{
		IConversionHelper conversionHelper = this.conversionHelper;
		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		IDatabase database = this.database.getCurrent();

		ILinkedMap<ObjRelationType, IList<OrelLoadItem>> groupedObjRelations = bucketSortObjRelations(database, orelsToLoad);

		Iterator<Entry<ObjRelationType, IList<OrelLoadItem>>> iter = groupedObjRelations.iterator();
		while (iter.hasNext())
		{
			Entry<ObjRelationType, IList<OrelLoadItem>> entry = iter.next();
			ObjRelationType objRelType = entry.getKey();
			IList<OrelLoadItem> orelLoadItems = entry.getValue();
			iter.remove();

			Class<?> targetingRequestType = objRelType.getEntityType();
			byte idIndex = objRelType.getIdIndex();
			// Here all objRels in this list have ObjRefs of the same targeting requestType AND same targeting idIndex

			IEntityMetaData targetingRequestMetaData = entityMetaDataProvider.getMetaData(targetingRequestType);
			ITable targetingRequestTable = database.getTableByType(targetingRequestType);
			IDirectedLink targetingRequestLink = targetingRequestTable.getLinkByMemberName(objRelType.getMemberName());

			RelationMember relationMember = targetingRequestLink.getMember();
			Class<?> requestedType = relationMember.getElementType();
			IEntityMetaData requestedMetaData = entityMetaDataProvider.getMetaData(requestedType);
			requestedType = requestedMetaData.getEntityType();
			ITable requestedTable = database.getTableByType(requestedType);
			Member targetingIdMember = targetingRequestMetaData.getIdMemberByIdIndex(idIndex);

			ArrayList<Object> fromIds = new ArrayList<Object>();
			LinkedHashMap<Object, Object[]> targetingIdsMap = new LinkedHashMap<Object, Object[]>();

			for (int a = orelLoadItems.size(); a-- > 0;)
			{
				OrelLoadItem orelLoadItem = orelLoadItems.get(a);
				IObjRef objRef = orelLoadItem.getObjRef();
				// We only have to store the targeting ids because all objRefs in this batch share the same idIndex
				Object id = objRef.getId();
				fromIds.add(id);
				ObjRelationResult objRelResult = new ObjRelationResult();
				objRelResult.setReference(orelLoadItem.getObjRel());

				targetingIdsMap.put(id, new Object[] { objRelResult, null });
			}
			Class<?> idTypeOfTargetingObject = targetingIdMember.getRealType();
			ILinkCursor cursor = targetingRequestLink.findAllLinked(fromIds);
			try
			{
				byte toIdIndex = cursor.getToIdIndex();
				Class<?> idType = toIdIndex == ObjRef.PRIMARY_KEY_INDEX ? requestedTable.getIdField().getFieldType()
						: requestedTable.getAlternateIdFields()[toIdIndex].getFieldType();
				Member toIdMember = requestedMetaData.getIdMemberByIdIndex(toIdIndex);
				IPreparedObjRefFactory preparedObjRefFactory = objRefFactory.prepareObjRefFactory(requestedType, toIdIndex);
				Class<?> toIdTypeOfObject = toIdMember.getRealType();
				while (cursor.moveNext())
				{
					ILinkCursorItem item = cursor.getCurrent();

					Object fromId = conversionHelper.convertValueToType(idTypeOfTargetingObject, item.getFromId());
					Object toId = conversionHelper.convertValueToType(idType, item.getToId());
					toId = conversionHelper.convertValueToType(toIdTypeOfObject, toId);

					IObjRef targetObjRef = preparedObjRefFactory.createObjRef(toId, null);

					Object[] objects = targetingIdsMap.get(fromId);

					@SuppressWarnings("unchecked")
					IList<IObjRef> resultingObjRefs = (IList<IObjRef>) objects[1];
					if (resultingObjRefs == null)
					{
						resultingObjRefs = new ArrayList<IObjRef>();
						objects[1] = resultingObjRefs;
					}
					resultingObjRefs.add(targetObjRef);
				}
			}
			finally
			{
				cursor.dispose();
			}

			for (Entry<Object, Object[]> objectsEntry : targetingIdsMap)
			{
				Object[] objects = objectsEntry.getValue();
				ObjRelationResult objRelResult = (ObjRelationResult) objects[0];

				targetRelations.add(objRelResult);

				@SuppressWarnings("unchecked")
				IList<IObjRef> resultingObjRefs = (IList<IObjRef>) objects[1];
				if (resultingObjRefs == null)
				{
					objRelResult.setRelations(ObjRef.EMPTY_ARRAY);
					continue;
				}
				objRelResult.setRelations(resultingObjRefs.toArray(IObjRef.class));
			}
		}
	}

	protected ILinkedMap<ObjRelationType, IList<OrelLoadItem>> bucketSortObjRelations(IDatabase database, List<IObjRelation> orisToLoad)
	{
		ILinkedMap<ObjRelationType, IList<OrelLoadItem>> sortedIObjRefs = new LinkedHashMap<ObjRelationType, IList<OrelLoadItem>>();
		ILinkedMap<Class<?>, ILinkedMap<Member, IList<Object>>> typeToMissingOris = new LinkedHashMap<Class<?>, ILinkedMap<Member, IList<Object>>>();
		IMap<CacheKey, IList<IObjRef>> keyToEmptyOris = new HashMap<CacheKey, IList<IObjRef>>();

		for (int i = orisToLoad.size(); i-- > 0;)
		{
			IObjRelation orelToLoad = orisToLoad.get(i);
			IObjRef[] objRefItems = orelToLoad.getObjRefs();

			Class<?> targetingRequestType = orelToLoad.getRealType();
			ITable targetingRequestTable = database.getTableByType(targetingRequestType);
			IDirectedLink targetingRequestLink = targetingRequestTable.getLinkByMemberName(orelToLoad.getMemberName());

			byte idIndex = targetingRequestLink.getFromIdIndex();
			IObjRef objRef = idIndex + 1 < objRefItems.length ? objRefItems[idIndex + 1] : null;
			if (objRef == null || objRef.getIdNameIndex() != idIndex)
			{
				objRef = null;
				for (IObjRef objRefItem : objRefItems)
				{
					if (objRefItem.getIdNameIndex() == idIndex)
					{
						objRef = objRefItem;
						break;
					}
				}
			}
			if (objRef == null)
			{
				objRef = batchMissingORIs(typeToMissingOris, keyToEmptyOris, objRefItems, targetingRequestType, idIndex);
			}
			ObjRelationType objRelType = new ObjRelationType(objRef.getRealType(), objRef.getIdNameIndex(), orelToLoad.getMemberName());

			IList<OrelLoadItem> oreLoadItems = sortedIObjRefs.get(objRelType);
			if (oreLoadItems == null)
			{
				oreLoadItems = new ArrayList<OrelLoadItem>();
				sortedIObjRefs.put(objRelType, oreLoadItems);
			}
			oreLoadItems.add(new OrelLoadItem(objRef, orelToLoad));
		}

		if (!typeToMissingOris.isEmpty())
		{
			loadMissingORIs(typeToMissingOris, keyToEmptyOris);
		}

		return sortedIObjRefs;
	}

	protected IObjRef batchMissingORIs(ILinkedMap<Class<?>, ILinkedMap<Member, IList<Object>>> typeToMissingOris, IMap<CacheKey, IList<IObjRef>> keyToEmptyOri,
			IObjRef[] objRefItems, Class<?> targetingRequestType, byte idIndex)
	{
		// Batch first given ori to resolve the missing one
		IObjRef givenOri = objRefItems[0];
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(targetingRequestType);
		Member idMember = metaData.getIdMemberByIdIndex(givenOri.getIdNameIndex());

		ILinkedMap<Member, IList<Object>> givenMemberToValues = typeToMissingOris.get(targetingRequestType);
		if (givenMemberToValues == null)
		{
			givenMemberToValues = new LinkedHashMap<Member, IList<Object>>();
			typeToMissingOris.put(targetingRequestType, givenMemberToValues);
		}
		IList<Object> values = givenMemberToValues.get(idMember);
		if (values == null)
		{
			values = new ArrayList<Object>();
			givenMemberToValues.put(idMember, values);
		}
		values.add(givenOri.getId());

		IObjRef objRef = objRefFactory.createObjRef(targetingRequestType, idIndex, null, null);
		CacheKey cacheKey = new CacheKey();
		cacheKey.setEntityType(givenOri.getRealType());
		cacheKey.setIdNameIndex(givenOri.getIdNameIndex());
		cacheKey.setId(conversionHelper.convertValueToType(idMember.getRealType(), givenOri.getId()));
		IList<IObjRef> oris = keyToEmptyOri.get(cacheKey);
		if (oris == null)
		{
			oris = new ArrayList<IObjRef>();
			keyToEmptyOri.put(cacheKey, oris);
		}
		oris.add(objRef);

		return objRef;
	}

	protected void loadMissingORIs(ILinkedMap<Class<?>, ILinkedMap<Member, IList<Object>>> typeToMissingOris, IMap<CacheKey, IList<IObjRef>> keyToEmptyOris)
	{
		CacheKey lookupKey = new CacheKey();
		for (Entry<Class<?>, ILinkedMap<Member, IList<Object>>> entry : typeToMissingOris)
		{
			Class<?> entityType = entry.getKey();
			ILinkedMap<Member, IList<Object>> givenMemberToValues = entry.getValue();

			IQueryBuilder<?> qb = queryBuilderFactory.create(entityType);

			IOperator[] wheres = new IOperator[givenMemberToValues.size()];
			int index = 0;
			for (Entry<Member, IList<Object>> entry2 : givenMemberToValues)
			{
				Member idMember = entry2.getKey();
				IList<Object> values = entry2.getValue();
				IOperator inOperator = qb.isIn(qb.property(idMember.getName()), qb.value(values));
				wheres[index++] = inOperator;
			}

			IVersionCursor versionCursor = qb.build(qb.or(wheres)).retrieveAsVersions();
			try
			{
				IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
				Member idMember = metaData.getIdMember();
				Member[] alternateIdMembers = metaData.getAlternateIdMembers();
				lookupKey.setEntityType(entityType);
				while (versionCursor.moveNext())
				{
					IVersionItem item = versionCursor.getCurrent();

					Object[] ids = new Object[alternateIdMembers.length + 1];

					lookupKey.setIdNameIndex(ObjRef.PRIMARY_KEY_INDEX);
					lookupMissingORIs(keyToEmptyOris, lookupKey, idMember, alternateIdMembers, item, ids);
					for (byte lookupIdIndex = 0; lookupIdIndex < alternateIdMembers.length; lookupIdIndex++)
					{
						lookupKey.setIdNameIndex(lookupIdIndex);
						lookupMissingORIs(keyToEmptyOris, lookupKey, idMember, alternateIdMembers, item, ids);
					}
				}
			}
			finally
			{
				versionCursor.dispose();
			}
		}
	}

	protected void lookupMissingORIs(IMap<CacheKey, IList<IObjRef>> keyToEmptyOris, CacheKey lookupKey, Member idMember, Member[] alternateIdMembers,
			IVersionItem item, Object[] ids)
	{
		byte lookupIdIndex = lookupKey.getIdNameIndex();
		Member lookupIdMember;
		if (lookupIdIndex == ObjRef.PRIMARY_KEY_INDEX)
		{
			lookupIdMember = idMember;
		}
		else
		{
			lookupIdMember = alternateIdMembers[lookupIdIndex];
		}

		lookupKey.setId(conversionHelper.convertValueToType(lookupIdMember.getRealType(), item.getId(lookupIdIndex)));

		IList<IObjRef> emptyOris = keyToEmptyOris.get(lookupKey);
		if (emptyOris != null)
		{
			for (int i = emptyOris.size(); i-- > 0;)
			{
				IObjRef emptyOri = emptyOris.get(i);
				byte reqestedIdIndex = emptyOri.getIdNameIndex();
				int idArrayIndex = alternateIdMembers.length;
				Class<?> requestedIdType = idMember.getRealType();
				if (reqestedIdIndex != ObjRef.PRIMARY_KEY_INDEX)
				{
					idArrayIndex = reqestedIdIndex;
					requestedIdType = alternateIdMembers[reqestedIdIndex].getRealType();
				}
				Object id = ids[idArrayIndex];
				if (id == null)
				{
					id = conversionHelper.convertValueToType(requestedIdType, item.getId(reqestedIdIndex));
					ids[idArrayIndex] = id;
				}
				emptyOri.setId(id);
			}
		}
	}

	protected boolean supportsLazyBehavior()
	{
		// TODO: Set to true to test assignRelations()
		return true;
	}

	@Override
	public void fillVersion(List<IObjRef> orisWithoutVersion)
	{
		IDatabase database = this.database.getCurrent();
		IAlreadyLoadedCache alCache = database.getContextProvider().getAlreadyLoadedCache();
		if (0 < alCache.size())
		{
			throw new RuntimeException();
		}
		IConversionHelper conversionHelper = this.conversionHelper;
		ILinkedMap<Class<?>, Collection<Object>[]> typeToPendingInit = new LinkedHashMap<Class<?>, Collection<Object>[]>();
		for (int a = orisWithoutVersion.size(); a-- > 0;)
		{
			IObjRef ori = orisWithoutVersion.get(a);
			Class<?> type = ori.getRealType();
			byte idNameIndex = ori.getIdNameIndex();

			ITable table = database.getTableByType(type);
			Class<?> idType = table.getIdField().getFieldType();
			Object id = conversionHelper.convertValueToType(idType, ori.getId());
			// Flush version. It will be set later to the current valid
			// value. If version remains null at the end, the entity is not persisted (any more)
			ori.setVersion(null);
			Collection<Object> pendingInit = getEnsurePendingInit(table, typeToPendingInit, idNameIndex);
			pendingInit.add(id);

			alCache.add(idNameIndex, id, type, ori);
		}
		initInstances(typeToPendingInit, null, LoadMode.VERSION_ONLY);
	}

	protected Collection<Object> getEnsurePendingInit(ITable table, Map<Class<?>, Collection<Object>[]> typeToPendingInit, byte idNameIndex)
	{
		return getEnsurePendingInit(table.getEntityType(), table.getAlternateIdCount(), typeToPendingInit, idNameIndex);
	}

	protected Collection<Object> getEnsurePendingInit(IEntityMetaData metaData, Map<Class<?>, Collection<Object>[]> typeToPendingInit, byte idNameIndex)
	{
		return getEnsurePendingInit(metaData.getEntityType(), metaData.getAlternateIdCount(), typeToPendingInit, idNameIndex);
	}

	@SuppressWarnings("unchecked")
	protected Collection<Object> getEnsurePendingInit(Class<?> type, int alternateIdCount, Map<Class<?>, Collection<Object>[]> typeToPendingInit,
			byte idNameIndex)
	{
		Collection<Object>[] pendingInits = typeToPendingInit.get(type);
		if (pendingInits == null)
		{
			pendingInits = new Collection[alternateIdCount + 1];
			typeToPendingInit.put(type, pendingInits);
		}
		Collection<Object> pendingInit = pendingInits[idNameIndex + 1];
		if (pendingInit == null)
		{
			pendingInit = new HashSet<Object>();
			pendingInits[idNameIndex + 1] = pendingInit;
		}
		return pendingInit;
	}

	protected Object ensureInstance(IDatabase database, Class<?> type, byte idIndex, Object id, Map<Class<?>, Collection<Object>[]> typeToPendingInit,
			ITypeInfoItem keyMember, LoadMode loadMode)
	{
		IConversionHelper conversionHelper = this.conversionHelper;
		ITable table = database.getTableByType(type);
		IField idField = table.getIdFieldByAlternateIdIndex(idIndex);
		Class<?> idType = idField.getFieldType();
		Class<?> idTypeOfObject = idField.getMember().getElementType();
		Object persistentId = conversionHelper.convertValueToType(idType, id);
		Object objectId = conversionHelper.convertValueToType(idTypeOfObject, id);

		IAlreadyLoadedCache alreadyLoadedCache = database.getContextProvider().getAlreadyLoadedCache();
		if (LoadMode.VERSION_ONLY == loadMode)
		{
			IObjRef objRef = alreadyLoadedCache.getRef(idIndex, persistentId, type);
			if (objRef == null)
			{
				objRef = objRefFactory.createObjRef(type, idIndex, objectId, null);
				alreadyLoadedCache.add(idIndex, persistentId, type, objRef);
				Collection<Object> pendingInit = getEnsurePendingInit(table, typeToPendingInit, idIndex);
				pendingInit.add(persistentId);
			}
			return objRef;
		}
		else if (LoadMode.REFERENCE_ONLY == loadMode)
		{
			ILoadContainer result = alreadyLoadedCache.getObject(idIndex, persistentId, type);
			if (result == null)
			{
				LoadContainer loadContainer = new LoadContainer();

				IObjRef objRef = alreadyLoadedCache.getRef(idIndex, persistentId, type);
				if (objRef == null)
				{
					objRef = objRefFactory.createObjRef(type, idIndex, objectId, null);
					alreadyLoadedCache.add(idIndex, persistentId, type, objRef, loadContainer);
				}
				else
				{
					alreadyLoadedCache.replace(idIndex, persistentId, type, loadContainer);
				}
				loadContainer.setReference(objRef);
				result = loadContainer;
				Collection<Object> pendingInit = getEnsurePendingInit(table, typeToPendingInit, idIndex);
				pendingInit.add(persistentId);
			}
			return result;
		}
		else
		{
			throw new IllegalArgumentException("LoadMode " + loadMode + " not supported");
		}
	}

	protected void initInstances(ILinkedMap<Class<?>, Collection<Object>[]> typeToPendingInit,
			final LinkedHashMap<Class<?>, Collection<Object>[]> cascadeTypeToPendingInit, final LoadMode loadMode)
	{
		ArrayList<ParallelLoadItem> parallelPendingItems = new ArrayList<ParallelLoadItem>();
		IDatabase database = this.database.getCurrent();
		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		Iterator<Entry<Class<?>, Collection<Object>[]>> iter = typeToPendingInit.iterator();
		while (iter.hasNext())
		{
			Entry<Class<?>, Collection<Object>[]> entry = iter.next();

			final Class<?> type = entry.getKey();
			Collection<Object>[] pendingInits = entry.getValue();

			iter.remove();
			entry = null;

			ITable table = null;

			if (entityMetaDataProvider.getMetaData(type).isLocalEntity())
			{
				table = database.getTableByType(type);
			}
			for (int a = 0, size = pendingInits.length; a < size; a++)
			{
				final Collection<Object> pendingInit = pendingInits[a];
				if (pendingInit == null)
				{
					// for this type of id or alternate id is nothing requested
					continue;
				}
				pendingInits[a] = null;
				if (table == null)
				{
					continue;
				}
				ParallelLoadItem pli = new ParallelLoadItem(type, (byte) (a - 1), pendingInit, loadMode, cascadeTypeToPendingInit);
				parallelPendingItems.add(pli);
			}
		}
		if (parallelPendingItems.size() == 0)
		{
			return;
		}
		entityLoaderParallelInvoker.invokeAndWait(parallelPendingItems, new IBackgroundWorkerParamDelegate<ParallelLoadItem>()
		{

			@Override
			public void invoke(ParallelLoadItem state) throws Throwable
			{
				initInstances(state.entityType, state.idIndex, state.ids, state.cascadeTypeToPendingInit, state.loadMode);
			}
		});
	}

	public void writePendingInitToShared(LinkedHashMap<Class<?>, Collection<Object>[]> cascadeTypeToPendingInit,
			LinkedHashMap<Class<?>, Collection<Object>[]> sharedCascadeTypeToPendingInit)
	{
		IDatabase database = this.database.getCurrent();
		for (Entry<Class<?>, Collection<Object>[]> entry : cascadeTypeToPendingInit)
		{
			Class<?> type = entry.getKey();
			Collection<Object>[] pendingInits = entry.getValue();
			for (int a = pendingInits.length; a-- > 0;)
			{
				Collection<Object> pendingInit = pendingInits[a];
				if (pendingInit == null)
				{
					continue;
				}
				ITable table = database.getTableByType(type);
				Collection<Object> sharedPendingInit = getEnsurePendingInit(table, sharedCascadeTypeToPendingInit, (byte) (a - 1));
				sharedPendingInit.addAll(pendingInit);
			}
		}
	}

	public void initInstances(Class<?> entityType, byte idIndex, Collection<Object> ids,
			LinkedHashMap<Class<?>, Collection<Object>[]> cascadeTypeToPendingInit, LoadMode loadMode)
	{
		if (LoadMode.VERSION_ONLY == loadMode)
		{
			loadVersionMode(entityType, idIndex, ids);
		}
		else if (LoadMode.REFERENCE_ONLY == loadMode || LoadMode.DEFAULT == loadMode)
		{
			loadDefault(entityType, idIndex, ids, cascadeTypeToPendingInit);
		}
		else
		{
			throw new IllegalArgumentException("LoadMode " + loadMode + " not supported");
		}

	}

	protected void loadVersionMode(Class<?> entityType, byte idIndex, Collection<Object> ids)
	{
		ArrayList<Object> realNeededIds = new ArrayList<Object>(ids.size());
		IDatabase database = this.database.getCurrent();
		IAlreadyLoadedCache alreadyLoadedCache = database.getContextProvider().getAlreadyLoadedCache();

		for (Object id : ids)
		{
			IObjRef ori = alreadyLoadedCache.getRef(idIndex, id, entityType);
			if (ori == null)
			{
				ori = objRefFactory.createObjRef(entityType, idIndex, id, null);
				alreadyLoadedCache.add(idIndex, id, entityType, ori);
			}
			if (ori.getVersion() == null)
			{
				realNeededIds.add(id);
			}
		}
		if (realNeededIds.isEmpty())
		{
			return;
		}
		IConversionHelper conversionHelper = this.conversionHelper;
		ITable table = database.getTableByType(entityType);
		IField givenIdField = table.getIdFieldByAlternateIdIndex(idIndex);
		String givenIdMemberName = givenIdField.getMember().getName();
		Class<?> givenIdType = givenIdField.getFieldType();
		Class<?> versionTypeOfObject = table.getVersionField().getMember().getElementType();
		IVersionCursor cursor = null;
		try
		{
			cursor = table.selectVersion(givenIdMemberName, realNeededIds);
			while (cursor.moveNext())
			{
				IVersionItem item = cursor.getCurrent();
				Object id = conversionHelper.convertValueToType(givenIdType, item.getId(idIndex));
				Object version = conversionHelper.convertValueToType(versionTypeOfObject, item.getVersion());

				IObjRef ori = alreadyLoadedCache.getRef(idIndex, id, entityType);

				// Set version number to ORI explicitly here. It is not
				// known earlier...
				ori.setVersion(version);
				// There is nothing to load further if the cache only
				// contains the ORI
			}
		}
		finally
		{
			if (cursor != null)
			{
				cursor.dispose();
				cursor = null;
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void loadDefault(Class<?> entityType, byte idIndex, Collection<Object> ids, LinkedHashMap<Class<?>, Collection<Object>[]> cascadeTypeToPendingInit)
	{
		IDatabase database = this.database.getCurrent();
		IAlreadyLoadedCache alreadyLoadedCache = database.getContextProvider().getAlreadyLoadedCache();
		IConversionHelper conversionHelper = this.conversionHelper;
		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		IInterningFeature interningFeature = this.interningFeature;
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
		ITable table = database.getTableByType(entityType);

		IDirectedLink[] standaloneDirectedLinks = new IDirectedLink[metaData.getRelationMembers().length];
		IDirectedLink[] directedLinks = new IDirectedLink[standaloneDirectedLinks.length];
		ArrayList<Object>[] directedLinkQueues = new ArrayList[standaloneDirectedLinks.length];
		IdentityHashMap<IField, Integer> fieldToDirectedLinkIndex = new IdentityHashMap<IField, Integer>();
		ArrayList<Object> idList = new ArrayList<Object>(ids);
		String idFieldMemberName = table.getIdFieldByAlternateIdIndex(idIndex).getMember().getName();
		Class<?> primIdType = table.getIdField().getFieldType();
		IField versionField = table.getVersionField();
		Class<?> versionTypeOfObject = versionField != null ? versionField.getMember().getElementType() : null;

		int typesRelatingToThisCount = metaData.getTypesRelatingToThis().length;

		int cursorCount = 0;
		ICursor cursor = null;
		try
		{
			cursor = table.selectValues(idFieldMemberName, idList);

			IField[] cursorFields = cursor.getFields();
			int[] cursorFieldToPrimitiveIndex = new int[cursorFields.length];
			int[] memberCounts = createMappingIndexes(cursor, cursorFieldToPrimitiveIndex, table, standaloneDirectedLinks, directedLinks,
					fieldToDirectedLinkIndex);

			for (int a = standaloneDirectedLinks.length; a-- > 0;)
			{
				if (standaloneDirectedLinks != null)
				{
					directedLinkQueues[a] = new ArrayList<Object>();
				}
			}
			int alternateIdCount = metaData.getAlternateIdCount();
			Object[] alternateIds = alternateIdCount > 0 ? new Object[alternateIdCount] : EMPTY_OBJECT_ARRAY;

			boolean doInternId = this.doInternId;
			boolean doInternVersion = this.doInternVersion;
			while (cursor.moveNext())
			{
				ICursorItem item = cursor.getCurrent();
				cursorCount++;

				Object id = conversionHelper.convertValueToType(primIdType, item.getId());
				Object version = versionField != null ? conversionHelper.convertValueToType(versionTypeOfObject, item.getVersion()) : null;

				if (id == null || versionField != null && version == null)
				{
					throw new IllegalStateException("Retrieved row with either null-id or null-version from table '" + table.getName()
							+ "'. This is a fatal database state");
				}
				if (interningFeature != null)
				{
					if (typesRelatingToThisCount > 0 && doInternId)
					{
						// If other entities may relate to this one, it makes sense to intern the id
						id = interningFeature.intern(id);
					}
					if (version != null && doInternVersion)
					{
						version = interningFeature.intern(version);
					}
				}
				Object[] primitives = new Object[memberCounts[0]];

				Object[] cursorValues = item.getValues();
				for (int a = cursorFields.length; a-- > 0;)
				{
					Object dbValue = cursorValues[a];
					if (dbValue == null)
					{
						continue;
					}
					int primitiveIndex = cursorFieldToPrimitiveIndex[a];
					if (primitiveIndex == -1)
					{
						continue;
					}
					IField field = cursorFields[a];
					Integer dirLinkIndex = fieldToDirectedLinkIndex.get(field);

					if (dirLinkIndex == null || field.isAlternateId())
					{
						Member fieldMember = field.getMember();
						Class<?> expectedType = fieldMember.getRealType();
						if (java.util.Date.class.isAssignableFrom(expectedType) || java.util.Calendar.class.isAssignableFrom(expectedType))
						{
							// store Date-instances only with their long-value for decreased heap consumption
							expectedType = long.class;
						}
						Object primitiveValue;
						if (field.getFieldSubType() != null && (Collection.class.isAssignableFrom(expectedType) || expectedType.isArray()))
						{
							Class<?> elementType = fieldMember.getElementType();
							primitiveValue = conversionHelper.convertValueToType(expectedType, dbValue, elementType);
						}
						else
						{
							// The column is only a primitive field
							primitiveValue = conversionHelper.convertValueToType(expectedType, dbValue);
						}
						if (interningFeature != null && (metaData.hasInterningBehavior(fieldMember) || metaData.isAlternateId(fieldMember)))
						{
							primitiveValue = interningFeature.intern(primitiveValue);
						}
						primitives[primitiveIndex] = primitiveValue;
					}
				}
				for (int alternateIdIndex = alternateIds.length; alternateIdIndex-- > 0;)
				{
					alternateIds[alternateIdIndex] = compositeIdFactory.createIdFromPrimitives(metaData, alternateIdIndex, primitives);
				}
				for (int a = standaloneDirectedLinks.length; a-- > 0;)
				{
					IDirectedLink link = standaloneDirectedLinks[a];
					if (link == null)
					{
						continue;
					}
					ArrayList<Object> directedLinkQueue = directedLinkQueues[a];
					byte linkIdIndex = link.getFromField().getIdIndex();
					if (linkIdIndex == ObjRef.PRIMARY_KEY_INDEX)
					{
						directedLinkQueue.add(id);
					}
					else
					{
						Object alternateId = alternateIds[linkIdIndex];
						if (alternateId != null)
						{
							directedLinkQueue.add(alternateId);
						}
					}
				}

				LoadContainer loadContainer = unionLoadContainers(table, id, version, alternateIds, alreadyLoadedCache);

				loadContainer.setPrimitives(primitives);

				IList<IObjRef>[] relationBuilds;
				IObjRef[][] relations;
				if (memberCounts[1] != 0)
				{
					relationBuilds = new IList[memberCounts[1]];
					relations = new IObjRef[memberCounts[1]][];
				}
				else
				{
					relationBuilds = EMPTY_LIST_ARRAY;
					relations = EMPTY_RELATIONS_ARRAY;
				}
				loadContainer.setRelationBuilds(relationBuilds);
				loadContainer.setRelations(relations);

				// Set version number to ORI explicitly here. It is not known earlier...
				loadContainer.getReference().setVersion(version);

				if (fieldToDirectedLinkIndex.size() > 0)
				{
					for (int a = cursorFields.length; a-- > 0;)
					{
						Object dbValue = cursorValues[a];
						IField field = cursorFields[a];

						Integer dirLinkIndex = fieldToDirectedLinkIndex.get(field);
						if (dirLinkIndex == null)
						{
							continue;
						}
						if (dbValue == null)
						{
							relations[dirLinkIndex.intValue()] = ObjRef.EMPTY_ARRAY;
							continue;
						}
						IDirectedLink columnBasedDirectedLink = directedLinks[dirLinkIndex.intValue()];
						IField toField = columnBasedDirectedLink.getToField();
						Class<?> targetType;
						if (toField != null)
						{
							targetType = toField.getFieldType();
						}
						else
						{
							targetType = columnBasedDirectedLink.getToMember().getRealType();
						}
						dbValue = conversionHelper.convertValueToType(targetType, dbValue);
						if (interningFeature != null && doInternId)
						{
							dbValue = interningFeature.intern(dbValue);
						}
						Class<?> toEntityType = columnBasedDirectedLink.getToEntityType();
						Member toMember = columnBasedDirectedLink.getToMember();
						IEntityMetaData toEntityMetaData = entityMetaDataProvider.getMetaData(toEntityType);
						byte toIdIndex = toEntityMetaData.getIdIndexByMemberName(toMember.getName());

						IObjRef toOri = alreadyLoadedCache.getRef(toIdIndex, dbValue, toEntityType);
						if (toOri == null)
						{
							Class<?> expectedType = toMember.getRealType();

							Object idOfObject = conversionHelper.convertValueToType(expectedType, dbValue);
							toOri = objRefFactory.createObjRef(toEntityType, toIdIndex, idOfObject, null);
							alreadyLoadedCache.add(toIdIndex, dbValue, toEntityType, toOri);
						}
						relations[dirLinkIndex.intValue()] = new IObjRef[] { toOri };
						Collection<Object> cascadePendingInit = getEnsurePendingInit(toEntityMetaData, cascadeTypeToPendingInit, toIdIndex);
						cascadePendingInit.add(dbValue);
					}
				}
			}
		}
		finally
		{
			if (cursor != null)
			{
				cursor.dispose();
				cursor = null;
			}
		}
		if (log.isDebugEnabled())
		{
			log.debug("Retrieved " + cursorCount + " row(s)");
		}
		ArrayList<ParallelLoadCascadeItem> parallelLinkItems = new ArrayList<ParallelLoadCascadeItem>(standaloneDirectedLinks.length);

		for (int relationIndex = standaloneDirectedLinks.length; relationIndex-- > 0;)
		{
			IDirectedLink link = standaloneDirectedLinks[relationIndex];
			if (link == null)
			{
				continue;
			}
			switch (link.getCascadeLoadMode())
			{
				case LAZY:
					if (supportsValueHolderContainer)
					{
						continue;
					}
					if (supportsLazyBehavior())
					{
						RelationMember member = link.getMember();
						if (!member.getRealType().equals(member.getElementType()))
						{
							continue;
						}
						// To-One-relations may NOT be lazy, because otherwise we would have a non-null valueholder
						// objects consisting of a null-relation
						// after initialization
					}
					// If we have no reverse mapped member it is currently impossible on a later request to resolve the
					// related members
					// So we still have to eager fetch them
					// Fall through intended
				case EAGER_VERSION:
				case EAGER:
				{
					ArrayList<Object> directedLinkQueue = directedLinkQueues[relationIndex];

					ParallelLoadCascadeItem pli = new ParallelLoadCascadeItem(entityType, link, directedLinkQueue, relationIndex, cascadeTypeToPendingInit);
					parallelLinkItems.add(pli);
					continue;
				}
				default:
					throw new IllegalStateException("Enum " + link.getCascadeLoadMode() + " not supported");
			}
		}
		if (parallelLinkItems.size() > 0)
		{
			entityLoaderParallelInvoker.invokeAndWait(parallelLinkItems, new IBackgroundWorkerParamDelegate<ParallelLoadCascadeItem>()
			{

				@Override
				public void invoke(ParallelLoadCascadeItem state) throws Throwable
				{
					cascadeLoadEagerVersion(state.entityType, state.link, state.splittedIds, state.relationIndex, state.cascadeTypeToPendingInit);
				}
			});
		}
		for (int index = idList.size(); index-- > 0;)
		{
			Object splittedId = idList.get(index);
			LoadContainer loadContainer = (LoadContainer) alreadyLoadedCache.getObject(idIndex, splittedId, entityType);
			if (loadContainer == null)
			{
				// Object with requested PK has not been found in
				// database, so it also has no version at this point
				continue;
			}
			Object version = loadContainer.getReference().getVersion();
			if (version == null && versionField != null)
			{
				throw new IllegalStateException();
				// Object with requested PK has not been found in
				// database, so it also has no version at this point
				// continue;
			}
			List<IObjRef>[] relationBuilds = loadContainer.getRelationBuilds();
			loadContainer.setRelationBuilds(null);
			IObjRef[][] relations = loadContainer.getRelations();
			if (relations == null)
			{
				if (relationBuilds.length == 0)
				{
					relations = EMPTY_RELATIONS_ARRAY;
				}
				else
				{
					relations = new IObjRef[relationBuilds.length][];
				}
				loadContainer.setRelations(relations);
			}
			for (int a = relationBuilds.length; a-- > 0;)
			{
				if (relations[a] != null)
				{
					// Relations already initialized. This is possible with column-based links handled earlier
					continue;
				}
				List<IObjRef> relationBuild = relationBuilds[a];
				if (relationBuild == null)
				{
					// Relation has not been initialized at all, this will result in a 'real lazy' value holder if
					// supported
					continue;
				}
				int size = relationBuild.size();
				IObjRef[] relationArray;
				if (size > 0)
				{
					relationArray = new IObjRef[size];
					for (int buildIndex = size; buildIndex-- > 0;)
					{
						relationArray[buildIndex] = relationBuild.get(buildIndex);
					}
				}
				else
				{
					relationArray = ObjRef.EMPTY_ARRAY;
				}
				relations[a] = relationArray;
			}
		}
	}

	protected int[] createMappingIndexes(ICursor cursor, int[] cursorFieldToPrimitiveIndex, ITable table, IDirectedLink[] standaloneDirectedLinks,
			IDirectedLink[] directedLinks, IMap<IField, Integer> fieldToDirectedLinkIndex)
	{
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(table.getEntityType());
		Arrays.fill(cursorFieldToPrimitiveIndex, -1);
		IField[] cursorFields = cursor.getFields();
		RelationMember[] relationMembers = metaData.getRelationMembers();
		int[] memberCounts = { 0, 0 };

		Member[] primitiveMembers = metaData.getPrimitiveMembers();

		int nextIndex = 0;
		for (int a = 0, size = primitiveMembers.length; a < size; a++)
		{
			Member primitiveMember = primitiveMembers[a];

			IField field = table.getFieldByMemberName(primitiveMember.getName());

			if (field == null)
			{
				if (log.isWarnEnabled())
				{
					log.warn("Member '" + metaData.getEntityType().getName() + "." + primitiveMember.getName() + "' is neither mapped to a field of table "
							+ table.getName() + " nor marked as transient");
				}
				continue;
			}
			IField mappedField = cursor.getFieldByMemberName(primitiveMember.getName());

			if (mappedField == null)
			{
				continue;
			}
			memberCounts[0]++;
			for (int b = cursorFields.length; b-- > 0;)
			{
				IField cursorField = cursorFields[b];
				if (cursorField.equals(mappedField))
				{
					cursorFieldToPrimitiveIndex[b] = nextIndex++;
					break;
				}
			}
		}

		for (int a = relationMembers.length; a-- > 0;)
		{
			RelationMember relationMember = relationMembers[a];
			String memberName = relationMember.getName();

			IDirectedLink directedLink = table.getLinkByMemberName(memberName);

			if (directedLink == null)
			{
				if (log.isWarnEnabled())
				{
					log.warn("Member '" + table.getEntityType().getName() + "." + memberName + "' is not mappable to a link");
				}
				continue;
			}
			memberCounts[1]++;

			if (directedLink.isStandaloneLink())
			{
				standaloneDirectedLinks[a] = directedLink;
			}
			else
			{
				directedLinks[a] = directedLink;
				fieldToDirectedLinkIndex.put(directedLink.getFromField(), Integer.valueOf(a));
			}
		}

		return memberCounts;
	}

	protected void cascadeLoadEagerVersion(Class<?> type, IDirectedLink link, ArrayList<Object> splittedIds, int relationIndex,
			Map<Class<?>, Collection<Object>[]> cascadeTypeToPendingInit)
	{
		IDatabase database = this.database.getCurrent();
		IAlreadyLoadedCache alreadyLoadedCache = database.getContextProvider().getAlreadyLoadedCache();
		Class<?> linkedEntityType = link.getToEntityType();
		IEntityMetaData linkedEntityMetaData = entityMetaDataProvider.getMetaData(linkedEntityType);

		IField fromField = link.getFromField();
		byte fromIdIndex = fromField.getIdIndex();
		Class<?> fromIdType = fromField.getFieldType();

		// entityMetaDataProvider.getMetaData(link.getFromEntityType()).getIdMemberByIdIndex(idIndex)
		// linkedEntityMetaData.getIdIndexByMemberName(memberName)

		byte toIdIndex;
		Class<?> toIdType;
		if (linkedEntityMetaData.isLocalEntity())
		{
			IField toField = link.getToField();

			toIdIndex = toField.getIdIndex();
			toIdType = toField.getFieldType();
		}
		else
		{
			Member toMember = link.getToMember();
			toIdIndex = linkedEntityMetaData.getIdIndexByMemberName(toMember.getName());
			toIdType = toMember.getRealType();
		}

		ILinkCursor linkCursor = null;
		try
		{
			for (int a = splittedIds.size(); a-- > 0;)
			{
				Object id = splittedIds.get(a);
				LoadContainer myLoadContainer = (LoadContainer) alreadyLoadedCache.getObject(fromIdIndex, id, type);

				List<IObjRef>[] relationBuilds = myLoadContainer.getRelationBuilds();
				List<IObjRef> objRefs = relationBuilds[relationIndex];
				if (objRefs == null)
				{
					objRefs = new ArrayList<IObjRef>();
					relationBuilds[relationIndex] = objRefs;
				}
			}
			linkCursor = link.findAllLinked(splittedIds);
			if (linkCursor == null)
			{
				return;
			}
			IPreparedObjRefFactory preparedObjRefFactory = objRefFactory.prepareObjRefFactory(linkedEntityType, toIdIndex);
			IConversionHelper conversionHelper = this.conversionHelper;
			IInterningFeature interningFeature = this.interningFeature;
			while (linkCursor.moveNext())
			{
				ILinkCursorItem item = linkCursor.getCurrent();

				Object fromId = conversionHelper.convertValueToType(fromIdType, item.getFromId());
				Object toId = conversionHelper.convertValueToType(toIdType, item.getToId());

				LoadContainer loadContainer = (LoadContainer) alreadyLoadedCache.getObject(fromIdIndex, fromId, type);

				if (interningFeature != null)
				{
					toId = interningFeature.intern(toId);
				}
				IObjRef objRef;
				if (linkedEntityMetaData.isLocalEntity())
				{
					objRef = (IObjRef) ensureInstance(database, linkedEntityType, toIdIndex, toId, cascadeTypeToPendingInit, null, LoadMode.VERSION_ONLY);
				}
				else
				{
					objRef = preparedObjRefFactory.createObjRef(toId, null);
				}

				List<IObjRef>[] relationBuilds = loadContainer.getRelationBuilds();
				List<IObjRef> objRefs = relationBuilds[relationIndex];
				objRefs.add(objRef);
			}
		}
		finally
		{
			if (linkCursor != null)
			{
				linkCursor.dispose();
				linkCursor = null;
			}
		}
	}

	protected LoadContainer unionLoadContainers(ITable table, Object id, Object version, Object[] alternateIds, IAlreadyLoadedCache alreadyLoadedCache)
	{
		Class<?> type = table.getEntityType();
		IdTypeTuple key = new IdTypeTuple(type, ObjRef.PRIMARY_KEY_INDEX, id);
		LoadContainer loadContainer = (LoadContainer) alreadyLoadedCache.getObject(key);
		if (loadContainer == null)
		{
			loadContainer = new LoadContainer();

			IObjRef primaryIdObjRef = alreadyLoadedCache.getRef(key);
			if (primaryIdObjRef == null)
			{
				Class<?> idTypeOfObject = table.getIdField().getMember().getRealType();
				Object idOfObject = conversionHelper.convertValueToType(idTypeOfObject, id);
				primaryIdObjRef = objRefFactory.createObjRef(type, ObjRef.PRIMARY_KEY_INDEX, idOfObject, version);
			}
			loadContainer.setReference(primaryIdObjRef);
			alreadyLoadedCache.add(key, primaryIdObjRef, loadContainer);
		}
		for (int idNameIndex = alternateIds.length; idNameIndex-- > 0;)
		{
			alreadyLoadedCache.replace((byte) idNameIndex, alternateIds[idNameIndex], type, loadContainer);
		}
		return loadContainer;
	}
}
