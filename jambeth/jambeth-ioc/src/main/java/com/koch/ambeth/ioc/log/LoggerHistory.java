package com.koch.ambeth.ioc.log;

import java.lang.ref.WeakReference;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.ILoggerHistory;
import com.koch.ambeth.util.collections.CleanupInvalidKeysSet;
import com.koch.ambeth.util.collections.IInvalidKeyChecker;

public class LoggerHistory implements IInitializingBean, ILoggerHistory, IInvalidKeyChecker<LoggerHistoryKey>
{
	protected final CleanupInvalidKeysSet<LoggerHistoryKey> logHistory = new CleanupInvalidKeysSet<LoggerHistoryKey>(this, 0.5f);

	protected final ReentrantLock lock = new ReentrantLock();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intended blank
	}

	@Override
	public boolean isKeyValid(LoggerHistoryKey key)
	{
		return key.isValid();
	}

	@Override
	public boolean addLogHistory(ILogger logger, Object contextHandle, String logTextForHistory)
	{
		LoggerHistoryKey key = new LoggerHistoryKey(logger, new WeakReference<Object>(contextHandle), logTextForHistory);
		ReentrantLock writeLock = this.lock;
		writeLock.lock();
		try
		{
			return logHistory.add(key);
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