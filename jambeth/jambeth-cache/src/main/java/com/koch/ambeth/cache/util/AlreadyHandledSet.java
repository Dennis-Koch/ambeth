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

import com.koch.ambeth.util.collections.Tuple2KeyEntry;
import com.koch.ambeth.util.collections.Tuple2KeyHashMap;

public class AlreadyHandledSet extends Tuple2KeyHashMap<Object, PrefetchPath[], Boolean> {
	@Override
	protected boolean equalKeys(Object obj, PrefetchPath[] prefetchPaths,
			Tuple2KeyEntry<Object, PrefetchPath[], Boolean> entry) {
		return obj == entry.getKey1() && prefetchPaths == entry.getKey2();
	}

	@Override
	protected int extractHash(Object obj, PrefetchPath[] prefetchPaths) {
		if (prefetchPaths == null) {
			return System.identityHashCode(obj);
		}
		return System.identityHashCode(obj) ^ prefetchPaths.hashCode();
	}
}
