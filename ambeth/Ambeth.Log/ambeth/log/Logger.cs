using System;
using System.ServiceModel;
using System.Threading;
using System.Text;
using System.IO;
using De.Osthus.Ambeth.Config;

namespace De.Osthus.Ambeth.Log
{
    public class Logger : IConfigurableLogger
    {
        static Logger()
        {
            LogStreamEnabled = true;
        }

        protected static StreamWriter loggerStream;

        protected static StreamWriter LoggerStream
        {
            get
            {
                if (loggerStream == null && LogStreamEnabled)
                {
                    String logfile = Properties.Application.GetString(LogConfigurationConstants.LogFile);
                    if (logfile != null)
                    {
                        LoggerStream = new StreamWriter(new FileStream(logfile, AppendModeActive ? FileMode.Append : FileMode.Create, FileAccess.Write, FileShare.Read));
                    }
                }
                return loggerStream;
            }
            set
            {
                if (loggerStream != null)
                {
                    loggerStream.Dispose();
                }
                loggerStream = value;
            }
        }

        public static bool LogStreamEnabled { get; set; }

        public static bool AppendModeActive { get; set; }

        protected static Object lockObject = new Object();

        public bool LogToConsole { get; set; }

        public LogSourceLevel LogSourceLevel { get; set; }

        public bool DebugEnabled { get; set; }

        public bool InfoEnabled { get; set; }

        public bool WarnEnabled { get; set; }

        public bool ErrorEnabled { get; set; }

        private readonly String _source, _shortSource;

        protected String forkName;

        public Logger(string source)
        {
            this._source = source;

            int lastIndexOf = _source.LastIndexOf('.');
            if (lastIndexOf >= 0)
            {
                _shortSource = _source.Substring(lastIndexOf + 1);
            }
            else
            {
                _shortSource = _source;
            }
            DebugEnabled = true;
            InfoEnabled = true;
            WarnEnabled = true;
            ErrorEnabled = true;
        }

        public void PostProcess(IProperties properties)
        {
            forkName = properties.GetString(UtilConfigurationConstants.ForkName);
        }

        public void Info(String message)
        {
            if (!InfoEnabled)
            {
                return;
            }
            AddNotification(LogLevel.INFO, message);
        }

        public void Info(String message, Exception e)
        {
            if (!InfoEnabled)
            {
                return;
            }
            AddNotification(LogLevel.INFO, message, e);
        }

        public void Info(String message, ExceptionDetail e)
        {
            if (!InfoEnabled)
            {
                return;
            }
            AddNotification(LogLevel.INFO, message, e);
        }

        public void Info(Exception e)
        {
            if (!InfoEnabled)
            {
                return;
            }
            AddNotification(LogLevel.INFO, e);
        }

        public void Info(ExceptionDetail e)
        {
            if (!InfoEnabled)
            {
                return;
            }
            AddNotification(LogLevel.INFO, e);
        }

        public void Debug(String message)
        {
            if (!DebugEnabled)
            {
                return;
            }
            AddNotification(LogLevel.DEBUG, message);
        }

        public void Debug(String message, Exception e)
        {
            if (!DebugEnabled)
            {
                return;
            } 
            AddNotification(LogLevel.DEBUG, message, e);
        }

        public void Debug(String message, ExceptionDetail e)
        {
            if (!DebugEnabled)
            {
                return;
            }
            AddNotification(LogLevel.DEBUG, message, e);
        }

        public void Debug(Exception e)
        {
            if (!DebugEnabled)
            {
                return;
            }
            AddNotification(LogLevel.DEBUG, e);
        }

        public void Debug(ExceptionDetail e)
        {
            if (!DebugEnabled)
            {
                return;
            }
            AddNotification(LogLevel.DEBUG, e);
        }

        public void Warn(String message)
        {
            if (!WarnEnabled)
            {
                return;
            }
            AddNotification(LogLevel.WARN, message);
        }

        public void Warn(String message, Exception e)
        {
            if (!WarnEnabled)
            {
                return;
            } 
            AddNotification(LogLevel.WARN, message, e);
        }

        public void Warn(String message, ExceptionDetail e)
        {
            if (!WarnEnabled)
            {
                return;
            }
            AddNotification(LogLevel.WARN, message, e);
        }

        public void Warn(Exception e)
        {
            if (!WarnEnabled)
            {
                return;
            }
            AddNotification(LogLevel.WARN, e);
        }

        public void Warn(ExceptionDetail e)
        {
            if (!WarnEnabled)
            {
                return;
            }
            AddNotification(LogLevel.WARN, e);
        }

        public void Error(String message)
        {
            if (!ErrorEnabled)
            {
                return;
            }
            AddNotification(LogLevel.ERROR, message);
        }

        public void Error(String message, Exception e)
        {
            if (!ErrorEnabled)
            {
                return;
            }
            AddNotification(LogLevel.ERROR, message, e);
        }

        public void Error(String message, ExceptionDetail e)
        {
            if (!ErrorEnabled)
            {
                return;
            }
            AddNotification(LogLevel.ERROR, message, e);
        }

        public void Error(Exception e)
        {
            if (!ErrorEnabled)
            {
                return;
            }
            AddNotification(LogLevel.ERROR, e);
        }

        public void Error(ExceptionDetail e)
        {
            if (!ErrorEnabled)
            {
                return;
            }
            AddNotification(LogLevel.ERROR, e);
        }

        protected virtual void AddNotification(LogLevel level, Exception e)
        {
            AddNotification(level, null, e);
        }

        protected virtual void AddNotification(LogLevel level, ExceptionDetail e)
        {
            AddNotification(level, null, e);
        }

        protected virtual void AddNotification(LogLevel level, String message)
        {
            AddNotification(level, message, (Exception)null);
        }

        protected virtual void AddNotification(LogLevel level, String message, Exception e)
        {
            if (e != null)
            {
                AddNotification(level, message, e.GetType().FullName + ": " + e.Message, ExtractFullStackTrace(e));
            }
            else
            {
                AddNotification(level, message, null, null);
            }
        }

        protected virtual void AddNotification(LogLevel level, String message, ExceptionDetail e)
        {
            if (e != null)
            {
                AddNotification(level, message, e.Message, e.StackTrace);
            }
            else
            {
                AddNotification(level, message, null, null);
            }
        }

        protected String ExtractFullStackTrace(Exception e)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(e.StackTrace);
            Exception innerException = e.InnerException;
            while (innerException != null)
            {
                sb.Append(Environment.NewLine);
                sb.Append(Environment.NewLine);
                sb.Append(innerException.Message);
                sb.Append(Environment.NewLine);
                sb.Append(innerException.StackTrace);
                innerException = innerException.InnerException;
            }
            return sb.ToString();
        }

        protected void AddNotification(LogLevel level, String message, String errorMessage, String stackTrace)
        {
            String notification;
            if (errorMessage != null)
            {
                if (stackTrace != null)
                {
                    notification = message + ": " + errorMessage + Environment.NewLine + stackTrace;
                }
                else
                {
                    notification = message + ": " + errorMessage;
                }
            }
            else
            {
                notification = message;
            }
            CreateLogEntry(level, notification);
        }

        protected virtual void CreateLogEntry(LogLevel logLevel, String notification)
        {
            Thread currentThread = Thread.CurrentThread;

            String threadName = currentThread.Name;
            if (threadName == null || threadName.Length == 0)
            {
                threadName = "<No Name>";
            }
            DateTime now = DateTime.Now;

            String printedSource;
            switch (LogSourceLevel)
            {
                case LogSourceLevel.DEFAULT:
                case LogSourceLevel.FULL:
                    printedSource = _source;
                    break;
                case LogSourceLevel.SHORT:
                    printedSource = _shortSource;
                    break;
                case LogSourceLevel.NONE:
                    printedSource = null;
                    break;
                default:
                    throw new Exception("Enum " + LogSourceLevel + " not supported");
            }
            String dateTimeFraction = now.ToString("yyyy-MM-dd'T'HH:mm:ss.fff");
            String timezoneFraction = now.ToString("zzz").Replace(":", "");
            String output = String.Format("[{6}{4,2}: {5}] [{0}{7}] {1,-5} {2}: {3}", new object[]
            {
                dateTimeFraction, logLevel.ToString(), printedSource, notification, currentThread.ManagedThreadId, threadName, forkName, timezoneFraction
            });
            Log(logLevel, output);
        }

        protected virtual void Log(LogLevel logLevel, String output)
        {
            bool errorLog = LogLevel.WARN.Equals(logLevel) || LogLevel.ERROR.Equals(logLevel);
            if (System.Diagnostics.Debugger.IsAttached)
            {
                System.Diagnostics.Debugger.Log(5, "LOG", output + Environment.NewLine);
            }
            else
            {
                Console.WriteLine(output);
            }
            if (LogStreamEnabled)
            {
                LogStream(logLevel, output, errorLog);
            }
        }

        protected virtual void LogStream(LogLevel logLevel, String output, bool autoFlush)
        {
            lock (lockObject)
            {
                StreamWriter writer = LoggerStream;
                if (writer == null)
                {
                    return;
                }
                writer.Write(Environment.NewLine);
                writer.WriteLine(output);
                if (autoFlush)
                {
                    writer.Flush();
                }
            }
        }
    }
}