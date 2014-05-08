using System;
using System.Threading;
using System.Windows.Threading;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Threading;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Service;

namespace De.Osthus.Ambeth.Cache.Valueholdercontainer
{
    public class ValueHolderContainerTestModule : IInitializingModule, IDisposableBean
    {
        public static Thread dispatcherThread;

        [LogInstance]
        public ILogger Log { private get; set; }

        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            IBeanConfiguration cacheRetrieverConf = beanContextFactory.RegisterAnonymousBean<CacheRetrieverMock>();
            beanContextFactory.Link(cacheRetrieverConf).To<ICacheRetrieverExtendable>().With(typeof(Material));
            beanContextFactory.Link(cacheRetrieverConf).To<ICacheRetrieverExtendable>().With(typeof(MaterialType));

            if (dispatcherThread != null)
            {
                Log.Info("Create SyncContext...");
                CreateDispatcherThread(beanContextFactory);
                Log.Info("SyncContext created");
            }
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

        protected void CreateDispatcherThread(IBeanContextFactory beanContextFactory)
        {
            SynchronizationContext syncContext = null;
            CountDownLatch latch = new CountDownLatch(1);
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
            //dispatcherThread.IsBackground = true;
            dispatcherThread.Start();
            latch.Await();

            //SynchronizationContext.SetSynchronizationContext(syncContext);
            beanContextFactory.RegisterExternalBean(syncContext).Autowireable<SynchronizationContext>();
            beanContextFactory.RegisterExternalBean(new UIThreadWrapper(dispatcherThread)).Autowireable<UIThreadWrapper>();
        }
    }
}