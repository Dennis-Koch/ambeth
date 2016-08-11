package de.osthus.ambeth.bytebuffer;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.locks.Lock;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.util.PhantomReferenceCleaner;

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
