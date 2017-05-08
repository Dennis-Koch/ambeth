package com.koch.ambeth.persistence.log;

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

import java.sql.Connection;
import java.sql.SQLException;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.ILoggerHistory;
import com.koch.ambeth.persistence.connection.IConnectionKeyHandle;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public final class PersistenceWarnUtil {
	public static void logDebugOnce(ILogger log, ILoggerHistory loggerHistory, Connection connection,
			String text) {
		IConnectionKeyHandle key;
		try {
			key = connection.unwrap(IConnectionKeyHandle.class);
		}
		catch (SQLException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		if (loggerHistory.addLogHistory(log, key, text)) {
			log.debug(text);
		}
	}

	public static void logInfoOnce(ILogger log, ILoggerHistory loggerHistory, Connection connection,
			String text) {
		IConnectionKeyHandle key;
		try {
			key = connection.unwrap(IConnectionKeyHandle.class);
		}
		catch (SQLException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		if (loggerHistory.addLogHistory(log, key, text)) {
			log.info(text);
		}
	}

	public static void logWarnOnce(ILogger log, ILoggerHistory loggerHistory, Connection connection,
			String text) {
		IConnectionKeyHandle key;
		try {
			key = connection.unwrap(IConnectionKeyHandle.class);
		}
		catch (SQLException e) {
			throw RuntimeExceptionUtil.mask(e);
		}
		if (loggerHistory.addLogHistory(log, key, text)) {
			log.warn(text);
		}
	}

	private PersistenceWarnUtil() {
		// intended blank
	}
}
