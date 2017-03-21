package com.koch.ambeth.cache.datachange;

/*-
 * #%L
 * jambeth-cache-datachange
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

import com.koch.ambeth.cache.rootcachevalue.RootCacheValue;
import com.koch.ambeth.security.privilege.model.IPrivilege;

public class CacheValueAndPrivilege
{
	public final RootCacheValue cacheValue;

	public final IPrivilege privilege;

	public CacheValueAndPrivilege(RootCacheValue cacheValue, IPrivilege privilege)
	{
		this.cacheValue = cacheValue;
		this.privilege = privilege;
	}
}
