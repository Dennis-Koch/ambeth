using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc;
using System;

namespace De.Osthus.Ambeth.Log
{
    public class LoggerHistory : IInitializingBean, ILoggerHistory, IInvalidKeyChecker<LoggerHistoryKey>
    {
        protected readonly CleanupInvalidKeysSet<LoggerHistoryKey> logHistory;

        protected readonly Object writeLock = new Object();

        public LoggerHistory()
        {
            logHistory = new CleanupInvalidKeysSet<LoggerHistoryKey>(this, 0.5f);
        }

        public void AfterPropertiesSet()
        {
            // Intended blank
        }

        public bool IsKeyValid(LoggerHistoryKey key)
        {
            return key.IsValid();
        }

        public bool AddLogHistory(ILogger logger, Object contextHandle, String logTextForHistory)
        {
            LoggerHistoryKey key = new LoggerHistoryKey(new WeakReference(logger), new WeakReference(contextHandle), logTextForHistory);
            lock (writeLock)
            {
                return logHistory.Add(key);
            }
        }

        public bool DebugOnce(ILogger log, Object contextHandle, String logTextForHistory)
        {
            if (!AddLogHistory(log, contextHandle, logTextForHistory))
            {
                return false;
            }
            log.Debug(logTextForHistory);
            return true;
        }

        public bool InfoOnce(ILogger log, Object contextHandle, String logTextForHistory)
        {
            if (!AddLogHistory(log, contextHandle, logTextForHistory))
            {
                return false;
            }
            log.Info(logTextForHistory);
            return true;
        }

        public bool WarnOnce(ILogger log, Object contextHandle, String logTextForHistory)
        {
            if (!AddLogHistory(log, contextHandle, logTextForHistory))
            {
                return false;
            }
            log.Warn(logTextForHistory);
            return true;
        }

        public bool ErrorOnce(ILogger log, Object contextHandle, String logTextForHistory)
        {
            if (!AddLogHistory(log, contextHandle, logTextForHistory))
            {
                return false;
            }
            log.Error(logTextForHistory);
            return true;
        }
    }
}