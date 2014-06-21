package de.osthus.ambeth.ioc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.factory.IBeanContextFactory;
import de.osthus.ambeth.testutil.AbstractIocTest;
import de.osthus.ambeth.testutil.category.PerformanceTests;
import de.osthus.ambeth.util.IDedicatedConverterExtendable;
import de.osthus.ambeth.util.converter.BooleanArrayConverter;

@Category(PerformanceTests.class)
public class ContextLifecycleLoadTest extends AbstractIocTest
{
	private static class ContextConsumer implements Runnable
	{
		public int i;

		public long t;

		public IServiceContext context;

		public long sleepTime;

		@Override
		public void run()
		{
			try
			{
				Thread.sleep(sleepTime);
			}
			catch (InterruptedException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			context.dispose();
			System.out.println("t+" + (System.currentTimeMillis() - t) + "ms: Consumer " + i + " shutting down");
		}
	}

	public static class ContextLifecycleLoadTestModule implements IInitializingModule
	{

		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable
		{
			beanContextFactory.registerBean("booleanArrayConverter", BooleanArrayConverter.class);
			beanContextFactory.link("booleanArrayConverter").to(IDedicatedConverterExtendable.class).with(boolean[].class, String.class);
			beanContextFactory.link("booleanArrayConverter").to(IDedicatedConverterExtendable.class).with(String.class, boolean[].class);
		}

	}

	private final ExecutorService pool = new ThreadPoolExecutor(10, 10, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

	@Test
	public void testSimpleLifecycles()
	{
		IServiceContext childContext1 = beanContext.createService(ContextLifecycleLoadTestModule.class);
		IServiceContext childContext2 = beanContext.createService(ContextLifecycleLoadTestModule.class);

		childContext1.dispose();
		childContext2.dispose();
	}

	@Test
	public void testConcurrentLifecycles() throws InterruptedException
	{
		int count = 10;
		int sleep = 400;
		int loopDuration = count * sleep;
		long t = System.currentTimeMillis();
		for (int i = 0; i < count; i++)
		{
			long diff = System.currentTimeMillis() - t;
			long sleepTime = loopDuration - diff + (10 - i) * 100 + i * 220 % 1000;
			IServiceContext childContext = beanContext.createService(ContextLifecycleLoadTestModule.class);
			ContextConsumer consumer = new ContextConsumer();
			consumer.i = i;
			consumer.t = t;
			consumer.context = childContext;
			consumer.sleepTime = sleepTime;
			System.out.println("t+" + diff + "ms: Starting consumer " + i + " with " + sleepTime + "ms sleep time");
			pool.submit(consumer);
			Thread.sleep(sleep);
		}
		pool.awaitTermination(2, TimeUnit.SECONDS);
	}
}
