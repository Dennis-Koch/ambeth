using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Util;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Ioc.Hierarchy;
using De.Osthus.Ambeth.Service;

#if !SILVERLIGHT
using Castle.DynamicProxy;
#else
using Castle.Core.Interceptor;
#endif

namespace De.Osthus.Ambeth.Remote
{
    public class InterfaceBean : IFactoryBean, IInitializingBean
    {
        public IProxyFactory ProxyFactory { get; set; }

        public Type Interface { get; set; }

        protected Object proxy;

        public virtual void AfterPropertiesSet()
        {
            ParamChecker.AssertNotNull(ProxyFactory, "ProxyFactory");
            ParamChecker.AssertNotNull(Interface, "Interface");
            
            proxy = ProxyFactory.CreateProxy(Interface);
        }

        public virtual Object GetObject()
        {
            return proxy;
        }
    }
}
