package de.osthus.ambeth.log;

public interface IConfigurableLogger extends ILogger
{
	LogSourceLevel getLogSourceLevel();

	void setLogSourceLevel(LogSourceLevel logSourceLevel);

	boolean getLogToConsole();

	void setLogToConsole(boolean logToConsole);

	void setDebugEnabled(boolean debugEnabled);

	void setInfoEnabled(boolean infoEnabled);

	void setWarnEnabled(boolean warnEnabled);

	void setErrorEnabled(boolean errorEnabled);

}
