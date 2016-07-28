package de.osthus.ambeth.log;

import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public final class LoggerFactory
{
	public static final String loggerPrefix = "ambeth.log.";

	public static final String logLevelPropertyName = "level";

	public static final String logConsolePropertyName = "console";

	public static final String logSourcePropertyName = "source";

	public static final String logLevelPropertyPrefix = loggerPrefix + logLevelPropertyName;

	public static final String logConsolePropertyPrefix = loggerPrefix + logConsolePropertyName;

	public static final String logSourcePropertyPrefix = loggerPrefix + logSourcePropertyName;

	public static final String logLevelDebug = "debug";

	public static final String logLevelInfo = "info";

	public static final String logLevelWarn = "warn";

	public static final String logLevelError = "error";

	protected static Class<? extends ILogger> loggerType;

	static
	{
		LoggerFactory.loggerType = Logger.class;
	}

	public static String logConsoleProperty(Class<?> type)
	{
		return logConsolePropertyPrefix + '.' + type.getName();
	}

	public static String logLevelProperty(Class<?> type)
	{
		return logLevelPropertyPrefix + '.' + type.getName();
	}

	public static String logSourceProperty(Class<?> type)
	{
		return logSourcePropertyPrefix + '.' + type.getName();
	}

	public static void setLoggerType(Class<? extends ILogger> loggerType)
	{
		if (loggerType == null)
		{
			throw new IllegalArgumentException("LoggerType must be derived from '" + ILogger.class.getName() + "'");
		}
		LoggerFactory.loggerType = loggerType;
	}

	public static ILogger getLogger(String source)
	{
		try
		{
			ILogger logger = LoggerFactory.loggerType.getConstructor(String.class).newInstance(source);

			if (logger instanceof IConfigurableLogger)
			{
				LoggerFactory.configureLogger(source, (IConfigurableLogger) logger);
			}
			return logger;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	public static ILogger getLogger(Class<?> source)
	{
		return getLogger(source, Properties.getApplication());
	}

	public static ILogger getLogger(Class<?> source, IProperties props)
	{
		if (props == null)
		{
			props = Properties.getApplication();
		}
		try
		{
			ILogger logger = LoggerFactory.loggerType.getConstructor(String.class).newInstance(source.getName());

			if (logger instanceof IConfigurableLogger)
			{
				LoggerFactory.configureLogger(source.getName(), (IConfigurableLogger) logger, props);
			}
			return logger;
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	public static void configureLogger(String loggerName, IConfigurableLogger logger)
	{
		configureLogger(loggerName, logger, Properties.getApplication());
	}

	public static void configureLogger(String loggerName, IConfigurableLogger logger, IProperties appProps)
	{
		String logLevelValue = null;
		String logConsoleValue = null;
		String logSourceValue = null;
		int lastDot = loggerName.length();
		StringBuilder tempSB = new StringBuilder(loggerPrefix.length()
				+ Math.max(Math.max(logLevelPropertyName.length(), logConsolePropertyName.length()), logSourcePropertyName.length()) + 1 + loggerName.length());
		tempSB.append(loggerPrefix);

		while (lastDot != -1)
		{
			String prefixOfLoggerName = loggerName.substring(0, lastDot);

			if (logLevelValue == null)
			{
				String key = buildKey(logLevelPropertyName, prefixOfLoggerName, tempSB, false);
				logLevelValue = appProps.getString(key);
			}
			if (logConsoleValue == null)
			{
				String key = buildKey(logConsolePropertyName, prefixOfLoggerName, tempSB, false);
				logConsoleValue = appProps.getString(key);
			}
			if (logSourceValue == null)
			{
				String key = buildKey(logSourcePropertyName, prefixOfLoggerName, tempSB, false);
				logSourceValue = appProps.getString(key);
			}
			String key = buildKey(logLevelPropertyName, prefixOfLoggerName, tempSB, true);
			String overridingValue = appProps.getString(key);
			if (overridingValue != null)
			{
				logLevelValue = overridingValue;
			}
			key = buildKey(logConsolePropertyName, prefixOfLoggerName, tempSB, true);
			overridingValue = appProps.getString(key);
			if (overridingValue != null)
			{
				logConsoleValue = overridingValue;
			}
			key = buildKey(logSourcePropertyName, prefixOfLoggerName, tempSB, true);
			overridingValue = appProps.getString(key);
			if (overridingValue != null)
			{
				logSourceValue = overridingValue;
			}

			int previousDot = -1;
			for (int a = lastDot; a-- > 0;)
			{
				if (loggerName.charAt(a) == '.')
				{
					previousDot = a;
					break;
				}
			}
			lastDot = previousDot;
		}
		if (logLevelDebug.equalsIgnoreCase(logLevelValue))
		{
			logger.setDebugEnabled(true);
			logger.setInfoEnabled(true);
			logger.setWarnEnabled(true);
			logger.setErrorEnabled(true);
		}
		else if (logLevelInfo.equalsIgnoreCase(logLevelValue))
		{
			logger.setDebugEnabled(false);
			logger.setInfoEnabled(true);
			logger.setWarnEnabled(true);
			logger.setErrorEnabled(true);
		}
		else if (logLevelWarn.equalsIgnoreCase(logLevelValue) || logLevelValue == null)
		{
			// if nothing is configured the logger defaults to "warn" level
			logger.setDebugEnabled(false);
			logger.setInfoEnabled(false);
			logger.setWarnEnabled(true);
			logger.setErrorEnabled(true);
		}
		else if (logLevelError.equalsIgnoreCase(logLevelValue))
		{
			logger.setDebugEnabled(false);
			logger.setInfoEnabled(false);
			logger.setWarnEnabled(false);
			logger.setErrorEnabled(true);
		}
		if (logConsoleValue != null)
		{
			logger.setLogToConsole(Boolean.parseBoolean(logConsoleValue));
		}
		if (logSourceValue != null)
		{
			logger.setLogSourceLevel(LogSourceLevel.valueOf(logSourceValue.toUpperCase()));
		}
		logger.postProcess(appProps);
	}

	private static String buildKey(String loggerKey, String loggerName, StringBuilder tempSB, boolean overridingKey)
	{
		tempSB.setLength(loggerPrefix.length());
		tempSB.append(loggerKey);
		if (loggerName.length() > 0)
		{
			tempSB.append('.');
			tempSB.append(loggerName);
		}
		if (overridingKey)
		{
			tempSB.append('.');
			tempSB.append('*');
		}
		return tempSB.toString();
	}

	private LoggerFactory()
	{
		// intended blank
	}
}
