package de.osthus.ambeth.persistence.parallel;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.threadlocal.IForkState;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;
import de.osthus.ambeth.util.ParamHolder;

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