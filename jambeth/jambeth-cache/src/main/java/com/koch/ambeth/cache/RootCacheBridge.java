package com.koch.ambeth.cache;

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

import com.koch.ambeth.cache.service.ICacheRetriever;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.ITransactionState;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.service.cache.model.ILoadContainer;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.Member;
import com.koch.ambeth.util.IInterningFeature;
import com.koch.ambeth.util.collections.ArrayList;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class RootCacheBridge implements ICacheRetriever {
    protected static final Set<CacheDirective> committedRootCacheCD = EnumSet.of(CacheDirective.FailEarly, CacheDirective.ReturnMisses, CacheDirective.LoadContainerResult);

    @Autowired
    protected IRootCache committedRootCache;

    @Autowired
    protected IEntityMetaDataProvider entityMetaDataProvider;

    @Autowired
    protected ICacheRetriever uncommittedCacheRetriever;

    @Autowired
    protected IInterningFeature interningFeature;

    @Autowired(optional = true)
    protected ITransactionState transactionState;

    @Override
    public List<ILoadContainer> getEntities(List<IObjRef> orisToLoad) {
        var isTransaction = false;
        if (transactionState != null) {
            isTransaction = transactionState.isTransactionActive();
        }
        if (!isTransaction) {
            // Allow committed root cache only OUT OF transactions to retrieve data by itself
            var loadContainers = committedRootCache.getObjects(orisToLoad, CacheDirective.loadContainerResult());
            var result = new ArrayList<ILoadContainer>(loadContainers.size());
            for (int a = loadContainers.size(); a-- > 0; ) {
                result.add((ILoadContainer) loadContainers.get(a));
            }
            internStrings(result);
            return result;
        }
        var orisToLoadWithVersion = new ArrayList<IObjRef>(orisToLoad.size());
        var missedOris = new ArrayList<IObjRef>(orisToLoad.size());
        for (int i = orisToLoad.size(); i-- > 0; ) {
            var ori = orisToLoad.get(i);
            if (ori.getVersion() != null) {
                orisToLoadWithVersion.add(ori);
            } else {
                missedOris.add(ori);
            }
        }
        var result = new ArrayList<ILoadContainer>(orisToLoadWithVersion.size());
        if (!orisToLoadWithVersion.isEmpty()) {
            var loadContainers = committedRootCache.getObjects(orisToLoadWithVersion, committedRootCacheCD);
            for (int a = loadContainers.size(); a-- > 0; ) {
                var loadContainer = (ILoadContainer) loadContainers.get(a);
                if (loadContainer == null) {
                    missedOris.add(orisToLoadWithVersion.get(a));
                } else {
                    result.add(loadContainer);
                }
            }
        }
        if (!missedOris.isEmpty()) {
            var uncommittedLoadContainer = uncommittedCacheRetriever.getEntities(missedOris);
            result.addAll(uncommittedLoadContainer);
        }
        internStrings(result);
        return result;
    }

    @Override
    public List<IObjRelationResult> getRelations(List<IObjRelation> objRelations) {
        var orelToLoadWithVersion = new ArrayList<IObjRelation>();
        var missedOrels = new ArrayList<IObjRelation>();
        for (int i = objRelations.size(); i-- > 0; ) {
            var orel = objRelations.get(i);
            if (orel.getVersion() != null) {
                orelToLoadWithVersion.add(orel);
            } else {
                missedOrels.add(orel);
            }
        }
        var relationResults = committedRootCache.getObjRelations(orelToLoadWithVersion, committedRootCacheCD);
        var result = new ArrayList<IObjRelationResult>(objRelations.size());
        for (int a = relationResults.size(); a-- > 0; ) {
            var relationResult = relationResults.get(a);
            if (relationResult == null) {
                missedOrels.add(orelToLoadWithVersion.get(a));
            } else {
                result.add(relationResult);
            }
        }
        if (!missedOrels.isEmpty()) {
            var uncommittedRelationResult = uncommittedCacheRetriever.getRelations(missedOrels);
            result.addAll(uncommittedRelationResult);
        }
        return result;
    }

    protected void internStrings(List<ILoadContainer> loadContainers) {
        for (int a = loadContainers.size(); a-- > 0; ) {
            var loadContainer = loadContainers.get(a);
            var metaData = entityMetaDataProvider.getMetaData(loadContainer.getReference().getRealType());
            var primitives = loadContainer.getPrimitives();
            for (var member : metaData.getPrimitiveMembers()) {
                if (!metaData.hasInterningBehavior(member)) {
                    continue;
                }
                internPrimitiveMember(metaData, primitives, member);
            }
        }
    }

    protected void internPrimitiveMember(IEntityMetaData metaData, Object[] primitives, Member member) {
        if (member == null) {
            return;
        }
        var index = metaData.getIndexByPrimitive(member);
        var value = primitives[index];
        if (value instanceof String) {
            var internValue = interningFeature.intern(value);
            if (value != internValue) {
                primitives[index] = internValue;
            }
        }
    }
}
