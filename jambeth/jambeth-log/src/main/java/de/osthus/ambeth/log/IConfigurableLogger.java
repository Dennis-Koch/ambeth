package de.osthus.ambeth.log;

import de.osthus.ambeth.config.IProperties;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;

public interface IConfigurableLogger extends ILogger
{
	LogSourceLevel getLogSourceLevel();

	void setLogSourceLevel(LogSourceLevel logSourceLevel);

	void setObjectCollector(IThreadLocalObjectCollector objectCollector);

	void postProcess(IProperties properties);

	boolean getLogToConsole();

	void setLogToConsole(boolean logToConsole);

	void setDebugEnabled(boolean debugEnabled);

	void setInfoEnabled(boolean infoEnabled);

	void setWarnEnabled(boolean warnEnabled);

	void setErrorEnabled(boolean errorEnabled);
}
