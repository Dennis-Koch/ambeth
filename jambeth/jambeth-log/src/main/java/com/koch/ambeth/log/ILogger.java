package com.koch.ambeth.log;

public interface ILogger
{
	boolean isDebugEnabled();

	boolean isInfoEnabled();

	boolean isWarnEnabled();

	boolean isErrorEnabled();

	void debug(CharSequence message);

	void debug(CharSequence message, Throwable e);

	void debug(Throwable e);

	void info(CharSequence message);

	void info(CharSequence message, Throwable e);

	void info(Throwable e);

	void warn(CharSequence message);

	void warn(CharSequence message, Throwable e);

	void warn(Throwable e);

	void error(CharSequence message);

	void error(CharSequence message, Throwable e);

	void error(Throwable e);

}
