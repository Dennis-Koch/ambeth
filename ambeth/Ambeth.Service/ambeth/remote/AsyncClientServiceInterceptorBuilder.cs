using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Log.Interceptor;
using De.Osthus.Ambeth.Ioc.Hierarchy;
using De.Osthus.Ambeth.Service;

#if !SILVERLIGHT
using Castle.DynamicProxy;
#else
using Castle.Core.Interceptor;
#endif
using De.Osthus.Ambeth.Service.Interceptor;
using De.Osthus.Ambeth.Ioc.Annotation;

namespace De.Osthus.Ambeth.Remote
{
    public class AsyncClientServiceInterceptorBuilder : IClientServiceInterceptorBuilder
    {
        [Autowired]
        public IClientServiceFactory ClientServiceFactory { get; set; }

        [Autowired]
        public IProxyFactory ProxyFactory { get; set; }

        public IInterceptor CreateInterceptor(IServiceContext sourceBeanContext, Type syncLocalInterface, Type syncRemoteInterface, Type asyncRemoteInterface)
        {
            ParamChecker.AssertParamNotNull(sourceBeanContext, "sourceBeanContext");
            Type syncInterceptorType = null;
            if (syncRemoteInterface == null)
            {
                syncRemoteInterface = syncLocalInterface;
            }
            else
            {
                syncInterceptorType = ClientServiceFactory.GetSyncInterceptorType(syncRemoteInterface);
            }

            if (asyncRemoteInterface == null)
            {
                asyncRemoteInterface = syncRemoteInterface;
            }

            Type clientProviderType = ClientServiceFactory.GetTargetProviderType(asyncRemoteInterface);

            String serviceName = ClientServiceFactory.GetServiceName(syncRemoteInterface);

            String syncRemoteInterceptorName = "syncRemoteInterceptor";
            String syncCallInterceptorName = "syncCallInterceptor";
            String targetProviderName = "targetProvider";
            String targetingInterceptorName = "targetingInterceptor";
            String asyncProxyName = "asyncProxy";

            IServiceContext childContext = sourceBeanContext.CreateService(delegate(IBeanContextFactory bcf)
            {
                if (typeof(IRemoteTargetProvider).IsAssignableFrom(clientProviderType))
                {
                    bcf.RegisterBean(targetProviderName, clientProviderType).PropertyValue("ServiceName", serviceName);
                    ClientServiceFactory.PostProcessTargetProviderBean(targetProviderName, bcf);

                    //TargetProvider and target have to be set up manually here
                    bcf.RegisterBean<TargetingInterceptor>(targetingInterceptorName).PropertyRef("TargetProvider", targetProviderName);

                    LogInterceptor logInterceptor = (LogInterceptor)bcf.RegisterBean<LogInterceptor>("logInterceptor").PropertyRef("Target", targetingInterceptorName).GetInstance();

                    Object asyncProxy = ProxyFactory.CreateProxy(asyncRemoteInterface, logInterceptor);
                    bcf.RegisterExternalBean(asyncProxyName, asyncProxy);

                    bcf.RegisterBean<SyncCallInterceptor>(syncCallInterceptorName).PropertyRef("AsyncService", asyncProxyName).PropertyValue("AsyncServiceInterface", asyncRemoteInterface);

                    if (syncRemoteInterface != syncLocalInterface)
                    {
                        bcf.RegisterBean(syncRemoteInterceptorName, syncInterceptorType).PropertyValue("WCFInterfaceType", syncRemoteInterface);
                    }
                    else
                    {
                        bcf.RegisterAlias(syncRemoteInterceptorName, syncCallInterceptorName);
                    }
                }
                else
                {
                    throw new Exception("ProviderType '" + clientProviderType + "' is not supported here"); 
                }
            });

            return childContext.GetService<IInterceptor>(syncRemoteInterceptorName);
        }
    }
}
