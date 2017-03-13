package com.koch.ambeth.cancel;

import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.cancel.CancelledException;
import com.koch.ambeth.ioc.cancel.ICancellation;
import com.koch.ambeth.ioc.cancel.ICancellationHandle;
import com.koch.ambeth.ioc.util.IMultithreadingHelper;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.util.ParamHolder;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.threading.IBackgroundWorkerParamDelegate;

public class CancellationTest extends AbstractIocTest
{
	@Autowired
	protected ICancellation cancellation;

	@Autowired
	protected IMultithreadingHelper multithreadingHelper;

	@Test
	public void testInterruptOnTransparentFork() throws Throwable
	{
		ArrayList<Object> items = new ArrayList<Object>();
		for (int a = 100; a-- > 0;)
		{
			items.add(new Object());
		}

		final ICancellationHandle cancellationHandle = cancellation.getEnsureCancellationHandle();

		final Thread mainThread = Thread.currentThread();
		final ParamHolder<Boolean> interruptReceivedPH = new ParamHolder<Boolean>();
		final CountDownLatch waitForCancelLatch = new CountDownLatch(1);
		final CountDownLatch waitForInterruptLatch = new CountDownLatch(1);
		final CountDownLatch waitForTerminationLatch = new CountDownLatch(1);
		boolean cancelledExceptionThrown = false;
		try
		{
			multithreadingHelper.invokeAndWait(items, new IBackgroundWorkerParamDelegate<Object>()
			{
				@Override
				public void invoke(Object item) throws Throwable
				{
					try
					{
						if (Thread.currentThread() == mainThread)
						{
							// waits till a FOREIGN child thread calls countDown()
							waitForCancelLatch.await();
							// then create a separate thread to simulate cancellation from a foreign source
							new Thread(new Runnable()
							{
								@Override
								public void run()
								{
									cancellationHandle.cancel();
								}
							}).start();
						}
						waitForCancelLatch.countDown(); // notify the waiting main thread
						try
						{
							waitForInterruptLatch.await(); // wait for the interrupt to be fired
						}
						catch (InterruptedException e)
						{
							if (cancellationHandle.isCancelled())
							{
								interruptReceivedPH.setValue(true);
							}
						}
					}
					finally
					{
						if (Thread.currentThread() != mainThread)
						{
							waitForTerminationLatch.countDown();
						}
					}
				}
			});
		}
		catch (CancelledException e)
		{
			cancelledExceptionThrown = true;
		}
		Assert.assertTrue(CancelledException.class.getName() + " should have been thrown", cancelledExceptionThrown);
		waitForTerminationLatch.await();
		Assert.assertSame(Boolean.TRUE, interruptReceivedPH.getValue());
	}
}
