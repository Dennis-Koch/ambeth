package com.koch.ambeth.persistence.parallel;

/*-
 * #%L
 * jambeth-persistence
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;

import com.koch.ambeth.ioc.threadlocal.IForkState;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import com.koch.ambeth.util.ParamHolder;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;

public class RunnableHandle<V> {
	public final IBackgroundWorkerParamDelegate<V> run;

	public final Lock parallelLock;

	public final CountDownLatch latch;

	public final IForkState forkState;

	public final ParamHolder<Throwable> exHolder;

	public final IList<V> items;

	public final IThreadLocalCleanupController threadLocalCleanupController;

	public RunnableHandle(IBackgroundWorkerParamDelegate<V> run, Lock parallelLock,
			CountDownLatch latch, IForkState forkState, ParamHolder<Throwable> exHolder, IList<V> items,
			IThreadLocalCleanupController threadLocalCleanupController) {
		this.run = run;
		this.parallelLock = parallelLock;
		this.latch = latch;
		this.forkState = forkState;
		this.exHolder = exHolder;
		this.items = items;
		this.threadLocalCleanupController = threadLocalCleanupController;
	}
}
