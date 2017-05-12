using De.Osthus.Ambeth.Helloworld.Service;
using De.Osthus.Ambeth.Ioc;
using De.Osthus.Ambeth.Ioc.Config;
using De.Osthus.Ambeth.Ioc.Factory;
using De.Osthus.Ambeth.Log;
using De.Osthus.Ambeth.Proxy;
using De.Osthus.Ambeth.Service;
using De.Osthus.Ambeth.Threading;
using System;
using System.Threading;
using System.Windows.Threading;

namespace De.Osthus.Ambeth.Test
{
    public class HelloWorldTestModule : IInitializingModule
    {
        public IProxyFactory ProxyFactory { protected get; set; }

        public void AfterPropertiesSet(IBeanContextFactory beanContextFactory)
        {
            IHelloWorldService service = ProxyFactory.CreateProxy<IHelloWorldService>();
            beanContextFactory.RegisterExternalBean("client.helloWorldService", service).Autowireable<IHelloWorldService>();
        }
    }
}