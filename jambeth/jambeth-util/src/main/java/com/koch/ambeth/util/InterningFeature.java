package com.koch.ambeth.util;

import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.util.collections.WeakHashSet;

public class InterningFeature extends WeakHashSet<Object> implements IInterningFeature
{
	protected final ReentrantLock writeLock = new ReentrantLock();

	public InterningFeature()
	{
		super(0.5f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T intern(T value)
	{
		if (value == null)
		{
			return null;
		}
		ReentrantLock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			Object internedValue = get(value);
			if (internedValue == null)
			{
				internedValue = value;
				add(internedValue);
			}
			return (T) internedValue;
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
