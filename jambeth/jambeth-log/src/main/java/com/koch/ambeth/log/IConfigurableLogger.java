package com.koch.ambeth.log;

import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.objectcollector.IThreadLocalObjectCollector;

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
