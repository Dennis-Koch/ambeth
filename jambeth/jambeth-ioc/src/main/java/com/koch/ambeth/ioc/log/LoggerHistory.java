package com.koch.ambeth.ioc.log;

/*-
 * #%L
 * jambeth-ioc
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

import java.lang.ref.WeakReference;
import java.util.concurrent.locks.ReentrantLock;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.ILoggerHistory;
import com.koch.ambeth.util.collections.CleanupInvalidKeysSet;
import com.koch.ambeth.util.collections.IInvalidKeyChecker;

public class LoggerHistory
		implements IInitializingBean, ILoggerHistory, IInvalidKeyChecker<LoggerHistoryKey> {
	protected final CleanupInvalidKeysSet<LoggerHistoryKey> logHistory =
			new CleanupInvalidKeysSet<>(this, 0.5f);

	protected final ReentrantLock lock = new ReentrantLock();

	@Override
	public void afterPropertiesSet() throws Throwable {
		// Intended blank
	}

	@Override
	public boolean isKeyValid(LoggerHistoryKey key) {
		return key.isValid();
	}

	@Override
	public boolean addLogHistory(ILogger logger, Object contextHandle, String logTextForHistory) {
		LoggerHistoryKey key =
				new LoggerHistoryKey(logger, new WeakReference<>(contextHandle), logTextForHistory);
		ReentrantLock writeLock = lock;
		writeLock.lock();
		try {
			return logHistory.add(key);
		}
		finally {
			writeLock.unlock();
		}
	}

	@Override
	public boolean debugOnce(ILogger log, Object contextHandle, String logTextForHistory) {
		if (!addLogHistory(log, contextHandle, logTextForHistory)) {
			return false;
		}
		log.debug(logTextForHistory);
		return true;
	}

	@Override
	public boolean infoOnce(ILogger log, Object contextHandle, String logTextForHistory) {
		if (!addLogHistory(log, contextHandle, logTextForHistory)) {
			return false;
		}
		log.info(logTextForHistory);
		return true;
	}

	@Override
	public boolean warnOnce(ILogger log, Object contextHandle, String logTextForHistory) {
		if (!addLogHistory(log, contextHandle, logTextForHistory)) {
			return false;
		}
		log.warn(logTextForHistory);
		return true;
	}

	@Override
	public boolean errorOnce(ILogger log, Object contextHandle, String logTextForHistory) {
		if (!addLogHistory(log, contextHandle, logTextForHistory)) {
			return false;
		}
		log.error(logTextForHistory);
		return true;
	}
}
