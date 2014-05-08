using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc;
using System;

namespace De.Osthus.Ambeth.Log
{
    public class LoggerHistoryKey
    {
        public readonly WeakReference logger;

        public readonly WeakReference contextHandle;

        public readonly String logTextForHistory;

        public readonly int hash;

        public LoggerHistoryKey(WeakReference logger, WeakReference contextHandle, String logTextForHistory)
        {
            this.logger = logger;
            this.contextHandle = contextHandle;
            this.logTextForHistory = logTextForHistory;
            int hash = 11;
            ILogger loggerR = (ILogger)logger.Target;
            if (loggerR != null)
            {
                hash ^= loggerR.GetHashCode();
            }
            Object contextHandleR = contextHandle.Target;
            if (contextHandleR != null)
            {
                hash ^= contextHandleR.GetHashCode();
            }
            this.hash = hash ^ logTextForHistory.GetHashCode();
        }

        public bool IsValid()
        {
            return logger.Target != null && contextHandle.Target != null;
        }

        public override int GetHashCode()
        {
            // Hash MUST be precalculated because of hash requirement for removal after the Refs are null
            // (and therefore the hash would have changed)
            return hash;
        }

        public override bool Equals(Object obj)
        {
            if (obj == this)
            {
                return true;
            }
            if (!(obj is LoggerHistoryKey))
            {
                return false;
            }
            Object contextHandle = this.contextHandle.Target;
            if (contextHandle == null)
            {
                // Cleanup contextHandle is never equal with anything
                return false;
            }
            ILogger logger = (ILogger)this.logger.Target;
            if (logger == null)
            {
                // Cleanup logger is never equal with anything
                return false;
            }
            LoggerHistoryKey other = (LoggerHistoryKey)obj;
            Object contextHandle2 = other.contextHandle.Target;
            if (contextHandle2 == null)
            {
                // Cleanup contextHandle is never equal with anything
                return false;
            }
            ILogger logger2 = (ILogger)other.logger.Target;
            if (logger2 == null)
            {
                // Cleanup logger is never equal with anything
                return false;
            }
            return logTextForHistory.Equals(other.logTextForHistory) && contextHandle.Equals(contextHandle2) && logger.Equals(logger2);
        }
    }

    public class LoggerHistory : IInitializingBean, ILoggerHistory
    {
        protected readonly LinkedHashSet<LoggerHistoryKey> logHistory = new LinkedHashSet<LoggerHistoryKey>(0.5f);

        protected int modCount = 0;

        protected int cleanupModInterval = 1000;

        public void AfterPropertiesSet()
        {
            // Intended blank
        }

        public bool AddLogHistory(ILogger logger, Object contextHandle, String logTextForHistory)
        {
            LoggerHistoryKey key = new LoggerHistoryKey(new WeakReference(logger), new WeakReference(contextHandle), logTextForHistory);
            lock (this)
            {
                if (!logHistory.Add(key))
                {
                    return false;
                }

                if (++modCount % cleanupModInterval == 0)
                {
                    Iterator<LoggerHistoryKey> iter = logHistory.Iterator();
                    while (iter.MoveNext())
                    {
                        LoggerHistoryKey existingKey = iter.Current;
                        if (!existingKey.IsValid())
                        {
                            iter.Remove();
                        }
                    }
                }
                return true;
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