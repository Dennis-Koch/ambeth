package de.osthus.ambeth.log;

public interface ILogger
{
	static final String INFO = "INFO";

	static final String DEBUG = "DEBUG";

	static final String WARN = "WARN";

	static final String ERROR = "ERROR";

	boolean isDebugEnabled();

	boolean isInfoEnabled();

	boolean isWarnEnabled();

	boolean isErrorEnabled();

	void debug(String message);

	void debug(String message, Throwable e);

	void debug(Throwable e);

	void info(String message);

	void info(String message, Throwable e);

	void info(Throwable e);

	void warn(String message);

	void warn(String message, Throwable e);

	void warn(Throwable e);

	void error(String message);

	void error(String message, Throwable e);

	void error(Throwable e);

}
