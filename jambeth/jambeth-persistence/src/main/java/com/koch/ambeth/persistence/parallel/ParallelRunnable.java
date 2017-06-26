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
import com.koch.ambeth.util.ParamHolder;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;

public class ParallelRunnable<V> implements Runnable {
	protected final RunnableHandle<V> runnableHandle;

	protected final boolean buildThreadLocals;

	public ParallelRunnable(RunnableHandle<V> runnableHandle, boolean buildThreadLocals) {
		this.runnableHandle = runnableHandle;
		this.buildThreadLocals = buildThreadLocals;
	}

	@Override
	public void run() {
		try {
			final Lock parallelLock = runnableHandle.parallelLock;
			IList<V> items = runnableHandle.items;
			IForkState forkState = runnableHandle.forkState;
			ParamHolder<Throwable> exHolder = runnableHandle.exHolder;
			CountDownLatch latch = runnableHandle.latch;

			IBackgroundWorkerParamDelegate<V> run = new IBackgroundWorkerParamDelegate<V>() {
				@Override
				public void invoke(V state) throws Exception {
					runnableHandle.run.invoke(state);
					writeParallelResult(parallelLock, state);
				}
			};

			while (true) {
				V item;
				parallelLock.lock();
				try {
					if (exHolder.getValue() != null) {
						// an uncatched error occurred somewhere
						return;
					}
					// pop the last item of the queue
					item = items.popLastElement();
				}
				finally {
					parallelLock.unlock();
				}
				if (item == null) {
					// queue finished
					return;
				}
				try {
					if (buildThreadLocals) {
						forkState.use(run, item);
					}
					else {
						run.invoke(item);
					}
				}
				catch (Throwable e) {
					parallelLock.lock();
					try {
						if (exHolder.getValue() == null) {
							exHolder.setValue(e);
						}
					}
					finally {
						parallelLock.unlock();
					}
				}
				finally {
					latch.countDown();
				}
			}
		}
		finally {
			if (buildThreadLocals) {
				runnableHandle.threadLocalCleanupController.cleanupThreadLocal();
			}
		}
	}

	protected void writeParallelResult(Lock parallelLock, V item) {
		// intended blank
	}
}
