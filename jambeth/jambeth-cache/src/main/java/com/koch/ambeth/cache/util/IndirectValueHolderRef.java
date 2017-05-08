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

import com.koch.ambeth.cache.RootCache;
import com.koch.ambeth.cache.rootcachevalue.RootCacheValue;
import com.koch.ambeth.merge.util.DirectValueHolderRef;
import com.koch.ambeth.service.metadata.RelationMember;

public class IndirectValueHolderRef extends DirectValueHolderRef {
	protected final RootCache rootCache;

	public IndirectValueHolderRef(RootCacheValue cacheValue, RelationMember member,
			RootCache rootCache) {
		super(cacheValue, member);
		this.rootCache = rootCache;
	}

	public IndirectValueHolderRef(RootCacheValue cacheValue, RelationMember member,
			RootCache rootCache, boolean objRefsOnly) {
		super(cacheValue, member, objRefsOnly);
		this.rootCache = rootCache;
	}

	public RootCache getRootCache() {
		return rootCache;
	}
}
