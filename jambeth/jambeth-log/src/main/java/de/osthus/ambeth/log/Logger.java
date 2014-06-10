package de.osthus.ambeth.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.osthus.ambeth.config.Properties;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.util.ParamChecker;
import de.osthus.ambeth.util.SystemUtil;

public class Logger implements IConfigurableLogger
{
	public static IThreadLocalObjectCollector objectCollector;

	protected static final DateFormat format = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss.SSS]");

	private static boolean logStreamEnabled;

	private static boolean appendModeActive;

	protected static Writer loggerStream;

	protected static final Lock streamWriteLock = new ReentrantLock();

	protected static final Lock formatWriteLock = new ReentrantLock();

	static
	{
		logStreamEnabled = true;
	}

	protected static Writer getLoggerStream()
	{
		if (loggerStream == null && logStreamEnabled)
		{
			String logfile = Properties.getApplication().getString(LogConfigurationConstants.LogFile);
			if (logfile != null)
			{
				File logfileHandle = new File(logfile);
				File parentFile = logfileHandle.getParentFile();
				if (parentFile != null && !parentFile.exists())
				{
					parentFile.mkdirs();
				}
				try
				{
					loggerStream = new BufferedWriter(new FileWriter(logfileHandle, appendModeActive));
				}
				catch (IOException e)
				{
					throw RuntimeExceptionUtil.mask(e);
				}
			}
		}
		return loggerStream;
	}

	protected static void setLoggerStream(Writer value)
	{
		if (loggerStream != null)
		{
			try
			{
				loggerStream.close();
			}
			catch (IOException e)
			{
				// Intended blank
			}
		}
		loggerStream = value;
	}

	public static boolean isAppendModeActive()
	{
		return appendModeActive;
	}

	public static void setAppendModeActive(boolean appendModeActive)
	{
		Logger.appendModeActive = appendModeActive;
	}

	public static boolean isLogStreamEnabled()
	{
		return logStreamEnabled;
	}

	public static void setLogStreamEnabled(boolean logStreamEnabled)
	{
		Logger.logStreamEnabled = logStreamEnabled;
	}

	protected boolean isDebugEnabled = true, isInfoEnabled = true, isWarnEnabled = true, isErrorEnabled = true, logToConsole = true;

	private final String source, shortSource;

	protected LogSourceLevel logSourceLevel = LogSourceLevel.DEFAULT;

	public Logger(String source)
	{
		this.source = source;
		int lastIndexOf = source.lastIndexOf('.');
		if (lastIndexOf >= 0)
		{
			shortSource = source.substring(lastIndexOf + 1);
		}
		else
		{
			shortSource = source;
		}
	}

	protected DateFormat getFormat()
	{
		return format;
	}

	@Override
	public boolean isDebugEnabled()
	{
		return isDebugEnabled;
	}

	@Override
	public void setDebugEnabled(boolean value)
	{
		isDebugEnabled = value;
	}

	@Override
	public LogSourceLevel getLogSourceLevel()
	{
		return logSourceLevel;
	}

	@Override
	public void setLogSourceLevel(LogSourceLevel logSourceLevel)
	{
		ParamChecker.assertParamNotNull(logSourceLevel, "logSourceLevel");
		this.logSourceLevel = logSourceLevel;
	}

	@Override
	public boolean getLogToConsole()
	{
		return logToConsole;
	}

	@Override
	public void setLogToConsole(boolean logToConsole)
	{
		this.logToConsole = logToConsole;
	}

	@Override
	public boolean isInfoEnabled()
	{
		return isInfoEnabled;
	}

	@Override
	public void setInfoEnabled(boolean value)
	{
		isInfoEnabled = value;
	}

	@Override
	public boolean isWarnEnabled()
	{
		return isWarnEnabled;
	}

	@Override
	public void setWarnEnabled(boolean value)
	{
		isWarnEnabled = value;
	}

	@Override
	public boolean isErrorEnabled()
	{
		return isErrorEnabled;
	}

	@Override
	public void setErrorEnabled(boolean value)
	{
		isErrorEnabled = value;
	}

	@Override
	public void info(CharSequence message)
	{
		if (!isInfoEnabled())
		{
			return;
		}
		addNotification(LogLevel.INFO, message);
	}

	@Override
	public void info(CharSequence message, Throwable e)
	{
		if (!isInfoEnabled())
		{
			return;
		}
		addNotification(LogLevel.INFO, message, e);
	}

	@Override
	public void info(Throwable e)
	{
		if (!isInfoEnabled())
		{
			return;
		}
		addNotification(LogLevel.INFO, e);
	}

	@Override
	public void debug(CharSequence message)
	{
		if (!isDebugEnabled())
		{
			return;
		}
		addNotification(LogLevel.DEBUG, message);
	}

	@Override
	public void debug(CharSequence message, Throwable e)
	{
		if (!isDebugEnabled())
		{
			return;
		}
		addNotification(LogLevel.DEBUG, message, e);
	}

	@Override
	public void debug(Throwable e)
	{
		if (!isDebugEnabled())
		{
			return;
		}
		addNotification(LogLevel.DEBUG, e);
	}

	@Override
	public void warn(CharSequence message)
	{
		if (!isWarnEnabled())
		{
			return;
		}
		addNotification(LogLevel.WARN, message);
	}

	@Override
	public void warn(CharSequence message, Throwable e)
	{
		if (!isWarnEnabled())
		{
			return;
		}
		addNotification(LogLevel.WARN, message, e);
	}

	@Override
	public void warn(Throwable e)
	{
		if (!isWarnEnabled())
		{
			return;
		}
		addNotification(LogLevel.WARN, e);
	}

	@Override
	public void error(CharSequence message)
	{
		if (!isErrorEnabled())
		{
			return;
		}
		addNotification(LogLevel.ERROR, message);
	}

	@Override
	public void error(CharSequence message, Throwable e)
	{
		if (!isErrorEnabled())
		{
			return;
		}
		addNotification(LogLevel.ERROR, message, e);
	}

	@Override
	public void error(Throwable e)
	{
		if (!isErrorEnabled())
		{
			return;
		}
		addNotification(LogLevel.ERROR, e);
	}

	protected void addNotification(LogLevel level, Throwable e)
	{
		addNotification(level, null, e);
	}

	protected void addNotification(LogLevel level, CharSequence message)
	{
		addNotification(level, message, (Exception) null);
	}

	protected void addNotification(LogLevel level, CharSequence message, Throwable e)
	{
		if (e != null)
		{
			addNotification(level, message, e.getClass().getName() + ": " + e.getMessage(), extractFullStackTrace(e));
		}
		else
		{
			addNotification(level, message, null, null);
		}
	}

	protected void printThrowable(Throwable e, StringBuilder sb, String newLine, boolean printHeader)
	{
		StackTraceElement[] stackTrace = e.getStackTrace();

		if (printHeader)
		{
			sb.append(e.getClass().getName()).append(": ").append(e.getMessage()).append(newLine);
		}
		for (int a = 0, size = stackTrace.length; a < size; a++)
		{
			sb.append('\t').append(stackTrace[a].toString());
			if (a + 1 < size)
			{
				sb.append(newLine);
			}
		}
	}

	protected String extractFullStackTrace(Throwable throwable)
	{
		String newLine = System.getProperty("line.separator");

		StringBuilder sb = acquireStringBuilder();
		try
		{
			boolean printHeader = false;
			Throwable currentThrowable = throwable;
			while (currentThrowable != null)
			{
				printThrowable(currentThrowable, sb, newLine, printHeader);
				printHeader = true;
				currentThrowable = currentThrowable.getCause();
				if (currentThrowable != null)
				{
					sb.append(newLine);
				}
			}
			return sb.toString();
		}
		finally
		{
			dispose(sb);
		}
	}

	protected void addNotification(LogLevel level, CharSequence message, String errorMessage, String stackTrace)
	{
		String newLine = SystemUtil.lineSeparator();
		StringBuilder sb = acquireStringBuilder();
		try
		{
			if (errorMessage != null)
			{
				if (message != null)
				{
					sb.append(message).append(": ");
				}
				if (stackTrace != null)
				{
					sb.append(errorMessage).append(newLine).append(stackTrace);
				}
				else
				{
					sb.append(errorMessage);
				}
			}
			else
			{
				if (message != null)
				{
					sb.append(message);
				}
			}
			createLogEntry(level, sb.toString());
		}
		finally
		{
			dispose(sb);
		}
	}

	protected void createLogEntry(LogLevel logLevel, String notification)
	{
		Thread currentThread = Thread.currentThread();

		String threadName = currentThread.getName();
		if (threadName == null || threadName.length() == 0)
		{
			threadName = "<No Name>";
		}
		long now = System.currentTimeMillis();

		Date date = new Date(now);
		// TODO: Better formatting here
		// String output = String
		// .Format("[{4,2}: {5}] [{0:yyyy-MM-dd HH:mm:ss.fff}] {1,-5} {2}: {3}", new Object[] {
		// now, level, _source, notification,
		// currentThread.ManagedThreadId, threadName });

		StringBuilder sb = acquireStringBuilder();
		try
		{
			String dateString;
			// DateFormat is not thread-safe
			DateFormat format = getFormat();
			formatWriteLock.lock();
			try
			{
				dateString = format.format(date);
			}
			finally
			{
				formatWriteLock.unlock();
			}
			sb.append('[').append(currentThread.getId()).append(": ").append(threadName).append("] ");

			String printedSource;
			switch (logSourceLevel)
			{
				case DEFAULT:
				case FULL:
					printedSource = source;
					break;
				case SHORT:
					printedSource = shortSource;
					break;
				case NONE:
					printedSource = null;
					break;
				default:
					throw new IllegalStateException("Enum " + logSourceLevel + " not supported");
			}

			sb.append(dateString).append(' ').append(logLevel.name());
			for (int a = logLevel.name().length(); a < 5; a++)
			{
				sb.append(' ');
			}
			if (printedSource != null)
			{
				sb.append(' ').append(printedSource);
			}
			sb.append(": ").append(notification);
			log(logLevel, sb.toString());
		}
		finally
		{
			dispose(sb);
		}
	}

	protected StringBuilder acquireStringBuilder()
	{
		IThreadLocalObjectCollector localObjectCollector = objectCollector;
		if (localObjectCollector == null)
		{
			return new StringBuilder();
		}
		return localObjectCollector.create(StringBuilder.class);
	}

	protected void dispose(StringBuilder sb)
	{
		IThreadLocalObjectCollector localObjectCollector = objectCollector;
		if (localObjectCollector == null)
		{
			return;
		}
		localObjectCollector.dispose(sb);
	}

	protected void log(LogLevel logLevel, String output)
	{
		boolean errorLog = LogLevel.WARN.equals(logLevel) || LogLevel.ERROR.equals(logLevel);
		if (logToConsole)
		{
			if (errorLog)
			{
				System.err.println(output);
			}
			else
			{
				System.out.println(output);
			}
		}
		if (logStreamEnabled)
		{
			logStream(logLevel, output, errorLog);
		}
	}

	protected void logStream(LogLevel logLevel, String output, boolean autoFlush)
	{
		String lineSeparator = SystemUtil.lineSeparator();
		streamWriteLock.lock();
		try
		{
			Writer writer = getLoggerStream();
			if (writer == null)
			{
				return;
			}
			try
			{
				writer.write(lineSeparator);
				writer.write(output);
				if (autoFlush)
				{
					writer.flush();
				}
			}
			catch (IOException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
		}
		finally
		{
			streamWriteLock.unlock();
		}
	}
}
