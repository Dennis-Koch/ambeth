package com.koch.ambeth.ioc;

/*-
 * #%L
 * jambeth-test
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.koch.ambeth.ioc.factory.IBeanContextFactory;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.category.PerformanceTests;
import com.koch.ambeth.util.IDedicatedConverterExtendable;
import com.koch.ambeth.util.converter.BooleanArrayConverter;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

@Category(PerformanceTests.class)
public class ContextLifecycleLoadTest extends AbstractIocTest {
	@LogInstance
	private ILogger log;

	private class ContextConsumer implements Runnable {
		public int i;

		public long t;

		public IServiceContext context;

		public long sleepTime;

		@Override
		public void run() {
			try {
				Thread.sleep(sleepTime);
			}
			catch (InterruptedException e) {
				throw RuntimeExceptionUtil.mask(e);
			}
			context.dispose();
			log.info("t+" + (System.currentTimeMillis() - t) + "ms: Consumer " + i + " shutting down");
		}
	}

	public static class ContextLifecycleLoadTestModule implements IInitializingModule {

		@Override
		public void afterPropertiesSet(IBeanContextFactory beanContextFactory) throws Throwable {
			beanContextFactory.registerBean("booleanArrayConverter", BooleanArrayConverter.class);
			beanContextFactory.link("booleanArrayConverter").to(IDedicatedConverterExtendable.class)
					.with(boolean[].class, String.class);
			beanContextFactory.link("booleanArrayConverter").to(IDedicatedConverterExtendable.class)
					.with(String.class, boolean[].class);
		}

	}

	private final ExecutorService pool =
			new ThreadPoolExecutor(10, 10, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

	@Test
	public void testSimpleLifecycles() {
		IServiceContext childContext1 = beanContext.createService(ContextLifecycleLoadTestModule.class);
		IServiceContext childContext2 = beanContext.createService(ContextLifecycleLoadTestModule.class);

		childContext1.dispose();
		childContext2.dispose();
	}

	@Test
	public void testConcurrentLifecycles() throws InterruptedException {
		int count = 10;
		int sleep = 400;
		int loopDuration = count * sleep;
		long t = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			long diff = System.currentTimeMillis() - t;
			long sleepTime = loopDuration - diff + (10 - i) * 100 + i * 220 % 1000;
			IServiceContext childContext =
					beanContext.createService(ContextLifecycleLoadTestModule.class);
			ContextConsumer consumer = new ContextConsumer();
			consumer.i = i;
			consumer.t = t;
			consumer.context = childContext;
			consumer.sleepTime = sleepTime;
			log.info("t+" + diff + "ms: Starting consumer " + i + " with " + sleepTime + "ms sleep time");
			pool.submit(consumer);
			Thread.sleep(sleep);
		}
		pool.shutdown();
		if (!pool.awaitTermination(2, TimeUnit.SECONDS)) {
			throw new IllegalStateException("Threadpool did not shutdown properly");
		}
	}
}
