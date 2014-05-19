package de.osthus.ambeth.testutil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.log.LogLevel;
import de.osthus.ambeth.log.Logger;

public class ListLogger extends Logger
{
	protected static final DateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");

	protected final IList<String> debugEntries = new ArrayList<String>(), infoEntries = new ArrayList<String>(), warnEntries = new ArrayList<String>(),
			errorEntries = new ArrayList<String>();

	public ListLogger(String source)
	{
		super(source);
	}
	
	public DateFormat getFormat()
	{
		return format;
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

	protected void log(LogLevel logLevel, boolean errorLog, String output)
	{
		switch (logLevel)
		{
			case INFO:
			{
				infoEntries.add(output);
				break;
			}
			case DEBUG:
			{
				debugEntries.add(output);
				break;
			}
			case WARN:
			{
				warnEntries.add(output);
				break;
			}
			case ERROR:
			{
				errorEntries.add(output);
				break;
			}
			default:
				RuntimeExceptionUtil.createEnumNotSupportedException(logLevel);
		}
	}
}
