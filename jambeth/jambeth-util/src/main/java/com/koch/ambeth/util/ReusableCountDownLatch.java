package com.koch.ambeth.util;

import com.koch.ambeth.util.objectcollector.Collectable;
import com.koch.ambeth.util.objectcollector.IObjectCollector;

public class ReusableCountDownLatch extends Collectable
{
	public static ReusableCountDownLatch create(IObjectCollector objectCollector, final int count)
	{
		if (count < 0)
		{
			throw new IllegalArgumentException("count < 0");
		}
		ReusableCountDownLatch latch = objectCollector.create(ReusableCountDownLatch.class);
		latch.count = count;
		return latch;
	}

	@Override
	public void disposeInternDoNotCall()
	{
		count = 0;
		super.disposeInternDoNotCall();
	}

	private final Object syncObject = new Object();

	private int count;

	public ReusableCountDownLatch()
	{
	}

	public void await() throws InterruptedException
	{
		synchronized (syncObject)
		{
			while (count != 0)
			{
				syncObject.wait();
			}
			return;
		}
	}

	public boolean countDown()
	{
		synchronized (syncObject)
		{
			if (count == 0)
			{
				throw new IllegalStateException("Latch already " + count);
			}
			count--;
			if (count == 0)
			{
				syncObject.notifyAll();
				return true;
			}
			return false;
		}
	}

	public int getCount()
	{
		return count;
	}
}
