package de.osthus.ambeth.database;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.ioc.IInitializingBean;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;

public class DatabaseSessionIdController implements IInitializingBean, IDatabaseSessionIdController
{
	@SuppressWarnings("unused")
	@LogInstance(DatabaseSessionIdController.class)
	private ILogger log;

	protected final Set<Long> sessionIdsInUse = new HashSet<Long>();

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intended blank
	}

	@Override
	public long acquireSessionId()
	{
		writeLock.lock();
		try
		{
			Random random = new Random();

			while (true)
			{
				long randomSessionId = (long) (random.nextDouble() * Long.MAX_VALUE);
				if (sessionIdsInUse.add(randomSessionId))
				{
					return randomSessionId;
				}
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void releaseSessionId(long sessionId)
	{
		writeLock.lock();
		try
		{
			if (!sessionIdsInUse.remove(sessionId))
			{
				throw new IllegalArgumentException("No session with id '" + sessionId + "' currently acquired to release");
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
