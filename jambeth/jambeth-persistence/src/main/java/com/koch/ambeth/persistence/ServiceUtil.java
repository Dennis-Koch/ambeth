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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.merge.compositeid.CompositeIdMember;
import com.koch.ambeth.merge.compositeid.ICompositeIdFactory;
import com.koch.ambeth.merge.metadata.IObjRefFactory;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.persistence.api.IDatabaseMetaData;
import com.koch.ambeth.persistence.api.ILinkCursor;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.query.persistence.IVersionCursor;
import com.koch.ambeth.query.persistence.IVersionItem;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.IPreparedConverter;
import com.koch.ambeth.util.collections.ArrayList;

import java.util.List;

public class ServiceUtil implements IServiceUtil {
    @Autowired
    protected ICache cache;

    @Autowired
    protected ICompositeIdFactory compositeIdFactory;

    @Autowired
    protected IEntityLoader entityLoader;

    @Autowired
    protected IConversionHelper conversionHelper;

    @Autowired
    protected IDatabaseMetaData databaseMetaData;

    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;

    @Autowired
    protected IObjRefFactory objRefFactory;

    @SuppressWarnings("unchecked")
    @Override
    public <T> void loadObjectsIntoCollection(List<T> targetEntities, Class<T> entityType, IVersionCursor cursor) {
        if (cursor == null) {
            return;
        }
        try {
            var conversionHelper = this.conversionHelper;
            var tableMetaData = databaseMetaData.getTableByType(entityType);
            var metaData = entityMetaDataProvider.getMetaData(entityType);
            var versionConverter = metaData.getVersionMember() != null ? conversionHelper.prepareConverter(metaData.getVersionMember().getElementType()) : null;
            var preparedCompositeIdFactory = prepareCompositeIdFactory(metaData, tableMetaData);
            var preparedObjRefFactory = objRefFactory.prepareObjRefFactory(entityType, ObjRef.PRIMARY_KEY_INDEX);

            var objRefs = new ArrayList<IObjRef>();
            for (var item : cursor) {
                var id = preparedCompositeIdFactory.convertValue(item.getId(), null);
                var version = versionConverter != null ? versionConverter.convertValue(item.getVersion(), null) : null;
                var objRef = preparedObjRefFactory.createObjRef(id, version);
                objRefs.add(objRef);
            }
            var objects = cache.getObjects(objRefs, CacheDirective.none());
            for (int a = 0, size = objects.size(); a < size; a++) {
                targetEntities.add((T) objects.get(a));
            }
        } finally {
            cursor.dispose();
        }
    }

    private IPreparedConverter<?> prepareCompositeIdFactory(IEntityMetaData metaData, ITableMetaData table) {
        var conversionHelper = this.conversionHelper;
        var idFields = table.getIdFields();
        var idMember = metaData.getIdMember();
        if (!Object.class.equals(idMember.getElementType())) {
            return compositeIdFactory.prepareCompositeIdFactory(metaData, idMember);
        }
        // INTENTIONALLY converting the id in 2 steps: first to field.fieldType, then to idMember.elementType
        // this is because of the fact that the idTypeOfObject may be an Object.class which does not convert the id correctly by itself
        if (idFields.length == 1) {
            var tableIdConverter = conversionHelper.prepareConverter(idFields[0].getFieldType());
            var cacheIdConverter = conversionHelper.prepareConverter(idMember.getElementType());
            return (value, additionalInformation) -> {
                var tableId = tableIdConverter.convertValue(value, additionalInformation);
                return cacheIdConverter.convertValue(tableId, additionalInformation);
            };
        }
        var compositeIdMember = (CompositeIdMember) idMember;
        var compositeIdMembers = compositeIdMember.getMembers();
        var tableIdConverters = new IPreparedConverter<?>[idFields.length];
        var cacheIdConverters = new IPreparedConverter<?>[idFields.length];
        for (int compositeIdIndex = idFields.length; compositeIdIndex-- > 0; ) {
            tableIdConverters[compositeIdIndex] = conversionHelper.prepareConverter(idFields[compositeIdIndex].getFieldType());
            cacheIdConverters[compositeIdIndex] = conversionHelper.prepareConverter(compositeIdMembers[compositeIdIndex].getElementType());
        }
        var nestedPreparedCompositeIdFactory = compositeIdFactory.prepareCompositeIdFactory(metaData, compositeIdMember);

        return (value, additionalInformation) -> {
            var ids = (Object[]) value;
            for (int compositeIdIndex = idFields.length; compositeIdIndex-- > 0; ) {
                var tableId = tableIdConverters[compositeIdIndex].convertValue(ids[compositeIdIndex], null);
                ids[compositeIdIndex] = cacheIdConverters[compositeIdIndex].convertValue(tableId, null);
            }
            return nestedPreparedCompositeIdFactory.convertValue(ids, null);
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T loadObject(Class<T> entityType, IVersionItem item) {
        if (item == null) {
            return null;
        }
        var metaData = entityMetaDataProvider.getMetaData(entityType);
        var idTypeOfObject = metaData.getIdMember().getRealType();
        var versionTypeOfObject = metaData.getVersionMember() != null ? metaData.getVersionMember().getRealType() : null;

        var id = conversionHelper.convertValueToType(idTypeOfObject, item.getId());
        var version = conversionHelper.convertValueToType(versionTypeOfObject, item.getVersion());
        var objRef = objRefFactory.createObjRef(entityType, ObjRef.PRIMARY_KEY_INDEX, id, version);
        return (T) cache.getObject(objRef, CacheDirective.none());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> void loadObjects(java.util.List<T> targetEntities, java.lang.Class<T> entityType, ILinkCursor cursor) {
        if (cursor == null) {
            return;
        }
        var metaData = entityMetaDataProvider.getMetaData(entityType);
        var idTypeOfObject = metaData.getIdMember().getRealType();
        var preparedObjRefFactory = objRefFactory.prepareObjRefFactory(entityType, ObjRef.PRIMARY_KEY_INDEX);
        var objRefs = new ArrayList<IObjRef>();
        try {
            for (var item : cursor) {
                var toId = conversionHelper.convertValueToType(idTypeOfObject, item.getToId());
                var objRef = preparedObjRefFactory.createObjRef(toId, null);
                objRefs.add(objRef);
            }
            var objects = cache.getObjects(objRefs, CacheDirective.none());
            for (int a = 0, size = objects.size(); a < size; a++) {
                targetEntities.add((T) objects.get(a));
            }
        } finally {
            cursor.dispose();
        }
    }

    @Override
    public List<IObjRef> loadObjRefs(Class<?> entityType, int idIndex, IVersionCursor cursor) {
        try {
            var objRefs = new ArrayList<IObjRef>();
            var preparedObjRefFactory = objRefFactory.prepareObjRefFactory(entityType, idIndex);
            for (var item : cursor) {
                objRefs.add(preparedObjRefFactory.createObjRef(item.getId(idIndex), item.getVersion()));
            }
            return objRefs;
        } finally {
            cursor.dispose();
        }
    }
}
