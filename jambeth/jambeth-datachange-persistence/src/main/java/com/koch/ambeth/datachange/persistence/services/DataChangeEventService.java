package com.koch.ambeth.datachange.persistence.services;

/*-
 * #%L
 * jambeth-datachange-persistence
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

import com.koch.ambeth.datachange.model.IDataChange;
import com.koch.ambeth.datachange.model.IDataChangeEntry;
import com.koch.ambeth.datachange.persistence.config.DataChangePersistenceConfigurationConstants;
import com.koch.ambeth.datachange.persistence.model.DataChangeEntryBO;
import com.koch.ambeth.datachange.persistence.model.DataChangeEventBO;
import com.koch.ambeth.datachange.persistence.model.EntityType;
import com.koch.ambeth.datachange.transfer.DataChangeEntry;
import com.koch.ambeth.datachange.transfer.DataChangeEvent;
import com.koch.ambeth.event.server.IEventStore;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.merge.IEntityFactory;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.merge.util.IPrefetchHelper;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.ISet;

import java.util.List;

public class DataChangeEventService implements IDataChangeEventService, IStartingBean {
    private static final long KEEP_EVENTS_FOREVER = -1;

    @Autowired
    protected ICache cache;

    @Autowired
    protected IConversionHelper conversionHelper;

    @Autowired
    protected IDataChangeEventDAO dataChangeEventDAO;

    @Autowired
    protected IEntityFactory entityFactory;

    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;

    @Autowired
    protected IEventStore eventStore;

    @Autowired
    protected IPrefetchHelper prefetchHelper;

    @Property(name = DataChangePersistenceConfigurationConstants.KeepEventsForMillis, defaultValue = KEEP_EVENTS_FOREVER + "")
    protected long keepEventsForMillis = KEEP_EVENTS_FOREVER;

    @Override
    public void afterStarted() throws Throwable {
        removeExpired();
        List<IDataChange> eventObjects = retrieveAll();
        @SuppressWarnings("unchecked") List<Object> asObjectList = (List<Object>) (Object) eventObjects;
        eventStore.addEvents(asObjectList);
    }

    @Override
    public void save(IDataChange dataChange) {
        var entityTypes = new HashSet<Class<?>>();
        addAllEntityTypes(entityTypes, dataChange.getInserts());
        addAllEntityTypes(entityTypes, dataChange.getUpdates());
        addAllEntityTypes(entityTypes, dataChange.getDeletes());

        var classToEntityType = loadEntityTypes(entityTypes);

        var bo = entityFactory.createEntity(DataChangeEventBO.class);
        bo.setChangeTime(dataChange.getChangeTime());
        bo.setInserts(toDataChangeEntryList(dataChange.getInserts(), classToEntityType));
        bo.setUpdates(toDataChangeEntryList(dataChange.getUpdates(), classToEntityType));
        bo.setDeletes(toDataChangeEntryList(dataChange.getDeletes(), classToEntityType));

        dataChangeEventDAO.save(bo);
    }

    protected List<IDataChange> retrieveAll() {
        var bos = dataChangeEventDAO.retrieveAll();
        var retrieved = new ArrayList<IDataChange>();

        var prefetchHandle = prefetchHelper.createPrefetch()
                                           .add(DataChangeEventBO.class, "Inserts.EntityType")
                                           .add(DataChangeEventBO.class, "Updates.EntityType")
                                           .add(DataChangeEventBO.class, "Deletes.EntityType")
                                           .build();
        prefetchHandle.prefetch(bos);
        for (int i = 0, size = bos.size(); i < size; i++) {
            var bo = bos.get(i);
            var entity = toEntity(bo);
            retrieved.add(entity);
        }

        return retrieved;
    }

    protected void removeExpired() {
        if (keepEventsForMillis == KEEP_EVENTS_FOREVER) {
            return;
        }
        long time = System.currentTimeMillis() - keepEventsForMillis;
        dataChangeEventDAO.removeBefore(time);
    }

    protected void addAllEntityTypes(ISet<Class<?>> entityTypes, List<IDataChangeEntry> dataChangeEntries) {
        if (dataChangeEntries == null) {
            return;
        }
        for (int i = dataChangeEntries.size(); i-- > 0; ) {
            var dataChangeEntry = dataChangeEntries.get(i);
            entityTypes.add(dataChangeEntry.getEntityType());
        }
    }

    protected IMap<Class<?>, EntityType> loadEntityTypes(ISet<Class<?>> entityTypes) {
        var classToEntityType = new HashMap<Class<?>, EntityType>();

        var metaData = entityMetaDataProvider.getMetaData(EntityType.class);
        var idIndex = metaData.getIdIndexByMemberName("Type");
        var orisToGet = new IObjRef[entityTypes.size()];
        var index = 0;
        for (var entry : entityTypes) {
            var objRef = new ObjRef(EntityType.class, idIndex, entry, null);
            orisToGet[index++] = objRef;
        }
        var retrieved = cache.getObjects(orisToGet, CacheDirective.none());
        for (int i = retrieved.size(); i-- > 0; ) {
            var entityType = (EntityType) retrieved.get(i);
            classToEntityType.put(entityType.getType(), entityType);
        }

        return classToEntityType;
    }

    private List<DataChangeEntryBO> toDataChangeEntryList(List<IDataChangeEntry> dataChangeEntries, IMap<Class<?>, EntityType> classToEntityType) {
        if (dataChangeEntries == null) {
            return null;
        }
        var entityFactory = this.entityFactory;
        var dataChangeEntryList = new ArrayList<DataChangeEntryBO>(dataChangeEntries.size());
        for (int i = 0, size = dataChangeEntries.size(); i < size; i++) {
            var dataChangeEntry = dataChangeEntries.get(i);

            var bo = entityFactory.createEntity(DataChangeEntryBO.class);
            var entityClass = dataChangeEntry.getEntityType();
            var entityType = classToEntityType.get(entityClass);
            if (entityType == null) {
                entityType = entityFactory.createEntity(EntityType.class);
                entityType.setType(entityClass);
                classToEntityType.put(entityClass, entityType);
            }
            bo.setEntityType(entityType);
            bo.setIdIndex(dataChangeEntry.getIdNameIndex());
            bo.setObjectId(dataChangeEntry.getId().toString());
            bo.setObjectVersion(dataChangeEntry.getVersion().toString());

            dataChangeEntryList.add(bo);
        }

        return dataChangeEntryList;
    }

    private DataChangeEvent toEntity(DataChangeEventBO bo) {
        var dce = new DataChangeEvent();
        dce.setChangeTime(bo.getChangeTime());
        dce.setInserts(toDataChangeEntries(bo.getInserts()));
        dce.setUpdates(toDataChangeEntries(bo.getUpdates()));
        dce.setDeletes(toDataChangeEntries(bo.getDeletes()));

        return dce;
    }

    private List<IDataChangeEntry> toDataChangeEntries(List<DataChangeEntryBO> bos) {
        var dataChangeEntries = new ArrayList<IDataChangeEntry>(bos.size());

        for (int i = 0, size = bos.size(); i < size; i++) {
            var bo = bos.get(i);
            var entry = new DataChangeEntry();
            var entityType = bo.getEntityType().getType();
            entry.setEntityType(entityType);
            var idIndex = bo.getIdIndex();
            entry.setIdNameIndex(idIndex);

            var metaData = entityMetaDataProvider.getMetaData(entityType);
            Object id = bo.getObjectId();
            var idMember = metaData.getIdMemberByIdIndex(idIndex);
            id = conversionHelper.convertValueToType(idMember.getRealType(), id);
            entry.setId(id);
            Object version = bo.getObjectVersion();
            var versionMember = metaData.getVersionMember();
            version = conversionHelper.convertValueToType(versionMember.getRealType(), version);
            entry.setVersion(version);

            dataChangeEntries.add(entry);
        }
        return dataChangeEntries;
    }
}
