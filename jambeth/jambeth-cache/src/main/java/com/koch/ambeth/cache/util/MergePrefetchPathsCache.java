package com.koch.ambeth.cache.util;

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

import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IEntityMetaData;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.collections.Tuple3KeyHashMap;

public class MergePrefetchPathsCache {
	public static final PrefetchPath[] EMPTY_PREFETCH_PATHS = new PrefetchPath[0];

	private final IEntityMetaDataProvider entityMetaDataProvider;

	private final Tuple3KeyHashMap<Class<?>, PrefetchPath[], IMap<Class<?>, PrefetchPath[]>, PrefetchPath[]> prefetchPathsMap =
			new Tuple3KeyHashMap<>();

	public MergePrefetchPathsCache(IEntityMetaDataProvider entityMetaDataProvider) {
		this.entityMetaDataProvider = entityMetaDataProvider;
	}

	public PrefetchPath[] mergePrefetchPaths(Class<?> entityType, PrefetchPath[] relativePrefetchPath,
			IMap<Class<?>, PrefetchPath[]> entityTypeToPrefetchPaths) {
		if (entityTypeToPrefetchPaths == null) {
			return EMPTY_PREFETCH_PATHS;
		}
		if (relativePrefetchPath == null) {
			relativePrefetchPath = EMPTY_PREFETCH_PATHS;
		}
		PrefetchPath[] prefetchPaths =
				prefetchPathsMap.get(entityType, relativePrefetchPath, entityTypeToPrefetchPaths);
		if (prefetchPaths != null) {
			return prefetchPaths;
		}
		IEntityMetaData metaData = entityMetaDataProvider.getMetaData(entityType, true);
		if (metaData == null) {
			prefetchPathsMap.put(entityType, relativePrefetchPath, entityTypeToPrefetchPaths,
					relativePrefetchPath);
			return relativePrefetchPath;
		}
		PrefetchPath[] absolutePrefetchPath = entityTypeToPrefetchPaths.get(metaData.getEntityType());
		if (absolutePrefetchPath == null) {
			prefetchPathsMap.put(entityType, relativePrefetchPath, entityTypeToPrefetchPaths,
					relativePrefetchPath);
			return relativePrefetchPath;
		}
		if (relativePrefetchPath.length == 0) {
			prefetchPathsMap.put(entityType, relativePrefetchPath, entityTypeToPrefetchPaths,
					absolutePrefetchPath);
			return absolutePrefetchPath;
		}
		HashMap<String, PrefetchPath> tempPrefetchPaths =
				HashMap.create(relativePrefetchPath.length + absolutePrefetchPath.length);
		for (PrefetchPath prefetchPath : relativePrefetchPath) {
			tempPrefetchPaths.putIfNotExists(prefetchPath.memberName, prefetchPath);
		}
		for (PrefetchPath prefetchPath : absolutePrefetchPath) {
			tempPrefetchPaths.putIfNotExists(prefetchPath.memberName, prefetchPath);
		}
		prefetchPaths = tempPrefetchPaths.toArray(PrefetchPath.class);
		prefetchPathsMap.put(entityType, relativePrefetchPath, entityTypeToPrefetchPaths,
				prefetchPaths);
		return prefetchPaths;
	}
}
