using System;

namespace De.Osthus.Ambeth.Log
{
    public interface ILoggerHistory
    {
        bool AddLogHistory(ILogger logger, Object contextHandle, String logTextForHistory);

        bool DebugOnce(ILogger log, Object contextHandle, String logTextForHistory);

        bool InfoOnce(ILogger log, Object contextHandle, String logTextForHistory);

        bool WarnOnce(ILogger log, Object contextHandle, String logTextForHistory);

        bool ErrorOnce(ILogger log, Object contextHandle, String logTextForHistory);
    }
}