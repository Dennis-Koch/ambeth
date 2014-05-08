package de.osthus.ambeth.log;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.collections.LinkedHashSet;
import de.osthus.ambeth.collections.SetLinkedIterator;
import de.osthus.ambeth.ioc.IInitializingBean;

public class LoggerHistory implements IInitializingBean, ILoggerHistory
{
	public static class LoggerHistoryKey
	{
		public final Reference<ILogger> logger;

		public final Reference<Object> contextHandle;

		public final String logTextForHistory;

		public final int hash;

		public LoggerHistoryKey(Reference<ILogger> logger, Reference<Object> contextHandle, String logTextForHistory)
		{
			this.logger = logger;
			this.contextHandle = contextHandle;
			this.logTextForHistory = logTextForHistory;
			int hash = 11;
			ILogger loggerR = logger.get();
			if (loggerR != null)
			{
				hash ^= loggerR.hashCode();
			}
			Object contextHandleR = contextHandle.get();
			if (contextHandleR != null)
			{
				hash ^= contextHandleR.hashCode();
			}
			this.hash = hash ^ logTextForHistory.hashCode();
		}

		public boolean isValid()
		{
			return logger.get() != null && contextHandle.get() != null;
		}

		@Override
		public int hashCode()
		{
			// Hash MUST be precalculated because of hash requirement for removal after the Refs are null
			// (and therefore the hash would have changed)
			return hash;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == this)
			{
				return true;
			}
			if (!(obj instanceof LoggerHistoryKey))
			{
				return false;
			}
			Object contextHandle = this.contextHandle.get();
			if (contextHandle == null)
			{
				// Cleanup contextHandle is never equal with anything
				return false;
			}
			ILogger logger = this.logger.get();
			if (logger == null)
			{
				// Cleanup logger is never equal with anything
				return false;
			}
			LoggerHistoryKey other = (LoggerHistoryKey) obj;
			Object contextHandle2 = other.contextHandle.get();
			if (contextHandle2 == null)
			{
				// Cleanup contextHandle is never equal with anything
				return false;
			}
			ILogger logger2 = other.logger.get();
			if (logger2 == null)
			{
				// Cleanup logger is never equal with anything
				return false;
			}
			return logTextForHistory.equals(other.logTextForHistory) && contextHandle.equals(contextHandle2) && logger.equals(logger2);
		}
	}

	protected final LinkedHashSet<LoggerHistoryKey> logHistory = new LinkedHashSet<LoggerHistoryKey>(0.5f);

	protected int modCount = 0;

	protected int cleanupModInterval = 1000;

	protected final ReentrantLock lock = new ReentrantLock();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intended blank
	}

	@Override
	public boolean addLogHistory(ILogger logger, Object contextHandle, String logTextForHistory)
	{
		LoggerHistoryKey key = new LoggerHistoryKey(new WeakReference<ILogger>(logger), new WeakReference<Object>(contextHandle), logTextForHistory);
		ReentrantLock writeLock = this.lock;
		writeLock.lock();
		try
		{
			if (!logHistory.add(key))
			{
				return false;
			}
			if (++modCount % cleanupModInterval == 0)
			{
				SetLinkedIterator<LoggerHistoryKey> iter = logHistory.iterator();
				while (iter.hasNext())
				{
					LoggerHistoryKey existingKey = iter.next();
					if (!existingKey.isValid())
					{
						iter.remove();
					}
				}
			}
			return true;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public boolean debugOnce(ILogger log, Object contextHandle, String logTextForHistory)
	{
		if (!addLogHistory(log, contextHandle, logTextForHistory))
		{
			return false;
		}
		log.debug(logTextForHistory);
		return true;
	}

	@Override
	public boolean infoOnce(ILogger log, Object contextHandle, String logTextForHistory)
	{
		if (!addLogHistory(log, contextHandle, logTextForHistory))
		{
			return false;
		}
		log.info(logTextForHistory);
		return true;
	}

	@Override
	public boolean warnOnce(ILogger log, Object contextHandle, String logTextForHistory)
	{
		if (!addLogHistory(log, contextHandle, logTextForHistory))
		{
			return false;
		}
		log.warn(logTextForHistory);
		return true;
	}

	@Override
	public boolean errorOnce(ILogger log, Object contextHandle, String logTextForHistory)
	{
		if (!addLogHistory(log, contextHandle, logTextForHistory))
		{
			return false;
		}
		log.error(logTextForHistory);
		return true;
	}
}