package com.koch.ambeth.persistence.sql;

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

import com.koch.ambeth.ioc.IDisposableBean;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.merge.compositeid.ICompositeIdFactory;
import com.koch.ambeth.persistence.api.IPrimaryKeyProvider;
import com.koch.ambeth.persistence.api.ITableMetaData;
import com.koch.ambeth.persistence.config.PersistenceConfigurationConstants;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.IClassCache;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.IPreparedConverter;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.SmartCopyMap;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractCachingPrimaryKeyProvider implements IPrimaryKeyProvider, IDisposableBean {
    protected final SmartCopyMap<String, IPrimaryKeyProvider> seqNameToPrimaryKeyProviderMap = new SmartCopyMap<>(0.5f);
    protected final HashMap<String, ArrayList<Object>> seqToCachedIdsMap = new HashMap<>(0.5f);
    protected final Lock writeLock = new ReentrantLock();
    @Autowired
    protected IServiceContext beanContext;

    @Autowired
    protected IClassCache classCache;

    @Autowired
    protected ICompositeIdFactory compositeIdFactory;

    @Autowired
    protected IConversionHelper conversionHelper;

    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;

    @Property(name = PersistenceConfigurationConstants.SequencePrefetchSize, defaultValue = "200")
    protected int prefetchIdAmount;

    @Override
    public void destroy() throws Throwable {
        for (var entry : seqNameToPrimaryKeyProviderMap) {
            var bean = entry.getValue();
            if (bean instanceof IDisposableBean) {
                ((IDisposableBean) bean).destroy();
            }
        }
        seqNameToPrimaryKeyProviderMap.clear();
    }

    @Override
    public void acquireIds(ITableMetaData table, List<IObjRef> idlessObjRefs) {
        var sequenceName = table.getSequenceName();
        if (sequenceName == null) {
            throw new IllegalStateException("No sequence configured for table " + table);
        }
        var primaryKeyProvider = seqNameToPrimaryKeyProviderMap.get(sequenceName);
        if (primaryKeyProvider != null) {
            primaryKeyProvider.acquireIds(table, idlessObjRefs);
            return;
        }
        var writeLock = this.writeLock;
        writeLock.lock();
        try {
            primaryKeyProvider = seqNameToPrimaryKeyProviderMap.get(sequenceName);
            if (primaryKeyProvider != null) {
                primaryKeyProvider.acquireIds(table, idlessObjRefs);
                return;
            }
            Class<?> customSequenceType = null;
            try {
                customSequenceType = classCache.loadClass(sequenceName);
            } catch (Throwable e) {
                // intended blank
            }
            if (customSequenceType == null) {
                primaryKeyProvider = (tableMetaData, objRefs) -> {
                    var acquiredIds = acquireIdsIntern(tableMetaData, objRefs.size());

                    for (int i = objRefs.size(); i-- > 0; ) {
                        var reference = objRefs.get(i);
                        reference.setId(acquiredIds.get(i));
                        reference.setIdNameIndex(IObjRef.PRIMARY_KEY_INDEX);
                    }
                };
            } else {
                primaryKeyProvider = (IPrimaryKeyProvider) beanContext.registerBean(customSequenceType).finish();
            }
            seqNameToPrimaryKeyProviderMap.put(sequenceName, primaryKeyProvider);
        } finally {
            writeLock.unlock();
        }
        primaryKeyProvider.acquireIds(table, idlessObjRefs);
    }

    protected IList<Object> acquireIdsIntern(ITableMetaData table, int count) {
        if (count == 0) {
            return EmptyList.getInstance();
        }
        var sequenceName = table.getSequenceName();
        if (sequenceName == null) {
            throw new IllegalStateException("No sequence configured for table " + table);
        }
        var ids = new ArrayList<>(count);
        int requestCount = count + prefetchIdAmount;

        var writeLock = this.writeLock;
        ArrayList<Object> cachedIds = null;

        writeLock.lock();
        try {
            cachedIds = seqToCachedIdsMap.get(sequenceName);
            if (cachedIds == null) {
                cachedIds = new ArrayList<>(requestCount);
                seqToCachedIdsMap.put(sequenceName, cachedIds);
            }
            while (count > 0 && cachedIds.size() >= count) {
                var cachedId = cachedIds.popLastElement();
                ids.add(cachedId);
                count--;
            }
        } finally {
            writeLock.unlock();
        }
        if (count == 0) {
            // ids could be fully satisfied by the cache
            return ids;
        }
        var newIds = new ArrayList<>(requestCount);

        // Make sure after the request are still enough ids cached
        acquireIdsIntern(table, requestCount, newIds);

        var idFields = table.getIdFields();
        IPreparedConverter cacheIdConverter;
        if (idFields != null && idFields.length > 0) {
            var metaData = entityMetaDataProvider.getMetaData(table.getEntityType());
            cacheIdConverter = compositeIdFactory.prepareCompositeIdFactory(metaData, metaData.getIdMember());
        } else {
            cacheIdConverter = null;
        }
        if (newIds.size() < requestCount) {
            throw new IllegalStateException("Requested at least " + requestCount + " ids from sequence '" + sequenceName + "' but retrieved only " + newIds.size());
        }
        for (int a = 0; a < count; a++) {
            var id = newIds.get(a);
            id = cacheIdConverter != null ? cacheIdConverter.convertValue(id, null) : id;
            ids.add(id);
        }
        writeLock.lock();
        try {
            for (int a = newIds.size(); a-- > count; ) {
                var id = newIds.get(a);
                id = cacheIdConverter != null ? cacheIdConverter.convertValue(id, null) : id;
                cachedIds.add(id);
            }
        } finally {
            writeLock.unlock();
        }
        return ids;
    }

    protected abstract void acquireIdsIntern(ITableMetaData table, int count, List<Object> targetIdList);
}
