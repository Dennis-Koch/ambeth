package com.koch.ambeth.util.threading;

/*-
 * #%L
 * jambeth-util-test
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.koch.ambeth.testutil.category.PerformanceTests;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.threading.FastThreadPool;

@Category(PerformanceTests.class)
public class FastThreadPoolTest {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	protected FastThreadPool ftp;

	@Before
	public void setUp() throws Exception {
		ftp = new FastThreadPool();
		ftp.setCoreThreadCount(0);
		ftp.setMaxThreadCount(2);
	}

	@After
	public void tearDown() throws Exception {
		ftp.shutdown();
		ftp = null;
	}

	@Test
	public void testFastThreadPoolIntIntInt() {
		FastThreadPool newFtp = new FastThreadPool(5, 4, 50000);

		Assert.assertEquals("CoreThreadCount", 5, newFtp.getCoreThreadCount());
		Assert.assertEquals("MaxThreadCount", 4, newFtp.getMaxThreadCount());
		Assert.assertEquals("Timeout", 50000, newFtp.getTimeout());

		newFtp.shutdown();
	}

	@Test
	public void throughput() {
		FastThreadPool newFtp = new FastThreadPool(5, 5, 60000);
		newFtp.setVariableThreads(false);
		ExecutorService executorService = Executors.newFixedThreadPool(5);

		long warmup = 10000;
		queue(newFtp, warmup);
		queue(executorService, warmup);

		long duration = 30000;

		long jdkThroughput = queue(executorService, duration);
		long ambethThroughput = queue(newFtp, duration);

		System.out.println("AMBETH: " + (long) (1000 * ambethThroughput / (double) duration) + ", JDK: "
				+ (long) (1000 * jdkThroughput / (double) duration) + " (calls per second)");
		Assert.assertTrue(ambethThroughput > jdkThroughput);
		executorService.shutdownNow();
		newFtp.shutdown();
	}

	class QueueRunnable extends ReentrantLock implements Runnable {
		private final CountDownLatch latch;

		private final long amount;

		public volatile long count;

		public QueueRunnable(CountDownLatch latch, long amount) {
			this.latch = latch;
			this.amount = amount;
		}

		@Override
		public void run() {
			lock();
			try {
				count++;
				if (count >= amount) {
					latch.countDown();
				}
			}
			finally {
				unlock();
			}
		}
	}

	protected long queue(Executor executor, long time) {
		try {
			long iterationCount = 5000000;
			CountDownLatch latch = new CountDownLatch(1);
			QueueRunnable runnable = new QueueRunnable(latch, iterationCount);

			long start = System.currentTimeMillis();
			for (long a = iterationCount; a-- > 0;) {
				executor.execute(runnable);
			}
			latch.await();
			long duration = System.currentTimeMillis() - start;
			return (long) ((iterationCount / (double) duration) * 1000);
		}
		catch (InterruptedException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	@Ignore
	@Test
	public void testAddBlockingMessage() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testQueueActionHandlerRunnableOfQQ() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testQueueActionOHandlerRunnableOfOQCountDownLatch() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testQueueActionsListOfOHandlerRunnableOfOQCountDownLatch() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testQueueActionsWaitListOfOHandlerRunnableOfOQ() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testQueueActionsWaitListOfOCHandlerRunnableOfOC() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testQueueActionsListOfOCHandlerRunnableOfOCCountDownLatch() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testQueueActionOCHandlerRunnableOfOCCountDownLatch() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testGetCoreThreadCount() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testSetMaxThreadCount() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testRefreshThreadCount() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testGetMaxThreadCount() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testIsVariableThreads() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testAwaitTermination() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testInvokeAllCollectionOfQextendsCallableOfT() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testInvokeAllCollectionOfQextendsCallableOfTLongTimeUnit() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testInvokeAnyCollectionOfQextendsCallableOfT() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testInvokeAnyCollectionOfQextendsCallableOfTLongTimeUnit() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testIsShutdown() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testIsTerminated() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testShutdown() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testShutdownNow() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testSubmitCallableOfT() {
		fail("Not yet implemented");
	}

	@Test
	public void testSubmitRunnable() throws InterruptedException, ExecutionException {
		final CountDownLatch latch = new CountDownLatch(1);

		Future<?> future = ftp.submit(new Runnable() {
			@Override
			public void run() {
				latch.countDown();
			}
		});
		latch.await(5, TimeUnit.SECONDS);
		Object result = future.get();
		Assert.assertNull("Task result invalid (null expected)", result);
	}

	@Test
	public void testSubmitRunnableFuture() throws InterruptedException, ExecutionException {
		final CountDownLatch latch = new CountDownLatch(1);

		Object result = new Object();

		Future<?> future = ftp.submit(new Runnable() {
			@Override
			public void run() {
				latch.countDown();
			}
		}, result);
		latch.await(5, TimeUnit.SECONDS);
		Object futureResult = future.get();
		Assert.assertSame("Result should be identical", result, futureResult);
	}

	@Test
	public void testExecute() throws InterruptedException {
		final CountDownLatch latch = new CountDownLatch(1);

		ftp.execute(new Runnable() {
			@Override
			public void run() {
				latch.countDown();
			}
		});
		latch.await(5, TimeUnit.SECONDS);
	}

	@Ignore
	@Test
	public void testGetThreadPoolID() {
		fail("Not yet implemented");
	}
}
