package de.osthus.ambeth.threading;

import java.util.concurrent.CountDownLatch;

import de.osthus.ambeth.collections.ListElem;

public class QueueItem
{
	private final Object context;

	private final Object object;

	private final HandlerRunnable<?, ?> handler;

	private final CountDownLatch latch;

	private final ListElem<QueueItem> threadingLE = new ListElem<QueueItem>(this);

	public QueueItem(Object context, Object object, HandlerRunnable<?, ?> handler, CountDownLatch latch)
	{
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
		return threadingLE;
	}

	public HandlerRunnable<?, ?> getHandler()
	{
		return handler;
	}

	public CountDownLatch getLatch()
	{
		return latch;
	}
}
