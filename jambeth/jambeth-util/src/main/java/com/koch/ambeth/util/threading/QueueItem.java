package com.koch.ambeth.util.threading;

import java.util.concurrent.CountDownLatch;

import com.koch.ambeth.util.collections.ListElem;

public class QueueItem extends ListElem<QueueItem>
{
	private final Object context;

	private final Object object;

	private final HandlerRunnable<?, ?> handler;

	private final CountDownLatch latch;

	private final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

	public QueueItem(Object context, Object object, HandlerRunnable<?, ?> handler, CountDownLatch latch)
	{
		super.value = this;
		this.context = context;
		this.object = object;
		this.handler = handler;
		this.latch = latch;
	}

	public Object getContext()
	{
		return context;
	}

	public Object getObject()
	{
		return object;
	}

	public ListElem<QueueItem> getThreadingLE()
	{
		return this;
	}

	public HandlerRunnable<?, ?> getHandler()
	{
		return handler;
	}

	public CountDownLatch getLatch()
	{
		return latch;
	}

	public ClassLoader getContextClassLoader()
	{
		return contextClassLoader;
	}
}
