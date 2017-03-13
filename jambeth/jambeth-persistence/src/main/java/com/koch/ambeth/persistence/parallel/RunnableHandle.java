package com.koch.ambeth.persistence.parallel;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;

import com.koch.ambeth.ioc.threadlocal.IForkState;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import com.koch.ambeth.util.ParamHolder;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;

public class RunnableHandle<V>
{
	public final IBackgroundWorkerParamDelegate<V> run;

	public final Lock parallelLock;

	public final CountDownLatch latch;

	public final IForkState forkState;

	public final ParamHolder<Throwable> exHolder;

	public final IList<V> items;

	public final IThreadLocalCleanupController threadLocalCleanupController;

	public RunnableHandle(IBackgroundWorkerParamDelegate<V> run, Lock parallelLock, CountDownLatch latch, IForkState forkState,
			ParamHolder<Throwable> exHolder, IList<V> items, IThreadLocalCleanupController threadLocalCleanupController)
	{
		this.run = run;
		this.parallelLock = parallelLock;
		this.latch = latch;
		this.forkState = forkState;
		this.exHolder = exHolder;
		this.items = items;
		this.threadLocalCleanupController = threadLocalCleanupController;
	}
}