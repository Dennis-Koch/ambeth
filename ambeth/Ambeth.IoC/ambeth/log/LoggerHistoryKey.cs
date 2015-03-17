using De.Osthus.Ambeth.Collections;
using De.Osthus.Ambeth.Ioc;
using System;

namespace De.Osthus.Ambeth.Log
{
    public class LoggerHistoryKey
    {
        public readonly WeakReference loggerR;

        public readonly WeakReference contextHandleR;

        public readonly String logTextForHistory;

        public readonly int hash;

        public LoggerHistoryKey(WeakReference loggerR, WeakReference contextHandleR, String logTextForHistory)
        {
            this.loggerR = loggerR;
            this.contextHandleR = contextHandleR;
            this.logTextForHistory = logTextForHistory;
            int hash = 11;
            ILogger logger = (ILogger)loggerR.Target;
            if (logger != null)
            {
                hash ^= logger.GetHashCode();
            }
            Object contextHandle = contextHandleR.Target;
            if (contextHandle != null)
            {
                hash ^= contextHandle.GetHashCode();
            }
            this.hash = hash ^ logTextForHistory.GetHashCode();
        }

        public bool IsValid()
        {
            return loggerR.Target != null && contextHandleR.Target != null;
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
            Object contextHandle = this.contextHandleR.Target;
            if (contextHandle == null)
            {
                // Cleanup contextHandle is never equal with anything
                return false;
            }
            ILogger logger = (ILogger)this.loggerR.Target;
            if (logger == null)
            {
                // Cleanup logger is never equal with anything
                return false;
            }
            LoggerHistoryKey other = (LoggerHistoryKey)obj;
            Object contextHandle2 = other.contextHandleR.Target;
            if (contextHandle2 == null)
            {
                // Cleanup contextHandle is never equal with anything
                return false;
            }
            ILogger logger2 = (ILogger)other.loggerR.Target;
            if (logger2 == null)
            {
                // Cleanup logger is never equal with anything
                return false;
            }
            return logTextForHistory.Equals(other.logTextForHistory) && contextHandle.Equals(contextHandle2) && logger.Equals(logger2);
        }
    }
}