package de.osthus.ambeth.log;

import java.sql.Connection;
import java.sql.SQLException;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.persistence.jdbc.connection.IConnectionKeyHandle;

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
