using System;
using System.Net;
using De.Osthus.Ambeth.Security;
using Castle.DynamicProxy;
using System.Threading;
using System.ServiceModel;
using De.Osthus.Ambeth.Service.Interceptor;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Model;

namespace De.Osthus.Ambeth.Service
{
    public class ServiceFactory : IInitializingBean
    {
        [LogInstance]
		public ILogger Log { private get; set; }

        public IServiceContext BeanContext { get; set; }

        public IExceptionHandler ExceptionHandler { get; set; }

        public IProxyFactory ProxyFactory { get; set; }
        
        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(BeanContext, "BeanContext");
            ParamChecker.AssertNotNull(ProxyFactory, "ProxyFactory");
        }

        public SyncInterface GetService<AsyncInterface, SyncInterface>(params ISecurityScope[] securityScopes)
            where AsyncInterface : class, ICommunicationObject
            where SyncInterface : class
        {
            AsyncInterface service = BeanContext.GetService<IServiceFactory>().GetService<AsyncInterface>(securityScopes);

            SyncCallInterceptor synchronizedInterceptor = BeanContext.RegisterAnonymousBean<SyncCallInterceptor>().PropertyValue("AsyncService", service).PropertyValue("AsyncServiceInterface", typeof(AsyncInterface)).Finish();

            return (SyncInterface)ProxyFactory.CreateProxy(typeof(SyncInterface), synchronizedInterceptor);
        }

        public void UseService<AsyncInterface, SyncInterface>(ServiceRunnableDelegate<SyncInterface> syncRunnable, params ISecurityScope[] securityScopes)
            where AsyncInterface : class, ICommunicationObject
            where SyncInterface : class
        {
            SyncInterface syncService = GetService<AsyncInterface, SyncInterface>(securityScopes);
            
            ThreadPool.QueueUserWorkItem(delegate(Object state)
            {
                try
                {
                    syncRunnable.Invoke(syncService);
                }
                catch (Exception e)
                {
                    if (ExceptionHandler != null)
                    {
                        try
                        {
                            ExceptionHandler.HandleException(null, e);
                        }
                        catch (Exception ex)
                        {
                            if (Log.ErrorEnabled)
                            {
                                Log.Error(ex);
                            }
                        }
                    }
                    else if (Log.ErrorEnabled)
                    {
                        Log.Error(e);
                    }
                }
            });
        }

        public delegate void ServiceRunnableDelegate<SyncInterface>(SyncInterface service) where SyncInterface : class;
    }
}
