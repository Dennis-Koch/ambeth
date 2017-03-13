package com.koch.ambeth.util.objectcollector;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleLockingObjectCollectorItem extends SimpleObjectCollectorItem
{
	protected final Lock lock = new ReentrantLock();

	public SimpleLockingObjectCollectorItem(IObjectCollector objectCollector, ICollectableController collectableController, Class<?> constructorClass)
	{
		super(objectCollector, collectableController, constructorClass);
	}

	@Override
	protected Object popLastElement()
	{
		lock.lock();
		try
		{
			return super.popLastElement();
		}
		finally
		{
			lock.unlock();
		}
	}

	@Override
	public void dispose(final Object object)
	{
		lock.lock();
		try
		{
			super.dispose(object);
		}
		finally
		{
			lock.unlock();
		}
	}

	@Override
	public void cleanUp()
	{
		lock.lock();
		try
		{
			super.cleanUp();
		}
		finally
		{
			lock.unlock();
		}
	}
}
