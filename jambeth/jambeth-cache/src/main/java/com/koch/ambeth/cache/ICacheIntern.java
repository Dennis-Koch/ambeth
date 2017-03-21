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

import java.util.List;
import java.util.Set;

import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.service.cache.model.IObjRelation;
import com.koch.ambeth.service.cache.model.IObjRelationResult;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.IList;

public interface ICacheIntern extends ICache {
	Object createCacheValueInstance(IEntityMetaData metaData, Object obj);

	void addDirect(IEntityMetaData metaData, Object id, Object version, Object primitiveFilledObject,
			Object parentCacheValueOrArray, IObjRef[][] relations);

	Object getObject(IObjRef oriToGet, ICacheIntern targetCache, Set<CacheDirective> cacheDirective);

	IList<Object> getObjects(List<IObjRef> orisToGet, ICacheIntern targetCache,
			Set<CacheDirective> cacheDirective);

	IList<IObjRelationResult> getObjRelations(List<IObjRelation> objRelation,
			ICacheIntern targetCache, Set<CacheDirective> cacheDirective);

	boolean acquireHardRefTLIfNotAlready();

	void clearHardRefs(boolean acquirementSuccessful);

	void assignEntityToCache(Object entity);
}
