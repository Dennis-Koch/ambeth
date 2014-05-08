package de.osthus.ambeth.threading;

import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

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
