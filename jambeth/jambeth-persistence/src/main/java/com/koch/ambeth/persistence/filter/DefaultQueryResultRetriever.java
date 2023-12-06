package com.koch.ambeth.persistence.filter;

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
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.persistence.api.database.ITransaction;
import com.koch.ambeth.query.IQueryIntern;
import com.koch.ambeth.query.filter.IQueryResultCacheItem;
import com.koch.ambeth.query.filter.IQueryResultRetriever;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.util.Arrays;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.IPreparedConverter;
import com.koch.ambeth.util.WrapperTypeSet;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IMap;

import java.lang.reflect.Array;
import java.util.List;

public class DefaultQueryResultRetriever implements IQueryResultRetriever {
    @Autowired
    protected IConversionHelper conversionHelper;

    @Autowired
    protected IMap<Object, Object> currentNameToValueMap;

    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;

    @Autowired
    protected IQueryIntern<?> query;

    @Autowired
    protected ITransaction transaction;

    @Property
    protected int size;

    @Override
    public boolean containsPageOnly() {
        return currentNameToValueMap.containsKey(QueryConstants.PAGING_SIZE_OBJECT);
    }

    @Override
    public List<Class<?>> getRelatedEntityTypes() {
        var relatedEntityTypes = new ArrayList<Class<?>>();
        query.fillRelatedEntityTypes(relatedEntityTypes);
        return relatedEntityTypes;
    }

    @SuppressWarnings("unchecked")
    @Override
    public IQueryResultCacheItem getQueryResult() {
        return transaction.processAndCommitWithResult(persistenceUnitToDatabaseMap -> {
            var conversionHelper = DefaultQueryResultRetriever.this.conversionHelper;
            var query = DefaultQueryResultRetriever.this.query;
            var entityType = query.getEntityType();
            var metaData = entityMetaDataProvider.getMetaData(entityType);
            var alternateIdMembers = metaData.getAlternateIdMembers();
            var length = alternateIdMembers.length + 1;

            var totalSize = query.count(currentNameToValueMap);
            var versionList = new ArrayList<>((int) totalSize);
            var versionType = metaData.getVersionMember() != null ? metaData.getVersionMember().getRealType() : null;
            var cacheVersionConverter = versionType != null ? conversionHelper.prepareConverter(versionType) : null;
            var cacheIdLists = new ArrayList[length];
            var cacheIdTypes = new Class[length];
            var cacheIdConverters = new IPreparedConverter[length];
            for (int a = length; a-- > 0; ) {
                cacheIdLists[a] = new ArrayList<>((int) totalSize);
                cacheIdTypes[a] = metaData.getIdMemberByIdIndex(a - 1).getRealType();
                cacheIdConverters[a] = conversionHelper.prepareConverter(cacheIdTypes[a]);
            }
            if (size != 0) {
                var versionCursor = query.retrieveAsVersions(currentNameToValueMap, true);
                try {
                    for (var versionItem : versionCursor) {
                        for (var idIndex = length; idIndex-- > 0; ) {
                            var id = cacheIdConverters[idIndex].convertValue(versionItem.getId(idIndex - 1), null);
                            cacheIdLists[idIndex].add(id);
                        }
                        var version = cacheVersionConverter != null ? cacheVersionConverter.convertValue(versionItem.getVersion(), null) : null;
                        versionList.add(version);
                    }
                } finally {
                    versionCursor.dispose();
                }
            }
            var idArrays = new Object[length];
            for (int a = length; a-- > 0; ) {
                idArrays[a] = convertListToArray(cacheIdLists[a], cacheIdTypes[a]);
            }
            var versionArray = versionType != null ? convertListToArray(versionList, versionType) : null;
            return new QueryResultCacheItem(entityType, totalSize, cacheIdLists[0].size(), idArrays, versionArray);
        });
    }

    protected Object convertListToArray(List<?> list, Class<?> expectedItemType) {
        if (expectedItemType != null) {
            var unwrappedType = WrapperTypeSet.getUnwrappedType(expectedItemType);
            if (unwrappedType != null) {
                expectedItemType = unwrappedType;
            }
        }
        if (expectedItemType == null) {
            return list.toArray(Object[]::new);
        }
        var array = Array.newInstance(expectedItemType, list.size());
        var preparedArraySet = Arrays.prepareSet(array);
        for (int a = list.size(); a-- > 0; ) {
            preparedArraySet.set(a, list.get(a));
        }
        return array;
    }
}
