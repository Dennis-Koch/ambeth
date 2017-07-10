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

import com.koch.ambeth.event.IEventTargetExtractor;
import com.koch.ambeth.merge.cache.ICacheProvider;

public class CacheEventTargetExtractor implements IEventTargetExtractor {
	@Override
	public Object extractEventTarget(Object eventTarget) {
		Class<?> targetType = eventTarget.getClass();
		if (ChildCache.class.equals(targetType) || RootCache.class.equals(targetType)) {
			return eventTarget;
		}
		if (ICacheProvider.class.isAssignableFrom(targetType)) {
			ICacheProvider cacheProvider = (ICacheProvider) eventTarget;
			if (cacheProvider.isNewInstanceOnCall()) {
				return null;
			}
			return cacheProvider.getCurrentCache();
		}
		return eventTarget;
	}
}
