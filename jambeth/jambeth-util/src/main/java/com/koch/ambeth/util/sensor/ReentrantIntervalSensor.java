package com.koch.ambeth.util.sensor;

/*-
 * #%L
 * jambeth-util
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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.util.collections.ArrayList;

public abstract class ReentrantIntervalSensor<I extends IntervalInfo> implements ISensorReceiver
{
	protected final ThreadLocal<ArrayList<I>> stackTL = new ThreadLocal<ArrayList<I>>()
	{
		@Override
		protected ArrayList<I> initialValue()
		{
			return new ArrayList<I>(1);
		}
	};

	protected final ArrayList<I> sharedInfos = new ArrayList<I>();

	protected final Lock writeLock = new ReentrantLock();

	@SuppressWarnings("unchecked")
	protected I createIntervalInfo(String sensorName)
	{
		return (I) new IntervalInfo(System.currentTimeMillis());
	}

	@SuppressWarnings("unchecked")
	protected I createIntervalInfo(String sensorName, Object[] additionalData)
	{
		return (I) new IntervalInfo(System.currentTimeMillis());
	}

	@Override
	public void touch(String sensorName)
	{
		// Intended blank. An interval sensor does not handle information without interval-context
	}

	@Override
	public void touch(String sensorName, Object... additionalData)
	{
		// Intended blank. An interval sensor does not handle information without interval-context
	}

	@Override
	public void on(String sensorName, Object... additionalData)
	{
		ArrayList<I> stack = stackTL.get();
		I intervalInfo = createIntervalInfo(sensorName, additionalData);
		stack.add(intervalInfo);
		if (intervalInfo == null)
		{
			return;
		}
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			sharedInfos.add(intervalInfo);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void on(String sensorName)
	{
		ArrayList<I> stack = stackTL.get();
		I intervalInfo = createIntervalInfo(sensorName);
		stack.add(intervalInfo);
		if (intervalInfo == null)
		{
			return;
		}
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			sharedInfos.add(intervalInfo);
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void off(String sensorName)
	{
		long endTime = System.currentTimeMillis();
		ArrayList<I> stack = stackTL.get();
		I intervalInfo = stack.remove(stack.size() - 1);
		if (intervalInfo == null)
		{
			return;
		}
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			sharedInfos.remove(intervalInfo);
		}
		finally
		{
			writeLock.unlock();
		}
		handleFinishedIntervalInfo(sensorName, intervalInfo, endTime);
	}

	protected abstract void handleFinishedIntervalInfo(String sensorName, I intervalInfo, long endTime);
}
