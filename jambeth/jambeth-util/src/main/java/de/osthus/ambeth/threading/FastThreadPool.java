package de.osthus.ambeth.threading;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.FastList;

public class FastThreadPool implements ExecutorService, IFastThreadPool
{
	private static final Random random = new Random();

	private final FastList<QueueItem> actionQueue = new FastList<QueueItem>();

	private final FastList<FastThreadPoolThread> freeThreadList = new FastList<FastThreadPoolThread>();

	private final FastList<FastThreadPoolThread> busyThreadList = new FastList<FastThreadPoolThread>();

	private FastThreadPoolThread blockingThread = null;

	private final HashSet<Class<?>> blockingSet = new HashSet<Class<?>>();

	private boolean shutdown = false, variableThreads = true;

	private final int timeout, threadPoolID = Math.abs(random.nextInt());

	private int threadCounter;

	private int coreThreadCount, maxThreadCount;

	private String name;

	private final Lock lock = new ReentrantLock();

	private final Condition syncCond = lock.newCondition();

	public FastThreadPool()
	{
		this(0, 5, 60000);
	}

	public FastThreadPool(final int coreThreadCount, final int maxThreadCount, final int timeout)
	{
		this.timeout = timeout;
		setMaxThreadCount(maxThreadCount);
		setCoreThreadCount(coreThreadCount);
	}

	public void destroy() throws Exception
	{
		shutdown();
	}

	public void setVariableThreads(boolean variableThreads)
	{
		this.variableThreads = variableThreads;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setCoreThreadCount(int coreThreadCount)
	{
		Lock lock = this.lock;
		lock.lock();
		try
		{
			if (coreThreadCount > this.coreThreadCount)
			{
				for (int a = 0, size = coreThreadCount - this.coreThreadCount; a < size; a++)
				{
					createThread();
				}
			}
			this.coreThreadCount = coreThreadCount;
			syncCond.signalAll();
		}
		finally
		{
			lock.unlock();
		}
	}

	public void addBlockingMessage(final Class<?> blockingClass)
	{
		Lock lock = this.lock;
		lock.lock();
		try
		{
			blockingSet.add(blockingClass);
		}
		finally
		{
			lock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.threading.IFastThreadPool#queueAction(de.osthus.ambeth .threading.HandlerRunnable)
	 */
	@Override
	public void queueAction(final HandlerRunnable<?, ?> handlerRunnable)
	{
		Lock lock = this.lock;
		lock.lock();
		try
		{
			queueAction(null, null, handlerRunnable, null);
		}
		finally
		{
			lock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.threading.IFastThreadPool#queueAction(O, de.osthus.ambeth.threading.HandlerRunnable, java.util.concurrent.CountDownLatch)
	 */
	@Override
	public <O> void queueAction(final O object, final HandlerRunnable<O, ?> handlerRunnable, final CountDownLatch latch)
	{
		Lock lock = this.lock;
		lock.lock();
		try
		{
			queueAction(object, null, handlerRunnable, latch);
		}
		finally
		{
			lock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.threading.IFastThreadPool#queueActions(java.util.ArrayList , de.osthus.ambeth.threading.HandlerRunnable,
	 * java.util.concurrent.CountDownLatch)
	 */
	@Override
	public <O> void queueActions(final List<O> objects, final HandlerRunnable<O, ?> handlerRunnable, final CountDownLatch latch)
	{
		Lock lock = this.lock;
		lock.lock();
		try
		{
			for (int a = 0, size = objects.size(); a < size; a++)
			{
				queueAction(objects.get(a), null, handlerRunnable, latch);
			}
			syncCond.signalAll();
		}
		finally
		{
			lock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.threading.IFastThreadPool#queueActionsWait(java.util .ArrayList, de.osthus.ambeth.threading.HandlerRunnable)
	 */
	@Override
	public <O> void queueActionsWait(final List<O> objects, final HandlerRunnable<O, ?> handlerRunnable)
	{
		CountDownLatch latch = new CountDownLatch(objects.size());
		queueActions(objects, handlerRunnable, latch);
		try
		{
			latch.await();
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.threading.IFastThreadPool#queueActionsWait(java.util .ArrayList, C, de.osthus.ambeth.threading.HandlerRunnable)
	 */
	@Override
	public <O, C> void queueActionsWait(final List<O> objects, final C context, final HandlerRunnable<O, C> handlerRunnable)
	{
		CountDownLatch latch = new CountDownLatch(objects.size());
		queueActions(objects, context, handlerRunnable, latch);
		try
		{
			latch.await();
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.threading.IFastThreadPool#queueActions(java.util.ArrayList , C, de.osthus.ambeth.threading.HandlerRunnable,
	 * java.util.concurrent.CountDownLatch)
	 */
	@Override
	public <O, C> void queueActions(final List<O> objects, final C context, final HandlerRunnable<O, C> handlerRunnable, final CountDownLatch latch)
	{
		Lock lock = this.lock;
		lock.lock();
		try
		{
			for (int a = 0, size = objects.size(); a < size; a++)
			{
				queueAction(objects.get(a), context, handlerRunnable, latch);
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.threading.IFastThreadPool#queueAction(O, C, de.osthus.ambeth.threading.HandlerRunnable, java.util.concurrent.CountDownLatch)
	 */
	@Override
	public <O, C> void queueAction(final O object, final C context, final HandlerRunnable<O, C> handlerRunnable, final CountDownLatch latch)
	{
		Lock lock = this.lock;
		lock.lock();
		try
		{
			if (shutdown)
			{
				throw new RejectedExecutionException();
			}
			QueueItem queueItem = new QueueItem(context, object, handlerRunnable, latch);
			actionQueue.pushLast(queueItem.getThreadingLE());
			if (blockingThread == null)
			{
				if (!isThreadMaximum() && freeThreadList.size() == 0)
				{
					createThread();
				}
				else
				{
					syncCond.signal();
				}
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	protected final void shutdownThread(final FastThreadPoolThread thread)
	{
		Lock lock = this.lock;
		lock.lock();
		try
		{
			if (thread.isActive())
			{
				thread.setActive(false);
				thread.queueOnList(null);
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	protected final QueueItem getNextMessage(final FastThreadPoolThread qThread) throws InterruptedException
	{
		Lock lock = this.lock;
		lock.lock();
		try
		{
			if (actionQueue.size() == 0 || blockingThread != null)
			{
				waitForMessage(qThread);
				return null;
			}
			QueueItem object = actionQueue.first().value;
			if (!isObjectAllowed(object))
			{
				waitForMessage(qThread);
				return null;
			}
			if (blockingSet.contains(object.getClass()))
			{
				blockingThread = qThread;
			}
			qThread.queueOnList(busyThreadList);
			actionQueue.popFirst();
			return object;
		}
		finally
		{
			lock.unlock();
		}
	}

	protected void waitForMessage(final FastThreadPoolThread qThread) throws InterruptedException
	{
		if (isMoreThanMaxThreads() || isShutdown())
		{
			shutdownThread(qThread);
			return;
		}
		int maxSleepTime = timeout - qThread.getTimeWithoutJob();
		if (timeout > 0 && isMoreThanCoreThreads())
		{
			if (maxSleepTime <= 0)
			{
				shutdownThread(qThread);
				return;
			}
			else
			{
				long start = System.currentTimeMillis();
				Lock lock = this.lock;
				lock.lock();
				try
				{
					syncCond.await(maxSleepTime, TimeUnit.MILLISECONDS);
				}
				finally
				{
					lock.unlock();
				}
				long end = System.currentTimeMillis();
				int timeSpent = (int) (end - start);
				qThread.setTimeWithoutJob(qThread.getTimeWithoutJob() + timeSpent);
			}
		}
		else
		{
			Lock lock = this.lock;
			lock.lock();
			try
			{
				syncCond.awaitUninterruptibly();
			}
			finally
			{
				lock.unlock();
			}
		}
	}

	protected boolean isObjectAllowed(final Object object)
	{
		return true;
	}

	protected boolean isThreadMaximum()
	{
		return freeThreadList.size() + busyThreadList.size() >= maxThreadCount;
	}

	protected boolean isMoreThanCoreThreads()
	{
		return freeThreadList.size() + busyThreadList.size() > coreThreadCount;
	}

	protected boolean isMoreThanMaxThreads()
	{
		return freeThreadList.size() + busyThreadList.size() > maxThreadCount;
	}

	protected void actionFinished()
	{
		Lock lock = this.lock;
		lock.lock();
		try
		{
			FastThreadPoolThread currentThread = (FastThreadPoolThread) Thread.currentThread();
			if (currentThread == blockingThread)
			{
				blockingThread = null;
			}
			currentThread.queueOnList(freeThreadList);
			syncCond.signalAll();
		}
		finally
		{
			lock.unlock();
		}
	}

	public int getTimeout()
	{
		return timeout;
	}

	public int getCoreThreadCount()
	{
		return coreThreadCount;
	}

	protected FastThreadPoolThread createThread()
	{
		FastThreadPoolThread thread = new FastThreadPoolThread(this);
		if (name != null)
		{
			thread.setName(name + "-" + ++threadCounter);
		}
		else
		{
			thread.setName(getThreadPoolID() + "-" + ++threadCounter);
		}
		thread.queueOnList(freeThreadList);
		thread.start();
		return thread;
	}

	public void setMaxThreadCount(int maxThreadCount)
	{
		Lock lock = this.lock;
		lock.lock();
		try
		{
			this.maxThreadCount = maxThreadCount;
			syncCond.signalAll();
		}
		finally
		{
			lock.unlock();
		}
	}

	public void refreshThreadCount()
	{
		if (variableThreads)
		{
			int processors = Runtime.getRuntime().availableProcessors();
			setMaxThreadCount(processors);
		}
	}

	public int getMaxThreadCount()
	{
		return maxThreadCount;
	}

	public boolean isVariableThreads()
	{
		return variableThreads;
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException
	{
		return false;
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
			TimeoutException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isShutdown()
	{
		return shutdown;
	}

	@Override
	public boolean isTerminated()
	{
		return isShutdown() && freeThreadList.size() == 0 && busyThreadList.size() == 0;
	}

	@Override
	public void shutdown()
	{
		Lock lock = this.lock;
		lock.lock();
		try
		{
			this.shutdown = true;
			syncCond.signalAll();
		}
		finally
		{
			lock.unlock();
		}
	}

	@Override
	public List<Runnable> shutdownNow()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> Future<T> submit(final Callable<T> task)
	{
		final FutureTask<T> futureTask = new FutureTask<T>(task);
		execute(futureTask);
		return futureTask;
	}

	@Override
	public Future<?> submit(Runnable task)
	{
		return submit(task, null);
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result)
	{
		final FutureTask<T> futureTask = new FutureTask<T>(task, result);
		execute(futureTask);
		return futureTask;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.osthus.ambeth.threading.IFastThreadPool#execute(java.lang.Runnable)
	 */
	@Override
	public void execute(Runnable command)
	{
		queueAction(command, null, null);
	}

	public int getThreadPoolID()
	{
		return threadPoolID;
	}
}