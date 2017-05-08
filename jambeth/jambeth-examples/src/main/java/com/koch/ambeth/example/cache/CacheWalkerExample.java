package com.koch.ambeth.example.cache;

/*-
 * #%L
 * jambeth-examples
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

import com.koch.ambeth.cache.walker.ICacheWalker;
import com.koch.ambeth.cache.walker.ICacheWalkerResult;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.merge.cache.CacheDirective;
import com.koch.ambeth.merge.cache.ICache;
import com.koch.ambeth.service.merge.model.IObjRef;

public class CacheWalkerExample {
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICacheWalker cacheWalker;

	@Autowired
	protected ICache cache;

	public Object loadEntity(IObjRef objRef) {
		Object entity = cache.getObject(objRef, CacheDirective.none());
		ICacheWalkerResult walkerResult = cacheWalker.walk(objRef);
		if (log.isInfoEnabled()) {
			log.info(walkerResult.toString());
		}
		return entity;
	}
}
