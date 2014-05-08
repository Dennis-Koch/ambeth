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
    public class ClientServiceBean : IFactoryBean, IInitializingBean
    {
        public IClientServiceFactory ClientServiceFactory { get; set; }

        public IClientServiceInterceptorBuilder ClientServiceInterceptorBuilder { get; set; }        

        public IProxyFactory ProxyFactory { get; set; }

        public IServiceContext BeanContext { get; set; }

        public Type Interface { get; set; }

        public Type SyncRemoteInterface { get; set; }

        public Type AsyncRemoteInterface { get; set; }

        public Object proxy;

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(ClientServiceFactory, "ClientServiceFactory");
            ParamChecker.AssertNotNull(ClientServiceInterceptorBuilder, "ClientServiceInterceptorBuilder");
            ParamChecker.AssertNotNull(ProxyFactory, "ProxyFactory");
            ParamChecker.AssertNotNull(BeanContext, "BeanContext");
            ParamChecker.AssertNotNull(Interface, "Interface");

            Init();
        }

        protected void Init(){
                IInterceptor interceptor = ClientServiceInterceptorBuilder.CreateInterceptor(BeanContext, Interface, SyncRemoteInterface, AsyncRemoteInterface);
                proxy = ProxyFactory.CreateProxy(Interface, interceptor);
        }

        public virtual Object GetObject()
        {
            if (proxy == null)
            {
                ParamChecker.AssertNotNull(ClientServiceInterceptorBuilder, "ClientServiceInterceptorBuilder");
                ParamChecker.AssertNotNull(ProxyFactory, "ProxyFactory");
                ParamChecker.AssertNotNull(BeanContext, "BeanContext");
                ParamChecker.AssertNotNull(Interface, "Interface");

                Init();
            }
            return proxy;
        }
    }
}
