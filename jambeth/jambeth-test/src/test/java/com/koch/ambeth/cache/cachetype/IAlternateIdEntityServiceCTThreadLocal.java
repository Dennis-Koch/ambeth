package com.koch.ambeth.cache.cachetype;

/*-
 * #%L
 * jambeth-test
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

import com.koch.ambeth.cache.CacheContext;
import com.koch.ambeth.cache.CacheType;
import com.koch.ambeth.merge.proxy.PersistenceContext;
import com.koch.ambeth.persistence.jdbc.alternateid.IAlternateIdEntityService;
import com.koch.ambeth.service.proxy.Service;

@Service(IAlternateIdEntityServiceCTThreadLocal.class)
@PersistenceContext
@CacheContext(CacheType.THREAD_LOCAL)
public interface IAlternateIdEntityServiceCTThreadLocal extends IAlternateIdEntityService
{
	// Intended blank
}
