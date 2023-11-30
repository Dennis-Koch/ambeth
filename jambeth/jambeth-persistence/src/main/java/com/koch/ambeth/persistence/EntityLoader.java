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
import com.koch.ambeth.ioc.util.IMultithreadingHelper;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.ILoggerHistory;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.compositeid.ICompositeIdFactory;
import com.koch.ambeth.merge.metadata.IObjRefFactory;
import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.merge.proxy.PersistenceContextType;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.persistence.api.ICursor;
import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.IDatabaseMetaData;
import com.koch.ambeth.persistence.api.IDirectedLink;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.ITable;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.persistence.parallel.ParallelLoadItem;
import com.koch.ambeth.query.IOperator;
import com.koch.ambeth.query.IQueryBuilderFactory;
import com.koch.ambeth.query.persistence.IVersionItem;
import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
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
import com.koch.ambeth.util.collections.Tuple3KeyHashMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@PersistenceContext(PersistenceContextType.REQUIRED)
public class EntityLoader implements IEntityLoader, ILoadContainerProvider, IStartingBean, IThreadLocalCleanupBean {
    private static final IObjRef[][] EMPTY_RELATIONS_ARRAY = ObjRef.EMPTY_ARRAY_ARRAY;
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    @SuppressWarnings("unchecked")
    private static final IList<IObjRef>[] EMPTY_LIST_ARRAY = new IList[0];
    @Forkable(processor = EntityLoaderForkProcessor.class)
    protected final ThreadLocal<Maps> loadContainerMapTL = new ThreadLocal<>();
    @Autowired
    protected IServiceContext beanContext;
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
    protected IQueryBuilderFactory queryBuilderFactory;
    @Autowired(optional = true)
    protected IInterningFeature interningFeature;
    protected boolean doInternId = true;
    protected boolean doInternVersion = true;
    @LogInstance
    private ILogger log;
    private boolean supportsValueHolderContainer;

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
        var conversionHelper = this.conversionHelper;
        var databaseMetaData = this.databaseMetaData;
        var typeToPendingInit = new LinkedHashMap<Class<?>, Collection<Object>[]>();
        var cascadeTypeToPendingInit = new LinkedHashMap<Class<?>, Collection<Object>[]>();
        var loadContainerSet = IdentityLinkedSet.<ILoadContainer>create(orisToLoad.size());
        var oldMaps = loadContainerMapTL.get();
        loadContainerMapTL.set(null);
        try {
            acquireMaps(orisToLoad.size());
            var loadContainerMap = getLoadContainerMap();
            for (int a = orisToLoad.size(); a-- > 0; ) {
                var oriToLoad = orisToLoad.get(a);
                var type = oriToLoad.getRealType();
                var idIndex = oriToLoad.getIdNameIndex();

                var table = databaseMetaData.getTableByType(type);
                var idFields = table.getIdFieldsByAlternateIdIndex(idIndex);
                if (idFields.length == 1) {
                    var persistentIdType = idFields[0].getFieldType();
                    var persistentId = conversionHelper.convertValueToType(persistentIdType, oriToLoad.getId());
                    var pendingInit = getEnsurePendingInit(table, typeToPendingInit, idIndex);
                    pendingInit.add(persistentId);
                } else {
                    var pendingInit = getEnsurePendingInit(table, typeToPendingInit, idIndex);
                    pendingInit.add(oriToLoad.getId());
                }
            }
            initInstances(typeToPendingInit, cascadeTypeToPendingInit, LoadMode.REFERENCE_ONLY);
            while (!cascadeTypeToPendingInit.isEmpty()) {
                typeToPendingInit.clear();
                var switchVariable = typeToPendingInit;
                typeToPendingInit = cascadeTypeToPendingInit;
                cascadeTypeToPendingInit = switchVariable;
                initInstances(typeToPendingInit, cascadeTypeToPendingInit, LoadMode.VERSION_ONLY);
            }
            for (int a = orisToLoad.size(); a-- > 0; ) {
                var oriToLoad = orisToLoad.get(a);

                var table = databaseMetaData.getTableByType(oriToLoad.getRealType());
                var idIndex = oriToLoad.getIdNameIndex();
                var idFields = table.getIdFieldsByAlternateIdIndex(idIndex);
                ILoadContainer loadContainer;
                if (idFields.length == 1) {
                    var persistentIdType = table.getIdFieldByAlternateIdIndex(idIndex).getFieldType();
                    var persistentId = conversionHelper.convertValueToType(persistentIdType, oriToLoad.getId());

                    loadContainer = loadContainerMap.get(table.getEntityType(), Integer.valueOf(idIndex), persistentId);
                } else {
                    loadContainer = loadContainerMap.get(table.getEntityType(), Integer.valueOf(idIndex), oriToLoad.getId());
                }
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
            for (var loadContainer : loadContainerSet) {
                targetEntities.add(loadContainer);
            }
        } finally {
            disposeMaps(oldMaps);
        }
    }

    @Override
    public void assignRelations(List<IObjRelation> orelsToLoad, List<IObjRelationResult> targetRelations) {
        var conversionHelper = this.conversionHelper;
        var entityMetaDataProvider = this.entityMetaDataProvider;
        var database = this.database.getCurrent();

        var groupedObjRelations = bucketSortObjRelations(database.getMetaData(), orelsToLoad);
        for (var entry : groupedObjRelations) {
            var objRelType = entry.getKey();
            var orelLoadItems = entry.getValue();

            var targetingRequestType = objRelType.getEntityType();
            var idIndex = objRelType.getIdIndex();
            // Here all objRels in this list have ObjRefs of the same targeting requestType AND same
            // targeting idIndex

            var targetingRequestMetaData = entityMetaDataProvider.getMetaData(targetingRequestType);
            var targetingRequestTable = database.getTableByType(targetingRequestType);
            var targetingRequestLink = targetingRequestTable.getLinkByMemberName(objRelType.getMemberName());

            if (targetingRequestLink == null) {
                for (int a = orelLoadItems.size(); a-- > 0; ) {
                    var orelLoadItem = orelLoadItems.get(a);
                    var objRelResult = new ObjRelationResult();
                    objRelResult.setRelations(IObjRef.EMPTY_ARRAY);
                    objRelResult.setReference(orelLoadItem.getObjRel());
                    targetRelations.add(objRelResult);
                }
                continue;
            }
            var targetingRequestLinkMetaData = targetingRequestLink.getMetaData();
            var requestedMetaData = entityMetaDataProvider.getMetaData(targetingRequestLinkMetaData.getToEntityType());
            var requestedType = requestedMetaData.getEntityType();

            var targetingIdMember = targetingRequestMetaData.getIdMemberByIdIndex(idIndex);

            var fromIds = new ArrayList<>();
            var targetingIdsMap = new LinkedHashMap<Object, Object[]>();

            for (int a = orelLoadItems.size(); a-- > 0; ) {
                var orelLoadItem = orelLoadItems.get(a);
                var objRef = orelLoadItem.getObjRef();
                // We only have to store the targeting ids because all objRefs in this batch share the same
                // idIndex
                var id = objRef.getId();
                fromIds.add(id);
                ObjRelationResult objRelResult = new ObjRelationResult();
                objRelResult.setReference(orelLoadItem.getObjRel());

                targetingIdsMap.put(id, new Object[] { objRelResult, null });
            }

            var idTypeOfTargetingObject = targetingIdMember.getRealType();

            var cursor = targetingRequestLink.findAllLinked(fromIds);
            try {
                byte toIdIndex;
                if (requestedMetaData.isLocalEntity()) {
                    toIdIndex = cursor.getToIdIndex();
                } else {
                    var requestedIdMember = targetingRequestLinkMetaData.getToMember();
                    toIdIndex = requestedMetaData.getIdIndexByMemberName(requestedIdMember.getName());
                }

                var preparedObjRefFactory = objRefFactory.prepareObjRefFactory(requestedType, toIdIndex);
                for (var item : cursor) {
                    var fromId = conversionHelper.convertValueToType(idTypeOfTargetingObject, item.getFromId());
                    var targetObjRef = preparedObjRefFactory.createObjRef(item.getToId(), null);

                    var objects = targetingIdsMap.get(fromId);

                    @SuppressWarnings("unchecked") var resultingObjRefs = (IList<IObjRef>) objects[1];
                    if (resultingObjRefs == null) {
                        resultingObjRefs = new ArrayList<>();
                        objects[1] = resultingObjRefs;
                    }
                    resultingObjRefs.add(targetObjRef);
                }
            } finally {
                cursor.dispose();
            }

            for (var objectsEntry : targetingIdsMap) {
                var objects = objectsEntry.getValue();
                var objRelResult = (ObjRelationResult) objects[0];

                targetRelations.add(objRelResult);

                @SuppressWarnings("unchecked") var resultingObjRefs = (IList<IObjRef>) objects[1];
                if (resultingObjRefs == null) {
                    objRelResult.setRelations(IObjRef.EMPTY_ARRAY);
                    continue;
                }
                objRelResult.setRelations(resultingObjRefs.toArray(IObjRef.class));
            }
        }
    }

    protected ILinkedMap<ObjRelationType, IList<OrelLoadItem>> bucketSortObjRelations(IDatabaseMetaData database, List<IObjRelation> orisToLoad) {
        var sortedIObjRefs = new LinkedHashMap<ObjRelationType, IList<OrelLoadItem>>();
        var typeToMissingOris = new LinkedHashMap<Class<?>, ILinkedMap<Member, IList<Object>>>();
        var keyToEmptyOris = new HashMap<CacheKey, IList<IObjRef>>();

        for (int i = orisToLoad.size(); i-- > 0; ) {
            var orelToLoad = orisToLoad.get(i);

            var objRef = prepareObjRefForObjRelType(orelToLoad, typeToMissingOris, keyToEmptyOris, database);
            var objRelType = new ObjRelationType(objRef.getRealType(), objRef.getIdNameIndex(), orelToLoad.getMemberName());

            var oreLoadItems = sortedIObjRefs.get(objRelType);
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

    private IObjRef prepareObjRefForObjRelType(IObjRelation orelToLoad, ILinkedMap<Class<?>, ILinkedMap<Member, IList<Object>>> typeToMissingOris, IMap<CacheKey, IList<IObjRef>> keyToEmptyOris,
            IDatabaseMetaData database) {
        var objRefItems = orelToLoad.getObjRefs();

        var targetingRequestType = orelToLoad.getRealType();
        var targetingRequestTable = database.getTableByType(targetingRequestType);
        var targetingRequestLink = targetingRequestTable.getLinkByMemberName(orelToLoad.getMemberName());

        var idIndex = targetingRequestLink != null ? targetingRequestLink.getFromIdIndex() : ObjRef.PRIMARY_KEY_INDEX;
        if (idIndex == ObjRef.UNDEFINED_KEY_INDEX) {
            idIndex = ObjRef.PRIMARY_KEY_INDEX;
        }
        var objRef = idIndex + 1 < objRefItems.length ? objRefItems[idIndex + 1] : null;
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
            objRef = batchMissingORIs(typeToMissingOris, keyToEmptyOris, objRefItems, targetingRequestType, idIndex);
        }
        return objRef;
    }

    protected IObjRef batchMissingORIs(ILinkedMap<Class<?>, ILinkedMap<Member, IList<Object>>> typeToMissingOris, IMap<CacheKey, IList<IObjRef>> keyToEmptyOri, IObjRef[] objRefItems,
            Class<?> targetingRequestType, byte idIndex) {
        // Batch first given ori to resolve the missing one
        var givenOri = objRefItems[0];
        var metaData = entityMetaDataProvider.getMetaData(targetingRequestType);
        var idMember = metaData.getIdMemberByIdIndex(givenOri.getIdNameIndex());

        var givenMemberToValues = typeToMissingOris.get(targetingRequestType);
        if (givenMemberToValues == null) {
            givenMemberToValues = new LinkedHashMap<>();
            typeToMissingOris.put(targetingRequestType, givenMemberToValues);
        }
        var values = givenMemberToValues.get(idMember);
        if (values == null) {
            values = new ArrayList<>();
            givenMemberToValues.put(idMember, values);
        }
        values.add(givenOri.getId());

        var objRef = objRefFactory.createObjRef(targetingRequestType, idIndex, null, null);
        var cacheKey = new CacheKey();
        cacheKey.setEntityType(givenOri.getRealType());
        cacheKey.setIdIndex(givenOri.getIdNameIndex());
        cacheKey.setId(conversionHelper.convertValueToType(idMember.getRealType(), givenOri.getId()));
        var oris = keyToEmptyOri.get(cacheKey);
        if (oris == null) {
            oris = new ArrayList<>();
            keyToEmptyOri.put(cacheKey, oris);
        }
        oris.add(objRef);

        return objRef;
    }

    protected void loadMissingORIs(ILinkedMap<Class<?>, ILinkedMap<Member, IList<Object>>> typeToMissingOris, IMap<CacheKey, IList<IObjRef>> keyToEmptyOris) {
        var lookupKey = new CacheKey();
        for (var entry : typeToMissingOris) {
            var entityType = entry.getKey();
            var givenMemberToValues = entry.getValue();

            var qb = queryBuilderFactory.create(entityType);

            var wheres = new IOperator[givenMemberToValues.size()];
            int index = 0;
            for (var entry2 : givenMemberToValues) {
                var idMember = entry2.getKey();
                var values = entry2.getValue();
                var inOperator = qb.let(qb.property(idMember.getName())).isIn(qb.value(values));
                wheres[index++] = inOperator;
            }

            var versionCursor = qb.build(qb.or(wheres)).retrieveAsVersions();
            try {
                var metaData = entityMetaDataProvider.getMetaData(entityType);
                var idMember = metaData.getIdMember();
                var alternateIdMembers = metaData.getAlternateIdMembers();
                lookupKey.setEntityType(entityType);
                for (var item : versionCursor) {
                    var ids = new Object[alternateIdMembers.length + 1];

                    lookupKey.setIdIndex(ObjRef.PRIMARY_KEY_INDEX);
                    lookupMissingORIs(keyToEmptyOris, lookupKey, idMember, alternateIdMembers, item, ids);
                    for (byte lookupIdIndex = 0; lookupIdIndex < alternateIdMembers.length; lookupIdIndex++) {
                        lookupKey.setIdIndex(lookupIdIndex);
                        lookupMissingORIs(keyToEmptyOris, lookupKey, idMember, alternateIdMembers, item, ids);
                    }
                }
            } finally {
                versionCursor.dispose();
            }
        }
    }

    protected void lookupMissingORIs(IMap<CacheKey, IList<IObjRef>> keyToEmptyOris, CacheKey lookupKey, Member idMember, Member[] alternateIdMembers, IVersionItem item, Object[] ids) {
        int lookupIdIndex = lookupKey.getIdIndex();
        Member lookupIdMember;
        if (lookupIdIndex == ObjRef.PRIMARY_KEY_INDEX) {
            lookupIdMember = idMember;
        } else {
            lookupIdMember = alternateIdMembers[lookupIdIndex];
        }

        lookupKey.setId(conversionHelper.convertValueToType(lookupIdMember.getRealType(), item.getId(lookupIdIndex)));

        var emptyOris = keyToEmptyOris.get(lookupKey);
        if (emptyOris != null) {
            for (int i = emptyOris.size(); i-- > 0; ) {
                var emptyOri = emptyOris.get(i);
                var reqestedIdIndex = emptyOri.getIdNameIndex();
                var idArrayIndex = alternateIdMembers.length;
                var requestedIdType = idMember.getRealType();
                if (reqestedIdIndex != ObjRef.PRIMARY_KEY_INDEX) {
                    idArrayIndex = reqestedIdIndex;
                    requestedIdType = alternateIdMembers[reqestedIdIndex].getRealType();
                }
                var id = ids[idArrayIndex];
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
        var database = this.database.getCurrent();
        var conversionHelper = this.conversionHelper;
        var typeToPendingInit = new LinkedHashMap<Class<?>, Collection<Object>[]>();
        var oldMaps = loadContainerMapTL.get();
        loadContainerMapTL.set(null);
        try {
            acquireMaps(orisWithoutVersion.size());
            var objRefMap = getObjRefMap();

            for (int a = orisWithoutVersion.size(); a-- > 0; ) {
                var ori = orisWithoutVersion.get(a);
                var type = ori.getRealType();
                var idNameIndex = ori.getIdNameIndex();

                var table = database.getTableByType(type).getMetaData();
                var idType = table.getIdField().getFieldType();
                var id = conversionHelper.convertValueToType(idType, ori.getId());
                // Flush version. It will be set later to the current valid
                // value. If version remains null at the end, the entity is not persisted (any more)
                ori.setVersion(null);
                var pendingInit = getEnsurePendingInit(table, typeToPendingInit, idNameIndex);
                pendingInit.add(id);

                objRefMap.put(type, Integer.valueOf(idNameIndex), id, ori);
            }
            initInstances(typeToPendingInit, null, LoadMode.VERSION_ONLY);
        } finally {
            disposeMaps(oldMaps);
        }
    }

    protected Collection<Object> getEnsurePendingInit(ITableMetaData table, Map<Class<?>, Collection<Object>[]> typeToPendingInit, int idNameIndex) {
        return getEnsurePendingInit(table.getEntityType(), table.getAlternateIdCount(), typeToPendingInit, idNameIndex);
    }

    protected Collection<Object> getEnsurePendingInit(IEntityMetaData metaData, Map<Class<?>, Collection<Object>[]> typeToPendingInit, int idNameIndex) {
        return getEnsurePendingInit(metaData.getEntityType(), metaData.getAlternateIdCount(), typeToPendingInit, idNameIndex);
    }

    @SuppressWarnings("unchecked")
    protected Collection<Object> getEnsurePendingInit(Class<?> type, int alternateIdCount, Map<Class<?>, Collection<Object>[]> typeToPendingInit, int idNameIndex) {
        var pendingInits = typeToPendingInit.get(type);
        if (pendingInits == null) {
            pendingInits = new Collection[alternateIdCount + 1];
            typeToPendingInit.put(type, pendingInits);
        }
        var pendingInit = pendingInits[idNameIndex + 1];
        if (pendingInit == null) {
            pendingInit = new HashSet<>();
            pendingInits[idNameIndex + 1] = pendingInit;
        }
        return pendingInit;
    }

    protected void initInstances(ILinkedMap<Class<?>, Collection<Object>[]> typeToPendingInit, final LinkedHashMap<Class<?>, Collection<Object>[]> cascadeTypeToPendingInit, final LoadMode loadMode) {
        var parallelPendingItems = new ArrayList<ParallelLoadItem>();
        var database = this.database.getCurrent();
        var entityMetaDataProvider = this.entityMetaDataProvider;
        var iter = typeToPendingInit.iterator();
        while (iter.hasNext()) {
            var entry = iter.next();

            var type = entry.getKey();
            var pendingInits = entry.getValue();

            iter.remove();
            entry = null;

            ITable table = null;

            if (entityMetaDataProvider.getMetaData(type).isLocalEntity()) {
                table = database.getTableByType(type);
            }
            for (int a = 0, size = pendingInits.length; a < size; a++) {
                var pendingInit = pendingInits[a];
                if (pendingInit == null) {
                    // for this type of id or alternate id is nothing requested
                    continue;
                }
                pendingInits[a] = null;
                if (table == null) {
                    continue;
                }
                var pli = new ParallelLoadItem(type, (byte) (a - 1), pendingInit, loadMode, cascadeTypeToPendingInit);
                parallelPendingItems.add(pli);
            }
        }
        if (parallelPendingItems.isEmpty()) {
            return;
        }
        multithreadingHelper.invokeAndWait(parallelPendingItems, state -> {
            initInstances(state.entityType, state.idIndex, state.ids, state.cascadeTypeToPendingInit, state.loadMode);
            return null;
        }, (resultOfFork, itemOfFork) -> writePendingInitToShared(itemOfFork.cascadeTypeToPendingInit, itemOfFork.sharedCascadeTypeToPendingInit));
    }

    public void writePendingInitToShared(LinkedHashMap<Class<?>, Collection<Object>[]> cascadeTypeToPendingInit, LinkedHashMap<Class<?>, Collection<Object>[]> sharedCascadeTypeToPendingInit) {
        var database = this.database.getCurrent();
        for (var entry : cascadeTypeToPendingInit) {
            var type = entry.getKey();
            var pendingInits = entry.getValue();
            for (int a = pendingInits.length; a-- > 0; ) {
                var pendingInit = pendingInits[a];
                if (pendingInit == null) {
                    continue;
                }
                var table = database.getTableByType(type).getMetaData();
                var sharedPendingInit = getEnsurePendingInit(table, sharedCascadeTypeToPendingInit, (byte) (a - 1));
                sharedPendingInit.addAll(pendingInit);
            }
        }
    }

    public void initInstances(Class<?> entityType, byte idIndex, Collection<Object> ids, LinkedHashMap<Class<?>, Collection<Object>[]> cascadeTypeToPendingInit, LoadMode loadMode) {
        if (LoadMode.VERSION_ONLY == loadMode) {
            loadVersionMode(entityType, idIndex, ids);
        } else if (LoadMode.REFERENCE_ONLY == loadMode || LoadMode.DEFAULT == loadMode) {
            loadDefault(entityType, idIndex, ids, cascadeTypeToPendingInit);
        } else {
            throw new IllegalArgumentException("LoadMode " + loadMode + " not supported");
        }

    }

    protected void loadVersionMode(Class<?> entityType, int idIndex, Collection<Object> ids) {
        var realNeededIds = new ArrayList<>(ids.size());
        var database = this.database.getCurrent();
        var objRefFactory = this.objRefFactory;
        var objRefMap = getObjRefMap();

        for (var id : ids) {
            var ori = objRefMap.get(entityType, Integer.valueOf(idIndex), id);
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
        var conversionHelper = this.conversionHelper;
        var table = database.getTableByType(entityType);
        var tableMD = table.getMetaData();
        var givenIdField = tableMD.getIdFieldByAlternateIdIndex(idIndex);
        var givenIdType = givenIdField.getFieldType();
        var versionTypeOfObject = tableMD.getVersionField().getMember().getElementType();
        var cursor = table.selectVersion(idIndex, realNeededIds);
        try {
            for (var item : cursor) {
                var id = conversionHelper.convertValueToType(givenIdType, item.getId(idIndex));
                var version = conversionHelper.convertValueToType(versionTypeOfObject, item.getVersion());

                var ori = objRefMap.get(entityType, idIndex, id);

                // Set version number to ORI explicitly here. It is not
                // known earlier...
                ori.setVersion(version);
                // There is nothing to load further if the cache only
                // contains the ORI
            }
        } finally {
            cursor.dispose();
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadDefault(Class<?> entityType, int idIndex, Collection<Object> ids, LinkedHashMap<Class<?>, Collection<Object>[]> cascadeTypeToPendingInit) {
        var database = this.database.getCurrent();
        var conversionHelper = this.conversionHelper;
        var entityMetaDataProvider = this.entityMetaDataProvider;
        var interningFeature = this.interningFeature;
        var metaData = entityMetaDataProvider.getMetaData(entityType);
        var table = database.getTableByType(entityType);
        var tableMD = table.getMetaData();
        var standaloneDirectedLinks = new IDirectedLink[metaData.getRelationMembers().length];
        var directedLinks = new IDirectedLink[standaloneDirectedLinks.length];
        var directedLinkQueues = new ArrayList[standaloneDirectedLinks.length];
        var fieldToDirectedLinkIndex = new IdentityHashMap<IFieldMetaData, Integer>();
        var idList = new ArrayList<>(ids);
        var idFields = tableMD.getIdFields();
        var versionField = tableMD.getVersionField();
        var versionTypeOfObject = versionField != null ? versionField.getMember().getElementType() : null;
        var primitiveMemberCount = metaData.getPrimitiveMembers().length;
        var relationMemberCount = metaData.getRelationMembers().length;

        var objRefMap = getObjRefMap();
        var loadContainerMap = getLoadContainerMap();

        var typesRelatingToThisCount = metaData.getTypesRelatingToThis().length;

        var cursorCount = 0;
        ICursor cursor = null;
        try {
            cursor = table.selectValues(idIndex, idList);

            var cursorFields = cursor.getFields();
            var cursorFieldToPrimitiveIndex = new int[cursorFields.length];
            var primitiveIndexToDefinedByCursorField = new int[primitiveMemberCount];
            createMappingIndexes(cursor, cursorFieldToPrimitiveIndex, primitiveIndexToDefinedByCursorField, table, standaloneDirectedLinks, directedLinks, fieldToDirectedLinkIndex);

            for (int a = standaloneDirectedLinks.length; a-- > 0; ) {
                if (standaloneDirectedLinks != null) {
                    directedLinkQueues[a] = new ArrayList<>();
                }
            }
            var alternateIdCount = metaData.getAlternateIdCount();
            var alternateIds = alternateIdCount > 0 ? new Object[alternateIdCount] : EMPTY_OBJECT_ARRAY;

            var doInternId = this.doInternId;
            var doInternVersion = this.doInternVersion;
            for (var item : cursor) {
                cursorCount++;

                var itemId = item.getId(IObjRef.PRIMARY_KEY_INDEX);
                Object id;
                if (idFields.length == 1) {
                    id = conversionHelper.convertValueToType(idFields[0].getMember().getElementType(), itemId);
                } else {
                    var itemIdArray = (Object[]) itemId;
                    id = compositeIdFactory.createCompositeId(metaData, IObjRef.PRIMARY_KEY_INDEX, itemIdArray);
                }
                var version = versionField != null ? conversionHelper.convertValueToType(versionTypeOfObject, item.getVersion()) : null;

                if (id == null || versionField != null && version == null) {
                    throw new IllegalStateException("Retrieved row with either null-id or null-version from table '" + table.getMetaData().getName() + "'. This is a fatal database state");
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
                var primitives = new Object[primitiveMemberCount];

                var cursorValues = item.getValues();
                for (int a = cursorFields.length; a-- > 0; ) {
                    var dbValue = cursorValues[a];
                    if (dbValue == null) {
                        continue;
                    }
                    var primitiveIndex = cursorFieldToPrimitiveIndex[a];
                    if (primitiveIndex == -1) {
                        continue;
                    }
                    var field = cursorFields[a];
                    var dirLinkIndex = fieldToDirectedLinkIndex.get(field);

                    if (dirLinkIndex == null || field.isAlternateId()) {
                        var fieldMember = field.getMember();
                        var expectedType = fieldMember.getRealType();
                        if (java.util.Date.class.isAssignableFrom(expectedType) || java.util.Calendar.class.isAssignableFrom(expectedType)) {
                            // store Date-instances only with their long-value for decreased heap consumption
                            expectedType = long.class;
                        }

                        var definedByCursorIndex = primitiveIndexToDefinedByCursorField[primitiveIndex];
                        if (definedByCursorIndex != -1) {
                            Object definedByValue = cursorValues[definedByCursorIndex];
                            expectedType = conversionHelper.convertValueToType(Class.class, definedByValue);
                        }

                        Object primitiveValue;
                        if (field.getFieldSubType() != null && (Collection.class.isAssignableFrom(expectedType) || expectedType.isArray())) {
                            var elementType = fieldMember.getElementType();
                            primitiveValue = conversionHelper.convertValueToType(expectedType, dbValue, elementType);
                        } else {
                            // The column is only a primitive field
                            try {
                                var requestedType = expectedType;
                                if (Optional.class.isAssignableFrom(expectedType)) {
                                    requestedType = fieldMember.getElementType();
                                }
                                primitiveValue = connectionDialect.convertFromFieldType(database, field, requestedType, dbValue);
                                if (Optional.class.isAssignableFrom(expectedType)) {
                                    primitiveValue = Optional.ofNullable(primitiveValue);
                                }
                            } catch (Throwable e) {
                                throw RuntimeExceptionUtil.mask(e, "Error occured while handling member: " + fieldMember.getDeclaringType().getName() + "." + fieldMember.getName());
                            }
                        }
                        if (interningFeature != null && (metaData.hasInterningBehavior(fieldMember) || metaData.isAlternateId(fieldMember))) {
                            primitiveValue = interningFeature.intern(primitiveValue);
                        }
                        primitives[primitiveIndex] = primitiveValue;
                    }
                }
                for (int alternateIdIndex = alternateIds.length; alternateIdIndex-- > 0; ) {
                    alternateIds[alternateIdIndex] = compositeIdFactory.createIdFromPrimitives(metaData, alternateIdIndex, primitives);
                }
                for (int a = standaloneDirectedLinks.length; a-- > 0; ) {
                    var link = standaloneDirectedLinks[a];
                    if (link == null) {
                        continue;
                    }
                    var directedLinkQueue = directedLinkQueues[a];
                    byte linkIdIndex = link.getMetaData().getFromField().getIdIndex();
                    if (linkIdIndex == ObjRef.PRIMARY_KEY_INDEX) {
                        directedLinkQueue.add(id);
                    } else {
                        var alternateId = alternateIds[linkIdIndex];
                        if (alternateId != null) {
                            directedLinkQueue.add(alternateId);
                        }
                    }
                }

                var loadContainer = unionLoadContainers(table, id, version, alternateIds);

                loadContainer.setPrimitives(primitives);

                IList<IObjRef>[] relationBuilds;
                IObjRef[][] relations;
                if (relationMemberCount != 0) {
                    relationBuilds = new IList[relationMemberCount];
                    relations = new IObjRef[relationMemberCount][];
                } else {
                    relationBuilds = EMPTY_LIST_ARRAY;
                    relations = EMPTY_RELATIONS_ARRAY;
                }
                loadContainer.setRelationBuilds(relationBuilds);
                loadContainer.setRelations(relations);

                // Set version number to ORI explicitly here. It is not known earlier...
                loadContainer.getReference().setVersion(version);

                if (!fieldToDirectedLinkIndex.isEmpty()) {
                    for (int a = cursorFields.length; a-- > 0; ) {
                        var dbValue = cursorValues[a];
                        var field = cursorFields[a];

                        var dirLinkIndex = fieldToDirectedLinkIndex.get(field);
                        if (dirLinkIndex == null) {
                            continue;
                        }
                        if (dbValue == null) {
                            relations[dirLinkIndex.intValue()] = IObjRef.EMPTY_ARRAY;
                            continue;
                        }
                        var columnBasedDirectedLink = directedLinks[dirLinkIndex.intValue()].getMetaData();
                        var toField = columnBasedDirectedLink.getToField();
                        Class<?> targetType;
                        if (toField != null) {
                            targetType = toField.getFieldType();
                        } else {
                            targetType = columnBasedDirectedLink.getToMember().getRealType();
                        }
                        dbValue = conversionHelper.convertValueToType(targetType, dbValue);
                        if (interningFeature != null && doInternId) {
                            dbValue = interningFeature.intern(dbValue);
                        }
                        var toEntityType = columnBasedDirectedLink.getToEntityType();
                        var toMember = columnBasedDirectedLink.getToMember();
                        var toEntityMetaData = entityMetaDataProvider.getMetaData(toEntityType);
                        var toIdIndex = toEntityMetaData.getIdIndexByMemberName(toMember.getName());

                        var toOri = objRefMap.get(toEntityType, Integer.valueOf(toIdIndex), dbValue);
                        if (toOri == null) {
                            toOri = objRefFactory.createObjRef(toEntityType, toIdIndex, dbValue, null);
                            objRefMap.put(toEntityType, Integer.valueOf(toIdIndex), dbValue, toOri);
                        }
                        relations[dirLinkIndex.intValue()] = new IObjRef[] { toOri };
                        switch (columnBasedDirectedLink.getCascadeLoadMode()) {
                            case LAZY: {
                                if (supportsValueHolderContainer) {
                                    break;
                                }
                                // fall through intended
                            }
                            case EAGER_VERSION:
                            case EAGER: {
                                Collection<Object> cascadePendingInit = getEnsurePendingInit(toEntityMetaData, cascadeTypeToPendingInit, toIdIndex);
                                cascadePendingInit.add(dbValue);
                                break;
                            }
                            default:
                                throw RuntimeExceptionUtil.createEnumNotSupportedException(columnBasedDirectedLink.getCascadeLoadMode());
                        }
                    }
                }
            }
        } finally {
            if (cursor != null) {
                cursor.dispose();
                cursor = null;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Retrieved " + cursorCount + " row(s)");
        }
        for (int index = idList.size(); index-- > 0; ) {
            var splittedId = idList.get(index);
            var loadContainer = (LoadContainer) loadContainerMap.get(entityType, Integer.valueOf(idIndex), splittedId);
            if (loadContainer == null) {
                // Object with requested PK has not been found in
                // database, so it also has no version at this point
                continue;
            }
            var version = loadContainer.getReference().getVersion();
            if (version == null && versionField != null) {
                throw new IllegalStateException();
                // Object with requested PK has not been found in
                // database, so it also has no version at this point
                // continue;
            }
            var relationBuilds = loadContainer.getRelationBuilds();
            loadContainer.setRelationBuilds(null);
            var relations = loadContainer.getRelations();
            if (relations == null) {
                if (relationBuilds.length == 0) {
                    relations = EMPTY_RELATIONS_ARRAY;
                } else {
                    relations = new IObjRef[relationBuilds.length][];
                }
                loadContainer.setRelations(relations);
            }
            for (int a = relationBuilds.length; a-- > 0; ) {
                if (relations[a] != null) {
                    // Relations already initialized. This is possible with column-based links handled earlier
                    continue;
                }
                var relationBuild = relationBuilds[a];
                if (relationBuild == null) {
                    // Relation has not been initialized at all, this will result in a 'real lazy' value
                    // holder if
                    // supported
                    continue;
                }
                var size = relationBuild.size();
                IObjRef[] relationArray;
                if (size > 0) {
                    relationArray = new IObjRef[size];
                    for (int buildIndex = size; buildIndex-- > 0; ) {
                        relationArray[buildIndex] = relationBuild.get(buildIndex);
                    }
                } else {
                    relationArray = IObjRef.EMPTY_ARRAY;
                }
                relations[a] = relationArray;
            }
        }
    }

    protected void createMappingIndexes(ICursor cursor, int[] cursorFieldToPrimitiveIndex, int[] primitiveIndexToDefinedByCursorField, ITable table, IDirectedLink[] standaloneDirectedLinks,
            IDirectedLink[] directedLinks, IMap<IFieldMetaData, Integer> fieldToDirectedLinkIndex) {
        var tableMD = table.getMetaData();
        var metaData = entityMetaDataProvider.getMetaData(tableMD.getEntityType());
        Arrays.fill(cursorFieldToPrimitiveIndex, -1);
        Arrays.fill(primitiveIndexToDefinedByCursorField, -1);
        var cursorFields = cursor.getFields();
        var primitiveMembers = metaData.getPrimitiveMembers();
        var relationMembers = metaData.getRelationMembers();

        for (int primitiveIndex = 0, size = primitiveMembers.length; primitiveIndex < size; primitiveIndex++) {
            var primitiveMember = primitiveMembers[primitiveIndex];

            if (primitiveMember.isTransient()) {
                continue;
            }
            var field = tableMD.getFieldByMemberName(primitiveMember.getName());

            if (field == null) {
                if (log.isWarnEnabled()) {
                    loggerHistory.warnOnce(log, this,
                            "Member '" + metaData.getEntityType().getName() + "." + primitiveMember.getName() + "' is neither mapped to a field of table " + table.getMetaData().getName() +
                                    " nor marked as " + "transient");
                }
                continue;
            }
            var mappedField = cursor.getFieldByMemberName(primitiveMember.getName());

            if (mappedField == null) {
                continue;
            }
            for (int b = cursorFields.length; b-- > 0; ) {
                var cursorField = cursorFields[b];
                if (cursorField.equals(mappedField)) {
                    cursorFieldToPrimitiveIndex[b] = primitiveIndex;
                    break;
                }
            }
            var definedBy = primitiveMember.getDefinedBy();
            if (definedBy == null) {
                continue;
            }
            var definedByField = cursor.getFieldByMemberName(definedBy.getName());
            for (int b = cursorFields.length; b-- > 0; ) {
                var cursorField = cursorFields[b];
                if (cursorField.equals(definedByField)) {
                    primitiveIndexToDefinedByCursorField[primitiveIndex] = b;
                    break;
                }
            }
        }

        for (int a = relationMembers.length; a-- > 0; ) {
            var relationMember = relationMembers[a];
            var memberName = relationMember.getName();

            var directedLink = table.getLinkByMemberName(memberName);

            if (directedLink == null) {
                loggerHistory.warnOnce(log, this, "Member '" + table.getMetaData().getEntityType().getName() + "." + memberName + "' is not mappable to a link");
                continue;
            }
            var directedLinkMD = directedLink.getMetaData();

            if (directedLinkMD.isStandaloneLink()) {
                standaloneDirectedLinks[a] = directedLink;
            } else {
                directedLinks[a] = directedLink;
                fieldToDirectedLinkIndex.put(directedLinkMD.getFromField(), Integer.valueOf(a));
            }
        }
    }

    protected LoadContainer unionLoadContainers(ITable table, Object id, Object version, Object[] alternateIds) {
        var loadContainerMap = getLoadContainerMap();
        var tableMD = table.getMetaData();
        var type = tableMD.getEntityType();
        var pkIdIndex = Integer.valueOf(ObjRef.PRIMARY_KEY_INDEX);
        var loadContainer = (LoadContainer) loadContainerMap.get(type, pkIdIndex, id);
        if (loadContainer == null) {
            loadContainer = new LoadContainer();

            var objRefMap = getObjRefMap();

            var primaryIdObjRef = objRefMap.get(type, pkIdIndex, id);
            if (primaryIdObjRef == null) {
                primaryIdObjRef = objRefFactory.createObjRef(type, ObjRef.PRIMARY_KEY_INDEX, id, version);
                objRefMap.put(type, pkIdIndex, id, primaryIdObjRef);
            }
            loadContainer.setReference(primaryIdObjRef);
            loadContainerMap.put(type, pkIdIndex, id, loadContainer);
        }
        for (int idNameIndex = alternateIds.length; idNameIndex-- > 0; ) {
            var alternateId = alternateIds[idNameIndex];
            if (alternateId == null) {
                continue;
            }
            loadContainerMap.put(type, Integer.valueOf(idNameIndex), alternateId, loadContainer);
        }
        return loadContainer;
    }

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
            var baseValue = (Maps) value;
            var fork = (Maps) forkedValue;

            for (var entry : fork.loadContainerMap) {
                baseValue.loadContainerMap.put(entry.getKey1(), entry.getKey2(), entry.getKey3(), entry.getValue());
            }
        }
    }

    public static class Maps {
        public final Tuple3KeyHashMap<Class<?>, Integer, Object, ILoadContainer> loadContainerMap;

        public final Tuple3KeyHashMap<Class<?>, Integer, Object, IObjRef> objRefMap;

        public Maps(int sizeHint) {
            loadContainerMap = sizeHint > 0 ? new Tuple3KeyHashMap<Class<?>, Integer, Object, ILoadContainer>((int) (sizeHint / 0.75f) + 1, 0.75f) :
                    new Tuple3KeyHashMap<Class<?>, Integer, Object, ILoadContainer>();
            objRefMap = sizeHint > 0 ? new Tuple3KeyHashMap<Class<?>, Integer, Object, IObjRef>((int) (sizeHint / 0.75f) + 1, 0.75f) : new Tuple3KeyHashMap<Class<?>, Integer, Object, IObjRef>();
        }
    }
}
