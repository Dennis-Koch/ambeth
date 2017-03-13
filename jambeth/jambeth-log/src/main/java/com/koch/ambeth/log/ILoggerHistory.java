package com.koch.ambeth.log;

public interface ILoggerHistory
{
	boolean addLogHistory(ILogger logger, Object contextHandle, String logTextForHistory);

	boolean debugOnce(ILogger log, Object contextHandle, String logTextForHistory);

	boolean infoOnce(ILogger log, Object contextHandle, String logTextForHistory);

	boolean warnOnce(ILogger log, Object contextHandle, String logTextForHistory);

	boolean errorOnce(ILogger log, Object contextHandle, String logTextForHistory);
}