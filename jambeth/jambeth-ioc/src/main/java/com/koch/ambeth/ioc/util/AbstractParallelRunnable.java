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

public abstract class AbstractParallelRunnable<V> implements Runnable {
	protected final boolean buildThreadLocals;

	private final AbstractRunnableHandle<V> abstractRunnableHandle;

	public AbstractParallelRunnable(AbstractRunnableHandle<V> abstractRunnableHandle,
			boolean buildThreadLocals) {
		this.abstractRunnableHandle = abstractRunnableHandle;
		this.buildThreadLocals = buildThreadLocals;
	}

	protected V retrieveNextItem() {
		AbstractRunnableHandle<V> runnableHandle = this.abstractRunnableHandle;
		Lock parallelLock = runnableHandle.parallelLock;
		parallelLock.lock();
		try {
			if (runnableHandle.getValue() != null) {
				// an uncatched error occurred somewhere
				return null;
			}
			// pop the last item of the queue
			return runnableHandle.items.popLastElement();
		}
		finally {
			parallelLock.unlock();
		}
	}

	protected void handleThrowable(Throwable e) {
		AbstractRunnableHandle<V> runnableHandle = this.abstractRunnableHandle;
		Lock parallelLock = runnableHandle.parallelLock;
		parallelLock.lock();
		try {
			if (runnableHandle.getValue() == null) {
				runnableHandle.setValue(e);
			}
		}
		finally {
			parallelLock.unlock();
		}
	}

	@Override
	public final void run() {
		Thread currentThread = Thread.currentThread();
		String oldName = currentThread.getName();
		if (buildThreadLocals) {
			String name = abstractRunnableHandle.createdThread.getName();
			currentThread.setName(name + " " + oldName);
		}
		try {
			CountDownLatch latch = abstractRunnableHandle.latch;

			while (true) {
				V item = retrieveNextItem();
				if (item == null) {
					// queue finished
					return;
				}
				try {
					runIntern(item);
				}
				catch (Throwable e) {
					handleThrowable(e);
				}
				finally {
					latch.countDown();
				}
			}
		}
		finally {
			if (buildThreadLocals) {
				currentThread.setName(oldName);
			}
			Thread.interrupted();
		}
	}

	protected abstract void runIntern(V item) throws Throwable;

}
