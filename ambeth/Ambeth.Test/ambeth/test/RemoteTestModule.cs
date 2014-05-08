using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Threading;
using System;
using System.Threading;
using System.Windows.Threading;

namespace De.Osthus.Ambeth.Test
{
    public class RemoteTestModule : IInitializingModule, IDisposableBean
    {
        public static Thread dispatcherThread;

        [LogInstance]
        public ILogger Log { private get; set; }
        
        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            if (dispatcherThread != null)
            {
                throw new Exception("Module instantiated twice");
            }
            SynchronizationContext syncContext = null;
            CountDownLatch latch = new CountDownLatch(1);
            Log.Info("Create SyncContext...");
            dispatcherThread = new Thread(delegate()
            {
                // Create our context, and install it:
                try
                {
                    syncContext = new DispatcherSynchronizationContext(Dispatcher.CurrentDispatcher);
                    SynchronizationContext.SetSynchronizationContext(syncContext);

                    Log.Info("I am the UI Thread");
                }
                finally
                {
                    latch.CountDown();
                }
                // Start the Dispatcher Processing
                System.Windows.Threading.Dispatcher.Run();
            });
            dispatcherThread.Name = "TestDispatcherThread";
            dispatcherThread.IsBackground = true;
            dispatcherThread.Start();
            latch.Await();
            Log.Info("SyncContext created");
        }

        public void Destroy()
        {
            if (dispatcherThread != null)
            {
                try
                {
                    dispatcherThread.Abort();
                }
                catch (Exception)
                {
                    // Intended blank
                }
                dispatcherThread = null;
            }
        }
    }
}