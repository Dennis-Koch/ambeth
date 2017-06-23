package com.koch.ambeth.persistence;

/*-
 * #%L
 * jambeth-persistence
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.koch.ambeth.cache.CacheKey;
import com.koch.ambeth.cache.collections.ICacheMapEntryTypeProvider;
import com.koch.ambeth.cache.transfer.LoadContainer;
import com.koch.ambeth.cache.transfer.ObjRelationResult;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.threadlocal.Forkable;
import com.koch.ambeth.ioc.threadlocal.IForkProcessor;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupBean;
import com.koch.ambeth.ioc.util.IAggregrateResultHandler;
import com.koch.ambeth.ioc.util.IMultithreadingHelper;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.ILoggerHistory;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.compositeid.ICompositeIdFactory;
import com.koch.ambeth.merge.metadata.IObjRefFactory;
import com.koch.ambeth.merge.metadata.IPreparedObjRefFactory;
import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.persistence.api.ICursor;
import com.koch.ambeth.persistence.api.ICursorItem;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.IDatabaseMetaData;
import com.koch.ambeth.persistence.api.IDirectedLink;
import com.koch.ambeth.persistence.api.IDirectedLinkMetaData;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.ILinkCursor;
import com.koch.ambeth.persistence.api.ILinkCursorItem;
import com.koch.ambeth.persistence.api.ITable;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.persistence.parallel.ParallelLoadItem;
import com.koch.ambeth.query.IOperator;
import com.koch.ambeth.query.IQueryBuilder;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.query.persistence.IVersionItem;
import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.service.metadata.PrimitiveMember;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.IInterningFeature;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.IdentityHashMap;
import com.koch.ambeth.util.collections.IdentityLinkedSet;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.collections.Tuple3KeyEntry;
import com.koch.ambeth.util.collections.Tuple3KeyHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerParamDelegate;

public class EntityLoader
		implements
			IEntityLoader,
			ILoadContainerProvider,
			IStartingBean,
			IThreadLocalCleanupBean {
	public static class EntityLoaderForkProcessor implements IForkProcessor {
		@Autowired
		protected ICacheMapEntryTypeProvider cacheMapEntryTypeProvider;

		@Override
		public Object resolveOriginalValue(Object bean, String fieldName, ThreadLocal<?> fieldValueTL) {
			return ((EntityLoader) bean).getOrAcquireMaps(-1);
		}

		@Override
		public Object createForkedValue(Object value) {
			return new Maps(-1);
		}

		@Override
		public void returnForkedValue(Object value, Object forkedValue) {
			Maps baseValue = (Maps) value;
			Maps fork = (Maps) forkedValue;

			for (Tuple3KeyEntry<Class<?>, Integer, Object, ILoadContainer> entry : fork.loadContainerMap) {
				baseValue.loadContainerMap.put(entry.getKey1(), entry.getKey2(), entry.getKey3(),
						entry.getValue());
			}
		}
	}

	public static class Maps {
		public final Tuple3KeyHashMap<Class<?>, Integer, Object, ILoadContainer> loadContainerMap;

		public final Tuple3KeyHashMap<Class<?>, Integer, Object, IObjRef> objRefMap;

		public Maps(int sizeHint) {
			loadContainerMap = sizeHint > 0
					? new Tuple3KeyHashMap<Class<?>, Integer, Object, ILoadContainer>(
							(int) (sizeHint / 0.75f) + 1, 0.75f)
					: new Tuple3KeyHashMap<Class<?>, Integer, Object, ILoadContainer>();
			objRefMap =
					sizeHint > 0
							? new Tuple3KeyHashMap<Class<?>, Integer, Object, IObjRef>(
									(int) (sizeHint / 0.75f) + 1, 0.75f)
							: new Tuple3KeyHashMap<Class<?>, Integer, Object, IObjRef>();
		}
	}

	@LogInstance
	private ILogger log;

	private static final IObjRef[][] EMPTY_RELATIONS_ARRAY = ObjRef.EMPTY_ARRAY_ARRAY;

	private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

	@SuppressWarnings("unchecked")
	private static final IList<IObjRef>[] EMPTY_LIST_ARRAY = new IList[0];

	@Autowired
	protected IServiceContext beanContext;

	@Autowired
	protected ICacheMapEntryTypeProvider cacheMapEntryTypeProvider;

	@Autowired
	protected ICompositeIdFactory compositeIdFactory;

	@Autowired
	protected IConnectionDialect connectionDialect;

	@Autowired
	protected IConversionHelper conversionHelper;

	@Autowired
	protected IDatabase database;

	@Autowired
	protected IDatabaseMetaData databaseMetaData;

	@Autowired
	protected IEntityFactory entityFactory;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected ILoggerHistory loggerHistory;

	@Autowired
	protected IMultithreadingHelper multithreadingHelper;

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

	@Forkable(processor = EntityLoaderForkProcessor.class)
	protected final ThreadLocal<Maps> loadContainerMapTL = new ThreadLocal<>();

	@Override
	public void afterStarted() throws Throwable {
		supportsValueHolderContainer = entityFactory.supportsEnhancement(IObjRefContainer.class);
	}

	@Override
	public void cleanupThreadLocal() {
		disposeMaps(null);
	}

	protected void acquireMaps(int sizeHint) {
		loadContainerMapTL.set(new Maps(sizeHint));
	}

	protected Maps getOrAcquireMaps(int sizeHint) {
		Maps maps = loadContainerMapTL.get();
		if (maps != null) {
			return maps;
		}
		acquireMaps(sizeHint);
		return loadContainerMapTL.get();
	}

	protected Tuple3KeyHashMap<Class<?>, Integer, Object, ILoadContainer> getLoadContainerMap() {
		return loadContainerMapTL.get().loadContainerMap;
	}

	protected Tuple3KeyHashMap<Class<?>, Integer, Object, IObjRef> getObjRefMap() {
		return loadContainerMapTL.get().objRefMap;
	}

	protected void disposeMaps(Maps oldMaps) {
		loadContainerMapTL.set(oldMaps);
	}

	@Override
	public void assignInstances(List<IObjRef> orisToLoad, List<ILoadContainer> targetEntities) {
		IConversionHelper conversionHelper = this.conversionHelper;
		IDatabaseMetaData databaseMetaData = this.databaseMetaData;
		LinkedHashMap<Class<?>, Collection<Object>[]> typeToPendingInit = new LinkedHashMap<>();
		LinkedHashMap<Class<?>, Collection<Object>[]> cascadeTypeToPendingInit = new LinkedHashMap<>();
		IdentityLinkedSet<ILoadContainer> loadContainerSet =
				IdentityLinkedSet.<ILoadContainer>create(orisToLoad.size());
		Maps oldMaps = loadContainerMapTL.get();
		loadContainerMapTL.set(null);
		try {
			acquireMaps(orisToLoad.size());
			Tuple3KeyHashMap<Class<?>, Integer, Object, ILoadContainer> loadContainerMap =
					getLoadContainerMap();
			for (int a = orisToLoad.size(); a-- > 0;) {
				IObjRef oriToLoad = orisToLoad.get(a);
				Class<?> type = oriToLoad.getRealType();
				byte idIndex = oriToLoad.getIdNameIndex();

				ITableMetaData table = databaseMetaData.getTableByType(type);
				Class<?> persistentIdType = table.getIdFieldByAlternateIdIndex(idIndex).getFieldType();
				Object persistentId =
						conversionHelper.convertValueToType(persistentIdType, oriToLoad.getId());
				Collection<Object> pendingInit = getEnsurePendingInit(table, typeToPendingInit, idIndex);
				pendingInit.add(persistentId);
			}
			initInstances(typeToPendingInit, cascadeTypeToPendingInit, LoadMode.REFERENCE_ONLY);
			while (0 < cascadeTypeToPendingInit.size()) {
				typeToPendingInit.clear();
				LinkedHashMap<Class<?>, Collection<Object>[]> switchVariable = typeToPendingInit;
				typeToPendingInit = cascadeTypeToPendingInit;
				cascadeTypeToPendingInit = switchVariable;
				initInstances(typeToPendingInit, cascadeTypeToPendingInit, LoadMode.VERSION_ONLY);
			}
			for (int a = orisToLoad.size(); a-- > 0;) {
				IObjRef oriToLoad = orisToLoad.get(a);

				ITableMetaData table = databaseMetaData.getTableByType(oriToLoad.getRealType());
				byte idIndex = oriToLoad.getIdNameIndex();
				Class<?> persistentIdType = table.getIdFieldByAlternateIdIndex(idIndex).getFieldType();
				Object persistentId =
						conversionHelper.convertValueToType(persistentIdType, oriToLoad.getId());

				ILoadContainer loadContainer =
						loadContainerMap.get(table.getEntityType(), Integer.valueOf(idIndex), persistentId);
				if (loadContainer == null) {
					// beanContext.getService(java.sql.Connection.class).commit();
					continue;
				}
				if (table.getVersionField() != null) {
					if (loadContainer.getReference().getVersion() == null) {
						// Entity has not been correctly initialized in
						// InitInstances...
						continue;
					}
				}
				loadContainerSet.add(loadContainer);
			}
			for (ILoadContainer loadContainer : loadContainerSet) {
				targetEntities.add(loadContainer);
			}
		}
		finally {
			disposeMaps(oldMaps);
		}
	}

	@Override
	public void assignRelations(List<IObjRelation> orelsToLoad,
			List<IObjRelationResult> targetRelations) {
		IConversionHelper conversionHelper = this.conversionHelper;
		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		IDatabase database = this.database.getCurrent();

		ILinkedMap<ObjRelationType, IList<OrelLoadItem>> groupedObjRelations =
				bucketSortObjRelations(database.getMetaData(), orelsToLoad);
		for (Entry<ObjRelationType, IList<OrelLoadItem>> entry : groupedObjRelations) {
			ObjRelationType objRelType = entry.getKey();
			IList<OrelLoadItem> orelLoadItems = entry.getValue();

			Class<?> targetingRequestType = objRelType.getEntityType();
			byte idIndex = objRelType.getIdIndex();
			// Here all objRels in this list have ObjRefs of the same targeting requestType AND same
			// targeting idIndex

			IEntityMetaData targetingRequestMetaData =
					entityMetaDataProvider.getMetaData(targetingRequestType);
			ITable targetingRequestTable = database.getTableByType(targetingRequestType);
			IDirectedLink targetingRequestLink =
					targetingRequestTable.getLinkByMemberName(objRelType.getMemberName());

			if (targetingRequestLink == null) {
				for (int a = orelLoadItems.size(); a-- > 0;) {
					OrelLoadItem orelLoadItem = orelLoadItems.get(a);
					ObjRelationResult objRelResult = new ObjRelationResult();
					objRelResult.setRelations(ObjRef.EMPTY_ARRAY);
					objRelResult.setReference(orelLoadItem.getObjRel());
					targetRelations.add(objRelResult);
				}
				continue;
			}
			IDirectedLinkMetaData targetingRequestLinkMetaData = targetingRequestLink.getMetaData();
			IEntityMetaData requestedMetaData =
					entityMetaDataProvider.getMetaData(targetingRequestLinkMetaData.getToEntityType());
			Class<?> requestedType = requestedMetaData.getEntityType();

			Member targetingIdMember = targetingRequestMetaData.getIdMemberByIdIndex(idIndex);

			ArrayList<Object> fromIds = new ArrayList<>();
			LinkedHashMap<Object, Object[]> targetingIdsMap = new LinkedHashMap<>();

			for (int a = orelLoadItems.size(); a-- > 0;) {
				OrelLoadItem orelLoadItem = orelLoadItems.get(a);
				IObjRef objRef = orelLoadItem.getObjRef();
				// We only have to store the targeting ids because all objRefs in this batch share the same
				// idIndex
				Object id = objRef.getId();
				fromIds.add(id);
				ObjRelationResult objRelResult = new ObjRelationResult();
				objRelResult.setReference(orelLoadItem.getObjRel());

				targetingIdsMap.put(id, new Object[] {objRelResult, null});
			}

			Class<?> idTypeOfTargetingObject = targetingIdMember.getRealType();

			ILinkCursor cursor = targetingRequestLink.findAllLinked(fromIds);
			try {
				byte toIdIndex;
				Class<?> idTypeOfRequestedObject;
				if (requestedMetaData.isLocalEntity()) {
					toIdIndex = cursor.getToIdIndex();
					ITableMetaData requestedTable = database.getTableByType(requestedType).getMetaData();
					IFieldMetaData idField = toIdIndex == ObjRef.PRIMARY_KEY_INDEX
							? requestedTable.getIdField()
							: requestedTable.getAlternateIdFields()[toIdIndex];
					idTypeOfRequestedObject = idField.getFieldType();
				}
				else {
					Member requestedIdMember = targetingRequestLinkMetaData.getToMember();
					idTypeOfRequestedObject = requestedIdMember.getRealType();
					toIdIndex = requestedMetaData.getIdIndexByMemberName(requestedIdMember.getName());
				}

				IPreparedObjRefFactory preparedObjRefFactory =
						objRefFactory.prepareObjRefFactory(requestedType, toIdIndex);
				while (cursor.moveNext()) {
					ILinkCursorItem item = cursor.getCurrent();

					Object fromId =
							conversionHelper.convertValueToType(idTypeOfTargetingObject, item.getFromId());
					Object toId =
							conversionHelper.convertValueToType(idTypeOfRequestedObject, item.getToId());

					IObjRef targetObjRef = preparedObjRefFactory.createObjRef(toId, null);

					Object[] objects = targetingIdsMap.get(fromId);

					@SuppressWarnings("unchecked")
					IList<IObjRef> resultingObjRefs = (IList<IObjRef>) objects[1];
					if (resultingObjRefs == null) {
						resultingObjRefs = new ArrayList<>();
						objects[1] = resultingObjRefs;
					}
					resultingObjRefs.add(targetObjRef);
				}
			}
			finally {
				cursor.dispose();
			}

			for (Entry<Object, Object[]> objectsEntry : targetingIdsMap) {
				Object[] objects = objectsEntry.getValue();
				ObjRelationResult objRelResult = (ObjRelationResult) objects[0];

				targetRelations.add(objRelResult);

				@SuppressWarnings("unchecked")
				IList<IObjRef> resultingObjRefs = (IList<IObjRef>) objects[1];
				if (resultingObjRefs == null) {
					objRelResult.setRelations(ObjRef.EMPTY_ARRAY);
					continue;
				}
				objRelResult.setRelations(resultingObjRefs.toArray(IObjRef.class));
			}
		}
	}

	protected ILinkedMap<ObjRelationType, IList<OrelLoadItem>> bucketSortObjRelations(
			IDatabaseMetaData database, List<IObjRelation> orisToLoad) {
		ILinkedMap<ObjRelationType, IList<OrelLoadItem>> sortedIObjRefs = new LinkedHashMap<>();
		ILinkedMap<Class<?>, ILinkedMap<Member, IList<Object>>> typeToMissingOris =
				new LinkedHashMap<>();
		IMap<CacheKey, IList<IObjRef>> keyToEmptyOris = new HashMap<>();

		for (int i = orisToLoad.size(); i-- > 0;) {
			IObjRelation orelToLoad = orisToLoad.get(i);

			IObjRef objRef =
					prepareObjRefForObjRelType(orelToLoad, typeToMissingOris, keyToEmptyOris, database);
			ObjRelationType objRelType = new ObjRelationType(objRef.getRealType(),
					objRef.getIdNameIndex(), orelToLoad.getMemberName());

			IList<OrelLoadItem> oreLoadItems = sortedIObjRefs.get(objRelType);
			if (oreLoadItems == null) {
				oreLoadItems = new ArrayList<>();
				sortedIObjRefs.put(objRelType, oreLoadItems);
			}
			oreLoadItems.add(new OrelLoadItem(objRef, orelToLoad));
		}

		if (!typeToMissingOris.isEmpty()) {
			loadMissingORIs(typeToMissingOris, keyToEmptyOris);
		}

		return sortedIObjRefs;
	}

	private IObjRef prepareObjRefForObjRelType(IObjRelation orelToLoad,
			ILinkedMap<Class<?>, ILinkedMap<Member, IList<Object>>> typeToMissingOris,
			IMap<CacheKey, IList<IObjRef>> keyToEmptyOris, IDatabaseMetaData database) {
		IObjRef[] objRefItems = orelToLoad.getObjRefs();

		Class<?> targetingRequestType = orelToLoad.getRealType();
		ITableMetaData targetingRequestTable = database.getTableByType(targetingRequestType);
		IDirectedLinkMetaData targetingRequestLink =
				targetingRequestTable.getLinkByMemberName(orelToLoad.getMemberName());

		byte idIndex = targetingRequestLink != null
				? targetingRequestLink.getFromIdIndex()
				: ObjRef.PRIMARY_KEY_INDEX;
		if (idIndex == ObjRef.UNDEFINED_KEY_INDEX) {
			idIndex = ObjRef.PRIMARY_KEY_INDEX;
		}
		IObjRef objRef = idIndex + 1 < objRefItems.length ? objRefItems[idIndex + 1] : null;
		if (objRef == null || objRef.getIdNameIndex() != idIndex) {
			objRef = null;
			for (IObjRef objRefItem : objRefItems) {
				if (objRefItem.getIdNameIndex() == idIndex) {
					objRef = objRefItem;
					break;
				}
			}
		}
		if (objRef == null) {
			objRef = batchMissingORIs(typeToMissingOris, keyToEmptyOris, objRefItems,
					targetingRequestType, idIndex);
		}
		return objRef;
	}

	protected IObjRef batchMissingORIs(
			ILinkedMap<Class<?>, ILinkedMap<Member, IList<Object>>> typeToMissingOris,
			IMap<CacheKey, IList<IObjRef>> keyToEmptyOri, IObjRef[] objRefItems,
			Class<?> targetingRequestType, byte idIndex) {
		// Batch first given ori to resolve the missing one
		IObjRef givenOri = objRefItems[0];
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(targetingRequestType);
		Member idMember = metaData.getIdMemberByIdIndex(givenOri.getIdNameIndex());

		ILinkedMap<Member, IList<Object>> givenMemberToValues =
				typeToMissingOris.get(targetingRequestType);
		if (givenMemberToValues == null) {
			givenMemberToValues = new LinkedHashMap<>();
			typeToMissingOris.put(targetingRequestType, givenMemberToValues);
		}
		IList<Object> values = givenMemberToValues.get(idMember);
		if (values == null) {
			values = new ArrayList<>();
			givenMemberToValues.put(idMember, values);
		}
		values.add(givenOri.getId());

		IObjRef objRef = objRefFactory.createObjRef(targetingRequestType, idIndex, null, null);
		CacheKey cacheKey = new CacheKey();
		cacheKey.setEntityType(givenOri.getRealType());
		cacheKey.setIdIndex(givenOri.getIdNameIndex());
		cacheKey.setId(conversionHelper.convertValueToType(idMember.getRealType(), givenOri.getId()));
		IList<IObjRef> oris = keyToEmptyOri.get(cacheKey);
		if (oris == null) {
			oris = new ArrayList<>();
			keyToEmptyOri.put(cacheKey, oris);
		}
		oris.add(objRef);

		return objRef;
	}

	protected void loadMissingORIs(
			ILinkedMap<Class<?>, ILinkedMap<Member, IList<Object>>> typeToMissingOris,
			IMap<CacheKey, IList<IObjRef>> keyToEmptyOris) {
		CacheKey lookupKey = new CacheKey();
		for (Entry<Class<?>, ILinkedMap<Member, IList<Object>>> entry : typeToMissingOris) {
			Class<?> entityType = entry.getKey();
			ILinkedMap<Member, IList<Object>> givenMemberToValues = entry.getValue();

			IQueryBuilder<?> qb = queryBuilderFactory.create(entityType);

			IOperator[] wheres = new IOperator[givenMemberToValues.size()];
			int index = 0;
			for (Entry<Member, IList<Object>> entry2 : givenMemberToValues) {
				Member idMember = entry2.getKey();
				IList<Object> values = entry2.getValue();
				IOperator inOperator = qb.isIn(qb.property(idMember.getName()), qb.value(values));
				wheres[index++] = inOperator;
			}

			IVersionCursor versionCursor = qb.build(qb.or(wheres)).retrieveAsVersions();
			try {
				IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
				Member idMember = metaData.getIdMember();
				Member[] alternateIdMembers = metaData.getAlternateIdMembers();
				lookupKey.setEntityType(entityType);
				while (versionCursor.moveNext()) {
					IVersionItem item = versionCursor.getCurrent();

					Object[] ids = new Object[alternateIdMembers.length + 1];

					lookupKey.setIdIndex(ObjRef.PRIMARY_KEY_INDEX);
					lookupMissingORIs(keyToEmptyOris, lookupKey, idMember, alternateIdMembers, item, ids);
					for (byte lookupIdIndex = 0; lookupIdIndex < alternateIdMembers.length; lookupIdIndex++) {
						lookupKey.setIdIndex(lookupIdIndex);
						lookupMissingORIs(keyToEmptyOris, lookupKey, idMember, alternateIdMembers, item, ids);
					}
				}
			}
			finally {
				versionCursor.dispose();
			}
		}
	}

	protected void lookupMissingORIs(IMap<CacheKey, IList<IObjRef>> keyToEmptyOris,
			CacheKey lookupKey, Member idMember, Member[] alternateIdMembers, IVersionItem item,
			Object[] ids) {
		int lookupIdIndex = lookupKey.getIdIndex();
		Member lookupIdMember;
		if (lookupIdIndex == ObjRef.PRIMARY_KEY_INDEX) {
			lookupIdMember = idMember;
		}
		else {
			lookupIdMember = alternateIdMembers[lookupIdIndex];
		}

		lookupKey.setId(conversionHelper.convertValueToType(lookupIdMember.getRealType(),
				item.getId(lookupIdIndex)));

		IList<IObjRef> emptyOris = keyToEmptyOris.get(lookupKey);
		if (emptyOris != null) {
			for (int i = emptyOris.size(); i-- > 0;) {
				IObjRef emptyOri = emptyOris.get(i);
				byte reqestedIdIndex = emptyOri.getIdNameIndex();
				int idArrayIndex = alternateIdMembers.length;
				Class<?> requestedIdType = idMember.getRealType();
				if (reqestedIdIndex != ObjRef.PRIMARY_KEY_INDEX) {
					idArrayIndex = reqestedIdIndex;
					requestedIdType = alternateIdMembers[reqestedIdIndex].getRealType();
				}
				Object id = ids[idArrayIndex];
				if (id == null) {
					id = conversionHelper.convertValueToType(requestedIdType, item.getId(reqestedIdIndex));
					ids[idArrayIndex] = id;
				}
				emptyOri.setId(id);
			}
		}
	}

	@Override
	public void fillVersion(List<IObjRef> orisWithoutVersion) {
		IDatabase database = this.database.getCurrent();
		IConversionHelper conversionHelper = this.conversionHelper;
		ILinkedMap<Class<?>, Collection<Object>[]> typeToPendingInit = new LinkedHashMap<>();
		Maps oldMaps = loadContainerMapTL.get();
		loadContainerMapTL.set(null);
		try {
			acquireMaps(orisWithoutVersion.size());
			Tuple3KeyHashMap<Class<?>, Integer, Object, IObjRef> objRefMap = getObjRefMap();

			for (int a = orisWithoutVersion.size(); a-- > 0;) {
				IObjRef ori = orisWithoutVersion.get(a);
				Class<?> type = ori.getRealType();
				int idNameIndex = ori.getIdNameIndex();

				ITableMetaData table = database.getTableByType(type).getMetaData();
				Class<?> idType = table.getIdField().getFieldType();
				Object id = conversionHelper.convertValueToType(idType, ori.getId());
				// Flush version. It will be set later to the current valid
				// value. If version remains null at the end, the entity is not persisted (any more)
				ori.setVersion(null);
				Collection<Object> pendingInit =
						getEnsurePendingInit(table, typeToPendingInit, idNameIndex);
				pendingInit.add(id);

				objRefMap.put(type, Integer.valueOf(idNameIndex), id, ori);
			}
			initInstances(typeToPendingInit, null, LoadMode.VERSION_ONLY);
		}
		finally {
			disposeMaps(oldMaps);
		}
	}

	protected Collection<Object> getEnsurePendingInit(ITableMetaData table,
			Map<Class<?>, Collection<Object>[]> typeToPendingInit, int idNameIndex) {
		return getEnsurePendingInit(table.getEntityType(), table.getAlternateIdCount(),
				typeToPendingInit, idNameIndex);
	}

	protected Collection<Object> getEnsurePendingInit(IEntityMetaData metaData,
			Map<Class<?>, Collection<Object>[]> typeToPendingInit, int idNameIndex) {
		return getEnsurePendingInit(metaData.getEntityType(), metaData.getAlternateIdCount(),
				typeToPendingInit, idNameIndex);
	}

	@SuppressWarnings("unchecked")
	protected Collection<Object> getEnsurePendingInit(Class<?> type, int alternateIdCount,
			Map<Class<?>, Collection<Object>[]> typeToPendingInit, int idNameIndex) {
		Collection<Object>[] pendingInits = typeToPendingInit.get(type);
		if (pendingInits == null) {
			pendingInits = new Collection[alternateIdCount + 1];
			typeToPendingInit.put(type, pendingInits);
		}
		Collection<Object> pendingInit = pendingInits[idNameIndex + 1];
		if (pendingInit == null) {
			pendingInit = new HashSet<>();
			pendingInits[idNameIndex + 1] = pendingInit;
		}
		return pendingInit;
	}

	protected void initInstances(ILinkedMap<Class<?>, Collection<Object>[]> typeToPendingInit,
			final LinkedHashMap<Class<?>, Collection<Object>[]> cascadeTypeToPendingInit,
			final LoadMode loadMode) {
		ArrayList<ParallelLoadItem> parallelPendingItems = new ArrayList<>();
		IDatabase database = this.database.getCurrent();
		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		Iterator<Entry<Class<?>, Collection<Object>[]>> iter = typeToPendingInit.iterator();
		while (iter.hasNext()) {
			Entry<Class<?>, Collection<Object>[]> entry = iter.next();

			final Class<?> type = entry.getKey();
			Collection<Object>[] pendingInits = entry.getValue();

			iter.remove();
			entry = null;

			ITable table = null;

			if (entityMetaDataProvider.getMetaData(type).isLocalEntity()) {
				table = database.getTableByType(type);
			}
			for (int a = 0, size = pendingInits.length; a < size; a++) {
				final Collection<Object> pendingInit = pendingInits[a];
				if (pendingInit == null) {
					// for this type of id or alternate id is nothing requested
					continue;
				}
				pendingInits[a] = null;
				if (table == null) {
					continue;
				}
				ParallelLoadItem pli = new ParallelLoadItem(type, (byte) (a - 1), pendingInit, loadMode,
						cascadeTypeToPendingInit);
				parallelPendingItems.add(pli);
			}
		}
		if (parallelPendingItems.isEmpty()) {
			return;
		}
		multithreadingHelper.invokeAndWait(parallelPendingItems,
				new IResultingBackgroundWorkerParamDelegate<Object, ParallelLoadItem>() {
					@Override
					public Object invoke(ParallelLoadItem state) throws Throwable {
						initInstances(state.entityType, state.idIndex, state.ids,
								state.cascadeTypeToPendingInit, state.loadMode);
						return null;
					}
				}, new IAggregrateResultHandler<Object, ParallelLoadItem>() {
					@Override
					public void aggregateResult(Object resultOfFork, ParallelLoadItem itemOfFork) {
						writePendingInitToShared(itemOfFork.cascadeTypeToPendingInit,
								itemOfFork.sharedCascadeTypeToPendingInit);
					}
				});
	}

	public void writePendingInitToShared(
			LinkedHashMap<Class<?>, Collection<Object>[]> cascadeTypeToPendingInit,
			LinkedHashMap<Class<?>, Collection<Object>[]> sharedCascadeTypeToPendingInit) {
		IDatabase database = this.database.getCurrent();
		for (Entry<Class<?>, Collection<Object>[]> entry : cascadeTypeToPendingInit) {
			Class<?> type = entry.getKey();
			Collection<Object>[] pendingInits = entry.getValue();
			for (int a = pendingInits.length; a-- > 0;) {
				Collection<Object> pendingInit = pendingInits[a];
				if (pendingInit == null) {
					continue;
				}
				ITableMetaData table = database.getTableByType(type).getMetaData();
				Collection<Object> sharedPendingInit =
						getEnsurePendingInit(table, sharedCascadeTypeToPendingInit, (byte) (a - 1));
				sharedPendingInit.addAll(pendingInit);
			}
		}
	}

	public void initInstances(Class<?> entityType, byte idIndex, Collection<Object> ids,
			LinkedHashMap<Class<?>, Collection<Object>[]> cascadeTypeToPendingInit, LoadMode loadMode) {
		if (LoadMode.VERSION_ONLY == loadMode) {
			loadVersionMode(entityType, idIndex, ids);
		}
		else if (LoadMode.REFERENCE_ONLY == loadMode || LoadMode.DEFAULT == loadMode) {
			loadDefault(entityType, idIndex, ids, cascadeTypeToPendingInit);
		}
		else {
			throw new IllegalArgumentException("LoadMode " + loadMode + " not supported");
		}

	}

	protected void loadVersionMode(Class<?> entityType, int idIndex, Collection<Object> ids) {
		ArrayList<Object> realNeededIds = new ArrayList<>(ids.size());
		IDatabase database = this.database.getCurrent();
		IObjRefFactory objRefFactory = this.objRefFactory;
		Tuple3KeyHashMap<Class<?>, Integer, Object, IObjRef> objRefMap = getObjRefMap();

		for (Object id : ids) {
			IObjRef ori = objRefMap.get(entityType, Integer.valueOf(idIndex), id);
			if (ori == null) {
				ori = objRefFactory.createObjRef(entityType, idIndex, id, null);
				objRefMap.put(entityType, Integer.valueOf(idIndex), id, ori);
			}
			if (ori.getVersion() == null) {
				realNeededIds.add(id);
			}
		}
		if (realNeededIds.isEmpty()) {
			return;
		}
		IConversionHelper conversionHelper = this.conversionHelper;
		ITable table = database.getTableByType(entityType);
		ITableMetaData tableMD = table.getMetaData();
		IFieldMetaData givenIdField = tableMD.getIdFieldByAlternateIdIndex(idIndex);
		String givenIdMemberName = givenIdField.getMember().getName();
		Class<?> givenIdType = givenIdField.getFieldType();
		Class<?> versionTypeOfObject = tableMD.getVersionField().getMember().getElementType();
		IVersionCursor cursor = table.selectVersion(givenIdMemberName, realNeededIds);
		try {
			while (cursor.moveNext()) {
				IVersionItem item = cursor.getCurrent();
				Object id = conversionHelper.convertValueToType(givenIdType, item.getId(idIndex));
				Object version =
						conversionHelper.convertValueToType(versionTypeOfObject, item.getVersion());

				IObjRef ori = objRefMap.get(entityType, idIndex, id);

				// Set version number to ORI explicitly here. It is not
				// known earlier...
				ori.setVersion(version);
				// There is nothing to load further if the cache only
				// contains the ORI
			}
		}
		finally {
			cursor.dispose();
		}
	}

	@SuppressWarnings("unchecked")
	protected void loadDefault(Class<?> entityType, int idIndex, Collection<Object> ids,
			LinkedHashMap<Class<?>, Collection<Object>[]> cascadeTypeToPendingInit) {
		IDatabase database = this.database.getCurrent();
		IConversionHelper conversionHelper = this.conversionHelper;
		IEntityMetaDataProvider entityMetaDataProvider = this.entityMetaDataProvider;
		IInterningFeature interningFeature = this.interningFeature;
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
		ITable table = database.getTableByType(entityType);
		ITableMetaData tableMD = table.getMetaData();
		IDirectedLink[] standaloneDirectedLinks =
				new IDirectedLink[metaData.getRelationMembers().length];
		IDirectedLink[] directedLinks = new IDirectedLink[standaloneDirectedLinks.length];
		ArrayList<Object>[] directedLinkQueues = new ArrayList[standaloneDirectedLinks.length];
		IdentityHashMap<IFieldMetaData, Integer> fieldToDirectedLinkIndex = new IdentityHashMap<>();
		ArrayList<Object> idList = new ArrayList<>(ids);
		String idFieldMemberName = tableMD.getIdFieldByAlternateIdIndex(idIndex).getMember().getName();
		Class<?> primIdType = tableMD.getIdField().getFieldType();
		IFieldMetaData versionField = tableMD.getVersionField();
		Class<?> versionTypeOfObject =
				versionField != null ? versionField.getMember().getElementType() : null;
		int primitiveMemberCount = metaData.getPrimitiveMembers().length;
		int relationMemberCount = metaData.getRelationMembers().length;

		Tuple3KeyHashMap<Class<?>, Integer, Object, IObjRef> objRefMap = getObjRefMap();
		Tuple3KeyHashMap<Class<?>, Integer, Object, ILoadContainer> loadContainerMap =
				getLoadContainerMap();

		int typesRelatingToThisCount = metaData.getTypesRelatingToThis().length;

		int cursorCount = 0;
		ICursor cursor = null;
		try {
			cursor = table.selectValues(idFieldMemberName, idList);

			IFieldMetaData[] cursorFields = cursor.getFields();
			int[] cursorFieldToPrimitiveIndex = new int[cursorFields.length];
			int[] primitiveIndexToDefinedByCursorField = new int[primitiveMemberCount];
			createMappingIndexes(cursor, cursorFieldToPrimitiveIndex,
					primitiveIndexToDefinedByCursorField, table, standaloneDirectedLinks, directedLinks,
					fieldToDirectedLinkIndex);

			for (int a = standaloneDirectedLinks.length; a-- > 0;) {
				if (standaloneDirectedLinks != null) {
					directedLinkQueues[a] = new ArrayList<>();
				}
			}
			int alternateIdCount = metaData.getAlternateIdCount();
			Object[] alternateIds =
					alternateIdCount > 0 ? new Object[alternateIdCount] : EMPTY_OBJECT_ARRAY;

			boolean doInternId = this.doInternId;
			boolean doInternVersion = this.doInternVersion;
			while (cursor.moveNext()) {
				ICursorItem item = cursor.getCurrent();
				cursorCount++;

				Object id = conversionHelper.convertValueToType(primIdType, item.getId());
				Object version = versionField != null
						? conversionHelper.convertValueToType(versionTypeOfObject, item.getVersion())
						: null;

				if (id == null || versionField != null && version == null) {
					throw new IllegalStateException(
							"Retrieved row with either null-id or null-version from table '"
									+ table.getMetaData().getName() + "'. This is a fatal database state");
				}
				if (interningFeature != null) {
					if (typesRelatingToThisCount > 0 && doInternId) {
						// If other entities may relate to this one, it makes sense to intern the id
						id = interningFeature.intern(id);
					}
					if (version != null && doInternVersion) {
						version = interningFeature.intern(version);
					}
				}
				Object[] primitives = new Object[primitiveMemberCount];

				Object[] cursorValues = item.getValues();
				for (int a = cursorFields.length; a-- > 0;) {
					Object dbValue = cursorValues[a];
					if (dbValue == null) {
						continue;
					}
					int primitiveIndex = cursorFieldToPrimitiveIndex[a];
					if (primitiveIndex == -1) {
						continue;
					}
					IFieldMetaData field = cursorFields[a];
					Integer dirLinkIndex = fieldToDirectedLinkIndex.get(field);

					if (dirLinkIndex == null || field.isAlternateId()) {
						Member fieldMember = field.getMember();
						Class<?> expectedType = fieldMember.getRealType();
						if (java.util.Date.class.isAssignableFrom(expectedType)
								|| java.util.Calendar.class.isAssignableFrom(expectedType)) {
							// store Date-instances only with their long-value for decreased heap consumption
							expectedType = long.class;
						}

						int definedByCursorIndex = primitiveIndexToDefinedByCursorField[primitiveIndex];
						if (definedByCursorIndex != -1) {
							Object definedByValue = cursorValues[definedByCursorIndex];
							expectedType = conversionHelper.convertValueToType(Class.class, definedByValue);
						}

						Object primitiveValue;
						if (field.getFieldSubType() != null
								&& (Collection.class.isAssignableFrom(expectedType) || expectedType.isArray())) {
							Class<?> elementType = fieldMember.getElementType();
							primitiveValue =
									conversionHelper.convertValueToType(expectedType, dbValue, elementType);
						}
						else {
							// The column is only a primitive field
							try {
								primitiveValue =
										connectionDialect.convertFromFieldType(database, field, expectedType, dbValue);
							}
							catch (Throwable e) {
								throw RuntimeExceptionUtil.mask(e, "Error occured while handling member: "
										+ fieldMember.getDeclaringType().getName() + "." + fieldMember.getName());
							}
						}
						if (interningFeature != null && (metaData.hasInterningBehavior(fieldMember)
								|| metaData.isAlternateId(fieldMember))) {
							primitiveValue = interningFeature.intern(primitiveValue);
						}
						primitives[primitiveIndex] = primitiveValue;
					}
				}
				for (int alternateIdIndex = alternateIds.length; alternateIdIndex-- > 0;) {
					alternateIds[alternateIdIndex] =
							compositeIdFactory.createIdFromPrimitives(metaData, alternateIdIndex, primitives);
				}
				for (int a = standaloneDirectedLinks.length; a-- > 0;) {
					IDirectedLink link = standaloneDirectedLinks[a];
					if (link == null) {
						continue;
					}
					ArrayList<Object> directedLinkQueue = directedLinkQueues[a];
					byte linkIdIndex = link.getMetaData().getFromField().getIdIndex();
					if (linkIdIndex == ObjRef.PRIMARY_KEY_INDEX) {
						directedLinkQueue.add(id);
					}
					else {
						Object alternateId = alternateIds[linkIdIndex];
						if (alternateId != null) {
							directedLinkQueue.add(alternateId);
						}
					}
				}

				LoadContainer loadContainer = unionLoadContainers(table, id, version, alternateIds);

				loadContainer.setPrimitives(primitives);

				IList<IObjRef>[] relationBuilds;
				IObjRef[][] relations;
				if (relationMemberCount != 0) {
					relationBuilds = new IList[relationMemberCount];
					relations = new IObjRef[relationMemberCount][];
				}
				else {
					relationBuilds = EMPTY_LIST_ARRAY;
					relations = EMPTY_RELATIONS_ARRAY;
				}
				loadContainer.setRelationBuilds(relationBuilds);
				loadContainer.setRelations(relations);

				// Set version number to ORI explicitly here. It is not known earlier...
				loadContainer.getReference().setVersion(version);

				if (!fieldToDirectedLinkIndex.isEmpty()) {
					for (int a = cursorFields.length; a-- > 0;) {
						Object dbValue = cursorValues[a];
						IFieldMetaData field = cursorFields[a];

						Integer dirLinkIndex = fieldToDirectedLinkIndex.get(field);
						if (dirLinkIndex == null) {
							continue;
						}
						if (dbValue == null) {
							relations[dirLinkIndex.intValue()] = ObjRef.EMPTY_ARRAY;
							continue;
						}
						IDirectedLinkMetaData columnBasedDirectedLink =
								directedLinks[dirLinkIndex.intValue()].getMetaData();
						IFieldMetaData toField = columnBasedDirectedLink.getToField();
						Class<?> targetType;
						if (toField != null) {
							targetType = toField.getFieldType();
						}
						else {
							targetType = columnBasedDirectedLink.getToMember().getRealType();
						}
						dbValue = conversionHelper.convertValueToType(targetType, dbValue);
						if (interningFeature != null && doInternId) {
							dbValue = interningFeature.intern(dbValue);
						}
						Class<?> toEntityType = columnBasedDirectedLink.getToEntityType();
						Member toMember = columnBasedDirectedLink.getToMember();
						IEntityMetaData toEntityMetaData = entityMetaDataProvider.getMetaData(toEntityType);
						int toIdIndex = toEntityMetaData.getIdIndexByMemberName(toMember.getName());

						IObjRef toOri = objRefMap.get(toEntityType, Integer.valueOf(toIdIndex), dbValue);
						if (toOri == null) {
							toOri = objRefFactory.createObjRef(toEntityType, toIdIndex, dbValue, null);
							objRefMap.put(toEntityType, Integer.valueOf(toIdIndex), dbValue, toOri);
						}
						relations[dirLinkIndex.intValue()] = new IObjRef[] {toOri};
						switch (columnBasedDirectedLink.getCascadeLoadMode()) {
							case LAZY: {
								if (supportsValueHolderContainer) {
									break;
								}
								// fall through intended
							}
							case EAGER_VERSION:
							case EAGER: {
								Collection<Object> cascadePendingInit =
										getEnsurePendingInit(toEntityMetaData, cascadeTypeToPendingInit, toIdIndex);
								cascadePendingInit.add(dbValue);
								break;
							}
							default:
								throw RuntimeExceptionUtil
										.createEnumNotSupportedException(columnBasedDirectedLink.getCascadeLoadMode());
						}
					}
				}
			}
		}
		finally {
			if (cursor != null) {
				cursor.dispose();
				cursor = null;
			}
		}
		if (log.isDebugEnabled()) {
			log.debug("Retrieved " + cursorCount + " row(s)");
		}
		for (int index = idList.size(); index-- > 0;) {
			Object splittedId = idList.get(index);
			LoadContainer loadContainer =
					(LoadContainer) loadContainerMap.get(entityType, Integer.valueOf(idIndex), splittedId);
			if (loadContainer == null) {
				// Object with requested PK has not been found in
				// database, so it also has no version at this point
				continue;
			}
			Object version = loadContainer.getReference().getVersion();
			if (version == null && versionField != null) {
				throw new IllegalStateException();
				// Object with requested PK has not been found in
				// database, so it also has no version at this point
				// continue;
			}
			List<IObjRef>[] relationBuilds = loadContainer.getRelationBuilds();
			loadContainer.setRelationBuilds(null);
			IObjRef[][] relations = loadContainer.getRelations();
			if (relations == null) {
				if (relationBuilds.length == 0) {
					relations = EMPTY_RELATIONS_ARRAY;
				}
				else {
					relations = new IObjRef[relationBuilds.length][];
				}
				loadContainer.setRelations(relations);
			}
			for (int a = relationBuilds.length; a-- > 0;) {
				if (relations[a] != null) {
					// Relations already initialized. This is possible with column-based links handled earlier
					continue;
				}
				List<IObjRef> relationBuild = relationBuilds[a];
				if (relationBuild == null) {
					// Relation has not been initialized at all, this will result in a 'real lazy' value
					// holder if
					// supported
					continue;
				}
				int size = relationBuild.size();
				IObjRef[] relationArray;
				if (size > 0) {
					relationArray = new IObjRef[size];
					for (int buildIndex = size; buildIndex-- > 0;) {
						relationArray[buildIndex] = relationBuild.get(buildIndex);
					}
				}
				else {
					relationArray = ObjRef.EMPTY_ARRAY;
				}
				relations[a] = relationArray;
			}
		}
	}

	protected void createMappingIndexes(ICursor cursor, int[] cursorFieldToPrimitiveIndex,
			int[] primitiveIndexToDefinedByCursorField, ITable table,
			IDirectedLink[] standaloneDirectedLinks, IDirectedLink[] directedLinks,
			IMap<IFieldMetaData, Integer> fieldToDirectedLinkIndex) {
		ITableMetaData tableMD = table.getMetaData();
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(tableMD.getEntityType());
		Arrays.fill(cursorFieldToPrimitiveIndex, -1);
		Arrays.fill(primitiveIndexToDefinedByCursorField, -1);
		IFieldMetaData[] cursorFields = cursor.getFields();
		PrimitiveMember[] primitiveMembers = metaData.getPrimitiveMembers();
		RelationMember[] relationMembers = metaData.getRelationMembers();

		for (int primitiveIndex = 0, size =
				primitiveMembers.length; primitiveIndex < size; primitiveIndex++) {
			PrimitiveMember primitiveMember = primitiveMembers[primitiveIndex];

			if (primitiveMember.isTransient()) {
				continue;
			}
			IFieldMetaData field = tableMD.getFieldByMemberName(primitiveMember.getName());

			if (field == null) {
				if (log.isWarnEnabled()) {
					loggerHistory.warnOnce(log, this,
							"Member '" + metaData.getEntityType().getName() + "." + primitiveMember.getName()
									+ "' is neither mapped to a field of table " + table.getMetaData().getName()
									+ " nor marked as transient");
				}
				continue;
			}
			IFieldMetaData mappedField = cursor.getFieldByMemberName(primitiveMember.getName());

			if (mappedField == null) {
				continue;
			}
			for (int b = cursorFields.length; b-- > 0;) {
				IFieldMetaData cursorField = cursorFields[b];
				if (cursorField.equals(mappedField)) {
					cursorFieldToPrimitiveIndex[b] = primitiveIndex;
					break;
				}
			}
			PrimitiveMember definedBy = primitiveMember.getDefinedBy();
			if (definedBy == null) {
				continue;
			}
			IFieldMetaData definedByField = cursor.getFieldByMemberName(definedBy.getName());
			for (int b = cursorFields.length; b-- > 0;) {
				IFieldMetaData cursorField = cursorFields[b];
				if (cursorField.equals(definedByField)) {
					primitiveIndexToDefinedByCursorField[primitiveIndex] = b;
					break;
				}
			}
		}

		for (int a = relationMembers.length; a-- > 0;) {
			RelationMember relationMember = relationMembers[a];
			String memberName = relationMember.getName();

			IDirectedLink directedLink = table.getLinkByMemberName(memberName);

			if (directedLink == null) {
				loggerHistory.warnOnce(log, this, "Member '" + table.getMetaData().getEntityType().getName()
						+ "." + memberName + "' is not mappable to a link");
				continue;
			}
			IDirectedLinkMetaData directedLinkMD = directedLink.getMetaData();

			if (directedLinkMD.isStandaloneLink()) {
				standaloneDirectedLinks[a] = directedLink;
			}
			else {
				directedLinks[a] = directedLink;
				fieldToDirectedLinkIndex.put(directedLinkMD.getFromField(), Integer.valueOf(a));
			}
		}
	}

	protected LoadContainer unionLoadContainers(ITable table, Object id, Object version,
			Object[] alternateIds) {
		Tuple3KeyHashMap<Class<?>, Integer, Object, ILoadContainer> loadContainerMap =
				getLoadContainerMap();
		ITableMetaData tableMD = table.getMetaData();
		Class<?> type = tableMD.getEntityType();
		Integer pkIdIndex = Integer.valueOf(ObjRef.PRIMARY_KEY_INDEX);
		LoadContainer loadContainer = (LoadContainer) loadContainerMap.get(type, pkIdIndex, id);
		if (loadContainer == null) {
			loadContainer = new LoadContainer();

			Tuple3KeyHashMap<Class<?>, Integer, Object, IObjRef> objRefMap = getObjRefMap();

			IObjRef primaryIdObjRef = objRefMap.get(type, pkIdIndex, id);
			if (primaryIdObjRef == null) {
				primaryIdObjRef = objRefFactory.createObjRef(type, ObjRef.PRIMARY_KEY_INDEX, id, version);
				objRefMap.put(type, pkIdIndex, id, primaryIdObjRef);
			}
			loadContainer.setReference(primaryIdObjRef);
			loadContainerMap.put(type, pkIdIndex, id, loadContainer);
		}
		for (int idNameIndex = alternateIds.length; idNameIndex-- > 0;) {
			Object alternateId = alternateIds[idNameIndex];
			if (alternateId == null) {
				continue;
			}
			loadContainerMap.put(type, Integer.valueOf(idNameIndex), alternateId, loadContainer);
		}
		return loadContainer;
	}
}
