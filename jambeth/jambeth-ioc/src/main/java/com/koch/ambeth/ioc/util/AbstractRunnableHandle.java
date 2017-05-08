package com.koch.ambeth.ioc.util;

/*-
 * #%L
 * jambeth-ioc
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
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.ioc.threadlocal.IForkState;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import com.koch.ambeth.util.collections.ArrayList;

public abstract class AbstractRunnableHandle<V> extends InterruptingParamHolder {
	public final Lock parallelLock = new ReentrantLock();

	public final CountDownLatch latch;

	public final IForkState forkState;

	public final ArrayList<V> items;

	public final IThreadLocalCleanupController threadLocalCleanupController;

	public final Thread createdThread = Thread.currentThread();

	public AbstractRunnableHandle(ArrayList<V> items,
			IThreadLocalCleanupController threadLocalCleanupController) {
		super(Thread.currentThread());
		this.latch = new CountDownLatch(items.size());
		this.items = items;
		this.threadLocalCleanupController = threadLocalCleanupController;
		this.forkState = threadLocalCleanupController.createForkState();
	}
}
