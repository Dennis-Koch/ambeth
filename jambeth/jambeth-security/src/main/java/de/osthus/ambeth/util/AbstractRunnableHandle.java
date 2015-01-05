package de.osthus.ambeth.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.ioc.threadlocal.IForkState;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;

public abstract class AbstractRunnableHandle<V>
{
	public final Lock parallelLock = new ReentrantLock();

	public final CountDownLatch latch;

	public final IForkState forkState;

	public final ParamHolder<Throwable> exHolder;

	public final IList<V> items;

	public final IThreadLocalCleanupController threadLocalCleanupController;

	public final Thread createdThread = Thread.currentThread();

	public AbstractRunnableHandle(IList<V> items, IThreadLocalCleanupController threadLocalCleanupController)
	{
		this.latch = new CountDownLatch(items.size());
		this.exHolder = new InterruptingParamHolder(Thread.currentThread());
		this.items = items;
		this.threadLocalCleanupController = threadLocalCleanupController;
		this.forkState = threadLocalCleanupController.createForkState();
	}
}