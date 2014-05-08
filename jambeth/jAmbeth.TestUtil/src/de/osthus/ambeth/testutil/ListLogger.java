package de.osthus.ambeth.testutil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.log.IConfigurableLogger;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogSourceLevel;
import de.osthus.ambeth.util.SystemUtil;

public class ListLogger implements ILogger, IConfigurableLogger
{
	protected static final DateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");

	protected boolean debugEnabled = true, infoEnabled = true, warnEnabled = true, errorEnabled = true, logToConsole = true;

	private final String source, shortSource;

	protected LogSourceLevel logSourceLevel = LogSourceLevel.DEFAULT;

	protected IList<String> debugEntries = new ArrayList<String>(), infoEntries = new ArrayList<String>(), warnEntries = new ArrayList<String>(),
			errorEntries = new ArrayList<String>();

	public ListLogger(String source)
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

	public IList<String> getDebugEntries()
	{
		return debugEntries;
	}

	public IList<String> getInfoEntries()
	{
		return infoEntries;
	}

	public IList<String> getWarnEntries()
	{
		return warnEntries;
	}

	public IList<String> getErrorEntries()
	{
		return errorEntries;
	}

	@Override
	public boolean isDebugEnabled()
	{
		return debugEnabled;
	}

	@Override
	public void setDebugEnabled(boolean debugEnabled)
	{
		this.debugEnabled = debugEnabled;
	}

	@Override
	public boolean isInfoEnabled()
	{
		return infoEnabled;
	}

	@Override
	public void setInfoEnabled(boolean infoEnabled)
	{
		this.infoEnabled = infoEnabled;
	}

	@Override
	public boolean isWarnEnabled()
	{
		return warnEnabled;
	}

	@Override
	public void setWarnEnabled(boolean warnEnabled)
	{
		this.warnEnabled = warnEnabled;
	}

	@Override
	public boolean isErrorEnabled()
	{
		return errorEnabled;
	}

	@Override
	public void setErrorEnabled(boolean errorEnabled)
	{
		this.errorEnabled = errorEnabled;
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
	public LogSourceLevel getLogSourceLevel()
	{
		return logSourceLevel;
	}

	@Override
	public void setLogSourceLevel(LogSourceLevel logSourceLevel)
	{
		this.logSourceLevel = logSourceLevel;
	}

	@Override
	public void info(String message)
	{
		if (!isInfoEnabled())
		{
			return;
		}
		addNotification(INFO, message);
	}

	@Override
	public void info(String message, Throwable e)
	{
		if (!isInfoEnabled())
		{
			return;
		}
		addNotification(INFO, message, e);
	}

	@Override
	public void info(Throwable e)
	{
		if (!isInfoEnabled())
		{
			return;
		}
		addNotification(INFO, e);
	}

	@Override
	public void debug(String message)
	{
		if (!isDebugEnabled())
		{
			return;
		}
		addNotification(DEBUG, message);
	}

	@Override
	public void debug(String message, Throwable e)
	{
		if (!isDebugEnabled())
		{
			return;
		}
		addNotification(DEBUG, message, e);
	}

	@Override
	public void debug(Throwable e)
	{
		if (!isDebugEnabled())
		{
			return;
		}
		addNotification(DEBUG, e);
	}

	@Override
	public void warn(String message)
	{
		if (!isWarnEnabled())
		{
			return;
		}
		addNotification(WARN, message);
	}

	@Override
	public void warn(String message, Throwable e)
	{
		if (!isWarnEnabled())
		{
			return;
		}
		addNotification(WARN, message, e);
	}

	@Override
	public void warn(Throwable e)
	{
		if (!isWarnEnabled())
		{
			return;
		}
		addNotification(WARN, e);
	}

	@Override
	public void error(String message)
	{
		if (!isErrorEnabled())
		{
			return;
		}
		addNotification(ERROR, message);
	}

	@Override
	public void error(String message, Throwable e)
	{
		if (!isErrorEnabled())
		{
			return;
		}
		addNotification(ERROR, message, e);
	}

	@Override
	public void error(Throwable e)
	{
		if (!isErrorEnabled())
		{
			return;
		}
		addNotification(ERROR, e);
	}

	protected void addNotification(String level, Throwable e)
	{
		addNotification(level, null, e);
	}

	protected void addNotification(String level, String message)
	{
		addNotification(level, message, (Exception) null);
	}

	protected void addNotification(String level, String message, Throwable e)
	{
		if (e != null)
		{
			addNotification(level, message, true, e.getClass().getName() + ": " + e.getMessage(), extractFullStackTrace(e));
		}
		else
		{
			addNotification(level, message, false, null, null);
		}
	}

	protected void addNotification(String level, String message, boolean errorLog, String errorMessage, String stackTrace)
	{
		String newLine = SystemUtil.lineSeparator();
		StringBuilder sb = new StringBuilder();
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
		createLogEntry(level, errorLog, sb.toString());
	}

	protected String extractFullStackTrace(Throwable throwable)
	{
		String newLine = System.getProperty("line.separator");

		StringBuilder sb = new StringBuilder();
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

	protected void createLogEntry(String level, boolean errorLog, String notification)
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
		// .Format("[{5,2}: {6}] {0} {1} {2,-5} {3}: {4}", new Object[] {
		// now.ToLongTimeString(), now.Millisecond.ToString("D3"),
		// level, _source, notification,
		// currentThread.ManagedThreadId, threadName });

		StringBuilder sb = new StringBuilder();
		String dateString = format.format(date);
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

		sb.append(dateString).append(' ').append(level);
		if (printedSource != null)
		{
			sb.append(' ').append(printedSource);
		}
		sb.append(": ").append(notification);
		log(level, errorLog, sb.toString());
	}

	protected void log(String level, boolean errorLog, String output)
	{
		if (INFO.equals(level))
		{
			infoEntries.add(output);
		}
		else if (DEBUG.equals(level))
		{
			debugEntries.add(output);
		}
		else if (WARN.equals(level))
		{
			warnEntries.add(output);
		}
		else if (ERROR.equals(level))
		{
			errorEntries.add(output);
		}
		else
		{
			throw new IllegalStateException("Level '" + level + "' not supported");
		}
	}

}
