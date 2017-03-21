package com.koch.ambeth.cache.stream.bytebuffer;

/*-
 * #%L
 * jambeth-cache-stream
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

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.locks.Lock;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.util.PhantomReferenceCleaner;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class FileContentPRC extends PhantomReferenceCleaner<ByteBuffer, ChunkPhantomReference>
{
	private final ILogger log;

	private int cleanupCounter;

	private final int cleanupCounterThreshold;

	@SuppressWarnings("unused")
	private final double freePhysicalMemoryRatio;

	private long lastCleanup = System.currentTimeMillis();

	public FileContentPRC(ILogger log, int cleanupCounterThreshold, double freePhysicalMemoryRatio)
	{
		this.log = log;
		this.cleanupCounterThreshold = cleanupCounterThreshold;
		this.freePhysicalMemoryRatio = freePhysicalMemoryRatio;
	}

	@Override
	protected void doCleanup(ChunkPhantomReference phantom)
	{
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			if (++cleanupCounter < cleanupCounterThreshold)
			{
				return;
			}
			cleanup();
		}
		finally
		{
			writeLock.unlock();
		}
	}

	protected void cleanup()
	{
		if (System.currentTimeMillis() - lastCleanup < 1000)
		{
			return;
		}
		cleanupCounter = 0;
		// OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
		// long committedVirtualMemorySize = (long) getValue(operatingSystemMXBean, "getCommittedVirtualMemorySize");
		// long totalSwapSpaceSize = (long) getValue(operatingSystemMXBean, "getTotalSwapSpaceSize");
		// long freeSwapSpaceSize = (long) getValue(operatingSystemMXBean, "getFreeSwapSpaceSize");
		// long freePhysicalMemorySize = (long) getValue(operatingSystemMXBean, "getFreePhysicalMemorySize");
		// long totalPhysicalMemorySize = (long) getValue(operatingSystemMXBean, "getTotalPhysicalMemorySize");

		// Workaround to do at least "something" against a "out of virtual address space" error because of uncleaned but already unused MappedByteBuffer
		// instances
		// if (freePhysicalMemorySize / (double) totalPhysicalMemorySize < freePhysicalMemoryRatio)
		// {
		System.gc();
		if (log.isInfoEnabled())
		{
			log.info("Requested System.gc()");
		}
		// }
		lastCleanup = System.currentTimeMillis();
	}

	protected Object getValue(Object obj, String getMethodName)
	{
		try
		{
			final Method method = obj.getClass().getMethod(getMethodName);
			AccessController.doPrivileged(new PrivilegedAction<Void>()
			{
				@Override
				public Void run()
				{
					method.setAccessible(true);
					return null;
				}
			});
			return method.invoke(obj);
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	public void increaseCleanupCounter()
	{
		Lock writeLock = this.writeLock;
		writeLock.lock();
		try
		{
			cleanupCounter++;
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
