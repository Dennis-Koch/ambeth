package com.koch.ambeth.merge.cache;

import com.koch.ambeth.util.state.IStateRollback;

/*-
 * #%L
 * jambeth-merge
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

import com.koch.ambeth.util.threading.IResultingBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerParamDelegate;

public interface ICacheContext {
	<R> R executeWithCache(IResultingBackgroundWorkerDelegate<R> runnable) throws Exception;

	<R> R executeWithCache(ICacheProvider cacheProvider,
			IResultingBackgroundWorkerDelegate<R> runnable) throws Exception;

	<R> R executeWithCache(ICache cache, IResultingBackgroundWorkerDelegate<R> runnable)
			throws Exception;

	<R, T> R executeWithCache(IResultingBackgroundWorkerParamDelegate<R, T> runnable, T state)
			throws Exception;

	<R, T> R executeWithCache(ICacheProvider cacheProvider,
			IResultingBackgroundWorkerParamDelegate<R, T> runnable, T state) throws Exception;

	<R, T> R executeWithCache(ICache cache, IResultingBackgroundWorkerParamDelegate<R, T> runnable,
			T state) throws Exception;

	IStateRollback pushCache(ICache cache, IStateRollback... rollbacks);

	IStateRollback pushCache(ICacheProvider cacheProvider, IStateRollback... rollbacks);
}
