package com.koch.ambeth.persistence.log;

import java.sql.Connection;
import java.sql.SQLException;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.ILoggerHistory;
import com.koch.ambeth.persistence.connection.IConnectionKeyHandle;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public final class PersistenceWarnUtil
{
	public static void logDebugOnce(ILogger log, ILoggerHistory loggerHistory, Connection connection, String text)
	{
		IConnectionKeyHandle key;
		try
		{
			key = connection.unwrap(IConnectionKeyHandle.class);
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		if (loggerHistory.addLogHistory(log, key, text))
		{
			log.debug(text);
		}
	}

	public static void logInfoOnce(ILogger log, ILoggerHistory loggerHistory, Connection connection, String text)
	{
		IConnectionKeyHandle key;
		try
		{
			key = connection.unwrap(IConnectionKeyHandle.class);
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		if (loggerHistory.addLogHistory(log, key, text))
		{
			log.info(text);
		}
	}

	public static void logWarnOnce(ILogger log, ILoggerHistory loggerHistory, Connection connection, String text)
	{
		IConnectionKeyHandle key;
		try
		{
			key = connection.unwrap(IConnectionKeyHandle.class);
		}
		catch (SQLException e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
		if (loggerHistory.addLogHistory(log, key, text))
		{
			log.warn(text);
		}
	}

	private PersistenceWarnUtil()
	{
		// intended blank
	}
}
