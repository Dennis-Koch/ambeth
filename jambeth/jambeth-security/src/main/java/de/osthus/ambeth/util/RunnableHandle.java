package de.osthus.ambeth.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.threadlocal.IForkState;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import de.osthus.ambeth.threading.IResultingBackgroundWorkerParamDelegate;

public class RunnableHandle<R, V>
{
	public final IResultingBackgroundWorkerParamDelegate<R, V> run;

	public final IAggregrateResultHandler<R, V> aggregrateResultHandler;

	public final Lock parallelLock = new ReentrantLock();

	public final CountDownLatch latch;

	public final IForkState forkState;

	public final ParamHolder<Throwable> exHolder;

	public final IList<V> items;

	public final IThreadLocalCleanupController threadLocalCleanupController;

	public RunnableHandle(IResultingBackgroundWorkerParamDelegate<R, V> run, IAggregrateResultHandler<R, V> aggregrateResultHandler, IList<V> items,
			IThreadLocalCleanupController threadLocalCleanupController)
	{
		this.run = run;
		this.aggregrateResultHandler = aggregrateResultHandler;
		this.latch = new CountDownLatch(items.size());
		this.exHolder = new InterruptingParamHolder(Thread.currentThread());
		this.items = items;
		this.threadLocalCleanupController = threadLocalCleanupController;
		this.forkState = threadLocalCleanupController.createForkState();
	}
}