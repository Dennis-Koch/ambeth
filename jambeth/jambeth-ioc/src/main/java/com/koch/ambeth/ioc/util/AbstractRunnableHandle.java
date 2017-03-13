package com.koch.ambeth.ioc.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.ioc.threadlocal.IForkState;
import com.koch.ambeth.ioc.threadlocal.IThreadLocalCleanupController;
import com.koch.ambeth.util.collections.ArrayList;

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