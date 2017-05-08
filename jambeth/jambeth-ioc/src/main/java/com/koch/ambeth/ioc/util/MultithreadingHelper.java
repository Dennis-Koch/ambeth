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

import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import com.koch.ambeth.ioc.IServiceContext;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.cancel.ICancellation;
import com.koch.ambeth.ioc.config.IocConfigurationConstants;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.ioc.threadlocal.IForkState;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import com.koch.ambeth.util.IClassLoaderProvider;
import com.koch.ambeth.util.ParamHolder;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.threading.IBackgroundWorkerDelegate;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;
import com.koch.ambeth.util.threading.IResultingBackgroundWorkerParamDelegate;

public class MultithreadingHelper implements IMultithreadingHelper {
	private class CancellationCheckBackgroundWorker<V> implements IBackgroundWorkerParamDelegate<V> {
		private final IBackgroundWorkerParamDelegate<V> itemHandler;

		private CancellationCheckBackgroundWorker(IBackgroundWorkerParamDelegate<V> itemHandler) {
			this.itemHandler = itemHandler;
		}

		@Override
		public void invoke(final V state) throws Throwable {
			cancellation.withCancellationAwareness(new IBackgroundWorkerDelegate() {
				@Override
				public void invoke() throws Throwable {
					itemHandler.invoke(state);
				}
			});
		}
	}

	private class CancellationCheckResultingBackgroundWorker<R, V>
			implements IResultingBackgroundWorkerParamDelegate<R, V> {
		private final IResultingBackgroundWorkerParamDelegate<R, V> itemHandler;

		private CancellationCheckResultingBackgroundWorker(
				IResultingBackgroundWorkerParamDelegate<R, V> itemHandler) {
			this.itemHandler = itemHandler;
		}

		@Override
		public R invoke(V state) throws Throwable {
			return cancellation.withCancellationAwareness(itemHandler, state);
		}
	}

	/**
	 * Defines the maximal amount of time threads is given to run a parallel task.
	 */
	public static final String TIMEOUT = "ambeth.mth.timeout";

	@Autowired
	protected ICancellation cancellation;

	@Autowired
	protected IClassLoaderProvider classLoaderProvider;

	@Autowired(optional = true)
	protected ExecutorService executor;

	@Autowired
	protected IThreadLocalCleanupController threadLocalCleanupController;

	@Property(name = TIMEOUT, defaultValue = "30000")
	protected long timeout;

	@Property(name = IocConfigurationConstants.TransparentParallelizationActive,
			defaultValue = "true")
	protected boolean transparentParallelizationActive;

	@Override
	public void invokeInParallel(IServiceContext serviceContext, Runnable runnable, int workerCount) {
		invokeInParallel(serviceContext, false, timeout, runnable, workerCount);
	}

	@Override
	public void invokeInParallel(IServiceContext serviceContext, boolean inheritThreadLocals,
			long timeout, Runnable runnable, int workerCount) {
		Runnable[] runnables = new Runnable[workerCount];
		for (int a = workerCount; a-- > 0;) {
			if (runnable instanceof INamedRunnable) {
				String name = ((INamedRunnable) runnable).getName() + "-" + a;
				runnables[a] = new WrappingNamedRunnable(runnable, name);
			}
			else {
				runnables[a] = runnable;
			}
		}
		invokeInParallel(serviceContext, inheritThreadLocals, timeout, runnables);
	}

	@Override
	public void invokeInParallel(final IServiceContext serviceContext, Runnable... runnables) {
		invokeInParallel(serviceContext, false, timeout, runnables);
	}

	@Override
	public void invokeInParallel(IServiceContext serviceContext, boolean inheritThreadLocals,
			long timeout, Runnable... runnables) {
		CountDownLatch latch = new CountDownLatch(runnables.length);
		ParamHolder<Throwable> throwableHolder = new ParamHolder<>();
		IForkState forkState =
				inheritThreadLocals ? threadLocalCleanupController.createForkState() : null;

		Thread[] threads = new Thread[runnables.length];
		for (int a = runnables.length; a-- > 0;) {
			Runnable catchingRunnable = new CatchingRunnable(forkState, runnables[a], latch,
					throwableHolder, cancellation, threadLocalCleanupController);

			Thread thread = new Thread(catchingRunnable);
			thread.setContextClassLoader(classLoaderProvider.getClassLoader());
			thread.setDaemon(true);
			threads[a] = thread;
		}
		for (Thread thread : threads) {
			thread.start();
		}
		try {
			latch.await(timeout, TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		if (throwableHolder.getValue() != null) {
			throw RuntimeExceptionUtil.mask(throwableHolder.getValue(),
					"Error occured while invoking runnables");
		}
	}

	protected boolean isMultiThreadingAllowed() {
		return transparentParallelizationActive && executor != null;
	}

	@Override
	public <R, V> void invokeAndWait(IList<V> items,
			IResultingBackgroundWorkerParamDelegate<R, V> itemHandler,
			IAggregrateResultHandler<R, V> aggregateResultHandler) {
		if (items.size() == 0) {
			return;
		}
		if (!isMultiThreadingAllowed() || items.size() == 1) {
			try {
				for (int a = items.size(); a-- > 0;) {
					V item = items.get(a);
					R result = itemHandler.invoke(item);
					if (aggregateResultHandler != null) {
						aggregateResultHandler.aggregateResult(result, item);
					}
				}
				return;
			}
			catch (Throwable e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		ResultingRunnableHandle<R, V> runnableHandle =
				new ResultingRunnableHandle<>(new CancellationCheckResultingBackgroundWorker<>(itemHandler),
						aggregateResultHandler, new ArrayList<>(items), threadLocalCleanupController);

		Runnable parallelRunnable = new ResultingParallelRunnable<>(runnableHandle, true);
		Runnable mainRunnable = new ResultingParallelRunnable<>(runnableHandle, false);

		queueAndWait(items.size() - 1, parallelRunnable, mainRunnable, runnableHandle);
	}

	@Override
	public <R, K, V> void invokeAndWait(IMap<K, V> items,
			IResultingBackgroundWorkerParamDelegate<R, Entry<K, V>> itemHandler,
			IAggregrateResultHandler<R, Entry<K, V>> aggregateResultHandler) {
		if (items.size() == 0) {
			return;
		}
		if (!isMultiThreadingAllowed() || items.size() == 1) {
			try {
				for (Entry<K, V> item : items) {
					R result = itemHandler.invoke(item);
					if (aggregateResultHandler != null) {
						aggregateResultHandler.aggregateResult(result, item);
					}
				}
				return;
			}
			catch (Throwable e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		ArrayList<Entry<K, V>> itemsList = new ArrayList<>(items.size());
		for (Entry<K, V> item : items) {
			itemsList.add(item);
		}
		ResultingRunnableHandle<R, Entry<K, V>> runnableHandle =
				new ResultingRunnableHandle<>(new CancellationCheckResultingBackgroundWorker<>(itemHandler),
						aggregateResultHandler, itemsList, threadLocalCleanupController);

		Runnable parallelRunnable = new ResultingParallelRunnable<>(runnableHandle, true);
		Runnable mainRunnable = new ResultingParallelRunnable<>(runnableHandle, false);

		queueAndWait(items.size() - 1, parallelRunnable, mainRunnable, runnableHandle);
	}

	@Override
	public <V> void invokeAndWait(IList<V> items, IBackgroundWorkerParamDelegate<V> itemHandler) {
		if (items.size() == 0) {
			return;
		}
		if (!isMultiThreadingAllowed() || items.size() == 1) {
			try {
				for (int a = items.size(); a-- > 0;) {
					V item = items.get(a);
					itemHandler.invoke(item);
				}
				return;
			}
			catch (Throwable e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		RunnableHandle<V> runnableHandle =
				new RunnableHandle<>(new CancellationCheckBackgroundWorker<>(itemHandler),
						new ArrayList<>(items), threadLocalCleanupController);

		Runnable parallelRunnable = new ParallelRunnable<>(runnableHandle, true);
		Runnable mainRunnable = new ParallelRunnable<>(runnableHandle, false);

		queueAndWait(items.size() - 1, parallelRunnable, mainRunnable, runnableHandle);
	}

	@Override
	public <K, V> void invokeAndWait(IMap<K, V> items,
			IBackgroundWorkerParamDelegate<Entry<K, V>> itemHandler) {
		if (items.size() == 0) {
			return;
		}
		if (!isMultiThreadingAllowed() || items.size() == 1) {
			try {
				for (Entry<K, V> item : items) {
					itemHandler.invoke(item);
				}
				return;
			}
			catch (Throwable e) {
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		ArrayList<Entry<K, V>> itemsList = new ArrayList<>(items.size());
		for (Entry<K, V> item : items) {
			itemsList.add(item);
		}
		RunnableHandle<Entry<K, V>> runnableHandle =
				new RunnableHandle<>(new CancellationCheckBackgroundWorker<>(itemHandler), itemsList,
						threadLocalCleanupController);

		Runnable parallelRunnable = new ParallelRunnable<>(runnableHandle, true);
		Runnable mainRunnable = new ParallelRunnable<>(runnableHandle, false);

		queueAndWait(items.size() - 1, parallelRunnable, mainRunnable, runnableHandle);
	}

	protected <V> void queueAndWait(int forkCount, Runnable parallelRunnable, Runnable mainRunnable,
			AbstractRunnableHandle<V> runnableHandle) {
		// for n items fork at most n - 1 threads because our main thread behaves like a worker by
		// itself
		for (int a = forkCount; a-- > 0;) {
			executor.execute(parallelRunnable);
		}
		// consume items with the "main thread" as long as there is one in the queue
		mainRunnable.run();

		// wait till the forked threads have finished, too
		waitForLatch(runnableHandle.latch, runnableHandle);

		runnableHandle.forkState.reintegrateForkedValues();
	}

	protected void waitForLatch(CountDownLatch latch, InterruptingParamHolder exHolder) {
		while (true) {
			Throwable ex = exHolder.getValue();
			if (ex != null) {
				// A parallel exception will be thrown here
				throw RuntimeExceptionUtil.mask(ex);
			}
			try {
				latch.await();
				if (latch.getCount() == 0) {
					return;
				}
			}
			catch (InterruptedException e) {
				// intended blank
				// will be thrown if a foreign thread throws an exception an is stored in the paramHolder
			}
		}
	}
}
