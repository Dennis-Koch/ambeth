package com.koch.ambeth.persistence.database;

/*-
 * #%L
 * jambeth-persistence
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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;

public class DatabaseSessionIdController implements IInitializingBean, IDatabaseSessionIdController
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final Set<Long> sessionIdsInUse = new HashSet<Long>();

	protected final Lock writeLock = new ReentrantLock();

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		// Intended blank
	}

	private static long id2;

	@Override
	public long acquireSessionId()
	{
		writeLock.lock();
		try
		{
			return ++id2;
			// Random random = new Random();
			//
			// while (true)
			// {
			// long randomSessionId = (long) (random.nextDouble() * Long.MAX_VALUE);
			// if (sessionIdsInUse.add(randomSessionId))
			// {
			// return randomSessionId;
			// }
			// }
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
			// if (!sessionIdsInUse.remove(sessionId))
			// {
			// throw new IllegalArgumentException("No session with id '" + sessionId + "' currently acquired to release");
			// }
		}
		finally
		{
			writeLock.unlock();
		}
	}
}
