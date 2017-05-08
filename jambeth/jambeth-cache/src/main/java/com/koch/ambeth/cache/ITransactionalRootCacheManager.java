package com.koch.ambeth.cache;

import com.koch.ambeth.cache.interceptor.TransactionalRootCacheInterceptor;

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
/**
 * Handles the lifecycle of a transactional 2nd level cache. The transactional 2nd level cache
 * injects itself into the cache hierarchy between the 1st level cache and the (committed-state) 2nd
 * level cache for the duration of it lifecycle.<br>
 * <br>
 * Technically there is a proxy object injected into each 1st level cache to redirect all its 2nd
 * level cache request either to the committed-state or transactional 2nd level cache - depending on
 * the transaction state of the current thread (see
 * {@link TransactionalRootCacheInterceptor#selectSecondLevelCache()}). This applies to both
 * privileged as well as non-privileged 1st level caches (getting their call forwarded to the
 * corresponding transactional privileged or transactional non-privileged 2nd level cache).
 */
public interface ITransactionalRootCacheManager {
	void acquireTransactionalRootCache();

	void disposeTransactionalRootCache(boolean success);
}
