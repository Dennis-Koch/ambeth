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

namespace De.Osthus.Ambeth.Remote
{
    public class SyncClientServiceInterceptorBuilder : IInitializingBean, IClientServiceInterceptorBuilder
    {
        public IClientServiceFactory ClientServiceFactory { get; set; }

        public IProxyFactory ProxyFactory { get; set; }
        
        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(ClientServiceFactory, "ClientServiceFactory");
            ParamChecker.AssertNotNull(ProxyFactory, "ProxyFactory");
        }

        public virtual IInterceptor CreateInterceptor(IServiceContext sourceBeanContext, Type syncLocalInterface, Type syncRemoteInterface, Type asyncRemoteInterface)
        {
            ParamChecker.AssertParamNotNull(sourceBeanContext, "sourceBeanContext");
            if (syncRemoteInterface == null)
            {
                syncRemoteInterface = syncLocalInterface;
            }
            Type clientProviderType = ClientServiceFactory.GetTargetProviderType(syncRemoteInterface);

            String serviceName = ClientServiceFactory.GetServiceName(syncRemoteInterface);

            String logInterceptorName = "logInterceptor";
            String remoteTargetProviderName = "remoteTargetProvider";
            String interceptorName = "interceptor";

            IServiceContext childContext = sourceBeanContext.CreateService(delegate(IBeanContextFactory bcf)
            {
                if (typeof(IRemoteTargetProvider).IsAssignableFrom(clientProviderType))
                {
                    bcf.RegisterBean(remoteTargetProviderName, clientProviderType).PropertyValue("ServiceName", serviceName);
                    ClientServiceFactory.PostProcessTargetProviderBean(remoteTargetProviderName, bcf);

                    bcf.RegisterBean<TargetingInterceptor>(interceptorName).PropertyRef("TargetProvider", remoteTargetProviderName);
                }
                else if (typeof(IRemoteInterceptor).IsAssignableFrom(clientProviderType))
                {
                    bcf.RegisterBean(interceptorName, clientProviderType).PropertyValue("ServiceName", serviceName);
                    ClientServiceFactory.PostProcessTargetProviderBean(interceptorName, bcf);
                }
                else
                {
                    throw new Exception("ProviderType '" + clientProviderType + "' is not supported here"); 
                }
                bcf.RegisterBean<LogInterceptor>(logInterceptorName).PropertyRef("Target", interceptorName);
            });

            return childContext.GetService<IInterceptor>(logInterceptorName);
        }
    }
}
