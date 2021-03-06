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

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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
import com.koch.ambeth.util.collections.IList;

public class RootCacheBridge implements ICacheRetriever {
	protected static final Set<CacheDirective> committedRootCacheCD = EnumSet.of(
			CacheDirective.FailEarly, CacheDirective.ReturnMisses, CacheDirective.LoadContainerResult);

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
		boolean isTransaction = false;
		if (transactionState != null) {
			isTransaction = transactionState.isTransactionActive();
		}
		ArrayList<ILoadContainer> result = new ArrayList<>();
		if (!isTransaction) {
			// Allow committed root cache only OUT OF transactions to retrieve data by itself
			IList<Object> loadContainers =
					committedRootCache.getObjects(orisToLoad, CacheDirective.loadContainerResult());
			for (int a = loadContainers.size(); a-- > 0;) {
				result.add((ILoadContainer) loadContainers.get(a));
			}
			internStrings(result);
			return result;
		}
		List<IObjRef> orisToLoadWithVersion = new ArrayList<>();
		List<IObjRef> missedOris = new ArrayList<>();
		for (int i = orisToLoad.size(); i-- > 0;) {
			IObjRef ori = orisToLoad.get(i);
			if (ori.getVersion() != null) {
				orisToLoadWithVersion.add(ori);
			}
			else {
				missedOris.add(ori);
			}
		}
		if (!orisToLoadWithVersion.isEmpty()) {
			IList<Object> loadContainers =
					committedRootCache.getObjects(orisToLoadWithVersion, committedRootCacheCD);
			for (int a = loadContainers.size(); a-- > 0;) {
				ILoadContainer loadContainer = (ILoadContainer) loadContainers.get(a);
				if (loadContainer == null) {
					missedOris.add(orisToLoadWithVersion.get(a));
				}
				else {
					result.add(loadContainer);
				}
			}
		}
		if (!missedOris.isEmpty()) {
			List<ILoadContainer> uncommittedLoadContainer =
					uncommittedCacheRetriever.getEntities(missedOris);
			result.addAll(uncommittedLoadContainer);
		}
		internStrings(result);
		return result;
	}

	@Override
	public List<IObjRelationResult> getRelations(List<IObjRelation> objRelations) {
		List<IObjRelation> orelToLoadWithVersion = new ArrayList<>();
		List<IObjRelation> missedOrels = new ArrayList<>();
		for (int i = objRelations.size(); i-- > 0;) {
			IObjRelation orel = objRelations.get(i);
			if (orel.getVersion() != null) {
				orelToLoadWithVersion.add(orel);
			}
			else {
				missedOrels.add(orel);
			}
		}
		IList<IObjRelationResult> relationResults =
				committedRootCache.getObjRelations(orelToLoadWithVersion, committedRootCacheCD);
		List<IObjRelationResult> result = new ArrayList<>(objRelations.size());
		for (int a = relationResults.size(); a-- > 0;) {
			IObjRelationResult relationResult = relationResults.get(a);
			if (relationResult == null) {
				missedOrels.add(orelToLoadWithVersion.get(a));
			}
			else {
				result.add(relationResult);
			}
		}
		if (!missedOrels.isEmpty()) {
			List<IObjRelationResult> uncommittedRelationResult =
					uncommittedCacheRetriever.getRelations(missedOrels);
			result.addAll(uncommittedRelationResult);
		}
		return result;
	}

	protected void internStrings(List<ILoadContainer> loadContainers) {
		for (int a = loadContainers.size(); a-- > 0;) {
			ILoadContainer loadContainer = loadContainers.get(a);
			IEntityMetaData metaData =
					entityMetaDataProvider.getMetaData(loadContainer.getReference().getRealType());
			Object[] primitives = loadContainer.getPrimitives();
			for (Member member : metaData.getPrimitiveMembers()) {
				if (!metaData.hasInterningBehavior(member)) {
					continue;
				}
				internPrimitiveMember(metaData, primitives, member);
			}
		}
	}

	protected void internPrimitiveMember(IEntityMetaData metaData, Object[] primitives,
			Member member) {
		if (member == null) {
			return;
		}
		int index = metaData.getIndexByPrimitive(member);
		Object value = primitives[index];
		if (value instanceof String) {
			Object internValue = interningFeature.intern(value);
			if (value != internValue) {
				primitives[index] = internValue;
			}
		}
	}
}
