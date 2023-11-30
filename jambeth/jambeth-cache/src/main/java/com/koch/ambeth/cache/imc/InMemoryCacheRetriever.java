package com.koch.ambeth.cache.imc;

/*-
 * #%L
 * jambeth-cache
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

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.cache.service.ICacheRetriever;
import com.koch.ambeth.cache.service.ICacheService;
import com.koch.ambeth.cache.transfer.LoadContainer;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.merge.IMergeServiceExtension;
import com.koch.ambeth.merge.copy.IObjectCopier;
import com.koch.ambeth.merge.incremental.IIncrementalMergeState;
import com.koch.ambeth.merge.model.ICUDResult;
import com.koch.ambeth.merge.model.IOriCollection;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.cache.model.IServiceResult;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.IValueObjectConfig;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.model.IServiceDescription;
import com.koch.ambeth.util.IConversionHelper;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.model.IMethodDescription;

public class InMemoryCacheRetriever implements ICacheRetriever, IMergeServiceExtension, ICacheService {
    protected final HashMap<IObjRef, ILoadContainer> databaseMap = new HashMap<>();
    protected final Lock writeLock = new ReentrantLock();
    @Autowired
    protected IConversionHelper conversionHelper;
    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;
    @Autowired
    protected IObjectCopier objectCopier;

    void addWithKey(LoadContainer lc, String alternateIdMember, Object alternateId) {
        IObjRef reference = lc.getReference();
        IEntityMetaData metaData = entityMetaDataProvider.getMetaData(reference.getRealType());
        byte idIndex = metaData.getIdIndexByMemberName(alternateIdMember);
        alternateId = conversionHelper.convertValueToType(metaData.getAlternateIdMembers()[idIndex].getRealType(), alternateId);
        databaseMap.put(new ObjRef(reference.getRealType(), idIndex, alternateId, reference.getVersion()), lc);
    }

    public IInMemoryConfig add(Class<?> entityType, Object primaryId) {
        return add(entityType, primaryId, null);
    }

    public IInMemoryConfig add(Class<?> entityType, Object primaryId, Object version) {
        IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType);
        LoadContainer lc = new LoadContainer();
        lc.setPrimitives(new Object[metaData.getPrimitiveMembers().length]);
        lc.setRelations(new IObjRef[metaData.getRelationMembers().length][]);

        primaryId = conversionHelper.convertValueToType(metaData.getIdMember().getRealType(), primaryId);
        if (metaData.getVersionMember() != null) {
            version = conversionHelper.convertValueToType(metaData.getVersionMember().getRealType(), version);
        }
        lc.setReference(new ObjRef(entityType, ObjRef.PRIMARY_KEY_INDEX, primaryId, version));

        databaseMap.put(lc.getReference(), lc);

        return new InMemoryEntryConfig(this, metaData, lc);
    }

    @Override
    public List<ILoadContainer> getEntities(List<IObjRef> orisToLoad) {
        List<ILoadContainer> result = new ArrayList<>(orisToLoad.size());
        writeLock.lock();
        try {
            for (IObjRef oriToLoad : orisToLoad) {
                ILoadContainer lc = databaseMap.get(oriToLoad);
                if (lc == null) {
                    continue;
                }
                result.add(lc);
            }
            result = objectCopier.clone(result);
        } finally {
            writeLock.unlock();
        }
        return result;
    }

    @Override
    public List<IObjRelationResult> getRelations(List<IObjRelation> objRelations) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ICUDResult evaluateImplictChanges(ICUDResult cudResult, IIncrementalMergeState incrementalState) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IOriCollection merge(ICUDResult cudResult, String[] causingUuids, IMethodDescription methodDescription) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<IEntityMetaData> getMetaData(List<Class<?>> entityTypes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IValueObjectConfig getValueObjectConfig(Class<?> valueType) {
        throw new UnsupportedOperationException();
    }

    public Class<?> getTargetProviderType(Class<?> clientInterface) {
        throw new UnsupportedOperationException();
    }

    public Class<?> getSyncInterceptorType(Class<?> clientInterface) {
        throw new UnsupportedOperationException();
    }

    public String getServiceName(Class<?> clientInterface) {
        throw new UnsupportedOperationException();
    }

    public void postProcessTargetProviderBean(String targetProviderBeanName, IBeanContextFactory beanContextFactory) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IServiceResult getORIsForServiceRequest(IServiceDescription serviceDescription) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String createMetaDataDOT() {
        throw new UnsupportedOperationException();
    }
}
