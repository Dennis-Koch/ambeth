package com.koch.ambeth.cache.mixin;

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

import java.util.Arrays;
import java.util.Set;

import com.koch.ambeth.cache.ICacheIntern;
import com.koch.ambeth.cache.proxy.IValueHolderContainer;
import com.koch.ambeth.cache.transfer.ObjRelation;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.ILightweightTransaction;
import com.koch.ambeth.merge.IObjRefHelper;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ValueHolderState;
import com.koch.ambeth.merge.proxy.IObjRefContainer;
import com.koch.ambeth.merge.util.ICacheHelper;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.service.metadata.RelationMember;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;

public class ValueHolderContainerMixin {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICacheHelper cacheHelper;

	@Autowired
	protected IEntityMetaDataProvider entityMetaDataProvider;

	@Autowired
	protected IObjRefHelper objRefHelper;

	@Autowired(optional = true)
	protected ILightweightTransaction transaction;

	public IObjRelation getSelf(Object entity, String memberName) {
		IList<IObjRef> allObjRefs = objRefHelper.entityToAllObjRefs(entity);
		return new ObjRelation(allObjRefs.toArray(IObjRef.class), memberName);
	}

	public IObjRelation getSelf(IObjRefContainer entity, int relationIndex) {
		String memberName = entity.get__EntityMetaData().getRelationMembers()[relationIndex].getName();
		IList<IObjRef> allObjRefs = objRefHelper.entityToAllObjRefs(entity);
		return new ObjRelation(allObjRefs.toArray(IObjRef.class), memberName);
	}

	public Object getValue(IObjRefContainer entity, RelationMember[] relationMembers,
			int relationIndex, ICacheIntern targetCache, IObjRef[] objRefs) {
		return getValue(entity, relationIndex, relationMembers[relationIndex], targetCache, objRefs,
				CacheDirective.none());
	}

	public Object getValue(IObjRefContainer entity, int relationindex, RelationMember relationMember,
			final ICacheIntern targetCache, IObjRef[] objRefs, final Set<CacheDirective> cacheDirective) {
		Object value;
		if (targetCache == null) {
			// This happens if an entity gets newly created and immediately called for relations (e.g.
			// collections to add sth)
			value = cacheHelper.createInstanceOfTargetExpectedType(relationMember.getRealType(),
					relationMember.getElementType());
		}
		else {
			IList<Object> results;
			if (objRefs == null) {
				final IObjRelation self = getSelf(entity, relationMember.getName());

				if (transaction != null) {
					results = transaction
							.runInLazyTransaction(new IResultingBackgroundWorkerDelegate<IList<Object>>() {
								@Override
								public IList<Object> invoke() throws Throwable {
									IList<IObjRelationResult> objRelResults =
											targetCache.getObjRelations(Arrays.asList(self), targetCache, cacheDirective);
									if (objRelResults.size() == 0) {
										return EmptyList.getInstance();
									}
									else {
										IObjRelationResult objRelResult = objRelResults.get(0);
										return targetCache.getObjects(
												new ArrayList<IObjRef>(objRelResult.getRelations()), targetCache,
												cacheDirective);
									}
								}
							});
				}
				else {
					IList<IObjRelationResult> objRelResults =
							targetCache.getObjRelations(Arrays.asList(self), targetCache, cacheDirective);
					if (objRelResults.size() == 0) {
						results = EmptyList.getInstance();
					}
					else {
						IObjRelationResult objRelResult = objRelResults.get(0);
						results = targetCache.getObjects(new ArrayList<IObjRef>(objRelResult.getRelations()),
								targetCache, cacheDirective);
					}
				}
			}
			else {
				results =
						targetCache.getObjects(new ArrayList<IObjRef>(objRefs), targetCache, cacheDirective);
			}
			value = cacheHelper.convertResultListToExpectedType(results, relationMember.getRealType(),
					relationMember.getElementType());
		}
		return value;
	}

	public Object getValue(IValueHolderContainer vhc, int relationIndex) {
		return getValue(vhc, relationIndex, CacheDirective.none());
	}

	public Object getValue(IValueHolderContainer vhc, int relationIndex,
			Set<CacheDirective> cacheDirective) {
		IEntityMetaData metaData = vhc.get__EntityMetaData();
		RelationMember relationMember = metaData.getRelationMembers()[relationIndex];
		if (ValueHolderState.INIT == vhc.get__State(relationIndex)) {
			return relationMember.getValue(vhc);
		}
		IObjRef[] objRefs = vhc.get__ObjRefs(relationIndex);
		return getValue(vhc, relationIndex, relationMember, vhc.get__TargetCache(), objRefs,
				cacheDirective);
	}
}
