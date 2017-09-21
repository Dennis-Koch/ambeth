package com.koch.ambeth.util;

/*-
 * #%L
 * jambeth-cache-test
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

import com.koch.ambeth.cache.util.AppendableCachePath;
import com.koch.ambeth.cache.util.ICachePathHelper;
import com.koch.ambeth.cache.util.PrefetchPath;
import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.merge.util.ICacheHelper;
import com.koch.ambeth.merge.util.IPrefetchState;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.ISet;

public class CacheHelperFake implements ICacheHelper, ICachePathHelper {
	public Class<?> entityType;
	public String memberToInitialize;
	public ISet<AppendableCachePath> cachePaths;

	@Autowired
	protected IServiceContext beanContext;

	@Override
	public void buildCachePath(Class<?> entityType, String memberToInitialize,
			ISet<AppendableCachePath> cachePaths) {
		this.entityType = entityType;
		this.memberToInitialize = memberToInitialize;
		this.cachePaths = cachePaths;
	}

	@Override
	public Object createInstanceOfTargetExpectedType(Class<?> expectedType, Class<?> elementType) {
		return null;
	}

	@Override
	public Object convertResultListToExpectedType(List<Object> resultList, Class<?> expectedType,
			Class<?> elementType) {
		return null;
	}

	@Override
	public Object[] extractPrimitives(IEntityMetaData metaData, Object obj) {
		return null;
	}

	@Override
	public IObjRef[][] extractRelations(IEntityMetaData metaData, Object obj) {
		return null;
	}

	@Override
	public IObjRef[][] extractRelations(IEntityMetaData metaData, Object obj,
			List<Object> relationValues) {
		return null;
	}

	@Override
	public PrefetchPath copyAppendableToCachePath(AppendableCachePath cachePath) {
		return null;
	}

	@Override
	public PrefetchPath[] copyAppendableToCachePath(ISet<AppendableCachePath> children) {
		return null;
	}

	@Override
	public AppendableCachePath copyCachePathToAppendable(PrefetchPath cachePath) {
		return null;
	}

	@Override
	public IPrefetchState ensureInitializedRelations(Object objects,
			ILinkedMap<Class<?>, PrefetchPath[]> entityTypeToPrefetchSteps) {
		return null;
	}

	@Override
	public void unionCachePath(AppendableCachePath cachePath, AppendableCachePath other) {
	}
}
