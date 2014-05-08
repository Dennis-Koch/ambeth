package de.osthus.ambeth.log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;

public final class LoggerFactory
{
	public static final String logLevelPropertyName = "level";

	public static final String logConsolePropertyName = "console";

	public static final String logSourcePropertyName = "source";

	public static final String logLevelDebug = "debug";

	public static final String logLevelInfo = "info";

	public static final String logLevelWarn = "warn";

	public static final String logLevelError = "error";

	protected static final Pattern logRegex = Pattern.compile("ambeth\\.log\\.(" + logLevelPropertyName + "|" + logConsolePropertyName + "|"
			+ logSourcePropertyName + ")(?:\\.(.+))?");

	protected static Class<? extends ILogger> loggerType;

	static
	{
		LoggerFactory.loggerType = Logger.class;
	}

	public static String logConsoleProperty(Class<?> type)
	{
		return "ambeth.log." + logConsolePropertyName + "." + type.getName();
	}

	public static String logLevelProperty(Class<?> type)
	{
		return "ambeth.log." + logLevelPropertyName + "." + type.getName();
	}

	public static String logSourceProperty(Class<?> type)
	{
		return "ambeth.log." + logSourcePropertyName + "." + type.getName();
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
		HashSet<String> allPropertiesSet = new HashSet<String>();
		appProps.collectAllPropertyKeys(allPropertiesSet);
		int highestPrecisionFound = 0;
		for (String key : allPropertiesSet)
		{
			Matcher matcher = LoggerFactory.logRegex.matcher(key);
			if (!matcher.matches())
			{
				continue;
			}
			String type = matcher.group(1);
			String target = matcher.group(2);
			if (target != null && !loggerName.startsWith(target))
			{
				continue;
			}
			if (target == null)
			{
				target = "";
			}
			if (logLevelPropertyName.equals(type))
			{
				if (target.length() < highestPrecisionFound)
				{
					continue;
				}
				highestPrecisionFound = target.length();
				String value = appProps.getString(key).toLowerCase();
				if (logLevelDebug.equals(value))
				{
					logger.setDebugEnabled(true);
					logger.setInfoEnabled(true);
					logger.setWarnEnabled(true);
					logger.setErrorEnabled(true);
				}
				else if (logLevelInfo.equals(value))
				{
					logger.setDebugEnabled(false);
					logger.setInfoEnabled(true);
					logger.setWarnEnabled(true);
					logger.setErrorEnabled(true);
				}
				else if (logLevelWarn.equals(value))
				{
					logger.setDebugEnabled(false);
					logger.setInfoEnabled(false);
					logger.setWarnEnabled(true);
					logger.setErrorEnabled(true);
				}
				else if (logLevelError.equals(value))
				{
					logger.setDebugEnabled(false);
					logger.setInfoEnabled(false);
					logger.setWarnEnabled(false);
					logger.setErrorEnabled(true);
				}
			}
			else if (logConsolePropertyName.equals(type))
			{
				String value = appProps.getString(key).toLowerCase();
				logger.setLogToConsole(Boolean.parseBoolean(value));
			}
			else if (logSourcePropertyName.equals(type))
			{
				String value = appProps.getString(key).toUpperCase();
				logger.setLogSourceLevel(LogSourceLevel.valueOf(value));
			}
			else
			{
				throw new IllegalStateException("Property: " + key + " not supported");
			}
		}
	}

	private LoggerFactory()
	{
		// intended blank
	}
}
