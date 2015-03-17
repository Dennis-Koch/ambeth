package de.osthus.ambeth.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.ioc.threadlocal.IForkState;
import de.osthus.ambeth.ioc.threadlocal.IThreadLocalCleanupController;

public abstract class AbstractRunnableHandle<V> extends InterruptingParamHolder
{
	public final Lock parallelLock = new ReentrantLock();

	public final CountDownLatch latch;

	public final IForkState forkState;

	public final ArrayList<V> items;

	public final IThreadLocalCleanupController threadLocalCleanupController;

	public final Thread createdThread = Thread.currentThread();

	public AbstractRunnableHandle(ArrayList<V> items, IThreadLocalCleanupController threadLocalCleanupController)
	{
		super(Thread.currentThread());
		this.latch = new CountDownLatch(items.size());
		this.items = items;
		this.threadLocalCleanupController = threadLocalCleanupController;
		this.forkState = threadLocalCleanupController.createForkState();
	}
}