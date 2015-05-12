package de.osthus.ambeth.threading;

import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public class FastThreadPoolTest
{
	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
	}

	protected FastThreadPool ftp;

	@Before
	public void setUp() throws Exception
	{
		ftp = new FastThreadPool();
		ftp.setCoreThreadCount(0);
		ftp.setMaxThreadCount(2);
	}

	@After
	public void tearDown() throws Exception
	{
		ftp.shutdown();
		ftp = null;
	}

	@Test
	public void testFastThreadPoolIntIntInt()
	{
		FastThreadPool newFtp = new FastThreadPool(5, 4, 50000);

		Assert.assertEquals("CoreThreadCount", 5, newFtp.getCoreThreadCount());
		Assert.assertEquals("MaxThreadCount", 4, newFtp.getMaxThreadCount());
		Assert.assertEquals("Timeout", 50000, newFtp.getTimeout());

		newFtp.shutdown();
	}

	@Test
	public void throughput()
	{
		FastThreadPool newFtp = new FastThreadPool(1, 1, 60000);
		newFtp.setVariableThreads(false);
		ExecutorService executorService = Executors.newSingleThreadExecutor();

		long warmup = 10000;
		queue(newFtp, warmup);
		queue(executorService, warmup);

		long duration = 30000;

		long jdkThroughput = queue(executorService, duration);
		long ambethThroughput = queue(newFtp, duration);

		System.out.println("AMBETH: " + (long) (1000 * ambethThroughput / (double) duration) + ", JDK: " + (long) (1000 * jdkThroughput / (double) duration)
				+ " (calls per second)");
		Assert.assertTrue(ambethThroughput > jdkThroughput);
		executorService.shutdownNow();
		newFtp.shutdown();
	}

	protected long queue(Executor executor, long time)
	{
		try
		{
			long till = System.currentTimeMillis() + time;
			long iterationCount = 0;
			while (System.currentTimeMillis() < till)
			{

				final Exchanger<Object> ex = new Exchanger<Object>();
				executor.execute(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							ex.exchange(this);
						}
						catch (InterruptedException e)
						{
							throw RuntimeExceptionUtil.mask(e);
						}
					}
				});
				ex.exchange(null);
				iterationCount++;
			}
			return iterationCount;
		}
		catch (InterruptedException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Ignore
	@Test
	public void testAddBlockingMessage()
	{
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testQueueActionHandlerRunnableOfQQ()
	{
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testQueueActionOHandlerRunnableOfOQCountDownLatch()
	{
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testQueueActionsListOfOHandlerRunnableOfOQCountDownLatch()
	{
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testQueueActionsWaitListOfOHandlerRunnableOfOQ()
	{
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testQueueActionsWaitListOfOCHandlerRunnableOfOC()
	{
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testQueueActionsListOfOCHandlerRunnableOfOCCountDownLatch()
	{
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testQueueActionOCHandlerRunnableOfOCCountDownLatch()
	{
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testGetCoreThreadCount()
	{
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testSetMaxThreadCount()
	{
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testRefreshThreadCount()
	{
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testGetMaxThreadCount()
	{
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testIsVariableThreads()
	{
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testAwaitTermination()
	{
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testInvokeAllCollectionOfQextendsCallableOfT()
	{
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testInvokeAllCollectionOfQextendsCallableOfTLongTimeUnit()
	{
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testInvokeAnyCollectionOfQextendsCallableOfT()
	{
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testInvokeAnyCollectionOfQextendsCallableOfTLongTimeUnit()
	{
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testIsShutdown()
	{
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testIsTerminated()
	{
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testShutdown()
	{
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testShutdownNow()
	{
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testSubmitCallableOfT()
	{
		fail("Not yet implemented");
	}

	@Test
	public void testSubmitRunnable() throws InterruptedException, ExecutionException
	{
		final CountDownLatch latch = new CountDownLatch(1);

		Future<?> future = ftp.submit(new Runnable()
		{
			@Override
			public void run()
			{
				latch.countDown();
			}
		});
		latch.await(5, TimeUnit.SECONDS);
		Object result = future.get();
		Assert.assertNull("Task result invalid (null expected)", result);
	}

	@Test
	public void testSubmitRunnableFuture() throws InterruptedException, ExecutionException
	{
		final CountDownLatch latch = new CountDownLatch(1);

		Object result = new Object();

		Future<?> future = ftp.submit(new Runnable()
		{
			@Override
			public void run()
			{
				latch.countDown();
			}
		}, result);
		latch.await(5, TimeUnit.SECONDS);
		Object futureResult = future.get();
		Assert.assertSame("Result should be identical", result, futureResult);
	}

	@Test
	public void testExecute() throws InterruptedException
	{
		final CountDownLatch latch = new CountDownLatch(1);

		ftp.execute(new Runnable()
		{
			@Override
			public void run()
			{
				latch.countDown();
			}
		});
		latch.await(5, TimeUnit.SECONDS);
	}

	@Ignore
	@Test
	public void testGetThreadPoolID()
	{
		fail("Not yet implemented");
	}
}
