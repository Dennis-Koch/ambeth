using System;
using System.ServiceModel;
using System.Threading;
using System.Text;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Ioc;
using System.IO.IsolatedStorage;
using System.IO;

namespace De.Osthus.Ambeth.Log
{
    public class ClientLogger : Logger
    {
        public static ILogClient LogClient { get; set; }

        static ClientLogger()
        {
            String filename = "silverlight_log.txt";
            if (IsolatedStorageFile.IsEnabled)
            {
                IsolatedStorageFile storage = IsolatedStorageFile.GetUserStoreForSite();

                IsolatedStorageFileStream stream = new IsolatedStorageFileStream(filename, FileMode.Create, FileAccess.Write, FileShare.ReadWrite, storage);

                Logger.LoggerStream = new StreamWriter(stream);
            }
            else
            {
                Logger.LogStreamEnabled = false;
            }
        }

        public ClientLogger(string source) : base(source)
        {
            // Intended blank
        }

        protected override void LogStream(LogLevel logLevel, String output, bool autoFlush)
        {
            bool retryLog = true;
            while (retryLog)
            {
                try
                {
                    base.LogStream(logLevel, output, autoFlush);
                    retryLog = false;
                }
                catch (IsolatedStorageException)
                {
                    IsolatedStorageFile storage = IsolatedStorageFile.GetUserStoreForSite();
                    if (storage.AvailableFreeSpace > 1024 * 100) // 100 kb
                    {
                        throw;
                    }
                    if (!storage.IncreaseQuotaTo(storage.Quota * 5))
                    {
                        // increase by factor 5 failed, so disable logging
                        Logger.LogStreamEnabled = false;
                        retryLog = false;
                    }
                }
            }
        }
    }
}
